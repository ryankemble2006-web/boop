# BOOP unified status

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v70 pinned-face developer lab + emphasized notification pose

Release identity remains unchanged:

- versionCode `70`;
- versionName `1.2.24-unified-dev-menu-doods`.

### Entry architecture and physical evidence

Ryan physically confirmed that the in-place exact spoken command `developer menu` opens BOOP Dev on the Pixel without forcing the app to close. The old `dev menu` phrase remains intentionally rejected. The active spoken/settings route stays inside `MainActivity`; do not restore the failed activity hop. Voice Settings remains vertically scrollable.

Ryan also physically confirmed the pinned-face developer-lab concept with `Awesome now I can see him`. Preserve the no-vertical-scroll Dev Lab: BOOP stays visible while the animation selector moves horizontally, and animation buttons drive that same visible BOOP directly.

### Current notification-puppet trial

Ryan asked for more obvious hands and a higher held banner on all notification doods and approved the shared layout change.

`BoopNotificationPuppetView` now:

- keeps the exact approved notification-hands PNG unchanged;
- rests the hands at `1.12x` scale;
- rests the banner/card `36dp` above its previous centered position;
- preserves the existing relative entrance motion: the banner begins another `16dp` above its new resting point and settles over `260ms` with the same overshoot curve; hands animate from `0.96x` of their new rest scale to the new rest scale over `220ms`;
- leaves notification content, privacy, tap/open, swipe/timeout dismiss, cue decisions, package, permissions and signer unchanged.

This shared renderer feeds both Dev Lab notification doods and real notification surfaces. GitHub does not visually judge the result; Ryan owns the Pixel appearance decision.

### Verification

RED evidence:

- head `9ecf556de5545eef19a73e490ccf5a6989ca1e85`;
- workflow `34392830481`;
- failed at the non-visual integration-contract gate before materialization/signing because the emphasized-pose semantic marker did not yet exist.

Exact GREEN app/test head:

`c68a7aba8f0c9bcffa81ad0b517453cc8e50b12d`

Exact workflow `34392969200`: SUCCESS.

Passed: non-visual integration contracts, materialization, notification/developer-lab contracts, seamless wake handoff, Launcher preservation/lint, Shield functional tests, Unified wake/routing/lifecycle tests, signed APK assembly, package/version/permanent-signer/archive verification and artifact upload.

- Artifact: `BOOP-Unified`
- Artifact ID: `10120505065`
- Artifact size: `62,740,052` bytes
- Artifact ZIP SHA-256: `de64be9f30d3aa54eb69b6d662e326f70ec56f55776efcf70c05502c4cef8608`
- APK SHA-256: `ab91846489799be9f6d7c8e38fb8c51925f6a983de2c6f2bb0cbdec676d14235`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused functional tests: 58/58, zero failures/errors/skips
- Unified focused functional tests: 144/144, zero failures/errors/skips

The artifact ZIP was independently downloaded and matched GitHub's digest. Extracted `apk-sha256.txt`, `built-commit.txt`, `badging.txt` and `signer-sha256.txt` matched the APK, exact app head, package/version and permanent signer.

## Acceptance boundary

The in-place developer-menu entry and pinned-face visibility behavior have positive Pixel evidence. The new `1.12x` hands / `36dp` raised-banner notification composition remains **CI/signer green only** until Ryan checks this exact APK. GitHub performed no visual acceptance.

Current physical check: swipe through several notification doods and judge whether the hands read clearly enough and whether the banner is high enough while the approved/current BOOP eyes remain visible. If tuning is needed, adjust shared transform/translation only; do not alter the locked PNG.

No v70 rollback checkpoint was created or repointed. Latest fully physically accepted rollback remains v59.

## Durable eye ordering

Canonical procedural-eye stages remain:

1. `patch-unified-reading-eyes.py`;
2. `patch-v64-procedural-sclera.py`;
3. `patch-v65-feathered-sclera.py`.

No later materialization stage may rerun the legacy bitmap hue setter. User hue remains procedural-iris-only. Sclera, pupils, catchlights, black lids and approved eye artwork remain outside hue control.

## Locked notification hands

Canonical asset `unified/assets/boop-notifications/boop-yellow-hands-approved.png` remains exactly size `1,809,990` bytes, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`, Git blob `d47037271bf320f4f110e3f8416f59882062afac`. Do not regenerate, recompress, recolour/recolor, crop or weaken this guard. Explicitly approved layout/transform changes may pose the exact asset.

## Preserved contracts

Android's original notification remains authoritative; BOOP mirrors it. Production locked presentation remains identity/icon/count-only before authentication. Preserve approved black-lidded eye master, procedural iris-only hue, feathered sclera, blink, notification privacy/tap/dismiss semantics, headphones/puppetry, one microphone owner, wake/name architecture, exact 100 ms wake bridge, uncensored speech request, HA names/Home controls, room isolation and idempotent Shield scaling.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / package `com.boop.shieldhome` until Ryan explicitly approves a later merge. No automatic installs/grants or signer/package changes.

## Physically accepted rollback

Latest fully physically accepted exact rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`.

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected eye provenance. Never repoint protected/accepted checkpoints.
