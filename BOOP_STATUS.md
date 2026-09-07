# BOOP Shield Full-screen Deezer Status

Updated 2026-09-07. Owning experiment branch: `boop-shield-fullscreen-deezer-wip`.

## Current candidate

- Base: live `boop-shield-media-puppetry@4f57a5b45adec5d4872dd83ec9301093f4c4d5d5`.
- Package: `com.boop.shieldoverlay`; permanent BOOP signer preserved.
- Candidate identity: versionCode 2 / `0.2-fullscreen-puppetry`.
- Existing Deezer media-session observation, controller selection, H1 asset and motion clock are preserved.
- Eligible Deezer headphone states expand the noninteractive application overlay to the complete Shield display on a pure-black canvas.
- PLAYING now uses richer `FullscreenPuppetMotion.groove` acting while preserving the accumulated media clock.
- PAUSED eases the current playing pose to neutral over 520 ms.
- Explicit skip states 9/10/11 trigger a 700 ms perk/lift/tilt acknowledgement, peaking around 180 ms.
- Other REST states remain neutral; equivalent non-skip REST transitions remain quiet.
- Remote input remains pass-through because `FLAG_NOT_FOCUSABLE` and `FLAG_NOT_TOUCHABLE` are unchanged.
- BOOP Home keeps its existing hide/show behaviour; fallback `EYES` keeps the compact transparent overlay.
- Useful debug/diagnostic machinery is preserved for later work rather than deleted.
- No new microphone, accessibility, UsageStats, foreground-app, HA or network permission/path was added.

## Verification

TDD RED run `34097940701` failed as intended because the motion test referenced missing `FullscreenPuppetMotion`.

Final green build commit: `0d9f5e6f2cc3249541667976a41405efd686a52a`.
GitHub Actions run: `34098619403`.

Passed: complete Python source regression suite, complete Shield unit suite, fullscreen acting-motion tests, preserved MediaPuppetState quiet-state contract, explicit track-change delivery, full groove/settle/accent geometry envelope tests, stable-signed APK assembly, package/permission inspection and permanent signer continuity.

Artifact: `BOOP-Shield-Fullscreen-Deezer` / ID `10009620031`.
APK SHA-256: `ea9fa94f0868943cf559b5b8e1406dcf28e07264a415aed7d58e7cc61c292a18`.

## Physical status

The earlier v1 full-screen experiment reached Ryan's Shield and exposed the large presentation. v2 puppetry itself is CI-green but physical acceptance is pending.

The WIP intentionally follows the existing Deezer-session contract, so full-screen BOOP can remain visible while Deezer continues playing in the background. Physical testing should decide whether that is desirable. Do not add broad foreground-tracking permissions without an explicit follow-up decision.

## Protected references

- `boop-shield-media-puppetry` remains untouched and owns the accepted corner-H1 lineage.
- `checkpoint-shield-home-f8e8135` remains protected.
- `checkpoint-shield-routines-3fa18c6` remains protected.
- Existing Deezer notification-listener access, HA auth/socket and app data are not changed by this branch.

Read `SESSION_HANDOFF.md` and `BOOP_SHIELD_FULLSCREEN_DEEZER_MEMORY.md` for the exact physical test checklist and implementation boundary.
