# BOOP unified handoff

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v70 pinned-face / raised-banner + Android tablet routing

Release identity remains `versionCode 70`, `versionName 1.2.24-unified-dev-menu-doods`.

### Permanent glossy eye master lock

Ryan re-confirmed the exact glossy `boopApprovedEyes.png` as BOOP's perfect permanent default on 2026-09-09.

Canonical identity:

- path `unified/assets/boop-eyes/boopApprovedEyes.png`;
- 1774 x 887 RGBA with supplied alpha preserved;
- 936,803 bytes;
- SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`;
- Git blob `f95375356b20297fa2b27ab8887f65d4cce5c7fd`.

The master is now also preserved as byte-identical source/reference copies for Shield and Launcher work, with explicit `DO_NOT_TOUCH_BOOP_EYES.md` and `.sha256` receipts beside the copies. The animation-lab branch carries the same source/reference lock separately. Never edit, flatten, recompress, regenerate, recreate, crop, resize on disk, recolor, threshold/flood-fill, reconstruct transparency or substitute this master. Runtime scaling/blink/mask/animation may be non-destructive only.

This source lock does **not** claim the current renderers are uniform. Ryan has separately observed that the Shield can present flatter/cartoon-coloured eyes instead of the glossy default. Renderer-uniformity work remains a separate implementation task; do not fix or redesign it as part of this documentation/asset-lock pass.

### Android tablet / Xiaomi Pad 7 Pro compatibility

The current V70 AIO routes ordinary Android tablets directly into BOOP Wall instead of treating them as handheld Launcher devices.

Device-profile order remains:

- explicit stored profile override first;
- Android TV / Leanback -> `SHIELD`;
- Pixel 7 Pro -> `WALL`;
- any other non-TV Android device with `smallestScreenWidthDp >= 600` -> `WALL`;
- sub-600dp handhelds -> `LAUNCHER`.

This is generic Android tablet support, not a Xiaomi-model hardcode. Existing Wall face geometry derives from live view dimensions, so no tablet-specific eye artwork was introduced.

Tablet-routing TDD lineage:

- RED test-only branch commit `db747c3c3e95acbc3ecae773daa451e6bd3eedc3`;
- GREEN implementation branch head before rebase `cf4c6918956f9cdb79f9b97f479c8a0c0d1de45f`;
- canonical rebased app/test head `bd878606809302de1b871e6c62d8ce905346e766`.

Exact workflow `34395085823`: SUCCESS.

- Shield focused functional tests: 58/58, zero failures/errors/skips;
- Unified focused functional tests: 148/148, zero failures/errors/skips;
- artifact `BOOP-Unified`, ID `10121327367`, size `62,739,601` bytes;
- artifact ZIP SHA-256 `6dda05bea0ebe78b2239026e813f76220c48405969a95a342820ef2b79ad2395`;
- APK SHA-256 `0955dbb51ffab6707f11df02bf3966fc90ce3d73f204b74a349519d4937b4612`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

### Xiaomi Pad physical evidence so far

Ryan installed the exact tablet-compatible V70 candidate on the Xiaomi Pad 7 Pro and physically confirmed:

- BOOP launches into the Wall body on the Pad;
- touch interaction works;
- local Home Assistant control works.

Landscape presentation is physically much larger than desired. That is **not a blocker for the routing proof**, but Ryan wants a later tablet-layout pass that scales BOOP down enough to leave useful side space, with weather on one side and sensor data on the other. Do not implement that layout change unless explicitly requested.

Pad physical acceptance is still in progress. Wake/voice, rotation, developer menu, notification doods and other real-device behavior remain to be exercised before treating the full candidate as physically accepted.

### Existing physical evidence

Ryan physically confirmed on Pixel that the current in-place `developer menu` route opens BOOP Dev without forcing the app to close, and confirmed the pinned-face iteration with `Awesome now I can see him`. Preserve the in-place `showDeveloperMenu()` route and no-vertical-scroll pinned BOOP developer lab.

### Current notification-puppet visual trial

Ryan requested the hands be more obvious and the held banner be moved upward on all notification doods. Current shared composition remains:

- exact approved five-finger notification-hands PNG unchanged;
- hands at `1.12x` resting scale;
- banner/card rests `36dp` upward;
- banner still approaches from another `16dp` above its resting point over `260ms` with the existing overshoot curve;
- hands still animate from `0.96x` of their new resting scale to full resting scale over `220ms`;
- eyes, notification content, privacy, tap/open, swipe/timeout dismiss, cue decisions, package, signer and permissions unchanged.

Current raised-banner GREEN app/test head remains `c68a7aba8f0c9bcffa81ad0b517453cc8e50b12d`, workflow `34392969200` SUCCESS, artifact ID `10120505065`, APK SHA-256 `ab91846489799be9f6d7c8e38fb8c51925f6a983de2c6f2bb0cbdec676d14235`.

## Physical acceptance boundary

The in-place developer-menu route and pinned-face visibility behavior have positive Pixel evidence. Tablet routing now has positive Xiaomi Pad evidence for correct body selection, touch and HA control, but the overall Pad candidate remains under physical shakedown. The emphasized-hands / raised-banner composition also remains physically pending.

Do not create or repoint a v70 rollback checkpoint yet. Latest fully physically accepted rollback remains v59.

## Durable finished-eye ordering and renderer history

Historical procedural-eye stages remain recorded for provenance:

1. `scripts/patch-unified-reading-eyes.py`;
2. `scripts/patch-v64-procedural-sclera.py`;
3. `scripts/patch-v65-feathered-sclera.py`.

They do not have authority to replace or destructively modify the locked glossy master bytes. The new permanent-master lock wins for source identity. Renderer uniformity across Wall/tablet/Shield is a separate task and must preserve the exact glossy default rather than creating another replacement master.

## Durable notification contract

Android's original notification remains authoritative. BOOP is a puppet mirror around it. Locked state may expose app identity/icon/count only before authentication. Tap preserves the source `PendingIntent`; swipe/timeout dismiss BOOP's mirror only; duplicate-alert prevention remains in force.

Exact approved notification hands remain locked at `unified/assets/boop-notifications/boop-yellow-hands-approved.png`, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`.

## Physically accepted rollback state

Latest fully physically accepted exact rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`.

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference provenance. Never repoint protected/accepted checkpoints.

## Architecture boundary

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve one microphone owner, local wake-name training, exact 100 ms wake bridge, uncensored speech request, physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
