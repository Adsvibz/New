package com.o2dialer.app

import org.junit.Assert.assertTrue
import org.junit.Test

class TouchFeedbackTest {
    @Test fun touchResponseStaysFastAndSubtle() {
        assertTrue(TouchFeedback.PRESS_MS in 85..125)
        assertTrue(TouchFeedback.RELEASE_MS in 100..155)
        assertTrue(TouchFeedback.BUTTON_SCALE in 0.980f..0.995f)
        assertTrue(TouchFeedback.COMPACT_SCALE in 0.970f..0.990f)
    }

    @Test fun rippleIsDiscreetButVisible() {
        assertTrue(TouchFeedback.PRESS_ALPHA > 0f)
        assertTrue(TouchFeedback.PRESS_ALPHA < 0.075f)
        assertTrue(TouchFeedback.FOCUS_ALPHA <= 0.07f)
        assertTrue(TouchFeedback.HOVER_ALPHA <= TouchFeedback.PRESS_ALPHA)
    }
}
