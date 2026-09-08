# BOOP unified handoff

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Always re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v58 natural BOOP wake

Ryan physically tested signed v57 on the powered Pixel and proved the learned custom-name path is now genuinely natural and reusable, not Steve-specific:

- `Steve lights on` worked with no deliberate pause.
- `Steve show diagnostics` worked with no deliberate pause.
- voice rename to `Fred` triggered the five-sample local enrolment; BOOP asked for five repeats, listened, then `Fred lights on` worked and BOOP said `Done`.
- voice rename to `Jeff` repeated the same successful five-sample flow; `Jeff lights on` worked and BOOP said `Done`.
- permanent fallback `BOOP` still woke BOOP, but `BOOP lights on` in one breath required a pause after `BOOP`.

This physically confirms the v57 learned-name streaming fix and five-sample enrolment architecture. It also isolates the remaining no-pause failure to the permanent/default BOOP Sherpa path. Do not disturb the learned-name matcher, enrolment, HA routing, TTS, command parser, power policy or recovery in response to this issue.

### Root cause of the remaining BOOP-only pause

`BoopSherpaWakeSpotter` configured Sherpa with `config.setNumTrailingBlanks(1);`. The default BOOP path therefore required one trailing blank after the keyword before finalizing the wake. A deliberate pause supplied that blank; continuous `BOOP lights on` delayed the wake decision.

v57 learned names no longer depend on trailing silence, which explains the clean A/B result: Steve/Fred/Jeff worked naturally while BOOP still wanted the pause.

### v58 change

One functional production change only:

- default Sherpa BOOP changes `setNumTrailingBlanks(1)` to `setNumTrailingBlanks(0)`.

Preserved unchanged:

- BOOP keyword score/threshold and phrase assets;
- v57 streaming learned-name matcher and its quiet-room RMS gate;
- five-sample local training and no raw PCM persistence;
- one controller-owned 16 kHz microphone stream;
- v56 exact 1,600-sample / 100 ms command bridge and no old one-second pre-roll;
- silent wake handoff and no artificial speaker bing;
- three-second command window;
- external-power continuous wake, unplugged tap-to-talk;
- post-wake no-match/timeout silent real re-arm and hard-start fail-safe latch;
- pull-only `show diagnostics`;
- HA, TTS, visuals, Launcher and Shield behavior;
- package and permanent signer.

### TDD trail

Valid RED:

- commit `177cb0adcf685101b6cbc77478bacb3f569d539f`
- workflow `34252407406`
- materialized wake-handoff suite ran 2 tests; the existing silent-seam test passed and exactly the new `test_default_boop_does_not_require_trailing_silence_before_trigger` test failed because `setNumTrailingBlanks(0)` was absent.

GREEN functional change:

- commit `2f4b225998a40835d3db81595573b8af28009c06`
- workflow `34252652849`
- materialized natural-BOOP contract, focused tests, signed build/package/signer/archive checks and artifact upload passed.

Final v58 build code:

- built commit `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`
- version 58 / `1.2.12-unified-natural-boop-wake`
- workflow `34252950640` SUCCESS
- artifact `BOOP-Unified`, ID `10066828560`
- artifact digest `sha256:feb800dd99d6da15876892fbae4027def903bd43f3887b4b771c0e384a2ab372`
- APK SHA-256 `5a5b4846a55bc58d3444a8c8f178441af576e86695025c17c8d4483ad9fd01aa`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused tests 58/58, zero failures/errors/skips
- unified focused tests 92/92, zero failures/errors/skips
- materialized wake-handoff tests 2/2 PASS
- Launcher lint, signed assembly, package/version, manifest, signer, APK ZIP integrity and artifact upload PASS

Detailed receipt: `docs/BOOP-V58-NATURAL-BOOP-WAKE-RECEIPT.md`.

CI/signer green only. Physical v58 acceptance is pending.

## Required next Pixel test

Install v58 over v57 without uninstalling. Keep the Pixel on external power and speak naturally, with no deliberate pause:

1. `BOOP lights on`
2. `BOOP lights off`
3. `Jeff lights on` to confirm the learned-name path remains intact
4. optionally `BOOP show diagnostics`

Also leave BOOP listening around ordinary nearby speech briefly to catch any obvious false-positive regression. Sensitivity and keyword thresholds were not changed.

If default BOOP still needs a pause, capture diagnostics before changing sensitivity or the 100 ms bridge. If BOOP works but false positives become obvious, investigate Sherpa confirmation semantics rather than altering the custom-name matcher.

## Durable wake/name contracts

- `BOOP` permanently remains an accepted fallback wake name; custom names are additive, never replacements.
- five custom-name samples are local and use the existing single controller-owned 16 kHz PCM stream; raw enrolment PCM is not persisted.
- learned custom names must be matchable while speech is active, without requiring trailing silence.
- default BOOP is intended to finalize without requiring a trailing blank so one-breath `BOOP + command` is possible.
- never add a competing microphone listener.
- never restore the old full one-second wake-history pre-roll.
- keep the exact final 1,600 detector samples / 100 ms command bridge unless new physical evidence justifies redesign.
- wake-to-command handoff remains acoustically silent.
- any external power permits continuous phone wake; unpowered phone remains tap-to-talk.
- after TTS, re-arm only after BOOP finishes speaking.
- no-match/timeout failures are silent and genuinely re-arm; hard wake-engine startup failures remain fail-safe latched.
- `show diagnostics` remains pull-only; normal failures do not pop a modal or speak an error.

## Physically proven rollback checkpoint

The exact built v48 wake-arm code remains permanently pinned at `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. On Ryan's Pixel: charger -> Android green mic ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP. Do not repoint it.

v57 has physical acceptance for the learned custom-name path and five-sample voice rename flow, but v58 default-BOOP no-pause behavior is not physically accepted until Ryan tests the signed APK.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eyes, iris-only hue, blink timing/gates, headphones/puppetry and five-digit yellow hands. Preserve physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. Assistant remote invocation/audio remains separately unresolved. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
