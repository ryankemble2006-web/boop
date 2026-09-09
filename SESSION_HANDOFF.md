# BOOP unified handoff

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v70 horizontal developer lab

Release identity remains `versionCode 70`, `versionName 1.2.24-unified-dev-menu-doods`.

### Physical evidence already established

Ryan physically confirmed on the Pixel that the current in-place `developer menu` route opens BOOP Dev without forcing the app to close. The older activity-hop candidate at `95224d95a04b6a79a8d8c4e9e5ac4c35cf64ca9a` remains physically failed for that bug. Preserve the current entry architecture: exact spoken trigger `developer menu`, old `dev menu` rejected, local routing before HA/chat fallback, and `showDeveloperMenu()` inside the existing `MainActivity` rather than an activity hop.

Voice Settings remains vertically scrollable and its `Developer menu` row stays reachable.

### Current developer-lab iteration

Ryan approved a trial with two horizontal selector shelves:

- `Animations`: horizontal right-to-left swipe selector. Tap a preview to open the real BOOP animation full-screen. A bottom `Dismiss` control stops/resets the preview and returns to the selector.
- `Notification doods`: separate horizontal right-to-left swipe selector. Dood previews keep the real `BoopNotificationPuppetView` for the banner/card and exact approved hands, hide its duplicate internal face, and render a dedicated current `BoopFaceView` above the puppet/banner area.
- Both shelves remember horizontal scroll position while a preview is open, so dismissal returns near the item just tested.
- Production notification behavior/layout is otherwise unchanged. The face-visibility hook is package-private and used by the developer preview composition.
- The visible `Stop` animation selector item is omitted because `Dismiss` is now the stop/reset/return path. The STOP action remains in the underlying model/dispatcher.

No approved artwork was regenerated or reinterpreted. GitHub does not judge the resulting visual spacing; Ryan does that on-device.

### Test-first and exact build evidence

RED contract head: `4323684b838a5c3e80a249d234ced3b19c3c5e87`. Workflow `34388180318` materialized successfully and then failed at the developer-lab contract gate because the new horizontal/full-screen behavior did not yet exist.

Implementation:

- `c74a79c268ccdab6cc147acb0f26ded45ded37e8` — dev-only face visibility hook on the reusable notification puppet;
- `9510a2eac42b272f9fbc6becb991a4fbc16482d1` — horizontal shelves, full-screen animation preview, bottom Dismiss, scroll-position restore, and separate approved-eye area for notification dood previews.

Exact GREEN workflow `34388440031`: SUCCESS at app/test head `9510a2eac42b272f9fbc6becb991a4fbc16482d1`.

Passed: non-visual integration contracts, materialization, developer-lab/notification contracts, seamless wake handoff, Launcher preservation/lint, Shield controls, Unified wake/name/routing/lifecycle/assistant-policy tests, permanent signer preparation, signed APK assembly, package/version/permanent-signer/archive verification and artifact upload.

- Shield focused functional tests: 58/58, zero failures/errors/skips;
- Unified focused functional tests: 144/144, zero failures/errors/skips;
- artifact `BOOP-Unified`, ID `10118781776`, size `62,739,308` bytes;
- artifact ZIP SHA-256 `9663ad3e648c54e026bfd15a14a44b89f45e4b3e13903b478a375839f4af44e1`;
- APK SHA-256 `76950071375a3ebf0bbd3f5a2c4725e93bcd76e4deacbdfe4481faa5903d7a27`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The exact artifact ZIP was independently downloaded and matched GitHub's digest. The extracted APK hash, built-commit receipt and signer receipt matched CI.

## Physical acceptance boundary

The in-place `developer menu` entry itself has positive Pixel evidence. The **new horizontal shelves, full-screen preview/Dismiss loop, shelf-position restore, and eye-above-banner dood composition remain physically unaccepted** until Ryan tests this exact APK. GitHub performed no visual acceptance.

Physical check: open `developer menu`, swipe both shelves right-to-left, tap several animations and confirm full-screen + bottom `Dismiss` + return near the previous selector position, then inspect every notification dood and confirm BOOP's approved/current eyes are clearly visible above the held banner/hands. Recheck wake/microphone health after leaving BOOP Dev.

Do not create or repoint a v70 rollback checkpoint yet. Latest fully physically accepted rollback remains v59.

## Original v70 developer-lab lineage

Original v70 final production/build commit: `825593a16c004d9c0825720eb014c4f5cc8e58af`. Original app implementation: `2f4a62150121b299a433674e971de1e058f6330f`. Materialized-router assertion correction: `9c907d3497067eb88ea1308875084b8965413951`.

The original separate `BoopDevMenuActivity` remains non-exported for provenance/compatibility, but it is no longer the active spoken/settings route. Do not reintroduce that activity hop without new physical evidence.

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

Latest fully physically accepted exact rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`.

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference eye provenance. Never repoint protected/accepted checkpoints.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eye master, procedural iris-only hue, feathered sclera cleanup, blink timing/gates, headphones/puppetry, exact five-finger yellow hands, one 16 kHz microphone owner, local wake-name training, exact 100 ms wake bridge, uncensored speech request, physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
