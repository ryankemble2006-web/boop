# BOOP unified memory

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Permanent glossy eye master, locked 2026-09-09

Ryan re-confirmed the exact glossy `boopApprovedEyes.png` as BOOP's perfect permanent default across Wall/phone, tablet, Shield, Launcher references and animation work.

Canonical source identity:
- `unified/assets/boop-eyes/boopApprovedEyes.png`;
- 1774 x 887 RGBA with supplied alpha preserved;
- 936,803 bytes;
- SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`;
- Git blob `f95375356b20297fa2b27ab8887f65d4cce5c7fd`.

Never edit, flatten, recompress, regenerate, recreate, crop, resize on disk, recolor/recolour, threshold/flood-fill, reconstruct transparency or substitute this source. Exact source/reference copies are retained beside current Unified, Shield, Launcher and animation lineages with do-not-touch/checksum receipts. Runtime scaling, posing, masking, blinking and animation may be non-destructive only. A user hue feature may affect the iris at runtime but must never rewrite the master bytes.

The source lock supersedes older appearance-source assumptions but does not erase renderer history. Ryan separately observed that Shield can currently show flatter/cartoon-coloured eyes instead of the glossy default. Renderer uniformity across Wall/tablet/Shield is therefore a separate implementation task. Do not solve it by creating another eye image.

## Current canonical candidate: v70 pinned-face / raised-banner + Android tablet routing

v70 remains versionCode `70`, versionName `1.2.24-unified-dev-menu-doods`.

## Durable Android tablet routing

As of canonical app/test head `bd878606809302de1b871e6c62d8ce905346e766`, Unified BOOP treats non-TV Android devices with `smallestScreenWidthDp >= 600` as Wall devices. Preserve the routing order:

- explicit profile override first;
- Android TV / Leanback / television mode -> `SHIELD`;
- Pixel 7 Pro -> `WALL`;
- other non-TV Android devices at 600dp or wider -> `WALL`;
- sub-600dp handhelds -> `LAUNCHER`.

The Xiaomi Pad 7 Pro uses the Wall body without a Xiaomi-specific hardcode.

Signed-build receipt:
- workflow `34395085823`: SUCCESS;
- artifact `BOOP-Unified`, ID `10121327367`, size `62,739,601` bytes;
- artifact ZIP SHA-256 `6dda05bea0ebe78b2239026e813f76220c48405969a95a342820ef2b79ad2395`;
- APK SHA-256 `0955dbb51ffab6707f11df02bf3966fc90ce3d73f204b74a349519d4937b4612`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused tests 58/58 and Unified focused tests 148/148, zero failures/errors/skips.

Ryan physically confirmed on Xiaomi Pad 7 Pro that the exact V70 tablet candidate opens the Wall body, touch works and local Home Assistant control works. Landscape BOOP is currently larger than desired. A future explicit tablet-layout pass may scale BOOP down in the centre to leave weather on one side and sensor data on the other. Full Pad shakedown remains in progress; do not call the candidate fully physically accepted yet.

Ryan physically confirmed the in-place `developer menu` entry works on Pixel without closing BOOP and confirmed the pinned-face developer-lab concept. Preserve Voice Settings vertical scrolling, in-place `showDeveloperMenu()`, and the pinned visible BOOP while animation controls are browsed.

## Durable developer-menu selector contract

- exact spoken trigger `developer menu`; old `dev menu` remains rejected;
- route stays local before HA/chat fallback;
- current route stays in existing `MainActivity`, not a separate activity hop;
- BOOP Dev is an in-place fullscreen overlay;
- developer lab itself does not vertically scroll;
- `Animations` remains a horizontal selector and calls the real current behaviors on the same pinned BOOP face;
- `Notification doods` remains a separate horizontal selector;
- dood previews are local fixtures only and never create Android shade notifications.

## Durable emphasized notification-puppet pose

Current shared trial remains exact approved notification hands at `1.12x` resting scale and banner/card `36dp` upward, preserving prior relative entrance motion. Notification privacy/tap/dismiss/cue semantics remain unchanged. Physical visual acceptance remains Ryan's gate.

Raised-banner GREEN app/test head `c68a7aba8f0c9bcffa81ad0b517453cc8e50b12d`, workflow `34392969200`: SUCCESS.

## Durable finished-eye and hue history

Historical procedural-eye stages remain provenance:

1. `scripts/patch-unified-reading-eyes.py`;
2. `scripts/patch-v64-procedural-sclera.py`;
3. `scripts/patch-v65-feathered-sclera.py`.

These stages have no authority to replace or destructively change the permanent glossy master bytes. The master image is source-of-truth. Any renderer-uniformity repair must preserve that source and avoid making a new default.

User-selected eye colour remains iris-only at runtime. Sclera/whites, pupils, catchlights, black eyelids/accents and the master bytes must not be tinted or rewritten.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference provenance. Never repoint it.

## Durable visual-verification boundary

Ryan owns BOOP visual acceptance. CI stays non-visual: compilation/lint, focused functional tests, package/signature/integrity and security checks. Do not add screenshot comparisons, golden-image checks or visual diffing. Exact locked-binary/hash checks are allowed because they verify source identity, not appearance.

## Durable notification contract

Android's original notification is authoritative. BOOP is a puppet mirror around it. Before authentication the locked surface may show app identity/icon/count only. Tap preserves source `PendingIntent`; swipe and timeout dismiss BOOP's mirror only. Preserve duplicate-alert prevention.

Exact approved notification hands binary remains locked at `unified/assets/boop-notifications/boop-yellow-hands-approved.png`, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`.

## Durable wake architecture

BOOP remains a permanent wake name. Custom name is additive. Preserve one controller-owned 16 kHz PCM stream, five local enrolment examples, exact 100 ms wake-to-command bridge, silent handoff, local wake-name profile and uncensored-speech request. Do not add a competing microphone listener.

Protected rollback checkpoints:
- `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`;
- `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

Never repoint them. Latest fully physically accepted rollback remains v59.

## Clean Shield HOME boundary

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Do not merge it into Unified until Ryan explicitly approves later.

## Permanent Home and assistant contracts

HA device names and Home controls are physically accepted and must not regress. Home remains room-scoped and fail-closed. Shield density scaling remains idempotent and never system-wide. Assistant ownership remains explicit/reversible through supported Android routes.

No automatic installs/grants. GitHub performs non-visual verification only; Ryan owns appearance, animation, device and acoustic acceptance.
