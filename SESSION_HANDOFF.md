## 2026-09-10 Deezer compatibility patch, v23 candidate

During approved native Deezer control work, Android UI hierarchy reads reconnected
ShieldHomeOverrideService. Its unconditional onServiceConnected launch brought
Home over Deezer even though Ryan was not using the remote. Logs and source agree.
The patch rearms Home once per device boot, using Android boot count and private
preferences. Real stock-Home window events still use the existing override path.
No permission, screen-content access, default Home, artwork or media changes.

Version 23 / 0.10.8-service-reconnect is a compatibility candidate on the existing
standalone lineage, not a new app. Focused policy tests cover same-boot reconnect,
new boot, first activation and unavailable boot count. GitHub performs only the
focused functional test, build, signer and integrity checks; no visual tests.
Signed source: ccb10658bded07dbf2a91ee8234509999f960a49. GitHub run
34446163444 SUCCESS; artifact 10139755652. ZIP SHA256
9643be98a41ccb6c4173bdf8e52994fbc46239029ebac0189aa8deb05d75537d.
APK SHA256 6bcc46633c38f61f4c7c5b4b0a49a4d3eec7b05818a25ef29abd4d36e82f411e.
Permanent signer independently verified after download. Two focused policy tests pass.
Ryan explicitly approved installing this exact v23 update; install succeeded after
preserving the actual installed v22 APK privately for rollback. Repeated hierarchy
reads now leave Deezer in front. Native track, Flow and artist switching component
tests proceeded successfully after the fix, with agent-inspected playing indicators.
This is scoped device evidence; Ryan's acoustic/end-to-end acceptance remains separate.
Reboot and single/double Home physical acceptance remain pending.

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

## Approved-master puppet blink baseline

Rendering is deliberately layered:
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

The approved pair itself remains unchanged and hash-gated. Do not change blink timing, the eye master, launcher layout, media plumbing or bay geometry to repair headphone artwork.

## Earlier TDD/build evidence

The first approved-master layered build reached source `e28f073c8566803bb8553c9f6ad1f4a0fb302f69`, workflow `34297552148` SUCCESS, artifact `10083716832`, APK SHA-256 `d4e3fafa43d072b1a269aeb89c99d61754dc7e8bf35f306c0e864e1debaf8aa7`. It passed 81 focused functional tests, master hash gate, permanent signer, signed assembly and package verification with visual checks disabled.

## Latest physical correction: duplicate legacy eyelid

Ryan physically tested the layered build and sent a Shield video. Verdict: **otherwise good, but BOOP visibly had two upper eyelids**.

Root cause is now proven: the legacy `boop_headphones.png` raster predates the permanent eye master and already contains an old upper eyelid/brow arc above each raster eye. The layered renderer correctly added the approved eye master and one animated top lid, but the old baked-in headphone arcs remained visible behind it even while fully open. The blink timing/animation itself was not the duplicate source.

The repair is intentionally narrow:
- the exact legacy headphones PNG bytes are preserved unchanged as `boop_headphones_legacy.png`;
- `R.drawable.boop_headphones` is now a wrapper drawable that renders that legacy raster and masks only the two obsolete baked-in upper-lid arcs with black caps matching the black launcher bay;
- the old direct unmasked `boop_headphones.png` resource is no longer selectable;
- the exact approved eye master and its pair geometry are untouched;
- the animated top lid remains the only animated eyelid;
- no bottom lid was added;
- no change to blink cadence, double-blink logic, dance, upset motion, +10% size, bay clipping, focus, favourites, album art, media plumbing or HOME behavior.

TDD for this physical bug:
- RED resource-contract run: workflow `34299420096`; 82 tests completed with exactly one expected failure because the legacy-lid masking drawable did not yet exist; signer/build/upload did not run;
- production resource commit: `f7d68c7bd2a0469834aaf4f85fbd242774fd9eba`;
- final source after making the test locate the Gradle project robustly: `71828a49239fd41f2b7dd81fdfb6a758c4839d22`;
- GREEN workflow `34299712206` SUCCESS;
- full focused Shield HOME test task passed with `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`;
- locked eye SHA gate passed;
- permanent signer preparation passed;
- signed standalone assembly passed;
- package/version/manifest/services/resources/signer/archive verification passed;
- artifact `BOOP-Shield-Clean-Launcher`, ID `10084479659`;
- APK SHA-256 `62886af8b7bac55bebb019555aba3f3b08bc0f060ae53780133b15b8a20f4bf0`;
- artifact ZIP SHA-256 `be286e1161df93705d065b0ff8cd790b0997c1662effa79b7b3b6f8e963190d4`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Scope review from pre-correction `c5a1bc620484238536a22fbdae8b304d23566b52` to green source `71828a49239fd41f2b7dd81fdfb6a758c4839d22` shows only the headphones raster rename, the masking drawable, and one focused regression test. No launcher/media/animation production code changed.

## Current physical gate

Install the signed source `71828a49239fd41f2b7dd81fdfb6a758c4839d22` candidate on the real Shield. The only pending visual question is whether the obsolete baked-in headphone arcs are fully hidden so BOOP presents one approved static upper lid per eye and the existing top-only blink still reads cleanly.

Do not claim physical single-eyelid acceptance from CI. Ryan's Shield remains the visual authority. Do not merge into unified or begin wider Tegra/GPU/2.5D work until Ryan explicitly approves.
