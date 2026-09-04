# BOOP Alpha 6.4 Wake Word Design

Date: 2026-09-04
Branch: `alpha6.4-wake-word`
Base: `alpha6.3-voice-sliders` at `3b19abfa9bfce66d0a1ddbcf97f917a765a2759d`
Status: approved design; implementation not started

## Goal

Add a local wake word to the dedicated Pixel 7 Pro prototype without disturbing the field-tested BOOP interaction and routing stack.

The wake word is exactly **BOOP**.

Both user flows must work:

- `BOOP` → eyes wake → user speaks a command within the next 3 seconds.
- `BOOP pause the music` as one continuous sentence.

Tap-to-talk remains permanent and must continue to behave exactly as it does in Alpha 6.3.

## Product rules

- Pixel-first. Portability to other devices is explicitly deferred until BOOP is otherwise mature.
- Wake-word listening exists only while the BOOP app is open/foreground-active.
- App closed or backgrounded means the wake microphone is released.
- No lock-screen bypass work.
- No background service.
- No cloud wake-word detection and no wake audio sent to OpenAI/Home Assistant.
- No spoken acknowledgement such as “yes?” and no wake beep in Alpha 6.4.
- The visual acknowledgement is the eyes waking immediately.
- While BOOP TTS is speaking, wake-word listening is suspended to prevent BOOP triggering herself.
- Tap-to-talk remains available as the reliable fallback and may still interrupt BOOP as it does now.

## Chosen approach

Use **sherpa-onnx keyword spotting** locally on the Pixel for the wake detector, with an English open-vocabulary KWS model and the custom keyword `BOOP`.

Use one app-owned `AudioRecord` stream while armed. Feed that stream to sherpa-onnx and simultaneously retain a small rolling PCM pre-roll buffer. On wake detection, continue capturing the same stream for the command so a continuous phrase is not chopped at the wake-word boundary.

For command transcription, feed the captured/streamed PCM to Android `SpeechRecognizer` using `RecognizerIntent.EXTRA_AUDIO_SOURCE` on API 33+, including channel count, encoding and sampling-rate metadata. Before enabling wake-word mode, check recognizer support for the required recognition intent. The Pixel is the acceptance device.

If the installed recognizer does not support supplied audio reliably, wake-word mode is disabled for that session rather than silently degrading into a version that breaks continuous-sentence commands. Tap-to-talk still works.

Reference APIs:

- Android `RecognizerIntent.EXTRA_AUDIO_SOURCE` (API 33+)
- Android `SpeechRecognizer.checkRecognitionSupport` (API 33+)
- sherpa-onnx open-vocabulary keyword spotting / Android KWS

## Why this approach

### Recommended: sherpa-onnx KWS + Android transcription

Pros:
- Wake detection is entirely local.
- No account, API key or wake-word cloud dependency.
- Custom keyword `BOOP` does not require training a bespoke model.
- Existing Android speech recognition and downstream BOOP routing remain in place.
- A rolling audio buffer lets one-breath commands survive the wake transition.

Cons:
- Adds native libraries and a small KWS model to the APK.
- Android supplied-audio recognition support must be proven on the Pixel.

### Rejected for Alpha 6.4: full sherpa-onnx ASR

This would make BOOP own both wake detection and full speech-to-text. It is more portable, but it duplicates a speech-recognition stack that currently works and increases model size, tuning work and regression surface.

### Rejected for Alpha 6.4: cloud or continuously-running Android SpeechRecognizer wake detection

The wake path must remain local. Android `SpeechRecognizer` is also not intended to be abused as a permanent wake-word loop.

## Component boundaries

### `BoopWakeWordController`

Owns only wake-word audio and KWS responsibilities:

- create/release `AudioRecord`
- load/unload sherpa-onnx KWS model
- maintain rolling PCM pre-roll
- feed PCM frames to keyword spotter
- emit one `onWakeDetected(...)` callback
- enforce a 750 ms refractory period after an accepted wake trigger
- suspend/resume cleanly when another BOOP feature owns the microphone

It must not know about Home Assistant, media commands, ChatGPT, voice settings or TTS text.

### `BoopWakeSessionCoordinator`

Owns the wake lifecycle/state machine and microphone handoff:

- arm only while the activity is foreground-active
- arbitrate microphone ownership between wake detector and normal/tap speech recognition
- wake the eyes on accepted trigger
- start wake-command transcription using the existing recognizer plumbing with supplied audio
- suspend wake detection while BOOP is speaking
- re-arm after recognition, command handling or TTS completes
- fail closed to tap-only behavior when wake initialization is unavailable

It must not implement command semantics.

### `BoopWakeTranscriptNormalizer`

Receives the transcript from a wake-initiated recognition session and removes only a leading wake-word token/phrase when present.

Examples:

- `boop pause the music` → `pause the music`
- `Boop, turn the lamp off` → `turn the lamp off`
- `pause the music` → unchanged
- transcript containing `boop` later in the sentence → unchanged
- transcript containing only `boop` → empty command; quietly re-arm

Do not add broad fuzzy text rewriting. The normalizer exists only to keep the wake word out of the downstream command router.

### Existing BOOP command path

After wake transcription and wake-word stripping, the resulting text re-enters the same existing transcript-handling seam used by tap-to-talk.

The existing local voice-settings / voice-cycle interception remains first. Remaining text then enters the existing `BoopCommandRouter`; its house path keeps the Alpha 5 direct-media behavior and normal Home Assistant handling, and only a genuine HA `NO_MATCH` falls through to the general assistant.

No media, HA or ChatGPT routing rules move into the wake subsystem, and their existing order must not change.

## Audio flow

### Armed state

- `AudioRecord` captures mono PCM suitable for the selected KWS model and Android recognizer.
- sherpa-onnx receives frames continuously.
- a rolling pre-roll buffer retains roughly the last 0.8–1.0 seconds of PCM.
- no audio leaves the Pixel merely because BOOP is armed.

### Wake trigger

When the KWS detector accepts `BOOP`:

1. enforce the 750 ms refractory period so the same utterance cannot double-trigger
2. wake BOOP’s eyes immediately
3. retain pre-roll containing the wake-word boundary
4. begin/continue command capture without reopening the physical microphone
5. stream the supplied PCM into Android speech recognition
6. allow a maximum 3-second post-wake command window
7. finish earlier when the recognizer reaches a valid end-of-speech result
8. normalize the transcript by removing a leading `BOOP` when present
9. send the remaining text through the existing BOOP transcript path

The maximum 3 seconds is a command window, not a mandatory delay. If the Pixel recognizer cannot provide an acceptably snappy early endpoint with supplied audio, that is a room-test failure to solve before Alpha 6.4 is accepted; do not redefine the 3-second maximum as a fixed wait.

### Separated command

Example:

`BOOP` → eyes wake → short pause → `pause the music`

The command must still be accepted provided it arrives within the 3-second post-wake window.

### Continuous command

Example:

`BOOP pause the music`

The pre-roll plus uninterrupted app-owned mic stream must preserve `pause the music`; the transition to command recognition must not require the user to repeat or pause after the wake word.

## State machine

### `DISARMED`

App is not foreground-active, microphone permission is unavailable, required wake capability is unsupported, or wake subsystem has failed for this session.

Tap-to-talk remains available whenever the existing app path allows it.

### `ARMED`

App is foreground-active and BOOP is waiting locally for `BOOP`.

The eyes may be in their normal idle/black state; the audio detector remains armed.

### `WAKE_CAPTURE`

`BOOP` accepted. Eyes wake immediately. BOOP captures/streams the command for up to 3 seconds.

### `PROCESSING`

Recognition produced a command and it has entered the existing BOOP transcript/router path.

Wake detector does not contend for the mic while a recognition session owns it.

### `SPEAKING`

BOOP TTS is active. Wake detector is suspended so BOOP cannot hear her own synthetic voice and self-trigger.

### Return to `ARMED`

Re-arm when:

- wake command recognition completes or errors
- command processing completes without TTS
- TTS completes, errors or is interrupted
- voice-cycle confirmation speech completes
- any other existing speech session releases mic ownership

If re-arm fails, remain tap-only until the next clean app lifecycle start; do not enter a retry loop.

## Interaction with existing features

### Tap-to-talk

Tap-to-talk is sacred and permanent.

When the user taps:

- wake detector yields/releases the mic first
- existing tap recognition starts with the normal Android microphone path
- existing transcript behavior remains unchanged
- after the tap session is fully complete, wake detection re-arms if the app is still eligible

No wake word is required for tap sessions.

### Member berry

No behavior change. Wake-word work must not remap the long-hold gesture or member-berry animation.

### Voice settings overlay

While the voice-settings overlay is open, wake detection is suspended because the current interaction surface intentionally blocks ordinary face interaction. Closing the overlay re-arms wake detection.

### Thinking animation / cloud wait

Wake-word behavior mirrors what the current tap path safely permits. The wake subsystem must not cancel, mutate or reroute a cloud request by itself. If a new local speech session is accepted during a wait, it enters the existing command path exactly like a tap session would.

### TTS

Wake detection must be suspended before BOOP begins any TTS utterance and resume only after the TTS utterance is finished, interrupted or errors.

This includes normal assistant replies and the local voice-cycle confirmation `This one?`.

## Capability and failure policy

Alpha 6.4 prioritizes preserving a known-good BOOP over making wake word work at all costs.

### Initialization checks

Wake mode requires:

- microphone permission
- foreground-active app
- successful sherpa-onnx native/model initialization
- working `AudioRecord`
- Android API level 33+
- recognizer support for the supplied-audio recognition intent

The current Pixel target satisfies the API-level requirement; actual recognizer behavior must still be field-tested.

### Failure behavior

If any wake prerequisite fails:

- no crash
- no restart loop
- no scary popup
- no change to media/HA/ChatGPT routing
- no broken tap-to-talk
- disable wake for the current session and continue as Alpha 6.3-style tap BOOP

A developer log may record the reason.

A transient runtime wake failure gets no repeated automatic recovery loop. A later clean app resume/relaunch may attempt normal initialization again.

## Privacy and resource contract

- Wake audio is processed locally on the Pixel.
- Raw armed-state audio is not sent to Home Assistant, OpenCode or OpenAI.
- Only after a wake trigger is accepted is command audio handed to the installed Android recognizer, matching the existing speech-recognition trust boundary rather than creating a new cloud wake path.
- The wake microphone is active only while the BOOP app is foreground-active and eligible.
- Leaving/closing the app releases the wake mic and model resources.
- No wake service persists in the background.

## Dependency packaging

Bundle sherpa-onnx Android runtime plus an English open-vocabulary KWS model suitable for the Pixel. Prefer an int8 English KWS model to minimize APK growth, provided Pixel detection quality remains good enough in room testing.

The keyword configuration contains only `BOOP` for Alpha 6.4. Multiple wake words, custom user wake words and a wake-word settings UI are out of scope.

## Testing strategy

### Pure logic / JVM tests

Test at minimum:

- legal state transitions
- app foreground arms and background disarms
- 3-second maximum command window
- 750 ms anti-double-trigger refractory behavior
- TTS suspend/resume
- tap mic handoff and re-arm
- voice-settings suspend/resume
- wake transcript normalization
- leading `BOOP` stripped only at the front
- empty post-strip transcript quietly re-arms
- failure state always leaves tap path usable

### Existing regression tests

Prove Alpha 6.3 behavior remains intact:

- tap-to-talk
- member berry long hold
- thinking puppet lifecycle
- voice cycling
- pitch/cadence settings
- direct media routing
- Home Assistant route
- genuine `NO_MATCH` general-assistant fallback

The wake subsystem must not be allowed to alter the direct-media/HA/ChatGPT route ordering.

### Android build / emulator verification

CI should prove:

- sherpa native libraries package successfully for the target ABI(s)
- KWS model assets are present and loadable
- app launches without crash
- wake controller can initialize/tear down without lifecycle leaks
- no microphone/resource ownership survives activity shutdown
- APK version/build metadata is correct

Emulator success is not treated as proof of acoustic wake-word quality.

### Pixel room acceptance

The Pixel 7 Pro is the truth device for Alpha 6.4.

Required room tests:

1. Normal voice: `BOOP` → eyes wake quickly → `pause the music` within 3 seconds.
2. Continuous sentence: `BOOP pause the music` in one breath.
3. Continuous non-media command: `BOOP turn the lamp off`.
4. General question: `BOOP why are oranges orange?` and verify normal HA `NO_MATCH` → assistant behavior.
5. Whisper/normal/loud `BOOP` sensitivity checks.
6. Several ordinary sentences and TV/background speech to assess false positives.
7. BOOP TTS containing the word `BOOP` must not self-trigger because KWS is suspended during speech.
8. Tap-to-talk still works before, during and after wake-word testing.
9. Voice settings and `change your voice` still work.
10. App background/close releases wake behavior; reopening re-arms.
11. Force a wake-init failure if practical and confirm tap-only behavior survives.

## Acceptance criteria

Alpha 6.4 is successful when all of the following are true on the Pixel 7 Pro:

- Saying `BOOP` while the app is open wakes the eyes immediately enough to feel responsive.
- A command spoken within the following 3 seconds is accepted.
- `BOOP <command>` works as one natural continuous sentence without dropping the first command word.
- BOOP does not audibly acknowledge the wake word before listening.
- BOOP cannot self-trigger from her own TTS.
- App closed/backgrounded means wake listening is off.
- Tap-to-talk remains fully functional and feels unchanged.
- Direct media commands still execute with the existing fast path.
- Home Assistant and general-assistant routing remain unchanged.
- Any wake-specific failure degrades cleanly to tap-only BOOP rather than taking the puppet down.

## Explicit non-goals for Alpha 6.4

- background wake service
- lock-screen wake/bypass
- cross-device portability work
- user-selectable wake words
- multiple wake phrases
- wake-word settings UI
- cloud wake detection
- full local ASR replacement
- echo cancellation / voice barge-in while BOOP is speaking
- weather/sensor work
- changes to BOOP performance presets
- changes to media/HA/ChatGPT routing

## Implementation boundary

This document approves the architecture only. Implementation starts only after the design has been reviewed and an implementation plan has been written under the normal BOOP TDD/verification workflow.
