O2Dialer v6.0 (package com.o2dialer.app)
Push this project to your GitHub repo root (keep .github/workflows/build.yml), run Actions, download the O2Dialer-APK artifact.
To rename the visible app name again: edit APP_NAME in Theme.kt and android:label in AndroidManifest.xml.
License server (optional): see server/README.md. Add the LICENSE_URL secret in GitHub to turn licensing on; without it the app works exactly as before.

v6.0 — “Nocturne” interface redesign
- New art direction: warm ivory paper and deep forest green in light mode, black-green and mint in dark, with brass used only as an accent.
- Editorial typography: Instrument Serif for display numbers and page titles, Outfit for interface text, IBM Plex Sans Arabic for Arabic. Serif is skipped in Arabic for correct shaping.
- Motion system: screen transitions with directional slides, a dock selection pill that springs between tabs, staggered card entrances, press-scale feedback, animated counters and progress.
- Onboarding is now a cinematic dark-green sequence with drifting aurora light, floating live previews, page parallax and an expanding page indicator.
- Campaigns home opens with a daily brief: animated answer rate, a seven-day sparkline and live workspace totals.
- Campaign cards show progress as a ring; the contact list uses a single grouped surface with cleaner status and stage treatment.
- Live dialer: aurora call hero, breathing rings around the contact orb, large serif countdown, live waveform, tactile circular controls and a compact run-setup strip.
- Results: animated stage donut with tappable legend, serif stat filters and clearer campaign progress.
- Settings uses animated accordion sections, the new switch/slider language and a brand header.
- Forms remain bottom sheets, now with the new sheet header, drag handle and field styling.
- New adaptive launcher icon: forest gradient tile with an ivory O2 monogram; themed (monochrome) icon supported.
- Functionality, permissions, licensing, persistence and the dialer service are unchanged.

Fonts: Outfit and IBM Plex Sans Arabic (existing, OFL), plus Instrument Serif (OFL, assets/OFL-InstrumentSerif.txt).
