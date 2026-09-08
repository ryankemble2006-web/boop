# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v62 single-layer reading eyes

v61 made the active-listening cue much more obvious with the exact approved eyes: 1.16x zoom, slightly downward reading gaze, smooth left/right sweep, plus wider iris hue coverage. Physical Pixel testing showed the motion itself was good but exposed a compositing defect: the stationary original iris/pupil remained visible behind the shifted reading patch, creating a double/ghost eye.

v62 changes only that compositing boundary. While active listening the face is drawn on a temporary layer, the stationary iris aperture is cleared, and a single shifted iris/pupil patch from the same approved bitmap is drawn into it. No replacement eye artwork is used. Wake/audio, HA routing, TTS, blink, package and signer are intentionally unchanged.

TDD/release evidence:

- RED `7b19f2c81f0fa2a6cb7a3512186298b3bdf4b5e4`, workflow `34265189069`: 97 focused unified tests ran and exactly the new anti-ghost compositing test failed.
- compositing policy `b54e6fad8dc30e4f90ff13a126038053d8d73881`.
- renderer `d7e4632ab014b026459fd63d8d9d17a8fd9dc16f`.
- version bump `55753ff70428a35b7b3f6d9da668b01e358fcb62`.
- final built code `6877bf3d97d069eda950938060e355da039d53cf`.
- version 62 / `1.2.16-unified-single-layer-reading-eyes`.
- workflow `34265615662` SUCCESS.
- artifact `BOOP-Unified`, ID `10071797863`.
- artifact digest `sha256:adb58b5eb0373fa1b581dccd625638a6bcaf151477215b57c930197ed52142ef`.
- APK SHA-256 `5def47113929e6b0aa59b868e5880056607473fb3ba1ff4e54ac3f771bb7bc3b`.
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- wake-handoff contracts 3/3 PASS.
- Shield focused tests 58/58, zero failures/errors/skips.
- unified focused tests 97/97, zero failures/errors/skips.
- Launcher lint, signed assembly, package/version, manifest, signer, APK integrity and artifact upload PASS.

Detailed receipt: `docs/BOOP-V62-SINGLE-LAYER-READING-EYES-RECEIPT.md`.

CI/signer green. Physical visual acceptance pending. v61 is physically rejected for eye-layer ghosting and is not a rollback checkpoint.

## Required Pixel acceptance

Install v62 over v61 without uninstalling. Confirm active listening from both wake-name command and tap-to-talk shows only one moving pupil/iris per eye, with no stationary ghost underneath. Confirm the reading motion still looks good, the previously missed blue iris portions now follow the selected hue, and natural current-name plus permanent BOOP commands remain operational.

Do not create a v62 checkpoint until Ryan physically accepts this exact signed build.

## Physically accepted rollback

v59 remains the latest exact physically accepted rollback for the functional wake/name path:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

Ryan physically confirmed adult/profane spoken rename, five-sample training and use work without asterisk masking.

v58 natural-wake rollback remains:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. Never repoint accepted checkpoints.

## Protected AIO state

BOOP remains the permanent fallback wake name; custom names are additive. Any external power allows continuous wake; unplugged phone remains tap-to-talk. Preserve one 16 kHz microphone owner, local five-say profiles, streaming learned-name matching, default BOOP zero-trailing-blank behavior, exact 100 ms bridge, silent wake handoff, silent no-match/timeout re-arm, pull-only `show diagnostics`, uncensored recognizer request, HA names/Home controls, locked eye master/hue/blink, headphones/puppetry, five-digit yellow hands, room isolation and Shield scaling.

Listening animation must use the existing approved eye bitmap. Do not regenerate listening poses. A moving gaze must replace the stationary iris/pupil aperture instead of stacking a second visible eye layer over it.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a later merge. Ryan owns visual/device/acoustic acceptance. No automatic installs/grants or signer/package changes.
