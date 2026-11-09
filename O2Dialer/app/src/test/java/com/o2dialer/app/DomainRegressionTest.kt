package com.o2dialer.app

import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject

class DomainRegressionTest {
    @Test fun emptyCampaignHasZeroProgress() {
        assertEquals(Prog(0, 0, 0, 0, 0), Store.prog(CallList(name = "Empty")))
        assertEquals(0, Store.prog(CallList(name = "Empty")).pct)
    }

    @Test fun unverifiedCallsAreProcessedButNotConfirmedAnswers() {
        val entries = listOf(St.PENDING, St.CALLING, St.ANSWERED, St.NO_ANSWER, St.FAILED, St.DONE)
            .mapIndexed { i, status -> Entry(number = "+20100000000$i", status = status) }
        val p = Store.prog(CallList(name = "Mixed", entries = entries))
        assertEquals(6, p.total)
        assertEquals(4, p.called)
        assertEquals(1, p.answered)
        assertEquals(1, p.unverified)
        assertEquals(2, p.remaining)
        assertEquals(66, p.pct)
        // Retain historical skip-answered behavior for unknown completed calls.
        assertTrue(St.isAnswered(St.DONE))
        assertFalse(St.isConfirmedAnswered(St.DONE))
    }

    @Test fun largeProgressCannotOverflow() {
        assertEquals(100, Prog(Int.MAX_VALUE, Int.MAX_VALUE, 0, 0, 0).pct)
    }

    @Test fun nextContactUsesActualQueueIncludingRetryAndShuffle() {
        val first = Entry(id = "first", number = "01011111111")
        val retry = Entry(id = "retry", number = "01022222222", status = St.NO_ANSWER)
        val last = Entry(id = "last", number = "01033333333")
        val list = CallList(name = "Campaign", entries = listOf(first, retry, last))
        val state = CampState(queuedIds = listOf("last", "removed", "retry", "first"))
        assertEquals(listOf(last, retry, first), queuedEntries(state, list))
        assertTrue(queuedEntries(CampState(status = CS.FINISHED), list).isEmpty())
    }

    @Test fun stoppedAndCompletedSessionsHaveDifferentLabels() {
        Store.cfg = Cfg(lang = 0)
        assertEquals("Session stopped", sessionLabel(CampState(status = CS.FINISHED)))
        assertEquals("Session complete", sessionLabel(CampState(status = CS.FINISHED, completed = true)))
        assertEquals("Waiting", sessionLabel(CampState(status = CS.WAITING)))
        assertEquals("Checking result", sessionLabel(CampState(status = CS.RUNNING, evaluating = true)))
    }

    @Test fun phoneNormalizationHandlesArabicAndInternationalFormats() {
        assertEquals("+201001234567", smartNumber("٠١٠٠١٢٣٤٥٦٧", "20"))
        assertEquals("+201001234567", smartNumber("00201001234567", "20"))
        assertEquals("+201001234567", smartNumber("+20 (100) 123-4567", "20"))
        assertEquals("*123#", smartNumber("*123#", "20"))
        assertEquals("", smartNumber("", "20"))
    }

    @Test fun csvImportHandlesQuotedNamesAndUnicodeNumbers() {
        val rows = parseLines("Name,Number\n\"Doe, Jane\",\"+201001234567\"\nنوح\t٠١١١٢٣٤٥٦٧٨\n+201221234567\ninvalid")
        assertEquals(3, rows.size)
        assertEquals("Doe, Jane" to "+201001234567", rows[0])
        assertEquals("نوح" to "01112345678", rows[1])
        assertEquals("" to "+201221234567", rows[2])
    }

    @Test fun existingConfigurationRoundTripsWithoutChangingSettings() {
        val cfg = Cfg(lang = 1, delaySec = 12, randomDelay = true, delayMaxSec = 25,
            retries = 3, loops = 2, countryCode = "20", autoSpeaker = false, themeMode = 2)
        assertEquals(cfg, Cfg.fromJson(JSONObject(cfg.toJson().toString())))
        assertEquals(Cfg(), Cfg.fromJson(JSONObject()))
    }

    @Test fun invalidPersistedSettingsAreBounded() {
        val cfg = Cfg.fromJson(JSONObject("""{"delaySec":-20,"loops":0,"themeMode":90,"retries":500,"quietTo":99}"""))
        assertEquals(0, cfg.delaySec)
        assertEquals(1, cfg.loops)
        assertEquals(2, cfg.themeMode)
        assertEquals(5, cfg.retries)
        assertEquals(23, cfg.quietTo)
    }
}
