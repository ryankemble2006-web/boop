# BOOP unified handoff

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v70 developer lab + notification doods

v70 is `versionCode 70`, `versionName 1.2.24-unified-dev-menu-doods`.

Final production/build commit:

`825593a16c004d9c0825720eb014c4f5cc8e58af`

The app implementation landed at `2f4a62150121b299a433674e971de1e058f6330f`; `9c907d3497067eb88ea1308875084b8965413951` corrected the materialized-router regression assertion without changing production speech behavior. The final two commits only tightened the Shield HOME CI trigger so Unified release version bumps run that separate routing gate.

v70 adds the local spoken command `dev menu`. `BoopDevMenuIntent` intercepts it in BOOP's local recognized-speech path before Home Assistant / command-router / chat fallback, so it requires no Chat Mode setup, OpenCode/ChatGPT Web dependency, or internet connection. `BoopDevMenuActivity` remains `exported=false`.

The developer lab is immersive/fullscreen with a black background and remote-friendly scrollable shelves. Animation controls exercise the real current BOOP animation methods: Wake, Think, Stop, Berry 1, Berry 2, Berry 3, Shake and Sleep. Stop resets the indefinite Think path; finite animations can be replayed.

The notification shelf now has local fake/demo presentations for Facebook, WhatsApp, Gmail, X/Twitter, YouTube, Messenger, Instagram, Discord, Spotify and Reddit, plus Locked and Bundle previews. They use the real `BoopNotificationPuppetView`, current procedural BOOP eyes and the exact locked five-finger yellow hands. Service identity is supplied locally through dev-only identity/icon treatment. Demos do not post Android shade notifications, call `NotificationManager`, invoke `BoopNotificationRuntime`/the listener service, or require notification-listener access. Returning from a dood returns to BOOP Dev.

Locked preview still uses the production privacy-redaction model: before authentication it exposes app identity/icon/count style only and no message title/body. Unlocked demos use safe fake local text only.

The concept sheet `Glossy Boop App Icon Collection.png` was consulted as identity/style direction only. Its old embedded face/hands were not baked into runtime doods.

## v70 release evidence

- main workflow `34322564398`: SUCCESS;
- separate Shield HOME routing workflow `34322564357`: SUCCESS;
- artifact `BOOP-Unified`, ID `10092558111`;
- artifact size `62,739,368` bytes;
- artifact ZIP SHA-256 `3c68ba78f2fb36bf50d6bbaf0d85a50a8a51dc5d345349b2dc70c32ae45c00e1`;
- APK SHA-256 `53c2956873e7a7268b829da5d9bd4f23d0f6ee20a0919051cb95bbd275f867a4`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused functional tests 58/58, zero failures/errors/skips;
- Unified focused functional tests 144/144, zero failures/errors/skips;
- Shield HOME routing contract PASS.

The exact workflow artifact ZIP was downloaded and independently SHA-256 hashed; it matched GitHub's uploaded-artifact digest. The APK was independently extracted and SHA-256 hashed; it matched `apk-sha256.txt`. The APK v2 signing block was independently parsed and its embedded signer certificate SHA-256 matched both `signer-sha256.txt` and the canonical permanent BOOP signer.

Detailed receipt: `docs/BOOP-V70-DEV-MENU-DOODS-RECEIPT.md`.

## Test-first / debugging evidence

The existing v70 RED commit `4c81770aa869a46352115572c0757ca0f9876847` produced workflow `34321173070`, which failed on the deliberately missing v70 intent/action/identity implementation. Production implementation `2f4a62150121b299a433674e971de1e058f6330f` then exposed one brittle materialized-router assertion in workflow `34321836150`; root cause was the already-approved Chat Mode materializer rewriting the later router boundary to the guarded two-argument form. `9c907d3497067eb88ea1308875084b8965413951` fixed the assertion only and workflow `34321947467` went fully green.

The final release head additionally makes `unified/app-build.gradle` a Shield HOME workflow trigger. This prevents future Unified version bumps from silently missing the separate HOME routing gate. Final exact-head workflows `34322564398` and `34322564357` are both green.

## Physical acceptance boundary

**GitHub performed NO visual acceptance. Physical v70 acceptance is pending and belongs to Ryan.** No screenshot tests, golden-image tests, pixel comparisons or automated visual judgments were added.

Physical v70 checks:

1. Say `dev menu` from ordinary local BOOP speech and confirm BOOP Dev opens immediately without Chat Mode/internet.
2. Confirm BOOP Dev and dood previews are properly fullscreen/immersive and exit cleanly.
3. Exercise Wake, Think + Stop, Berry 1/2/3, Shake and Sleep repeatedly and judge the real motion.
4. Open each service dood and confirm the app identity is recognizable while the face uses the current procedural eyes and exact five-finger hands.
5. Confirm Locked preview exposes no message title/body, and Bundle behaves sensibly.
6. Confirm previews never add a real Android shade notification.
7. Recheck the carried v68 hue behavior and finished v65 sclera/white blend physically.

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
