# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch: `boop-shield-clean-launcher`.

## Product boundary

Standalone Nvidia Shield clean-HOME validation app. Package `com.boop.shieldhome`. Unified/AIO `com.boop.alpha1` remains separate and untouched. Do not merge into unified until Ryan explicitly approves after physical Shield testing.

## Protected physical baseline

Preserve the v0.8 HOME mechanism: single Home -> BOOP; double Home -> native Nvidia/Shield Recent Apps; reboot rearm; stock Android TV Home remains installed/enabled for recovery/trigger. Preserve accepted banners, grab/reorder, floating-square Apps drawer, 0.9.4 HOME geometry/chrome and normal Shield system behavior.

Ryan owns visual acceptance. GitHub must not run screenshot/golden/layout/animation judging.

## Physically proven Now Playing authority

Notification Listener access is the proven Shield media-session authority. Ryan manually enabled **Shield Settings -> Apps -> Special app access -> Notification access -> BOOP Now Playing**, and Deezer Now Playing appeared immediately. Accessibility is HOME-only again.

`Media access` reflects the Notification Listener grant and tries Android TV's exact Notification Access activity first, with generic/detail listener fallbacks.

## Physical progress through v0.10.5

- v0.10.3 removed the large headphones-BOOP collisions; Ryan reported it **much better**.
- v0.10.4 reduced transport controls and moved the top navigation row down; Ryan reported **awesome spacing**.
- v0.10.5 lifted the transport row by 4dp and added HTTPS MediaMetadata artwork support plus broader artwork-only notification fallback.
- Ryan physically confirmed **album art now works** on v0.10.5. It appeared without even skipping the current Deezer track.

Album-art plumbing is physically green and should not be changed casually.

## v0.10.6 focus outline

Version code 21 / `0.10.6-focus-outline`, source `7ff4e64a0cdb1ca02dd4b4af5391ba1095b2569d`, workflow `34287673372` SUCCESS, artifact `10080137191`, APK SHA-256 `048ca7fa179c21b4f307774f07eb7eabb6eb706444b228fa4b32391a9c746ea1`.

It adds the shared cyan/blue focus treatment requested for HOME, Apps drawer and Launcher Settings while preserving existing scale/pop behavior. This visual treatment remains physical-pending unless Ryan explicitly accepts it.

## Current candidate: v0.10.7 card-owned BOOP stage

Version code 22 / `0.10.7-puppet-bay`.

Ryan approved putting headphones BOOP into the open right-hand Now Playing space, while deliberately deferring a full Shield/Tegra 2.5D animation engine until placement is physically settled.

Implementation boundary:

- existing approved `ShieldNowPlayingPuppetView` and `boop_headphones` artwork are reused unchanged;
- existing groove / acknowledgement motion policy and power-saver / animator-disable behavior are preserved;
- `ShieldNowPlayingView` now owns the puppet directly inside the existing **230dp reserved right-hand bay**;
- the former Activity-root puppet overlay and its manual page-position lifecycle were removed;
- the puppet remains non-focusable, non-clickable and excluded from accessibility navigation;
- its stage is clipped by the puppet view, so motion stays inside the reserved bay;
- Now Playing snapshot binding drives the puppet directly; when media is hidden the puppet hides with the card;
- no new GPU/OpenGL/skeletal animation engine is introduced in this candidate.

This creates the intended clean future seam: later richer Tegra/GPU puppetry can replace the internals of the card-owned puppet stage without moving the launcher layout again.

## TDD / verification

Ownership contract RED:

- test commit `980b2d698032124b81a477e54c538171dbee1142`;
- workflow `34288555747`;
- **73 tests, exactly 1 failure**, `NowPlayingPuppetHostContractTest.nowPlayingCardOwnsPuppetInsteadOfActivityOverlay`, because `ShieldNowPlayingView` did not yet own `puppetView`.

Production transplant:

- card ownership commit `ff8efc77efb36ca31d17de7bcb00fcad6d634af3`;
- Activity overlay removal `17dcc253a25344ab424a01110666fe0b67529e51`;
- workflow `34288770043` then passed the full fast functional/build/sign/package lane.

Final v0.10.7 release:

- APK source: `8d6486ba51e747286847b7453189981266f13243`
- workflow: `34288943710` SUCCESS
- artifact: `BOOP-Shield-Clean-Launcher`, ID `10080598609`
- version: code 22 / `0.10.7-puppet-bay`
- APK SHA-256: `5bb188d520fcf0ed73a871c6ef60007a58d782226349113377fcf58f406c2ae9`
- artifact ZIP SHA-256: `a7b453e6922aa35fd227337318e9a371379bd2f03c7e5fbcd8d5b6f40b97332c`
- permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Final CI passed functional tests, signed assembly, exact package/code22/version, protected manifest/service/resource checks, permanent signer verification, APK integrity and artifact upload. `BOOP_SKIP_MANUAL_VISUAL_TESTS=1` remained active; no visual acceptance was run.

The downloaded artifact was independently unpacked. Its APK SHA matched CI exactly; package `com.boop.shieldhome`, code 22 / `0.10.7-puppet-bay` and the permanent signer were independently confirmed.

## Next physical gate

Install/update v0.10.7 and inspect the Now Playing card on the real Shield:

1. BOOP should appear inside the right-hand reserved bay rather than as a separate Activity overlay.
2. He must not overlap title, progress, Open player, transport controls or favourite apps.
3. Remote navigation must remain unchanged because the puppet is non-focusable/non-clickable.
4. Existing groove / track-change acknowledgement motion should still run within the bay.
5. Judge size and exact vertical/horizontal placement manually; minor position/scale tweaks can follow after use.

Do not begin the full Tegra/GPU puppetry pass until Ryan decides the card placement is worth locking. Real Shield behavior is authority. Do not merge into unified until Ryan explicitly approves the standalone behavior.
