package com.o2dialer.app

import org.junit.Assert.*
import org.junit.Test

class CampaignSelectionTest {
    @Test fun onlySelectedExistingCampaignsCanBeRemoved() {
        assertEquals(setOf("a"), removableCampaignIds(setOf("a", "b"), setOf("a", "missing"), null))
        assertTrue(removableCampaignIds(setOf("a"), emptySet(), null).isEmpty())
    }
    @Test fun activeCampaignIsProtectedEvenIfSelectedBeforeItStarted() {
        assertEquals(setOf("b"), removableCampaignIds(setOf("a", "b"), setOf("a", "b"), "a"))
        assertTrue(removableCampaignIds(setOf("a"), setOf("a"), "a").isEmpty())
    }
    @Test fun finishedCampaignCanBeSelectedWhenNoSessionIsActive() {
        assertEquals(setOf("a", "b"), removableCampaignIds(setOf("a", "b"), setOf("a", "b"), null))
    }
    @Test fun storeProtectsRunningPausedAndWaitingCampaignsAtSaveTime() {
        val oldCamp = Store.camp
        val oldLists = Store.lists.toList()
        try {
            Store.lists.clear()
            Store.lists.add(CallList(id = "active", name = "Original", simSlot = 0))
            for (status in listOf(CS.RUNNING, CS.PAUSED, CS.WAITING)) {
                Store.camp = CampState(status = status, listId = "active")
                assertFalse(Store.editCampaign("active", "Changed", 1))
                assertEquals(0, Store.deleteLists(setOf("active")))
                assertEquals("Original", Store.lists.single().name)
                assertEquals(0, Store.lists.single().simSlot)
            }
        } finally {
            Store.camp = oldCamp
            Store.lists.clear(); Store.lists.addAll(oldLists)
        }
    }
}
