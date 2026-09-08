# BOOP unified handoff

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Always re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v57 streaming custom wake

Ryan physically tested signed v56 on the powered Pixel and confirmed the remaining natural-speech failure was not Home Assistant, tap ASR, wake-name training or recovery:

- `Steve` wakes BOOP and `BOOP` wakes BOOP;
- tap-to-talk works;
- `Steve`, pause, `lights off` works and says `Done`;
- `BOOP`, pause, `lights off` works and says `Done`;
- natural one-breath wake + command still requires a pause.

After a failed one-breath `Steve lights on`, Ryan used tap-to-talk to request the retained pull-only diagnostic. Sanitized trace:

`WAKE ASR ERROR 7 +1502ms ready=28 begin=290 end=1405 partial=- final=-`

Sanitized state: wake name `Steve`; external power true; microphone permission true; last recovery `ARMED`.

This proves downstream Android command ASR observed speech boundaries but decoded no transcript, while the silent failure path genuinely recovered to armed wake state.

### Root cause

The learned custom-name matcher was not truly streaming. `BoopWakeTemplateMatcher` delegated to `BoopWakeUtteranceSegmenter`, which only emitted a candidate after two quiet 100 ms chunks. A learned name therefore effectively required about 200 ms trailing silence before it could be evaluated.

With natural speech such as `Steve lights on`, the matcher kept consuming the command as part of the same utterance. Detection of the learned name arrived too late, so most/all of the command was already past before Android command ASR got useful audio.

This is separate from the v53/v54 full-pre-roll defect and from v56's speaker-cue/100 ms bridge fix. Do not restore the old one-second pre-roll and do not widen the v56 bridge blindly.

### v57 change

v57 changes only the learned custom-name path:

- a trained custom name can be evaluated while speech is still active, without waiting for trailing silence;
- the original silence-ended matcher remains a fallback;
- expensive pronunciation feature extraction is gated behind cheap RMS/speech activity so an always-listening powered Pixel does not continuously run the heavy matcher while the room is quiet;
- the same controller-owned 16 kHz PCM stream remains the only microphone owner;
- an unrelated-continuous-speech negative regression is included;
- default Sherpa `BOOP`, v56's exact 100 ms command bridge, silent wake handoff, three-second command window, powered wake, silent re-arm, pull-only diagnostics, five-say training, HA, visuals, Launcher and Shield behavior are otherwise unchanged.

### TDD and review trail

RED:

- commit `032ba991f4f70880bd2c108473c34b7701db7917`
- workflow `34248072092`
- focused unified suite: 92 tests, exactly 1 failure, the new no-trailing-silence custom-wake regression.

Initial GREEN:

- commit `964142749a2560b90a8be608b5c5deaa0f893066`
- workflow `34248439829` SUCCESS
- not shipped because review found its sliding-window feature extraction too eager for an always-listening device.

Reviewed/performance-gated final code:

- built code `6c8131200ee0ff8a91464569a982312cb5512cb2`
- version 57 / `1.2.11-unified-streaming-custom-wake`
- workflow `34249050741`, attempt 2 SUCCESS
- artifact `BOOP-Unified`, ID `10065473551`
- artifact digest `sha256:496786d407a39d8034ec4dfd335e903c4ae8e3531a1128bf1f3e4a82440d1912`
- APK SHA-256 `3ce0593f61fbf6b35e6fbb664bc5bc7984b053844c7f6be83afe2955ab713459`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused tests 58/58, zero failures/errors/skips
- unified focused tests 92/92, zero failures/errors/skips
- seamless wake-command handoff contract PASS
- Launcher lint, signed assembly, package/version, manifest, permanent signer and APK archive integrity PASS
- artifact upload PASS on attempt 2.

Attempt 1 built and verified the same APK/hash but artifact finalization returned a transient GitHub 403 after all bytes uploaded. No code changed between attempts.

Detailed receipt: `docs/BOOP-V57-STREAMING-CUSTOM-WAKE-RECEIPT.md`.

## Required next Pixel test

Install v57 over v56 without uninstalling. Keep the Pixel on external power. Use natural speech with **no deliberate pause**:

1. `Steve lights on`
2. `Steve show diagnostics`
3. `BOOP lights off`

The first two specifically test v57's learned custom-name streaming change. The third deliberately tests the default Sherpa BOOP path separately. If Steve works but BOOP still requires a pause, treat that as a separate remaining default-wake seam and do not bundle it into the custom-name fix.

Also wake once and give no command, then confirm it silently re-arms and accepts another wake afterward.

Physical v57 acceptance is pending Ryan's device test. CI/signer green is not physical green.

## Durable wake/name contracts

- `BOOP` permanently remains an accepted wake name; custom names are additive, never replacements.
- custom-name training is five local examples on the existing single 16 kHz PCM stream; raw enrolment PCM is not persisted.
- no second microphone listener.
- v54 rule remains: never feed the old full one-second wake-history ring into Android command ASR.
- v56's permitted command bridge remains exactly the final 1,600 samples / 100 ms detector block unless new physical evidence justifies a different design.
- wake-to-command handoff remains acoustically silent; do not restore the artificial wake speaker cue.
- any external power permits continuous phone wake; unpowered phone remains tap-to-talk.
- after TTS, wake re-arms only after BOOP finishes speaking.
- post-wake no-match/timeout failures are silent and genuinely re-arm; hard wake-engine startup failures remain fail-safe latched.
- `show diagnostics` remains pull-only; normal failures do not pop a modal or speak an error.

## Physically proven rollback checkpoint

The exact built v48 wake-arm code remains permanently pinned at branch `checkpoint-boop-unified-v48-wake-arm`, commit `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. On Ryan's Pixel: charger -> Android green mic ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP. Do not repoint it.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eyes, iris-only hue, blink timing/gates, headphones/puppetry and five-digit yellow hands. Preserve physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. Assistant remote invocation/audio remains separately unresolved. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
