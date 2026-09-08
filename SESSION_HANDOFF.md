# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch: `boop-shield-clean-launcher`.

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

## v0.10.7 card-owned puppet stage

Version code 22 / `0.10.7-puppet-bay` moved the existing approved headphones BOOP into the reserved 230dp right-hand Now Playing bay. `ShieldNowPlayingView` owns `ShieldNowPlayingPuppetView`; the former Activity-root overlay is gone. Existing groove/acknowledgement motion, power-saver behavior, non-focusable/non-clickable behavior and clipping are preserved. Full Tegra/GPU puppetry remains deliberately deferred until placement is physically settled.

Original v0.10.7 release receipt:
- source `8d6486ba51e747286847b7453189981266f13243`
- workflow `34288943710` SUCCESS
- artifact `10080598609`
- APK SHA-256 `5bb188d520fcf0ed73a871c6ef60007a58d782226349113377fcf58f406c2ae9`

Ryan then physically reported that BOOP was correctly inside the box but had not become meaningfully larger. The first fill refresh removed the old 88%-width / 75%-height artificial sizing and let the ImageView use the full stage bounds. Ryan then requested one more bounded visual tweak: **make BOOP 10% bigger**.

## Current candidate: v0.10.7 fill +10%

This remains a same-version visual refresh on code 22 / `0.10.7-puppet-bay`; no package, permission, media, HOME, focus, artwork, control, stage, or animation-policy changes were made.

Exact visual change:
- `ShieldNowPlayingPuppetView.onSizeChanged()` now sizes the puppet ImageView to 110% of the stage width and height;
- gravity remains centered;
- `FIT_CENTER` remains, preserving artwork proportions;
- the parent stage still clips children, so the enlarged puppet cannot escape into media controls;
- current groove/acknowledgement motion remains untouched.

Exact signed candidate receipt:
- source `242ad467d2cbe91d2de9d4cc5e44cf278ec20fa5`
- workflow `34291502544` SUCCESS
- artifact `BOOP-Shield-Clean-Launcher`, ID `10081530720`
- package `com.boop.shieldhome`
- version code 22 / `0.10.7-puppet-bay`
- APK SHA-256 `26e623be00928c263053427c9f07bd01540c2cf964a06b3f1bf55610fd00219d`
- artifact ZIP SHA-256 `177bab9198ddece6bd6bbfc0ebef40e47cebc860302c5376b40ac39a3be146f0`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

GitHub completed the functional-test lane with **no visual checks**, prepared the permanent signer, built the signed standalone launcher, verified exact package/version/signer, checked APK integrity and uploaded the artifact. The downloaded artifact was independently unpacked and matched the CI APK hash, exact source commit, package, code 22/versionName and permanent signer.

## Next physical gate

Install the v0.10.7 fill +10% APK over the current launcher and judge BOOP in the Now Playing bay. Confirm the extra size is right, clipping remains clean and remote navigation/media UI remain unchanged. Real Shield appearance is authority; do not infer visual acceptance from CI.

Do not begin the full Tegra/GPU puppetry pass until Ryan decides the stage placement/scale is worth locking. Do not merge into unified until Ryan explicitly approves the standalone behavior.
