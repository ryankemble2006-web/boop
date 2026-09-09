# BOOP v63 notification presenter receipt

Date: 2026-09-09

## Release identity

- Canonical branch: `boop-unified`
- Package: `com.boop.alpha1`
- Version code: `63`
- Version name: `1.2.17-unified-notifications`
- Built source head: `2b0484cb9c95b0836c2bb6e93f6d7c890bc0e2a4`
- Merge tree: `1db12b4f85e02af099afefcfacbfd81c17fa8797`
- Parent 1, canonical concurrent work: `df8f8973431cc802d6fd16fb61c5b0c072eeda58`
- Parent 2, reviewed notification branch: `45fc81c95f9fece434d7c6a0ca7ae9eb8cc4c183`

The two-parent merge preserves the canonical queued `What If?` screensaver notes while importing the complete reviewed notification lineage.

## Exact artwork provenance

Canonical notification hands path:

`unified/assets/boop-notifications/boop-yellow-hands-approved.png`

Verified exact binary:

- size: `1,809,990` bytes
- SHA-256: `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`
- Git blob SHA-1: `d47037271bf320f4f110e3f8416f59882062afac`

Notification branch binary repair:

`45fc81c95f9fece434d7c6a0ca7ae9eb8cc4c183`

That exact branch build passed GitHub Actions run `34308367325`; artifact `BOOP-Unified` ID `10087535360`, digest `sha256:5f83b3d4d76d8bad8c0394445a07d8a973eea93886f7ed88c6845913b7c166a3`.

Animation root master was repaired to the same exact Git blob at:

`animation-freddie-mercury@783d38d0cbc18e3e93ed305ba36446da79f27ce3`

No regeneration, recompression or hash-guard weakening was used.

## Notification contract shipped in v63

- Reusable `BoopNotificationPuppetView` across in-place Wall, unlocked overlay and locked presentation.
- Locked presentation exposes only app identity/icon/count before authentication.
- Source notification `PendingIntent` is preserved.
- Successful `FLAG_AUTO_CANCEL` mirror cancellation occurs only after source send succeeds.
- Swipe/timeout dismisses BOOP's mirror only; Android's shade notification remains authoritative.
- Swipe threshold >=72dp on a dominant axis with strict >1.25x directional dominance.
- Entrance recipe: card alpha/translation from -16dp over 260ms with `OvershootInterpolator(0.7f)`; hands scale 0.96 -> 1 over 220ms.
- Deterministic 320ms BOOP notification cue with vibration waveform `{0,35,55,28}`.
- BOOP audio/vibration only when `playCue=true` and the native channel is known silent for both sound and vibration; unknown/noisy channels remain visual-only.
- Manifest permits required overlay/screen-on/vibrate authority and rejects full-screen-intent, query-all, accessibility-service and device-admin authority.

## Canonical CI and signed artifact

Main build workflow:

- Run: `34308822296`
- Result: SUCCESS
- Head SHA: `2b0484cb9c95b0836c2bb6e93f6d7c890bc0e2a4`

Completed PASS steps include:

- non-visual integration contracts;
- one-APK materialization;
- notification presenter contracts;
- seamless wake-command handoff;
- preserved Launcher source;
- approved-artwork copy without visual inspection;
- Shield control behavior;
- wake names, routing, lifecycle and assistant policy;
- permanent BOOP signer preparation;
- signed unified APK assembly;
- package, signer and archive verification;
- signed artifact upload.

Signed artifact verification payload reports:

- Shield focused functional tests: 58 tests, 0 failures, 0 errors, 0 skipped;
- Unified focused functional tests: 136 tests, 0 failures, 0 errors, 0 skipped.

Artifact:

- Name: `BOOP-Unified`
- ID: `10087693779`
- ZIP size: `62,721,296` bytes
- ZIP digest / SHA-256: `5673289f3a11cceceec99cfdeac2506c17eae0fbdfd86560b3c407f49c9e96ea`
- APK SHA-256: `e92963c4bff18b8b8fb2b88202aac3207186edb4af05113874d92e0e455e130f`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Package badging: `com.boop.alpha1`, versionCode `63`, versionName `1.2.17-unified-notifications`

Separate Shield HOME routing workflow:

- Run: `34308822310`
- Result: SUCCESS
- Head SHA: `2b0484cb9c95b0836c2bb6e93f6d7c890bc0e2a4`

## Verification boundary

GitHub functional/non-visual verification and permanent signing are green. This receipt does not claim visual, lock-screen or acoustic physical acceptance.

Ryan owns final Pixel acceptance. Required checks include unlocked notification presentation, privacy-safe lock presentation, source tap behavior, non-destructive swipe/timeout, duplicate-alert avoidance, silent-channel cue/vibration judgment, and the carried v62 single-layer listening-eye behavior.

Do not create or repoint a v63 physical rollback checkpoint until Ryan explicitly accepts this exact signed build on real hardware.
