# BOOP Launcher source

Standalone source branch: boop-shield-launcher-standalone.
Base: boop-shield-defaults@503cdb63d64716c9c1a568aadca97ba1d24680cd.
Original signed v145 source: 8fbe84411ec211f3adfec4409e41f3d49b2607fa.
New package: com.boop.shieldlauncher, version 1 / 1.0.0-launcher-tools.

The generated project includes only the existing Shield Home/Now Playing/advanced-tools module,
its local ADB helper and three small media/state types. Launcher settings and Close Player are
adapted to work without Unified or Home Assistant. The rejected v146 integration is not an input.
No phone, Wall, microphone, speech models, recipes, HA credentials, overlay puppet or Animation Lab.
Existing launcher artwork is copied byte-for-byte. Application icon composes the existing eyes.

User must separately enable desired Home-button/media access and authorize local tools on the
receiving Shield. Default package selections remain an explicit review/Apply action. Restore
records and ADB identities belong only to that device/app, with Android backup disabled.
The local close route retains the established Deezer/native YouTube targets and never force-stops
the shared Cast receiver. It uses the existing selected-session Stop action for Cast instead.
Signing remains in the existing GitHub secret-backed BOOP signer; no replacement key.
