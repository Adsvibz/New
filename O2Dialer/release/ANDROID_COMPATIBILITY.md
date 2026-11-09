# O2Dialer Android compatibility verification

This branch supports Android API 26 (Android 8.0) and later and targets API 36 (Android 16). A handset must support telephony. OS and OEM policies can still restrict call-log access, background foreground-service launches, call end, and notifications.

## Code safeguards
- Dialing never begins when foreground-service promotion is rejected by Android. The user sees a bilingual failure instead of an uncaught crash.
- Background Pause/Resume/Stop dispatch rejections are reported without crashing the activity.
- Optional call-log, automatic-hang-up and notification restrictions are explained to the user; denying an optional permission does not disable the basic call action.
- Old phone-state listeners (API 26–30) and the newer TelephonyCallback (31+) are selected by an explicitly tested API rule.
- Local bundled fonts and system window insets remain enabled; no global font scale override is used.
- Updates retain the app ID, local user data, current key identity and Android version checks.

## Automated checks
- Main CI: release unit tests, Android lint, R8 packaging, APK signature and update-feed verification.
- PR/manual [Android compatibility smoke](../../.github/workflows/android-compatibility.yml): install + launch + relaunch with 320x720 viewport and 1.3x font scale on API 26, 30, 33, 35 and 36. Each job saves a screenshot.
- These launch checks do **not** verify real SIM phone calls, OEM service survival, audio routing, call logs, or true visual parity. Review captured screenshots and run the manual matrix before release signoff.

## Manual device acceptance (required)
On real phones (at least one manufacturer for each Android generation):
1. Check onboarding, Campaigns, Dialer, Reports, History and Settings in Arabic and English; verify 1.0x and 1.3x+ system font sizes, portrait/landscape, gesture and 3-button navigation, keyboard open, light/dark themes, accent color, and screen reader semantics.
2. Import a CSV and contacts; create/edit/delete a campaign; confirm persistence after force-stop and reopening.
3. Grant and deny CALL_PHONE, READ_PHONE_STATE, READ_CALL_LOG, ANSWER_PHONE_CALLS and POST_NOTIFICATIONS separately. Check the displayed limits, call result accuracy, and whether a rejected background launch recovers.
4. Test incoming-call interruption, no answer, answered call, retry, pause/resume/stop, manual and automatic hang-up, and speaker/mute restoration. OEM policies may prevent automatic call termination.
5. Verify that in-app updates only install a higher signed version compatible with the device, and retain all campaigns and settings.
6. Compare UI screenshots at matching density/font scale and review any overflow and missed tap targets.

No update can guarantee full telephony feature parity on *every* Android model because privileges and background execution restrictions differ. Do not treat emulator smoke as production device certification.
