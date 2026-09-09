# BOOP Shield clean launcher handoff

Updated 2026-09-09. Authoritative branch: `boop-shield-clean-launcher`.

## Product boundary

Standalone Nvidia Shield clean-HOME validation app. Package `com.boop.shieldhome`. Unified/AIO `com.boop.alpha1` remains separate and untouched. Do not merge into unified until Ryan explicitly approves after physical Shield testing.

## Protected physical baseline

Preserve the v0.8 HOME mechanism: single Home -> BOOP; double Home -> native Nvidia/Shield Recent Apps; reboot rearm; stock Android TV Home remains installed/enabled for recovery/trigger. Preserve accepted banners, grab/reorder, floating-square Apps drawer, 0.9.4 HOME geometry/chrome and normal Shield system behavior.

Ryan owns visual acceptance. GitHub must not run screenshot/golden/layout/animation/focus-scale judging.

## Physically proven Now Playing path

Notification Listener access is the proven Shield media-session authority. Deezer Now Playing and remote HTTPS album art are physically proven. Accessibility remains HOME-only.

Physical progress:
- v0.10.3 collision layout: **much better**;
- v0.10.4 spacing: **awesome spacing**;
- v0.10.5 album art: **physical PASS**;
- v0.10.6 focus outline: CI/signer green, physical judgement pending unless Ryan explicitly accepts it;
- v0.10.7 puppet stage: BOOP physically confirmed inside the right-hand bay;
- +10% size pass retained;
- playback dance: after system animations were re-enabled, Ryan reported BOOP **"looks awesome"**. Do not infer paused-sulk acceptance unless separately confirmed.

Album-art plumbing and protected HOME behavior should not be changed casually.

## Dance + DJ sulk baseline

Code 22 / `0.10.7-puppet-bay` keeps the approved headphones BOOP in the clipped 230dp bay at +10% size. Playing -> continuous dance; paused -> upset/sulk; other eligible non-playing states -> neutral rest; track/session change keeps the acknowledgement hop. Animator-disable and power-saver safeguards remain.

Signed dance receipt:
- source `cc54657a3ef5e97114e4b753c23b8801d56b2bda`
- workflow `34292426967` SUCCESS
- artifact `10081873605`
- APK SHA-256 `1d7607c5b8830b98d9d2235ec77a50f5b246875a67e583799c6281d3f5350a06`

## Current candidate: natural blink

Ryan asked to stop BOOP staring and approved a bounded natural-blink pass. Existing dance/sulk/media/HOME/focus/stage geometry remain unchanged.

Implementation:
- reused BOOP's proven 183ms close/reopen curve;
- irregular blink delay is 3–7 seconds;
- 18% chance of a double blink with a 110ms inter-blink gap;
- no new artwork was generated;
- `ShieldNowPlayingPuppetView` now uses a custom `ImageView` that draws near-black eyelid masks over the two eye ovals in the existing `boop_headphones` artwork;
- the approved image itself remains intact; dance transforms still apply to the same image so blink and dance compose rather than overwrite one another;
- blink stops when BOOP is hidden, detached, system animators are disabled, or Power Saver blocks animation;
- +10% size, clipping, non-focusability, accessibility exclusion, controls and album-art path are unchanged.

## Blink TDD / verification

RED:
- commit `c435f28e659c583b13cffd3aad0da8d245157226`
- workflow `34293639208`
- 77 tests completed, exactly 3 expected failures, all in `NowPlayingPuppetBlinkTest` because the blink helper did not yet exist;
- signing/build/upload skipped on RED;
- `BOOP_SKIP_MANUAL_VISUAL_TESTS=1` remained active.

GREEN signed candidate:
- exact APK source `b5221b842aff959d283e561aeac1f8b32e25a5ca`
- workflow `34293856616` SUCCESS
- artifact `BOOP-Shield-Clean-Launcher`, ID `10082387913`
- artifact ZIP SHA-256 `69a4588e41fd724e9493afd3956cd4783992ad12e95b159993559877041811ff`
- package `com.boop.shieldhome`
- version code 22 / `0.10.7-puppet-bay`
- APK SHA-256 `5078e83e1763d92b2d58cb7748e50693d12f7b4407ecdc2c3f5a531c5f6a0ccd`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

GitHub completed functional tests with no visual checks, prepared the permanent signer, built the signed launcher, verified package/version/signer and uploaded the artifact. The downloaded APK independently matched the CI hash, exact source commit, package, code/version and permanent signer.

## Next physical gate

Install the natural-blink APK on the real Shield. Confirm blink reads as eyelids rather than a mask, feels naturally irregular, does not interfere with dance or paused sulk, and leaves clipping/media controls/album art/remote navigation unchanged. Ryan's Shield is visual authority.

Do not begin a full Tegra/GPU/2.5D renderer or merge into unified until Ryan explicitly approves that wider step.
