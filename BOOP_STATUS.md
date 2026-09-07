# BOOP Shield Full-screen Deezer Status

Updated 2026-09-07. Owning experiment branch: `boop-shield-fullscreen-deezer-wip`.

## Current candidate

- Base: live `boop-shield-media-puppetry@4f57a5b45adec5d4872dd83ec9301093f4c4d5d5`.
- Package: `com.boop.shieldoverlay`; permanent BOOP signer preserved.
- Existing Deezer media-session observation, play/pause policy, H1 asset and motion clock are preserved.
- Eligible Deezer headphone states now expand the noninteractive application overlay to the complete Shield display.
- Deezer puppet mode paints a pure-black full-screen canvas and centres/enlarges H1 using measured motion-envelope geometry.
- Remote input remains pass-through because `FLAG_NOT_FOCUSABLE` and `FLAG_NOT_TOUCHABLE` are unchanged.
- BOOP Home keeps its existing hide/show behaviour.
- Fallback `EYES` state keeps the existing compact transparent overlay.
- No new microphone, accessibility, UsageStats, foreground-app, HA or network permission/path was added.

## Verification

GitHub Actions run `34096866418` passed for build commit `cd56e3bd0bf4ff6547e8cd2ea45631cde1418c36`.

Passed: complete Python source regression suite, complete Shield unit suite, new full-screen H1 geometry/envelope tests, signed APK assembly, package/permission inspection and permanent signer continuity.

Artifact: `BOOP-Shield-Fullscreen-Deezer` / ID `10008967780`.
APK SHA-256: `bb225e7c7f9fbfed13d758155b42208482921c4ef815d509e78dc5bb5edf5b4e`.

## Physical status

CI green only. No physical Shield install is claimed yet.

The first WIP intentionally follows the existing Deezer-session contract, so full-screen BOOP can remain visible while Deezer continues playing in the background. Physical testing should decide whether that is desirable. Do not add broad foreground-tracking permissions without an explicit follow-up decision.

## Protected references

- `boop-shield-media-puppetry` remains untouched and owns the accepted corner-H1 lineage.
- `checkpoint-shield-home-f8e8135` remains protected.
- `checkpoint-shield-routines-3fa18c6` remains protected.
- Existing Deezer notification-listener access, HA auth/socket and app data are not changed by this branch.

Read `SESSION_HANDOFF.md` and `BOOP_SHIELD_FULLSCREEN_DEEZER_MEMORY.md` for the exact test checklist and implementation boundary.
