# BOOP v70 Natural Voices Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an optional, one-tap-download, fully local Kokoro natural-voice layer to the exact phone v70 BOOP baseline while preserving Android TextToSpeech as the unchanged fallback and preserving BOOP's wake/TTS handoff.

**Architecture:** `BoopVoiceController` remains the product-level voice owner and persists backend/speaker selection. A small `BoopSpeechBackend` contract separates Android TTS from the new local Sherpa Kokoro synthesis path. `BoopNaturalVoicePack` owns app-private download verification/extraction/atomic activation, while `BoopNaturalSpeechBackend` owns lazy Sherpa loading, synthesis, and local PCM playback. `MainActivity.speak()` remains the single speech entry point and the existing `finishTtsUtterance()` remains the only completion boundary. Voice Settings receives a focused Natural voices section through a deterministic materialization patch after the existing v70 scroll repair.

**Tech Stack:** Android Java 17, Sherpa-ONNX 1.13.7 already bundled in BOOP, OkHttp 4.12.0 already present, Apache Commons Compress 1.28.0 for tar.bz2 extraction, JUnit 4.13.2, pytest contract tests, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-09-natural-voices-v70-design.md`

## Global Constraints

- Work only on `boop-v70-natural-voices`; do not consume, edit, merge, reset, or force-update concurrent tablet work on `boop-unified`.
- Base behavior is exact phone v70 from `c68a7aba8f0c9bcffa81ad0b517453cc8e50b12d` plus this branch's docs-only spec commits.
- Keep package `com.boop.alpha1`, existing permanent signer, versionCode 70, and current versionName unless a build-system requirement forces a revision.
- Do not touch BOOP eyes, blink, animation, notification dood, launcher, Shield HOME, Home Assistant, wake recognition, ASR ownership, or the exact 100 ms wake bridge.
- Never start a second microphone owner. Never leave `onTtsStarting()` unmatched by `finishTtsUtterance()`.
- Natural voice model data stays outside the APK and downloads only after an explicit tap, into app-private storage.
- Exact natural voice order is Emma (`bf_emma`, sid 21), Isabella (`bf_isabella`, sid 22), George (`bm_george`, sid 26), Fable (`bm_fable`, sid 25).
- Pin current official GitHub release artifact `kokoro-multi-lang-v1_0.tar.bz2`, size 349906910, SHA-256 `c5f7e2d2caf082bc1d20fb70334a61d99d20b484500aad32e7cf84c128ea3298`.
- CI may verify structure, behavior, package identity, signature and archive integrity. Ryan owns voice quality, accent, latency, volume and physical UI acceptance.

---

### Task 1: Add a RED natural-voices contract gate

**Files:**
- Create: `tests/test_unified_v70_natural_voices_contract.py`
- Modify: `.github/workflows/build-boop-unified.yml`

**Interfaces / contracts:**
- Materialized source must expose exactly four fixed natural voices in the approved order and speaker IDs.
- Pack manifest must pin HTTPS URL, exact SHA-256, version, archive size, required extracted files, and minimum free space.
- Materialized manifest must not add storage permissions, browser/file-picker download paths, API keys, or cloud TTS dependencies.
- `MainActivity.speak()` must retain `wakeCoordinator.onTtsStarting()` and completion through `finishTtsUtterance()`.
- Voice Settings must retain the existing v70 `ScrollView` repair and gain one `Download natural voices` entry point.

- [ ] Write pytest assertions for the missing manifest/classes/patch wiring and forbidden behavior.
- [ ] Add `boop-v70-natural-voices` to the existing Unified workflow branch trigger and run the natural contract test after materialization.
- [ ] Commit only tests/workflow plumbing.
- [ ] Observe GitHub Actions fail for the expected missing natural-voice implementation, not for an unrelated baseline fault.

### Task 2: Pin and validate the voice-pack manifest

**Files:**
- Create: `natural-voices/manifest.json`
- Create: `source/BoopNaturalVoiceManifest.java`
- Create: `source-test/BoopNaturalVoiceManifestTest.java`
- Modify: `scripts/materialize-unified.sh`

**Interfaces:**
- `BoopNaturalVoiceManifest.load(Context)` returns an immutable manifest model.
- `voices()` returns exactly Emma, Isabella, George, Fable with approved keys/sids.
- `requiredFiles()` includes `model.onnx`, `voices.bin`, `tokens.txt`, `espeak-ng-data`, and the British English lexicon used by the backend.

- [ ] Add JVM tests for exact URL/hash/size/version, voice order/mappings, HTTPS-only policy and required-file set; observe RED.
- [ ] Implement the smallest parser/model and copy the checked-in manifest into app assets during Unified materialization.
- [ ] Run focused JVM + pytest contracts; observe GREEN.
- [ ] Commit manifest and parser.

### Task 3: Extend `BoopVoiceController` without disturbing Android voices

**Files:**
- Modify: `source/BoopVoiceController.java`
- Modify: `source-test/BoopVoiceControllerTest.java`

**Interfaces:**
- Preserve current keys: `voice_name`, `pitch`, `speech_rate`.
- Add keys for `selected_backend`, `natural_speaker_key`, `natural_pack_version`, `natural_verification_state`.
- Add `selectedBackend()`, `selectedNaturalVoice()`, `selectNaturalVoice(String)`, `setNaturalPackState(...)`, `naturalPackUsable(...)`.
- `maybeChangeVoice(...)` cycles only the four natural voices when backend is natural and verified; otherwise executes the existing Android local-English cycle unchanged.
- Installing/verifying a pack does not switch backend until a natural voice is selected.

- [ ] Add controller tests for defaults, persistence, non-migration of Android voice/pitch/rate, natural four-voice cycling and Android fallback cycling; observe RED.
- [ ] Implement minimum controller state/selection logic.
- [ ] Run focused `*BoopVoice*` tests; observe GREEN.
- [ ] Commit controller behavior.

### Task 4: Build safe app-private download, verification and atomic activation

**Files:**
- Create: `source/BoopNaturalVoicePack.java`
- Create: `source/BoopNaturalVoiceDownloader.java`
- Create: `source-test/BoopNaturalVoicePackTest.java`
- Modify: `source/app-build.gradle`
- Modify: `unified/app-build.gradle`

**Interfaces:**
- `BoopNaturalVoicePack` exposes active/temp/archive paths under `Context.getFilesDir()` or `getNoBackupFilesDir()` only.
- `verifyArchive(File, expectedSha256)` streams SHA-256.
- Extraction uses Apache Commons Compress 1.28.0 and rejects absolute paths, `..`, symlinks/links, and any canonical path outside the staging directory.
- Validate required files before activation; activation renames staging to active only after complete validation.
- `BoopNaturalVoiceDownloader.start(Listener)` performs explicit HTTPS OkHttp download, free-space checks, foreground progress callbacks, cancellation, clean restart of incomplete archives, checksum verification, extraction, and atomic activation.
- Hash mismatch surfaces `Download didn't verify. Try again.` and leaves Android TTS active.

- [ ] Add pure file-policy/verifier tests including path traversal rejection and incomplete-pack rejection; observe RED.
- [ ] Add pinned Commons Compress dependency to both build files.
- [ ] Implement pack verifier/extractor/activation and downloader.
- [ ] Run focused JVM + pytest contracts; observe GREEN.
- [ ] Commit pack installation path.

### Task 5: Add local Sherpa Kokoro synthesis and one completion callback

**Files:**
- Create: `source/BoopSpeechBackend.java`
- Create: `source/BoopNaturalSpeechBackend.java`
- Create: `source-test/BoopNaturalSpeechBackendTest.java`

**Interfaces:**
- `BoopSpeechBackend.speak(String, float pitch, float rate, Callback)` invokes exactly one terminal `onDone()` or `onError(Throwable)` per accepted utterance.
- `BoopNaturalSpeechBackend` lazily creates Sherpa `OfflineTts` using pack `model.onnx`, `voices.bin`, `tokens.txt`, `espeak-ng-data`, British lexicon, two threads, debug false.
- Generation uses Sherpa `GenerationConfig` with approved sid and existing BOOP speech rate mapped/clamped to Kokoro `speed`.
- PCM plays locally through one `AudioTrack`; pitch is applied using Android playback parameters while generation speed remains the cadence control.
- Unsupported ABI/native load/synthesis/playback errors return through callback, never throw across the UI boundary; `release()` tears down executor/player/Sherpa safely.

- [ ] Add tests around rate mapping, exact speaker IDs and exactly-once callback/error guard; observe RED.
- [ ] Implement backend and local PCM playback on a private single-thread executor.
- [ ] Run focused tests/build compilation; observe GREEN.
- [ ] Commit synthesis backend.

### Task 6: Wire one-mouth fallback into `MainActivity.speak()`

**Files:**
- Create: `scripts/patch-unified-natural-voices.py`
- Modify: `scripts/materialize-unified.sh`
- Modify: `tests/test_unified_v70_natural_voices_contract.py`

**Interfaces:**
- `MainActivity.speak()` still calls `wakeCoordinator.onTtsStarting()` once.
- If verified natural backend is selected, call `BoopNaturalSpeechBackend`; natural success calls `finishTtsUtterance()` once.
- Natural runtime failure immediately attempts Android TTS for the same utterance without a second `onTtsStarting()`; Android completion remains handled by the existing TTS listener.
- If Android fallback cannot start, call `finishTtsUtterance()` once.
- `startListening()` stops any active natural playback as well as Android TTS.
- `onDestroy()` releases natural backend/download work without changing wake ownership.

- [ ] Extend materialized contract test to require fallback routing and exactly one lifecycle start boundary; observe RED.
- [ ] Implement deterministic patch after `patch-unified-v70-regressions.py` and before notification/dev-menu patches.
- [ ] Materialize and run wake/TTS/natural contract tests; observe GREEN.
- [ ] Commit speech routing.

### Task 7: Add the one-button Voice Settings download and four fixed choices

**Files:**
- Modify: `scripts/patch-unified-natural-voices.py`
- Modify: `tests/test_unified_v70_natural_voices_contract.py`

**Interfaces / UI:**
- Existing Pitch, Cadence, Wake sensitivity and Done controls remain.
- Natural voices section before install shows one `Download natural voices` button and plain-English progress/state text.
- Download stays in-app; cancel is available while active.
- After verified install, show Emma, Isabella, George, Fable in fixed order.
- Tapping a voice selects/persists it and runs a short preview through BOOP's speech lifecycle; no install alone changes current voice.
- Download failure/cancel keeps Android TTS untouched and the settings page usable.

- [ ] Add contract assertions for the exact labels/order, progress/cancel callback wiring, no browser/file picker, and retained scroll marker; observe RED.
- [ ] Patch the materialized Voice Settings content and callbacks with minimal controls.
- [ ] Run materialization, JVM tests and natural contract tests; observe GREEN.
- [ ] Commit Voice Settings integration.

### Task 8: Full CI, artifact and handoff verification

**Files:**
- Modify only if needed for factual handoff: `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md`

- [ ] Re-check live `boop-v70-natural-voices`, `boop-unified`, and `main` heads before final verification; confirm no tablet branch mutation.
- [ ] Run the entire `Build BOOP Unified APK` workflow on the natural-voices branch.
- [ ] Inspect job steps and logs; require all structural/JVM/pytest/build/signing/archive checks GREEN.
- [ ] Fetch artifact metadata, download `BOOP-Unified`, and verify built commit, APK SHA-256, package `com.boop.alpha1`, versionCode 70, permanent signer digest and ZIP integrity from receipts.
- [ ] Update handoff/status/memory docs only after code is verified, clearly marking physical/acoustic Pixel acceptance pending; commit docs separately.
- [ ] Re-check live branch head and compare tested app head to final docs-only head so no untested source changes slipped in.
- [ ] Hand Ryan the exact APK for physical testing. Do not call the voice quality, accent, latency, volume or UI physically accepted until Ryan says so.
