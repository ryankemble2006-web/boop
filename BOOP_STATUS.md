# BOOP unified status

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current physical rollback: v88 Android voice restored

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Ryan physically confirmed exact v88 is usable: normal Android TextToSpeech replies work and failed Natural Voice experiments no longer silence ordinary BOOP speech. Natural speech itself is not accepted. Never repoint this checkpoint or weaken the runtime-proof fallback gate.

## Current signed test candidate: v90

Exact source/build head:

`97688d1606e7ac1665a8626a00fed9111f062135`

Identity and receipts:

- versionCode `90`;
- versionName `1.2.90-unified-kokoro-preflight-fix`;
- package `com.boop.alpha1`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- full Unified workflow `34426014943`: **SUCCESS**;
- Shield HOME routing workflow `34426014914`: **SUCCESS**;
- artifact ID `10132764611`;
- artifact ZIP SHA-256 `e9cda859f89198fdc9cbc87b56c9c4322ec4d6d1fa51f2e04e6f8f6fb99544ae`;
- APK SHA-256 `94b63c350d50029d875e40bd4e18b839addad020155de231c918de8bbc3e8275`;
- Shield tests `58/58`;
- Unified tests `157/157`;
- package/version/entry activity/signer/ZIP checks: SUCCESS.

Downloaded artifact was independently verified after CI. v90 is **not physically accepted yet** and Natural Voices are **not yet finished**.

## v89 physical result

Ryan tested v89 and reported:

- `lights off` executed and Android BOOP spoke normally;
- Natural Voice preview showed `BOOP DEV E890`;
- detail: `IllegalStateException: Natural voice runtime files incomplete`.

This proved two things:

1. v88 Android TTS isolation still works on-device.
2. Natural speech was blocked before `OfflineTts` construction.

## E890 root cause

v89's diagnostic preflight accidentally required two files that are not part of BOOP's pinned Kokoro v1 runtime manifest:

- `inno/nif_model.onnx`;
- `inno/possible_tokens.txt`.

The valid BOOP/Sherpa runtime inputs are:

- `model.onnx`;
- `voices.bin`;
- `tokens.txt`;
- `lexicon-gb-en.txt`;
- readable non-empty `espeak-ng-data/`.

Therefore v89's E890 was a false preflight failure caused by BOOP's diagnostic, not evidence that Ryan's downloaded pack was corrupt.

## v90 TDD proof

Work was isolated on `boop-natural-v90-runtime-files` before promotion.

RED:

- workflow `34425786847`, job `102710598737`;
- exactly `1 failed, 9 passed`;
- the failure named only the two invented `inno/...` paths.

Minimal fix:

- production commit `86970a634565b7ed9a3efcaa7fd8433dc61af797`;
- removed only the two bogus runtime requirements.

GREEN:

- focused workflow `34425864351`: SUCCESS;
- canonical v90 workflow `34426014943`: SUCCESS;
- new `BoopNaturalVoiceRuntimeFilesTest` verifies the official runtime-file shape is accepted and a genuinely missing required input is still rejected.

Net v90 change from v89 is limited to:

- `source/BoopNaturalSpeechBackend.java`;
- `source-test/BoopNaturalVoiceRuntimeFilesTest.java`;
- `tests/test_unified_v85_natural_voice_install_flow.py`;
- `unified/app-build.gradle`.

No HA routing, wake architecture, approved eyes, launcher behavior, natural SIDs, AudioTrack strategy, package ID or signer changed.

## Physical test required next

1. Install exact v90 over v89. Do not redownload the Natural Voice pack unless BOOP explicitly says it is absent.
2. Say `lights off` or `lights on`; HA should act and Android BOOP should speak.
3. Tap **Emma once**.
4. Report the giant `BOOP DEV E###` code plus its short detail, or report that Emma spoke.
5. Issue another light command; Android BOOP must still speak if natural preview failed.

Expected interpretation:

- `E890`: a real required file/directory is inaccessible; identify that exact item.
- `E891`: preflight passed and Sherpa/Kokoro initialization failed.
- `E892`: initialization passed and generation failed.
- `E893`: synthesis produced audio and PCM16 playback failed.
- Emma speaks: test another speaker and then normal natural routing before accepting Natural Voices.

## Protected Natural Voice rules

- Pack `kokoro-multi-lang-v1_0`.
- Emma `bf_emma`/21; Isabella `bf_isabella`/22; Fable `bm_fable`/25; George `bm_george`/26.
- Preview is not selection; persist natural only after successful requested natural playback.
- Failed preview must leave Android TTS available.
- Keep `tts.generateWithConfig(text, generation)` and never restore Sherpa 1.13.7 `generateWithConfigAndCallback(...)` after the v86 JNI-abort evidence.
- Natural playback remains signed PCM16 without `PlaybackParams`; natural pitch remains deferred while this fault is unresolved.
- Keep explicit `lexicon-gb-en.txt`; do not force `lang="eng"` without new evidence.
- App-private pinned pack download/install safeguards remain intact.

## Canonical rebuild gate

The approved `BOOP CANONICAL REBUILD` prompt is queued but its Phase 1 assumes Natural Voices are finished. They are not physically proven yet. Do not create `boop-canonical-rebuild` until Natural Voices genuinely work and are physically verified. Then create it from that exact verified `boop-unified` head and execute the supplied phases there.

## Other protected contracts

- Approved eye master remains byte-locked at SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; never regenerate it.
- Approved notification hands remain byte-locked.
- Preserve the single 16 kHz microphone owner and exact 100 ms wake bridge.
- Unified device routing remains unchanged.
- Clean Shield HOME remains standalone until explicit merge approval.
- Ryan owns visual/device/acoustic acceptance; CI performs non-visual verification only.

## Historical rollbacks

Also preserve v59, v58, v48 and the v65 procedural-eyes checkpoint. v88 remains the current usable rollback.
