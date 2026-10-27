package com.o2dialer.app

import org.junit.Assert.*
import org.junit.Test

class StageQueueGuardTest {
    @Test fun historicalOutcomeCannotControlAnotherCampaign() {
        assertFalse(stageCanControlQueue(CampState(status = CS.RUNNING, listId = "live", awaitingStage = true), "history"))
    }
    @Test fun historicalOutcomeCannotInterfereWithCurrentCallOrEvaluation() {
        val live = CampState(status = CS.RUNNING, listId = "live")
        assertFalse(stageCanControlQueue(live.copy(currentNumber = "+201001234567"), "live"))
        assertFalse(stageCanControlQueue(live.copy(evaluating = true), "live"))
    }
    @Test fun currentCampaignOutcomeCanReleaseWaitingQueue() {
        for (status in listOf(CS.RUNNING, CS.PAUSED, CS.WAITING))
            assertTrue(stageCanControlQueue(CampState(status = status, listId = "live", awaitingStage = true), "live"))
    }
    @Test fun finishedSessionNeverReceivesQueueCommands() {
        assertFalse(stageCanControlQueue(CampState(status = CS.FINISHED, listId = "live"), "live"))
        assertFalse(stageCanControlQueue(CampState(), ""))
    }
}
