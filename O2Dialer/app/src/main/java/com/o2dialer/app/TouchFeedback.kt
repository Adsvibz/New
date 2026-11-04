package com.o2dialer.app

/** Shared fast, quiet touch response. Values are independent of platform APIs. */
internal object TouchFeedback {
    const val PRESS_MS = 95
    const val RELEASE_MS = 135

    const val BUTTON_SCALE = 0.985f
    const val COMPACT_SCALE = 0.978f

    const val PRESS_ALPHA = 0.055f
    const val FOCUS_ALPHA = 0.055f
    const val HOVER_ALPHA = 0.035f
    const val DRAG_ALPHA = 0.075f
}
