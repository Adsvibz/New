package com.o2dialer.app

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Calendar
import java.util.UUID

/**
 * Optional device licensing against the O2Dialer license server (see /server).
 *
 * - Off by default: when BuildConfig.LICENSE_URL is empty the app behaves exactly as before.
 * - On: a device must be activated with an admin-issued code; before every campaign start the device is checked
 *   online. A suspended device, or an offline device without an offline allowance, cannot start campaigns.
 * - A running campaign is never interrupted by a lost connection; it is only stopped by the server (suspend/remove).
 */
object License {
    enum class Gate { ALLOWED, NEED_ACTIVATION, DENIED, OFFLINE_BLOCKED }

    class Policy(
        val canStart: Boolean = true,
        val canImport: Boolean = true,
        val canExport: Boolean = true,
        val offlineHours: Int = 0
    )

    private sealed class Net {
        class Ok(val j: JSONObject) : Net()
        class Denied(val error: String) : Net()
        object Unauthorized : Net()
        object Offline : Net()
    }

    private class Resp(val code: Int, val json: JSONObject?)

    val serverUrl: String get() = BuildConfig.LICENSE_URL.trim().trimEnd('/')
    val enabled: Boolean get() = serverUrl.startsWith("https://")

    // ---- observable state (drives banners and the settings section)
    var activated by mutableStateOf(false); private set
    var status by mutableStateOf("unknown"); private set      // active | suspended | unknown
    var policy by mutableStateOf(Policy()); private set
    var lastOkAt by mutableStateOf(0L); private set
    var message by mutableStateOf(""); private set
    var checking by mutableStateOf(false); private set
    var showActivation by mutableStateOf(false)
    var pendingStart: Pair<String, Boolean>? = null

    val canImport: Boolean get() = !enabled || !activated || policy.canImport
    val canExport: Boolean get() = !enabled || !activated || policy.canExport

    private lateinit var app: Context
    private lateinit var sp: SharedPreferences
    private var token = ""
    private var deviceId = ""
    private var intervalMs = 300_000L
    private var loopStarted = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun init(ctx: Context) {
        app = ctx.applicationContext
        sp = app.getSharedPreferences("o2license", Context.MODE_PRIVATE)
        token = sp.getString("token", "") ?: ""
        deviceId = sp.getString("device_id", "") ?: ""
        activated = token.isNotEmpty()
        status = sp.getString("status", "unknown") ?: "unknown"
        lastOkAt = sp.getLong("last_ok", 0L)
        policy = Policy(
            sp.getBoolean("can_start", true), sp.getBoolean("can_import", true),
            sp.getBoolean("can_export", true), sp.getInt("offline_hours", 0)
        )
        intervalMs = sp.getLong("hb_ms", 300_000L).coerceIn(60_000L, 3_600_000L)
        if (enabled && activated) startLoop()
    }

    /** Stable, privacy-friendly device identifier: a hash of the app-scoped Android ID. */
    private fun uid(): String {
        sp.getString("uid", null)?.let { return it }
        val raw = runCatching { Settings.Secure.getString(app.contentResolver, Settings.Secure.ANDROID_ID) }.getOrNull()
        val base = if (raw.isNullOrBlank()) UUID.randomUUID().toString() else raw
        val hash = MessageDigest.getInstance("SHA-256").digest(("o2dialer:" + base).toByteArray())
            .joinToString("") { "%02x".format(it) }.take(32)
        sp.edit().putString("uid", hash).apply()
        return hash
    }

    fun shortId(): String = (if (deviceId.isNotEmpty()) deviceId else uid()).take(12)

    fun serverHost(): String = runCatching { URL(serverUrl).host }.getOrDefault(serverUrl)

    fun denyImport() = Store.notify(Tk.ERROR, T("Importing numbers is disabled for this device", "استيراد الأرقام غير مسموح لهذا الجهاز"))
    fun denyExport() = Store.notify(Tk.ERROR, T("Sharing is disabled for this device", "المشاركة غير مسموحة لهذا الجهاز"))

    // ---------------------------------------------------------------- network
    private fun post(path: String, body: JSONObject, auth: String?): Resp {
        val conn = URL(serverUrl + path).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            if (auth != null) conn.setRequestProperty("Authorization", "Bearer $auth")
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            return Resp(code, runCatching { JSONObject(text) }.getOrNull())
        } finally {
            conn.disconnect()
        }
    }

    private suspend fun call(path: String, body: JSONObject, auth: String?): Net = withContext(Dispatchers.IO) {
        try {
            val r = post(path, body, auth)
            val j = r.json
            when {
                r.code in 200..299 && j != null -> Net.Ok(j)
                r.code == 401 -> Net.Unauthorized
                j != null && r.code in 400..499 -> Net.Denied(j.optString("error", "error"))
                else -> Net.Offline          // 5xx, HTML error page, no body: treat as "server not reachable"
            }
        } catch (e: Exception) {
            Net.Offline
        }
    }

    /** Must run on the main thread (reads Compose state). */
    private fun stateBody(): JSONObject {
        val c = Store.camp
        val state = when (c.status) {
            CS.RUNNING -> "running"
            CS.PAUSED -> "paused"
            CS.WAITING -> "waiting"
            else -> "idle"
        }
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        val today = Store.log.filter { it.time >= startOfDay }
        val list = Store.lists.find { it.id == c.listId }
        val p = if (Store.campActive() && list != null) Store.prog(list) else null
        return JSONObject()
            .put("app", APP_VERSION)
            .put("state", state)
            .put("called", p?.called ?: 0)
            .put("total", p?.total ?: 0)
            .put("calls_today", today.size)
            .put("answered_today", today.count { St.isConfirmedAnswered(it.status) })
            .put("campaigns", Store.lists.size)
    }

    /** Main thread only. */
    private fun applyPolicy(j: JSONObject) {
        val st = j.optString("status", "unknown")
        val p = Policy(
            j.optBoolean("can_start", true), j.optBoolean("can_import", true),
            j.optBoolean("can_export", true), j.optInt("offline_hours", 0)
        )
        val hb = (j.optInt("heartbeat_sec", 300) * 1000L).coerceIn(60_000L, 3_600_000L)
        val now = System.currentTimeMillis()
        sp.edit().putString("status", st).putBoolean("can_start", p.canStart).putBoolean("can_import", p.canImport)
            .putBoolean("can_export", p.canExport).putInt("offline_hours", p.offlineHours)
            .putLong("last_ok", now).putLong("hb_ms", hb).apply()
        status = st; policy = p; lastOkAt = now; intervalMs = hb
    }

    private fun forget() {
        token = ""; deviceId = ""
        sp.edit().remove("token").remove("device_id").putString("status", "unknown").apply()
        activated = false; status = "unknown"
    }

    private fun offlineOk(): Boolean {
        val p = policy
        if (!p.canStart || status != "active" || p.offlineHours <= 0) return false
        val now = System.currentTimeMillis()
        if (lastOkAt <= 0L || now < lastOkAt) return false          // never verified, or the clock was moved back
        return p.offlineHours >= 9999 || now - lastOkAt <= p.offlineHours * 3_600_000L
    }

    // ---------------------------------------------------------------- public actions (call from the main thread)
    fun activate(code: String, onDone: (Boolean, String) -> Unit) {
        if (!enabled) { onDone(false, T("Licensing is not enabled in this build", "الترخيص غير مفعّل في هذه النسخة")); return }
        checking = true
        val body = JSONObject()
            .put("code", code.trim())
            .put("uid", uid())
            .put("name", "${Build.MANUFACTURER} ${Build.MODEL}".trim())
            .put("model", Build.MODEL ?: "")
            .put("os", Build.VERSION.RELEASE ?: "")
            .put("app", APP_VERSION)
        scope.launch {
            val r = call("/api/activate", body, null)
            withContext(Dispatchers.Main) {
                checking = false
                when (r) {
                    is Net.Ok -> {
                        token = r.j.optString("token")
                        deviceId = r.j.optString("device_id")
                        sp.edit().putString("token", token).putString("device_id", deviceId).apply()
                        activated = token.isNotEmpty()
                        r.j.optJSONObject("policy")?.let { applyPolicy(it) }
                        message = ""
                        startLoop()
                        onDone(true, T("Device activated", "تم تفعيل الجهاز"))
                    }
                    is Net.Denied -> onDone(false, when (r.error) {
                        "invalid_code" -> T("Invalid or expired code", "الكود غير صحيح أو منتهي")
                        "code_used" -> T("This code has already been used the maximum number of times", "هذا الكود استُخدم بالحد الأقصى")
                        "suspended" -> T("This device has been deactivated by the administrator", "هذا الجهاز موقوف من المسؤول")
                        else -> T("Activation was refused", "تم رفض التفعيل")
                    })
                    is Net.Unauthorized -> onDone(false, T("Activation was refused", "تم رفض التفعيل"))
                    Net.Offline -> onDone(false, T("Could not reach the server. Check your internet connection.", "تعذّر الوصول للسيرفر. تحقق من الإنترنت."))
                }
            }
        }
    }

    /** Checks the device online. Called right before a campaign starts. */
    fun gate(onResult: (Gate, String) -> Unit) {
        if (!enabled) { onResult(Gate.ALLOWED, ""); return }
        if (!activated) {
            onResult(Gate.NEED_ACTIVATION, T("Activate this device first", "فعّل هذا الجهاز أولًا"))
            return
        }
        checking = true
        val body = stateBody()
        val t = token
        scope.launch {
            val r = call("/api/validate", body, t)
            withContext(Dispatchers.Main) {
                checking = false
                when (r) {
                    is Net.Ok -> {
                        r.j.optJSONObject("policy")?.let { applyPolicy(it) }
                        if (r.j.optBoolean("ok", false)) {
                            message = ""
                            onResult(Gate.ALLOWED, "")
                        } else {
                            val m = if (status == "suspended")
                                T("This device has been deactivated by the administrator", "هذا الجهاز موقوف من المسؤول")
                            else
                                T("Starting campaigns is not allowed for this device", "بدء الحملات غير مسموح لهذا الجهاز")
                            message = m
                            onResult(Gate.DENIED, m)
                        }
                    }
                    is Net.Unauthorized -> {
                        forget()
                        onResult(Gate.NEED_ACTIVATION, T("This device is no longer registered. Enter a new activation code.", "هذا الجهاز لم يعد مسجلًا. أدخل كود تفعيل جديد."))
                    }
                    else -> {
                        if (offlineOk()) {
                            onResult(Gate.ALLOWED, "")
                        } else {
                            val m = T("Internet is required to verify this device before starting a campaign",
                                "الإنترنت مطلوب للتحقق من الجهاز قبل بدء الحملة")
                            message = m
                            onResult(Gate.OFFLINE_BLOCKED, m)
                        }
                    }
                }
            }
        }
    }

    /** Settings > Check now. */
    fun check(onDone: (Boolean, String) -> Unit) = gate { g, m ->
        onDone(g == Gate.ALLOWED, if (g == Gate.ALLOWED) T("Device is active", "الجهاز مفعّل") else m)
    }

    /** Fire-and-forget activity report (never contains phone numbers or names). */
    fun event(type: String, detail: String = "") {
        if (!enabled || token.isEmpty()) return
        val t = token
        scope.launch { call("/api/event", JSONObject().put("type", type).put("detail", detail), t) }
    }

    // ---------------------------------------------------------------- heartbeat
    private fun startLoop() {
        if (loopStarted) return
        loopStarted = true
        scope.launch {
            delay(5_000)
            while (isActive) {
                if (enabled && token.isNotEmpty()) beatOnce()
                delay(intervalMs)
            }
        }
    }

    private suspend fun beatOnce() {
        val body = withContext(Dispatchers.Main) { stateBody() }
        val t = token
        val r = call("/api/heartbeat", body, t)
        withContext(Dispatchers.Main) {
            when (r) {
                is Net.Ok -> {
                    r.j.optJSONObject("policy")?.let { applyPolicy(it) }
                    if ((status == "suspended" || !policy.canStart) && Store.campActive()) {
                        DialerService.stopWithReason(app, T("Stopped: this device was deactivated by the administrator",
                            "تم الإيقاف: الجهاز موقوف من المسؤول"))
                    }
                }
                is Net.Unauthorized -> {
                    forget()
                    if (Store.campActive()) {
                        DialerService.stopWithReason(app, T("Stopped: this device is no longer registered", "تم الإيقاف: الجهاز لم يعد مسجلًا"))
                    }
                }
                else -> Unit                      // offline: a running campaign simply continues
            }
        }
    }
}
