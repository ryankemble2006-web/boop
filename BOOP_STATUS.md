# BOOP unified status

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v70 pinned-face developer lab

Release identity remains unchanged:

- versionCode `70`;
- versionName `1.2.24-unified-dev-menu-doods`.

### Entry architecture and physical evidence

Ryan physically confirmed that the in-place exact spoken command `developer menu` opens BOOP Dev on the Pixel without forcing the app to close. The old `dev menu` phrase remains intentionally rejected. The active spoken/settings route stays inside `MainActivity`; do not restore the failed activity hop. Voice Settings remains vertically scrollable.

### Current UI iteration

The prior full-screen animation preview was rejected. The Dev Lab now keeps the real current BOOP face pinned and visible while selecting animations.

There is no vertical Dev Lab page scroll. The `Animations` selector is horizontal right-to-left beneath the pinned face. Swiping changes animation buttons without moving BOOP off-screen. Tapping `Wake`, `Think`, `Stop`, `Berry 1`, `Berry 2`, `Berry 3`, `Shake`, or `Sleep` runs that real behavior directly on the same visible BOOP. There is no animation page replacement and no Dismiss hop.

`Notification doods` remains a separate horizontal selector. Dood previews use a dedicated current BOOP face above the real `BoopNotificationPuppetView` banner/card and exact approved hands, with the puppet's duplicate internal face suppressed only for this dev composition. Production notification behavior/layout remains unchanged.

Exact app/test head: `a9e4e6a8f6abf023bc9ba1779d0a51f698ee0c3f`.

Exact workflow `34391151333` completed SUCCESS. Non-visual integration, materialization, pinned-face/no-vertical-scroll developer-lab contracts, notification/JUnit contracts, seamless wake handoff, Launcher lint, Shield functional tests, Unified wake/routing/lifecycle tests, signed APK assembly, package/version/permanent-signer/archive verification and artifact upload all passed.

- Artifact: `BOOP-Unified`
- Artifact ID: `10119809751`
- Artifact size: `62,740,109` bytes
- Artifact ZIP SHA-256: `fa7d59bcdc105907a988fea1043895d21b060ad1ed97aaaef6e3d4f70f21fd91`
- APK SHA-256: `b06d4c2dd5c6b6fa2dac969b7406c401195b01b961263e418eff5101b75b552e`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused functional tests: 58/58, zero failures/errors/skips
- Unified focused functional tests: 144/144, zero failures/errors/skips

RED evidence: head `6ada374665a9cc6d504a7188f4df1b406fffdcc6`, workflow `34391024568`, failed at the developer-lab contract gate after successful materialization as expected.

The exact GREEN artifact ZIP was independently downloaded and matched GitHub's digest; extracted APK, built-commit and signer receipts matched CI.

## Acceptance boundary

The in-place developer-menu entry has positive Pixel evidence. The new pinned-face/no-vertical-scroll selector layout and direct animation reactions remain **CI/signer green only** until Ryan checks the exact APK. GitHub performed no visual acceptance.

Current physical checks: keep BOOP visible while swiping the animation selector horizontally, press several actions and confirm the same visible BOOP reacts immediately, with no up/down page scrolling or animation page transition. Then inspect notification doods for approved/current eyes visibly above the banner/hands and confirm wake/microphone health after exit.

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
