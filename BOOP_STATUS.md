# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v60 listening eyes

v60 adds a visual listening state while preserving the exact approved BOOP eye artwork. No new eye image is used.

Behavior:

- cue starts only when tap-to-talk ASR or post-wake command ASR is actually listening;
- existing approved black-lidded eyes perform a gentle vertical attentive pulse;
- half-cycle `520 ms`, vertical scale `1.025` to `1.060`;
- static maximum attentive pose when Android animations are disabled;
- cue stops on recognition result/error/cancel, idle/sleep, thinking, Activity pause or destroy;
- ordinary powered/wake-armed waiting does not pulse;
- previous static alpha-dim listening cue is removed from active recognizer start/stop paths.

No approved bitmap bytes, hue behavior, blink timing/gates, wake/audio path, command routing, TTS, Launcher, Shield, package or signer is intentionally changed.

TDD/build evidence:

- RED commit `5e2e0160994d44804f33a06faef3bd668d8a57d4`, workflow `34258998143`: new listening-state test failed exactly on the missing lifecycle helper.
- code-green commit `921e221e608e900537bb6d6c5797fc1cab7ee5ad`, workflow `34259741946`: materialization, lint, focused tests, signer/build/verification/upload passed.
- materializer idempotency review fix `490eb26b286c33e29e4d4a2e1ca497379bec4c61`, no runtime behavior change.

Final v60 receipt:

- built code `47e2edb1cd4415d8716a108cfc0f0cf7fb82de8a`
- version 60 / `1.2.14-unified-listening-eyes`
- workflow `34260135850` SUCCESS
- artifact `BOOP-Unified`, ID `10069626604`
- artifact digest `sha256:533672bee255644d6db50a60d0bc1dfe8d46b1184f46d9a2e6504b25197ac3a0`
- APK SHA-256 `4478be2d4b684ff2688fea5ae662a205bcce0ce5da1e25f162b4ef7ed411d1c6`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- wake-handoff contracts 3/3 PASS
- Shield focused tests 58/58, zero failures/errors/skips
- unified focused tests 93/93, zero failures/errors/skips
- Launcher lint, signed assembly, package/version, manifest, signer, APK integrity and artifact upload PASS.

Detailed receipt: `docs/BOOP-V60-LISTENING-EYES-RECEIPT.md`.

CI/signer green. Physical visual acceptance pending.

## v59 is physically accepted

Ryan physically confirmed v59's uncensored speech path on the Pixel. Spoken adult/profane rename no longer becomes asterisks, the five-sample prompt uses the actual recognized word, and local training completes.

Protected exact v59 rollback:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

Never repoint it.

v58 natural-wake rollback remains:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. Never repoint any accepted checkpoint.

## Required Pixel acceptance for v60

Install v60 over v59 without uninstalling. Confirm the same approved eyes visibly pulse only while active listening, both after wake and during tap-to-talk, then return to ordinary eyes before/when BOOP replies. Confirm idle powered wake-armed waiting does not pulse. Reconfirm the current custom name and permanent BOOP still execute natural no-pause HA commands.

Do not create a v60 checkpoint until Ryan accepts the visual on the real Pixel. If tuning is requested, adjust only the listening-pulse parameters and preserve the approved asset plus all accepted wake/audio behavior.

## Protected AIO state

BOOP remains the permanent fallback wake name; custom names are additive. Any external power allows continuous wake; unplugged phone remains tap-to-talk. Preserve one 16 kHz microphone owner, local five-say profiles, streaming learned-name matching, default BOOP zero-trailing-blank behavior, the exact 100 ms command bridge, silent wake handoff, silent no-match/timeout re-arm, pull-only `show diagnostics`, uncensored recognizer request, HA names/Home controls, locked eye master/hue/blink, headphones/puppetry, five-digit yellow hands, room isolation and Shield scaling.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a later merge. Ryan owns visual/device/acoustic acceptance. No automatic installs/grants or signer/package changes.
