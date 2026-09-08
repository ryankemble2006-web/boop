# BOOP unified memory

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Durable wake architecture

BOOP permanently remains an accepted wake name. A custom name is additive, never a replacement. Custom-name training uses five local spoken examples from the existing single controller-owned 16 kHz PCM stream and stores only a compact pronunciation profile; raw enrolment PCM is not persisted. Never add a competing microphone listener.

Continuous phone wake is allowed while the phone is on any external power: wireless/magnetic dock, USB or AC. Unpowered handheld behavior remains tap-to-talk. The wake engine is the resting state while powered except while tap recognition, settings, command processing or TTS legitimately owns the microphone/state.

After normal TTS, wake re-arms only after BOOP finishes speaking. A post-wake Android command-ASR no-match or speech timeout is silent and performs a genuine controller/coordinator re-arm. A hard wake-engine/microphone startup failure remains fail-safe latched rather than tight-loop retrying.

`show diagnostics` is the hidden pull-only diagnostic command. Normal failures retain useful trace state but do not automatically display a modal, speak an error, persist raw audio or cloud-log diagnostics.

## Durable wake-to-command boundary

v53 physically proved that feeding the full wake-detection history into Android command ASR is wrong because Android transcribed the wake phrase itself. v54 removed that old full one-second pre-roll.

v56 established the bounded seam rule:

- never restore the old one-second wake-history pre-roll;
- preserve exactly the final 1,600 samples / 100 ms detector block as the command-ASR bridge;
- continue the same live PCM stream immediately afterward;
- keep natural wake-prefix transcript stripping as a defensive parser layer;
- keep wake-to-command handoff acoustically silent and do not restore the artificial wake speaker cue.

## Durable learned-name rule from v57

v56 physical testing showed learned custom names still required a deliberate pause before a command. A retained failed-session trace after `Steve lights on` was:

`WAKE ASR ERROR 7 +1502ms ready=28 begin=290 end=1405 partial=- final=-`

Source tracing found the learned matcher waited for two quiet 100 ms chunks before evaluating the name. v57 made learned names matchable while speech is still active and kept the older silence-ended matcher as fallback, with expensive feature extraction gated behind cheap speech/RMS activity.

Physical v57 evidence proves the architecture generally: `Steve lights on`, `Steve show diagnostics`, spoken rename/five-sample training to `Fred` followed by `Fred lights on`, and the same flow with `Jeff` all worked naturally.

Durable rule: learned custom wake names must remain matchable during active speech and must not regain a trailing-silence requirement or second microphone path.

## Durable default BOOP rule from v58

v57 physical testing isolated the remaining pause to permanent fallback `BOOP`. Root cause was Sherpa `config.setNumTrailingBlanks(1);`. v58 changes only this to `config.setNumTrailingBlanks(0);`.

Ryan physically accepted signed v58 on the powered Pixel: permanent/default `BOOP + command` works naturally without a deliberate pause whether spoken slowly or quickly.

Durable rule:

- default/permanent BOOP keeps zero intentional Sherpa trailing blanks;
- do not change wake score, threshold, phrase assets, sensitivity or the command bridge in response to this accepted behavior;
- preserve natural one-breath commands for both default BOOP and learned custom names.

Exact physically accepted v58 rollback:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Never repoint it.

## Durable uncensored-speech rule from v59

v59 requests `RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS=false` in both ordinary tap-to-talk and post-wake command recognition. BOOP must not add its own profanity blacklist or replace recognized adult/profane speech with asterisks.

Ryan physically accepted this on the Pixel: spoken adult/profane rename survives as the actual word, BOOP's five-sample prompt says the actual word instead of stars, and the local training flow completes.

Protected v59 rollback:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

Never repoint it.

v59 build landmarks:

- version 59 / `1.2.13-unified-uncensored-speech`
- workflow `34257117357` SUCCESS
- APK SHA-256 `7d48cc77407b69428bd2456b80cefbe56cb60f6f8326eb2e3686aa7b22bc7a2e`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Durable listening-eyes rule from v60

Ryan asked for a tiny visual indication that BOOP is actively listening, then explicitly rejected replacement/generated eye artwork. The listening state must therefore reuse the exact approved black-lidded BOOP eye master already in the app. Do not regenerate or substitute a listening-pose image.

v60 listening behavior:

- active only while tap-to-talk ASR or post-wake command ASR is actually listening;
- ordinary powered/wake-armed waiting is **not** active listening and must not pulse;
- use a gentle vertical runtime scale pulse in the existing renderer, half-cycle `520 ms`, scale `1.025` to `1.060`;
- use the static maximum attentive pose if Android animations are disabled;
- stop on recognition result/error/cancel, idle/sleep, thinking, Activity pause or destroy;
- preserve the existing `listening` gate that blocks idle blink while command ASR owns the interaction;
- the old static alpha-dim recognizer cue is not the listening indicator in v60.

Visual boundaries:

- approved PNG bytes remain untouched;
- iris-only hue behavior remains unchanged;
- approved geometry, black lids and blink timing/gates remain unchanged;
- listening feedback is a runtime renderer state only.

No wake detector, learned matcher, five-sample training, microphone ownership, exact 100 ms bridge, command window, powered wake/recovery, diagnostics, HA routing, TTS, Launcher, Shield behavior, package or signer is intentionally changed by v60.

### v60 TDD / release trail

RED:

- commit `5e2e0160994d44804f33a06faef3bd668d8a57d4`
- workflow `34258998143`
- new listening lifecycle test failed exactly because `BoopListeningCueState` did not exist.

GREEN/review:

- code-green commit `921e221e608e900537bb6d6c5797fc1cab7ee5ad`
- workflow `34259741946` passed materialization, lint, focused tests, signing, assembly, verification and upload
- materializer idempotency review at `490eb26b286c33e29e4d4a2e1ca497379bec4c61`, no runtime behavior change.

Final v60 build:

- built code `47e2edb1cd4415d8716a108cfc0f0cf7fb82de8a`
- version 60 / `1.2.14-unified-listening-eyes`
- workflow `34260135850` SUCCESS
- artifact `BOOP-Unified`, ID `10069626604`
- artifact digest `sha256:533672bee255644d6db50a60d0bc1dfe8d46b1184f46d9a2e6504b25197ac3a0`
- APK SHA-256 `4478be2d4b684ff2688fea5ae662a205bcce0ce5da1e25f162b4ef7ed411d1c6`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- wake-handoff contracts 3/3 PASS
- Shield focused tests 58/58; unified focused tests 93/93; zero failures/errors/skips
- Launcher lint, signed assembly, package/version, manifest, signer, APK integrity and artifact upload PASS.

v60 is CI/signer green but **not yet visually accepted**. Do not create a v60 rollback checkpoint until Ryan confirms the real Pixel appearance and lifecycle. If he wants the pulse tuned, modify only the listening pose parameters unless new physical evidence identifies a different defect.

Detailed receipt: `docs/BOOP-V60-LISTENING-EYES-RECEIPT.md`.

## Historical wake landmarks retained

The older exact v48 wake-arm rollback remains protected at `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. Never repoint it.

Wake progression: v51 natural prefix stripping; v53 persistent ASR evidence; v54 full-pre-roll repair; v55 any-external-power wake, silent real re-arm and pull-only diagnostics; v56 silent wake seam + exact 100 ms bridge; v57 streaming learned names; v58 default BOOP zero trailing blank; v59 uncensored recognizer request; v60 exact-approved-eye active-listening visual state.

## Clean Shield HOME boundary

The clean Nvidia Shield HOME replacement remains standalone for testing on `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Current unified Shield routing stays on the existing AIO Shield body. Do not merge the standalone launcher into unified until Ryan explicitly approves later.

## Permanent visual, Home and assistant contracts

Preserve the approved paired black-lidded eye master, approved geometry/alpha, iris-only hue, headphones/puppetry and five-digit yellow hands. Blink is user-confirmed working; preserve timing/curve/delay and motion/power/lifecycle gates.

HA device names and Home controls are physically accepted and must not regress. Home remains room-scoped and fail-closed; room changes tear down previous-room ownership/state before rebuilding. Shield density scaling remains idempotent and never system-wide.

Assistant ownership remains explicit/reversible through supported Android routes. Shield remote assistant activation/audio remains separately unresolved. No overlay mic, competing recorder, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs focused non-visual tests, compilation/lint, package/signature/integrity checks and artifact upload. Ryan owns screenshots, appearance, animation, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.
