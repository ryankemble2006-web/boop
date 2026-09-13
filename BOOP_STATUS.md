# BOOP v147 current status: installed on both devices

Updated 2026-09-13. Owner/worktree: `boop-unified-v146-integration`.

## Current build

- Package `com.boop.alpha1`; version `147 / 1.2.147-duplicate-cleanup`.
- App source commit `35bf096d4efe5bf40990dbc12d493cd2372e7c33`.
- GitHub Actions run `34732219690`: SUCCESS; artifact `10309931232`, `BOOP-Unified`.
- Signed APK SHA256 `2f56fc31645b34c69670d1b3abc4d3d63382d8bc762bf339151e43d59faa5db9`.
- Permanent signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Duplicate-render cleanup

- Phone notification/dev/voice portrait surfaces draw one canonical cropped eye pair instead of the whole approved eye atlas.
- Shield Now Playing masks the legacy baked-in headphone eyes before the canonical GLES eye surface draws.
- No animation clip, approved master artwork file, signing setting, permission or unrelated feature logic was removed.
- Regression guard `tests/test_unified_duplicate_puppet_renderers.py` was red against pre-fix sources and green after restoration.

## Verification and installation

- CI: 235 focused Unified tests + 68 focused Shield tests, zero failures/errors/skips; package/signature/ZIP integrity verified.
- Explicit `adb install -r` returned `Success` on Nvidia Shield and Pixel 10 Pro XL.
- Both devices report v147 and the installed `base.apk` on each device hashes exactly to the signed artifact above.
- BOOP was launched on both after install. No uninstall, data clear, permission grant or signer change.
- Ryan owns visual acceptance; duplicate appearance is not yet claimed physically green.

## Preserve

Three unrelated dirty Shield overlay source files remain local and excluded from this delivery: `BoopHomeActivity.java`, `HomeDashboardController.java`, `TvSettingsView.java`. Preserve them as concurrent work.

Use this v147 combined branch as the current Unified app. See `SESSION_HANDOFF.md` for the complete v146 history and exact v147 receipts.
