# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v58 natural BOOP wake

Physical v57 result on Ryan's powered Pixel:

- learned custom wake names now work naturally without a deliberate pause before the command;
- `Steve lights on` worked;
- `Steve show diagnostics` worked;
- voice rename to `Fred` invoked the five-sample local training flow; BOOP listened to five repeats and then `Fred lights on` worked with `Done`;
- voice rename to `Jeff` repeated the same successful flow and `Jeff lights on` worked with `Done`;
- permanent fallback `BOOP` still wakes, but continuous `BOOP lights on` required a pause after BOOP.

This physically accepts the v57 learned-name streaming and five-sample rename path while isolating the remaining pause to default Sherpa BOOP.

## v58 root cause and change

`BoopSherpaWakeSpotter` used `config.setNumTrailingBlanks(1);`, requiring one trailing blank before the default keyword trigger finalized. A pause supplied that blank; one-breath `BOOP lights on` did not.

v58 changes only that setting to `config.setNumTrailingBlanks(0);`. It does not change sensitivity, keyword threshold/phrases, the v57 custom matcher, five-sample training, the v56 exact 100 ms command bridge, microphone ownership, command window, power/recovery behavior, diagnostics, HA, TTS, visuals, Launcher, Shield, package or signer.

## TDD evidence

Valid RED:

- `177cb0adcf685101b6cbc77478bacb3f569d539f`
- workflow `34252407406`
- materialized wake-handoff suite: existing seam test PASS, new default-BOOP no-trailing-silence regression FAIL exactly because `setNumTrailingBlanks(0)` was absent.

GREEN functional change:

- `2f4b225998a40835d3db81595573b8af28009c06`
- workflow `34252652849`
- new contract, focused functional tests, signed build/package/signer/archive checks and artifact upload PASS.

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

CI/signer green. Physical v58 acceptance pending.

## Required Pixel acceptance

Install v58 over v57 and keep the Pixel powered. Without deliberate pauses test `BOOP lights on`, `BOOP lights off`, then `Jeff lights on` to confirm the learned-name path did not regress. Optionally test `BOOP show diagnostics` in one breath. Briefly leave BOOP listening around ordinary nearby speech to catch any obvious false-positive regression; wake sensitivity and threshold are unchanged.

## Protected AIO state

BOOP remains the permanent fallback wake name; custom names are additive. Any external power allows continuous wake; unplugged phone remains tap-to-talk. Preserve one 16 kHz microphone owner, local five-say profiles, streaming learned-name matching, the v56 exact 100 ms command bridge, silent wake handoff, silent no-match/timeout re-arm, pull-only `show diagnostics`, HA names/Home controls, locked eyes/hue/blink, headphones/puppetry, five-digit yellow hands, room isolation and Shield scaling.

The exact physically proven v48 wake rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`; do not repoint it.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a later merge. Ryan owns visual/device/acoustic acceptance. No automatic installs/grants or signer/package changes.
