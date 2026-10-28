# Campaign screen reference

Base: restored build 165 (0480711e66fe8260482ee13740af16d45cd0cf57).

The user's attached My TAI login screenshot is the visual source. Public URL https://mytai.app/login returned only Loading; no live DOM inspection or website font identity is claimed.

Reference lock: white neutral surfaces, soft gray fields, thin borders, rounded 14dp controls / 18–20dp cards, a single strong blue primary action, modest outline icons, generous but consistent spacing. Adapt the visual language to campaigns; do not reproduce a login form.

Only V7Workspace.kt changes application source. A local MaterialTheme scopes the palette and existing bundled Outfit/IBM Plex Sans Arabic font resources to this screen. Existing navigation, details, dialogs, dialing services, persistence, licensing, settings, and app icon remain the build 165 versions.

The header shows actual campaign/contact counts. Overview values come from Store and wrap into fewer columns as font scaling increases. Search gains an explicit clear action and focus border. Existing open/edit/delete/report/reset/dedupe actions are retained.

Native rendering, Arabic/English optical alignment and device font-scale verification are pending; no emulator/device available. This is not a pixel-identical reproduction of a web login page.
