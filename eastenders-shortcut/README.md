# EastEnders 2.3.0 — 1-Click Standalone Android TV iPlayer Launcher

A 100% standalone, zero-menu Android TV launcher app for BBC iPlayer EastEnders. Designed to run on any Android TV, Google TV, NVIDIA Shield, or Fire TV without requiring any custom launcher, companion app, or adb tools. Perfect for parents and family.

## How it works

1. **Instant 1-Click Launch (Zero Menus)**:
   - Clicking the EastEnders app icon immediately launches BBC iPlayer directly into the EastEnders programme page.
   - Zero setup dialogs, zero extra menus, zero configuration buttons.
   - `Theme.NoDisplay` ensures no intermediate window ever flashes on screen.

2. **Automated Accessibility Flow**:
   - **First User Login**: Automatically detects the iPlayer profile selector ("Select who's watching") and selects the primary user profile.
   - **Newest Episode CTA**: Automatically finds the newest episode Hero CTA / "Watch now" / "Resume" button and triggers playback.
   - **Skip Trailers & Pre-rolls**: Actively monitors playback start and immediately clicks "Skip trailer" / "Skip pre-roll" as soon as it appears.
   - **Auto Return Home**: Once playback finishes (and credits roll back to the programme page), automatically returns the TV to the home screen via `GLOBAL_ACTION_HOME`.

3. **Standalone Dad-Friendly Setup**:
   - Zero ADB or PC required.
   - Sideload `EastEnders-2.3.0.apk` onto the TV.
   - Go to standard Android TV **Settings > Device Preferences / System > Accessibility** and toggle ON **EastEnders auto-play** (only needed once).
   - From then on, pressing the EastEnders button immediately plays the latest episode every single time.
   - Self-heals registration automatically on systems with `WRITE_SECURE_SETTINGS`.

## Build Artifacts

- Signed APK: `build/EastEnders-2.3.0.apk` (and on Desktop `EastEnders-2.3.0.apk`)
- Keystore: `signing/eastenders.p12` (`CN=EastEnders Personal Launcher`)
