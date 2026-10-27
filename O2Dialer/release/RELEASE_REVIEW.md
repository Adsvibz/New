# O2Dialer 7.0 release review

Branch: `design/o2dialer-v7-rebuild`. Base inspected: `c7dfa52d2662d09d1193082c8939168bfa557792`.
Main and the existing signing identity are unchanged. This is a release candidate, not an unconditional production approval.

## Screen and interaction audit

| Area | Implementation review and changes | Runtime verification |
| --- | --- | --- |
| Welcome / onboarding | Replaced unused decorative/demo UI with three bilingual, theme-aware, scrollable capability pages; honest permission and device limits | Pending |
| Campaign workspace | Real Store totals, inset-safe header, animated counters, search, all original campaign actions restored | Pending |
| Create / edit / delete | Shared forms, saved drafts, SIM labels refresh on permissions/language, protect running campaign, confirm deletion/reset | Pending |
| Campaign contacts | Preserved CRUD and stages, current-call edit/delete guard, empty layout scrolls, returning to active session | Pending |
| Manual / bulk / files / contacts | Unicode normalization, quoted CSV fields, bounded file read and background parsing, explicit errors, licensing recheck | Pending |
| Live session | Actual service queue for retries/shuffle; distinct waiting/evaluation/paused/stopped/completed states; confirmation for stop/removal; launcher after finish | Pending |
| Outcomes / notes / WhatsApp | Preserved stage flow and follow-up; saved note draft; unknown connection no longer inflates confirmed answers | Pending |
| Results / reporting | Real Store progress, unverified explanation, empty-state copy, filters and sharing retained, narrow-screen chart adjustment | Pending |
| History | Confirmed-answer count, separate unverified filter, Arabic day labels, sharing/clear confirmation retained | Pending |
| Settings / licensing / permissions | All existing preferences preserved; capability limits and equal quiet-hour warning; licensing UI retained; device token excluded from backup | Pending |
| Shared UI / navigation | Primary blue and navy, light/dark, RTL segment fix, directional navigation, saved outgoing detail identity, larger touch targets, dialog width/keyboard handling, accessible toast lifetime | Pending |
| Motion | Central timings/springs; observe system disabled animations; no perpetual active-call decoration; stagger skipped for reduced motion and active calls | Pending |

## Data and behavior compatibility

- Existing `o2dialer` preferences and JSON keys/IDs/status integers remain unchanged. No destructive migration.
- Legacy DONE entries remain completed and retain original skip-answered/restart behavior. Only confirmed ANSWERED entries count in answer metrics; DONE is visibly unverified.
- Config loading bounds invalid numeric values to supported UI ranges. Valid settings round-trip unchanged.
- Save revisions prevent a cancelled background serialization from overwriting newer lifecycle data.
- A process restart never silently starts calls. Existing in-progress entries recover to pending, matching previous behavior; user initiates resume.
- Service changes address observed problems only: wrong-number call-log association, misleading OFFHOOK duration, stale current-contact status after pause, overlapping calls, missing hang-up feedback, original audio-state restoration and teardown cleanup.
- License URL/policy/gate requirements remain intact. CI requires the existing HTTPS licensing secret. Local APKs without that secret are for validation only.

## Release verification contract

The existing workflow is manual, plus an explicit verification-request file trigger restricted to the development branch. Ordinary source edits do not trigger builds. It runs release unit tests, fatal lint and minified release packaging. There is no debug fallback. Signing verification must succeed; metadata must identify the expected package/version and must not be debuggable. Artifact contains APK, SHA-256, certificate verification, metadata and commit/run provenance.

## Required physical-device acceptance

- Upgrade over the installed version using the same certificate; verify campaigns, contacts, stage notes, settings and logs survive.
- Android 8/9 and current Android devices, including Samsung/other OEM: CALL_PHONE, phone-state, notification and call-log denial/revocation; first run and permanent denial.
- Dual SIM (including eSIM/disabled slots), preferred SIM and system default picker. SIM index/account mapping remains device dependent.
- Sequential dialing, busy/no-answer/failure/answered, call-log delay/unavailable, manual phone calls during countdown, rapid start/pause/resume/skip/stop, retries, shuffle, rounds and quiet windows crossing midnight.
- Automatic hang-up/max-duration/skip on devices that restrict TelecomManager.endCall; manual termination recovery. OFFHOOK includes dialing and does not prove a connection.
- Audio routing and microphone changes/restoration; hardware mute, headset/Bluetooth, permissions. UI shows configured preference, not a verified hardware state.
- Background execution, battery restrictions, screen lock, service termination, low memory/process kill, restart, notification actions and wake-lock release.
- Licensing activation, offline allowance, revocation/suspension heartbeat, rejected imports/exports, no activation token transferred through backup.
- TalkBack, font scale 1.0/1.3/2.0, Arabic RTL and English LTR, compact phone/landscape/tablet, both themes and system animations disabled.
- Large real lists/files, cancelled/unreadable imports, selection/search, CSV reports and WhatsApp intent behavior.

## Security release blocker

The existing signing keystore and its build configuration are committed in this public repository. The key is retained to avoid breaking Android upgrade compatibility; its confidentiality cannot be assumed. A successful APK signature is not evidence of secure key custody. Owner must decide a supported signing/distribution migration before broad production distribution. Do not rotate automatically, disclose credentials or claim this is resolved by changing the file location. Prior history also needs assessment.

## Verification results

- GitHub Actions run [40460783024](https://github.com/Adsvibz/New/actions/runs/40460783024) completed successfully for commit `bd91f6becac93bac9f5965db1c73bfc1d2ee9a63`.
- Release unit tests: 9 passed. Fatal Android lint: passed. R8/minified release packaging: passed.
- Artifact `O2Dialer-7.0-release` (ID `12713909642`) was generated and retained by GitHub Actions through 2027-01-25.
- APK identity: `com.o2dialer.app`, version name `7.0`, version code `157`, minimum SDK 26, target SDK 34; licensing was enabled for the build.
- APK SHA-256: `75691eba18885f9ab86f95314e2332dd92e4bf406d3b40a83a70209ad491e3c2`; the downloaded artifact checksum was independently rechecked successfully.
- APK signature verification passed with one RSA-2048 signer using APK Signature Scheme v2. Certificate SHA-256: `c484e4216d54bcf601fb00850c5673bd36b1c5451ad71158a94f5450f1cfae3b`.
- The extracted APK archive passed an independent ZIP integrity check. Workflow metadata also verified that the APK is not debuggable.
- Android runtime tooling was unavailable in the final execution environment. Consequently, no emulator result is claimed; all hardware-, telephony-, OEM-, upgrade- and accessibility-dependent checks above remain required on physical devices.

The first successful candidate above targeted API 34 and is retained as reproducible evidence. The final candidate raised the build toolchain and application target to API 36 and repeated the complete verification contract successfully:

- Final GitHub Actions run [40461569301](https://github.com/Adsvibz/New/actions/runs/40461569301) completed successfully for commit `53633463109594273f51c3ce8c7876503df0137f`.
- Gradle 8.11.1, Android Gradle Plugin 8.10.1, compile SDK 36 and target SDK 36 were used; minimum SDK remains 26.
- 9 tests passed with 0 failures or ignored tests; fatal lint passed with 0 errors; R8/minified release packaging passed.
- Final artifact `O2Dialer-7.0-release` (ID `12714833061`) contains version `7.0` (code `158`) with licensing enabled and is retained through 2027-01-25.
- Final APK SHA-256: `91bcf33c00a8584632ba683b2419a6edfc5bd2aa485f6d1dfc8ee1ef29fc01eb`; downloaded checksum and ZIP integrity were independently rechecked successfully.
- Signing identity is unchanged and independently reconfirmed: one RSA-2048 signer, APK Signature Scheme v2, certificate SHA-256 `c484e4216d54bcf601fb00850c5673bd36b1c5451ad71158a94f5450f1cfae3b`.
- Lint reports 20 non-fatal warnings and one hint: dependency update notices, intentionally required telephony/device-license identifiers, application-context false positives, KTX style suggestions and launcher/Compose style improvements. None are compile, packaging, signing or fatal correctness failures; dependency upgrades require a separately tested compatibility cycle.
