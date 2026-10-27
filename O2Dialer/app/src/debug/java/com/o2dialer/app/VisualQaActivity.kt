package com.o2dialer.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * Debug-only, deterministic screen states used by screenshot QA.
 * This source set is excluded from release builds and never writes fixture data to Store.
 */
class VisualQaActivity : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scenario = intent.getStringExtra("scenario").orEmpty().ifBlank { "workspace" }
        val language = intent.getIntExtra("lang", 0).coerceIn(0, 1)
        val theme = intent.getIntExtra("theme", 1).coerceIn(1, 2)
        configureFixture(scenario, language, theme)

        setContent {
            val direction = if (Store.cfg.lang == 1) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                AppTheme {
                    MotionPreferences {
                        if (scenario.startsWith("welcome")) {
                            WelcomeScreen(scenario.removePrefix("welcome").toIntOrNull()?.minus(1) ?: 0)
                        } else {
                            Root(this@VisualQaActivity)
                        }
                    }
                }
            }
        }
    }

    private fun configureFixture(scenario: String, language: Int, theme: Int) {
        Store.welcomed = !scenario.startsWith("welcome")
        Store.cfg = Cfg(
            lang = language,
            themeMode = theme,
            delaySec = 12,
            delayMaxSec = 28,
            randomDelay = true,
            retries = 1,
            loops = 2,
            dynamicColor = false
        )
        Store.lists.clear()
        Store.log.clear()
        Store.camp = CampState()
        Store.selectedListId = null
        Store.runListId = null
        Store.resultsListId = null
        Store.stageTarget = null
        Store.pendingResultsNav = false
        Store.toast = null

        val now = System.currentTimeMillis()
        val east = CallList(
            id = "qa-east",
            name = if (language == 1) "عملاء سوديك إيست" else "SODIC East leads",
            entries = listOf(
                Entry(id = "e1", name = if (language == 1) "أحمد مصطفى" else "Ahmed Mostafa", number = "+20 100 555 0123", status = St.ANSWERED, attempts = 1, duration = 84, calledAt = now - 180_000, stage = Stg.POTENTIAL, feedback = "Send details"),
                Entry(id = "e2", name = if (language == 1) "سارة محمود" else "Sara Mahmoud", number = "+20 111 420 7712", status = St.NO_ANSWER, attempts = 1, calledAt = now - 360_000),
                Entry(id = "e3", name = if (language == 1) "كريم علي" else "Karim Ali", number = "+20 122 808 4401", status = St.ANSWERED, attempts = 2, duration = 137, calledAt = now - 540_000, stage = Stg.CALL_BACK),
                Entry(id = "e4", name = if (language == 1) "نور حسين" else "Nour Hussein", number = "+20 155 209 3810", status = St.DONE, attempts = 1, calledAt = now - 720_000),
                Entry(id = "e5", name = if (language == 1) "يوسف خالد" else "Youssef Khaled", number = "+20 106 301 9904"),
                Entry(id = "e6", name = if (language == 1) "مريم عمر" else "Mariam Omar", number = "+20 109 770 1145")
            )
        )
        val coast = CallList(
            id = "qa-coast",
            name = if (language == 1) "متابعة الساحل" else "North Coast follow-up",
            entries = List(8) { i ->
                Entry(
                    id = "c${i}",
                    name = if (language == 1) "عميل ${i + 1}" else "Contact ${i + 1}",
                    number = "+20 120 800 ${1200 + i}",
                    status = if (i < 3) St.ANSWERED else if (i < 5) St.NO_ANSWER else St.PENDING,
                    attempts = if (i < 5) 1 else 0,
                    calledAt = if (i < 5) now - (i + 1) * 600_000L else 0L,
                    stage = if (i == 0) Stg.WHATSAPP else Stg.NONE
                )
            }
        )
        Store.lists.addAll(listOf(east, coast))
        Store.log.addAll(
            east.entries.filter { it.calledAt > 0 }.map {
                LogItem(it.calledAt, east.name, it.name, it.number, it.status, it.duration, it.attempts)
            }
        )

        Store.tab = when (scenario) {
            "live" -> 1
            "results" -> 2
            "history" -> 3
            "settings" -> 4
            else -> 0
        }
        if (scenario == "live") {
            Store.runListId = east.id
            Store.camp = CampState(
                status = CS.WAITING,
                listId = east.id,
                listName = east.name,
                message = T("Preparing the next call", "جارٍ تجهيز المكالمة التالية"),
                loop = 1,
                loops = 2,
                nextAt = now + 12_000,
                queuedIds = listOf("e5", "e6")
            )
        }
        if (scenario == "results") Store.resultsListId = east.id
    }
}
