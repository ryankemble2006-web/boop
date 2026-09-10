# BOOP unified memory

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Fresh `main` owns shared product contracts. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current physically usable checkpoint: v88

Protected checkpoint:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Ryan physically accepted this exact v88 release as the usable rollback because ordinary Android TextToSpeech speaks again and a failed Natural Voice attempt no longer poisons ordinary BOOP speech. This does **not** mean Kokoro works. Never repoint the checkpoint and do not weaken the v88 runtime-proof isolation.

## Current signed but unaccepted candidate: v90

Exact app/test build head:

`97688d1606e7ac1665a8626a00fed9111f062135`

Release:

- versionCode `90`;
- versionName `1.2.90-unified-kokoro-preflight-fix`;
- canonical workflow `34426014943`: SUCCESS;
- Shield HOME routing `34426014914`: SUCCESS;
- artifact ID `10132764611`;
- artifact ZIP SHA-256 `e9cda859f89198fdc9cbc87b56c9c4322ec4d6d1fa51f2e04e6f8f6fb99544ae`;
- APK SHA-256 `94b63c350d50029d875e40bd4e18b839addad020155de231c918de8bbc3e8275`;
- signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused tests `58/58`;
- Unified focused tests `157/157`;
- package/version/entry activity/signer/archive verification green.

The exact artifact was downloaded and independently verified against its receipts. v90 is awaiting physical Natural Voice evidence.

## Natural voice identity and safety contract

Natural voices are optional local/offline voices after one in-app model download. No paid/cloud TTS API is used.

Pinned pack: `kokoro-multi-lang-v1_0`.

Exact voices:

1. Emma: `bf_emma`, SID 21;
2. Isabella: `bf_isabella`, SID 22;
3. George: `bm_george`, SID 26;
4. Fable: `bm_fable`, SID 25.

Mandatory selection semantics:

1. Pressing a natural row means **preview candidate**, not select it.
2. Current installed pack must be verified before preview.
3. Dedicated natural synthesis/playback must actually complete.
4. Only then persist the requested speaker.
5. Only then persist `natural_runtime_proven_version` for the current pack.
6. Ordinary BOOP speech may use natural only when selection + current-pack runtime proof both match.
7. Failed preview must leave ordinary Android TTS working and must never substitute Android TTS while pretending the natural preview succeeded.

Keep Sherpa synthesis on:

`tts.generateWithConfig(text, generation)`

Never restore `generateWithConfigAndCallback(...)` with Sherpa 1.13.7; v86 physically exposed its Android JNI process-abort path.

Natural playback remains conservative signed PCM16. Do not restore float PCM or `PlaybackParams` while basic natural playback remains unresolved. Natural pitch stays deferred; Android TTS pitch remains available. Keep explicit `lexicon-gb-en.txt`; do not force `lang="eng"` without new evidence.

Pack installation remains app-private and pinned: exact HTTPS archive, size/SHA verification, streaming digest, explicit extraction progress, cooperative cancellation, traversal/link rejection, required-file validation and safe activation.

## Natural speech physical history

### v85

Rows appeared to work but actually spoke Android TTS. Fallback disguised Natural Voice failure.

### v86

Natural-only preview entered Sherpa callback JNI. Ryan reported no sound plus crash/minimise after a successful HA command. Root cause: Sherpa 1.13.7 Android callback bridge can abort the process. Callback generation is permanently banned for this version.

### v87

Non-callback synthesis stopped the hard callback crash, but all four natural previews failed. Persisting a natural selection before successful playback also left ordinary speech mute.

### v88

Runtime-proof gating and Android fallback isolation restored normal BOOP speech. Ryan physically accepted v88 as the usable rollback. Natural preview still failed.

### v89

v89 introduced photographable debug-only stage codes to locate the remaining natural failure. Ryan physically reported:

- `lights off` succeeded;
- Android voice replied normally;
- Emma preview -> `BOOP DEV E890`;
- detail -> `IllegalStateException: Natural voice runtime files incomplete`.

This proved Android fallback still worked and the Natural Voice path failed **before OfflineTts initialization**.

### v90 root cause and repair

v89's new preflight itself was wrong. It required two invented files:

- `inno/nif_model.onnx`;
- `inno/possible_tokens.txt`.

Those are not part of BOOP's pinned `natural-voices/manifest.json` requirements for the official Kokoro v1 runtime path. The valid preflight inputs are:

- `model.onnx`;
- `voices.bin`;
- `tokens.txt`;
- `lexicon-gb-en.txt`;
- readable, non-empty `espeak-ng-data/`.

Therefore the v89 E890 was a **false BOOP diagnostic failure**, not evidence that Ryan's downloaded model pack was corrupt.

TDD proof:

- isolated branch `boop-natural-v90-runtime-files` from then-live `boop-unified@beef30882c617cf1cffddd893e3ef4022813c216`;
- RED workflow `34425786847`, job `102710598737`: exactly `1 failed, 9 passed`; the failure listed only the two bogus `inno/...` extras;
- minimal production fix `86970a634565b7ed9a3efcaa7fd8433dc61af797` removed only those two requirements;
- focused GREEN workflow `34425864351`: SUCCESS;
- `BoopNaturalVoiceRuntimeFilesTest` now verifies the real official runtime-file shape passes preflight and a genuinely missing required input still fails;
- full v90 workflow `34426014943`: SUCCESS.

Do not redownload the pack just because v89 showed E890. First test v90 against the existing installed verified pack.

## Durable diagnostic ladder

v90 retains the v89 debug-only full-screen codes:

| Code | Meaning |
| --- | --- |
| `BOOP DEV E890` | A real required runtime file/directory is incomplete or unreadable. |
| `BOOP DEV E891` | Sherpa/Kokoro `OfflineTts` initialization or runtime metadata failed. |
| `BOOP DEV E892` | Model initialized but `generateWithConfig(...)` failed or returned no audio. |
| `BOOP DEV E893` | Synthesis returned audio but Android PCM16 playback failed. |
| `BOOP DEV E899` | Unknown natural failure stage. |

DEV diagnostics remain debug-only, photographable and sanitized. They must never expose credentials, private addresses, signing material or personal data.

## Required next physical evidence

1. Install exact v90 over v89; do not redownload the voice pack unless BOOP explicitly says it is absent.
2. Say `lights off` or `lights on`; HA should act and Android BOOP should speak.
3. Tap Emma once.
4. Record the new `E###` + short detail, or report if Emma speaks.
5. Issue another light command; Android BOOP must still speak if natural preview failed.

Engineering branches based on that evidence:

- E890 again -> identify the exact real file/directory that is unreadable.
- E891 -> focus only on Sherpa native/model initialization/config/path/runtime metadata.
- E892 -> focus only on Kokoro generation/frontend/text/speaker input.
- E893 -> focus only on Android PCM16 playback.
- Emma speaks -> test another speaker plus normal Natural Voice routing before calling the subsystem finished.

## Canonical rebuild gate

Ryan supplied and approved the full `BOOP CANONICAL REBUILD / FULL AUTONOMOUS GITHUB DEVELOPMENT RUN` prompt. Target architecture: **BOOP is one logical puppet; state is data; every surface renders that same puppet rather than independently implementing him.**

The prompt explicitly assumes Natural Voices are finished before the rebuild branch begins. They are not yet physically proven. Therefore:

1. finish Natural Voices on `boop-unified` while protecting v88;
2. physically verify natural speech;
3. fetch live `boop-unified` and `main`;
4. create `boop-canonical-rebuild` from that exact verified voice-working head;
5. execute the approved phases there without routine approval stops.

Do not create the canonical rebuild branch early. Ryan is the only visual QA. Never regenerate BOOP's canonical eyes/blink. If visual authority is missing or uncertain during the later rebuild, request `canonical-idle-blink-v1.zip`.

## Other durable Unified contracts

- Home Assistant remains local authority; basic control must not depend on cloud chat.
- Permanent approved eye master: `unified/assets/boop-eyes/boopApprovedEyes.png`, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; never regenerate/destructively edit it.
- Eye hue remains procedural iris-only, default 190 degrees.
- Approved five-digit notification hands remain byte-locked. Android notification remains authoritative.
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
