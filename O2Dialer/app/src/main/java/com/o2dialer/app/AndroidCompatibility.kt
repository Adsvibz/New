package com.o2dialer.app

/** Rules for Android API-level behavior; pure functions so every boundary is unit-tested. */
internal enum class CallLimitation {
    UNVERIFIED_RESULTS,
    MANUAL_HANGUP,
    HIDDEN_NOTIFICATIONS
}

internal object AndroidCompatibility {
    const val MIN_SUPPORTED_SDK = 26
    const val TARGET_SDK = 36

    fun supportsSdk(sdk: Int): Boolean = sdk >= MIN_SUPPORTED_SDK
    fun usesModernCallStateListener(sdk: Int): Boolean = sdk >= 31
    fun requiresNotificationPermission(sdk: Int): Boolean = sdk >= 33
    fun canAutomaticallyHangUp(sdk: Int, granted: Boolean): Boolean = sdk >= 28 && granted

    fun callLimitations(
        sdk: Int,
        wantsCallLog: Boolean,
        callLogAllowed: Boolean,
        maxCallSeconds: Int,
        endCallAllowed: Boolean,
        notificationsAllowed: Boolean
    ): Set<CallLimitation> {
        val result = linkedSetOf<CallLimitation>()
        if (wantsCallLog && !callLogAllowed) result.add(CallLimitation.UNVERIFIED_RESULTS)
        if (maxCallSeconds > 0 && !canAutomaticallyHangUp(sdk, endCallAllowed)) {
            result.add(CallLimitation.MANUAL_HANGUP)
        }
        if (requiresNotificationPermission(sdk) && !notificationsAllowed) {
            result.add(CallLimitation.HIDDEN_NOTIFICATIONS)
        }
        return result
    }
}
