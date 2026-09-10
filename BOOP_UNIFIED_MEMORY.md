# BOOP unified memory

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Fresh `main` owns shared product contracts. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current physically usable checkpoint: v88

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Ryan physically accepted exact v88 as the usable rollback because ordinary Android TextToSpeech speaks again and failed Natural Voice attempts no longer poison normal BOOP speech. This does **not** mean Kokoro works. Never repoint the checkpoint or weaken the runtime-proof isolation.

## Current signed but physically unaccepted candidate: v91

Exact canonical source/build head:

`11650313221ae5bf997dbb93b6a905bfdc7da1ed`

Release:

- versionCode `91`;
- versionName `1.2.91-unified-static-track-state-fix`;
- canonical Unified workflow `34433115316`: SUCCESS;
- canonical Shield HOME routing `34433115318`: SUCCESS;
- artifact ID `10135283428`;
- artifact ZIP SHA-256 `e4f2fb47e2a3b5db2d338512b44ebdbfa840a42b03acc99a5bce02243f8c820c`;
- APK SHA-256 `42dc50d12031a674aa751918f6bfd6b4deab8b6ced95332a437f4068124fe53d`;
- signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused tests `58/58`;
- Unified focused tests `157/157`;
- package/version/entry activity/signer/archive verification green.

The canonical artifact was downloaded after CI and independently checked against its receipts. v91 still needs physical Natural Voice evidence.

## Durable v90 -> v91 lesson

Ryan's v90 physical test moved past the former false E890 and produced:

`BOOP DEV E893`

with detail:

`IllegalStateException: Natural speech audio output unavailable`

That result is strong evidence, not a generic voice failure. It proves:

1. BOOP's real runtime files passed preflight;
2. Sherpa/Kokoro `OfflineTts` initialized successfully;
3. synthesis returned generated audio;
4. the failure occurred in Android playback before generated PCM was written.

The precise v90 bug was the `AudioTrack` state gate. BOOP builds the generated utterance as signed PCM16 in `MODE_STATIC`. A successfully initialized Android static track can report `STATE_NO_STATIC_DATA` until sample data is written. v90 incorrectly required `STATE_INITIALIZED` **before** `write(...)`, so it could reject a healthy track as unavailable.

Permanent engineering rule for this implementation:

- a newly created `MODE_STATIC` natural-speech track must not be rejected merely because its pre-write state is `STATE_NO_STATIC_DATA`;
- reject `STATE_UNINITIALIZED` as the construction failure state;
- do not switch to streaming or redesign the playback transport unless later physical evidence actually requires it.

v91 implements only that state correction. It keeps `MODE_STATIC`, signed PCM16, the same generated samples, whole-utterance buffer/write/play flow, AudioAttributes, sample rate, session handling and fallback architecture.

Production repair commit:

`cd75e7b6e6344bf8122e30c2e4a025529be60cab`

## v91 TDD evidence

Work was isolated on `boop-natural-v91-audiotrack-stream`. The branch name records the first hypothesis, not the chosen production design.

RED:

- workflow `34432449031`, job `102730608219`;
- exactly `1 failed, 31 passed`;
- the only failure was the new static-track state-contract regression against v90 behaviour.

GREEN:

- one production condition changed at `cd75e7b6e6344bf8122e30c2e4a025529be60cab`;
- natural/wake stages were green on superseded run `34432516657` before its version-stamp push cancelled later unrelated stages;
- full isolated v91 workflow `34432642013`: SUCCESS;
- canonical promoted Unified workflow `34433115316`: SUCCESS;
- canonical Shield routing `34433115318`: SUCCESS.

No Kokoro model config, pack contents, natural voice SIDs, HA routing, wake architecture, launcher behaviour, approved eyes, package identity or signer changed.

## Natural voice identity and safety contract

Natural voices are optional local/offline voices after one in-app model download. No paid/cloud TTS API is required for this subsystem.

Pinned pack: `kokoro-multi-lang-v1_0`.

Exact voices:

1. Emma: `bf_emma`, SID 21;
2. Isabella: `bf_isabella`, SID 22;
3. George: `bm_george`, SID 26;
4. Fable: `bm_fable`, SID 25.

Mandatory selection semantics:

1. Pressing a Natural Voice row means **preview candidate**, not blindly select it.
2. Current installed pack must be verified before preview.
3. Dedicated natural synthesis/playback must actually complete.
4. Only then persist the requested speaker.
5. Only then persist `natural_runtime_proven_version` for the current pack.
6. Ordinary BOOP speech may use natural only when selection + current-pack runtime proof both match.
7. Failed preview must leave ordinary Android TTS working and must never substitute Android TTS while pretending the natural preview succeeded.

Keep Sherpa synthesis on:

`tts.generateWithConfig(text, generation)`

Never restore `generateWithConfigAndCallback(...)` with Sherpa 1.13.7; v86 physically exposed its Android JNI process-abort path.

Natural playback remains signed PCM16. Do not restore float PCM or `PlaybackParams` while basic natural playback is unresolved. Natural pitch stays deferred until basic natural speech is physically proven. Keep explicit `lexicon-gb-en.txt`; do not force `lang="eng"` without new evidence.

Pack installation remains app-private and pinned: exact HTTPS archive, size/SHA verification, streaming digest, explicit extraction progress, cooperative cancellation, traversal/link rejection, required-file validation and safe activation.

## Natural speech physical history

### v85

Natural rows appeared to work but actually spoke Android TTS. Fallback disguised Natural Voice failure.

### v86

Natural-only preview entered Sherpa callback JNI. Physical test produced no sound plus process crash/minimise after an otherwise successful HA command. `generateWithConfigAndCallback(...)` is permanently banned while using Sherpa 1.13.7.

### v87

Non-callback synthesis stopped the hard callback crash, but Natural Voice still failed and premature natural selection could leave ordinary speech mute.

### v88

Runtime-proof gating and Android fallback isolation restored normal BOOP speech. Physically accepted as usable rollback. Natural preview still not accepted.

### v89

Added photographable debug-only stage codes. Physical result: HA worked, Android BOOP spoke, Emma produced E890 / `Natural voice runtime files incomplete`.

Investigation proved v89's own preflight invented `inno/nif_model.onnx` and `inno/possible_tokens.txt`, which are not required by BOOP's pinned Kokoro v1 runtime. The downloaded pack was not proven corrupt.

### v90

Removed only the bogus `inno/...` requirements. Physical test progressed to E893 / `Natural speech audio output unavailable`, proving file preflight, model initialization and synthesis passed. This exposed the Android static-track state bug fixed in v91.

### v91

CI/build/signing verified. Physical acceptance pending.

## Durable diagnostic ladder

| Code | Meaning |
| --- | --- |
| `BOOP DEV E890` | A real required runtime file/directory is incomplete or unreadable. |
| `BOOP DEV E891` | Sherpa/Kokoro `OfflineTts` initialization or runtime metadata failed. |
| `BOOP DEV E892` | Model initialized but `generateWithConfig(...)` failed or returned no audio. |
| `BOOP DEV E893` | Synthesis returned audio but Android PCM16 playback failed. Use the short detail to distinguish track creation, write and play/output faults. |
| `BOOP DEV E899` | Unknown natural failure stage. |

DEV diagnostics remain debug-only, photographable and sanitized. They must never expose credentials, private addresses, signing material or personal data.

## Required v91 physical evidence

1. Install exact v91 over v90. Do not redownload the Natural Voice pack unless BOOP explicitly says it is absent.
2. Issue `lights off` or `lights on`; HA should act and ordinary Android BOOP speech must still work.
3. Tap Emma once.
4. If Emma speaks, tap one second Natural Voice such as George or Isabella.
5. Issue a normal BOOP/HA command and verify the reply uses the selected Natural Voice.
6. If a preview fails, record the giant E### plus short detail, then verify ordinary Android speech still works with another light command.

Do not declare Natural Voices finished from one successful preview alone. Require at least two speaker previews plus normal routed Natural Voice speech.

## Canonical rebuild gate

Ryan supplied and approved the full `BOOP CANONICAL REBUILD / FULL AUTONOMOUS GITHUB DEVELOPMENT RUN` prompt. Target architecture: **BOOP is one logical puppet; state is data; every surface renders that same puppet rather than independently implementing him.**

The prompt assumes Natural Voices are finished before the rebuild begins. Therefore:

1. physically verify v91, or continue narrowly if v91 produces a new diagnostic;
2. once Natural Voices are genuinely accepted, fetch live `boop-unified` and `main`;
3. create `boop-canonical-rebuild` from that exact physically verified voice-working head;
4. execute the approved rebuild phases there without routine approval stops;
5. inspect already-landed media-control improvements before adding more so working media code is not duplicated or redesigned.

Do not create the canonical rebuild branch early. Ryan is the only visual QA. Never regenerate BOOP's canonical eyes/blink. If visual authority is missing or uncertain during the later rebuild, request `canonical-idle-blink-v1.zip`.

## Other durable Unified contracts

- Home Assistant remains local authority; basic control must not depend on cloud chat.
- Permanent approved eye master: `unified/assets/boop-eyes/boopApprovedEyes.png`, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; never regenerate/destructively edit it.
- Eye hue remains procedural iris-only, default 190 degrees.
- Approved notification hands remain byte-locked. Android notification remains authoritative.
- Preserve one controller-owned 16 kHz microphone stream, permanent BOOP wake name, additive custom name and exact 1,600-sample / 100 ms wake-command bridge.
- Developer menu remains the accepted in-place `MainActivity` route with exact spoken trigger `developer menu`.
- Unified routing remains recovery override -> TV/Leanback Shield -> Pixel 7 Pro Wall -> other non-TV >=600dp Wall -> smaller handheld Launcher.
- Clean Nvidia Shield HOME remains separate on `boop-shield-clean-launcher` / `com.boop.shieldhome` until explicit merge approval.
- GitHub verifies non-visual functional behavior, compilation, package/signature/integrity and security. Ryan owns physical, visual and acoustic acceptance.

## Protected rollback checkpoints

Current usable rollback:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Also preserve:

- `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`;
- v58 `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- v48 `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`;
- `checkpoint-boop-unified-v65-procedural-eyes`.
