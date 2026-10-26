package com.o2dialer.app

import android.content.Context
import android.content.SharedPreferences
import android.telephony.TelephonyManager
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Tiny i18n helper: English by default, Arabic when selected in Settings. */
fun T(en: String, ar: String): String = if (Store.cfg.lang == 1) ar else en

object St {
    const val PENDING = 0
    const val CALLING = 1
    const val ANSWERED = 2
    const val NO_ANSWER = 3
    const val FAILED = 4
    const val DONE = 5
    fun isAnswered(s: Int) = s == ANSWERED || s == DONE
    fun label(s: Int): String = when (s) {
        PENDING -> T("Pending", "في الانتظار")
        CALLING -> T("Calling", "جارٍ الاتصال")
        ANSWERED -> T("Answered", "تم الرد")
        NO_ANSWER -> T("No answer", "لم يُرد")
        FAILED -> T("Failed", "فشل")
        else -> T("Completed", "انتهت")
    }
}

/** Feedback kinds shared by banners and toasts. */
object Tk {
    const val INFO = 0
    const val SUCCESS = 1
    const val ERROR = 2
    const val WARN = 3
}

data class UiToast(val kind: Int, val msg: String, val id: Long)

object CS {
    const val IDLE = 0
    const val RUNNING = 1
    const val PAUSED = 2
    const val WAITING = 3
    const val FINISHED = 4
}

object Stg {
    const val NONE = 0
    const val POTENTIAL = 1
    const val NOT_INTERESTED = 2
    const val CALL_BACK = 3
    const val WHATSAPP = 4
    const val VOICEMAIL = 5
    val all = listOf(POTENTIAL, NOT_INTERESTED, CALL_BACK, WHATSAPP, VOICEMAIL)
    fun needsFeedback(s: Int) = s == POTENTIAL || s == CALL_BACK || s == WHATSAPP
    fun label(s: Int): String = when (s) {
        POTENTIAL -> T("Potential", "مهتم")
        NOT_INTERESTED -> T("Not Interested", "غير مهتم")
        CALL_BACK -> T("Call Back", "معاودة الاتصال")
        WHATSAPP -> T("WhatsApp", "واتساب")
        VOICEMAIL -> T("Voicemail", "بريد صوتي")
        else -> "—"
    }
    fun color(s: Int): Color = when (s) {
        POTENTIAL -> Color(0xFF3E7C8C)
        NOT_INTERESTED -> Color(0xFFB0574A)
        CALL_BACK -> Color(0xFFC2913C)
        WHATSAPP -> Color(0xFF2F8F6B)
        VOICEMAIL -> Color(0xFF7C6A9E)
        else -> Color(0xFF8A948C)
    }
}

@Immutable
data class Entry(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val number: String,
    val status: Int = St.PENDING,
    val attempts: Int = 0,
    val duration: Int = 0,
    val calledAt: Long = 0,
    val stage: Int = Stg.NONE,
    val feedback: String = ""
)

@Immutable
data class CallList(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val entries: List<Entry> = emptyList(),
    val simSlot: Int = -1
)

@Immutable
data class LogItem(
    val time: Long, val listName: String, val name: String, val number: String,
    val status: Int, val duration: Int, val attempt: Int
)

data class Prog(val total: Int, val called: Int, val answered: Int, val noAnswer: Int, val failed: Int) {
    val remaining: Int get() = total - called
    val pct: Int get() = if (total == 0) 0 else called * 100 / total
}

data class CampState(
    val status: Int = CS.IDLE,
    val listId: String = "",
    val listName: String = "",
    val currentName: String = "",
    val currentNumber: String = "",
    val message: String = "",
    val loop: Int = 1,
    val loops: Int = 1,
    val nextAt: Long = 0,
    val awaitingStage: Boolean = false,
    val error: Boolean = false
)

data class Cfg(
    val lang: Int = 0,
    val delaySec: Int = 5,
    val randomDelay: Boolean = false,
    val delayMaxSec: Int = 15,
    val startCountdown: Int = 3,
    val maxCallSec: Int = 0,
    val retries: Int = 0,
    val loops: Int = 1,
    val shuffle: Boolean = false,
    val skipAnswered: Boolean = true,
    val useCallLog: Boolean = true,
    val countryCode: String = "",
    val autoSpeaker: Boolean = true,
    val askStage: Boolean = true,
    val stageWaitSec: Int = 15,
    val quietEnabled: Boolean = false,
    val quietFrom: Int = 9,
    val quietTo: Int = 21,
    val keepScreenOn: Boolean = true,
    val vibrateOnFinish: Boolean = true,
    val themeMode: Int = 0,
    val dynamicColor: Boolean = false
) {
    fun toJson(): JSONObject = JSONObject()
        .put("lang", lang).put("delaySec", delaySec).put("randomDelay", randomDelay)
        .put("delayMaxSec", delayMaxSec).put("startCountdown", startCountdown).put("maxCallSec", maxCallSec)
        .put("retries", retries).put("loops", loops).put("shuffle", shuffle)
        .put("skipAnswered", skipAnswered).put("useCallLog", useCallLog).put("countryCode", countryCode)
        .put("autoSpeaker", autoSpeaker).put("askStage", askStage).put("stageWaitSec", stageWaitSec)
        .put("quietEnabled", quietEnabled).put("quietFrom", quietFrom).put("quietTo", quietTo)
        .put("keepScreenOn", keepScreenOn).put("vibrateOnFinish", vibrateOnFinish)
        .put("themeMode", themeMode).put("dynamicColor", dynamicColor)

    companion object {
        fun fromJson(o: JSONObject): Cfg {
            val d = Cfg()
            return Cfg(
                lang = o.optInt("lang", d.lang),
                delaySec = o.optInt("delaySec", d.delaySec),
                randomDelay = o.optBoolean("randomDelay", d.randomDelay),
                delayMaxSec = o.optInt("delayMaxSec", d.delayMaxSec),
                startCountdown = o.optInt("startCountdown", d.startCountdown),
                maxCallSec = o.optInt("maxCallSec", d.maxCallSec),
                retries = o.optInt("retries", d.retries),
                loops = o.optInt("loops", d.loops),
                shuffle = o.optBoolean("shuffle", d.shuffle),
                skipAnswered = o.optBoolean("skipAnswered", d.skipAnswered),
                useCallLog = o.optBoolean("useCallLog", d.useCallLog),
                countryCode = o.optString("countryCode", d.countryCode),
                autoSpeaker = o.optBoolean("autoSpeaker", d.autoSpeaker),
                askStage = o.optBoolean("askStage", d.askStage),
                stageWaitSec = o.optInt("stageWaitSec", d.stageWaitSec),
                quietEnabled = o.optBoolean("quietEnabled", d.quietEnabled),
                quietFrom = o.optInt("quietFrom", d.quietFrom),
                quietTo = o.optInt("quietTo", d.quietTo),
                keepScreenOn = o.optBoolean("keepScreenOn", d.keepScreenOn),
                vibrateOnFinish = o.optBoolean("vibrateOnFinish", d.vibrateOnFinish),
                themeMode = o.optInt("themeMode", d.themeMode),
                dynamicColor = o.optBoolean("dynamicColor", d.dynamicColor)
            )
        }
    }
}

object Store {
    private lateinit var sp: SharedPreferences
    private var simCc = ""

    val lists = mutableStateListOf<CallList>()
    val log = mutableStateListOf<LogItem>()
    var cfg by mutableStateOf(Cfg())
    /** False only on a fresh install: shows the welcome screen once. */
    var welcomed by mutableStateOf(true)
    var camp by mutableStateOf(CampState())
    var selectedListId by mutableStateOf<String?>(null)
    var runListId by mutableStateOf<String?>(null)
    var resultsListId by mutableStateOf<String?>(null)
    var stageTarget by mutableStateOf<Pair<String, String>?>(null)
    var pendingResultsNav by mutableStateOf(false)
    var tab by mutableIntStateOf(0)
    var prevTab = 0
    var toast by mutableStateOf<UiToast?>(null)
    var permTick by mutableIntStateOf(0)

    fun notify(kind: Int, msg: String) { toast = UiToast(kind, msg, System.nanoTime()) }

    fun openSettings() { if (tab != 4) { prevTab = tab; tab = 4 } }

    fun campActive(): Boolean =
        camp.status == CS.RUNNING || camp.status == CS.PAUSED || camp.status == CS.WAITING

    /** Country code used to fix local numbers: user setting, else guessed from the SIM country. */
    fun defaultCc(): String = cfg.countryCode.filter { it.isDigit() }.ifEmpty { simCc }

    fun init(ctx: Context) {
        if (::sp.isInitialized) return
        sp = ctx.applicationContext.getSharedPreferences("o2dialer", Context.MODE_PRIVATE)
        runCatching {
            val iso = (ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).simCountryIso ?: ""
            simCc = isoCc[iso.lowercase()] ?: ""
        }
        welcomed = sp.getBoolean("welcomed", sp.contains("cfg") || sp.contains("lists") || sp.contains("log"))
        runCatching { sp.getString("cfg", null)?.let { cfg = Cfg.fromJson(JSONObject(it)) } }
        runCatching {
            sp.getString("lists", null)?.let { s ->
                val a = JSONArray(s)
                for (i in 0 until a.length()) {
                    val o = a.getJSONObject(i)
                    val ea = o.getJSONArray("entries")
                    val es = (0 until ea.length()).map { k ->
                        val e = ea.getJSONObject(k)
                        val st = e.optInt("status")
                        Entry(
                            id = e.getString("id"), name = e.optString("name"), number = e.getString("number"),
                            status = if (st == St.CALLING) St.PENDING else st,
                            attempts = e.optInt("attempts"), duration = e.optInt("duration"),
                            calledAt = e.optLong("calledAt"), stage = e.optInt("stage"),
                            feedback = e.optString("feedback")
                        )
                    }
                    lists.add(CallList(o.getString("id"), o.getString("name"), es, o.optInt("sim", -1)))
                }
            }
        }
        runCatching {
            sp.getString("log", null)?.let { s ->
                val a = JSONArray(s)
                for (i in 0 until a.length()) {
                    val o = a.getJSONObject(i)
                    log.add(LogItem(o.getLong("t"), o.optString("l"), o.optString("n"), o.optString("p"),
                        o.optInt("s"), o.optInt("d"), o.optInt("a")))
                }
            }
        }
    }

    fun finishWelcome() {
        welcomed = true
        sp.edit().putBoolean("welcomed", true).apply()
    }

    fun updateCfg(f: (Cfg) -> Cfg) {
        cfg = f(cfg)
        sp.edit().putString("cfg", cfg.toJson().toString()).apply()
    }

    // Saving is debounced and runs on one background thread, so taps and list scrolling never wait for disk or JSON.
    private val saveDispatcher = java.util.concurrent.Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val saveScope = CoroutineScope(SupervisorJob() + saveDispatcher)
    private var listsJob: Job? = null
    private var logJob: Job? = null
    @Volatile private var listsDirty = false
    @Volatile private var logDirty = false

    private fun saveLists() {
        val snap = lists.toList()                 // cheap copy on the main thread; entries are immutable
        listsDirty = true
        listsJob?.cancel()
        listsJob = saveScope.launch { delay(400); writeLists(snap) }
    }

    private fun saveLog() {
        val snap = log.toList()
        logDirty = true
        logJob?.cancel()
        logJob = saveScope.launch { delay(400); writeLog(snap) }
    }

    /** Writes anything still pending right now. Called when the app goes to the background. */
    fun flush() {
        val needLists = listsDirty || listsJob?.isActive == true
        val needLog = logDirty || logJob?.isActive == true
        listsJob?.cancel(); logJob?.cancel()
        if (needLists) writeLists(lists.toList())
        if (needLog) writeLog(log.toList())
    }

    private fun writeLists(snap: List<CallList>) {
        val a = JSONArray()
        snap.forEach { l ->
            val ea = JSONArray()
            l.entries.forEach { e ->
                ea.put(JSONObject().put("id", e.id).put("name", e.name).put("number", e.number)
                    .put("status", e.status).put("attempts", e.attempts).put("duration", e.duration)
                    .put("calledAt", e.calledAt).put("stage", e.stage).put("feedback", e.feedback))
            }
            a.put(JSONObject().put("id", l.id).put("name", l.name).put("sim", l.simSlot).put("entries", ea))
        }
        sp.edit().putString("lists", a.toString()).apply()
        listsDirty = false
    }

    private fun writeLog(snap: List<LogItem>) {
        val a = JSONArray()
        snap.forEach {
            a.put(JSONObject().put("t", it.time).put("l", it.listName).put("n", it.name)
                .put("p", it.number).put("s", it.status).put("d", it.duration).put("a", it.attempt))
        }
        sp.edit().putString("log", a.toString()).apply()
        logDirty = false
    }

    fun addList(name: String, simSlot: Int): String {
        val l = CallList(name = name, simSlot = simSlot)
        lists.add(l); saveLists(); return l.id
    }

    fun deleteList(id: String) { lists.removeAll { it.id == id }; saveLists() }

    fun updateList(id: String, f: (CallList) -> CallList) {
        val i = lists.indexOfFirst { it.id == id }
        if (i >= 0) { lists[i] = f(lists[i]); saveLists() }
    }

    fun updateEntry(listId: String, entryId: String, f: (Entry) -> Entry) =
        updateList(listId) { l -> l.copy(entries = l.entries.map { if (it.id == entryId) f(it) else it }) }

    fun deleteEntry(listId: String, entryId: String) =
        updateList(listId) { l -> l.copy(entries = l.entries.filter { it.id != entryId }) }

    fun editEntry(listId: String, entryId: String, name: String, number: String) =
        updateEntry(listId, entryId) { it.copy(name = name.trim(), number = smartNumber(number, defaultCc())) }

    fun setStage(listId: String, entryId: String, stage: Int, feedback: String) =
        updateEntry(listId, entryId) { it.copy(stage = stage, feedback = feedback) }

    fun addEntries(listId: String, items: List<Pair<String, String>>): Int {
        var added = 0
        val cc = defaultCc()
        updateList(listId) { l ->
            val seen = l.entries.map { it.number }.toMutableSet()
            val fresh = items.mapNotNull { (n, num) ->
                val c = smartNumber(num, cc)
                if (c.filter { it.isDigit() }.length < 3 || !seen.add(c)) null else Entry(name = n.trim(), number = c)
            }
            added = fresh.size
            l.copy(entries = l.entries + fresh)
        }
        return added
    }

    fun dedupe(listId: String): Int {
        var removed = 0
        updateList(listId) { l ->
            val seen = HashSet<String>()
            val out = l.entries.filter { seen.add(it.number) }
            removed = l.entries.size - out.size
            l.copy(entries = out)
        }
        return removed
    }

    /** Back to "not called yet". Answered numbers can be kept (Skip answered setting). Stages are kept. */
    fun resetForRestart(listId: String, keepAnswered: Boolean) = updateList(listId) { l ->
        l.copy(entries = l.entries.map {
            if (keepAnswered && St.isAnswered(it.status)) it
            else it.copy(status = St.PENDING, attempts = 0, duration = 0, calledAt = 0)
        })
    }

    fun resetAll(listId: String) = updateList(listId) { l ->
        l.copy(entries = l.entries.map {
            it.copy(status = St.PENDING, attempts = 0, duration = 0, calledAt = 0, stage = Stg.NONE, feedback = "")
        })
    }

    fun prog(l: CallList): Prog {
        val called = l.entries.count { it.status != St.PENDING && it.status != St.CALLING }
        return Prog(
            total = l.entries.size, called = called,
            answered = l.entries.count { St.isAnswered(it.status) },
            noAnswer = l.entries.count { it.status == St.NO_ANSWER },
            failed = l.entries.count { it.status == St.FAILED }
        )
    }

    fun addLog(i: LogItem) {
        log.add(0, i)
        while (log.size > 1000) log.removeAt(log.lastIndex)
        saveLog()
    }

    fun clearLog() { log.clear(); saveLog() }

    private fun csv(s: String) = "\"" + s.replace("\"", "\"\"") + "\""

    fun listReport(l: CallList): String =
        "Name,Number,Status,Duration(s),Called at,Stage,Feedback\n" + l.entries.joinToString("\n") {
            listOf(csv(it.name), it.number, St.label(it.status), it.duration.toString(),
                if (it.calledAt > 0) fmtTime(it.calledAt) else "", if (it.stage > 0) Stg.label(it.stage) else "",
                csv(it.feedback)).joinToString(",")
        }

    fun logReport(items: List<LogItem>): String =
        "Time,Campaign,Name,Number,Status,Duration(s),Attempt\n" + items.joinToString("\n") {
            "${fmtTime(it.time)},${csv(it.listName)},${csv(it.name)},${it.number},${St.label(it.status)},${it.duration},${it.attempt}"
        }
}
