# BOOP unified status

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v70 developer lab + app-specific notification doods

Release identity:

- versionCode `70`;
- versionName `1.2.24-unified-dev-menu-doods`;
- final production/build commit `825593a16c004d9c0825720eb014c4f5cc8e58af`;
- app implementation `2f4a62150121b299a433674e971de1e058f6330f`, materialized-router test correction `9c907d3497067eb88ea1308875084b8965413951`.

v70 adds a local spoken `dev menu` command intercepted before HA/command-router/chat fallback. It opens non-exported `BoopDevMenuActivity` with no Chat Mode, OpenCode/ChatGPT Web or internet dependency.

The BOOP Dev presentation is fullscreen/immersive, black, scrollable and remote-friendly. Animation actions are Wake, Think, Stop, Berry 1, Berry 2, Berry 3, Shake and Sleep and call the real current BOOP face/animation methods rather than a fake engine.

Notification demos are local presentation fixtures for Facebook, WhatsApp, Gmail, X/Twitter, YouTube, Messenger, Instagram, Discord, Spotify and Reddit, plus Locked and Bundle. They use the real `BoopNotificationPuppetView`, current procedural eye stack and exact approved five-finger yellow hands. They do not post through Android `NotificationManager`, do not call the notification runtime/listener and do not require listener access merely to preview. The concept sheet `Glossy Boop App Icon Collection.png` supplied service identity/style direction only; old sheet eyes/hands are not runtime art.

Locked preview remains privacy-redacted through the production presentation model: app identity/icon/count style only before authentication, with no message title/body leakage.

## CI / artifact status

Final exact-head evidence:

- Build BOOP Unified APK workflow `34322564398`: SUCCESS;
- Shield HOME routing workflow `34322564357`: SUCCESS;
- artifact `BOOP-Unified`, ID `10092558111`, size `62,739,368` bytes;
- artifact ZIP SHA-256 `3c68ba78f2fb36bf50d6bbaf0d85a50a8a51dc5d345349b2dc70c32ae45c00e1`;
- APK SHA-256 `53c2956873e7a7268b829da5d9bd4f23d0f6ee20a0919051cb95bbd275f867a4`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused functional tests 58/58;
- Unified focused functional tests 144/144;
- Shield HOME routing PASS;
- materialization, notification/dev contracts, wake handoff, Launcher checks, signed assembly, package/version/signature/archive verification and artifact upload PASS.

The exact final artifact ZIP was independently downloaded and SHA-256 checked against GitHub's artifact digest. Its APK was independently extracted and SHA-256 checked against the CI receipt. The APK v2 signing block was independently parsed and the embedded signer certificate digest matched the canonical permanent BOOP signer.

Detailed receipt: `docs/BOOP-V70-DEV-MENU-DOODS-RECEIPT.md`.

## Acceptance boundary

**GitHub performed NO visual acceptance. Physical v70 acceptance is pending and belongs to Ryan.** No screenshot tests, golden-image tests, pixel comparisons or automated visual judgments were used.

Physical checks:

1. Say `dev menu` and confirm the lab opens locally before any HA/chat fallback.
2. Confirm fullscreen/immersive behavior and clean exit/return.
3. Exercise Wake, Think/Stop, Berry 1/2/3, Shake and Sleep repeatedly.
4. Exercise all ten app-specific notification doods plus Locked and Bundle; judge service identity, current BOOP face and exact hands on-device.
5. Confirm Locked exposes no title/body and previews add no real shade notification.
6. Recheck carried v68 iris-only hue and finished v65 sclera/white blend.

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
