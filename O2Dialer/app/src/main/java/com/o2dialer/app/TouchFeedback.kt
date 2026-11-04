package com.o2dialer.app

/** No color or shadow on touch: only quick, reversible inward scaling. */
internal object TouchFeedback {
    const val PRESS_MS = 90
    const val RELEASE_MS = 135

    const val BUTTON_SCALE = 0.968f
    const val COMPACT_SCALE = 0.960f

    // Material ripple must be completely invisible on every theme.
    const val PRESS_ALPHA = 0f
    const val FOCUS_ALPHA = 0f
    const val HOVER_ALPHA = 0f
    const val DRAG_ALPHA = 0f
}
