# BOOP unified status

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v70 pinned-face / raised-banner + Android tablet routing

Release identity remains unchanged:

- versionCode `70`;
- versionName `1.2.24-unified-dev-menu-doods`.

## Permanent glossy default eye master

Ryan re-confirmed `boopApprovedEyes.png` as BOOP's perfect permanent default on 2026-09-09.

Locked identity:

- canonical path `unified/assets/boop-eyes/boopApprovedEyes.png`;
- 1774 x 887 RGBA with supplied alpha preserved;
- 936,803 bytes;
- SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`;
- Git blob `f95375356b20297fa2b27ab8887f65d4cce5c7fd`.

Never edit, flatten, recompress, regenerate, recreate, crop, resize on disk, recolor/recolour, threshold/flood-fill, reconstruct transparency or substitute this source. Exact reference copies are preserved beside current Unified, Shield and Launcher source/reference areas with `DO_NOT_TOUCH_BOOP_EYES.md` and `.sha256` receipts. Animation work carries the same lock separately.

This locks source identity only. It does not claim the current renderers are uniform. Ryan has separately observed the Shield showing flatter/cartoon-coloured eyes rather than the glossy default. That renderer-uniformity issue remains separate and this asset-lock pass changes no runtime code.

## Android tablet / Xiaomi Pad 7 Pro routing

V70 treats non-TV Android devices with `smallestScreenWidthDp >= 600` as BOOP Wall devices. Routing remains:

- explicit profile override first;
- TV / Leanback -> `SHIELD`;
- Pixel 7 Pro -> `WALL`;
- other non-TV devices at 600dp or wider -> `WALL`;
- sub-600dp handhelds -> `LAUNCHER`.

The Xiaomi Pad 7 Pro therefore takes the Wall path without Xiaomi-specific hardcoding.

Tablet-routing lineage:

- RED test-only commit `db747c3c3e95acbc3ecae773daa451e6bd3eedc3`;
- GREEN implementation before rebase `cf4c6918956f9cdb79f9b97f479c8a0c0d1de45f`;
- canonical app/test head `bd878606809302de1b871e6c62d8ce905346e766`;
- workflow `34395085823`: SUCCESS.

Verification receipt:

- Artifact `BOOP-Unified`, ID `10121327367`, size `62,739,601` bytes
- Artifact ZIP SHA-256 `6dda05bea0ebe78b2239026e813f76220c48405969a95a342820ef2b79ad2395`
- APK SHA-256 `0955dbb51ffab6707f11df02bf3966fc90ce3d73f204b74a349519d4937b4612`
- Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused tests 58/58, zero failures/errors/skips
- Unified focused tests 148/148, zero failures/errors/skips

## Xiaomi Pad physical evidence so far

Ryan physically installed the exact tablet-compatible V70 candidate and confirmed:

- BOOP opens as the Wall body on the Xiaomi Pad 7 Pro;
- touch works;
- local Home Assistant control works.

Landscape BOOP is currently much larger than desired. A later tablet-layout pass is wanted so horizontal mode can leave weather on one side and sensor data on the other, with BOOP scaled down in the centre. That is future work only and is not part of this asset lock.

Full Pad physical acceptance is still in progress. Wake/voice, rotation, developer menu, notification doods and other device behavior remain to be exercised.

## Existing entry / developer-lab evidence

Ryan physically confirmed the exact spoken `developer menu` route works on Pixel without closing BOOP, and the pinned-face developer-lab concept is accepted. Preserve the in-place `showDeveloperMenu()` route, vertically scrollable Voice Settings, no-vertical-scroll Dev Lab, and horizontal animation selector.

## Current notification-puppet trial

Current shared composition remains:

- approved notification-hands PNG unchanged;
- hands rest at `1.12x` scale;
- banner/card rests `36dp` above its previous centered position;
- relative entrance motion unchanged;
- notification content, privacy, tap/open, swipe/timeout dismiss, cues, package, permissions and signer unchanged.

Prior raised-banner GREEN app/test head `c68a7aba8f0c9bcffa81ad0b517453cc8e50b12d`, workflow `34392969200`: SUCCESS.

## Acceptance boundary

The in-place developer-menu entry and pinned-face visibility have positive Pixel evidence. Tablet routing has positive Xiaomi Pad evidence for body selection, touch and HA control, but the candidate is still under physical shakedown. Raised-banner visual acceptance is still pending.

No v70 rollback checkpoint was created or repointed. Latest fully physically accepted rollback remains v59.

## Durable eye-source rule

Historical procedural-eye stages remain provenance only. They do not have authority to replace or destructively alter the locked glossy master bytes. Any later renderer-uniformity repair must use the exact permanent master as its source of truth rather than making another default.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference provenance and must never be repointed.

## Preserved contracts

Android's original notification remains authoritative; BOOP mirrors it. Preserve notification privacy/tap/dismiss semantics, headphones/puppetry, one microphone owner, wake/name architecture, exact 100 ms wake bridge, uncensored speech request, HA names/Home controls, room isolation and idempotent Shield scaling.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / package `com.boop.shieldhome` until Ryan explicitly approves a later merge. No automatic installs/grants or signer/package changes.

## Physically accepted rollback

Latest fully physically accepted exact rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`.

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.
