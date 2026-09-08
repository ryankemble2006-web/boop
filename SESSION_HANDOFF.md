# BOOP unified handoff

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Always re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v60 listening eyes

Ryan asked for a tiny visual behavior that shows when BOOP is actively listening. A generated replacement-eye concept was explicitly rejected. The v60 implementation therefore uses the **exact existing approved BOOP eye artwork** and changes only runtime pose/state.

Listening behavior:

- active only while tap-to-talk ASR or post-wake command ASR is actually listening;
- uses the existing approved black-lidded eye renderer, no new or regenerated eye art;
- gentle vertical attentive/breathing pulse, `520 ms` half-cycle, scale `1.025` to `1.060`;
- if system animations are disabled, use the static maximum attentive pose;
- stop on result/error/cancel, idle/sleep, thinking, Activity pause or destroy;
- ordinary powered/wake-armed waiting does **not** pulse;
- the prior static alpha-dim listening cue is removed from active recognizer start/stop paths.

Preserved unchanged: approved eye bitmap bytes, iris-only hue, blink timing/gates, wake detectors, learned-name matching, five-sample enrolment, one controller-owned 16 kHz microphone stream, exact 100 ms command bridge, three-second command window, silent wake handoff, powered wake/recovery, pull-only diagnostics, HA routing, TTS, Launcher, Shield behavior, package and signer.

Detailed receipt: `docs/BOOP-V60-LISTENING-EYES-RECEIPT.md`.

### v60 TDD / build receipt

RED:

- test `source-test/BoopWakeListeningCueStateTest.java`
- commit `5e2e0160994d44804f33a06faef3bd668d8a57d4`
- workflow `34258998143`
- failed exactly because the new `BoopListeningCueState` lifecycle helper did not yet exist.

GREEN/review:

- code-green commit `921e221e608e900537bb6d6c5797fc1cab7ee5ad`
- workflow `34259741946` passed materialization, lint, functional tests, signing, assembly, verification and upload
- review made the materializer idempotent at `490eb26b286c33e29e4d4a2e1ca497379bec4c61` without changing runtime behavior.

Final signed v60:

- built commit `47e2edb1cd4415d8716a108cfc0f0cf7fb82de8a`
- version 60 / `1.2.14-unified-listening-eyes`
- workflow `34260135850` SUCCESS
- artifact `BOOP-Unified`, ID `10069626604`
- artifact digest `sha256:533672bee255644d6db50a60d0bc1dfe8d46b1184f46d9a2e6504b25197ac3a0`
- APK SHA-256 `4478be2d4b684ff2688fea5ae662a205bcce0ce5da1e25f162b4ef7ed411d1c6`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- materialized wake-handoff contracts 3/3 PASS
- Shield focused tests 58/58, zero failures/errors/skips
- unified focused tests 93/93, zero failures/errors/skips
- Launcher lint, signed assembly, package/version, manifest, signer, APK ZIP integrity and artifact upload PASS.

CI/signer green. Physical visual acceptance is pending and belongs to Ryan.

## v59 uncensored speech is physically accepted

Ryan installed v59 and physically confirmed the adult/profane spoken rename path works: the actual word survives recognition, BOOP asks for the five repetitions using the real word rather than asterisks, and training completes. Treat the Android profanity-masking defect as physically passed on this Pixel.

Exact v59 built code:

- built commit `136b56e6faac8ce450b957ac3057a379c68c7b7b`
- workflow `34257117357` SUCCESS
- APK SHA-256 `7d48cc77407b69428bd2456b80cefbe56cb60f6f8326eb2e3686aa7b22bc7a2e`

Protected v59 rollback:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

Never repoint it.

v58 natural-wake rollback remains:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Never repoint it either. The older v48 wake-arm rollback also remains permanently pinned at `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

## Required next Pixel test

Install v60 over v59 without uninstalling.

1. While powered, say a natural current-wake-name + HA command. The approved eyes should show the listening pulse only while command ASR is active, then return to normal before/when the spoken reply begins.
2. Tap-to-talk should show the same cue while listening and stop afterward.
3. Confirm ordinary powered wake-armed waiting does not pulse.
4. Confirm the current custom wake name and permanent `BOOP` still execute natural no-pause commands.

If the visual pulse needs tuning, change only the listening pose parameters after Ryan's real-device observation. Do not regenerate eye artwork or disturb the accepted wake/audio boundary. Do not create a v60 checkpoint until Ryan visually accepts this exact signed build.

## Durable wake/name contracts

- `BOOP` permanently remains an accepted fallback wake name; custom names are additive, never replacements.
- five custom-name samples are local on the existing single controller-owned 16 kHz PCM stream; raw enrolment PCM is not persisted.
- learned custom names are matchable while speech is active without trailing silence.
- default BOOP uses zero intentional Sherpa trailing blanks so natural `BOOP + command` remains seamless.
- never add a competing microphone listener or restore the old full one-second wake-history pre-roll.
- keep the exact final 1,600 detector samples / 100 ms command bridge unless new physical evidence justifies redesign.
- wake-to-command handoff remains acoustically silent.
- any external power permits continuous phone wake; unpowered phone remains tap-to-talk.
- after TTS, re-arm only after BOOP finishes speaking.
- no-match/timeout failures are silent and genuinely re-arm; hard wake-engine startup failures remain fail-safe latched.
- `show diagnostics` remains pull-only.
- BOOP does not intentionally censor recognized adult/profane speech; both recognition intents request unmasked offensive words.
- listening feedback must reuse the exact approved eye master and be driven by active recognizer state, not by a replacement pose image.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eyes, iris-only hue, blink timing/gates, headphones/puppetry and five-digit yellow hands. Preserve physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. Assistant remote invocation/audio remains separately unresolved. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
