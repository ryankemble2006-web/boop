# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current physically accepted wake candidate: v58 natural BOOP wake

Ryan physically tested signed v58 on the powered Pixel and confirmed permanent fallback `BOOP` now works naturally without a deliberate pause. He reported that spoken slowly or quickly, BOOP accepts the wake + command and performs it.

The learned custom-name path was already physically accepted on v57:

- `Steve lights on` worked naturally.
- `Steve show diagnostics` worked naturally.
- spoken rename to `Fred` invoked five-sample local training; `Fred lights on` worked and BOOP said `Done`.
- spoken rename to `Jeff` repeated the five-sample flow; `Jeff lights on` worked and BOOP said `Done`.

This physically accepts the natural one-breath wake boundary for both paths: custom learned names and permanent BOOP fallback.

## v58 change

Root cause of the remaining BOOP-only pause was Sherpa `config.setNumTrailingBlanks(1);`. v58 changes only this to `config.setNumTrailingBlanks(0);`.

It does not alter sensitivity, keyword score/threshold/phrases, v57 custom-name matching, five-sample training, the v56 exact 100 ms command bridge, microphone ownership, command window, powered wake/recovery behavior, diagnostics, HA, TTS, visuals, Launcher, Shield, package or signer.

## v58 receipt

- built code `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`
- version 58 / `1.2.12-unified-natural-boop-wake`
- workflow `34252950640` SUCCESS
- artifact `BOOP-Unified`, ID `10066828560`
- artifact digest `sha256:feb800dd99d6da15876892fbae4027def903bd43f3887b4b771c0e384a2ab372`
- APK SHA-256 `5a5b4846a55bc58d3444a8c8f178441af576e86695025c17c8d4483ad9fd01aa`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused tests 58/58, zero failures/errors/skips
- unified focused tests 92/92, zero failures/errors/skips
- materialized wake-handoff contracts 2/2 PASS
- Launcher lint, signed assembly, package/version, manifest, signer, APK integrity and artifact upload PASS

Detailed receipt: `docs/BOOP-V58-NATURAL-BOOP-WAKE-RECEIPT.md`.

## Protected checkpoints

Current physically accepted natural-wake rollback:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Do not repoint it.

Older wake-arm rollback remains:

`checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`

Do not repoint it either.

## Remaining physical observation

No blanket false-positive claim is recorded yet because Ryan did not explicitly report ordinary-room-chatter observation in the v58 acceptance message. If false positives later appear, investigate Sherpa confirmation timing before changing sensitivity, custom-name matching or the command bridge.

## Protected AIO state

BOOP remains the permanent fallback wake name; custom names are additive. Any external power allows continuous wake; unplugged phone remains tap-to-talk. Preserve one 16 kHz microphone owner, local five-say profiles, streaming learned-name matching, default BOOP zero-trailing-blank behavior, the v56 exact 100 ms command bridge, silent wake handoff, silent no-match/timeout re-arm, pull-only `show diagnostics`, HA names/Home controls, locked eyes/hue/blink, headphones/puppetry, five-digit yellow hands, room isolation and Shield scaling.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a later merge. Ryan owns visual/device/acoustic acceptance. No automatic installs/grants or signer/package changes.
