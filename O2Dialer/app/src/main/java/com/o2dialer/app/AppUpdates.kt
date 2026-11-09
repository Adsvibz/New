@file:Suppress("DEPRECATION")

package com.o2dialer.app

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** Foreground discovery, user-initiated download, and Android-confirmed self-update only. */
object AppUpdates {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    var available by mutableStateOf<UpdateManifest?>(null); private set
    var checking by mutableStateOf(false); private set
    var downloading by mutableStateOf(false); private set
    var verifying by mutableStateOf(false); private set
    var progress by mutableStateOf(0); private set
    var ready by mutableStateOf(false); private set
    var error by mutableStateOf(false); private set
    var showDialog by mutableStateOf(false); private set
    private var downloadJob: Job? = null
    private var lastAttempt = 0L
    private const val CHECK_INTERVAL = 6 * 60 * 60 * 1000L
    private fun prefs(ctx: Context) = ctx.getSharedPreferences("o2updates", Context.MODE_PRIVATE)
    private fun target(ctx: Context, code: Int) = File(ctx.cacheDir, "updates/update-$code.apk")

    fun check(context: Context, manual: Boolean = false) {
        if (checking || downloading || verifying) return
        val ctx = context.applicationContext
        val now = System.currentTimeMillis()
        val previous = maxOf(lastAttempt, prefs(ctx).getLong("checked", 0L))
        if (!manual && now >= previous && now - previous < CHECK_INTERVAL) return
        lastAttempt = now
        checking = true
        scope.launch {
            try {
                val update = withContext(Dispatchers.IO) {
                    val connection = connect(UpdateManifest.FEED_URL)
                    try {
                        val bytes = connection.inputStream.use { it.readBytesBounded(65536) }
                        UpdateManifest.parse(bytes.toString(Charsets.UTF_8))
                    } finally { connection.disconnect() }
                }
                prefs(ctx).edit().putLong("checked", now).apply()
                if (update.appliesTo(BuildConfig.VERSION_CODE, Build.VERSION.SDK_INT)) {
                    if (available?.versionCode != update.versionCode) ready = false
                    available = update
                    error = false
                    val dismissed = prefs(ctx).getLong("dismissedAt", 0)
                    val suppressed = prefs(ctx).getInt("dismissedVersion", 0) == update.versionCode &&
                        now >= dismissed && now - dismissed < 24 * 60 * 60 * 1000L
                    if (manual || !suppressed) showDialog = true
                } else {
                    available = null
                    ready = false
                    if (manual) Store.notify(Tk.INFO, if (update.versionCode > BuildConfig.VERSION_CODE)
                        T("The new version requires a newer Android version", "التحديث يحتاج إصدار أندرويد أحدث")
                        else T("You're up to date", "أنت تستخدم أحدث إصدار"))
                }
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) {
                // Retry transient/offline failures on a later resume, without hammering the host.
                lastAttempt = now - CHECK_INTERVAL + 15 * 60 * 1000L
                if (manual) Store.notify(Tk.ERROR, T("Couldn't check for updates. Try again later.", "تعذّر فحص التحديثات. حاول لاحقًا."))
            } finally { checking = false }
        }
    }

    fun open() { if (available != null) showDialog = true }

    fun dismiss(ctx: Context) {
        showDialog = false
        available?.let { prefs(ctx).edit().putInt("dismissedVersion", it.versionCode)
            .putLong("dismissedAt", System.currentTimeMillis()).apply() }
    }

    fun download(context: Context) {
        val update = available ?: return
        if (downloading || verifying) return
        val ctx = context.applicationContext
        downloading = true
        ready = false
        error = false
        progress = 0
        downloadJob = scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val file = target(ctx, update.versionCode)
                    file.parentFile!!.mkdirs()
                    // Cache contains only update packages; never touches campaigns or preferences.
                    file.parentFile!!.listFiles()?.forEach { it.delete() }
                    val partial = File(file.parentFile, "download.part")
                    try {
                        val connection = connect(update.apkUrl)
                        try {
                            val declared = connection.contentLengthLong
                            require(declared == -1L || declared == update.size)
                            var count = 0L
                            var lastPercent = -1
                            connection.inputStream.use { input -> partial.outputStream().use { output ->
                                val buffer = ByteArray(32768)
                                while (true) {
                                    currentCoroutineContext().ensureActive()
                                    val n = input.read(buffer)
                                    if (n < 0) break
                                    count += n
                                    require(count <= update.size)
                                    output.write(buffer, 0, n)
                                    val pct = (count * 100 / update.size).toInt()
                                    if (pct != lastPercent) {
                                        lastPercent = pct
                                        withContext(Dispatchers.Main) { progress = pct }
                                    }
                                }
                            } }
                            require(count == update.size)
                        } finally { connection.disconnect() }
                        verify(ctx, partial, update)
                        currentCoroutineContext().ensureActive()
                        kotlin.check(partial.renameTo(file))
                    } finally { partial.delete() }
                }
                ready = true
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) { error = true
            } finally { downloading = false; downloadJob = null }
        }
    }

    fun cancelDownload() { downloadJob?.cancel() }

    fun install(activity: Activity) {
        val update = available ?: return
        if (!ready || verifying || Store.campActive()) return
        if (!activity.packageManager.canRequestPackageInstalls()) {
            runCatching {
                activity.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${activity.packageName}")))
            }.onFailure { Store.notify(Tk.ERROR, T("Open Android settings to allow updates from O2Dialer", "افتح إعدادات أندرويد للسماح بالتحديث من O2Dialer")) }
            return
        }
        verifying = true
        scope.launch {
            try {
                val file = target(activity, update.versionCode)
                withContext(Dispatchers.IO) { verify(activity.applicationContext, file, update) }
                // Recheck after IO: a campaign could have started while validating the file.
                if (Store.campActive() || activity.isFinishing || activity.isDestroyed) return@launch
                Store.flush()
                val uri = FileProvider.getUriForFile(activity, "${activity.packageName}.updates", file)
                val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.clipData = ClipData.newRawUri("O2Dialer update", uri)
                activity.startActivity(intent)
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) {
                ready = false
                error = true
            } finally { verifying = false }
        }
    }

    private fun verify(ctx: Context, file: File, update: UpdateManifest) {
        require(file.isFile && file.length() == update.size)
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(32768)
            while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
        }
        require(digest.digest().joinToString("") { "%02x".format(it) } == update.sha256)
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val pm = ctx.packageManager
        val apk = requireNotNull(pm.getPackageArchiveInfo(file.path, flags))
        val installed = pm.getPackageInfo(ctx.packageName, flags)
        val code = if (Build.VERSION.SDK_INT >= 28) apk.longVersionCode else apk.versionCode.toLong()
        require(apk.packageName == ctx.packageName && code == update.versionCode.toLong() && code > BuildConfig.VERSION_CODE)
        require(matchingUpdateSigners(signers(installed), signers(apk)))
    }

    private fun signers(info: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
        return signatures.orEmpty().map { it.toCharsString() }.toSet()
    }

    /** HTTPS only, bounded redirects, and GitHub's asset hosts only. No credentials are sent. */
    private fun connect(address: String): HttpURLConnection {
        var url = URL(address)
        repeat(6) {
            require(url.protocol == "https" && url.userInfo == null && url.port in listOf(-1, 443))
            require(url.host in setOf("github.com", "release-assets.githubusercontent.com", "objects.githubusercontent.com", "github-releases.githubusercontent.com"))
            val c = url.openConnection() as HttpURLConnection
            c.instanceFollowRedirects = false
            c.connectTimeout = 15000
            c.readTimeout = 20000
            c.useCaches = false
            c.setRequestProperty("User-Agent", "O2Dialer/${BuildConfig.VERSION_CODE}")
            c.setRequestProperty("Accept-Encoding", "identity")
            try {
                val status = c.responseCode
                if (status in listOf(301, 302, 303, 307, 308)) {
                    url = URL(url, requireNotNull(c.getHeaderField("Location")))
                    c.disconnect()
                } else {
                    kotlin.check(status == 200)
                    return c
                }
            } catch (e: Exception) { c.disconnect(); throw e }
        }
        throw IllegalStateException("Too many redirects")
    }

    private fun java.io.InputStream.readBytesBounded(limit: Int): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        while (true) {
            val n = read(buffer)
            if (n < 0) break
            require(output.size() + n <= limit)
            output.write(buffer, 0, n)
        }
        return output.toByteArray()
    }
}
