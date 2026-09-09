# BOOP unified status

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v70 horizontal developer lab

Release identity remains unchanged:

- versionCode `70`;
- versionName `1.2.24-unified-dev-menu-doods`.

### Entry architecture and physical evidence

Ryan physically confirmed that the in-place exact spoken command `developer menu` opens BOOP Dev on the Pixel without forcing the app to close. The old `dev menu` phrase remains intentionally rejected. The active spoken/settings route stays inside `MainActivity`; do not restore the failed activity hop. Voice Settings remains vertically scrollable.

### Current UI iteration

The Dev Lab now has two horizontal right-to-left swipe shelves: `Animations` and `Notification doods`.

Animation items open the real current BOOP animation full-screen. A bottom `Dismiss` button stops/resets the preview and returns to the selector. The animation shelf remembers its horizontal position while previewing. `Stop` is no longer shown as a shelf item because `Dismiss` is the stop/reset path; the STOP action remains in the internal model/dispatcher.

Notification doods also use a horizontal shelf and remember their selector position. Dev dood previews keep the real `BoopNotificationPuppetView` for the banner/card and exact approved hands, hide its duplicate internal face, and use a separate current `BoopFaceView` above the puppet/banner area. Production notification behavior/layout is otherwise unchanged.

Exact app/test head: `9510a2eac42b272f9fbc6becb991a4fbc16482d1`.

Exact workflow `34388440031` completed SUCCESS. Non-visual integration, materialization, new horizontal developer-lab contracts, notification/JUnit contracts, seamless wake handoff, Launcher lint, Shield functional tests, Unified wake/routing/lifecycle tests, signed APK assembly, package/version/permanent-signer/archive verification and artifact upload all passed.

- Artifact: `BOOP-Unified`
- Artifact ID: `10118781776`
- Artifact size: `62,739,308` bytes
- Artifact ZIP SHA-256: `9663ad3e648c54e026bfd15a14a44b89f45e4b3e13903b478a375839f4af44e1`
- APK SHA-256: `76950071375a3ebf0bbd3f5a2c4725e93bcd76e4deacbdfe4481faa5903d7a27`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused functional tests: 58/58, zero failures/errors/skips
- Unified focused functional tests: 144/144, zero failures/errors/skips

RED evidence: head `4323684b838a5c3e80a249d234ced3b19c3c5e87`, workflow `34388180318`, failed at the developer-lab contract gate after successful materialization as expected.

The exact GREEN artifact ZIP was independently downloaded and matched GitHub's digest; extracted APK/built-commit/signer receipts matched CI.

## Acceptance boundary

The in-place developer-menu entry has positive Pixel evidence. The new horizontal shelves, full-screen preview/Dismiss loop, scroll-position restore, and eye-above-banner notification composition remain **CI/signer green only** until Ryan checks the exact APK. GitHub performed no visual acceptance.

Current physical checks: swipe both shelves, open/dismiss animations, confirm return near the same selector position, inspect every notification dood for approved/current eyes visibly above the banner/hands, then confirm wake/microphone health after exit.

No v70 rollback checkpoint was created or repointed. Latest fully physically accepted rollback remains v59.

## Durable eye ordering

Canonical procedural-eye stages remain:

1. `patch-unified-reading-eyes.py`;
2. `patch-v64-procedural-sclera.py`;
3. `patch-v65-feathered-sclera.py`.

No later materialization stage may rerun the legacy bitmap hue setter. User hue remains procedural-iris-only. Sclera, pupils, catchlights, black lids and approved eye artwork remain outside hue control.

## Locked notification hands

Canonical asset `unified/assets/boop-notifications/boop-yellow-hands-approved.png` remains exactly size `1,809,990` bytes, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`, Git blob `d47037271bf320f4f110e3f8416f59882062afac`. Do not regenerate, recompress, recolour/recolor, crop or weaken this guard.

## Preserved contracts

Android's original notification remains authoritative; BOOP mirrors it. Production locked presentation remains identity/icon/count-only before authentication. Preserve approved black-lidded eye master, procedural iris-only hue, feathered sclera, blink, notification privacy/tap/dismiss semantics, headphones/puppetry, one microphone owner, wake/name architecture, exact 100 ms wake bridge, uncensored speech request, HA names/Home controls, room isolation and idempotent Shield scaling.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / package `com.boop.shieldhome` until Ryan explicitly approves a later merge. No automatic installs/grants or signer/package changes.

## Physically accepted rollback

Latest fully physically accepted exact rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`.

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected eye provenance. Never repoint protected/accepted checkpoints.
