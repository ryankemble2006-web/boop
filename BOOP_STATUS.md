# BOOP Shield clean launcher status

Updated 2026-09-09. Standalone branch `boop-shield-clean-launcher`.

- Package: `com.boop.shieldhome`
- Unified/AIO `com.boop.alpha1`: separate and untouched
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer: 0.9 floating square icons
- Physically-good HOME geometry/chrome: 0.9.4
- Notification Listener: physical PASS
- v0.10.4 spacing: physical **awesome spacing**
- v0.10.5 album art: physical **PASS**
- v0.10.6 focus outline: CI/signer green, physical judgement pending
- +10% puppet size retained
- Playback dance: physically reported **"looks awesome"**
- Paused upset/sulk: implemented; explicit physical acceptance not separately recorded
- Current candidate: code 22 / `0.10.7-puppet-bay`, exact approved eye master + top-only puppet blink
- Exact APK source: `e28f073c8566803bb8553c9f6ad1f4a0fb302f69`
- Workflow: `34297552148` SUCCESS
- Artifact ID: `10083716832`
- APK SHA-256: `d4e3fafa43d072b1a269aeb89c99d61754dc7e8bf35f306c0e864e1debaf8aa7`
- Artifact ZIP SHA-256: `011abdd1723f4278a6801ee32bdfeb6f0997f0057e840f2da325dbcb2c812ba3`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Functional/build/signer/package: GREEN
- Visual checks: DISABLED by design; physical blink appearance pending Ryan

## Permanent eye lock

Ryan re-confirmed the exact 1774 x 887 approved PNG and locked it as BOOP forever, infinitely poseable.

Authority SHA-256: `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`.

The build packages that exact master as `boop_approved_eyes.png` and hash-checks it before tests. The entire pair is rendered as one undistorted bitmap so eye scale, level, spacing and the approved one-source-pixel-left offset cannot drift. Pose/animation can transform the puppet; source identity must not be redrawn or normalised. See `BOOP_EYES_MASTER.md`.

## Puppet blink pass

- Layer order: headphones -> approved eye master -> animated eyelid.
- Top black curved lid only.
- No animated bottom lid.
- Top lid reaches beyond the eye bottom at the existing minimum openness, guaranteeing complete clipped closure.
- Animated lid is clipped per eye, so no black slab can render above/outside the eye.
- Existing 183 ms blink curve retained.
- Random interval remains 3-7 seconds.
- Double-blink chance remains 18% with 110 ms gap.
- Dance, paused upset motion, acknowledgement hop, +10% size, clipped bay, focus behavior and media plumbing unchanged.
- Lifecycle/animator/Power Saver safeguards unchanged.

Focused TDD finished with 81 functional tests passing on final source. The permanent eye hash gate, signed assembly, package/resource/manifest/service checks, signer check, archive integrity and artifact upload all passed.

## Fast CI rule

Ryan owns real-device visual acceptance. CI keeps `BOOP_SKIP_MANUAL_VISUAL_TESTS=1`; no screenshot, golden, emulator appearance, layout, focus-scale or animation visual acceptance runs. Functional logic, exact master bytes, signed assembly, package/version, manifest/service/resource presence, permanent signer, APK integrity and upload remain checked.

## Next gate

Install the signed candidate on the Shield and physically judge eyelid placement/closure. Confirm there is no upper slab, no bottom lid, and no regression to dance, upset state, clipping, album art, controls or remote navigation.

Real Shield behavior is visual authority. Do not merge into unified or expand into full Tegra/GPU puppetry until Ryan explicitly approves.
