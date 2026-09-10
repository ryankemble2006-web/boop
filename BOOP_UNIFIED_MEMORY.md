# BOOP unified memory

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Fresh `main` owns shared product contracts. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current physically usable checkpoint: v88 Android voice restored

Exact app/test release head:

`f5f086fc4f67712b5746be067aff852331299bb0`

Protected checkpoint branch:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Release identity and receipts:

- versionCode `88`;
- versionName `1.2.88-unified-natural-playback-recovery`;
- full workflow `34418073143`: SUCCESS;
- Shield HOME routing `34418073200`: SUCCESS;
- artifact `BOOP-Unified`, ID `10129945925`, size `63,993,353` bytes;
- artifact ZIP SHA-256 `a3d52235115a6a1dc2a4781022cf9339843e615e7b79f85958312873cff9275e`;
- APK SHA-256 `8fca19f2005b8a252488978e0efc7f7391711207d06ac1557088648a0402e108`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- natural/integration Python contracts `29/29`;
- Shield focused tests `58/58`;
- Unified focused tests `155/155`;
- zero failures/errors/skips in the focused Java suites.

The exact release artifact was downloaded and independently matched its GitHub ZIP digest, APK digest, release commit, package/version and signer receipts.

Ryan physically confirmed this exact v88 build is back to a **usable BOOP baseline** and explicitly approved it as a checkpoint. Android TextToSpeech is restored and ordinary BOOP replies speak again. The natural voice attempt still fails. Therefore the checkpoint means **usable Android speech restored**, not **natural speech fixed**.

The v88 checkpoint is pinned to the exact tested release commit, not later documentation commits. Do not repoint it.

## Durable natural voice identity

Natural voices remain optional, local/offline after one in-app model download and use no paid/cloud TTS API.

Pinned Sherpa-ONNX Kokoro pack: `kokoro-multi-lang-v1_0`.

Exact voice order:

1. Emma: key `bf_emma`, speaker 21;
2. Isabella: key `bf_isabella`, speaker 22;
3. George: key `bm_george`, speaker 26;
4. Fable: key `bm_fable`, speaker 25.

The pack remains downloaded from the pinned HTTPS Sherpa release, verified by pinned archive size/SHA, extracted into app-private storage and reused offline. Preserve the v85 streaming-hash / visible extraction / cooperative cancellation / traversal and link rejection / required-file validation / safe activation flow.

## Physical history that must not be forgotten

### v85

Natural voice rows appeared to work but actually spoke the selected Android TTS voice. Generic speech fallback hid natural synthesis failure.

### v86

The natural-only selector/preview path finally entered Sherpa's native backend. On hardware this exposed a process-crashing Android JNI callback route. Ryan reported no sound and crash/minimise. `lights on` still completed Home Assistant control, then BOOP minimised and gave no acknowledgement when speech started.

Do not restore Sherpa-ONNX 1.13.7 Android `generateWithConfigAndCallback(...)`. Use `tts.generateWithConfig(text, generation)` instead.

### v87

Removing the JNI callback stopped the hard-crash route, but Ryan physically reported `natural voice preview failed` for all four natural voices and ordinary BOOP speech was still mute.

The second architecture defect was selection poisoning: pressing a natural row persisted `selected_backend=natural` before that natural backend had ever proven it could synthesize and play on the device. Therefore an experimental broken mouth could take over ordinary BOOP speech.

### v88

v88 physically restored the Android voice path. Natural voice still failed, but that failure no longer poisoned ordinary BOOP speech. Ryan explicitly accepted v88 as the new usable rollback checkpoint.

This physical result validates the **isolation architecture**, not Kokoro synthesis/playback itself.

## Durable v88 natural voice safety contract

A natural voice does **not** become BOOP's normal mouth merely because its button was pressed.

Selection order is mandatory:

1. Resolve the candidate voice/SID without selecting it.
2. Require the installed current pack to be verified and ready for preview.
3. Attempt dedicated natural synthesis/playback.
4. Only after successful completed playback persist that natural speaker.
5. Only after successful completed playback mark the current natural pack version as runtime-proven.
6. Ordinary BOOP speech may route through natural only when the natural backend is selected **and** the current pack version equals the stored runtime-proven version.

Controller concepts:

- `naturalPackReadyForPreview()` = current installed/verified pack may be tried;
- `markNaturalPlaybackProven()` = persist `natural_runtime_proven_version` after successful preview playback only;
- `naturalBackendSelectedAndUsable()` = selected natural + preview-ready pack + matching runtime-proven version.

A failed preview must never persist/select that candidate and must never poison later ordinary speech. Preview itself must not substitute Android TTS and impersonate the requested natural voice. Ordinary speech retains Android TTS resilience.

Where possible preview failure must identify the stage:

- `Natural voice synthesis failed. Android voice kept.`
- `Natural voice playback failed. Android voice kept.`

## Durable v88 natural Android playback path

Sherpa synthesis:

`tts.generateWithConfig(text, generation)`

Never restore `generateWithConfigAndCallback(...)` while BOOP ships Sherpa-ONNX 1.13.7 on Android.

Natural playback follows a conservative PCM16 path:

- clamp generated float samples to [-1, 1];
- convert to signed 16-bit PCM;
- `AudioFormat.ENCODING_PCM_16BIT`;
- mono `AudioTrack`;
- static playback buffer;
- no `AudioFormat.ENCODING_PCM_FLOAT`;
- no natural `PlaybackParams` pitch manipulation in this recovery baseline.

Speech rate still maps to Kokoro generation speed. Natural pitch is deliberately deferred until basic natural playback is physically proven.

Keep the explicit `lexicon-gb-en.txt`. Do not restore `kokoro.setLang("eng")`. Exact Sherpa 1.13.7 source permits blank language when a lexicon is configured.

## v88 TDD lineage

- RED `0f80d8d4acb7c8cf991fbdb1284ec01929d778f5`, workflow `34417410560`: exactly 4 expected failures and 25 passes;
- legacy `PlaybackParams` contract retired before production at `0a5959b630eebc587d5aa09616224827665fabee`;
- runtime proof/controller change `dcb31c2c5b7df74d7d78cdf7dfc8474e8a2b7c27`;
- PCM16 backend `f564e4333cbff5b7106314f750397c0cec652395`;
- guarded candidate-preview/selection `d12267dd403a8236cd92913fa8f2bd95164739f7`;
- full pre-release workflow `34417766406`: SUCCESS;
- final release `f5f086fc4f67712b5746be067aff852331299bb0`, workflow `34418073143`: SUCCESS;
- Shield routing `34418073200`: SUCCESS.

The functional v88 diff is restricted to natural voice patch/backend/controller, two natural voice contracts and release metadata. Do not attribute Home Assistant, approved eyes, launcher, package or signer changes to v88.

## Next natural voice repair boundary

Start from the current live `boop-unified` lineage only after re-fetching the branch and `main`; preserve `checkpoint-boop-unified-v88-android-voice-restored` exactly.

Use systematic root-cause debugging before another production tweak. The v88 physical evidence proves the Android fallback isolation is valuable and must remain untouched while Kokoro is investigated.

Ryan wants the next natural-voice repair to also incorporate a new prompt/plan developed with the other BOOP assistant. The exact new prompt is not stored here yet. Do not invent it or substitute the older four-voice plan. Once the exact prompt becomes available in conversation/repository context, merge those requirements into the next root-cause pass before implementation.

## Other durable Unified contracts remain unchanged

- Home Assistant remains the local authority and basic local control must not depend on cloud chat.
- Permanent approved eye master remains `unified/assets/boop-eyes/boopApprovedEyes.png`, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; never regenerate/destructively edit it.
- Eye hue remains procedural iris-only; default 190 degrees. Preserve reading-eyes -> v64 sclera -> v65 feathering order.
- Approved five-digit notification hands remain byte-locked. Android's original notification remains authoritative.
- Preserve one controller-owned 16 kHz microphone stream, BOOP permanent wake name, additive custom wake name and exact 1,600-sample / 100 ms wake-to-command bridge.
- Developer menu remains the accepted in-place `MainActivity` route; do not restore the rejected activity-hop design without new evidence.
- Unified routing remains: explicit recovery override first, TV/Leanback -> Shield, Pixel 7 Pro -> Wall, other non-TV >=600dp -> Wall, smaller handheld -> Launcher.
- Clean Nvidia Shield HOME remains separate on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a merge.
- GitHub checks compilation, non-visual functional behavior, package/signature/integrity and security only. Ryan owns visual/device/acoustic acceptance.

## Protected rollback checkpoints

Current usable rollback:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Meaning: Android BOOP voice is physically restored and usable; natural voice remains failed/unaccepted.

Also preserve:

- `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`;
- v58 `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- v48 `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`;
- `checkpoint-boop-unified-v65-procedural-eyes`.
