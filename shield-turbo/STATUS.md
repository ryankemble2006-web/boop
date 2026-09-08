# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Current candidate

**v0.5.0 / code 7** is machine-verified and ready for physical Shield testing. Exact built source: `6f89c0d90fb08e7ef723226b10d33f45d6468f34`.

CLEAN START now replaces the failed app-op-only startup claim with real selected-package `force-stop` plus process/stopped/enabled-state verification. A reviewed target group can be cleaned manually, and optional AUTO CLEAN START schedules a bounded one-shot post-boot job using only the already-trusted local ADB key. It makes at most 3 attempts (30s/60s/120s), has no periodic/resident killer, skips the currently resumed app, records explicit outcomes and leaves packages enabled for manual launch. It does not guarantee an app never starts briefly during boot, and background-only playback is not separately detected in this release.

Old v0.4 Turbo startup changes remain undoable. HARD BLOCK remains separate/explicit. System/updated-system, Android/NVIDIA/Google-core and BOOP packages remain excluded from automatic cleanup.

## Physical acceptance

Pending for v0.5.0. First test one Kodi fork with `STOP + VERIFY NOW`, add it to CLEAN START, enable AUTO, reboot, confirm it is no longer lingering in the Shield task manager, then manually launch it normally. Only expand to the other forks after that succeeds.

Prior physical evidence retained:
- Bedroom brightness works.
- Corrected STANDARD maintenance items are selectable.
- Developer Options opens.
- v0.4.1 Startup Manager action menu works and Kodi forks launch manually.
- v0.4 background restriction failed: Kodi forks remained in Shield task manager and Ryan reported noticeable improvement after manual swipe/force-close.
- Display & Sound and Accessibility remain parked/unresolved.

## Exact verification

Run `34211569892`, job `102013555053`, success. **68 JVM tests passed; 18 source/API/security contracts passed; lint 0 errors / 22 warnings.** Package/version, permanent signer, archive integrity and nonvisual cold/warm launch smoke passed.

Signed artifact `10050102992`, ZIP `758585` bytes, SHA-256 `344e42969ec61c20dfda1da5748d3468024daeebadac3dd59b27067e8dddb59c`. Test artifact `10050154988`, ZIP `84726` bytes, SHA-256 `0c2916b5c435bcb0737b696f99c3b2ab4357854ba6135ce455b74e0cf6774624`.

Delivered APK `Shield-Turbo-v0.5.0.apk`, `2314138` bytes, SHA-256 `a7b8e25ea73e69976244a706abe301ad2e92b585d420a061480b6a1c760c2145`. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

No GitHub visual confirmation ran. Ryan owns real-device appearance, D-pad feel and CLEAN START acceptance. Exact behavior/limits and historical receipts are in SESSION_HANDOFF.md.
