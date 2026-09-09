# BOOP unified handoff

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v70 developer lab + notification doods, physical hotfix

v70 remains `versionCode 70`, `versionName 1.2.24-unified-dev-menu-doods`.

### Physical v70 hotfix, current app head

Ryan's first physical v70 pass exposed two regressions:

1. saying `dev menu` made BOOP disappear instead of presenting BOOP Dev;
2. the `Dev menu` row in Voice Settings could sit below the physical touchable viewport because the settings column was not vertically scrollable.

Current hotfix app implementation:

`95224d95a04b6a79a8d8c4e9e5ac4c35cf64ca9a`

The spoken command still remains local and before HA/router/chat fallback, but it now uses a shared `openDevMenu()` path that releases wake PROCESSING state before switching activities and posts the explicit `BoopDevMenuActivity` launch through the interaction surface so the speech callback can unwind first. Runtime launch failure is caught and BOOP returns to the ordinary interaction face.

Voice Settings is now wrapped in a vertical `ScrollView` around the existing settings content column. The repair is materialized after the older Wall/eye/wake patches consume their established `MainActivity` anchors and before Notifications and Dev menu rows are appended. `Dev menu` and `Done` therefore remain reachable without reordering or changing their intended behavior.

No package, permission, version, signer, notification privacy contract, eye artwork, approved hands, HA behavior, Shield behavior or automatic-install policy changed.

Hotfix verification:

- Build BOOP Unified APK workflow `34381830799`: SUCCESS;
- exact workflow head `95224d95a04b6a79a8d8c4e9e5ac4c35cf64ca9a`;
- canonical materialization PASS;
- new v70 developer-menu/Voice-Settings regression contracts PASS;
- seamless wake-command handoff PASS;
- Launcher preservation/lint PASS;
- Shield focused functional tests PASS;
- wake/name/routing/lifecycle/assistant-policy tests PASS;
- signed APK assembly PASS;
- package/version/permanent-signer/archive verification PASS;
- artifact upload PASS;
- artifact `BOOP-Unified`, ID `10116298801`, size `62,739,475` bytes;
- GitHub artifact ZIP digest `sha256:8007675a308e3921fe6bde895c0d60bec7c5beb550a9ae31dac604f9112ed7ee`.

Detailed hotfix receipt: `docs/BOOP-V70-DEV-MENU-PHYSICAL-HOTFIX-RECEIPT.md`.

**Physical hotfix acceptance remains pending and belongs to Ryan.** Test the exact hotfix artifact by saying `dev menu`, then open Voice Settings and scroll/tap `Dev menu` and `Done`, then exit/repeat the spoken command to confirm wake/microphone behavior stays healthy. Do not create or repoint a v70 rollback checkpoint until Ryan confirms the physical pass. Latest physically accepted rollback remains v59.

### Original v70 release lineage

Original v70 final production/build commit:

`825593a16c004d9c0825720eb014c4f5cc8e58af`

The original app implementation landed at `2f4a62150121b299a433674e971de1e058f6330f`; `9c907d3497067eb88ea1308875084b8965413951` corrected the materialized-router regression assertion without changing production speech behavior.

v70 adds the local spoken command `dev menu`. `BoopDevMenuIntent` intercepts it in BOOP's local recognized-speech path before Home Assistant / command-router / chat fallback, so it requires no Chat Mode setup, OpenCode/ChatGPT Web dependency, or internet connection. `BoopDevMenuActivity` remains `exported=false`.

The developer lab is immersive/fullscreen with a black background and remote-friendly scrollable shelves. Animation controls exercise the real current BOOP animation methods: Wake, Think, Stop, Berry 1, Berry 2, Berry 3, Shake and Sleep. Stop resets the indefinite Think path; finite animations can be replayed.

The notification shelf has local fake/demo presentations for Facebook, WhatsApp, Gmail, X/Twitter, YouTube, Messenger, Instagram, Discord, Spotify and Reddit, plus Locked and Bundle previews. They use the real `BoopNotificationPuppetView`, current procedural BOOP eyes and the exact locked five-finger yellow hands. Service identity is supplied locally through dev-only identity/icon treatment. Demos do not post Android shade notifications, call `NotificationManager`, invoke `BoopNotificationRuntime`/the listener service, or require notification-listener access. Returning from a dood returns to BOOP Dev.

Locked preview still uses the production privacy-redaction model: before authentication it exposes app identity/icon/count style only and no message title/body. Unlocked demos use safe fake local text only.

The concept sheet `Glossy Boop App Icon Collection.png` was consulted as identity/style direction only. Its old embedded face/hands were not baked into runtime doods.

Original v70 release receipt: `docs/BOOP-V70-DEV-MENU-DOODS-RECEIPT.md`.

## Physical acceptance boundary

**GitHub performed NO visual acceptance. Physical v70 acceptance is pending and belongs to Ryan.** No screenshot tests, golden-image tests, pixel comparisons or automated visual judgments were added.

Current physical v70 checks:

1. Install the hotfix artifact from workflow `34381830799`; say `dev menu` and confirm BOOP Dev opens rather than BOOP disappearing.
2. Open Voice Settings, scroll to the bottom, and confirm `Dev menu` and `Done` are reachable and tappable.
3. Exit BOOP Dev, repeat `dev menu`, and confirm wake/microphone behavior remains healthy.
4. Confirm BOOP Dev and dood previews are properly fullscreen/immersive and exit cleanly.
5. Exercise Wake, Think + Stop, Berry 1/2/3, Shake and Sleep repeatedly and judge the real motion.
6. Open each service dood and confirm the app identity is recognizable while the face uses the current procedural eyes and exact five-finger hands.
7. Confirm Locked preview exposes no message title/body, Bundle behaves sensibly, and previews never add a real Android shade notification.
8. Recheck the carried v68 hue behavior and finished v65 sclera/white blend physically.

**No v70 rollback checkpoint was created or repointed.** Latest physically accepted rollback remains v59.

## Durable finished-eye ordering

Preserve the canonical procedural-eye stages in this order:

1. `scripts/patch-unified-reading-eyes.py`;
2. `scripts/patch-v64-procedural-sclera.py`;
3. `scripts/patch-v65-feathered-sclera.py`.

No later materialization stage may rerun the legacy bitmap hue-cache setter. User hue remains iris-only; default cyan/blue remains 190 degrees. Sclera, pupils, catchlights, black lids and approved eye artwork remain outside hue control.

## Durable notification contract

Android's original notification remains authoritative. BOOP is a puppet mirror around it. Locked state may expose app identity/icon/count only before authentication. Tap preserves the source `PendingIntent`; swipe/timeout dismiss BOOP's mirror only; duplicate-alert prevention remains in force.

Exact approved notification hands remain locked:

- path `unified/assets/boop-notifications/boop-yellow-hands-approved.png`;
- size `1,809,990` bytes;
- SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`;
- Git blob `d47037271bf320f4f110e3f8416f59882062afac`.

Do not regenerate, recompress, recolor/recolour, crop or weaken the hash contract.

## Physically accepted rollback state

Latest physically accepted exact rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`.

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference eye provenance. Never repoint protected/accepted checkpoints.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eye master, procedural iris-only hue, feathered sclera cleanup, blink timing/gates, headphones/puppetry, exact five-finger yellow hands, one 16 kHz microphone owner, local wake-name training, exact 100 ms wake bridge, uncensored speech request, physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
