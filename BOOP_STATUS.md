# BOOP Shield Full-screen Deezer Status

Updated 2026-09-07. Owning experiment branch: `boop-shield-fullscreen-deezer-wip`.

## Current candidate

- Base: live `boop-shield-media-puppetry@4f57a5b45adec5d4872dd83ec9301093f4c4d5d5`.
- Package: `com.boop.shieldoverlay`; permanent BOOP signer preserved.
- Candidate identity: versionCode 3 / `0.3-friendly-deezer-access`.
- v2 fullscreen puppetry remains: black full-TV stage, richer PLAYING groove, 520 ms PAUSED settle, 700 ms skip acknowledgement, remote pass-through, compact ordinary-eye fallback.
- v3 changes the Deezer special-access setup UX, not the renderer or media observer.

## Physical status

Ryan installed v2 and saw ordinary compact eyes rather than full-screen Deezer BOOP. BOOP Settings reported `Deezer headphones — Access needed` and Android notification-listener access `Not granted`.

The missing Android notification-listener grant is therefore the currently confirmed reason BOOP stays in `EYES`; do not infer why Android lost or withheld the grant. The earlier v1 full-screen/debug presentation had reached the Shield successfully.

## Friendly access flow

- First entry to BOOP Home offers `Let BOOP see Deezer playback?` once.
- `Continue` enables Deezer puppet and, if needed, routes to Android's BOOP-specific notification-listener detail page on API 30+.
- If that detail activity is unavailable, BOOP falls back to the generic Notification access list. Older Android uses generic directly.
- Returning to BOOP refreshes permission state immediately.
- `Manage Deezer access` uses the same detail-first/fallback route.
- `Not now` is persisted as an offered setup and does not nag; Settings remains available later.
- BOOP cannot and does not silently grant this Android special access. The user must toggle BOOP on in system UI.

## Verification

Friendly-access TDD RED: run `34100353543` failed as intended because the tests referenced missing one-time setup preference methods and the missing settings-route planner.

GREEN build commit: `b5e9e33bf1b98bcb94176afeea3fb21ad99ab9fa`.
GitHub Actions run: `34100989718`.

Passed on that exact build commit: 46 Python source regressions, complete Shield JVM/unit suite, friendly access routing/preferences tests, existing fullscreen puppet motion/state/geometry regressions, stable-signed APK assembly, package/version/permission inspection and permanent signer continuity.

Artifact: `BOOP-Shield-Fullscreen-Deezer` / ID `10010497159`.
APK SHA-256: `06ef591b117720334f6a9c2c6b7f0c8f8e66dc448a375ad9fb9b35b68fc617c9`.
Signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Physical test next

Install v3 over v2 without uninstall/data clear. On first BOOP Home entry choose Continue, toggle BOOP on in the Android Notification access page, return to BOOP and confirm status reaches `On`. Then start Deezer. If status is `On` but ordinary eyes remain, investigate Deezer media-session discovery/package/state next.

CI green is not physical green. Do not promote a checkpoint yet.

## Protected references

- `boop-shield-media-puppetry` remains untouched and owns the accepted corner-H1 lineage.
- `checkpoint-shield-home-f8e8135` remains protected.
- `checkpoint-shield-routines-3fa18c6` remains protected.
- Existing HA auth/socket, Home, Routines, pairing, debug machinery and signer are preserved.
