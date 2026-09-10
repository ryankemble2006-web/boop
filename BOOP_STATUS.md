# BOOP unified status

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current usable checkpoint: v88 Android voice restored

Exact app/test release head:

`f5f086fc4f67712b5746be067aff852331299bb0`

Protected checkpoint branch:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Release:

- versionCode `88`;
- versionName `1.2.88-unified-natural-playback-recovery`;
- full workflow `34418073143`: **SUCCESS**;
- Shield HOME routing `34418073200`: **SUCCESS**;
- artifact `BOOP-Unified`, ID `10129945925`, size `63,993,353` bytes;
- artifact ZIP SHA-256 `a3d52235115a6a1dc2a4781022cf9339843e615e7b79f85958312873cff9275e`;
- APK SHA-256 `8fca19f2005b8a252488978e0efc7f7391711207d06ac1557088648a0402e108`;
- signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- natural/integration Python contracts `29/29`;
- Shield focused tests `58/58`, zero failures/errors/skips;
- Unified focused tests `155/155`, zero failures/errors/skips.

The exact artifact was downloaded and independently matched the CI ZIP digest, APK digest, release commit, package, version and permanent signer.

## Fresh physical result

Ryan physically confirmed v88 is back to a **usable BOOP baseline** and explicitly approved it as a checkpoint:

- ordinary Android TextToSpeech is restored;
- BOOP is speaking ordinary replies again;
- the natural voice attempt still fails;
- natural failure no longer takes ordinary Android speech down with it.

This is a physical acceptance of v88 as the current usable rollback point, **not** acceptance that natural voices work. The natural voice subsystem remains unresolved and must stay isolated behind the v88 runtime-proof gate.

v87 remains physically rejected for voice output: all four natural buttons showed `natural voice preview failed`, none spoke, and ordinary BOOP speech remained mute afterward.

## v88 behavior that must not regress

v88 makes natural speech opt-in by successful playback rather than by button press:

- tapping a natural row previews a candidate without first selecting it;
- only a successful completed natural preview persists that speaker;
- the current pack version is marked runtime-proven only after successful playback;
- ordinary BOOP speech uses natural only when the selected backend is natural **and** the current pack version has runtime playback proof;
- stale pre-v88 natural selections cannot own ordinary speech;
- failed preview keeps Android voice active and does not poison future ordinary speech;
- preview failure distinguishes `synthesis failed` from `playback failed` where possible;
- preview itself never substitutes Android TTS and pretends it was the requested natural voice.

Natural playback converts Sherpa float output to signed PCM16 and uses `AudioFormat.ENCODING_PCM_16BIT`. The v87 float PCM + `PlaybackParams` layer is removed. Natural pitch is temporarily not applied; Kokoro speech-rate control remains.

Sherpa-ONNX 1.13.7 synthesis remains `tts.generateWithConfig(text, generation)`. Do not restore `generateWithConfigAndCallback(...)` because v86 hardware testing exposed that JNI callback path as process-crashing.

Exact voices remain Emma `bf_emma`/21, Isabella `bf_isabella`/22, George `bm_george`/26, Fable `bm_fable`/25. Keep `lexicon-gb-en.txt`; do not restore `kokoro.setLang("eng")`.

## TDD receipt

- RED `0f80d8d4acb7c8cf991fbdb1284ec01929d778f5`, workflow `34417410560`: exactly 4 expected failures, 25 passes;
- legacy natural `PlaybackParams` test contract retired before production at `0a5959b630eebc587d5aa09616224827665fabee`;
- controller repair `dcb31c2c5b7df74d7d78cdf7dfc8474e8a2b7c27`;
- PCM16 backend `f564e4333cbff5b7106314f750397c0cec652395`;
- guarded preview/selection `d12267dd403a8236cd92913fa8f2bd95164739f7`;
- pre-release workflow `34417766406`: SUCCESS;
- final release `f5f086fc4f67712b5746be067aff852331299bb0`, workflow `34418073143`: SUCCESS.

## Next natural-voice repair boundary

Start from the live `boop-unified` lineage after re-fetching it and `main`, while preserving the exact v88 checkpoint above. The next attempt must follow systematic root-cause investigation before another production fix. Do not touch the Android TTS fallback or weaken the v88 runtime-proof isolation.

Ryan also wants the next natural-voice repair to incorporate the new prompt/plan he developed with the other BOOP assistant. Do not guess or paraphrase an unavailable new prompt: merge its exact requirements into the next repair once that prompt is present in the conversation/repository context.

## Preserved contracts

- Natural pack install/download keeps v85 streaming-hash, visible extraction, app-private validation and cooperative cancellation.
- Permanent eye master remains byte-locked at SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; iris hue remains procedural only, default 190 degrees.
- Approved notification hands remain byte-locked.
- Preserve one 16 kHz microphone owner and exact 100 ms wake bridge.
- Unified routing remains explicit override -> TV Shield -> Pixel 7 Pro Wall -> non-TV >=600dp Wall -> smaller handheld Launcher.
- Clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a merge.
- GitHub performs non-visual functional/build/signing checks only. Ryan owns visual, device and acoustic acceptance.

## Physically accepted rollback points

Current usable rollback:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

This checkpoint means Android BOOP speech is physically usable again while natural voice remains failed/unaccepted.

Also preserve:

- `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`;
- v58 `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- v48 `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`;
- `checkpoint-boop-unified-v65-procedural-eyes`.
