# BOOP Shield clean launcher handoff

Updated 2026-09-09. Authoritative branch: `boop-shield-clean-launcher`.

## Product boundary

Standalone Nvidia Shield clean-HOME validation app. Package `com.boop.shieldhome`. Unified/AIO `com.boop.alpha1` remains separate and untouched. Do not merge into unified until Ryan explicitly approves after physical Shield testing.

## Protected physical baseline

Preserve the v0.8 HOME mechanism: single Home -> BOOP; double Home -> native Nvidia/Shield Recent Apps; reboot rearm; stock Android TV Home remains installed/enabled for recovery/trigger. Preserve accepted banners, grab/reorder, floating-square Apps drawer, 0.9.4 HOME geometry/chrome and normal Shield system behavior.

Ryan owns visual acceptance. GitHub must not run screenshot/golden/layout/animation/focus-scale judging.

## Physically proven Now Playing path

Notification Listener access is the proven Shield media-session authority. Ryan manually enabled **Shield Settings -> Apps -> Special app access -> Notification access -> BOOP Now Playing**, and Deezer Now Playing appeared immediately. Accessibility is HOME-only.

Physical progress:
- v0.10.3 collision layout: **much better**;
- v0.10.4 spacing: **awesome spacing**;
- v0.10.5 album art: **physical PASS**, appeared without skipping track;
- v0.10.6 focus outline: CI/signer green, physical judgement pending unless Ryan explicitly accepts it.

Album-art plumbing is physically green and should not be changed casually.

## v0.10.7 card-owned puppet stage and size passes

Version code 22 / `0.10.7-puppet-bay` moved the existing approved headphones BOOP into the reserved 230dp right-hand Now Playing bay. `ShieldNowPlayingView` owns `ShieldNowPlayingPuppetView`; the former Activity-root overlay is gone. Power-saver behavior, non-focusable/non-clickable behavior and clipping remain protected.

Original v0.10.7 source was `8d6486ba51e747286847b7453189981266f13243`, workflow `34288943710`, artifact `10080598609`.

Ryan physically reported BOOP correctly inside the box but too small. The first fill refresh removed old 88%-width / 75%-height sizing. The next approved visual pass made the puppet ImageView 110% of the stage while preserving `FIT_CENTER`, centered gravity and parent clipping.

+10% signed receipt:
- source `242ad467d2cbe91d2de9d4cc5e44cf278ec20fa5`
- workflow `34291502544` SUCCESS
- artifact `10081530720`
- APK SHA-256 `26e623be00928c263053427c9f07bd01540c2cf964a06b3f1bf55610fd00219d`

## Current candidate: playback dance + paused DJ sulk

Ryan approved this behavior: **dance continuously during playback; act upset at the DJ when paused**. Track/session change keeps the existing excited acknowledgement hop. No new artwork, hands, media-session plumbing, HOME behavior, focus behavior, stage geometry or permissions were added.

Implementation:
- `NowPlayingPuppetPolicy.Mode` now includes `UPSET`;
- playing -> `GROOVE`; paused -> `UPSET`; other eligible non-playing states such as buffering -> `REST`; ineligible/no media -> `HIDDEN`;
- playback groove is a faster smooth 2.4s loop with side-to-side lean, double bounce, head tilt and gentle squash/stretch;
- paused upset is a 5.2s sulk loop with a lowered/side-leaning stance and two periodic irritated head-shake windows;
- `ShieldNowPlayingPuppetView` runs continuous frames for both `GROOVE` and `UPSET`, resets the motion clock when media mode changes, and keeps the existing track/session acknowledgement layered over the current pose;
- 33ms frame cadence, animator-disable guard, power-saver guard, +10% size, clipping, non-focusability and accessibility exclusion are preserved.

## TDD / verification

RED commit `64ea1be637092738942e92bb5ed5f0e3201607bc`, workflow `34292204559`:
- 74 tests completed;
- exactly 2 expected failures: paused media did not yet return `UPSET`, and existing playback groove did not yet have the requested visible lean/squash amplitude;
- signing/build/upload steps were skipped on RED;
- `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`; no visual acceptance ran.

GREEN production source:
- source `cc54657a3ef5e97114e4b753c23b8801d56b2bda`
- workflow `34292426967` SUCCESS
- artifact `BOOP-Shield-Clean-Launcher`, ID `10081873605`
- artifact ZIP SHA-256 `cd52b6100b14f6b4890d9ca1f141b1be23bb0854fe297ad2a00a0fa1d7e1808f`
- package `com.boop.shieldhome`
- version code 22 / `0.10.7-puppet-bay`
- APK SHA-256 `1d7607c5b8830b98d9d2235ec77a50f5b246875a67e583799c6281d3f5350a06`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

GitHub completed functional tests with no visual checks, prepared the permanent signer, built the signed standalone launcher, verified package/version/signer, checked APK integrity and uploaded the artifact. The downloaded artifact was independently unpacked and matched the CI APK hash, exact source commit, package, code 22/versionName and permanent signer.

## Next physical gate

Install the dance/DJ-sulk APK on the real Shield. During playback BOOP should dance continuously. Pause media and he should slump/lean and periodically shake his head at the DJ. Confirm the +10% size remains right, clipping stays clean, album art/media controls remain untouched and remote navigation is unchanged.

Ryan's Shield is the authority for animation feel. If the dance or sulk needs tuning, change only motion parameters unless fresh evidence requires a wider change. Do not begin a full Tegra/GPU/2.5D renderer until Ryan explicitly chooses that architectural pass. Do not merge into unified until Ryan explicitly approves the standalone behavior.
