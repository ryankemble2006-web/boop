# BOOP unified memory

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Durable wake architecture

BOOP permanently remains an accepted wake name. A custom name such as `Steve` is additive, never a replacement. Custom-name training uses five local spoken examples from the existing single controller-owned 16 kHz PCM stream and stores only a compact pronunciation profile; raw enrolment PCM is not persisted. Never add a competing microphone listener.

Continuous phone wake is allowed while the phone is on **any external power** (wireless/magnetic dock, USB or AC). Unpowered/undocked handheld behavior remains tap-to-talk. The wake engine is the resting state while powered except while tap recognition, settings, command processing or TTS legitimately owns the microphone/state.

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

## New durable custom-name rule from v57

Ryan physically proved v56 still required a deliberate pause between a learned custom wake name and its command. After failed one-breath `Steve lights on`, the pull-only diagnostic retained:

`WAKE ASR ERROR 7 +1502ms ready=28 begin=290 end=1405 partial=- final=-`

Sanitized state showed external power true, mic permission true and recovery `ARMED`. Android ASR saw speech boundaries but decoded no transcript; powered wake recovery itself was healthy.

Source tracing found the learned custom-name matcher depended on `BoopWakeUtteranceSegmenter`, which required two quiet 100 ms chunks before emitting an utterance. A custom name therefore effectively required about 200 ms trailing silence before evaluation. With `Steve lights on` spoken continuously, the learned-name detector consumed the command as part of the same utterance and woke too late.

Durable v57 rule:

- learned custom wake names must be eligible for matching **while speech is still active**; do not require trailing silence after the wake name;
- retain the older silence-ended matcher as fallback for deliberate wake-only utterances;
- use the same single controller-owned PCM stream, never a second recorder;
- gate expensive pronunciation-feature extraction behind cheap speech activity/RMS so quiet-room continuous wake does not run the heavy matcher continuously;
- retain explicit negative coverage for unrelated continuous speech;
- this streaming learned-name behavior does **not** redefine default Sherpa `BOOP`; diagnose default BOOP separately if its no-pause behavior differs.

## Current signed candidate: v57

TDD RED:

- commit `032ba991f4f70880bd2c108473c34b7701db7917`
- workflow `34248072092`
- focused unified suite ran 92 tests and failed exactly the new `trainedNameMatchesBeforeImmediateCommandFinishes` regression.

Initial GREEN:

- commit `964142749a2560b90a8be608b5c5deaa0f893066`
- workflow `34248439829` SUCCESS
- not shipped because review found the sliding-window feature extraction too eager for always-on use.

Reviewed/performance-gated build:

- built code `6c8131200ee0ff8a91464569a982312cb5512cb2`
- version 57 / `1.2.11-unified-streaming-custom-wake`
- workflow `34249050741`, attempt 2 SUCCESS
- artifact `BOOP-Unified`, ID `10065473551`
- artifact digest `sha256:496786d407a39d8034ec4dfd335e903c4ae8e3531a1128bf1f3e4a82440d1912`
- APK SHA-256 `3ce0593f61fbf6b35e6fbb664bc5bc7984b053844c7f6be83afe2955ab713459`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused tests 58/58; unified focused tests 92/92; zero failures/errors/skips
- no-trailing-silence learned-wake regression PASS
- unrelated-continuous-speech rejection PASS
- seamless wake-command handoff contract PASS
- Launcher lint, signed assembly, package/version, manifest, signer, APK integrity and artifact upload PASS.

Attempt 1 built and verified the same APK but GitHub artifact finalization returned a transient 403 after the bytes uploaded; attempt 2 succeeded without a code change.

Physical v57 acceptance is pending. Required powered-Pixel tests with no deliberate pause: `Steve lights on`, `Steve show diagnostics`, then separately `BOOP lights off`. If Steve works but BOOP does not, treat default Sherpa wake as a separate remaining boundary rather than widening the custom-name fix.

Detailed receipt: `docs/BOOP-V57-STREAMING-CUSTOM-WAKE-RECEIPT.md`.

## Physically proven rollback and historical wake landmarks

The exact built v48 wake-arm code remains the protected rollback at branch `checkpoint-boop-unified-v48-wake-arm`, commit `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. Never repoint it. On Ryan's Pixel: charger -> Android green mic ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP.

v51 repaired natural wake-prefix stripping before local routing. v53 introduced persistent physical ASR evidence. v54 repaired full wake-history contamination and physically enabled a separate post-wake rename command; Ryan then trained/used `Steve`. v55 added any-external-power wake, silent failure re-arm and pull-only diagnostics. v56 removed the artificial wake cue and retained exactly the final detector block as a 100 ms command bridge. Detailed historical receipts remain in `docs/`.

## Clean Shield HOME boundary

The clean Nvidia Shield HOME replacement remains standalone for testing on branch `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Current unified Shield routing stays on the existing AIO Shield body. Do not merge the standalone launcher into unified until Ryan explicitly approves later.

## Permanent visual, Home and assistant contracts

Preserve the approved paired black-lidded eye master, approved geometry/alpha, iris-only hue, headphones/puppetry and five-digit yellow hands. Blink is user-confirmed working; preserve timing/curve/delay and motion/power/lifecycle gates.

HA device names and Home controls are physically accepted and must not regress. Home remains room-scoped and fail-closed; room changes tear down previous-room ownership/state before rebuilding. Shield density scaling remains idempotent and never system-wide.

Assistant ownership remains explicit/reversible through supported Android routes. Shield remote assistant activation/audio remains separately unresolved. No overlay mic, competing recorder, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs focused non-visual tests, compilation/lint, package/signature/integrity checks and artifact upload. Ryan owns screenshots, appearance, animation, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.
