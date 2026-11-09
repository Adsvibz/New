package com.o2dialer.app

import org.junit.Assert.*
import org.junit.Test

class AndroidCompatibilityTest {
    @Test fun minSupportedSdkMatchesGradleAndUpdateFeed() {
        assertFalse(AndroidCompatibility.supportsSdk(25))
        assertTrue(AndroidCompatibility.supportsSdk(26))
        assertTrue(AndroidCompatibility.supportsSdk(36))
        assertEquals(36, AndroidCompatibility.TARGET_SDK)
    }

    @Test fun callbackSwitchIsAtAndroid12() {
        assertFalse(AndroidCompatibility.usesModernCallStateListener(26))
        assertFalse(AndroidCompatibility.usesModernCallStateListener(30))
        assertTrue(AndroidCompatibility.usesModernCallStateListener(31))
        assertTrue(AndroidCompatibility.usesModernCallStateListener(36))
    }

    @Test fun hangupRequiresBothAndroid9AndPermission() {
        assertFalse(AndroidCompatibility.canAutomaticallyHangUp(26, true))
        assertFalse(AndroidCompatibility.canAutomaticallyHangUp(27, true))
        assertFalse(AndroidCompatibility.canAutomaticallyHangUp(28, false))
        assertTrue(AndroidCompatibility.canAutomaticallyHangUp(28, true))
        assertTrue(AndroidCompatibility.canAutomaticallyHangUp(36, true))
    }

    @Test fun notificationPermissionStartsAtAndroid13() {
        assertFalse(AndroidCompatibility.requiresNotificationPermission(26))
        assertFalse(AndroidCompatibility.requiresNotificationPermission(32))
        assertTrue(AndroidCompatibility.requiresNotificationPermission(33))
        assertTrue(AndroidCompatibility.requiresNotificationPermission(36))
    }

    @Test fun missingOptionalPermissionsShowAccurateFeatureLimits() {
        val limits = AndroidCompatibility.callLimitations(
            sdk = 36, wantsCallLog = true, callLogAllowed = false,
            maxCallSeconds = 45, endCallAllowed = false, notificationsAllowed = false
        )
        assertEquals(setOf(
            CallLimitation.UNVERIFIED_RESULTS,
            CallLimitation.MANUAL_HANGUP,
            CallLimitation.HIDDEN_NOTIFICATIONS
        ), limits)
    }

    @Test fun olderAndroidDoesNotFalselyRequireNotificationPermission() {
        assertEquals(setOf(CallLimitation.MANUAL_HANGUP),
            AndroidCompatibility.callLimitations(26, false, false, 30, true, false))
        assertTrue(AndroidCompatibility.callLimitations(33, false, false, 0, false, true).isEmpty())
    }
}
