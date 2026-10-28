# Direct APK update channel

The APK distribution channel is GitHub Releases in `Adsvibz/New`. This is not a Google Play update integration.

## Publication

Use the existing `design/o2dialer-v7-rebuild` branch. Change `release/verification-request.txt` once a cohesive update is ready, or run the Build APK workflow manually on that branch. Routine source edits alone do not trigger distribution.

The build runs release unit tests, lint, R8 packaging, signing/metadata verification, and creates `update.json` from the resulting APK. Only then does the separate publication job upload a draft release and publish it as latest. Publication uses a scoped write token; the build job retains read-only repository access. Older workflow reruns cannot replace a newer update, and published assets are not overwritten.

Each publication must have a higher Android versionCode and retain the existing application ID and signing identity. The existing workflow number controls versionCode; do not reset that numbering by recreating the workflow. The current signing-key custody issue documented in RELEASE_REVIEW.md remains unresolved by this feature.

## Client behavior

- Existing builds through 162 have no updater. Users must install the first updater-enabled APK once over their current installation; never instruct them to uninstall first.
- On opening/resuming the app, check at most every six hours. Settings offers an immediate manual check. This is polling while using the app, not push delivery to closed/offline devices.
- Show available updates outside active/paused sessions, outcome dialogs and activation prompts. Dismissal suppresses that version's automatic prompt for 24 hours.
- Download only after a tap, into a private cache directory. Display progress, support cancellation/retry, and bound sizes/timeouts/redirects. No campaign/contact/license data is sent to GitHub; normal network metadata and the app build number are sent.
- Require the exact repository/tag asset URL, HTTPS, SHA-256, expected size, package ID, versionCode, and the same nonempty APK signing identity as the installed app. Revalidate before installation.
- Android's unknown-source permission and installer confirmation remain required. After granting permission, return to the app and tap Install. Installation is disabled while a campaign is active, including paused campaigns. Flush pending Store writes before launching the installer.
- Rotation retains in-process download state. Process death discards transient UI state; check again from Settings and restart the download. Partial files are never offered to the installer. Cache files may be removed by Android and are revalidated before use.
- APK caches are excluded from Android backups automatically. Existing campaign/contact/log/settings formats and licensing gates are unchanged.

## Device acceptance still required

Install the bootstrap APK over build 162, then publish a later verified build and exercise discovery, Later, cancel/retry, offline recovery, unknown-source denial/grant, installer cancellation, successful upgrade and data retention. Test API 26 plus a modern Android/OEM device, rotation/process death, a cleared download cache and running/paused campaigns. Unit tests and CI do not prove these Android installer/UI workflows.

Before any future Google Play distribution, replace this direct-APK channel with the Play update mechanism and review/remove REQUEST_INSTALL_PACKAGES as appropriate.
