# BOOP unified status

Updated 2026-09-07. Owning branch `boop-unified`.

## Current candidate

- One APK: package `com.boop.alpha1`.
- VersionCode 43 / `1.1.0-unified-dock-mirror-shield-settings`.
- Built code commit: `950611df0235d3943bf9958153efa470a342036b`.
- Last physically accepted unified rollback point: `e746affbb82b577cef2f1cf6e731dff186c8f881`; Ryan tested that APK and said it was fine to modify.
- Permanent BOOP signer preserved.
- Wall, Launcher and Shield remain compiled into one application from their pinned source inputs in `unified/SOURCE_HEADS.md`.
- Automatic routing remains Android TV/Leanback -> Shield; Pixel 7 Pro -> Wall; other handheld Android -> Launcher.

## v43 behavior

- Handheld Wall body: undocked means wake-word capture is disarmed and BOOP is tap-to-talk; wireless charging permits foreground wake-word listening.
- Docked eyes may sleep while the wake microphone remains available. A proximity nudge can request a short front-camera presence peek rather than continuous idle vision.
- Voice mirror mode accepts natural “BOOP mirror”/“Hey BOOP mirror” variants and natural close/stop/back variants. Mirror uses the front camera continuously only while explicitly open.
- Mirror reserves `INSIDE` and `OUTSIDE` side rails for future Home Assistant sensor data; no sensor entity mapping is invented in v43.
- Shield settings are redesigned for television use with large grouped cards, BOOP black/cyan styling, strong focus state, generous spacing and D-pad/Enter navigation.
- Shield Home controls are fail-closed to the installation's selected HA area. HA expands the selected area target, including device-level membership, then BOOP locally filters returned cards. No HA area/configuration is edited.

## Build evidence

Green built head: `950611df0235d3943bf9958153efa470a342036b`.
Workflow run: `34117631109`.
Artifact: `BOOP-Unified` / ID `10017287954`.
APK SHA-256: `95ba6292c04edaa4db2f1028337f0b3009a7c5006ee9b423e1bc40a9addef4fb`.

Passed: unified source-head/feature contract checks, preserved Wall guards, unified materialization, Launcher tests/lint, Shield unit tests including room-scope and navigation-model coverage, unified dock/wake/mirror unit tests, permanent signer setup, signed APK build, Shield-body emulator launch smoke, exact package/version/entry/manifest checks, signer continuity, APK archive integrity and artifact upload.

## Physical status

- `e746aff` is the physically accepted unified starting point for this pass.
- v43 itself is CI/signer/emulator green only until tested on real bodies.
- Still needs physical handheld/tablet checks for wireless-dock detection, mic release/re-arm, camera peek usefulness/thermals, mirror orientation and side rails.
- Still needs physical Shield remote inspection of the new settings page and real Home Assistant inventory confirmation that only the assigned room is shown. Automated room filtering and navigation-model tests are green, but that is not a substitute for sofa testing.
- Existing protected historical Wall/Shield checkpoints remain rollback/reference and were not repointed.

## Migration note

The unified package keeps Wall's `com.boop.alpha1` identity. Existing separate Launcher and Shield packages keep their own private Android state and grants; moving to unified BOOP may require one-time HOME selection, Shield special-access setup and other device-local setup. This remains expected migration work.

## Release discipline

Do not promote v43 to the physical checkpoint until Ryan tests it. If it fails physically, return to exact accepted `e746aff` rather than guessing by APK filename. GitHub remains the archive; deployment copies may keep current `BOOP.apk` plus one last-good APK.

## Official yellow hands, 2026-09-07

Design approved and locked across all BOOP bodies and animations. See `BOOP_YELLOW_HANDS.md` and `unified/assets/boop-yellow-hands/manifest.json`. The approved master is a hands-only transparent RGBA PNG, 1774 x 887, with five digits per hand and no arms. The master checksum is recorded; its binary transfer to GitHub is still pending manual upload from the supplied ZIP. No runtime integration was part of v43.

## Shield 2.5D animation archive, 2026-09-07

Design direction remains saved in `docs/animation/SHIELD_2_5D_HANDS.md`: deliberately explore Shield GPU-backed 2.5D puppetry, independent five-digit hands, headphone grips/adjustments and restrained layered depth. Five digits means four fingers plus one thumb on every hand throughout all poses/transitions. This v43 settings/dock pass did not implement that animation work.

For durable decisions from this pass, also read `docs/BOOP-UNIFIED-V43-MEMORY.md`.