package com.o2dialer.app

/** Shared contract for language-switch motion; no Android/Compose dependencies. */
internal object LanguageMotionPolicy {
    const val EXIT_MS = 110
    const val ENTER_MS = 285
    const val BLUR_DP = 4.5f

    // RTL content arrives gently from the right; LTR from the left.
    fun enterOffsetDp(targetLanguage: Int): Float = if (targetLanguage == 1) 12f else -12f
    fun exitOffsetDp(targetLanguage: Int): Float = -enterOffsetDp(targetLanguage) * .45f

    fun shouldAnimate(currentLanguage: Int, targetLanguage: Int, reducedMotion: Boolean): Boolean =
        currentLanguage != targetLanguage && !reducedMotion
}
