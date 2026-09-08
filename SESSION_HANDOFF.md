# BOOP unified handoff

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Always re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v59 uncensored speech

Ryan physically accepted v58 natural wake and then reported that spoken rename to adult/profane words was being converted to asterisks. BOOP subsequently spoke those literal stars during the five-sample training prompt.

Source tracing isolated this outside BOOP's rename parser. `BoopWakeNameIntent` accepts the recognizer transcript and `BoopWakeName` only normalizes whitespace/length. Both Android speech-recognition intents omitted `RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS`, leaving Android recognition-service profanity masking enabled by default.

v59 makes one bounded behavior change:

`intent.putExtra(RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS, false);`

is now requested in both ordinary tap-to-talk recognition and post-wake command recognition. No wake detector, learned-name matcher, five-sample training, rename parser, Home Assistant routing, TTS, command bridge/window, power/recovery policy, diagnostics, visuals, Launcher, Shield behavior, package or signer is intentionally changed.

The recognizer service ultimately decides whether it honors this extra, so profanity recognition is CI/signer green but physically pending.

### v59 TDD / build receipt

RED:

- commit `b021b28d3f635f884dbc144252834b315712c94b`
- workflow `34256341474`
- wake-handoff suite: 3 tests ran, exactly the new uncensored-speech contract failed; the two established seam contracts passed.

GREEN materialization:

- patch script `09971b5a734c23134ed1b326aa9cab5036674855`
- materializer wiring `3b2ebeb66c357b947bec4c793165d3e97a8b994f`
- intermediate workflow `34256484198`: new wake-handoff suite 3/3 PASS before the run was superseded by the v59 release bump.

Release metadata:

- version bump `0f2fe9d12e473b122938d06d767facc646c65476`
- version 59 / `1.2.13-unified-uncensored-speech`
- first labelled run `34256571189` compiled/tested/signed but failed only because CI still expected v58 package metadata.
- verifier-only correction `136b56e6faac8ce450b957ac3057a379c68c7b7b` updated the expected version fields and changed no app behavior.

Final signed candidate:

- built commit `136b56e6faac8ce450b957ac3057a379c68c7b7b`
- workflow `34257117357` SUCCESS
- artifact `BOOP-Unified`, ID `10068400476`
- artifact digest `sha256:f397a85aa4747a51394d0bbc42266658cc6609a355a25b210bd761d8813e023c`
- APK SHA-256 `7d48cc77407b69428bd2456b80cefbe56cb60f6f8326eb2e3686aa7b22bc7a2e`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- materialized wake-handoff contracts 3/3 PASS
- Shield focused tests 58/58, zero failures/errors/skips
- unified focused tests 92/92, zero failures/errors/skips
- Launcher lint, signed assembly, package/version, manifest, signer, APK ZIP integrity and artifact upload PASS.

Detailed receipt: `docs/BOOP-V59-UNCENSORED-SPEECH-RECEIPT.md`.

## Required next Pixel test

Install v59 over v58 without uninstalling. Use the current wake name to say a natural rename command containing an adult/profane new name. The five-sample prompt must contain the actual recognized word rather than asterisks. Complete the five repetitions, then test the new wake name with a normal Home Assistant command and confirm permanent `BOOP` still accepts natural no-pause commands.

If Android still returns asterisks, do not change wake sensitivity, the custom matcher, command bridge or rename parser. Capture the transcript/diagnostic path and investigate the active Android recognition service's handling of the masking extra.

## Current physically accepted rollback: v58 natural BOOP wake

Ryan physically tested signed v58 on the powered Pixel and confirmed permanent/default `BOOP` handles natural wake + command speech without a deliberate pause whether spoken slowly or quickly.

Previously accepted learned-name evidence remains valid:

- `Steve lights on` worked naturally with no deliberate pause.
- `Steve show diagnostics` worked naturally.
- spoken rename to `Fred` triggered five-sample local enrolment; `Fred lights on` worked and BOOP said `Done`.
- spoken rename to `Jeff` repeated the five-sample flow; `Jeff lights on` worked and BOOP said `Done`.

Exact v58 rollback:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Never repoint it. v58 workflow `34252950640`, artifact `10066828560`, APK SHA-256 `5a5b4846a55bc58d3444a8c8f178441af576e86695025c17c8d4483ad9fd01aa`.

The older exact v48 wake-arm rollback remains permanently pinned at `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`; never repoint it.

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
- BOOP does not intentionally censor user-selected wake names; both recognition intents request unmasked offensive words as of v59.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eyes, iris-only hue, blink timing/gates, headphones/puppetry and five-digit yellow hands. Preserve physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. Assistant remote invocation/audio remains separately unresolved. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
