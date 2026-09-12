# Current phone animation crash repair: v145

Updated 2026-09-12. Owner: `boop-v144-devmenu-hue-experimental`,
worktree `.worktrees/boop-v144-devmenu-hue-experimental`.
This is the isolated phone animation experiment, not the active Shield branch.
The inherited Startup Manager notes below are history, not this task's scope.

## Current evidence

v143 phone animation delivery was received positively by the user. v144
(`97982839738141b4ea3e557754fd9f6f25c31bc5`) installed, but entering the eyes
crashed: saved-hue initialization requested a frame before `setRenderer` created
GLThread. The v145 constructor initializes renderer state without requesting a frame.
A second audit found spoken/settings developer entry owns an in-place menu in
MainActivity. It now uses the canonical face and all 26 catalogue clips, rather
than updating only the separate BoopDevMenuActivity.

Local materialization, 10 focused contracts, and full Unified compilation/unit
checks pass. Locked eye and hand hashes remain exact. Version is
`145 / 1.2.145-devmenu-hue-crashfix`. Signed build and phone verification pending.
Keep permissions, signing, animation timelines/assets and Shield installation unchanged.
Next: signed workflow, exact artifact verification, phone-only update, then
repeat the former Launcher-to-eyes crash path and inspect crash/renderer logs.

---
## Inherited historical notes

# BOOP Startup Manager continuation

Updated 2026-09-12. Current scope: **Startup Manager only. Lyrics is WIP and must stay out.** This explicit Ryan correction supersedes the preceding v133 integration plan.

## Owning checkout and branch

- Repository: `ryankemble2006-web/boop`.
- Task branch/worktree: `boop-v125-animation-integration`.
- Primary checkout is not this app's current worktree. Follow root AGENTS.md and fetched main rules.
- Last verified remote before this correction: `7e0d17907f5b3e59ecf108cbcc58911a283ec6c9`.
- The complete previous handoff is preserved byte-for-byte at [docs/history/startup-v133/SESSION_HANDOFF.md](docs/history/startup-v133/SESSION_HANDOFF.md). Its dated release claims are historical, not the current scope.

## Candidate scope

Version `134 / 1.2.134-startup-manager-only`, package `com.boop.alpha1`, permanent signing workflow unchanged.

The app and tests use the Startup Manager repair from `42fe4be8ddfb196a34be73c713d434b235663415`, before the lyrics merge. The Lyrics button, browser/policy classes, bridge entry point, shared-screen additions and lyrics-specific tests are excluded. The `boop-now-playing-lyrics` branch and worktree are not changed.

Existing approved music artwork is retained exactly as already present in the v133 source and the observed installed v132 lineage. No new artwork is created or edited. The only new feature being delivered is Startup Manager.

The approved Startup Manager implementation includes the sidebar, responsive Overview cards, package controls/detail pane, Restore, Android 11-safe parsers, exact original-state recovery, verified package actions, migration, cancellation and minimal recovery protection.

## Verification level

Before this scope correction, the pre-lyrics repair passed local Android compilation, the full 277-test Unified unit gate and 15 focused Startup Manager suites. The synthetic dex harness also ran on the actual Shield Android 11 runtime without changing installed package state. These are recorded prior results, not a claim that v134 has been built or installed.

v134 source is assembled from those exact pre-lyrics Git objects plus its version change and the unchanged approved art. A new permanent-signed build and physical checks are still required.

v128 crashed on `String.lines()` and is not a working package-control checkpoint. v127 rollback was preserved privately. The last actual installed version observed before the connection slipped was v132; re-read installed version and preserve its exact rollback APK before installing anything.

## Connection and next step

Remote Desktop Commander currently answers a ping but rejects terminal/file calls with `Not connected`. No local files or Shield state have been changed during this scope correction. The correction is being published directly through the connected GitHub tools.

After the Yoga bridge reconnects: fetch this branch, preserve any dirty/concurrent work, fast-forward only if clean, run the existing build workflow for the exact current source, verify artifact hash/package/version/permanent signer, preserve the installed APK, then install. Do not install the superseded lyrics-containing v133 artifact. Recheck live branch and installed version immediately before deployment.

Ryan authorized installation and one music-track-change completion signal after the verified candidate is genuinely installed. Do not signal completion while installation or the Package Control crash check remains unfinished. No permission grants, root, signer changes, automatic debloat or arbitrary package disabling are authorized. Destructive package and stock-launcher disable/Restore acceptance remains user-owned.

## 2026-09-12 v143 phone animation experiment
- Branch: `boop-v143-phone-animation-experimental`.
- Source commit: `955a2d14484776bc2bd7a645034d66a306bc0115`.
- Version: `143 / 1.2.143-phone-animation-experimental`.
- Purpose: wire the finished canonical Animation Lab engine into the phone/Wall production face, not only the embedded lab and Shield media path.
- Production phone face now materializes as `BoopCanonicalFaceView`, backed by `ProductionAnimationController` + `CanonicalEyeRenderer`, while keeping existing semantic hooks for wake/sleep/listening/thinking/Berry/shake and notification presentation.
- GitHub signed run `34677505803`: SUCCESS. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. APK SHA-256 `51b879797ab9f9a84da53a7b80c3b7ce0b7d1500ba92fa9270f7de1b134c43cf`.
- Pixel 10 Pro XL install: upgraded from v142 to v143 with `adb install -r`; post-install dumpsys confirmed versionCode 143 and versionName 1.2.143-phone-animation-experimental; launcher intent injected successfully. v142 rollback APK preserved privately before replacement.
- Physical animation appearance remains Ryan-owned manual acceptance. Shield was not modified by this install.
