# BOOP v70 Natural Voices Design

Date: 2026-09-09

## Baseline and isolation

This work starts from exact phone v70 app head `c68a7aba8f0c9bcffa81ad0b517453cc8e50b12d` and lives on branch `boop-v70-natural-voices`.

The live `boop-unified` branch has already advanced into Android-tablet routing work at `bd878606809302de1b871e6c62d8ce905346e766`. This natural-voice branch must not consume, alter, merge, reset, or force-update that tablet work.

Package remains `com.boop.alpha1`. Permanent signer remains unchanged. No automatic install or permission grant is part of this work.

## Product goal

Add the previously selected BOOP natural voice pack as an optional local/offline voice layer while preserving the existing free Android `TextToSpeech` path as the installed fallback.

The user-facing voice set is exactly four British English voices:

- Female: `bf_emma`
- Female: `bf_isabella`
- Male: `bm_george`
- Male: `bm_fable`

These are existing Kokoro speaker identities. The app does not rename their gender or accent heuristically.

## User experience

Voice Settings keeps the existing pitch and speech-rate controls and gains one simple **Natural voices** area.

Before the pack is installed:

- show one clear **Download natural voices** button;
- no browser, file picker, Downloads app, or external manual hunting;
- the app performs the download itself;
- progress is shown in plain English;
- the downloaded pack is verified before it becomes usable;
- a failed or interrupted download leaves Android TTS working exactly as before.

After the pack is installed:

- show the four fixed voice choices by friendly name and gender: Emma, Isabella, George, Fable;
- tapping a voice selects it and BOOP remembers the choice;
- provide a short preview action for each voice using BOOP's own speech pipeline;
- existing pitch/rate controls continue to affect BOOP speech. Where the natural engine's speed control differs from Android TTS, map the existing speech-rate value into the supported natural-speed range rather than adding a second speed control;
- the existing spoken `change voice` behavior cycles through the four natural voices when the natural pack is active;
- if the pack is absent or unusable, `change voice` continues to use the current Android offline voice behavior.

There is no API key, account, subscription, cloud synthesis, browser download, or external TTS service.

## Natural voice model

Use Sherpa-ONNX's offline Kokoro support because v70 already carries Sherpa-ONNX for the wake system, avoiding a second native inference stack.

Use an English Kokoro package compatible with the Sherpa-ONNX version already materialized by v70. The pack must contain the required Kokoro model, voices data, tokens and espeak-ng data. Only the four approved British speakers are exposed in BOOP UI even if the underlying pack contains additional speaker IDs.

The app stores the pack in app-private storage. Model payloads are not committed to this public repository or bundled into the APK.

A small checked-in manifest defines:

- model-pack version;
- download URL(s) from the upstream project release;
- expected archive SHA-256;
- expected extracted required files;
- speaker ID mapping for `bf_emma`, `bf_isabella`, `bm_fable`, `bm_george`;
- minimum free-space requirement.

The runtime must verify the archive checksum before activation, extract into a temporary app-private directory, validate the expected files, then atomically promote that directory to the active pack. A partial or mismatched pack is deleted or quarantined and never selected for speech.

## Runtime architecture

Keep `BoopVoiceController` as the product-level voice owner. Split synthesis behind a small internal interface so the rest of MainActivity does not learn about model files.

Two backends:

1. **Android backend**: wraps the current `TextToSpeech` behavior unchanged and remains the fallback.
2. **Natural backend**: loads Sherpa-ONNX Kokoro from the verified app-private pack and generates PCM locally.

`MainActivity.speak()` continues to be the single speech entry point. It asks `BoopVoiceController` to synthesize through the selected backend. Natural speech is played through one BOOP-owned audio path and reports completion/error back through the same `finishTtsUtterance()` boundary used today, so wake re-arm and assistant-follow-up timing stay intact.

Do not create a second microphone owner. Natural TTS must never touch wake recognition, ASR ownership, the exact 100 ms wake bridge, or the proven TTS-follow-up handoff semantics.

If natural synthesis fails at runtime, BOOP speaks the same text through Android TTS and records the natural failure for diagnostics without breaking the conversation flow.

## Download subsystem

Add an app-private download manager dedicated to the natural voice pack.

Requirements:

- HTTPS only;
- one explicit user-triggered download;
- resumable when practical, otherwise clean restart;
- foreground UI progress while Voice Settings is open;
- cancellation allowed;
- archive SHA-256 verification before extraction;
- extraction path traversal protection;
- free-space check before download/extraction;
- atomic activation after validation;
- no storage permission;
- no shared Downloads directory;
- no browser/file picker;
- no silent background auto-download.

Deleting/replacing the pack later is outside this first implementation unless required to recover a corrupt pack.

## Persistence

Extend the existing `boop_voice` preferences with:

- selected backend (`android` or `natural`);
- selected natural speaker ID/name;
- installed natural-pack version;
- last natural-pack verification state.

Do not migrate or overwrite the existing Android `voice_name`, pitch, or speech-rate keys. Installing the natural pack does not automatically change BOOP's current voice until the user selects one.

## Failure and fallback behavior

- No pack: Android TTS works exactly as v70.
- Download interrupted: keep Android TTS; show Download again/Resume as supported.
- Hash mismatch: reject pack, show plain-English `Download didn't verify. Try again.`
- Extraction/space failure: reject pack and preserve Android TTS.
- Natural model load/synthesis failure: fall back to Android TTS for that utterance and keep the app usable.
- Unsupported device ABI/native failure: Natural voices show unavailable, Android TTS remains usable.

No failure is allowed to wedge BOOP's wake state or leave `onTtsStarting()` unmatched by `onTtsFinished()`.

## Security and repository safety

The repository is public.

- Do not commit model binaries, downloaded archives, private URLs, tokens, signing material or device paths.
- Upstream voice pack URLs must be public release URLs.
- Hashes and speaker mappings are safe to commit.
- Extraction must reject absolute paths and `..` traversal.
- Model files stay under app-private storage.

## Tests and verification

Use TDD.

Non-visual contract/unit tests should cover:

- exact four approved voice IDs and labels;
- natural pack absent -> Android fallback;
- selected natural voice persistence;
- `change voice` cycles exactly the four natural voices when active;
- download state machine;
- SHA-256 pass/fail;
- archive path traversal rejection;
- incomplete pack rejection;
- atomic activation behavior;
- natural synthesis success -> exactly one completion callback;
- natural synthesis failure -> Android fallback and exactly one completion callback;
- wake/TTS handoff contract remains balanced;
- no new microphone owner;
- no storage permission/browser/file-picker flow introduced;
- package and permanent signer unchanged.

CI must not judge voice quality, accent, naturalness, latency, volume, or UI appearance. Ryan owns physical/acoustic acceptance on the Pixel.

## Physical acceptance

After CI/signer-green APK exists, Ryan checks on the phone:

1. v70 behavior still works before downloading anything;
2. Voice Settings shows one in-app natural-voice download action;
3. download completes without browser/file-picker detours;
4. all four voices appear: Emma, Isabella, George, Fable;
5. each voice previews and can be selected;
6. selected voice persists after restart;
7. ordinary local replies and chat replies use the selected natural voice;
8. wake -> command -> reply -> wake re-arm still behaves normally;
9. pitch/rate controls remain useful;
10. disabling/removing access to the natural pack or forcing a synthesis failure leaves Android TTS usable.

CI/signer green is not physical/acoustic green. Do not create or repoint an accepted rollback checkpoint until Ryan explicitly accepts the build.

## Out of scope

- tablet routing work currently underway on `boop-unified`;
- OpenAI or other cloud TTS APIs;
- voice cloning;
- user-imported models;
- arbitrary voice marketplace/catalogue;
- extra languages;
- automatic model updates;
- background downloads without a user press;
- redesigning BOOP eyes, animations, notification doods, launcher, Shield HOME, HA behavior or wake-word recognition.
