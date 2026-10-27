# Consolidated screenshot feedback

Implemented together on the V7 development branch:

- Shared centered metric columns for campaign overview, contact details, live outcomes/setup, reports and call history. Columns wrap when available width or accessibility font scaling requires it.
- Real Medium/Bold Outfit and IBM Plex Sans Arabic font weights, balanced line boxes and no Android font padding. Replaces Tajawal's uneven Latin baseline placement. Existing font licenses are bundled.
- Centered live contact block, with duplicate unnamed phone number removed. Informative service messages remain visible.
- Compact primary pause/resume action and secondary skip/stop controls, placed before outcomes; controls stack at narrow widths or larger text sizes. Stop still requires confirmation.
- Inset rounded bottom navigation, semantic selected tabs, animated selection/tint and spring touch feedback respecting reduced motion. Scaffold reserves its full height; system navigation and horizontal cutout insets are respected.
- Contact status, duration and stage use separate wrapping rows in both contact lists and results. Stage text is not truncated to a single line. Visual badges stay small while interactive targets retain 48dp height; WhatsApp follow-up uses a compact icon action.
- Import/contact action chips wrap rather than hiding off screen. Settings segments, stage choices and metric blocks adapt to text scaling; sheet actions remain outside scrolling content.
- Long-press campaign selection, toggle selection, select all/search results, selection count, confirmation and multi-delete. Back exits selection. Active/paused/waiting campaigns are protected again at mutation time; deletion uses one persistence update. Existing call history is retained.
- Edit action inside campaign details, with both entry points calling the same save-time session guard.

## Verification limits

CI covers release compilation, regression tests, lint, signing and public update publication. Source-level review covers the user's screenshots and all shared components changed here. No Android emulator/device is available in this workspace. Actual optical alignment, animation, keyboard behavior, 320dp/360dp/large phones, landscape, 1.0/1.5/2.0 text scales and Arabic/English must still be checked on-device. No claim that every phone layout has been visually verified.
