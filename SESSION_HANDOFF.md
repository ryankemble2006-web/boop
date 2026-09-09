# BOOP Shield clean launcher handoff

Updated 2026-09-09. Authoritative branch: `boop-shield-clean-launcher`.

## Product boundary

Standalone Nvidia Shield clean-HOME validation app. Package `com.boop.shieldhome`. Unified/AIO `com.boop.alpha1` remains separate and untouched. Do not merge into unified until Ryan explicitly approves after physical Shield testing.

## Protected physical baseline

Preserve the v0.8 HOME mechanism: single Home -> BOOP; double Home -> native Nvidia/Shield Recent Apps; reboot rearm; stock Android TV Home remains installed/enabled for recovery/trigger. Preserve accepted banners, grab/reorder, floating-square Apps drawer, 0.9.4 HOME geometry/chrome and normal Shield system behavior.

Ryan owns visual acceptance. GitHub must not run screenshot/golden/layout/animation/focus-scale judging.

## Physically proven Now Playing path

Notification Listener access is the proven Shield media-session authority. Deezer Now Playing and remote HTTPS album art are physically proven.

Physical progress:
- v0.10.3 collision layout: **much better**;
- v0.10.4 spacing: **awesome spacing**;
- v0.10.5 album art: **physical PASS**;
- v0.10.6 focus outline: CI/signer green, physical judgement pending unless Ryan explicitly accepts it;
- v0.10.7 puppet stage: BOOP physically confirmed inside the right-hand bay;
- +10% size pass retained;
- playback dance: after system animations were re-enabled, Ryan reported BOOP **"looks awesome"**;
- paused upset/sulk exists, but explicit physical acceptance remains separate.

Album-art plumbing, HOME geometry, favourites, focus/navigation and media selection are protected from this blink pass.

## Permanent BOOP eye identity

Ryan visually re-confirmed the exact approved eyes PNG on 2026-09-09 and said to **lock him in forever, infinitely poseable**.

Authority is `BOOP_EYES_MASTER.md` and SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22` (1774 x 887 RGBA, 936803 bytes). The pair is immutable identity. Posing/animation is allowed around the master; regeneration, individual-eye drift, geometric normalisation or a newly redrawn substitute is not.

The one-source-pixel-left pair offset in the authority image is explicitly preserved by contract tests. Do not "fix" it.

## Current signed candidate: approved-master puppet blink

Source commit: `e28f073c8566803bb8553c9f6ad1f4a0fb302f69`.

Rendering is now deliberately layered:
1. headphones;
2. exact approved eye pair, drawn once as an undistorted master pair;
3. black animated top eyelids.

Blink behavior:
- animated top eyelid only; no bottom eyelid;
- rounded/curved black lid descends completely over each eye and reopens;
- each animated lid is clipped to its eye oval, preventing a black slab above/outside the eye;
- existing timing is untouched: 183 ms curve, random 3-7 s delay, 18% double blink, 110 ms double gap;
- dance, paused upset state, track acknowledgement hop, +10% size and clipped right-hand bay remain unchanged;
- hidden/detached, system-animation-disabled and Power Saver safeguards remain.

The legacy `boop_headphones` raster remains the headphones layer. Its old eye ovals are occluded only underneath the new approved eye layer; no launcher/media layout or plumbing was changed.

## TDD and build evidence

RED contract commit: `dba0c9a4823474619c211ca60c3d4ec62b03daf1`.
- workflow `34296744780`;
- 80 tests, exactly 3 expected failures for missing layered renderer/master/top-lid helper;
- signing/build/upload skipped.

First green implementation commit: `3340554465b88ac4bc40a50e1b4afbdae3a19393` passed functional/build/sign/package, but post-build review found that independently placing each eye could alter the master pair's relative geometry. It was not delivered as the final candidate.

Pair-geometry contract commit `247c1ecf252d63eef3fbb78791512568f4e5fd47` correctly exposed a mistaken test assumption: the approved source itself is one pixel left of mathematical centre. The renderer preserved it; the contract was corrected rather than changing the master.

FINAL GREEN:
- source `e28f073c8566803bb8553c9f6ad1f4a0fb302f69`;
- workflow `34297552148` SUCCESS;
- 81 focused functional tests passed;
- locked eye SHA gate passed;
- permanent signer preparation passed;
- signed assembly passed;
- package/version/manifest/services/resources/signer/archive verification passed;
- visual checks remained disabled (`BOOP_SKIP_MANUAL_VISUAL_TESTS=1`);
- artifact `BOOP-Shield-Clean-Launcher`, ID `10083716832`;
- APK SHA-256 `d4e3fafa43d072b1a269aeb89c99d61754dc7e8bf35f306c0e864e1debaf8aa7`;
- artifact ZIP SHA-256 `011abdd1723f4278a6801ee32bdfeb6f0997f0057e840f2da325dbcb2c812ba3`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

A scope compare from pre-task `cf0c859d4078afac8d82bc05641c4c80bf1694e9` to final source shows only the Shield build workflow, exact eye resource, two puppet-geometry helpers, `ShieldNowPlayingPuppetView` and their focused tests changed.

## Next physical gate

Install this APK on the real Shield. Ryan should judge only the real-device appearance: exact eyes seated correctly in the headphones, top lid reads as a puppet blink, complete closure/reopen, no top slab, no bottom lid, and no regression to dance/sulk/bay/media controls/album art/remote navigation.

Do not claim physical visual acceptance from CI. Do not merge into unified or begin wider Tegra/GPU/2.5D work until Ryan explicitly approves.
