# O2Dialer coherent interface repair

Base: 2678e66037dd26eb60c3213eb3447d8d6d9b6bf3. Existing functionality and storage schema are retained.

## Reference lock

Primary: user's Clean Professional brief and screenshot corrections, interpreted through Refero's bundled typography, color, craft-details and anti-AI-slop references. Refero live research returned NO_SUBSCRIPTION; no live product reference is claimed.
Secondary: UI/UX Pro Max Flat Design guidance (native product fit only) and Jetpack Compose theme/layout guidance. Its generated marketing-page pattern and teal/orange palette were rejected as mismatched to the existing Android product and explicit blue identity.

| Decision | Source | Purpose |
|---|---|---|
| #F7F9FC canvas, white surfaces, #101828 text, #2457E7 action | User brief | Clear neutral light theme; blue stays action/progress-only |
| Neutral dark gray surfaces with light blue actions | Refero color / Pro Max theme rules | Avoid a blue cast on every surface |
| 14/16 body, 20/24/28 headings, real medium/bold | User font feedback / Refero typography | Readable hierarchy without bolding every paragraph |
| 16/20dp page spacing, 16dp grouped surfaces, 12dp controls | Refero craft / screenshot evidence | Stable rhythm and distinct control/container roles |
| Centered live contact, compact controls, neutral status surfaces | User screenshots | One focal task without a dark promotional hero |
| Results summary once; outcome bars with labels and values | Refero anti-slop card test / user stage feedback | Comparable data without redundant ring and donut cards |
| 48dp touch targets; wrapping rows at larger fonts | Pro Max native checklist | Usable small screens and larger accessibility text |

No photos or generated decoration: this is a calling workspace. Native icons and progress are functional. All production statistics remain Store-derived.

## Source audit

Reviewed workspace, detail/imports, live/queue/controls, results/history, settings, onboarding, licensing/update dialogs, shared forms/sheets, theme/motion/navigation, Store persistence and service command wiring. Identified dark hardcoded live colors, duplicate analytics, compressed labels, oversized decorative empty states, reduced-motion gaps in custom counters, missing selection semantics, and falsely optimistic deletion feedback.

## Verification

Release CI and device checks are recorded after implementation. Browser preview is a separate simulation, never evidence of native Android rendering. No emulator/device is available locally; optical alignment, RTL, 320dp/landscape, 2x text, TalkBack, keyboard and real telephony acceptance remain required.
