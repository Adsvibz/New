package com.o2dialer.app

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.CallLog
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import java.util.Calendar
import kotlin.random.Random

class DialerService : Service() {

    companion object {
        const val ACT_START = "start"
        const val ACT_STOP = "stop"
        const val ACT_PAUSE = "pause"
        const val ACT_RESUME = "resume"
        const val ACT_SKIP = "skip"
        const val ACT_STAGE_DONE = "stage_done"
        const val ACT_STAGE_HOLD = "stage_hold"

        fun send(ctx: Context, action: String, listId: String? = null, restart: Boolean = false) {
            val i = Intent(ctx, DialerService::class.java).setAction(action)
            if (listId != null) i.putExtra("list", listId)
            i.putExtra("restart", restart)
            if (action == ACT_START) ContextCompat.startForegroundService(ctx, i) else ctx.startService(i)
        }

        /** Stops the running campaign and shows [reason] (used when the license server deactivates the device). */
        fun stopWithReason(ctx: Context, reason: String) {
            val i = Intent(ctx, DialerService::class.java).setAction(ACT_STOP).putExtra("reason", reason)
            runCatching { ctx.startService(i) }
        }
    }

    private val S: Cfg get() = Store.cfg
    private val handler = Handler(Looper.getMainLooper())
    private val queue = kotlin.collections.ArrayDeque<String>()
    private val retriesLeft = HashMap<String, Int>()
    private var listId = ""
    private var curId: String? = null
    private var callStart = 0L
    private var offhookAt = 0L
    private var sawOffhook = false
    private var busy = false
    private var running = false
    private var paused = false
    private var awaiting = false
    private var loop = 1
    private lateinit var tm: TelephonyManager
    private lateinit var telecom: TelecomManager
    private var modernCb: Any? = null
    private var legacyCb: PhoneStateListener? = null
    private var wake: PowerManager.WakeLock? = null

    private val nextRunnable = Runnable { next() }
    private val noCallRunnable = Runnable { onNoCall() }
    private val hangupRunnable = Runnable { hangup() }
    private val audioRunnable = Runnable { if (sawOffhook && S.autoSpeaker) applyAudio(true) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACT_START -> {
                startFg()
                if (!running) startCampaign(
                    intent?.getStringExtra("list") ?: "",
                    intent?.getBooleanExtra("restart", false) ?: false
                )
            }
            ACT_STOP -> if (running) {
                val reason = intent?.getStringExtra("reason")
                finish(reason ?: T("Campaign stopped", "تم إيقاف الحملة"), false, reason != null)
            } else stopSelf()
            ACT_PAUSE -> pause()
            ACT_RESUME -> resume()
            ACT_SKIP -> hangup()
            ACT_STAGE_DONE -> if (running) stageDone() else stopSelf()
            ACT_STAGE_HOLD -> if (running) stageHold()
            else -> stopSelf()
        }
        return START_NOT_STICKY
    }

    // ------------------------------------------------------------ campaign flow
    private fun startCampaign(id: String, restart: Boolean) {
        val first = Store.lists.find { it.id == id }
        if (first == null || first.entries.isEmpty()) {
            finish(T("The campaign is empty", "الحملة فارغة"), false, true); return
        }
        tm = getSystemService(TelephonyManager::class.java)
        telecom = getSystemService(TelecomManager::class.java)
        listId = id; running = true; paused = false; busy = false; awaiting = false; loop = 1
        if (restart) Store.resetForRestart(id, S.skipAnswered)
        val list = Store.lists.find { it.id == id } ?: return
        acquireWake()
        try { register() } catch (e: SecurityException) {
            finish(T("Phone-state permission is missing", "صلاحية حالة الهاتف غير ممنوحة"), false, true); return
        }
        val n = buildQueue(list)
        if (n == 0) { finish(T("Nothing left to call", "لا توجد أرقام متبقية للاتصال"), false); return }
        License.event("campaign_start", "${list.entries.size} contacts")
        val cd = S.startCountdown * 1000L
        upd(CampState(status = CS.RUNNING, listId = id, listName = list.name, loops = S.loops, loop = 1,
            message = T("Starting in", "تبدأ بعد"), nextAt = System.currentTimeMillis() + cd))
        handler.postDelayed(nextRunnable, cd)
    }

    /** Resume semantics: only numbers never called (plus retry candidates). Already-called numbers are skipped. */
    private fun buildQueue(list: CallList): Int {
        queue.clear(); retriesLeft.clear()
        var fresh = list.entries.filter { it.status == St.PENDING || it.status == St.CALLING }.map { it.id }
        if (S.shuffle) fresh = fresh.shuffled()
        queue.addAll(fresh)
        if (S.retries > 0) {
            list.entries
                .filter { (it.status == St.NO_ANSWER || it.status == St.FAILED) && it.attempts - 1 < S.retries }
                .forEach { retriesLeft[it.id] = S.retries - (it.attempts - 1); queue.addLast(it.id) }
        }
        return queue.size
    }

    private fun inWindow(): Boolean {
        if (!S.quietEnabled) return true
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (S.quietFrom <= S.quietTo) h >= S.quietFrom && h < S.quietTo else h >= S.quietFrom || h < S.quietTo
    }

    private fun next() {
        if (!running || paused) return
        if (awaiting) { awaiting = false; Store.stageTarget = null }
        if (queue.isEmpty()) {
            if (loop < S.loops) {
                loop++
                Store.resetForRestart(listId, S.skipAnswered)
                val l = Store.lists.find { it.id == listId }
                val n = if (l != null) buildQueue(l) else 0
                if (n == 0) { finish(T("Campaign completed ✓", "اكتملت الحملة ✓"), true); return }
                upd(Store.camp.copy(loop = loop))
            } else { finish(T("Campaign completed ✓", "اكتملت الحملة ✓"), true); return }
        }
        if (!inWindow()) {
            upd(Store.camp.copy(status = CS.WAITING, nextAt = 0,
                message = T("Outside allowed hours — waiting until ${S.quietFrom}:00", "خارج ساعات السماح — الانتظار حتى ${S.quietFrom}:00")))
            handler.postDelayed(nextRunnable, 30_000)
            return
        }
        place(queue.removeFirst())
    }

    private fun simHandle(slot: Int) =
        if (slot < 0) null else telecom.callCapablePhoneAccounts.getOrNull(slot)

    private fun place(id: String) {
        val list = Store.lists.find { it.id == listId }
        val e = list?.entries?.find { it.id == id }
        // The number was removed meanwhile (for example with the X in Up next): go straight to the next one.
        if (list == null || e == null) { handler.post(nextRunnable); return }
        busy = true; curId = id; sawOffhook = false; callStart = System.currentTimeMillis()
        Store.updateEntry(listId, id) { it.copy(status = St.CALLING, attempts = it.attempts + 1) }
        upd(Store.camp.copy(status = CS.RUNNING, currentName = e.name, currentNumber = e.number,
            message = T("Calling…", "جارٍ الاتصال…"), nextAt = 0, awaitingStage = false))
        try {
            val extras = Bundle()
            runCatching {
                simHandle(list.simSlot)?.let { extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, it) }
            }
            telecom.placeCall(Uri.fromParts("tel", smartNumber(e.number, Store.defaultCc()), null), extras)
            handler.postDelayed(noCallRunnable, 25_000)
        } catch (ex: SecurityException) {
            finish(T("Call permission is missing", "صلاحية الاتصال غير ممنوحة"), false, true)
        }
    }

    private fun onState(state: Int) {
        if (!running) return
        if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
            if (curId != null && !sawOffhook) {
                sawOffhook = true
                offhookAt = System.currentTimeMillis()
                handler.removeCallbacks(noCallRunnable)
                if (S.maxCallSec > 0) handler.postDelayed(hangupRunnable, S.maxCallSec * 1000L)
                if (S.autoSpeaker) {
                    handler.postDelayed(audioRunnable, 400)
                    handler.postDelayed(audioRunnable, 1500)
                    handler.postDelayed(audioRunnable, 3500)
                }
            }
        } else if (state == TelephonyManager.CALL_STATE_IDLE && sawOffhook && curId != null) {
            sawOffhook = false
            handler.removeCallbacks(hangupRunnable)
            handler.removeCallbacks(audioRunnable)
            if (S.autoSpeaker) applyAudio(false)
            val id = curId!!
            curId = null
            val dur = ((System.currentTimeMillis() - offhookAt) / 1000).toInt()
            val since = callStart
            handler.postDelayed({ evaluate(id, since, dur, 0) }, 1500)
        }
    }

    private fun onNoCall() {
        val id = curId ?: return
        if (sawOffhook) return
        curId = null
        record(id, St.FAILED, 0)
    }

    private fun evaluate(id: String, since: Long, dur: Int, tries: Int) {
        if (!running) return
        if (S.useCallLog && has(Manifest.permission.READ_CALL_LOG)) {
            val d = queryLog(since)
            if (d == null && tries < 2) {
                handler.postDelayed({ evaluate(id, since, dur, tries + 1) }, 1500)
                return
            }
            if (d != null) { record(id, if (d > 0) St.ANSWERED else St.NO_ANSWER, d); return }
        }
        record(id, St.DONE, dur)
    }

    private fun queryLog(since: Long): Int? = try {
        contentResolver.query(
            CallLog.Calls.CONTENT_URI, arrayOf(CallLog.Calls.DURATION),
            "${CallLog.Calls.DATE} >= ? AND ${CallLog.Calls.TYPE} = ?",
            arrayOf(since.toString(), CallLog.Calls.OUTGOING_TYPE.toString()),
            "${CallLog.Calls.DATE} ASC"
        )?.use { c -> if (c.moveToFirst()) c.getInt(0) else null }
    } catch (e: Exception) { null }

    private fun record(id: String, status: Int, dur: Int) {
        busy = false
        val list = Store.lists.find { it.id == listId }
        if (list == null) { scheduleNext(); return }
        val e = list.entries.find { it.id == id }
        val started = callStart
        Store.updateEntry(listId, id) { it.copy(status = status, duration = dur, calledAt = started) }
        if (e != null) Store.addLog(LogItem(System.currentTimeMillis(), list.name, e.name, e.number, status, dur, e.attempts))

        // Ask for the call result (stage) after answered calls only.
        if (e != null && S.askStage && St.isAnswered(status) && !(status == St.DONE && dur < 8)) {
            Store.stageTarget = listId to id
            if (S.stageWaitSec > 0) awaiting = true
        }

        val unanswered = status == St.NO_ANSWER || status == St.FAILED
        val left = retriesLeft.getOrPut(id) { S.retries - ((e?.attempts ?: 1) - 1) }
        if (unanswered && left > 0) {
            retriesLeft[id] = left - 1
            queue.addLast(id)
            upd(Store.camp.copy(message = T("Will retry this number later", "سيُعاد الاتصال بهذا الرقم لاحقًا")))
        }
        scheduleNext()
    }

    private fun scheduleNext() {
        if (!running) return
        if (paused) {
            upd(Store.camp.copy(status = CS.PAUSED, message = T("Paused", "متوقفة مؤقتًا"), nextAt = 0)); return
        }
        if (awaiting) {
            val ms = S.stageWaitSec * 1000L
            upd(Store.camp.copy(status = CS.RUNNING, currentName = "", currentNumber = "", awaitingStage = true,
                message = T("Choose the call result", "اختر نتيجة المكالمة"), nextAt = System.currentTimeMillis() + ms))
            handler.postDelayed(nextRunnable, ms)
            return
        }
        if (queue.isEmpty()) { handler.post(nextRunnable); return }
        val lo = S.delaySec
        val hi = maxOf(lo, S.delayMaxSec)
        val sec = if (S.randomDelay) Random.nextInt(lo, hi + 1) else lo
        upd(Store.camp.copy(status = CS.RUNNING, currentName = "", currentNumber = "", awaitingStage = false,
            message = T("Next call in", "الاتصال التالي بعد"), nextAt = System.currentTimeMillis() + sec * 1000L))
        handler.postDelayed(nextRunnable, sec * 1000L)
    }

    /**
     * The user picked a stage that needs a comment: freeze the campaign (no next call, no countdown)
     * until the sheet sends stage_done (Save feedback, another stage, Skip or dismiss).
     */
    private fun stageHold() {
        if (!running || paused || busy) return
        awaiting = true
        handler.removeCallbacks(nextRunnable)
        upd(Store.camp.copy(status = CS.RUNNING, currentName = "", currentNumber = "", awaitingStage = true,
            message = T("Waiting for your feedback", "في انتظار ملاحظاتك"), nextAt = 0))
    }

    private fun stageDone() {
        if (!awaiting) return
        awaiting = false
        handler.removeCallbacks(nextRunnable)
        scheduleNext()
    }

    private fun pause() {
        if (!running || paused) return
        paused = true
        handler.removeCallbacks(nextRunnable)
        upd(Store.camp.copy(status = CS.PAUSED, message = T("Paused", "متوقفة مؤقتًا"), nextAt = 0))
    }

    private fun resume() {
        if (!running || !paused) return
        paused = false
        upd(Store.camp.copy(status = CS.RUNNING, message = T("Resuming…", "استئناف…")))
        if (!busy) handler.postDelayed(nextRunnable, 1000)
    }

    @Suppress("DEPRECATION")
    private fun hangup() {
        try {
            if (Build.VERSION.SDK_INT >= 28 && has(Manifest.permission.ANSWER_PHONE_CALLS)) telecom.endCall()
        } catch (e: Exception) {}
    }

    @Suppress("DEPRECATION")
    private fun applyAudio(on: Boolean) {
        try {
            val am = getSystemService(AudioManager::class.java)
            am.isSpeakerphoneOn = on
            am.isMicrophoneMute = on
        } catch (e: Exception) {}
    }

    private fun finish(msg: String, completed: Boolean = false, error: Boolean = false) {
        License.event("campaign_end", msg)
        if (::telecom.isInitialized && (curId != null || sawOffhook)) hangup()
        running = false; paused = false; busy = false; curId = null; sawOffhook = false; awaiting = false
        handler.removeCallbacksAndMessages(null)
        if (S.autoSpeaker) applyAudio(false)
        unregister(); releaseWake()
        if (listId.isNotEmpty()) {
            Store.updateList(listId) { l ->
                l.copy(entries = l.entries.map { if (it.status == St.CALLING) it.copy(status = St.PENDING) else it })
            }
        }
        Store.camp = Store.camp.copy(status = CS.FINISHED, message = msg, currentName = "", currentNumber = "",
            nextAt = 0, awaitingStage = false, error = error)
        if (completed) {
            Store.resultsListId = listId
            Store.pendingResultsNav = true
            notifyDone()
            if (S.vibrateOnFinish) buzz()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ------------------------------------------------------------ telephony callbacks
    @RequiresApi(31)
    private inner class Cb : TelephonyCallback(), TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) = onState(state)
    }

    @Suppress("DEPRECATION")
    private fun register() {
        unregister()
        if (Build.VERSION.SDK_INT >= 31) {
            val cb = Cb()
            modernCb = cb
            tm.registerTelephonyCallback(mainExecutor, cb)
        } else {
            val l = object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, phoneNumber: String?) { onState(state) }
            }
            legacyCb = l
            tm.listen(l, PhoneStateListener.LISTEN_CALL_STATE)
        }
    }

    @Suppress("DEPRECATION")
    private fun unregister() {
        if (!::tm.isInitialized) return
        try {
            if (Build.VERSION.SDK_INT >= 31) (modernCb as? TelephonyCallback)?.let { tm.unregisterTelephonyCallback(it) }
            else legacyCb?.let { tm.listen(it, PhoneStateListener.LISTEN_NONE) }
        } catch (e: Exception) {}
        modernCb = null; legacyCb = null
    }

    // ------------------------------------------------------------ helpers
    private fun acquireWake() {
        releaseWake()
        wake = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "o2dialer:run").also { it.acquire(8 * 60 * 60 * 1000L) }
    }

    private fun releaseWake() {
        wake?.let { if (it.isHeld) it.release() }
        wake = null
    }

    private fun buzz() {
        try {
            getSystemService(Vibrator::class.java)
                .vibrate(VibrationEffect.createOneShot(600, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {}
    }

    private fun upd(c: CampState) {
        Store.camp = c
        if (running) getSystemService(NotificationManager::class.java).notify(1, notif())
    }

    private fun pi(action: String, req: Int) = PendingIntent.getService(
        this, req, Intent(this, DialerService::class.java).setAction(action),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun openAppIntent() = PendingIntent.getActivity(
        this, 9, Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun notif(): Notification {
        val r = Store.camp
        val p = Store.lists.find { it.id == r.listId }?.let { Store.prog(it) }
        val who = r.currentName.ifEmpty { r.currentNumber }
        val stats = if (p != null) "${p.called}/${p.total} • ${p.pct}%" else ""
        val b = NotificationCompat.Builder(this, "dialer")
            .setSmallIcon(R.drawable.ic_stat_blend)
            .setColor(0xFF6C47FF.toInt())
            .setContentTitle(if (who.isNotEmpty()) T("Calling $who", "جارٍ الاتصال بـ $who") else r.listName)
            .setContentText(if (who.isNotEmpty()) "${r.listName} • $stats" else "${r.message} • $stats")
            .setOngoing(true).setOnlyAlertOnce(true)
            .setProgress(p?.total ?: 0, p?.called ?: 0, false)
            .setContentIntent(openAppIntent())
        if (paused) b.addAction(0, T("Resume", "استئناف"), pi(ACT_RESUME, 1))
        else b.addAction(0, T("Pause", "إيقاف مؤقت"), pi(ACT_PAUSE, 2))
        b.addAction(0, T("Stop", "إيقاف"), pi(ACT_STOP, 3))
        return b.build()
    }

    private fun notifyDone() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("done", "Campaign results", NotificationManager.IMPORTANCE_DEFAULT))
        val l = Store.lists.find { it.id == listId }
        val p = l?.let { Store.prog(it) }
        val text = if (p != null) T(
            "${p.answered} answered • ${p.noAnswer} no answer • ${p.failed} failed",
            "${p.answered} ردّوا • ${p.noAnswer} لم يردّوا • ${p.failed} فشل"
        ) else ""
        nm.notify(2, NotificationCompat.Builder(this, "done")
            .setSmallIcon(R.drawable.ic_stat_blend)
            .setContentTitle(T("Campaign finished ✓", "اكتملت الحملة ✓") + (l?.let { " · ${it.name}" } ?: ""))
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setColor(0xFF6C47FF.toInt())
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build())
    }

    private fun startFg() {
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel("dialer", "Campaign", NotificationManager.IMPORTANCE_LOW))
        val type = if (Build.VERSION.SDK_INT >= 34) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        ServiceCompat.startForeground(this, 1, notif(), type)
    }

    override fun onDestroy() {
        if (running) {
            running = false
            Store.camp = Store.camp.copy(status = CS.FINISHED, message = T("Service stopped", "توقفت الخدمة"))
        }
        handler.removeCallbacksAndMessages(null)
        unregister(); releaseWake()
        super.onDestroy()
    }
}
