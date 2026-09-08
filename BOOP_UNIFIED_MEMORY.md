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

Physical v57 evidence proves the architecture generally:

- `Steve lights on` worked naturally with no deliberate pause;
- `Steve show diagnostics` worked naturally;
- spoken rename to `Fred` invoked five-sample local enrolment and `Fred lights on` worked with `Done`;
- spoken rename to `Jeff` repeated the same five-sample flow and `Jeff lights on` worked with `Done`.

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

Never repoint it. v58 workflow `34252950640`; artifact `10066828560`; APK SHA-256 `5a5b4846a55bc58d3444a8c8f178441af576e86695025c17c8d4483ad9fd01aa`.

## Durable speech-censorship rule from v59

After v58 acceptance, Ryan physically observed that spoken adult/profane wake names were returned by Android recognition as asterisks. BOOP then stored that masked transcript and TTS spoke the literal stars during five-sample training.

BOOP's own rename code does not contain a profanity filter. The masking boundary was Android speech recognition: both the ordinary tap recognizer and post-wake command recognizer omitted `RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS`, leaving the recognition service free to use its default masking behavior.

v59 requests in **both** recognition paths:

`intent.putExtra(RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS, false);`

Durable rule:

- BOOP does not intentionally censor user-selected wake names or ordinary recognized speech;
- tap-to-talk and post-wake recognition both request unmasked offensive words;
- do not add a BOOP-side profanity blacklist or replace a recognized adult word with asterisks;
- the Android recognizer service ultimately decides whether it honors this request, so physical acceptance is required before promoting v59;
- if a device still returns stars, investigate the active recognizer service/transcript boundary rather than modifying wake sensitivity, local training, the command bridge or rename parser.

## Current signed candidate: v59 uncensored speech

TDD RED:

- commit `b021b28d3f635f884dbc144252834b315712c94b`
- workflow `34256341474`
- materialized wake-handoff suite ran 3 tests; exactly the new uncensored-speech contract failed while the two existing seam tests passed.

GREEN / release trail:

- patch `09971b5a734c23134ed1b326aa9cab5036674855`
- materializer wiring `3b2ebeb66c357b947bec4c793165d3e97a8b994f`
- intermediate workflow `34256484198` reached 3/3 wake-handoff PASS before being superseded by the version bump
- v59 bump `0f2fe9d12e473b122938d06d767facc646c65476`
- first v59-labelled run `34256571189` compiled/tested/signed but failed package verification only because the workflow still expected v58 metadata
- verifier-only correction `136b56e6faac8ce450b957ac3057a379c68c7b7b` updated expected version fields without changing app behavior.

Final v59 build:

- built code `136b56e6faac8ce450b957ac3057a379c68c7b7b`
- version 59 / `1.2.13-unified-uncensored-speech`
- workflow `34257117357` SUCCESS
- artifact `BOOP-Unified`, ID `10068400476`
- artifact digest `sha256:f397a85aa4747a51394d0bbc42266658cc6609a355a25b210bd761d8813e023c`
- APK SHA-256 `7d48cc77407b69428bd2456b80cefbe56cb60f6f8326eb2e3686aa7b22bc7a2e`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- materialized wake-handoff contracts 3/3 PASS
- Shield focused tests 58/58; unified focused tests 92/92; zero failures/errors/skips
- Launcher lint, signed assembly, package/version, manifest, signer, APK integrity and artifact upload PASS.

CI/signer green only. Required physical test: install over v58, voice-rename to an adult/profane wake name, verify the five-sample prompt contains the actual word rather than stars, finish training, use the new name with a normal HA command, and confirm permanent BOOP still works naturally.

Detailed receipt: `docs/BOOP-V59-UNCENSORED-SPEECH-RECEIPT.md`.

## Historical wake landmarks retained

The older exact v48 wake-arm rollback remains protected at `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. Never repoint it.

Wake progression: v51 natural prefix stripping; v53 persistent ASR evidence; v54 full-pre-roll repair; v55 any-external-power wake, silent real re-arm and pull-only diagnostics; v56 silent wake seam + exact 100 ms bridge; v57 streaming learned names; v58 default BOOP zero trailing blank; v59 unmasked Android recognition request.

## Clean Shield HOME boundary

The clean Nvidia Shield HOME replacement remains standalone for testing on `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Current unified Shield routing stays on the existing AIO Shield body. Do not merge the standalone launcher into unified until Ryan explicitly approves later.

## Permanent visual, Home and assistant contracts

Preserve the approved paired black-lidded eye master, approved geometry/alpha, iris-only hue, headphones/puppetry and five-digit yellow hands. Blink is user-confirmed working; preserve timing/curve/delay and motion/power/lifecycle gates.

HA device names and Home controls are physically accepted and must not regress. Home remains room-scoped and fail-closed; room changes tear down previous-room ownership/state before rebuilding. Shield density scaling remains idempotent and never system-wide.

Assistant ownership remains explicit/reversible through supported Android routes. Shield remote assistant activation/audio remains separately unresolved. No overlay mic, competing recorder, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs focused non-visual tests, compilation/lint, package/signature/integrity checks and artifact upload. Ryan owns screenshots, appearance, animation, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.
