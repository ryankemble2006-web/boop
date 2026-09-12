# BOOP current status

Updated 2026-09-12. Owning branch: `boop-v125-animation-integration`.

## Current delivery

**Startup Manager only. Lyrics is work in progress and excluded by Ryan's explicit instruction.** Candidate version is `134 / 1.2.134-startup-manager-only`.

The Startup Manager repair/UI is retained from `42fe4be8ddfb196a34be73c713d434b235663415`. Existing approved music art remains unchanged. The lyrics branch is untouched. v133's proposed combined integration is superseded and must not be installed for this request.

## Evidence and pending work

- Prior repair checks: 15 focused suites; local Android compilation; 277 Unified unit tests with no failures/errors/skips; synthetic Android 11 dex-harness checks with no installed-package mutations.
- Current v134: source scope corrected; new signed build and installation not yet verified.
- Last observed Shield install: v132. Recheck live before installing and preserve its exact rollback APK.
- Yoga terminal/files are unavailable despite a successful ping. GitHub is accessible. Local checkout synchronization is not claimed.
- No completion track change has been sent for this corrected candidate.

Follow [SESSION_HANDOFF.md](SESSION_HANDOFF.md) for the next safe step. The previous detailed status is preserved unchanged at [docs/history/startup-v133/BOOP_STATUS.md](docs/history/startup-v133/BOOP_STATUS.md); its earlier v133 integration plan is historical.

## 2026-09-12 v143 phone animation experiment
- Branch: `boop-v143-phone-animation-experimental`.
- Source commit: `955a2d14484776bc2bd7a645034d66a306bc0115`.
- Version: `143 / 1.2.143-phone-animation-experimental`.
- Purpose: wire the finished canonical Animation Lab engine into the phone/Wall production face, not only the embedded lab and Shield media path.
- Production phone face now materializes as `BoopCanonicalFaceView`, backed by `ProductionAnimationController` + `CanonicalEyeRenderer`, while keeping existing semantic hooks for wake/sleep/listening/thinking/Berry/shake and notification presentation.
- GitHub signed run `34677505803`: SUCCESS. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. APK SHA-256 `51b879797ab9f9a84da53a7b80c3b7ce0b7d1500ba92fa9270f7de1b134c43cf`.
- Pixel 10 Pro XL install: upgraded from v142 to v143 with `adb install -r`; post-install dumpsys confirmed versionCode 143 and versionName 1.2.143-phone-animation-experimental; launcher intent injected successfully. v142 rollback APK preserved privately before replacement.
- Physical animation appearance remains Ryan-owned manual acceptance. Shield was not modified by this install.
