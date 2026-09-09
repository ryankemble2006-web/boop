# BOOP unified handoff

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v70 in-place developer-menu hotfix

Release identity remains `versionCode 70`, `versionName 1.2.24-unified-dev-menu-doods`.

Ryan physically confirmed that the earlier activity-hop hotfix at `95224d95a04b6a79a8d8c4e9e5ac4c35cf64ca9a` still forced BOOP to close when he said `dev menu`. Treat that candidate as physically failed for this bug even though its CI was green. Its Voice Settings vertical-scroll repair remains retained.

The current repair removes the fragile developer-menu activity hop from both spoken entry and the Voice Settings button. The exact spoken trigger is now `developer menu`; old `dev menu` intentionally does not match. The local route remains before Home Assistant / command-router / chat fallback, releases wake PROCESSING state, and calls `showDeveloperMenu()` inside the existing `MainActivity`. BOOP Dev is rendered as a fullscreen overlay on the current `interactionSurface`; the spoken/settings route does not call `startActivity()` or reference `BoopDevMenuActivity.class`.

The in-place developer lab preserves the current controls and local preview behavior: Wake, Think, Stop, Berry 1/2/3, Shake, Sleep, Facebook, WhatsApp, Gmail, X/Twitter, YouTube, Messenger, Instagram, Discord, Spotify, Reddit, Locked and Bundle. Notification demos remain local fixtures only and never post Android shade notifications or expand authority.

Voice Settings remains vertically scrollable. Its row is now labelled `Developer menu`, and `Done` remains reachable.

### Exact CI/signer-green app head

`c17e98d9a09471cf8f53f2bee171a77e3b3b1203`

Verification:

- Build BOOP Unified APK workflow `34385817960`: SUCCESS;
- exact workflow head `c17e98d9a09471cf8f53f2bee171a77e3b3b1203`;
- materialized developer-menu contract: PASS;
- exact `developer menu` JUnit intent contract: PASS;
- old `dev menu` rejection contract: PASS;
- seamless wake-command handoff: PASS;
- Launcher preservation/lint: PASS;
- Shield focused functional tests: 58/58, zero failures/errors/skips;
- Unified focused functional tests: 144/144, zero failures/errors/skips;
- signed APK assembly: PASS;
- package/version/permanent-signer/archive verification: PASS;
- artifact upload: PASS.

Artifact `BOOP-Unified`: ID `10117795236`, size `62,741,119` bytes. GitHub artifact ZIP SHA-256 `5f11f925c097b67f1650fcb445918148e633071739b23245c54742d984c20faf`. APK SHA-256 `c45962533574b9a0ef5bd08ad3f785c94668e9ec793ed6377967d7cae5195a20`. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The artifact ZIP was independently downloaded and its digest matched GitHub. The extracted APK hash matched the included CI receipt and the signer receipt matched the permanent BOOP signer.

Detailed receipt: `docs/BOOP-V70-DEVELOPER-MENU-IN-PLACE-HOTFIX-RECEIPT.md`.

## Physical acceptance boundary

**Current candidate is CI/signer green, not physically accepted.** GitHub performed no visual acceptance. Ryan owns the real-device result.

Next physical test:

1. install the APK from workflow `34385817960`;
2. say `developer menu` and confirm BOOP stays open and presents BOOP Dev;
3. confirm `dev menu` no longer invokes the developer menu;
4. open Voice Settings, scroll to `Developer menu`, open it, use `Done`, then repeat the spoken command;
5. confirm wake/microphone behavior remains healthy after entering and leaving the in-place menu.

Do not create or repoint a v70 rollback checkpoint until Ryan confirms the physical pass. Latest physically accepted rollback remains v59.

## Original v70 developer-lab lineage

Original v70 final production/build commit: `825593a16c004d9c0825720eb014c4f5cc8e58af`. Original app implementation: `2f4a62150121b299a433674e971de1e058f6330f`. Materialized-router assertion correction: `9c907d3497067eb88ea1308875084b8965413951`.

The original separate `BoopDevMenuActivity` remains non-exported in the package for provenance/compatibility, but it is no longer the active spoken/settings route in the current hotfix. Do not reintroduce that hop without new physical evidence.

## Durable finished-eye ordering

Preserve the canonical procedural-eye stages in this order:

1. `scripts/patch-unified-reading-eyes.py`;
2. `scripts/patch-v64-procedural-sclera.py`;
3. `scripts/patch-v65-feathered-sclera.py`.

No later materialization stage may rerun the legacy bitmap hue-cache setter. User hue remains iris-only; default cyan/blue remains 190 degrees. Sclera, pupils, catchlights, black lids and approved eye artwork remain outside hue control.

## Durable notification contract

Android's original notification remains authoritative. BOOP is a puppet mirror around it. Locked state may expose app identity/icon/count only before authentication. Tap preserves the source `PendingIntent`; swipe/timeout dismiss BOOP's mirror only; duplicate-alert prevention remains in force.

Exact approved notification hands remain locked at `unified/assets/boop-notifications/boop-yellow-hands-approved.png`, size `1,809,990` bytes, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`, Git blob `d47037271bf320f4f110e3f8416f59882062afac`. Do not regenerate, recompress, recolor/recolour, crop or weaken the hash contract.

## Physically accepted rollback state

Latest physically accepted exact rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`.

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference eye provenance. Never repoint protected/accepted checkpoints.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eye master, procedural iris-only hue, feathered sclera cleanup, blink timing/gates, headphones/puppetry, exact five-finger yellow hands, one 16 kHz microphone owner, local wake-name training, exact 100 ms wake bridge, uncensored speech request, physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
