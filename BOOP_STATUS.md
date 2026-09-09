# BOOP unified status

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v68 hue wire fix

Ryan physically reported that v67's hue control did not work. Root cause was a late legacy bitmap hue-cache patch being rerun from `scripts/patch-unified-shield-dashboard.py` after the procedural-eye stack had already installed its own hue setter.

v68 removes that late overwrite only. The finished v65 eye geometry/sclera stack is unchanged.

Built source:

`91e562754be31745a5ee76538ebef50e6c6a9b2d`

Release evidence:

- version 68 / `1.2.22-unified-hue-wire-fix`;
- workflow `34314023763` SUCCESS;
- artifact `BOOP-Unified`, ID `10089480166`;
- artifact digest `sha256:f1b546f52800260a878940db0b8f074b1a8a07fec9df4370b2843acfc59c1699`;
- APK SHA-256 `571f0a501e5a3df921fc1e521ae235810860df3f493c223af491f974cd30e352`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused tests 58/58, zero failures/errors/skips;
- Unified focused tests 136/136, zero failures/errors/skips;
- notification presenter/manifest, wake handoff, Launcher preservation, signed assembly, package/version/signer/integrity and artifact upload all PASS.

Regression workflow `34313965830` failed before the production fix exactly because the late Shield dashboard pass still referenced the old hue-cache patch.

Detailed receipt: `docs/BOOP-V68-HUE-WIRE-FIX-RECEIPT.md`.

## Acceptance boundary

**No visual acceptance was performed by GitHub.** v68 is CI/signer green only.

Immediate physical check: move the existing eye hue slider across obvious colours and confirm the procedural iris updates live while sclera, pupils, catchlights and black lids remain unchanged. Confirm the finished v65 sclera/white blend is unchanged.

Do not create or repoint a v68 rollback checkpoint until Ryan explicitly accepts this exact signed APK.

## Durable eye ordering

The canonical procedural eye stages remain:

1. `patch-unified-reading-eyes.py`;
2. `patch-v64-procedural-sclera.py`;
3. `patch-v65-feathered-sclera.py`.

No later materialization stage may rerun the legacy bitmap hue setter after those stages. The Shield dashboard pass must remain unrelated to Wall iris tinting.

## Preserved contracts

Android's original notification remains authoritative; BOOP mirrors it. Locked notification presentation stays privacy-safe. Exact notification hands remain locked. Preserve procedural iris-only hue, finished sclera feathering, approved black-lidded eye master, blink, notifications, headphones/puppetry, one microphone owner, wake/name architecture, exact 100 ms bridge, uncensored speech request, HA names/Home controls, room isolation and Shield scaling.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a later merge. No automatic installs/grants or signer/package changes.

## Physically accepted rollback

Latest exact physically accepted rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`.

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected eye provenance. Never repoint protected/accepted checkpoints.
