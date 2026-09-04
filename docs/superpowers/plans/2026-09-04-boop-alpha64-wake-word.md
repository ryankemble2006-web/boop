# BOOP Alpha 6.4 Wake Word Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add the local wake word `BOOP` to the foreground Pixel 7 Pro app while preserving Alpha 6.3 tap-to-talk, member berry, voice tuning, direct media, Home Assistant and general-assistant behavior.

**Architecture:** Keep the existing Android `SpeechRecognizer` and command-routing path as the only transcript consumer. Add a local sherpa-onnx keyword-spotting front end that owns one foreground `AudioRecord` stream, keeps roughly one second of PCM pre-roll, and on `BOOP` feeds the preserved continuous command into the existing recognizer through `RecognizerIntent.EXTRA_AUDIO_SOURCE`. A small coordinator arbitrates foreground state, TTS, tap recognition, voice settings and wake capture so any wake failure degrades to the existing tap-only puppet.

**Tech Stack:** Android Java 17, Android API 36 with API 33+ supplied-audio speech APIs, `AudioRecord` mono PCM16 at 16 kHz, sherpa-onnx Android AAR v1.13.7, English GigaSpeech 3.3M mobile KWS model, JUnit 4, Python source-regression tests, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-04-boop-alpha64-wake-word-design.md`

## Global Constraints

- Base implementation is Alpha 6.3 code 16 at `3b19abfa9bfce66d0a1ddbcf97f917a765a2759d`.
- Wake word is exactly `BOOP`.
- Tap-to-talk remains permanent and keeps the normal Android microphone recognizer path.
- Long-hold member berry remains unchanged.
- Wake listening is foreground-only; no background service and no lock-screen bypass.
- Wake detection is local; armed-state audio is never sent to Home Assistant, OpenCode or OpenAI.
- Eyes wake immediately on accepted wake detection; no verbal acknowledgement and no beep.
- Support both `BOOP` then a command within 3 seconds and `BOOP <command>` as one continuous sentence.
- The 3-second command window is a maximum, not an artificial delay.
- Reject duplicate wake triggers for 750 ms after an accepted trigger.
- Suspend wake listening before BOOP TTS begins and resume after done/error/stop.
- Suspend wake listening while voice settings are open.
- Any wake-only failure disables wake for that foreground session and leaves tap-to-talk usable.
- Do not change `HomeAssistantClient`, `HomeAssistantDirectMediaClient`, `BoopCommandRouter`, `HomeAssistantGeneralAssistantClient`, `BoopVoiceController`, `BoopFaceView`, `BoopPresenceState` or `source/AndroidManifest.xml` for this feature.
- Version target is `versionCode 17`, `versionName "0.4.4-alpha6.4"`.
- Final field acceptance is Pixel 7 Pro first; emulator success does not prove acoustic wake quality.

---

### Task 1: Lock the pure wake-word behavior with JVM tests

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
- Produces: `BoopWakeTranscriptNormalizer.stripLeadingWakeWord(String) -> String`.
- Produces: `BoopWakeTriggerGate.accept(long elapsedMs) -> boolean`, with `REFRACTORY_MS = 750L`.
- Produces: `BoopWakeSessionState.State { DISARMED, ARMED, WAKE_CAPTURE, PROCESSING, SPEAKING }` and lifecycle setters used by the Android coordinator.
- Produces: `BoopPcmRingBuffer.write(short[], int)` and `snapshot() -> short[]` in chronological order.

- [ ] **Step 1: Write the transcript-normalizer tests first**

Create `source-test/BoopWakeTranscriptNormalizerTest.java` with these exact cases:

```java
package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public final class BoopWakeTranscriptNormalizerTest {
    @Test public void stripsOnlyLeadingWakeWord() {
        assertEquals("pause the music",
                BoopWakeTranscriptNormalizer.stripLeadingWakeWord("boop pause the music"));
        assertEquals("turn the lamp off",
                BoopWakeTranscriptNormalizer.stripLeadingWakeWord("Boop, turn the lamp off"));
        assertEquals("",
                BoopWakeTranscriptNormalizer.stripLeadingWakeWord("  BOOP!  "));
    }

    @Test public void leavesOrdinaryAndLaterBoopUntouched() {
        assertEquals("pause the music",
                BoopWakeTranscriptNormalizer.stripLeadingWakeWord("pause the music"));
        assertEquals("tell me why boop is a funny word",
                BoopWakeTranscriptNormalizer.stripLeadingWakeWord("tell me why boop is a funny word"));
        assertEquals("boopity boop",
                BoopWakeTranscriptNormalizer.stripLeadingWakeWord("boopity boop"));
    }
}
```

- [ ] **Step 2: Write the refractory-gate tests first**

Create `source-test/BoopWakeTriggerGateTest.java`:

```java
package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public final class BoopWakeTriggerGateTest {
    @Test public void blocksDuplicatesForExactlyTheRefractoryWindow() {
        BoopWakeTriggerGate gate = new BoopWakeTriggerGate();
        assertTrue(gate.accept(10_000L));
        assertFalse(gate.accept(10_749L));
        assertTrue(gate.accept(10_750L));
    }
}
```

- [ ] **Step 3: Write the state-machine tests first**

Create `source-test/BoopWakeSessionStateTest.java`. Cover foreground/permission/support gating, wake capture, processing, TTS, tap, settings and fail-closed behavior:

```java
package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public final class BoopWakeSessionStateTest {
    private BoopWakeSessionState readyState() {
        BoopWakeSessionState state = new BoopWakeSessionState();
        state.setForeground(true);
        state.setMicrophonePermission(true);
        state.setRecognitionSupported(true);
        return state;
    }

    @Test public void armsOnlyWhenForegroundPermissionAndRecognitionAreReady() {
        BoopWakeSessionState state = new BoopWakeSessionState();
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
        state.setForeground(true);
        state.setMicrophonePermission(true);
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
        state.setRecognitionSupported(true);
        assertEquals(BoopWakeSessionState.State.ARMED, state.state());
        state.setForeground(false);
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
    }

    @Test public void wakeCaptureHasAThreeSecondDeadlineThenProcessing() {
        BoopWakeSessionState state = readyState();
        assertTrue(state.acceptWake(5_000L));
        assertEquals(BoopWakeSessionState.State.WAKE_CAPTURE, state.state());
        assertEquals(8_000L, state.commandDeadlineMs());
        state.markProcessing();
        assertEquals(BoopWakeSessionState.State.PROCESSING, state.state());
        state.finishProcessing();
        assertEquals(BoopWakeSessionState.State.ARMED, state.state());
    }

    @Test public void ttsTapAndSettingsAlwaysDisarmTheWakeEngine() {
        BoopWakeSessionState state = readyState();
        state.setTtsSpeaking(true);
        assertEquals(BoopWakeSessionState.State.SPEAKING, state.state());
        state.setTtsSpeaking(false);
        assertEquals(BoopWakeSessionState.State.ARMED, state.state());
        state.setTapListening(true);
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
        state.setTapListening(false);
        assertEquals(BoopWakeSessionState.State.ARMED, state.state());
        state.setVoiceSettingsOpen(true);
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
        state.setVoiceSettingsOpen(false);
        assertEquals(BoopWakeSessionState.State.ARMED, state.state());
    }

    @Test public void failureStaysTapOnlyForTheSession() {
        BoopWakeSessionState state = readyState();
        state.failWakeSession();
        assertTrue(state.wakeFailed());
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
        state.setForeground(false);
        state.setForeground(true);
        assertEquals(BoopWakeSessionState.State.DISARMED, state.state());
    }
}
```

- [ ] **Step 4: Write the PCM ring-buffer tests first**

Create `source-test/BoopPcmRingBufferTest.java`:

```java
package com.boop.alpha1;

import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

public final class BoopPcmRingBufferTest {
    @Test public void snapshotIsChronologicalBeforeAndAfterWrap() {
        BoopPcmRingBuffer ring = new BoopPcmRingBuffer(5);
        ring.write(new short[]{1, 2, 3}, 3);
        assertArrayEquals(new short[]{1, 2, 3}, ring.snapshot());
        ring.write(new short[]{4, 5, 6, 7}, 4);
        assertArrayEquals(new short[]{3, 4, 5, 6, 7}, ring.snapshot());
    }

    @Test public void oversizedWriteKeepsNewestSamples() {
        BoopPcmRingBuffer ring = new BoopPcmRingBuffer(4);
        ring.write(new short[]{10, 11, 12, 13, 14, 15}, 6);
        assertArrayEquals(new short[]{12, 13, 14, 15}, ring.snapshot());
    }
}
```

- [ ] **Step 5: Materialize and prove the new tests are RED**

Run:

```bash
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

Expected: compile/test failure because the four new production classes do not exist yet.

- [ ] **Step 6: Implement the minimal pure classes**

`BoopWakeTranscriptNormalizer` must use a front-anchored, whole-word wake expression only:

```java
package com.boop.alpha1;

import java.util.regex.Pattern;

final class BoopWakeTranscriptNormalizer {
    private static final Pattern LEADING_BOOP =
            Pattern.compile("(?i)^\\s*boop\\b[\\s,;:!?.-]*");

    private BoopWakeTranscriptNormalizer() { }

    static String stripLeadingWakeWord(String transcript) {
        if (transcript == null) return "";
        return LEADING_BOOP.matcher(transcript).replaceFirst("").trim();
    }
}
```

`BoopWakeTriggerGate`:

```java
package com.boop.alpha1;

final class BoopWakeTriggerGate {
    static final long REFRACTORY_MS = 750L;
    private long lastAcceptedMs = Long.MIN_VALUE;

    boolean accept(long elapsedMs) {
        if (lastAcceptedMs != Long.MIN_VALUE
                && elapsedMs - lastAcceptedMs < REFRACTORY_MS) {
            return false;
        }
        lastAcceptedMs = elapsedMs;
        return true;
    }
}
```

`BoopWakeSessionState` must use `COMMAND_WINDOW_MS = 3_000L`; `reevaluate()` must never arm while foreground, permission, support, settings, tap or TTS blockers say otherwise. `acceptWake()` is valid only from `ARMED`; `markProcessing()` is valid only from `WAKE_CAPTURE`; `failWakeSession()` latches failure until a new coordinator object is created on a clean lifecycle start.

`BoopPcmRingBuffer` must copy only the newest samples into a fixed-size `short[]` ring and return an independent chronological snapshot.

- [ ] **Step 7: Re-run JVM tests and confirm GREEN**

Run the same materialize + Gradle command. Expected: all existing and four new test classes pass.

- [ ] **Step 8: Commit the pure wake core**

```bash
git add source/BoopWakeTranscriptNormalizer.java source/BoopWakeTriggerGate.java source/BoopWakeSessionState.java source/BoopPcmRingBuffer.java source-test/BoopWakeTranscriptNormalizerTest.java source-test/BoopWakeTriggerGateTest.java source-test/BoopWakeSessionStateTest.java source-test/BoopPcmRingBufferTest.java
git commit -m "test: lock BOOP wake state and audio primitives"
```

---

### Task 2: Pin sherpa-onnx and deterministic wake assets

**Files:**
- Create: `wake-assets/boop-kws/keywords_raw.txt`
- Create: `wake-assets/boop-kws/keywords.txt`
- Create: `scripts/fetch-wake-assets.sh`
- Modify: `scripts/materialize-android.sh`
- Modify: `source/app-build.gradle`
- Modify: `.gitignore`
- Create: `tests/test_alpha64_wake_assets.py`

**Interfaces:**
- Produces materialized `app/libs/sherpa-onnx-1.13.7.aar`.
- Produces materialized `app/src/main/assets/boop-kws/{encoder,decoder,joiner,tokens,keywords}`.
- Build-time only: fetches official sherpa release assets; no runtime download or network dependency.

- [ ] **Step 1: Write source-level asset tests first**

Create `tests/test_alpha64_wake_assets.py` asserting all of the following strings/files before production changes:

```python
import unittest
from pathlib import Path

class Alpha64WakeAssetsTest(unittest.TestCase):
    def test_wake_dependencies_are_pinned(self):
        fetch = Path('scripts/fetch-wake-assets.sh').read_text(encoding='utf-8')
        self.assertIn('SHERPA_VERSION="1.13.7"', fetch)
        self.assertIn('c4ef49e309f24fcee5c106b8a279481aaecaabb078cd37b2cd6e9a62cc8a73c8', fetch)
        self.assertIn('sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile.tar.bz2', fetch)
        self.assertIn('checksum.txt', fetch)

    def test_only_boop_is_configured_as_the_raw_keyword(self):
        raw = Path('wake-assets/boop-kws/keywords_raw.txt').read_text(encoding='utf-8').strip()
        self.assertEqual('BOOP :1.5 #0.25 @BOOP', raw)

    def test_materializer_places_aar_and_runtime_model_assets(self):
        text = Path('scripts/materialize-android.sh').read_text(encoding='utf-8')
        self.assertIn('bash scripts/fetch-wake-assets.sh "$ROOT/app"', text)
        gradle = Path('source/app-build.gradle').read_text(encoding='utf-8')
        self.assertIn("implementation files('libs/sherpa-onnx-1.13.7.aar')", gradle)
        self.assertIn("org.jetbrains.kotlin:kotlin-stdlib:1.7.20", gradle)

if __name__ == '__main__':
    unittest.main()
```

- [ ] **Step 2: Run only this test and confirm RED**

```bash
python3 -m unittest tests/test_alpha64_wake_assets.py -v
```

Expected: failures because the fetch script and keyword files are absent.

- [ ] **Step 3: Add the raw keyword and generate the tokenized keyword deterministically**

Create exactly:

```text
BOOP :1.5 #0.25 @BOOP
```

in `wake-assets/boop-kws/keywords_raw.txt`.

Download/extract the pinned English model into `.cache/boop-wake`, then generate `keywords.txt` from that exact model:

```bash
python3 -m pip install 'sherpa-onnx==1.13.7'
sherpa-onnx-cli text2token \
  --tokens .cache/boop-wake/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile/tokens.txt \
  --tokens-type bpe \
  --bpe-model .cache/boop-wake/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile/bpe.model \
  wake-assets/boop-kws/keywords_raw.txt \
  wake-assets/boop-kws/keywords.txt
```

Commit the generated text file; do not hand-type BPE pieces.

- [ ] **Step 4: Implement the pinned fetch script**

`scripts/fetch-wake-assets.sh` must use these exact pins:

```bash
#!/usr/bin/env bash
set -euo pipefail

DEST="${1:?usage: fetch-wake-assets.sh <android-app-dir>}"
CACHE=".cache/boop-wake"
SHERPA_VERSION="1.13.7"
AAR="sherpa-onnx-${SHERPA_VERSION}.aar"
AAR_SHA256="c4ef49e309f24fcee5c106b8a279481aaecaabb078cd37b2cd6e9a62cc8a73c8"
MODEL_ARCHIVE="sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile.tar.bz2"
MODEL_DIR="${CACHE}/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile"
BASE="https://github.com/k2-fsa/sherpa-onnx/releases/download"

mkdir -p "$CACHE" "$DEST/libs" "$DEST/src/main/assets/boop-kws"

if [ ! -f "$CACHE/$AAR" ]; then
  curl -fL --retry 3 -o "$CACHE/$AAR" "$BASE/v${SHERPA_VERSION}/$AAR"
fi
printf '%s  %s\n' "$AAR_SHA256" "$CACHE/$AAR" | sha256sum -c -

if [ ! -f "$CACHE/$MODEL_ARCHIVE" ]; then
  curl -fL --retry 3 -o "$CACHE/$MODEL_ARCHIVE" "$BASE/kws-models/$MODEL_ARCHIVE"
fi
curl -fsSL "$BASE/kws-models/checksum.txt" -o "$CACHE/checksum.txt"
MODEL_SHA256="$(awk -v name="$MODEL_ARCHIVE" '$2 == name {print $1}' "$CACHE/checksum.txt")"
test -n "$MODEL_SHA256"
printf '%s  %s\n' "$MODEL_SHA256" "$CACHE/$MODEL_ARCHIVE" | sha256sum -c -

if [ ! -d "$MODEL_DIR" ]; then
  tar -xjf "$CACHE/$MODEL_ARCHIVE" -C "$CACHE"
fi

cp "$CACHE/$AAR" "$DEST/libs/$AAR"
for file in \
  encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx \
  decoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx \
  joiner-epoch-12-avg-2-chunk-16-left-64.int8.onnx \
  tokens.txt; do
  test -s "$MODEL_DIR/$file"
  cp "$MODEL_DIR/$file" "$DEST/src/main/assets/boop-kws/$file"
done
cp wake-assets/boop-kws/keywords.txt "$DEST/src/main/assets/boop-kws/keywords.txt"
```

- [ ] **Step 5: Wire materialization and Gradle**

Append after the normal Java/test overlay in `scripts/materialize-android.sh`:

```bash
bash scripts/fetch-wake-assets.sh "$ROOT/app"
```

Add to `source/app-build.gradle` dependencies:

```gradle
implementation files('libs/sherpa-onnx-1.13.7.aar')
implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.7.20'
```

Add `.cache/boop-wake/` to `.gitignore` so the AAR/model archive and expanded model are never committed.

- [ ] **Step 6: Run asset tests and materialize once**

```bash
python3 -m unittest tests/test_alpha64_wake_assets.py -v
bash scripts/materialize-android.sh
test -s boop-build/BOOP-Alpha1/app/libs/sherpa-onnx-1.13.7.aar
test -s boop-build/BOOP-Alpha1/app/src/main/assets/boop-kws/encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx
test -s boop-build/BOOP-Alpha1/app/src/main/assets/boop-kws/keywords.txt
```

Expected: all checks pass.

- [ ] **Step 7: Run Android unit tests after the dependency is present**

```bash
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

Expected: all existing and Task 1 tests pass.

- [ ] **Step 8: Commit deterministic wake dependencies**

```bash
git add .gitignore wake-assets/boop-kws scripts/fetch-wake-assets.sh scripts/materialize-android.sh source/app-build.gradle tests/test_alpha64_wake_assets.py
git commit -m "build: pin BOOP local wake dependencies"
```

---

### Task 3: Build the supplied-audio speech-recognition seam

**Files:**
- Create: `source/BoopWakeRecognitionIntent.java`
- Create: `source/BoopWakeAudioSession.java`
- Create: `source-test/BoopWakeRecognitionIntentTest.java`
- Create: `tests/test_alpha64_wake_recognition_surface.py`

**Interfaces:**
- Produces: `BoopWakeRecognitionIntent.build(Locale, ParcelFileDescriptor) -> Intent`.
- Produces constants `SAMPLE_RATE_HZ = 16000`, `CHANNEL_COUNT = 1`, `ENCODING = AudioFormat.ENCODING_PCM_16BIT`.
- Produces: `BoopWakeAudioSession.audioSource()`, `finishCapture()`, `close()`.

- [ ] **Step 1: Write a JVM/source test for exact recognition metadata**

Create `tests/test_alpha64_wake_recognition_surface.py`:

```python
import unittest
from pathlib import Path

class Alpha64WakeRecognitionSurfaceTest(unittest.TestCase):
    def test_wake_intent_uses_supplied_pcm_without_changing_tap_mode(self):
        wake = Path('source/BoopWakeRecognitionIntent.java').read_text(encoding='utf-8')
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('RecognizerIntent.EXTRA_AUDIO_SOURCE', wake)
        self.assertIn('RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT', wake)
        self.assertIn('RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING', wake)
        self.assertIn('RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE', wake)
        self.assertIn('AudioFormat.ENCODING_PCM_16BIT', wake)
        self.assertIn('16_000', wake)
        self.assertIn('SpeechRecognizer.createSpeechRecognizer(this)', main)
        self.assertNotIn('createOnDeviceSpeechRecognizer', main)

if __name__ == '__main__':
    unittest.main()
```

- [ ] **Step 2: Confirm RED**

```bash
python3 -m unittest tests/test_alpha64_wake_recognition_surface.py -v
```

Expected: missing `BoopWakeRecognitionIntent.java`.

- [ ] **Step 3: Implement `BoopWakeRecognitionIntent`**

Use the same language model, locale tag, max results and partial-results setting as existing tap recognition, with only the supplied-audio extras added:

```java
package com.boop.alpha1;

import android.content.Intent;
import android.media.AudioFormat;
import android.os.ParcelFileDescriptor;
import android.speech.RecognizerIntent;
import java.util.Locale;

final class BoopWakeRecognitionIntent {
    static final int SAMPLE_RATE_HZ = 16_000;
    static final int CHANNEL_COUNT = 1;
    static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private BoopWakeRecognitionIntent() { }

    static Intent build(Locale locale, ParcelFileDescriptor audioSource) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                locale.toLanguageTag());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
        intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE, audioSource);
        intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT, CHANNEL_COUNT);
        intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING, ENCODING);
        intent.putExtra(RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE, SAMPLE_RATE_HZ);
        return intent;
    }
}
```

- [ ] **Step 4: Implement the audio-session ownership object**

`BoopWakeAudioSession` owns the read end of the pipe and one idempotent capture-stop callback. Its `close()` must call `finishCapture()` and then close the read descriptor exactly once. Use `AtomicBoolean` so `onEndOfSpeech`, `onResults`, `onError` and lifecycle teardown may race safely.

Required shape:

```java
final class BoopWakeAudioSession implements AutoCloseable {
    private final ParcelFileDescriptor audioSource;
    private final Runnable finishCapture;
    private final AtomicBoolean captureFinished = new AtomicBoolean();
    private final AtomicBoolean closed = new AtomicBoolean();

    ParcelFileDescriptor audioSource() { return audioSource; }
    void finishCapture() {
        if (captureFinished.compareAndSet(false, true)) finishCapture.run();
    }
    @Override public void close() {
        finishCapture();
        if (closed.compareAndSet(false, true)) {
            try { audioSource.close(); } catch (IOException ignored) { }
        }
    }
}
```

- [ ] **Step 5: Add a JVM test for idempotent session finish/close using a real `ParcelFileDescriptor.createPipe()` under Android unit-test stubs only if the local JVM supports it; otherwise keep ownership assertions in the Python source test and exercise the real descriptor in emulator CI.**

Do not introduce Robolectric just for this holder.

- [ ] **Step 6: Re-run source and existing speech-mode tests**

```bash
python3 -m unittest tests/test_alpha64_wake_recognition_surface.py tests/test_speech_recognizer_mode.py -v
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

Expected: green, and the existing test still proves tap mode does not force `createOnDeviceSpeechRecognizer` or `EXTRA_PREFER_OFFLINE`.

- [ ] **Step 7: Commit the recognition seam**

```bash
git add source/BoopWakeRecognitionIntent.java source/BoopWakeAudioSession.java source-test/BoopWakeRecognitionIntentTest.java tests/test_alpha64_wake_recognition_surface.py
git commit -m "feat: add BOOP supplied-audio recognition seam"
```

If the JVM cannot support a meaningful `BoopWakeRecognitionIntentTest.java`, omit that file from the commit rather than adding a fake test; the Python contract test plus emulator integration is the required coverage.

---

### Task 4: Implement the local sherpa keyword/audio controller

**Files:**
- Create: `source/BoopSherpaWakeSpotter.java`
- Create: `source/BoopWakeWordController.java`
- Create: `tests/test_alpha64_wake_controller.py`

**Interfaces:**
- `BoopSherpaWakeSpotter.accept(short[], int) -> boolean` returns true only when sherpa reports `BOOP`.
- `BoopWakeWordController.Listener.onWakeDetected(BoopWakeAudioSession, long detectedAtElapsedMs)`.
- `BoopWakeWordController.Listener.onWakeFailure(String reason)`.
- `BoopWakeWordController.arm() -> boolean`, `suspendAll()`, `shutdown()`.
- The controller owns `AudioRecord`, KWS objects, pre-roll, capture pipe writer and one worker thread; it has no Home Assistant or command-routing imports.

- [ ] **Step 1: Write the controller surface test first**

Create `tests/test_alpha64_wake_controller.py` asserting:

```python
import unittest
from pathlib import Path

class Alpha64WakeControllerTest(unittest.TestCase):
    def test_controller_is_local_audio_only(self):
        controller = Path('source/BoopWakeWordController.java').read_text(encoding='utf-8')
        self.assertIn('AudioRecord', controller)
        self.assertIn('BoopPcmRingBuffer', controller)
        self.assertIn('ParcelFileDescriptor.createPipe()', controller)
        self.assertIn('BoopWakeTriggerGate', controller)
        self.assertIn('3_000L', controller)
        self.assertNotIn('HomeAssistant', controller)
        self.assertNotIn('OpenAI', controller)
        self.assertNotIn('BoopCommandRouter', controller)

    def test_spotter_uses_only_pinned_local_model_assets(self):
        spotter = Path('source/BoopSherpaWakeSpotter.java').read_text(encoding='utf-8')
        self.assertIn('KeywordSpotter', spotter)
        self.assertIn('boop-kws/encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx', spotter)
        self.assertIn('boop-kws/decoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx', spotter)
        self.assertIn('boop-kws/joiner-epoch-12-avg-2-chunk-16-left-64.int8.onnx', spotter)
        self.assertIn('boop-kws/tokens.txt', spotter)
        self.assertIn('boop-kws/keywords.txt', spotter)

if __name__ == '__main__':
    unittest.main()
```

- [ ] **Step 2: Confirm RED**

```bash
python3 -m unittest tests/test_alpha64_wake_controller.py -v
```

- [ ] **Step 3: Implement the sherpa wrapper with explicit Java setters**

Use the v1.13.7 Kotlin AAR from Java via its generated no-arg constructors/getters/setters:

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

Construct `KeywordSpotter` with `context.getAssets()`, create one `OnlineStream`, convert PCM shorts to floats by `sample / 32768.0f`, call `acceptWaveform`, decode while `spotter.isReady(stream)`, and treat a non-empty result equal to `BOOP` case-insensitively as a trigger. Call `spotter.reset(stream)` after a trigger. `close()` must release stream then spotter.

- [ ] **Step 4: Implement `BoopWakeWordController` microphone rules**

Use:

```java
static final int SAMPLE_RATE_HZ = 16_000;
static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
static final long COMMAND_WINDOW_MS = 3_000L;
static final int PRE_ROLL_SAMPLES = SAMPLE_RATE_HZ;
```

`arm()` must:

1. Return true immediately if already armed.
2. Construct the sherpa wrapper lazily.
3. Create `AudioRecord` with at least `max(getMinBufferSize(...), 1600 * 2)` bytes.
4. Start recording and one named worker thread, `boop-wake-audio`.
5. Catch `SecurityException`, `IllegalStateException`, native/model exceptions and report one `onWakeFailure` without a retry loop.

The worker loop must read roughly 100 ms of shorts, feed the ring and spotter, and do nothing off-device until KWS says `BOOP`.

When `BoopWakeTriggerGate.accept(SystemClock.elapsedRealtime())` accepts the trigger:

1. Create a pipe with `ParcelFileDescriptor.createPipe()`.
2. Wrap the writer with `ParcelFileDescriptor.AutoCloseOutputStream`.
3. Snapshot the pre-roll and write it as little-endian signed PCM16.
4. Create `BoopWakeAudioSession` around the reader; its stop callback stops capture and closes the pipe writer.
5. Post `onWakeDetected(session, detectedAt)` through a main-thread `Handler`.
6. Stop feeding sherpa and continue forwarding subsequent mic frames to the writer.
7. Close the writer and stop/release `AudioRecord` at `detectedAt + 3_000L` even if recognition never calls back.

Do not write the trigger frame twice: once pre-roll is written, continue with the next microphone read before streaming new PCM.

`suspendAll()` must synchronously mark the worker stopped, stop/release `AudioRecord`, close any active writer/session capture, and leave the sherpa model reusable. `shutdown()` additionally releases the sherpa objects and worker references.

- [ ] **Step 5: Add diagnostic logging without user-facing error UI**

Use tag `BOOP-Wake` and concise reasons such as `model init failed`, `audio record failed`, `pipe failed`. No toast, dialog or spoken wake failure is allowed.

- [ ] **Step 6: Run source tests, materialize and compile the real AAR integration**

```bash
python3 -m unittest tests/test_alpha64_wake_controller.py tests/test_alpha64_wake_assets.py -v
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
gradle -p boop-build/BOOP-Alpha1 :app:assembleDebug --stacktrace
```

Expected: Java compiles against the pinned sherpa AAR and APK assembly succeeds.

- [ ] **Step 7: Inspect the APK for required native/model assets**

```bash
APK=boop-build/BOOP-Alpha1/app/build/outputs/apk/debug/app-debug.apk
unzip -l "$APK" | grep 'sherpa-onnx-jni'
unzip -l "$APK" | grep 'assets/boop-kws/encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx'
unzip -l "$APK" | grep 'assets/boop-kws/keywords.txt'
```

Expected: all three checks find packaged content.

- [ ] **Step 8: Commit the controller**

```bash
git add source/BoopSherpaWakeSpotter.java source/BoopWakeWordController.java tests/test_alpha64_wake_controller.py
git commit -m "feat: add local BOOP keyword spotter"
```

---

### Task 5: Add the wake coordinator and preserve the sacred tap path

**Files:**
- Create: `source/BoopWakeSessionCoordinator.java`
- Create: `source-test/BoopWakeSessionCoordinatorTest.java`
- Modify: `source/MainActivity.java`
- Create: `tests/test_alpha64_wake_integration.py`

**Interfaces:**
- `BoopWakeSessionCoordinator.Engine.arm() -> boolean`, `suspendAll()`, `shutdown()`.
- Coordinator methods: `setForeground(boolean)`, `setMicrophonePermission(boolean)`, `setRecognitionSupported(boolean)`, `setVoiceSettingsOpen(boolean)`, `onTapStarted()`, `onTapFinished()`, `onTtsStarting()`, `onTtsFinished()`, `onWakeDetected(long) -> boolean`, `markWakeProcessing()`, `finishWakeProcessing()`, `failWakeSession()`, `shutdown()`.
- `MainActivity` adds `RecognitionMode { NONE, TAP, WAKE }` and one current `BoopWakeAudioSession`.

- [ ] **Step 1: Write coordinator tests with a fake engine first**

Create tests proving:

- ready state arms exactly once;
- foreground false suspends;
- tap start suspends and tap finish re-arms;
- TTS start suspends and TTS finish re-arms;
- settings open suspends and close re-arms;
- engine `arm()` false latches wake failure and does not spin/retry;
- `WAKE_CAPTURE` does not call `suspendAll()` because the controller must keep feeding the supplied-audio pipe;
- `PROCESSING` is disarmed until either TTS starts or processing settles.

Use a fake `Engine` with integer `armCalls`, `suspendCalls`, `shutdownCalls` fields.

- [ ] **Step 2: Run coordinator tests and confirm RED**

```bash
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

Expected: coordinator class missing.

- [ ] **Step 3: Implement the coordinator as a state-to-engine adapter**

Core behavior:

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

Every public state-changing method updates `BoopWakeSessionState` then calls `syncEngine()`. `onWakeDetected(long)` calls `state.acceptWake(long)` and only returns true when the activity still considers wake mode armed. `shutdown()` calls `engine.shutdown()` exactly once.

- [ ] **Step 4: Write the MainActivity integration source test first**

Create `tests/test_alpha64_wake_integration.py` asserting:

```python
import unittest
from pathlib import Path

class Alpha64WakeIntegrationTest(unittest.TestCase):
    def test_tap_path_stays_normal_and_wake_path_is_explicit(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('enum RecognitionMode', main)
        self.assertIn('RecognitionMode.TAP', main)
        self.assertIn('RecognitionMode.WAKE', main)
        self.assertIn('BoopWakeSessionCoordinator', main)
        self.assertIn('BoopWakeTranscriptNormalizer.stripLeadingWakeWord', main)
        self.assertIn('BoopWakeRecognitionIntent.build', main)
        self.assertIn('SpeechRecognizer.createSpeechRecognizer(this)', main)
        self.assertIn('commandRouter.process(transcript)', main)

    def test_existing_local_intercepts_still_precede_house_router(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        settings = main.find('BoopVoiceSettingsIntent.matches(transcript)')
        voice = main.find('voiceController.maybeChangeVoice(transcript)')
        router = main.find('commandRouter.process(transcript)')
        self.assertGreaterEqual(settings, 0)
        self.assertGreater(voice, settings)
        self.assertGreater(router, voice)

if __name__ == '__main__':
    unittest.main()
```

Confirm RED before touching `MainActivity`.

- [ ] **Step 5: Add wake objects without changing the existing router construction**

In `MainActivity`, add fields:

```java
private enum RecognitionMode { NONE, TAP, WAKE }
private RecognitionMode recognitionMode = RecognitionMode.NONE;
private BoopWakeWordController wakeWordController;
private BoopWakeSessionCoordinator wakeCoordinator;
private BoopWakeAudioSession wakeAudioSession;
private boolean wakeSupportCheckInFlight = false;
```

Construct `BoopWakeWordController` after the normal speech recognizer exists. Adapt it to `BoopWakeSessionCoordinator.Engine`. The controller listener must post to the UI thread; on accepted wake call `wakeFaceForInteraction()` immediately and then start wake recognition with the delivered `BoopWakeAudioSession`.

Do not change `HomeAssistantClient`, `generalAssistant`, `commandRouter`, device setup or discovery construction.

- [ ] **Step 6: Preserve tap-to-talk as a distinct normal-mic entry point**

Rename no existing user behavior. In `beginTapToSpeak()` after permission is confirmed:

```java
wakeCoordinator.onTapStarted();
recognitionMode = RecognitionMode.TAP;
startListening();
```

`startListening()` retains the current `ACTION_RECOGNIZE_SPEECH` intent with no `EXTRA_AUDIO_SOURCE`. It still stops current TTS, marks `listening`, animates face alpha and calls `recognizer.startListening(intent)`.

On tap `onResults`/`onError`, call `wakeCoordinator.onTapFinished()` after recognition owns no mic. Tap errors keep the existing spoken `Speech error ...` behavior.

- [ ] **Step 7: Add wake recognition using the supplied pipe**

Create `startWakeRecognition(BoopWakeAudioSession session)`:

```java
private void startWakeRecognition(BoopWakeAudioSession session) {
    wakeAudioSession = session;
    recognitionMode = RecognitionMode.WAKE;
    listening = true;
    wakeFaceForInteraction();
    face.animate().alpha(0.78f).setDuration(120).start();
    Intent intent = BoopWakeRecognitionIntent.build(Locale.getDefault(), session.audioSource());
    recognizer.startListening(intent);
}
```

If `recognizer` is null or `startListening` throws, close the session, restore face alpha, call `wakeCoordinator.failWakeSession()` and return silently to tap-only mode.

In `onEndOfSpeech()`, if mode is `WAKE`, call `wakeAudioSession.finishCapture()` so the writer can EOF early.

In wake `onError`, close the session, clear `listening`/mode, restore face alpha, and call `wakeCoordinator.finishWakeProcessing()` or an equivalent quiet re-arm transition. Do not speak the speech error for wake mode.

In wake `onResults`:

1. close the session and clear `listening`/mode;
2. choose the same first non-empty recognizer result as tap;
3. normalize it with `BoopWakeTranscriptNormalizer.stripLeadingWakeWord`;
4. if empty, quietly call `wakeCoordinator.finishWakeProcessing()` and re-arm;
5. otherwise call `wakeCoordinator.markWakeProcessing()` and pass the normalized text to the exact existing `handleRecognizedSpeech(String)` method.

No wake-specific command parser is allowed.

- [ ] **Step 8: Add API 33 recognition-support probing without altering tap recognition**

When foreground + permission are present and `recognizer != null`, create a temporary `ParcelFileDescriptor.createPipe()`, immediately close the writer, build a `BoopWakeRecognitionIntent` with the read side, and call:

```java
recognizer.checkRecognitionSupport(
        probeIntent,
        getMainExecutor(),
        new RecognitionSupportCallback() {
            @Override public void onSupportResult(RecognitionSupport support) {
                closeProbe();
                wakeSupportCheckInFlight = false;
                wakeCoordinator.setRecognitionSupported(true);
            }
            @Override public void onError(int error) {
                closeProbe();
                wakeSupportCheckInFlight = false;
                wakeCoordinator.setRecognitionSupported(false);
            }
        });
```

API below 33 sets recognition support false. The Pixel room test remains authoritative because a recognizer can claim generic support while mishandling supplied audio.

- [ ] **Step 9: Run integration, routing and media regression tests**

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
```

Expected: all green.

- [ ] **Step 10: Commit coordinator and recognition integration**

```bash
git add source/BoopWakeSessionCoordinator.java source-test/BoopWakeSessionCoordinatorTest.java source/MainActivity.java tests/test_alpha64_wake_integration.py
git commit -m "feat: route BOOP wake capture into existing speech path"
```

---

### Task 6: Wire foreground, TTS and voice-settings lifecycle safely

**Files:**
- Modify: `source/MainActivity.java`
- Create: `tests/test_alpha64_wake_lifecycle.py`

**Interfaces:**
- Foreground lifecycle: `onResume()` eligible, `onPause()` disarmed.
- TTS lifecycle: `onTtsStarting()` before every `tts.speak`, `onTtsFinished()` from done/error/stop.
- Settings lifecycle: open/close mapped to wake blocker.
- Permission lifecycle: existing mic permission result updates wake eligibility without adding a new startup permission prompt.

- [ ] **Step 1: Write lifecycle source tests first**

Create `tests/test_alpha64_wake_lifecycle.py` asserting:

- `onResume` calls wake foreground true and refreshes mic/support eligibility;
- `onPause` calls foreground false before `super.onPause()`;
- `showVoiceSettings` marks settings open and `hideVoiceSettings` clears it;
- `speak` suspends wake before `tts.speak`;
- `UtteranceProgressListener` handles `onDone`, `onError`, `onStop` by re-evaluating wake;
- `onDestroy` calls `wakeCoordinator.shutdown()` and closes any active wake session;
- no `Service`, `startForegroundService` or lock-screen bypass API appears in `MainActivity`/manifest.

- [ ] **Step 2: Confirm RED**

```bash
python3 -m unittest tests/test_alpha64_wake_lifecycle.py -v
```

- [ ] **Step 3: Add foreground lifecycle**

Implement:

```java
@Override protected void onResume() {
    super.onResume();
    if (wakeCoordinator != null) {
        wakeCoordinator.setForeground(true);
        wakeCoordinator.setMicrophonePermission(
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED);
        checkWakeRecognitionSupport();
    }
}

@Override protected void onPause() {
    if (wakeCoordinator != null) wakeCoordinator.setForeground(false);
    closeWakeAudioSession();
    super.onPause();
}
```

Do not request microphone permission merely because the app opened. Existing tap flow remains the permission entry point; installs that already have permission arm automatically.

- [ ] **Step 4: Add TTS lifecycle before audio starts**

Install one `UtteranceProgressListener` on `tts`. `speak(String)` must call `wakeCoordinator.onTtsStarting()` before `tts.speak(...)`. If TTS is unavailable or `tts.speak` returns `TextToSpeech.ERROR`, call `onTtsFinished()` immediately.

Listener callbacks must return to UI thread:

```java
@Override public void onDone(String utteranceId) {
    runOnUiThread(() -> wakeCoordinator.onTtsFinished());
}
@Override public void onError(String utteranceId) {
    runOnUiThread(() -> wakeCoordinator.onTtsFinished());
}
@Override public void onStop(String utteranceId, boolean interrupted) {
    runOnUiThread(() -> wakeCoordinator.onTtsFinished());
}
```

This is intentionally broader than only assistant replies: local `This one?`, connection messages and error speech must all prevent BOOP from hearing herself.

- [ ] **Step 5: Preserve tap interruption of TTS**

`beginTapToSpeak()` claims the tap blocker before `startListening()` calls `tts.stop()`. Therefore `onStop()` may clear the TTS blocker but cannot re-arm wake while tap recognition owns the mic.

Add a coordinator JVM test for this exact ordering:

1. TTS speaking → `SPEAKING`;
2. tap starts → `DISARMED`;
3. TTS stop callback → still `DISARMED`;
4. tap finishes → `ARMED`.

- [ ] **Step 6: Wire voice settings**

Immediately after `voiceSettingsOpen = true`, call `wakeCoordinator.setVoiceSettingsOpen(true)`. Immediately after setting it false in `hideVoiceSettings()`, call `setVoiceSettingsOpen(false)`.

The overlay behavior, slider values and Done button remain otherwise untouched.

- [ ] **Step 7: Wire permission and teardown**

In the existing `REQ_RECORD_AUDIO` result branch, always update `wakeCoordinator.setMicrophonePermission(granted)` before the existing tap continuation logic.

At the top of `onDestroy()`:

```java
closeWakeAudioSession();
if (wakeCoordinator != null) {
    wakeCoordinator.shutdown();
    wakeCoordinator = null;
}
wakeWordController = null;
```

Then retain the existing recognizer/TTS/discovery/executor teardown.

- [ ] **Step 8: Run all source/JVM regressions**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

Expected: green.

- [ ] **Step 9: Commit lifecycle integration**

```bash
git add source/MainActivity.java source-test/BoopWakeSessionCoordinatorTest.java tests/test_alpha64_wake_lifecycle.py
git commit -m "feat: suspend BOOP wake listening around foreground and speech"
```

---

### Task 7: Bump code 17 and build a frozen Alpha 6.4 APK in CI

**Files:**
- Modify: `tests/test_alpha2_build_surface.py`
- Modify: `source/app-build.gradle`
- Create: `.github/workflows/build-alpha64.yml`
- Modify: `.github/workflows/verify-alpha6.yml`
- Create: `tests/test_alpha64_build_workflow.py`

**Interfaces:**
- Version: code 17, name `0.4.4-alpha6.4`.
- Dedicated full workflow artifact: `BOOP-Alpha6.4-Wake-Word-debug`.
- Freeze baseline: code16 head `3b19abfa9bfce66d0a1ddbcf97f917a765a2759d`.

- [ ] **Step 1: Change the build-surface test first and confirm RED**

Rename its version test to `test_build_is_alpha64_android36_java17` and require:

```python
self.assertIn('versionCode 17', text)
self.assertIn('versionName "0.4.4-alpha6.4"', text)
```

Run:

```bash
python3 -m unittest tests/test_alpha2_build_surface.py -v
```

Expected: exactly the version assertion fails while production remains code16.

- [ ] **Step 2: Bump production metadata**

Change only:

```gradle
versionCode 17
versionName "0.4.4-alpha6.4"
```

Re-run the build-surface test; expect green.

- [ ] **Step 3: Write the workflow-contract test first**

Create `tests/test_alpha64_build_workflow.py` requiring branch `alpha6.4-wake-word`, freeze baseline SHA, code17/name, wake asset fetch/materialize, JVM tests, assemble, APK signing verification, clean Android 16 emulator install/launch and artifact name.

- [ ] **Step 4: Create `.github/workflows/build-alpha64.yml` from the proven Alpha 6.3 workflow**

Keep the same Java 17, Android 36, Gradle 9.6, signing and clean Pixel 7 Pro emulator steps. Change names/branch/version/artifact and add wake-specific checks.

The freeze step must be:

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

This must prove every pre-existing production source other than `MainActivity.java` and build metadata remains byte-for-byte code16.

After materialization add:

```bash
test -s boop-build/BOOP-Alpha1/app/libs/sherpa-onnx-1.13.7.aar
test -s boop-build/BOOP-Alpha1/app/src/main/assets/boop-kws/keywords.txt
```

APK inspection must require:

```bash
grep -q "versionCode='17'" badging.txt
grep -q "versionName='0.4.4-alpha6.4'" badging.txt
unzip -l "$APK" | grep -q 'assets/boop-kws/keywords.txt'
unzip -l "$APK" | grep -q 'sherpa-onnx-jni'
```

Artifact upload name:

```yaml
name: BOOP-Alpha6.4-Wake-Word-debug
```

- [ ] **Step 5: Add Alpha 6.4 to the generic Alpha 6 verifier**

Append `"alpha6.4-wake-word"` to the existing `verify-alpha6.yml` push branches; do not remove older branches.

- [ ] **Step 6: Run all repository tests before pushing**

```bash
python3 -m unittest bridge/test_boop_wyoming_bridge.py -v
python3 -m unittest discover -s tests -p 'test_*.py' -v
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
gradle -p boop-build/BOOP-Alpha1 :app:assembleDebug --stacktrace
```

Expected: all green.

- [ ] **Step 7: Commit version and CI**

```bash
git add tests/test_alpha2_build_surface.py source/app-build.gradle .github/workflows/build-alpha64.yml .github/workflows/verify-alpha6.yml tests/test_alpha64_build_workflow.py
git commit -m "ci: fully verify BOOP Alpha 6.4 wake-word APK"
```

- [ ] **Step 8: Wait for both Alpha 6.4 GitHub Actions runs and inspect every job step**

Required green evidence:

- source freeze proof;
- bridge/source regression tests;
- deterministic wake asset verification;
- Android materialization;
- Java 17 / SDK 36 / Gradle 9.6;
- Android JVM tests;
- permanent signing key validation;
- code17 assembly and package inspection;
- clean Android 16 Pixel 7 Pro emulator install/launch with surviving process;
- artifact upload.

Do not claim the wake word acoustically works from CI.

---

### Task 8: Fresh verification and Pixel room handoff

**Files:**
- No production changes unless a verified failure requires returning to the relevant earlier task under systematic debugging.

**Interfaces:**
- Produces one signed raw `.apk` extracted from the successful dedicated workflow artifact.
- Pixel 7 Pro field result decides whether Alpha 6.4 is known-good.

- [ ] **Step 1: Run the verification-before-completion checklist on the final head**

Confirm the final branch diff against code16 contains only:

- approved wake production classes;
- `MainActivity.java` wake lifecycle/integration;
- `app-build.gradle` dependency/version changes;
- build/materialization scripts and wake assets;
- tests/docs/workflows.

Any unexpected existing production-source change is a release blocker.

- [ ] **Step 2: Download the successful workflow artifact and extract the raw APK**

Expected user-facing filename:

```text
BOOP-Alpha6.4-Wake-Word-code17.apk
```

Verify artifact ZIP digest, raw APK SHA256, ZIP integrity, `AndroidManifest.xml`, `classes.dex`, code17/name and signing before sharing the raw `.apk`.

- [ ] **Step 3: Give the Pixel acceptance checklist, not a generic test request**

Run in this order:

1. Install over code16 so the existing mic/HA permissions and settings persist.
2. Open BOOP and wait for normal idle black.
3. Say `BOOP`; eyes should wake immediately; say `pause the music` within 3 seconds.
4. Say `BOOP play the music` as one continuous sentence.
5. Say `BOOP turn the lamp off` continuously.
6. Say `BOOP why are oranges orange?` and verify the normal HA `NO_MATCH` → general assistant path.
7. Try whisper, normal and loud `BOOP` from normal room distance.
8. Leave ordinary TV/conversation audio playing and watch for false triggers.
9. Ask BOOP a reply containing the word `BOOP`; her own TTS must not wake herself.
10. Tap the face before and after wake tests; tap speech must feel exactly like code16.
11. Long-hold for member berry; behavior must be unchanged.
12. Say `voice settings`, move Pitch/Cadence, close it, and verify wake resumes afterward.
13. Say `change your voice`; verify `This one?` and wake resumes after TTS.
14. Background/close the app and say `BOOP`; nothing should happen. Reopen and verify it arms again.

- [ ] **Step 4: Treat failures by subsystem, not by guessing**

- Wake never fires but tap works: inspect model/AudioRecord/KWS init logs and Pixel mic permission.
- Eyes wake but continuous command loses its first word: inspect pre-roll/pipe timing and supplied-audio recognizer behavior.
- Wake fires twice: inspect trigger gate and session re-arm timing.
- BOOP self-triggers from TTS: inspect `onTtsStarting` ordering and TTS progress callbacks.
- Tap regresses: stop and revert the wake handoff seam; tap is higher priority than wake.
- Media/HA/assistant routing regresses: compare against code16 freeze and do not modify routing to compensate.

Use `superpowers:systematic-debugging` before any corrective code change.

- [ ] **Step 5: Only after Ryan reports room success, mark Alpha 6.4 known-good**

Create an immutable checkpoint branch from the exact field-tested final commit, e.g. `checkpoint-alpha6.4-known-good`. Do not move `checkpoint-alpha6-known-good` or any earlier checkpoint.

---

## Plan self-review checklist

Before execution begins, verify these facts against the approved spec:

- Wake is foreground-only and local: Tasks 2, 4, 6.
- Exact word `BOOP`, 750 ms refractory and 3-second max: Tasks 1, 2, 4.
- Continuous sentence preservation via pre-roll + one physical mic + supplied-audio pipe: Tasks 3, 4, 5.
- Eyes wake immediately with no verbal acknowledgement: Task 5.
- Tap stays normal and permanent: Tasks 5, 6, 7 freeze proof.
- TTS cannot self-trigger and tap can still interrupt it: Task 6.
- Voice settings suspend wake: Task 6.
- Failure is tap-only, quiet and non-looping: Tasks 1, 4, 5.
- Direct media/HA/general assistant are untouched: Global Constraints, Tasks 5 and 7 freeze proof.
- App close/background releases wake resources: Task 6.
- Pixel-first acoustic acceptance is explicit and CI is not treated as proof: Tasks 7 and 8.
- Code17 raw APK delivery is verified before field test: Tasks 7 and 8.
