# Browser-based native Android preview
O2Dialer V7 is a native Kotlin/Jetpack Compose Android app in the `O2Dialer/` directory.
The root `ramus.toml` points Ramus's GitHub PR builder there, and the included Gradle 8.11.1 wrapper is used for `./gradlew --no-daemon assembleDebug`.

## Owner setup (no software to install)
1. Visit https://ramus.dev/products/github-previews and sign in with GitHub.
2. Install the Ramus GitHub App only on `Adsvibz/New`. Review the requested permissions before authorizing.
3. Enable **Automatic PR previews** for `Adsvibz/New` in Ramus.
4. Open https://github.com/Adsvibz/New/pull/1 . New commits on the existing V7 branch will trigger a fresh native Android APK preview.
5. Open the Ramus preview link from the PR when the build completes. You can tap, scroll and interact with the Android emulator in the browser.

This is **not** instantaneous Android Studio Live Edit. Each push generates a new build and a new preview; you can inspect each build without installing an APK locally.
The Ramus free plan is limited to 30 device minutes per month at the time of writing (2026-10-28), and conditions may change.
Native telephone/call features may be unsupported in a cloud emulator; do not use real client data there.
The existing signed release GitHub Actions workflow and app logic have not been changed.
