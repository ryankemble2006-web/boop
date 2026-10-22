# EastEnders 2.0.0 — Standalone Android TV iPlayer Shortcut

A clean, zero-friction shortcut for Android TV and NVIDIA Shield designed to be shared directly as a standalone APK.

## How it works

1. **One Click Launch**: Clicking the shortcut launches official BBC iPlayer directly to the EastEnders programme page and finishes the shortcut activity immediately.
2. **Auto-Play Accessibility Helper**: When enabled in TV Settings > Accessibility:
   - Selects the focused user profile automatically if the profile chooser appears.
   - Selects the newest episode automatically on the EastEnders page.
   - Clicks `Skip trailer` once if a trailer skip control appears within 60 seconds.
   - When playback finishes and returns to the programme page, automatically calls `performGlobalAction(GLOBAL_ACTION_HOME)` to return the TV to the Home launcher.
3. **80-Year-Old Friendly Setup**:
   - Zero ADB, zero USB debugging, zero network debugging, zero developer options, and zero socket permissions.
   - If accessibility is disabled on first launch, shows a friendly 2-button screen: "Enable auto-play" (takes them to standard Accessibility settings) and "Watch EastEnders now" (launches iPlayer directly so they are never blocked).

## Building & Signing

- Build and verify: `build.ps1`
- Local packaging & signing: `build-local.ps1`
- Output artifact: `EastEnders-2.0.0.apk` signed with `signing/eastenders.p12`
