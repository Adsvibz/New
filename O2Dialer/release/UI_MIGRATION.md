# AI Studio native UI migration

Native baseline: `3c808f141afc1f6006c6d410bc83ca6651f15d04`.
Reference: `design_reference` from main `58b3cd39129e80d5bba6797a416225e75a5268ee`.

## Reference lock
The executable TSX/CSS and index.html are authoritative. HANDOFF_SPECIFICATION.md describes an earlier white/blue Outfit design. Current CSS uses Plus Jakarta Sans, IBM Plex Sans Arabic, JetBrains Mono, warm radial/linear canvas, orange #E2532B hero surfaces, 24dp translucent cards and a floating pill navigation. Android system bars remain real system UI, not simulated web decorations. Demo contacts, fake timers and browser-only product controls must not enter production data.

| Module/reference | Native state | Existing actions preserved |
|---|---|---|
| Campaigns / CampaignCard | Store.lists, Store.prog, Store.camp | addList, updateList, deleteList, dedupe, resetForRestart, listReport |
| CampaignDetails / contact sheets | CallList.entries, selectedListId | addEntries, editEntry, deleteEntry, OpenDocument, parseLines, ensureContacts, loadContacts |
| LiveDialer / ActiveCallingHero | Store.camp, runListId, queuedEntries | MainActivity.startCampaign, DialerService.send |
| SessionControls | CS status, nextAt, loops, evaluating | ACT_PAUSE, ACT_RESUME, ACT_SKIP, ACT_STOP; unchanged confirmations |
| StageClassificationSheet | stageTarget, Entry.stage/feedback | Store.setStage, stage hold/done service actions, openWhatsApp |
| Results / CallLogs | Store.prog, Store.log, resultsListId | filters, stage edits, listReport/logReport, shareText, clearLog |
| Settings | Store.cfg, License state, permission state | updateCfg, permission launchers, License.gate/activate |
| Update UI (native-only) | AppUpdates state | check, download, cancelDownload, dismiss, install |

The native app has no ViewModels, StateFlow or LiveData. Compose observes Store's mutableStateOf/mutableStateListOf and the License/AppUpdates objects. The migration retains those owners, IDs, serialization and service callbacks.

## Token decision ledger
| Decision | Source | Role |
|---|---|---|
| #E2532B, gradient 96% white mix to 84% charcoal mix | index.css | hero metric / active emphasis |
| #F7F6F2 → #F1EFEA → #EAE7E1 | index.css | light canvas |
| #131519 → #171A20 → #111317 | index.css dark overrides | dark canvas |
| white 78%, dark #1C1F26 72% | CSS bg-white/60 override | cards, not opaque page backgrounds |
| Plus Jakarta Sans / IBM Plex Sans Arabic / JetBrains Mono | index.html | body / Arabic / numbers |
| 20dp screen inset; 14dp grid gaps; 24dp cards | current TSX | screen geometry |
| Dialer, Results, Campaigns, History, Settings | FloatingBottomNav.tsx | same native destinations, center campaign anchor |

## Compatibility contract
No changes to Models.kt, DialerService.kt, Util.kt, License.kt, AppUpdates.kt, UpdateManifest.kt, App.kt, manifest, signing key, build configuration, workflows, or server. UI uses real Store data. Existing import limits and permission/activation checks take precedence over demo behavior. Preserve all native features even when absent from mockups.

## Verification
See final verification notes. Visual parity and real-device telephony acceptance are separate from compilation; do not infer either from a successful build.
