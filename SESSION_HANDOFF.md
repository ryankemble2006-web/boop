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

Album-art plumbing is therefore physically green and should not be changed casually.

## Current candidate: v0.10.6 focus outline

Version code 21 / `0.10.6-focus-outline`.

User-requested visual change only:

- keep every existing focus scale/pop animation;
- add a **3dp cyan/blue focus border** to navigable items on HOME, Apps drawer and Launcher Settings;
- HOME favourite banners keep the accepted no-black-plate behavior: their outline is attached to the artwork itself;
- Apps drawer cards keep their existing selection plate and gain the outline around the card;
- HOME action buttons, Now Playing controls, Now Playing artwork and Launcher Settings rows use the same outline;
- one shared `FocusChrome` resolves Android `colorControlActivated` and supplies both the focus outline and the Now Playing progress fill, so they use the same runtime colour;
- no accepted layout dimensions or focus scale values were intentionally changed.

No automated appearance/focus test was added because Ryan explicitly owns real-device visual acceptance and BOOP_RULES forbids GitHub visual judging.

## Verification / release receipt

A pre-release compile/functional pass on source through `44214b1dba68f9e5e609c5f6a89f48e9246c4014` passed the fast functional lane before version stamping.

Final v0.10.6 release:

- APK source: `7ff4e64a0cdb1ca02dd4b4af5391ba1095b2569d`
- workflow: `34287673372` SUCCESS
- artifact: `BOOP-Shield-Clean-Launcher`, ID `10080137191`
- version: code 21 / `0.10.6-focus-outline`
- APK SHA-256: `048ca7fa179c21b4f307774f07eb7eabb6eb706444b228fa4b32391a9c746ea1`
- artifact ZIP SHA-256: `cedef689f8658a3d646579246c31fd6531246580e68c813610bfb4ab90d885cf`
- permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Final CI passed functional tests, signed assembly, exact package/code21/version, protected manifest/service/resource checks, permanent signer verification, APK integrity and artifact upload. `BOOP_SKIP_MANUAL_VISUAL_TESTS=1` remained active.

The downloaded artifact was independently unpacked. Its APK SHA matched CI exactly; package `com.boop.shieldhome`, code 21 / `0.10.6-focus-outline` and the permanent signer were independently confirmed.

## Next physical gate

Install/update v0.10.6 and inspect focus while navigating:

1. HOME top buttons and optional action tiles;
2. HOME favourite banners;
3. Now Playing artwork / Open player / enabled transport controls;
4. Apps drawer cards;
5. Launcher Settings rows.

Border should read as the same cyan/blue as the Now Playing progress bar and should add clarity without changing the accepted geometry or scale animation.

Real Shield behavior is authority. Do not merge into unified until Ryan explicitly approves the standalone behavior.
