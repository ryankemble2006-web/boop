# BOOP unified handoff

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v70 pinned-face / raised-banner + Android tablet routing

Release identity remains `versionCode 70`, `versionName 1.2.24-unified-dev-menu-doods`.

### Android tablet / Xiaomi Pad 7 Pro compatibility

The current V70 AIO now routes ordinary Android tablets directly into BOOP Wall instead of treating them as handheld Launcher devices.

Device-profile order is intentionally preserved and extended only at the presentation-routing boundary:

- an explicit stored profile override still wins first;
- Android TV / Leanback devices still route to `SHIELD`;
- the existing Pixel 7 Pro rule still routes to `WALL`;
- any other non-TV Android device with `smallestScreenWidthDp >= 600` routes to `WALL`;
- sub-600dp handhelds still route to `LAUNCHER`.

This is generic Android tablet support, not a Xiaomi-model hardcode. The Xiaomi Pad 7 Pro is covered by the tablet-width path. The existing Wall face already sizes from the live view dimensions through `BoopEyeLayout`, with separate portrait and landscape handling, so no tablet-specific eye artwork, geometry fork, manifest route, HA behavior, notification behavior, permissions, package identity, version or signer change was introduced.

Tablet-routing TDD lineage:

- RED test-only branch commit `db747c3c3e95acbc3ecae773daa451e6bd3eedc3`: the four new width-aware routing tests failed to compile against the prior resolver because the four-argument route did not yet exist;
- GREEN implementation branch head before rebase `cf4c6918956f9cdb79f9b97f479c8a0c0d1de45f`;
- canonical rebased app/test head `bd878606809302de1b871e6c62d8ce905346e766`.

Exact workflow `34395085823`: SUCCESS.

Passed: non-visual integration contracts, materialization, notification/developer-lab contracts, seamless wake handoff, Launcher preservation/lint, Shield controls, Unified wake/name/routing/lifecycle/assistant-policy tests including the tablet profile contract, permanent signer preparation, signed APK assembly, package/version/permanent-signer/archive verification and artifact upload.

- Shield focused functional tests: 58/58, zero failures/errors/skips;
- Unified focused functional tests: 148/148, zero failures/errors/skips;
- artifact `BOOP-Unified`, ID `10121327367`, size `62,739,601` bytes;
- artifact ZIP SHA-256 `6dda05bea0ebe78b2239026e813f76220c48405969a95a342820ef2b79ad2395`;
- APK SHA-256 `0955dbb51ffab6707f11df02bf3966fc90ce3d73f204b74a349519d4937b4612`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The exact artifact ZIP was independently downloaded after CI. Its SHA-256 matched GitHub's artifact digest; the extracted APK matched `apk-sha256.txt`; `built-commit.txt` matched `bd878606809302de1b871e6c62d8ce905346e766`; `badging.txt` confirmed package `com.boop.alpha1`, versionCode `70`, versionName `1.2.24-unified-dev-menu-doods`; and `signer-sha256.txt` matched the permanent BOOP signer.

### Physical evidence already established

Ryan physically confirmed on the Pixel that the current in-place `developer menu` route opens BOOP Dev without forcing the app to close. He then physically confirmed the pinned-face iteration with: `Awesome now I can see him`. Treat the pinned BOOP model staying visible while animation controls are browsed as positive physical evidence. The older activity-hop candidate at `95224d95a04b6a79a8d8c4e9e5ac4c35cf64ca9a` remains physically failed for that bug.

Preserve the current entry architecture: exact spoken trigger `developer menu`, old `dev menu` rejected, local routing before HA/chat fallback, and `showDeveloperMenu()` inside the existing `MainActivity` rather than an activity hop. Voice Settings remains vertically scrollable and its `Developer menu` row stays reachable.

### Current developer-lab behavior

The required/current animation design is:

- BOOP's real current `BoopFaceView` stays pinned and visible in the Dev Lab while choosing animations;
- the Dev Lab page itself has no vertical `ScrollView` and does not scroll up/down;
- `Animations` is a horizontal right-to-left selector below the pinned face;
- swiping the selector changes which animation buttons are visible without moving BOOP off-screen;
- tapping `Wake`, `Think`, `Stop`, `Berry 1`, `Berry 2`, `Berry 3`, `Shake`, or `Sleep` calls the real animation directly on that same pinned face;
- animation selection does not clear/replace the Dev Lab, open a second animation page, or require a Dismiss step;
- `Notification doods` remains a separate horizontal selector. Dood previews may temporarily replace the selector view and Dismiss returns to the Dev Lab with horizontal position restored.

The prior full-screen animation-preview/Dismiss design is rejected and must not be restored.

### Current notification-puppet visual trial

Ryan requested the hands be more obvious and the held banner be moved upward on **all notification doods**. He explicitly approved the following shared composition change:

- keep the exact approved five-finger notification-hands PNG byte-for-byte unchanged;
- display the hands at `1.12x` resting scale;
- move the banner/card resting position `36dp` upward;
- preserve the existing relative entrance motion: banner still approaches from another `16dp` above its resting point over `260ms` with the existing overshoot curve, and hands still animate from `0.96x` of their new resting scale to full resting scale over `220ms`;
- keep BOOP eyes, notification content, privacy, tap/open, swipe/timeout dismiss, cue decisions, package, signer and permissions unchanged.

This is implemented once in shared `source/BoopNotificationPuppetView.java`, so Dev Lab Facebook/WhatsApp/Gmail/etc fixtures and real notification surfaces use the same pose. The exact hands asset was not regenerated, recompressed, recolored, cropped or otherwise changed.

GitHub performs no visual judgement. Whether `1.12x` hands and a `36dp` lift look right remains Ryan's Pixel acceptance call.

### Test-first evidence for notification pose

RED contract head:

`9ecf556de5545eef19a73e490ccf5a6989ca1e85`

Workflow `34392830481` failed at the non-visual integration-contract gate before materialization/signing because the new semantic emphasized-pose marker was absent. This is the intended RED evidence.

GREEN app/test head:

`c68a7aba8f0c9bcffa81ad0b517453cc8e50b12d`

Exact workflow `34392969200`: SUCCESS.

Passed: non-visual integration contracts, materialization, notification/developer-lab contracts, seamless wake handoff, Launcher preservation/lint, Shield controls, Unified wake/name/routing/lifecycle/assistant-policy tests, permanent signer preparation, signed APK assembly, package/version/permanent-signer/archive verification and artifact upload.

- Shield focused functional tests: 58/58, zero failures/errors/skips;
- Unified focused functional tests: 144/144, zero failures/errors/skips;
- artifact `BOOP-Unified`, ID `10120505065`, size `62,740,052` bytes;
- artifact ZIP SHA-256 `de64be9f30d3aa54eb69b6d662e326f70ec56f55776efcf70c05502c4cef8608`;
- APK SHA-256 `ab91846489799be9f6d7c8e38fb8c51925f6a983de2c6f2bb0cbdec676d14235`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The exact artifact ZIP was independently downloaded and its SHA-256 matched GitHub's artifact digest. The extracted APK hash matched `apk-sha256.txt`, `built-commit.txt` matched `c68a7aba8f0c9bcffa81ad0b517453cc8e50b12d`, `badging.txt` confirmed package `com.boop.alpha1`, versionCode `70`, versionName `1.2.24-unified-dev-menu-doods`, and the signer receipt matched the permanent BOOP signer.

### Prior pinned-face verification lineage

Pinned-face RED head `6ada374665a9cc6d504a7188f4df1b406fffdcc6`, workflow `34391024568`, failed at the developer-lab contract gate as intended. Pinned-face GREEN app/test head `a9e4e6a8f6abf023bc9ba1779d0a51f698ee0c3f`, workflow `34391151333`, passed and produced artifact ID `10119809751`, APK SHA-256 `b06d4c2dd5c6b6fa2dac969b7406c401195b01b961263e418eff5101b75b552e`.

## Physical acceptance boundary

The in-place developer-menu route and the pinned-face visibility behavior have positive Pixel evidence. The **emphasized-hands / raised-banner notification composition remains physically unaccepted**, and the **new Android tablet route at app head `bd878606...` is CI/signer green but physically unaccepted on the Xiaomi Pad 7 Pro** until Ryan tests this exact APK. GitHub performed no visual/device acceptance.

Pad physical check: install the exact `bd878606...` artifact on the Xiaomi Pad 7 Pro, launch BOOP normally, confirm it opens directly to BOOP Wall rather than the handheld Launcher/All Apps surface, rotate through portrait and landscape, then check tap-to-speak/wake microphone behavior, Voice Settings / developer menu, and one local HA command. The existing raised-banner check remains: swipe through several `Notification doods` and judge whether the hands read clearly enough and whether the banner is high enough while BOOP's eyes remain readable. If notification tuning is needed, adjust the shared resting scale/lift rather than touching the locked PNG.

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

Exact approved notification hands remain locked at `unified/assets/boop-notifications/boop-yellow-hands-approved.png`, size `1,809,990` bytes, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`, Git blob `d47037271bf320f4f110e3f8416f59882062afac`. Do not regenerate, recompress, recolor/recolour, crop or weaken the hash contract. Layout/transform changes may pose this exact asset when Ryan explicitly approves them.

## Physically accepted rollback state

Latest fully physically accepted exact rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`.

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference eye provenance. Never repoint protected/accepted checkpoints.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eye master, procedural iris-only hue, feathered sclera cleanup, blink timing/gates, headphones/puppetry, exact five-finger yellow hands, one 16 kHz microphone owner, local wake-name training, exact 100 ms wake bridge, uncensored speech request, physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
