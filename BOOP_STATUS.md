# BOOP unified status

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v67 finished eyes + repaired hue

Built source:

`63bb80283af7424bc1012fe71444552d8b942a74`

Production eye transplant:

`c35b57a44ec0e7fb8f06cb49a4f0ab10915dbf5d`

Release evidence:

- version 67 / `1.2.21-unified-finished-eyes-hue`;
- workflow `34312779359` SUCCESS;
- separate Shield HOME routing workflow `34312684627` SUCCESS on the production code;
- artifact `BOOP-Unified`, ID `10089043590`;
- artifact digest `sha256:c0a291a324b0e96a81c4726ad187efa5f65f0ba63dc41d428d7aaae1bc001000`;
- APK SHA-256 `13c51f8a56e109a9dc57bc37cba3175ce5290b210f65b2692ce575d76194b55b`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- non-visual integration contracts 12/12;
- Shield focused tests 58/58, zero failures/errors/skips;
- Unified focused tests 136/136, zero failures/errors/skips;
- notification presenter/manifest, wake handoff, Launcher preservation, signed assembly, package/version/signer/integrity and artifact upload all PASS.

Detailed receipt: `docs/BOOP-V67-FINISHED-EYES-HUE-RECEIPT.md`.

## Eye state now canonical

v67 surgically brings the finished v65 procedural eye stack into the current notification lineage:

- procedural Canvas iris/pupil/catchlight renderer;
- v64 widened neutral sclera cleanup across the complete reading-motion envelope;
- v65 feathering into the approved original grey sclera shading;
- colour changer repaired to drive `proceduralIrisHueDegrees` directly;
- user hue remains iris-only and must not tint sclera, pupil, highlights, lids or other eye artwork;
- no shifted PNG iris patch is stacked over a stationary iris.

The v63 notification behavior and exact notification-hands hash contract remain preserved.

## Visual/device acceptance

**No visual acceptance was performed by GitHub.** No screenshot, golden-image, appearance/layout/animation comparison or pixel-judging CI was added or run. Ryan owns the real-device judgment.

CI/signer green. Physical acceptance of v67 is pending. Required physical checks include the finished v65 sclera/whites appearance, live iris-only colour changes, default cyan, reading motion with no ghost/socket crescent, blink, notification presentation, and existing wake/Home behavior.

Do not create or repoint a v67 rollback checkpoint until Ryan explicitly accepts this exact signed build.

## Preserved notification contract

Android's original notification remains authoritative. Locked BOOP presentation remains privacy-safe; tap preserves the source `PendingIntent`; swipe/timeout must not destructively cancel the shade notification; duplicate-alert prevention remains in force.

Exact approved notification hands remain:

- 1,809,990 bytes;
- SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`;
- Git blob `d47037271bf320f4f110e3f8416f59882062afac`.

## Physically accepted rollback

v59 remains the latest exact physically accepted rollback for the functional wake/name path:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference eye provenance. Never repoint protected/accepted checkpoints.

## Protected AIO state

Preserve approved black-lidded eye master, procedural iris-only hue, feathered sclera cleanup, blink, notifications, headphones/puppetry, one microphone owner, wake/name architecture, exact 100 ms bridge, uncensored speech request, HA names/Home controls, room isolation and Shield scaling.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a later merge. Ryan owns visual/device/acoustic acceptance. No automatic installs/grants or signer/package changes.
