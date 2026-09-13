# BOOP v148 status

- Branch: `boop-unified-animation-v148`
- Source checkpoint: `80cd81c1`
- Scope: exclusive face ownership for Voice Settings, Developer Menu and notification preview
- Red proof: four ownership tests fail on untouched v147
- Green proof: four ownership tests pass on v148; Java ownership harness passes 8 scenarios
- Clean detached build: materialization + Android compile + focused unit tests green
- Local release APK: not produced because stable BOOP signing remains GitHub-owned
- Preserve: three unrelated dirty Shield files remain local and excluded
- Next: v149 independent BOOP animation timing, then signed install/physical checks on Pixel 10 + Shield
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


# v149 signed/install verification - 2026-09-13

Source commit `23b4646ceb465a5438fcf8dc980651651616e1b5`, branch `boop-unified-animation-v149`.
GitHub Actions run `34738750812` completed SUCCESS. Artifact `BOOP-Unified` / ID `10311787700` was built from that exact commit. CI passed exclusive-face ownership, Android-scale-independent BOOP motion, canonical animation transplant, notifications/developer/Natural Voice, Shield control, wake/lifecycle, signing/package/archive and remaining workflow gates.

Signed APK package/version: `com.boop.alpha1`, `149 / 1.2.149-independent-motion`. APK SHA256 `1fea7893c4a26e190d064ab5f758daf822fac2ebdd1b649f163a3dd59e916847`. Permanent signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Explicit `adb install -r` returned Success on Nvidia Shield and Pixel 10 Pro XL. Both report v149 and both installed `base.apk` hashes exactly match the signed artifact. No uninstall/data clear/permission grant/signing change was performed.

Physical Shield independent-motion verification: Android window, transition and animator scales were all 0. Unified routed to `com.boop.alpha1/com.boop.shieldhome.ShieldLauncherActivity`; the resumed activity was explicitly confirmed before capture. Three screenshots 350 ms apart produced three different hashes, proving live Unified motion continued with Android animation scales disabled. Johnny Castaway can take the dream foreground later; any hierarchy captured while it owned foreground was discarded.

Phone duplicate visual acceptance is still pending. The physical Pixel is securely locked and normal `wm dismiss-keyguard` did not remove the keyguard; no PIN was entered or bypass attempted. A fresh Pixel 10 emulator installed v149, but its Android System UI entered the already-known ANR state, so its UI hierarchy was also discarded. Source red/green ownership tests and CI are green, but they are not substituted for Ryan's real-device visual acceptance.
