package com.o2dialer.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageMotionPolicyTest {
    @Test fun arabicEntersFromRightAndEnglishFromLeft() {
        assertEquals(12f, LanguageMotionPolicy.enterOffsetDp(1), .001f)
        assertEquals(-12f, LanguageMotionPolicy.enterOffsetDp(0), .001f)
    }

    @Test fun outgoingPageMovesOppositeToIncomingLanguage() {
        assertTrue(LanguageMotionPolicy.exitOffsetDp(1) < 0f)
        assertTrue(LanguageMotionPolicy.exitOffsetDp(0) > 0f)
    }

    @Test fun motionIsDisabledWhenLanguageIsUnchangedOrAccessibilityRequestsReducedMotion() {
        assertFalse(LanguageMotionPolicy.shouldAnimate(0, 0, false))
        assertFalse(LanguageMotionPolicy.shouldAnimate(0, 1, true))
        assertTrue(LanguageMotionPolicy.shouldAnimate(0, 1, false))
    }
}
