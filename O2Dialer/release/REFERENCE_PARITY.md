# Reference parity audit — correction after build 190

Build 190 is not a faithful reconstruction. Passing compilation does not establish visual or interaction parity.

## Source lock

`design_reference` at `58b3cd39129e80d5bba6797a416225e75a5268ee`. Preserve its visible elements, ordering, copy, options, states, gestures and animation choreography. Kotlin remains native; real Android service, import, permission, license and update handlers remain authoritative. Reference simulation controls must be explicitly accounted for, never silently omitted.

## Required coverage

| Reference | Missing or mismatched in 190 | Verification required |
|---|---|---|
| App / FloatingBottomNav | dock reserves layout space, does not hide on scroll; wrong route transition; detail hides navigation | floating overlay, 16dp downward / 10dp upward hysteresis, 300ms fade/20dp movement, 320ms icon pop, 340ms screen entrance |
| ScreenHeader / CampaignOverview | New label shortened; statistic composition simplified | same labels, inline completion pill, grid dimensions and card hierarchy |
| SearchAndFilterBar | input shape, sort selection behavior, filter order/counts differ | 44dp pill field, repeated sort toggles direction, same filter order |
| CampaignCard / MultiSelectToolbar | card actions/typography/sections and selection presentation differ | all menus, protected running state, selection and bulk actions |
| NewCampaignSheet / EditCampaignSheet | vertical radio list replaces three SIM tiles; generic sheet structure | same form header, three tiles, field styling, explanatory copy and actions |
| CampaignDetailsScreen | original native screen structure retained | reference header/menu, overview, four contact tools, search/status filters, rows, CTA and all contact modals |
| LiveDialerScreen / DialerHeader | generic header, picker and settings routing | campaign selector sheet, live configuration dialog, state-specific header |
| ActiveCallingHero / SessionControls | ready/calling/paused/countdown/finished layouts differ; missing wave animation | same state composition, controls, wave timing and status presentation; real service data |
| DialerProgress / CallingQueue / SessionConfigStrip | generic cards, old queue rows and configuration summary | exact metrics hierarchy, queue options, configuration affordances |
| ResultsScreen | metric arrangement, filters and contact result details differ | complete reference hierarchy and states |
| CallLogsScreen | search added but header/cards/rows differ | complete reference hierarchy, filters, grouping and clear confirmation |
| SettingsScreen | generic collapsible sections; omitted ten colors, bezel and handoff controls | exact eleven sections, controls, color popup, footer, permission and update flows |
| StageClassificationSheet | old native form retained | exact status tiles, selected state, notes, actions and transition |
| OnboardingModal | generic orange hero replaces reference illustrations and feature lists | all three pages, floating illustrations, pulse/progress animations, page navigation |
| Rename / Delete / Duplicate / Share modals | direct action or generic dialog substituted | exact prompts, details, options and confirmation flows |
| Toast / empty states | native generic components | same position, content hierarchy, duration and dismissal |

## Animation values

- Screen entry: 340ms, opacity 0→1, translateY 10dp→0, cubic-bezier(.22,1,.36,1).
- Bento entry: 420ms, translateY 12dp→0, scale .985→1; delays 20/70/120/170ms.
- Bar growth: 750ms, scaleX .08→1, origin follows reading direction; value changes 500ms.
- Navigation selection: 320ms, scale .82→1.08 at 60%→1.
- Calling wave: 900ms oscillation, delays 0/180/340/120/260ms.
- Welcome entry: 450ms, translateY 18dp→0, scale .97→1.
- Welcome floating elements: 4.2s (-6dp) and 4.8s (+5dp); pulse ring 2.4s; progress 1.2s after 150ms.

## Requested release checkpoint — 2026-11-01

The user explicitly requested publishing the current changes now and deferring further design work until they ask to resume. This is an intermediate correction, not a declaration of complete reference parity.

Implemented at this checkpoint: floating dock and scroll behavior; screen/card entrance motion; campaign search/filter/card corrections; the three-tile SIM forms; campaign detail layout, contact tools and status filters; integrated live controls and campaign selector; ten accent colors; plain settings cards and numeric timing fields; responsive phone-frame preference and handoff viewer; three welcome pages with reference copy and animated previews.

The 18 protected-code checks pass. Native Android hierarchy renders have been inspected for English, Arabic, dark theme, a narrow display, campaigns, detail, settings and welcome. These are native rendering checks, not pixel comparisons against the browser reference. The reference browser remains blocked by a saved browser permission even after the user approved opening it.

Still pending after this release: complete modal parity (share, rename, deduplication, deletion, contact import and stage classification), remaining results/history differences, precise live state/configuration parity, settings range-control details, complete animation choreography and direct reference-to-native visual comparison. Real-device telephony acceptance remains a separate user check.
