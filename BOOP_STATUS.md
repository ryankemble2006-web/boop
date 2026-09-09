# BOOP unified status

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v70 developer lab + app-specific notification doods

Release identity remains unchanged:

- versionCode `70`;
- versionName `1.2.24-unified-dev-menu-doods`.

### v70 physical dev-menu hotfix

Ryan's first physical v70 pass found two regressions: saying `dev menu` made BOOP disappear instead of presenting BOOP Dev, and the `Dev menu` row in Voice Settings could sit below the reachable viewport because the settings column was not vertically scrollable.

The current hotfix implementation is `95224d95a04b6a79a8d8c4e9e5ac4c35cf64ca9a`. Spoken `dev menu` now releases wake PROCESSING state before the activity hop and posts an explicit `BoopDevMenuActivity` launch through the interaction surface so the speech callback can unwind first. Voice Settings is now wrapped in a vertical `ScrollView`; the existing settings column, Notifications row, Dev menu row and Done button therefore remain reachable without moving the controls or changing their behavior.

Exact hotfix build workflow `34381830799` completed SUCCESS. Canonical materialization, the new v70 developer-menu/scroll regression contracts, wake handoff, Launcher lint, Shield functional tests, wake/routing/lifecycle tests, signed APK assembly, package/version/permanent-signer/archive verification and artifact upload all passed. Fresh artifact `BOOP-Unified` is ID `10116298801`, size `62,739,475` bytes, GitHub artifact ZIP digest `sha256:8007675a308e3921fe6bde895c0d60bec7c5beb550a9ae31dac604f9112ed7ee`.

Detailed hotfix receipt: `docs/BOOP-V70-DEV-MENU-PHYSICAL-HOTFIX-RECEIPT.md`.

**Physical hotfix acceptance is still pending.** Ryan must confirm that spoken `dev menu` opens BOOP Dev, that Voice Settings scrolls far enough to tap `Dev menu` and `Done`, and that returning/repeating the command leaves wake/microphone behavior healthy. Latest physically accepted rollback remains v59.

### Original v70 implementation

The original v70 production/build commit was `825593a16c004d9c0825720eb014c4f5cc8e58af`; app implementation `2f4a62150121b299a433674e971de1e058f6330f`, materialized-router test correction `9c907d3497067eb88ea1308875084b8965413951`.

v70 adds a local spoken `dev menu` command intercepted before HA/command-router/chat fallback. It opens non-exported `BoopDevMenuActivity` with no Chat Mode, OpenCode/ChatGPT Web or internet dependency.

The BOOP Dev presentation is fullscreen/immersive, black, scrollable and remote-friendly. Animation actions are Wake, Think, Stop, Berry 1, Berry 2, Berry 3, Shake and Sleep and call the real current BOOP face/animation methods rather than a fake engine.

Notification demos are local presentation fixtures for Facebook, WhatsApp, Gmail, X/Twitter, YouTube, Messenger, Instagram, Discord, Spotify and Reddit, plus Locked and Bundle. They use the real `BoopNotificationPuppetView`, current procedural eye stack and exact approved five-finger yellow hands. They do not post through Android `NotificationManager`, do not call the notification runtime/listener and do not require listener access merely to preview. The concept sheet `Glossy Boop App Icon Collection.png` supplied service identity/style direction only; old sheet eyes/hands are not runtime art.

Locked preview remains privacy-redacted through the production presentation model: app identity/icon/count style only before authentication, with no message title/body leakage.

Original release receipt: `docs/BOOP-V70-DEV-MENU-DOODS-RECEIPT.md`.

## Acceptance boundary

**GitHub performed NO visual acceptance. Physical v70 acceptance is pending and belongs to Ryan.** No screenshot tests, golden-image tests, pixel comparisons or automated visual judgments were used.

Current physical checks:

1. Install the hotfix artifact from workflow `34381830799` and say `dev menu`; confirm BOOP Dev opens rather than BOOP disappearing.
2. Open Voice Settings, scroll to the bottom, and confirm `Dev menu` and `Done` are both reachable and tappable.
3. Exit BOOP Dev back to BOOP, repeat the spoken command, and confirm wake/microphone behavior remains healthy.
4. Confirm fullscreen/immersive behavior and clean exit/return.
5. Exercise Wake, Think/Stop, Berry 1/2/3, Shake and Sleep repeatedly.
6. Exercise all ten app-specific notification doods plus Locked and Bundle; judge service identity, current BOOP face and exact hands on-device.
7. Confirm Locked exposes no title/body and previews add no real shade notification.
8. Recheck carried v68 iris-only hue and finished v65 sclera/white blend.

No v70 rollback checkpoint was created or repointed. Latest physically accepted rollback remains v59.

## Durable eye ordering

Canonical procedural-eye stages remain:

1. `patch-unified-reading-eyes.py`;
2. `patch-v64-procedural-sclera.py`;
3. `patch-v65-feathered-sclera.py`.

No later materialization stage may rerun the legacy bitmap hue setter. User hue remains procedural-iris-only. Sclera, pupils, catchlights, black lids and approved eye artwork remain outside hue control.

## Locked notification hands

Canonical asset `unified/assets/boop-notifications/boop-yellow-hands-approved.png` remains exactly:

- size `1,809,990` bytes;
- SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`;
- Git blob `d47037271bf320f4f110e3f8416f59882062afac`.

Do not regenerate, recompress, recolour/recolor, crop or weaken this guard.

## Preserved contracts

Android's original notification remains authoritative; BOOP mirrors it. Production locked presentation remains identity/icon/count-only before authentication. Preserve approved black-lidded eye master, procedural iris-only hue, feathered sclera, blink, notification privacy/tap/dismiss semantics, headphones/puppetry, one microphone owner, wake/name architecture, exact 100 ms wake bridge, uncensored speech request, HA names/Home controls, room isolation and idempotent Shield scaling.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / package `com.boop.shieldhome` until Ryan explicitly approves a later merge. No automatic installs/grants or signer/package changes.

## Physically accepted rollback

Latest exact physically accepted rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`.

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected eye provenance. Never repoint protected/accepted checkpoints.
