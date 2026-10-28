# AI Studio Campaigns design migration (2026-10-28)

Source: user-supplied `o2dialer-7.0-—-premium-campaigns.zip` (React/Tailwind prototype).
Visual reference: 390×844 Campaigns screenshot embedded in the supplied design ZIP.
Target: `app/src/main/java/com/o2dialer/app/V7Workspace.kt` (native Jetpack Compose).
No React bundle or mock campaign data are included in the Android application.

## Main-screen fidelity
- Light ivory/white full-screen design with compact Outfit/IBM Plex Sans Arabic typography.
- O2DIALER 7.0 eyebrow; Campaigns heading, native language switch and dark New Campaign CTA.
- Four statistics in a soft gray 2×2 panel with overall blue progress line.
- Compact search, sorting menu and scrollable status chips.
- Thin-border 15dp campaign cards: live name, status, call counts, progress and time of last genuine call.
- Long-press selection, bulk delete confirmation, rename, reset, deduplication, share, results and native editing.
- Existing app bottom navigation and underlying calling/storage/licensing remain untouched.

## Intentional differences from the mock
- Prototype datasets, fake caller ID, imaginary predictive/rapid dialer modes, invented activity dates and scheduled statuses are NOT persisted in the real app's CallList model. UI shows real SIM and call history instead. The 'Ready' chip substitutes for unsupported 'Scheduled'.
- Pause/play icon on an active campaign navigates to the actual Live session manager; no fake toggle is performed.
- Native creation/edit/detail dialogs are kept from the existing app to preserve backend and permissions; this change ports the Campaigns list itself, not all React demo modal screens.
- Existing five-destination native bottom navigation is not replaced by the React simulation.
- Pixel-perfect validation, real-device screenshots, Arabic RTL and accessibility font scaling need a completed native build and device/emulator review. This is not yet claimed to be a 1:1 screenshot match.

## Verification
The separate `.github/workflows/campaign-ui-check.yml` workflow compiles the debug APK and executes unit tests on changes to the native screen. It does not publish production releases or require secrets. Check that GitHub Actions has completed successfully before distributing the APK.
