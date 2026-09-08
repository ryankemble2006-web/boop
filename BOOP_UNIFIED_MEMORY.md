# BOOP unified memory

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Durable wake architecture

BOOP permanently remains an accepted wake name. A custom name such as `Steve`, `Fred` or `Jeff` is additive, never a replacement. Custom-name training uses five local spoken examples from the existing single controller-owned 16 kHz PCM stream and stores only a compact pronunciation profile; raw enrolment PCM is not persisted. Never add a competing microphone listener.

Continuous phone wake is allowed while the phone is on any external power: wireless/magnetic dock, USB or AC. Unpowered handheld behavior remains tap-to-talk. The wake engine is the resting state while powered except while tap recognition, settings, command processing or TTS legitimately owns the microphone/state.

After normal TTS, wake re-arms only after BOOP finishes speaking. A post-wake Android command-ASR no-match or speech timeout is silent and performs a genuine controller/coordinator re-arm. A hard wake-engine/microphone startup failure remains fail-safe latched rather than tight-loop retrying.

`show diagnostics` is the hidden pull-only diagnostic command. Normal failures retain useful trace state but do not display a modal automatically, speak an error, persist raw audio or cloud-log diagnostics.

## Durable wake-to-command audio boundary

v53 physically proved that feeding the full wake-detection history into Android command ASR is wrong because Android transcribed the wake phrase itself. v54 removed that old full one-second pre-roll.

v56 established the bounded seam rule:

- never restore the old one-second wake-history pre-roll;
- preserve exactly the final 1,600 samples / 100 ms detector block as the command-ASR bridge;
- continue the same live PCM stream immediately afterward;
- keep natural wake-prefix transcript stripping as a defensive parser layer;
- keep wake-to-command handoff acoustically silent and do not restore the artificial wake speaker cue.

Do not widen that bridge merely because a wake + command phrase fails. Diagnose which detector path fired and when.

## Durable learned custom-name rule from v57

Ryan physically proved v56 required a deliberate pause between a learned custom wake name and its command. A retained failed-session diagnostic after `Steve lights on` was:

`WAKE ASR ERROR 7 +1502ms ready=28 begin=290 end=1405 partial=- final=-`

Source tracing found the learned matcher waited for two quiet 100 ms chunks before evaluating the name. Continuous `Steve lights on` therefore consumed command speech before wake handoff.

v57 changed learned custom names to streaming matching during active speech, kept the older silence-ended matcher as fallback and gated expensive acoustic feature extraction behind cheap speech/RMS activity.

Physical v57 evidence confirms this architecture generally:

- `Steve lights on` worked naturally with no deliberate pause;
- `Steve show diagnostics` worked naturally;
- spoken rename to `Fred` invoked five-sample local enrolment and `Fred lights on` worked with `Done`;
- spoken rename to `Jeff` repeated the same five-sample flow and `Jeff lights on` worked with `Done`.

Durable rule: learned custom wake names must be eligible for matching while speech is still active; do not reintroduce a trailing-silence requirement or a second microphone path.

## Durable default BOOP rule from v58

The v57 physical test isolated a separate problem: permanent fallback `BOOP` still woke, but continuous `BOOP lights on` required a pause while Steve/Fred/Jeff did not.

Root cause was the Sherpa default keyword configuration:

`config.setNumTrailingBlanks(1);`

That intentionally waited for one trailing blank after the keyword before finalizing the wake. v58 changes only this to:

`config.setNumTrailingBlanks(0);`

Physical v58 acceptance now confirms the intended behavior on Ryan's powered Pixel: default `BOOP` accepts natural wake + command speech without a deliberate pause, and Ryan reports it works whether spoken slowly or quickly.

Durable v58 rule:

- default/permanent BOOP uses zero intentional trailing blanks;
- do not change wake score, threshold, BOOP keyword phrases, sensitivity or command bridge as part of this behavior;
- custom learned-name streaming remains separate and unchanged;
- preserve the natural one-breath behavior for both default BOOP and learned names.

No blanket false-positive acceptance is recorded yet because ordinary-room-chatter observation was not explicitly reported in the v58 acceptance turn. If false positives later appear, investigate Sherpa confirmation timing before altering sensitivity or the custom matcher.

## Current physically accepted wake checkpoint: v58

Final v58 build:

- built code `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`
- version 58 / `1.2.12-unified-natural-boop-wake`
- workflow `34252950640` SUCCESS
- artifact `BOOP-Unified`, ID `10066828560`
- artifact digest `sha256:feb800dd99d6da15876892fbae4027def903bd43f3887b4b771c0e384a2ab372`
- APK SHA-256 `5a5b4846a55bc58d3444a8c8f178441af576e86695025c17c8d4483ad9fd01aa`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused tests 58/58; unified focused tests 92/92; zero failures/errors/skips
- materialized wake-handoff contracts 2/2 PASS
- Launcher lint, signed assembly, package/version, manifest, signer, APK integrity and artifact upload PASS.

Physically accepted rollback branch:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Never repoint it.

Detailed receipt: `docs/BOOP-V58-NATURAL-BOOP-WAKE-RECEIPT.md`.

## Physically proven rollback and wake landmarks

The older exact v48 wake-arm code remains protected at `checkpoint-boop-unified-v48-wake-arm`, commit `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. Never repoint it.

Wake progression retained for diagnosis:

- v51 repaired natural wake-prefix stripping.
- v53 introduced persistent physical ASR evidence.
- v54 repaired full wake-history contamination and physically enabled separate post-wake rename.
- v55 added any-external-power wake, silent failure re-arm and pull-only diagnostics.
- v56 removed the artificial wake cue and kept exactly the final detector block as a 100 ms command bridge.
- v57 made learned custom names stream-matchable and is physically accepted for natural Steve/Fred/Jeff commands plus five-sample spoken rename.
- v58 removed the default Sherpa trailing-blank wait and is physically accepted for natural slow/fast `BOOP + command` speech.

## Clean Shield HOME boundary

The clean Nvidia Shield HOME replacement remains standalone for testing on `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Current unified Shield routing stays on the existing AIO Shield body. Do not merge the standalone launcher into unified until Ryan explicitly approves later.

## Permanent visual, Home and assistant contracts

Preserve the approved paired black-lidded eye master, approved geometry/alpha, iris-only hue, headphones/puppetry and five-digit yellow hands. Blink is user-confirmed working; preserve timing/curve/delay and motion/power/lifecycle gates.

HA device names and Home controls are physically accepted and must not regress. Home remains room-scoped and fail-closed; room changes tear down previous-room ownership/state before rebuilding. Shield density scaling remains idempotent and never system-wide.

Assistant ownership remains explicit/reversible through supported Android routes. Shield remote assistant activation/audio remains separately unresolved. No overlay mic, competing recorder, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs focused non-visual tests, compilation/lint, package/signature/integrity checks and artifact upload. Ryan owns screenshots, appearance, animation, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.
