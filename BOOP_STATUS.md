# BOOP Launcher Alpha 2 status

## Current

Launcher Alpha 2 is on a physically useful baseline with a stronger fullscreen follow-up ready for Pixel verification.

Ryan physically confirmed the black Home screen and swipe-up app drawer. The `0.2.1` immersive build still showed Android's top status-bar clock on the Pixel, so `0.2.2` adds a window-level fullscreen flag in addition to the existing WindowInsets immersive path.

Current application source: `324ef2e8246ca787a93e39cf29aeb3325d13d148`.
Functional fullscreen commit: `14139c82a8dbba61ac14ff2aa30cfb4fbe152f7c`.
GitHub Actions run: `34081679120` — success.
Signed artifact ID: `10003901065`.
APK SHA-256: `987d0e9d39af44599ef648a7f3075b1cb38415cf0d9f883e42464a6c16bb5e89`.

## CI / smoke green

- Package `com.boop.launcher`, version `0.2.2` / code `5`.
- Android API 29 minimum, target/compile 36.
- Unit tests pass, including explicit `FLAG_FULLSCREEN` regression coverage.
- Android lint passes.
- Release assemble and permanent BOOP signer pass.
- Signed APK installs and survives launch on Android 16 smoke emulator.
- Window fullscreen flag is applied initially and again when launcher focus returns.
- Existing transient system-bar edge-swipe behavior remains.
- Drawer bottom-safe padding fix remains.

## Physical state

Physically confirmed: black Home screen and working app drawer.

Not yet physically confirmed on `0.2.2`: whether Pixel HOME finally suppresses the persistent clock/Wi-Fi/battery status bar. The Pixel result is authoritative for this behavior.

## Still incomplete

- Drawer motion is not yet full Launcher3 direct-finger/spring physics.
- Widget host flow exists but widget rendering/move/resize is incomplete.
- Dynamic multi-page workspace behavior is incomplete.
- Rotation/process-death and real widget flows still need current physical checks.

## Next

Install `0.2.2` / code `5`. Verify the top status bar is gone during normal HOME use, edge-swipe recovery still works, and the final app-drawer row remains clear. Continue the physical feel/bug list from there.

## Protect

- Preserve `boop-launcher-alpha1` as historical fallback until Alpha 2 is accepted.
- Keep package `com.boop.launcher` and existing BOOP signing identity.
- Do not publish signing keys/private certificates.
- Keep Wall and Shield app lineages independent.
- CI-green, smoke-green and physical acceptance are distinct states.
