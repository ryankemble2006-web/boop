# BOOP Alpha 6.4 Wake Word Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the local wake word `BOOP` to the foreground Pixel 7 Pro app while preserving Alpha 6.3 tap-to-talk, member berry, voice tuning, direct media, Home Assistant and general-assistant behavior.

**Architecture:** Keep the existing Android `SpeechRecognizer` and command-routing path as the only transcript consumer. Add a sherpa-onnx keyword-spotting front end that owns one foreground `AudioRecord` stream, retains one second of PCM pre-roll, and after `BOOP` streams that same physical microphone into Android recognition through `RecognizerIntent.EXTRA_AUDIO_SOURCE`. A coordinator owns wake eligibility and mic arbitration; any wake-only failure degrades to the existing tap-only puppet for that foreground session.

**Tech Stack:** Android Java 17, Android API 36 with API 33+ supplied-audio recognition, mono PCM16 at 16 kHz, sherpa-onnx Android AAR v1.13.7, English GigaSpeech 3.3M mobile KWS model, JUnit 4, Python source-regression tests, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-04-boop-alpha64-wake-word-design.md`

## Global Constraints

- Base is Alpha 6.3 code 16 at `3b19abfa9bfce66d0a1ddbcf97f917a765a2759d`.
- Wake word is exactly `BOOP`.
- Tap-to-talk remains permanent and keeps the normal Android microphone recognizer path.
- Long-hold member berry remains unchanged.
- Wake listening is foreground-only. No background service and no lock-screen bypass.
- Armed-state wake audio stays on the Pixel. Only accepted wake-command audio enters the installed Android recognizer.
- Eyes wake immediately on accepted wake detection. No spoken acknowledgement and no beep.
- Both `BOOP` → command within 3 seconds and `BOOP <command>` in one breath must work.
- Three seconds is a hard maximum capture window, never a forced wait.
- Duplicate accepted triggers are blocked for 750 ms.
- Wake is suspended before BOOP TTS, during tap recognition, and while voice settings are open.
- Wake-specific failure is quiet, non-looping and must not damage tap-to-talk.
- Do not modify `HomeAssistantClient`, `HomeAssistantDirectMediaClient`, `BoopCommandRouter`, `HomeAssistantGeneralAssistantClient`, `BoopVoiceController`, `BoopFaceView`, `BoopPresenceState` or `source/AndroidManifest.xml`.
- Target metadata: `versionCode 17`, `versionName "0.4.4-alpha6.4"`.
- Pixel 7 Pro room behavior is the acoustic acceptance test; emulator launch is not proof of wake quality.

---

### Task 1: Pure wake behavior, timing and pre-roll

**Files:**
- Create: `source/BoopWakeTranscriptNormalizer.java`
- Create: `source/BoopWakeTriggerGate.java`
- Create: `source/BoopWakeSessionState.java`
- Create: `source/BoopPcmRingBuffer.java`
- Create: `source-test/BoopWakeTranscriptNormalizerTest.java`
- Create: `source-test/BoopWakeTriggerGateTest.java`
- Create: `source-test/BoopWakeSessionStateTest.java`
- Create: `source-test/BoopPcmRingBufferTest.java`

**Interfaces:**
- `BoopWakeTranscriptNormalizer.stripLeadingWakeWord(String) -> String`.
- `BoopWakeTriggerGate.accept(long elapsedMs) -> boolean`, `REFRACTORY_MS = 750L`.
- `BoopWakeSessionState.State { DISARMED, ARMED, WAKE_CAPTURE, PROCESSING, SPEAKING }`, `COMMAND_WINDOW_MS = 3_000L`.
- `BoopPcmRingBuffer.write(short[], int)`, `snapshot() -> short[]`.

- [ ] **Step 1: Write failing normalizer and trigger-gate tests**

`BoopWakeTranscriptNormalizerTest` must prove:

```java
assertEquals("pause the music", strip("boop pause the music"));
assertEquals("turn the lamp off", strip("Boop, turn the lamp off"));
assertEquals("", strip(" BOOP! "));
assertEquals("pause the music", strip("pause the music"));
assertEquals("tell me why boop is funny", strip("tell me why boop is funny"));
assertEquals("boopity boop", strip("boopity boop"));
```

`BoopWakeTriggerGateTest` must prove:

```java
BoopWakeTriggerGate gate = new BoopWakeTriggerGate();
assertTrue(gate.accept(10_000L));
assertFalse(gate.accept(10_749L));
assertTrue(gate.accept(10_750L));
```

- [ ] **Step 2: Write failing state-machine tests**

Use this ready-state helper:

```java
private BoopWakeSessionState readyState() {
    BoopWakeSessionState state = new BoopWakeSessionState();
    state.beginForegroundSession();
    state.setMicrophonePermission(true);
    state.setRecognitionSupported(true);
    return state;
}
```

Tests must prove:

1. foreground + permission + recognition support are all required for `ARMED`;
2. `acceptWake(5_000L)` changes `ARMED -> WAKE_CAPTURE` and `commandDeadlineMs() == 8_000L`;
3. `markProcessing()` changes `WAKE_CAPTURE -> PROCESSING`;
4. `finishProcessing()` changes `PROCESSING -> ARMED` when no blockers exist;
5. `cancelWakeCapture()` changes `WAKE_CAPTURE -> ARMED` for no-match/speech-timeout;
6. TTS creates `SPEAKING`, and stopping TTS re-arms only when no tap/settings blocker exists;
7. tap and voice-settings blockers force `DISARMED`;
8. `failWakeSession()` latches `DISARMED` for the current foreground session;
9. `endForegroundSession(); beginForegroundSession();` clears the prior wake failure but does not arm until recognition support is checked again.

- [ ] **Step 3: Write failing ring-buffer tests**

```java
BoopPcmRingBuffer ring = new BoopPcmRingBuffer(5);
ring.write(new short[]{1, 2, 3}, 3);
assertArrayEquals(new short[]{1, 2, 3}, ring.snapshot());
ring.write(new short[]{4, 5, 6, 7}, 4);
assertArrayEquals(new short[]{3, 4, 5, 6, 7}, ring.snapshot());

BoopPcmRingBuffer small = new BoopPcmRingBuffer(4);
small.write(new short[]{10, 11, 12, 13, 14, 15}, 6);
assertArrayEquals(new short[]{12, 13, 14, 15}, small.snapshot());
```

- [ ] **Step 4: Materialize and confirm RED**

```bash
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

Expected: new test classes fail to compile because production classes are absent.

- [ ] **Step 5: Implement the four minimal production classes**

Normalizer:

```java
private static final Pattern LEADING_BOOP =
        Pattern.compile("(?i)^\\s*boop\\b[\\s,;:!?.-]*");
static String stripLeadingWakeWord(String text) {
    if (text == null) return "";
    return LEADING_BOOP.matcher(text).replaceFirst("").trim();
}
```

Trigger gate:

```java
static final long REFRACTORY_MS = 750L;
private long lastAcceptedMs = Long.MIN_VALUE;
boolean accept(long nowMs) {
    if (lastAcceptedMs != Long.MIN_VALUE
            && nowMs - lastAcceptedMs < REFRACTORY_MS) return false;
    lastAcceptedMs = nowMs;
    return true;
}
```

State must hold these exact blockers/fields:

```java
static final long COMMAND_WINDOW_MS = 3_000L;
private State state = State.DISARMED;
private boolean foreground;
private boolean microphonePermission;
private boolean recognitionSupported;
private boolean voiceSettingsOpen;
private boolean tapListening;
private boolean ttsSpeaking;
private boolean wakeFailed;
private long commandDeadlineMs = -1L;
```

`beginForegroundSession()` sets `foreground=true`, clears `wakeFailed`, resets `recognitionSupported=false`, then reevaluates. `endForegroundSession()` sets `foreground=false` and `DISARMED`. `reevaluate()` uses this priority: failed/not foreground/missing permission/missing support/settings/tap → `DISARMED`; TTS → `SPEAKING`; otherwise, if state is not `WAKE_CAPTURE` or `PROCESSING`, → `ARMED`. `cancelWakeCapture()` clears the deadline and reevaluates. `markProcessing()` requires `WAKE_CAPTURE`; `finishProcessing()` clears processing and reevaluates.

Ring buffer must keep only newest samples and return a defensive chronological copy.

- [ ] **Step 6: Confirm GREEN and commit**

```bash
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
git add source/BoopWakeTranscriptNormalizer.java source/BoopWakeTriggerGate.java source/BoopWakeSessionState.java source/BoopPcmRingBuffer.java source-test/BoopWakeTranscriptNormalizerTest.java source-test/BoopWakeTriggerGateTest.java source-test/BoopWakeSessionStateTest.java source-test/BoopPcmRingBufferTest.java
git commit -m "test: lock BOOP wake state and audio primitives"
```

---

### Task 2: Deterministic sherpa runtime and BOOP keyword assets

**Files:**
- Create: `wake-assets/boop-kws/keywords_raw.txt`
- Create: `wake-assets/boop-kws/keywords.txt`
- Create: `scripts/fetch-wake-assets.sh`
- Modify: `scripts/materialize-android.sh`
- Modify: `source/app-build.gradle`
- Modify: `.gitignore`
- Create: `tests/test_alpha64_wake_assets.py`

**Pins:**
- sherpa AAR: `v1.13.7`, `sherpa-onnx-1.13.7.aar`.
- AAR SHA256: `c4ef49e309f24fcee5c106b8a279481aaecaabb078cd37b2cd6e9a62cc8a73c8`.
- Model: `sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile.tar.bz2` from official `kws-models` release.
- Kotlin runtime: `org.jetbrains.kotlin:kotlin-stdlib:1.7.20`.

- [ ] **Step 1: Write `tests/test_alpha64_wake_assets.py` and confirm RED**

Require exact pins, the fetch script, Gradle AAR dependency, materializer hook, and raw keyword equal to:

```text
BOOP :1.5 #0.25 @BOOP
```

Run:

```bash
python3 -m unittest tests/test_alpha64_wake_assets.py -v
```

- [ ] **Step 2: Generate the tokenized BOOP keyword from the pinned model**

Create `keywords_raw.txt` with the exact line above. Download/extract the pinned model into `.cache/boop-wake`, then run:

```bash
python3 -m pip install 'sherpa-onnx==1.13.7'
sherpa-onnx-cli text2token \
  --tokens .cache/boop-wake/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile/tokens.txt \
  --tokens-type bpe \
  --bpe-model .cache/boop-wake/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile/bpe.model \
  wake-assets/boop-kws/keywords_raw.txt \
  wake-assets/boop-kws/keywords.txt
```

Commit the generated `keywords.txt`; never hand-type BPE tokens.

- [ ] **Step 3: Implement `scripts/fetch-wake-assets.sh`**

Use `set -euo pipefail`, one destination argument, cache directory `.cache/boop-wake`, the exact AAR SHA above, and official release URLs. Verify the model archive against official `kws-models/checksum.txt` before extraction. Copy only these runtime model files into `app/src/main/assets/boop-kws/`:

```text
encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx
decoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx
joiner-epoch-12-avg-2-chunk-16-left-64.int8.onnx
tokens.txt
keywords.txt
```

Copy the AAR to `app/libs/sherpa-onnx-1.13.7.aar`.

Model checksum extraction must fail closed:

```bash
curl -fsSL "$BASE/kws-models/checksum.txt" -o "$CACHE/checksum.txt"
MODEL_SHA256="$(awk -v name="$MODEL_ARCHIVE" '$2 == name {print $1}' "$CACHE/checksum.txt")"
test -n "$MODEL_SHA256"
printf '%s  %s\n' "$MODEL_SHA256" "$CACHE/$MODEL_ARCHIVE" | sha256sum -c -
```

- [ ] **Step 4: Wire materialization and Gradle**

Append:

```bash
bash scripts/fetch-wake-assets.sh "$ROOT/app"
```

to `scripts/materialize-android.sh` after source/test overlay.

Add:

```gradle
implementation files('libs/sherpa-onnx-1.13.7.aar')
implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.7.20'
```

to `source/app-build.gradle`, and add `.cache/boop-wake/` to `.gitignore`.

- [ ] **Step 5: Verify assets, compile and commit**

```bash
python3 -m unittest tests/test_alpha64_wake_assets.py -v
bash scripts/materialize-android.sh
test -s boop-build/BOOP-Alpha1/app/libs/sherpa-onnx-1.13.7.aar
test -s boop-build/BOOP-Alpha1/app/src/main/assets/boop-kws/encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx
test -s boop-build/BOOP-Alpha1/app/src/main/assets/boop-kws/keywords.txt
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
git add .gitignore wake-assets/boop-kws scripts/fetch-wake-assets.sh scripts/materialize-android.sh source/app-build.gradle tests/test_alpha64_wake_assets.py
git commit -m "build: pin BOOP local wake dependencies"
```

---

### Task 3: Supplied-audio recognition seam

**Files:**
- Create: `source/BoopWakeRecognitionIntent.java`
- Create: `source/BoopWakeAudioSession.java`
- Create: `source-test/BoopWakeRecognitionIntentTest.java`
- Create: `tests/test_alpha64_wake_recognition_surface.py`

**Interfaces:**
- `BoopWakeRecognitionIntent.build(Locale, ParcelFileDescriptor) -> Intent`.
- Constants: `SAMPLE_RATE_HZ=16_000`, `CHANNEL_COUNT=1`, `ENCODING=AudioFormat.ENCODING_PCM_16BIT`.
- `BoopWakeAudioSession.audioSource()`, `finishCapture()`, `close()`.

- [ ] **Step 1: Write failing tests**

Python source test must require all four supplied-audio extras and must continue to require `SpeechRecognizer.createSpeechRecognizer(this)` while forbidding `createOnDeviceSpeechRecognizer` in `MainActivity`.

JUnit constant test:

```java
@Test public void wakePcmMetadataIsFixed() {
    assertEquals(16_000, BoopWakeRecognitionIntent.SAMPLE_RATE_HZ);
    assertEquals(1, BoopWakeRecognitionIntent.CHANNEL_COUNT);
    assertEquals(AudioFormat.ENCODING_PCM_16BIT, BoopWakeRecognitionIntent.ENCODING);
}
```

Run Python test first and confirm missing-class RED.

- [ ] **Step 2: Implement `BoopWakeRecognitionIntent`**

```java
static Intent build(Locale locale, ParcelFileDescriptor source) {
    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toLanguageTag());
    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
    intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, source);
    intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, CHANNEL_COUNT);
    intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, ENCODING);
    intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, SAMPLE_RATE_HZ);
    return intent;
}
```

- [ ] **Step 3: Implement idempotent `BoopWakeAudioSession`**

Use one read `ParcelFileDescriptor`, one `Runnable finishCapture`, and two `AtomicBoolean`s. `finishCapture()` runs its callback once. `close()` calls `finishCapture()` then closes the read descriptor once. Do not transfer ownership of the read descriptor to another BOOP object; `MainActivity` closes the session after wake recognition completes/errors.

- [ ] **Step 4: Verify and commit**

```bash
python3 -m unittest tests/test_alpha64_wake_recognition_surface.py tests/test_speech_recognizer_mode.py -v
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
git add source/BoopWakeRecognitionIntent.java source/BoopWakeAudioSession.java source-test/BoopWakeRecognitionIntentTest.java tests/test_alpha64_wake_recognition_surface.py
git commit -m "feat: add BOOP supplied-audio recognition seam"
```

---

### Task 4: Local sherpa spotter and one-mic wake capture

**Files:**
- Create: `source/BoopSherpaWakeSpotter.java`
- Create: `source/BoopWakeWordController.java`
- Create: `tests/test_alpha64_wake_controller.py`

**Interfaces:**
- `BoopSherpaWakeSpotter.accept(short[], int) -> boolean`.
- `BoopWakeWordController.Listener.onWakeDetected(BoopWakeAudioSession, long)`.
- `BoopWakeWordController.Listener.onWakeFailure(String)`.
- `BoopWakeWordController.arm() -> boolean`, `suspendAll()`, `shutdown()`.

- [ ] **Step 1: Write source tests and confirm RED**

Require `AudioRecord`, `BoopPcmRingBuffer`, `BoopWakeTriggerGate`, `ParcelFileDescriptor.createPipe()`, and the exact pinned int8 asset names. Assert controller source contains no `HomeAssistant`, `OpenAI` or `BoopCommandRouter`.

- [ ] **Step 2: Implement `BoopSherpaWakeSpotter` against v1.13.7**

Use Java no-arg constructors/setters generated from sherpa Kotlin data classes:

```java
FeatureConfig feature = new FeatureConfig();
feature.setSampleRate(16_000);
feature.setFeatureDim(80);
feature.setDither(0.0f);

OnlineTransducerModelConfig transducer = new OnlineTransducerModelConfig();
transducer.setEncoder("boop-kws/encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx");
transducer.setDecoder("boop-kws/decoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx");
transducer.setJoiner("boop-kws/joiner-epoch-12-avg-2-chunk-16-left-64.int8.onnx");

OnlineModelConfig model = new OnlineModelConfig();
model.setTransducer(transducer);
model.setTokens("boop-kws/tokens.txt");
model.setNumThreads(2);
model.setProvider("cpu");
model.setModelType("zipformer2");

KeywordSpotterConfig config = new KeywordSpotterConfig();
config.setFeatConfig(feature);
config.setModelConfig(model);
config.setKeywordsFile("boop-kws/keywords.txt");
config.setMaxActivePaths(4);
config.setKeywordsScore(1.5f);
config.setKeywordsThreshold(0.25f);
config.setNumTrailingBlanks(2);
```

Create `KeywordSpotter(context.getAssets(), config)` and one stream. Convert each PCM short to float via `sample / 32768.0f`, call `stream.acceptWaveform(samples, 16_000)`, decode while ready, and accept only a non-empty `getKeyword()` equal to `BOOP` ignoring case. Reset stream after a trigger. `close()` releases stream then spotter.

- [ ] **Step 3: Implement foreground `AudioRecord` KWS**

Use mono PCM16 16 kHz, one-second ring pre-roll, roughly 100 ms read buffers, and worker name `boop-wake-audio`. `arm()` lazily creates the spotter, creates/starts `AudioRecord`, then starts the worker. It is idempotent while already armed.

On KWS result:

1. gate with `BoopWakeTriggerGate.accept(SystemClock.elapsedRealtime())`;
2. create `ParcelFileDescriptor.createPipe()`;
3. wrap writer with `ParcelFileDescriptor.AutoCloseOutputStream`;
4. write the ring snapshot as little-endian PCM16;
5. create `BoopWakeAudioSession` around the read PFD, whose finish callback stops command capture and closes the writer;
6. post `onWakeDetected(session, detectedAtMs)` to the main thread;
7. stop feeding KWS and stream subsequent microphone samples to the pipe;
8. stop/release `AudioRecord` and EOF the pipe no later than `detectedAtMs + BoopWakeSessionState.COMMAND_WINDOW_MS`.

After pre-roll is written, continue from the next microphone read so the trigger frame is not duplicated.

`suspendAll()` stops/release `AudioRecord`, ends an active pipe capture and stops the worker without releasing the reusable spotter. `shutdown()` also releases sherpa resources. Catch model/native/audio/pipe initialization errors, log once with tag `BOOP-Wake`, call `onWakeFailure`, and do not retry in a loop.

- [ ] **Step 4: Compile the real AAR and inspect APK contents**

```bash
python3 -m unittest tests/test_alpha64_wake_controller.py tests/test_alpha64_wake_assets.py -v
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
gradle -p boop-build/BOOP-Alpha1 :app:assembleDebug --stacktrace
APK=boop-build/BOOP-Alpha1/app/build/outputs/apk/debug/app-debug.apk
unzip -l "$APK" | grep 'sherpa-onnx-jni'
unzip -l "$APK" | grep 'assets/boop-kws/encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx'
unzip -l "$APK" | grep 'assets/boop-kws/keywords.txt'
```

- [ ] **Step 5: Commit**

```bash
git add source/BoopSherpaWakeSpotter.java source/BoopWakeWordController.java tests/test_alpha64_wake_controller.py
git commit -m "feat: add local BOOP keyword spotter"
```

---

### Task 5: Wake coordinator and sacred tap-path integration

**Files:**
- Create: `source/BoopWakeSessionCoordinator.java`
- Create: `source-test/BoopWakeSessionCoordinatorTest.java`
- Modify: `source/MainActivity.java`
- Create: `tests/test_alpha64_wake_integration.py`

**Coordinator interface:**

```java
interface Engine {
    boolean arm();
    void suspendAll();
    void shutdown();
}
```

Methods: `beginForegroundSession()`, `endForegroundSession()`, `setMicrophonePermission(boolean)`, `setRecognitionSupported(boolean)`, `setVoiceSettingsOpen(boolean)`, `onTapStarted()`, `onTapFinished()`, `onTtsStarting()`, `onTtsFinished()`, `onWakeDetected(long) -> boolean`, `markWakeProcessing()`, `cancelWakeCapture()`, `finishWakeProcessing()`, `failWakeSession()`, `shutdown()`.

- [ ] **Step 1: Write fake-engine coordinator tests and confirm RED**

Prove:

- ready state calls `arm()` once and repeated sync is harmless;
- foreground stop, tap start, settings open and TTS start call `suspendAll()`;
- TTS stop while tap is still active remains disarmed;
- `WAKE_CAPTURE` does not suspend because the controller must keep feeding the pipe;
- `PROCESSING` stays disarmed;
- engine `arm()` returning false latches wake failure and does not call arm repeatedly in that foreground session;
- next `beginForegroundSession()` after a foreground stop clears the failure but waits for a fresh recognition-support result.

Core adapter:

```java
private void syncEngine() {
    switch (state.state()) {
        case ARMED:
            if (!engine.arm()) {
                state.failWakeSession();
                engine.suspendAll();
            }
            return;
        case WAKE_CAPTURE:
            return;
        case DISARMED:
        case PROCESSING:
        case SPEAKING:
        default:
            engine.suspendAll();
    }
}
```

- [ ] **Step 2: Write `tests/test_alpha64_wake_integration.py` and confirm RED**

Require `RecognitionMode { NONE, TAP, WAKE }`, wake coordinator, wake normalizer, wake recognition intent, existing `SpeechRecognizer.createSpeechRecognizer(this)`, and unchanged `commandRouter.process(transcript)`. Also assert voice-settings interception precedes voice cycling, which precedes router processing.

- [ ] **Step 3: Construct wake objects without touching house/router construction**

Add fields:

```java
private enum RecognitionMode { NONE, TAP, WAKE }
private RecognitionMode recognitionMode = RecognitionMode.NONE;
private BoopWakeWordController wakeWordController;
private BoopWakeSessionCoordinator wakeCoordinator;
private BoopWakeAudioSession wakeAudioSession;
private boolean wakeSupportCheckInFlight;
```

Create controller/coordinator after `createRecognizer()`. Controller listener posts to UI. On candidate wake:

```java
if (!wakeCoordinator.onWakeDetected(detectedAtMs)) {
    session.close();
    return;
}
wakeFaceForInteraction();
startWakeRecognition(session);
```

No routing client or parser is moved into this callback.

- [ ] **Step 4: Preserve normal tap recognition**

After RECORD_AUDIO permission is confirmed:

```java
wakeCoordinator.onTapStarted();
recognitionMode = RecognitionMode.TAP;
startListening();
```

Keep the existing `startListening()` intent unchanged: free-form model, locale BCP-47 tag, max 3, no partials, no supplied audio. It still stops current TTS and calls the existing recognizer.

For successful tap results: clear recognition mode/listening, call `wakeCoordinator.onTapFinished()`, then pass the same best transcript to `handleRecognizedSpeech`.

For tap errors or empty results that immediately speak an error: call `speak(...)` while the tap blocker is still set, then call `onTapFinished()`. This prevents a wake-arm gap between mic release and BOOP error TTS.

- [ ] **Step 5: Start wake recognition from the pipe**

```java
private void startWakeRecognition(BoopWakeAudioSession session) {
    if (recognizer == null) {
        session.close();
        wakeCoordinator.failWakeSession();
        return;
    }
    wakeAudioSession = session;
    recognitionMode = RecognitionMode.WAKE;
    listening = true;
    face.animate().alpha(0.78f).setDuration(120).start();
    try {
        recognizer.startListening(
                BoopWakeRecognitionIntent.build(Locale.getDefault(), session.audioSource()));
    } catch (RuntimeException e) {
        closeWakeAudioSession();
        listening = false;
        recognitionMode = RecognitionMode.NONE;
        face.animate().alpha(1.0f).setDuration(120).start();
        wakeCoordinator.failWakeSession();
    }
}
```

`onEndOfSpeech()` calls `wakeAudioSession.finishCapture()` only for WAKE mode.

Wake `onError` policy is exact:

- `ERROR_NO_MATCH` or `ERROR_SPEECH_TIMEOUT`: close session, clear UI/mode, call `wakeCoordinator.cancelWakeCapture()`, no speech;
- any other wake recognizer error: close session, clear UI/mode, call `wakeCoordinator.failWakeSession()`, no speech.

Wake `onResults` policy is exact:

1. close wake session, clear listening/mode and restore face alpha;
2. call `wakeCoordinator.markWakeProcessing()`;
3. take the same best non-empty recognizer result as tap;
4. normalize with `BoopWakeTranscriptNormalizer.stripLeadingWakeWord`;
5. if result is empty, `wakeCoordinator.finishWakeProcessing()` and return silently;
6. otherwise call the unchanged `handleRecognizedSpeech(normalized)`.

Every non-empty wake transcript therefore enters the exact same local voice-settings / voice-cycle / house / assistant path as tap.

- [ ] **Step 6: Add API 33 supplied-audio support probing**

When a foreground session has mic permission and a recognizer, make a temporary pipe, close its writer, build the same wake intent with the read PFD, and call `recognizer.checkRecognitionSupport(intent, getMainExecutor(), callback)`. Close the probe read PFD in both callbacks. `onSupportResult` sets coordinator recognition support true; callback `onError` sets it false. API below 33 sets false without probing.

The support callback is a gate, not proof that the recognizer honors supplied audio correctly; the Pixel test decides that.

- [ ] **Step 7: Verify regressions and commit**

```bash
python3 -m unittest \
  tests/test_alpha64_wake_integration.py \
  tests/test_speech_recognizer_mode.py \
  tests/test_alpha5_direct_media.py \
  tests/test_alpha6_routing.py \
  tests/test_alpha61_thinking_puppet.py \
  tests/test_alpha62_voice.py \
  tests/test_alpha63_voice_sliders.py \
  tests/test_member_berries.py -v
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
git add source/BoopWakeSessionCoordinator.java source-test/BoopWakeSessionCoordinatorTest.java source/MainActivity.java tests/test_alpha64_wake_integration.py
git commit -m "feat: route BOOP wake capture into existing speech path"
```

---

### Task 6: Foreground, TTS, voice-settings and teardown lifecycle

**Files:**
- Modify: `source/MainActivity.java`
- Modify: `source-test/BoopWakeSessionCoordinatorTest.java`
- Create: `tests/test_alpha64_wake_lifecycle.py`

- [ ] **Step 1: Write lifecycle source tests and confirm RED**

Require all of these:

- `onResume()` begins a foreground wake session, refreshes mic permission and starts support probing;
- `onPause()` ends the foreground wake session and closes active wake audio before `super.onPause()`;
- voice-settings open/close set/clear the coordinator blocker;
- `speak` calls `onTtsStarting()` before `tts.speak`;
- one `UtteranceProgressListener` calls `onTtsFinished()` on done/error/stop;
- `onDestroy` closes wake session and calls coordinator shutdown;
- no Android `Service`, `startForegroundService`, keyguard bypass or lock-screen flags are added.

- [ ] **Step 2: Implement foreground session lifecycle**

```java
@Override protected void onResume() {
    super.onResume();
    if (wakeCoordinator != null) {
        wakeCoordinator.beginForegroundSession();
        boolean granted = checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
        wakeCoordinator.setMicrophonePermission(granted);
        if (granted) checkWakeRecognitionSupport();
    }
}

@Override protected void onPause() {
    closeWakeAudioSession();
    if (wakeCoordinator != null) wakeCoordinator.endForegroundSession();
    super.onPause();
}
```

Do not add an automatic startup mic permission prompt. Existing installs retain permission; a fresh install can grant it through the permanent tap flow.

- [ ] **Step 3: Suspend wake before all BOOP TTS**

Install one `UtteranceProgressListener`. `speak(String)` calls `wakeCoordinator.onTtsStarting()` before `tts.speak`. If TTS is unavailable or `tts.speak` returns `TextToSpeech.ERROR`, immediately call `onTtsFinished()`.

Callbacks:

```java
@Override public void onDone(String id) {
    runOnUiThread(() -> wakeCoordinator.onTtsFinished());
}
@Override public void onError(String id) {
    runOnUiThread(() -> wakeCoordinator.onTtsFinished());
}
@Override public void onStop(String id, boolean interrupted) {
    runOnUiThread(() -> wakeCoordinator.onTtsFinished());
}
```

Guard coordinator null during teardown. This covers assistant replies, `This one?`, setup speech and error speech.

- [ ] **Step 4: Prove tap can interrupt TTS without rearming wake in between**

Add this exact coordinator test sequence:

```java
coordinator.onTtsStarting();
coordinator.onTapStarted();
coordinator.onTtsFinished();
assertEquals(BoopWakeSessionState.State.DISARMED, coordinator.state());
coordinator.onTapFinished();
assertEquals(BoopWakeSessionState.State.ARMED, coordinator.state());
```

- [ ] **Step 5: Wire voice settings and permission result**

Immediately after `voiceSettingsOpen = true`, set coordinator settings blocker true. Immediately after setting false in `hideVoiceSettings`, clear the blocker. In existing `REQ_RECORD_AUDIO` result handling, update `wakeCoordinator.setMicrophonePermission(granted)` before continuing the existing pending tap action.

- [ ] **Step 6: Teardown**

At `onDestroy`, close wake audio session, shutdown coordinator/controller, then retain the existing discovery/recognizer/TTS/executor teardown. No wake worker, `AudioRecord`, pipe PFD or sherpa stream may survive activity destruction.

- [ ] **Step 7: Run full regressions and commit**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
git add source/MainActivity.java source-test/BoopWakeSessionCoordinatorTest.java tests/test_alpha64_wake_lifecycle.py
git commit -m "feat: suspend BOOP wake listening around foreground and speech"
```

---

### Task 7: Code 17, frozen-source CI and signed APK

**Files:**
- Modify: `tests/test_alpha2_build_surface.py`
- Modify: `source/app-build.gradle`
- Create: `.github/workflows/build-alpha64.yml`
- Modify: `.github/workflows/verify-alpha6.yml`
- Create: `tests/test_alpha64_build_workflow.py`

- [ ] **Step 1: Version TDD RED then GREEN**

Change the build-surface test first to require:

```python
self.assertIn('versionCode 17', text)
self.assertIn('versionName "0.4.4-alpha6.4"', text)
```

Run it and confirm the only failure is old code16 metadata. Then change production to:

```gradle
versionCode 17
versionName "0.4.4-alpha6.4"
```

and re-run green.

- [ ] **Step 2: Write workflow source test first**

Require branch `alpha6.4-wake-word`, code16 freeze baseline SHA, pinned wake-asset fetch, full Python/JVM tests, code17 assembly, signing verification, clean Android 16 Pixel 7 Pro emulator launch, and artifact name `BOOP-Alpha6.4-Wake-Word-debug`.

- [ ] **Step 3: Create `build-alpha64.yml` from the proven Alpha 6.3 workflow**

Keep Java 17, Android 36, Gradle 9.6, permanent signing, KVM, clean Pixel 7 Pro emulator and artifact upload. Freeze every pre-existing production source except `MainActivity.java` and build metadata:

```bash
git diff --exit-code 3b19abfa9bfce66d0a1ddbcf97f917a765a2759d HEAD -- \
  source/ \
  ':(exclude)source/MainActivity.java' \
  ':(exclude)source/BoopWakeTranscriptNormalizer.java' \
  ':(exclude)source/BoopWakeTriggerGate.java' \
  ':(exclude)source/BoopWakeSessionState.java' \
  ':(exclude)source/BoopPcmRingBuffer.java' \
  ':(exclude)source/BoopWakeRecognitionIntent.java' \
  ':(exclude)source/BoopWakeAudioSession.java' \
  ':(exclude)source/BoopSherpaWakeSpotter.java' \
  ':(exclude)source/BoopWakeWordController.java' \
  ':(exclude)source/BoopWakeSessionCoordinator.java' \
  ':(exclude)source/app-build.gradle'
```

After materialization require AAR/model/keywords files. APK inspection requires code17/name, `assets/boop-kws/keywords.txt`, sherpa JNI, `apksigner verify --verbose`, and `unzip -tq`.

- [ ] **Step 4: Add Alpha 6.4 to generic verifier**

Append `"alpha6.4-wake-word"` to the existing `verify-alpha6.yml` push branches. Do not remove older Alpha 6 branches.

- [ ] **Step 5: Run everything locally available before push**

```bash
python3 -m unittest bridge/test_boop_wyoming_bridge.py -v
python3 -m unittest discover -s tests -p 'test_*.py' -v
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
gradle -p boop-build/BOOP-Alpha1 :app:assembleDebug --stacktrace
```

- [ ] **Step 6: Commit and inspect both GitHub Actions runs**

```bash
git add tests/test_alpha2_build_surface.py source/app-build.gradle .github/workflows/build-alpha64.yml .github/workflows/verify-alpha6.yml tests/test_alpha64_build_workflow.py
git commit -m "ci: fully verify BOOP Alpha 6.4 wake-word APK"
```

Required green evidence: source freeze, bridge/source tests, wake-asset verification, materialization, Java17, SDK36, Gradle9.6, JVM tests, signing, code17 inspection, clean Android16 emulator install/launch with surviving process, artifact upload.

Do not claim acoustic success from CI.

---

### Task 8: Fresh verification and Pixel 7 Pro acceptance

**Files:**
- No production change unless a verified failure returns execution to the relevant task under `superpowers:systematic-debugging`.

- [ ] **Step 1: Verify final diff and artifact before handoff**

Run verification-before-completion. Final branch diff against code16 may contain only approved wake classes, `MainActivity` wake integration/lifecycle, build dependency/version changes, wake asset/materialization files, tests/docs/workflows. Unexpected existing production-source changes block release.

Download the successful dedicated artifact, extract raw APK as:

```text
BOOP-Alpha6.4-Wake-Word-code17.apk
```

Verify artifact digest, raw APK SHA256, ZIP integrity, manifest/classes.dex, code17/name and signature before sharing the raw `.apk`.

- [ ] **Step 2: Pixel room checklist**

1. Install over code16.
2. Open BOOP and allow normal idle black.
3. Say `BOOP`; eyes wake immediately; say `pause the music` within 3 seconds.
4. Say `BOOP play the music` in one continuous sentence.
5. Say `BOOP turn the lamp off` continuously.
6. Say `BOOP why are oranges orange?`; verify normal HA `NO_MATCH` → general assistant.
7. Try whisper, normal and loud `BOOP` at normal room distance.
8. Play ordinary TV/conversation audio and watch for false triggers.
9. Have BOOP speak the word `BOOP`; she must not self-trigger.
10. Tap before/after wake tests; tap must feel exactly like code16.
11. Long-hold member berry; unchanged.
12. Open `voice settings`, move Pitch/Cadence, close; wake must resume.
13. Say `change your voice`; after `This one?`, wake must resume.
14. Background/close app and say `BOOP`; nothing happens. Reopen and verify wake returns.

- [ ] **Step 3: Diagnose by subsystem if room behavior fails**

- Wake never fires while tap works → model/AudioRecord/KWS initialization and mic permission.
- Eyes wake but one-breath command loses its first word → pre-roll/pipe/supplied-audio timing.
- Duplicate wake → refractory gate/re-arm timing.
- Self-trigger from TTS → `onTtsStarting` ordering/progress callbacks.
- Tap regression → revert/debug wake handoff; tap has priority.
- Media/HA/assistant regression → compare against code16 freeze; never modify routing as compensation.

Invoke `superpowers:systematic-debugging` before corrective code.

- [ ] **Step 4: Checkpoint only after room success**

After Ryan reports Pixel success, create immutable `checkpoint-alpha6.4-known-good` from the exact field-tested commit. Do not move earlier checkpoints.

---

## Plan self-review

- Spec coverage: foreground-only/local wake, exact `BOOP`, continuous sentence, 3-second max, 750 ms refractory, TTS/settings/tap blockers, fail-to-tap-only, no routing changes, Pixel acceptance are each assigned to an implementation task and a test.
- Placeholder scan: no `TBD`, unresolved implementation choice or fake test is permitted by this plan.
- Type/signature consistency: wake errors from `WAKE_CAPTURE` use `cancelWakeCapture()`; result handling enters `PROCESSING` with `markWakeProcessing()` and leaves through TTS/settings or `finishWakeProcessing()`; the recognition-intent JUnit test is fixed to constant-only JVM-safe assertions; foreground failure resets only on a new foreground session and recognition support is re-probed before arming.
