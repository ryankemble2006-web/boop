# BOOP unified status

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v63 notifications

BOOP's notification presenter is merged into canonical Unified and the exact-hand binary blocker is resolved.

Built source:

`2b0484cb9c95b0836c2bb6e93f6d7c890bc0e2a4`

Release evidence:

- version 63 / `1.2.17-unified-notifications`;
- workflow `34308822296` SUCCESS;
- Shield HOME routing workflow `34308822310` SUCCESS;
- artifact `BOOP-Unified`, ID `10087693779`;
- artifact digest `sha256:5673289f3a11cceceec99cfdeac2506c17eae0fbdfd86560b3c407f49c9e96ea`;
- APK SHA-256 `e92963c4bff18b8b8fb2b88202aac3207186edb4af05113874d92e0e455e130f`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused tests 58/58, zero failures/errors/skips;
- unified focused tests 136/136, zero failures/errors/skips;
- notification presenter, wake handoff, Launcher preservation, signed assembly, package/version/signer/integrity and artifact upload all PASS.

Exact approved notification hands:

- 1,809,990 bytes;
- SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`;
- Git blob `d47037271bf320f4f110e3f8416f59882062afac`.

Detailed receipt: `docs/BOOP-V63-NOTIFICATIONS-RECEIPT.md`.

CI/signer green. Physical Pixel visual/lock-screen/acoustic acceptance is pending.

## Required Pixel acceptance

Install v63 over the current BOOP without uninstalling. Confirm unlocked presentation, privacy-safe locked presentation, source `PendingIntent` tap behavior, non-destructive swipe and timeout behavior, no duplicate cue on noisy Android channels, BOOP cue/vibration on an appropriate known-silent channel, and the carried v62 single-layer listening-eye fix.

Do not create a v63 rollback checkpoint until Ryan explicitly accepts this exact signed build.

## Physically accepted rollback

v59 remains the latest exact physically accepted rollback for the functional wake/name path:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. Never repoint accepted checkpoints.

## Protected AIO state

Android's original notification remains authoritative; BOOP mirrors it. Locked presentation stays privacy-safe. Swipe/timeout must not destructively cancel the shade notification. Preserve exact approved hands/eyes, iris-only hue, blink, headphones/puppetry, one microphone owner, wake/name architecture, exact 100 ms bridge, uncensored speech request, HA names/Home controls, room isolation and Shield scaling.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a later merge. Ryan owns visual/device/acoustic acceptance. No automatic installs/grants or signer/package changes.
