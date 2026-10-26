O2Dialer v5.0 (package com.o2dialer.app)
Push this project to your GitHub repo root (keep .github/workflows/build.yml), run Actions, download the O2Dialer-APK artifact.
To rename the visible app name again: edit APP_NAME in Theme.kt and android:label in AndroidManifest.xml.
License server (optional): see server/README.md. Add the LICENSE_URL secret in GitHub to turn licensing on; without it the app works exactly as before.

v5.0 — interface redesign
- New visual system: neutral surfaces, one indigo accent, editorial type scale, larger titles.
- Flat grouped lists replace card-per-row contact and result lists; hairline separators.
- Floating five-item dock replaces the raised "bump" navigation bar.
- Live dialer: dark focus card, numeric countdown, circular call controls, stacked stage chart in Results.
- Forms (campaign, contact, paste, contacts) now open as bottom sheets instead of centred dialogs.
- Onboarding, settings and the launcher icon rebuilt in the same style.
- Functionality, permissions, licensing, persistence and the dialer service are unchanged.
