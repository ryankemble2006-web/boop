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

Ryan then physically reported that BOOP was correctly inside the box but had not become meaningfully larger and requested that he fill the available bay.

## Current candidate: v0.10.7 fill refresh

This is intentionally a same-version visual refresh on code 22 / `0.10.7-puppet-bay`, because the connected GitHub write safety layer blocked editing the signing workflow verifier after the temporary code-23 bump. The branch was restored to the existing verified code-22 lane rather than leaving a broken release state.

Visual-only change:
- removed the previous 88%-width / 75%-height artificial puppet sizing;
- the puppet ImageView now occupies the full stage bounds;
- `FIT_CENTER` remains, so artwork proportions are preserved and no cropping is introduced;
- stage size, media layout, controls, focus behavior, artwork path and animation policy are otherwise unchanged.

Exact fill-refresh receipt:
- source `85a3278d52a4c359a58716644b949924abaf961b`
- workflow `34290757484` SUCCESS
- artifact `BOOP-Shield-Clean-Launcher`, ID `10081272677`
- package `com.boop.shieldhome`
- version code 22 / `0.10.7-puppet-bay`
- APK SHA-256 `f9509a3af47b4e50eac79fb96b173eba10216a625be1bf601a8bf148d4da7d21`
- artifact ZIP SHA-256 `a01b88140192e4c6db534ab19cb4e54965c46f569152ab521fc27b43f34318eb`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

CI passed the fast functional tests, signed assembly, exact package/version, protected manifest/service/resource checks, permanent signer verification, APK integrity and artifact upload. `BOOP_SKIP_MANUAL_VISUAL_TESTS=1` remained active. No visual acceptance was run.

The downloaded artifact was independently unpacked and matched the CI APK hash, package, code 22/versionName and permanent signer.

## FTP handoff rule / current result

Shared BOOP rules now say verified APK handoffs should also be uploaded to Ryan's private FTP `/apk/` folder when the current runtime can reach it. Credentials must never be committed to this public repository.

For this fill refresh, the direct FTP attempt failed with TCP connection refused on port 21 before authentication, so **no FTP upload occurred**. Do not claim a remote receipt for this build.

## Next physical gate

Install the v0.10.7 fill-refresh APK over the current launcher and judge BOOP in the Now Playing bay. He should now use the full bay bounds while retaining proportions. Confirm he remains clipped inside the bay, does not overlap media UI and remote navigation remains unchanged. Minor placement/scale tweaks can continue after use.

Do not begin the full Tegra/GPU puppetry pass until Ryan decides the stage placement is worth locking. Real Shield behavior is authority. Do not merge into unified until Ryan explicitly approves the standalone behavior.
