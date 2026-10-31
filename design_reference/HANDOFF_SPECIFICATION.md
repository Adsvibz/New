# O2Dialer 7.0 — Native Android (Kotlin / Jetpack Compose) Technical Handoff Specification

## 1. Full Screen Inventory
### Primary Screens
1. **Onboarding / Welcome** (`src/components/OnboardingModal.tsx`) — 3-step introduction (Your Campaigns, Your Calling Workflow, Results & Follow-Ups).
2. **Campaigns Overview** (`src/App.tsx` + `src/components/CampaignOverview.tsx` + `src/components/CampaignCard.tsx`) — Central campaign workspace.
3. **Campaign Details** (`src/components/CampaignDetailsScreen.tsx`) — Full-screen contact & progress workspace for a single campaign.
4. **Live Dialer** (`src/components/dialer/LiveDialerScreen.tsx`) — Operational center for launching and controlling automated calling sessions.
5. **Results & Performance** (`src/components/ResultsScreen.tsx`) — Campaign outcome breakdown, 5-stage distribution, and CSV report export.
6. **Call History** (`src/components/CallLogsScreen.tsx`) — Chronological call log grouped by Today / Yesterday / Older dates.
7. **Settings** (`src/components/SettingsScreen.tsx`) — Structured utility interface covering all 12 native O2Dialer configuration categories.

### Secondary Sheets, Modals & States
8. **Create Campaign Sheet** (`src/components/NewCampaignSheet.tsx`) — Campaign Name + Outgoing SIM (`Default SIM`, `SIM 1`, `SIM 2`).
9. **Edit Campaign Sheet** (`src/components/EditCampaignSheet.tsx`) — Reuses Create Campaign design system.
10. **Add / Edit Contact Sheet** (in `CampaignDetailsScreen.tsx`) — Phone Number + Optional Name.
11. **Paste Numbers Sheet** (in `CampaignDetailsScreen.tsx`) — Multiline parser (`Name, +12025550142` or `+201001234567`).
12. **Import CSV/TXT Sheet** (in `CampaignDetailsScreen.tsx`) — UTF-8 CSV/TXT upload with 2MB safeguard and validation feedback.
13. **Select Phone Contacts Sheet** (in `CampaignDetailsScreen.tsx`) — Checks `READ_CONTACTS` permission before listing phonebook contacts.
14. **Campaign Multi-Selection Toolbar** (`src/components/MultiSelectToolbar.tsx`) — Long-press selection with running-session deletion lock.
15. **Call Result / Stage Classification Sheet** (`src/components/StageClassificationSheet.tsx`) — 5 core stages (`Potential`, `Not Interested`, `Call Back`, `WhatsApp`, `Voicemail`) + multiline feedback + external WhatsApp shortcut.
16. **Share CSV Report Modal** (`src/components/ShareCampaignModal.tsx`) — Generates `Name,Number,Status,Duration,Called At,Stage,Feedback` CSV.
17. **Duplicate Contacts Cleanup Modal** (`src/components/DuplicateContactsModal.tsx`).
18. **Stop Session & Delete Confirmation Dialogs** (`src/components/dialer/StopConfirmModal.tsx`, `src/components/DeleteConfirmDialog.tsx`).

---

## 2. Navigation Map
Five principal bottom navigation tabs (`src/components/FloatingBottomNav.tsx`):
1. **Dialer** (`dialer`)
2. **Results** (`results`)
3. **Campaigns** (`campaigns`) — Center anchor
4. **History** (`history`)
5. **Settings** (`settings`)

Opening a campaign from **Campaigns Overview** enters the full-screen **Campaign Details** workspace with a predictable top-left back action returning to Campaigns Overview.

---

## 3. Design Tokens (Color, Typography, Spacing, Radius)

### Color Palette
| Token | Hex | Compose Role |
|---|---|---|
| Main Background | `#FFFFFF` | `MaterialTheme.colorScheme.background` |
| Secondary Background | `#F7F8FA` | `MaterialTheme.colorScheme.surfaceVariant` |
| Quiet Surface | `#F4F5F7` | `SurfaceContainerLow` |
| Primary Text | `#111827` | `MaterialTheme.colorScheme.onSurface` |
| Secondary Text | `#8E95A2` | `MaterialTheme.colorScheme.onSurfaceVariant` |
| Hairline Border | `#E5E7EB` / `#F0F2F5` | `MaterialTheme.colorScheme.outlineVariant` |
| Primary Action | `#17191D` | `MaterialTheme.colorScheme.primary` |
| Accent Blue | `#2563EB` | Active Calling / Running indicator |
| Success | `#047857` | Answered / Verified status |
| Warning | `#B45309` | Paused / No Answer / Unreachable |
| Error | `#B42318` | Failed / Destructive actions |

### Typography Scale
- **English Primary**: `Outfit` (Regular 400, Medium 500, SemiBold 600)
- **Arabic RTL Companion**: `IBM Plex Sans Arabic` (Regular 400, Medium 500, SemiBold 600)
- **Monospace / Phone Numbers & Timers**: `JetBrains Mono` (`tabular-nums`, always `dir="ltr"`)
- **Scale**:
  - Main Screen Heading: `24sp` (`font-semibold`)
  - Section Title: `16sp–18sp`
  - Card Title: `14sp–15sp`
  - Standard Body: `13sp`
  - Supporting Metadata: `11sp–12sp`
  - Important Metric: `18sp–20sp` (`tabular-nums`)
  - Active Calling Contact Name: `25sp`

### Spacing & Shape System
- **Spacing Scale**: `4dp`, `8dp`, `12dp`, `16dp`, `20dp`, `24dp`, `32dp`
- **Horizontal Screen Margins**: `24dp` (`px-6`)
- **Card Borders**: `1dp` solid `#E5E7EB`
- **Corner Radii**: Buttons/Inputs `12dp` (`rounded-xl`), Cards `16dp` (`rounded-2xl`), Bottom Sheets `24dp` top corners.

---

## 4. Real Application Data Schema
All screens share a single source of truth (`src/types/campaign.ts`, `src/utils/campaignLogic.ts`):
- `Campaign`: `id`, `name`, `simSlot` (`-1` Default, `0` SIM 1, `1` SIM 2), `contacts: CampaignContact[]`
- `CampaignContact`: `id`, `name`, `phone`, `status` (`pending | calling | answered | no_answer | failed | unverified`), `attempts`, `callDurationSeconds`, `calledAt`, `stage` (`none | potential | not_interested | call_back | whatsapp | voicemail`), `notes` (`feedback`).
- `CampaignProgress`: Dynamically derived from `contacts` via `computeCampaignProgress(contacts)` — never hardcoded.
