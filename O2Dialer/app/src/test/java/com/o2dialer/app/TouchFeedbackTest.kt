package com.o2dialer.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TouchFeedbackTest {
    @Test fun pressFeelsLikeAnActualButtonDepressionAndImmediatelyReleases() {
        assertTrue(TouchFeedback.PRESS_MS in 70..110)
        assertTrue(TouchFeedback.RELEASE_MS in 100..155)
        assertTrue(TouchFeedback.BUTTON_SCALE in 0.955f..0.975f)
        assertTrue(TouchFeedback.COMPACT_SCALE in 0.950f..0.975f)
    }

    @Test fun everyRippleAndHighlightIsExactlyZero() {
        assertEquals(0f, TouchFeedback.PRESS_ALPHA, 0f)
        assertEquals(0f, TouchFeedback.FOCUS_ALPHA, 0f)
        assertEquals(0f, TouchFeedback.HOVER_ALPHA, 0f)
        assertEquals(0f, TouchFeedback.DRAG_ALPHA, 0f)
    }
}
