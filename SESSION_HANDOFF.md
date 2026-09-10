# BOOP unified handoff

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current usable checkpoint: v88 Android voice restored

Exact app/test release head:

`f5f086fc4f67712b5746be067aff852331299bb0`

Protected checkpoint branch:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Release identity:

- versionCode `88`;
- versionName `1.2.88-unified-natural-playback-recovery`;
- package `com.boop.alpha1`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Verification:

- canonical full workflow `34418073143`: **SUCCESS**;
- separate Shield HOME routing workflow `34418073200`: **SUCCESS**;
- artifact `BOOP-Unified`, ID `10129945925`, size `63,993,353` bytes;
- artifact ZIP SHA-256 `a3d52235115a6a1dc2a4781022cf9339843e615e7b79f85958312873cff9275e`;
- APK SHA-256 `8fca19f2005b8a252488978e0efc7f7391711207d06ac1557088648a0402e108`;
- natural/integration Python contracts `29/29`;
- Shield focused functional tests `58/58`, zero failures/errors/skips;
- Unified focused functional tests `155/155`, zero failures/errors/skips.

The exact artifact ZIP was downloaded after CI and independently checked. ZIP digest, APK digest, `built-commit.txt`, package/version and permanent signer all match the workflow receipts.

## Fresh physical evidence: v88 accepted as usable rollback, natural voice still failed

Ryan physically confirmed the exact v88 candidate is **back to usable** and explicitly approved it as a checkpoint:

- ordinary Android TextToSpeech is restored;
- BOOP speaks ordinary replies again;
- the natural voice attempt still fails;
- natural failure no longer silences the working Android voice path.

Interpretation is strict: v88 is physically accepted as the current **usable rollback baseline**, not as a successful natural-voice implementation. Natural speech remains unresolved and acoustically unaccepted.

The checkpoint is pinned to the exact release commit above rather than the later documentation head, so rollback reproduces the exact tested APK source.

v87 remains physically rejected: all four natural rows displayed `natural voice preview failed`, none produced natural speech, and ordinary BOOP speech remained mute afterward.

## v88 safety architecture that must not regress

v88 separates **trying a natural voice** from **making it BOOP's normal voice**.

Natural selection order remains mandatory:

1. A voice row identifies the candidate speaker without selecting it.
2. The downloaded/verified pack is allowed to preview.
3. Kokoro synthesizes the candidate and BOOP attempts local playback.
4. Only after playback completes successfully does BOOP persist that natural speaker and mark the current pack version as runtime-proven.
5. Until that proof exists, ordinary BOOP speech stays on Android TextToSpeech even if older preferences contain `selected_backend=natural`.

Durable controller rule:

- `naturalPackReadyForPreview()` means the current pack is installed/verified and may be tried;
- `naturalBackendSelectedAndUsable()` additionally requires `natural_runtime_proven_version` to equal the current pack version;
- `markNaturalPlaybackProven()` is written only after successful natural preview playback;
- stale pre-v88 natural selection must never take ordinary Android speech down again.

Durable preview rule:

- preview never substitutes Android TTS and pretends it was the requested natural voice;
- failure says either `Natural voice synthesis failed. Android voice kept.` or `Natural voice playback failed. Android voice kept.` when that stage is known;
- failed preview does not select/persist that natural voice;
- ordinary speech remains resilient through Android TTS.

## v88 Android Kokoro playback path

Keep Sherpa-ONNX 1.13.7 Android synthesis on:

`tts.generateWithConfig(text, generation)`

Do **not** restore `generateWithConfigAndCallback(...)`; v86 physical testing exposed that JNI callback route as process-crashing on Android.

v88 removes BOOP's float PCM + `PlaybackParams` layer from natural playback. Generated Sherpa floats are clamped/converted to signed PCM16 and played using `AudioFormat.ENCODING_PCM_16BIT`.

Natural pitch adjustment is intentionally not applied in this PCM16 recovery baseline. Speech-rate still maps to Kokoro generation speed. Restore natural pitch only after basic natural playback is physically proven.

Exact natural voices remain:

1. Emma `bf_emma`, speaker 21;
2. Isabella `bf_isabella`, speaker 22;
3. George `bm_george`, speaker 26;
4. Fable `bm_fable`, speaker 25.

The explicit `lexicon-gb-en.txt` remains. Do not restore the rejected `kokoro.setLang("eng")` guess; exact Sherpa 1.13.7 source permits blank language with the configured lexicon.

## TDD / verification lineage

- v88 RED commit `0f80d8d4acb7c8cf991fbdb1284ec01929d778f5`, workflow `34417410560`: exactly 4 new failures and 25 passes, covering selection poisoning, runtime proof, PCM16 playback and staged failure reporting;
- old `PlaybackParams` contract retired at `0a5959b630eebc587d5aa09616224827665fabee` before production change;
- controller repair `dcb31c2c5b7df74d7d78cdf7dfc8474e8a2b7c27`;
- PCM16 backend repair `f564e4333cbff5b7106314f750397c0cec652395`;
- guarded preview/selection repair `d12267dd403a8236cd92913fa8f2bd95164739f7`;
- pre-release full build `34417766406`: SUCCESS;
- final v88 release head `f5f086fc4f67712b5746be067aff852331299bb0`, workflow `34418073143`: SUCCESS;
- Shield HOME routing `34418073200`: SUCCESS.

The v88 functional diff is limited to the natural voice patch, natural backend, voice controller, two natural-voice contract files and release version metadata. Home Assistant routing, approved eyes, launcher source, package ID and signer were not changed.

## Next natural-voice repair

The next attempt must start from the live `boop-unified` lineage after a fresh branch/main check while preserving `checkpoint-boop-unified-v88-android-voice-restored` unchanged.

Follow systematic root-cause debugging before changing production code. v88 proves the Android fallback isolation works; do not disturb it while investigating why dedicated Kokoro preview still fails.

Ryan also wants the next repair to include the new prompt/plan developed with the other BOOP assistant. The exact new prompt is not currently present in this handoff. Do not invent it. Once supplied or otherwise available in repository/conversation context, combine its requirements with the natural-voice root-cause pass before implementation.

## Protected contracts unchanged

- Natural voice pack download/install remains the v85 verified app-private flow with streaming SHA, explicit extraction progress and cooperative cancellation.
- Permanent approved eye master remains locked at SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; do not regenerate or destructively edit it.
- User eye hue remains procedural-iris-only; default 190 degrees.
- Exact approved notification hands remain byte-locked.
- Preserve one 16 kHz microphone owner, accepted wake/name architecture and exact 100 ms wake bridge.
- Generic tablet routing remains TV -> Shield, Pixel 7 Pro -> Wall, non-TV >=600dp -> Wall, smaller handheld -> Launcher, after explicit recovery override.
- Clean Nvidia Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a merge.
- GitHub performs non-visual functional/build/signing verification only. Ryan owns physical, visual and acoustic acceptance.

## Physically accepted rollback state

Current usable rollback:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Meaning: Android BOOP speech is restored and physically usable; natural voice remains failed/unaccepted.

Also preserve:

- `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`;
- v58 `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- v48 `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`;
- `checkpoint-boop-unified-v65-procedural-eyes`.
