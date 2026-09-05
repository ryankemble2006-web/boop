#!/usr/bin/env python3
# BOOP Alpha 6.5.2 assistant-only follow-up patch.
from pathlib import Path

MAIN = Path("source/MainActivity.java")
GRADLE = Path("source/app-build.gradle")


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one match, found {count}")
    return text.replace(old, new, 1)


main = MAIN.read_text(encoding="utf-8")

main = replace_once(
    main,
    '    private static final long IDLE_TIMEOUT_MS = 30_000L;\n',
    '    private static final long IDLE_TIMEOUT_MS = 30_000L;\n'
    '    private static final long ASSISTANT_FOLLOW_UP_SILENCE_MS = 5_000L;\n',
    "follow-up timeout constant",
)

main = replace_once(
    main,
    '    private boolean pendingListenAfterPermission = false;\n'
    '    private boolean pendingDiscoveryAfterPermission = false;\n',
    '    private boolean pendingListenAfterPermission = false;\n'
    '    private boolean assistantFollowUpAfterTts = false;\n'
    '    private boolean assistantFollowUpListening = false;\n'
    '    private boolean suppressNextRecognizerError = false;\n'
    '    private String latestAssistantFollowUpPartial = null;\n'
    '    private boolean pendingDiscoveryAfterPermission = false;\n',
    "follow-up state fields",
)

main = replace_once(
    main,
    '    private final Runnable memberBerryRunnable = () -> {\n',
    '    private final Runnable assistantFollowUpSilenceRunnable =\n'
    '            this::finishAssistantFollowUpSilently;\n\n'
    '    private final Runnable memberBerryRunnable = () -> {\n',
    "follow-up timeout runnable",
)

old_tts = '''    private void installTtsListener() {
        if (tts == null) {
            return;
        }
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) { }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(() -> {
                    if (wakeCoordinator != null) {
                        wakeCoordinator.onTtsFinished();
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {
                runOnUiThread(() -> {
                    if (wakeCoordinator != null) {
                        wakeCoordinator.onTtsFinished();
                    }
                });
            }

            @Override
            public void onStop(String utteranceId, boolean interrupted) {
                runOnUiThread(() -> {
                    if (wakeCoordinator != null) {
                        wakeCoordinator.onTtsFinished();
                    }
                });
            }
        });
    }

'''
new_tts = '''    private void installTtsListener() {
        if (tts == null) {
            return;
        }
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) { }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(() -> finishTtsUtterance());
            }

            @Override
            public void onError(String utteranceId) {
                runOnUiThread(() -> finishTtsUtterance());
            }

            @Override
            public void onStop(String utteranceId, boolean interrupted) {
                runOnUiThread(() -> finishTtsUtterance());
            }
        });
    }

    private void finishTtsUtterance() {
        boolean openAssistantFollowUp = assistantFollowUpAfterTts;
        assistantFollowUpAfterTts = false;

        // Preserve the proven hand-off: reserve the tap recognizer before releasing TTS,
        // so the wake-word engine cannot race the follow-up microphone.
        if (openAssistantFollowUp && wakeCoordinator != null) {
            wakeCoordinator.onTapStarted();
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsFinished();
        }
        if (!openAssistantFollowUp) {
            return;
        }

        assistantFollowUpListening = true;
        latestAssistantFollowUpPartial = null;
        recognitionMode = RecognitionMode.TAP;
        startListening();
        scheduleAssistantFollowUpSilenceTimeout();
    }

    private void scheduleAssistantFollowUpSilenceTimeout() {
        if (presenceHandler == null
                || !assistantFollowUpListening
                || recognitionMode != RecognitionMode.TAP
                || !listening) {
            return;
        }
        presenceHandler.removeCallbacks(assistantFollowUpSilenceRunnable);
        presenceHandler.postDelayed(
                assistantFollowUpSilenceRunnable,
                ASSISTANT_FOLLOW_UP_SILENCE_MS);
    }

    private void cancelAssistantFollowUpSilenceTimeout() {
        if (presenceHandler != null) {
            presenceHandler.removeCallbacks(assistantFollowUpSilenceRunnable);
        }
    }

    private void sleepFaceImmediately() {
        if (presenceHandler != null) {
            presenceHandler.removeCallbacks(faceIdleRunnable);
        }
        if (presenceState != null) {
            presenceState.idle();
        }
        if (face != null) {
            face.goIdleBlack();
        }
    }

    private void finishAssistantFollowUpSilently() {
        if (!assistantFollowUpListening || recognitionMode != RecognitionMode.TAP) {
            return;
        }
        cancelAssistantFollowUpSilenceTimeout();
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        suppressNextRecognizerError = true;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
        if (face != null) {
            face.animate().alpha(1.0f).setDuration(120).start();
        }
        if (recognizer != null) {
            recognizer.cancel();
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.onTapFinished();
        }
        sleepFaceImmediately();
    }

'''
main = replace_once(main, old_tts, new_tts, "TTS hand-off")

main = replace_once(
    main,
    '''    private void startListening() {
        wakeFaceForInteraction();
''',
    '''    private void startListening() {
        suppressNextRecognizerError = false;
        wakeFaceForInteraction();
''',
    "recognizer suppression reset",
)

main = replace_once(
    main,
    '        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);\n',
    '        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, assistantFollowUpListening);\n',
    "partial-result enablement",
)

main = replace_once(
    main,
    '''    private void stopListening() {
        RecognitionMode stoppedMode = recognitionMode;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
''',
    '''    private void stopListening() {
        RecognitionMode stoppedMode = recognitionMode;
        cancelAssistantFollowUpSilenceTimeout();
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
''',
    "manual stop cleanup",
)

old_outcome = '''            setupFailureSpoken = false;
            CommandOutcome outcome = commandRouter.process(transcript);
            runOnUiThread(() -> {
                speak(LocalReply.forOutcome(outcome));
                if (outcome.status() == CommandOutcome.Status.AUTH_REQUIRED) {
                    tokenStore.clear();
                    setupFailureSpoken = false;
                    ensureHouseConnection();
                }
            });
'''
new_outcome = '''            setupFailureSpoken = false;
            CommandOutcome outcome = commandRouter.process(transcript);
            runOnUiThread(() -> {
                if (outcome.status() == CommandOutcome.Status.ASSISTANT_REPLY) {
                    speakThenOpenAssistantFollowUp(LocalReply.forOutcome(outcome));
                } else {
                    speak(LocalReply.forOutcome(outcome));
                }
                if (outcome.status() == CommandOutcome.Status.AUTH_REQUIRED) {
                    tokenStore.clear();
                    setupFailureSpoken = false;
                    ensureHouseConnection();
                }
            });
'''
main = replace_once(main, old_outcome, new_outcome, "assistant-only outcome routing")

main = replace_once(
    main,
    '''    private void speak(String text) {
''',
    '''    private void speakThenOpenAssistantFollowUp(String text) {
        assistantFollowUpAfterTts = true;
        speak(text);
    }

    private void speak(String text) {
''',
    "assistant follow-up speech helper",
)

old_speak_tail = '''        if (ttsReady && tts != null) {
            int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "boop-alpha3");
            if (result == TextToSpeech.ERROR && wakeCoordinator != null) {
                wakeCoordinator.onTtsFinished();
            }
        } else if (wakeCoordinator != null) {
            wakeCoordinator.onTtsFinished();
        }
'''
new_speak_tail = '''        if (ttsReady && tts != null) {
            int result = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "boop-alpha3");
            if (result == TextToSpeech.ERROR) {
                finishTtsUtterance();
            }
        } else {
            finishTtsUtterance();
        }
'''
main = replace_once(main, old_speak_tail, new_speak_tail, "TTS failure hand-off")

main = replace_once(
    main,
    '    @Override public void onBeginningOfSpeech() { }\n',
    '''    @Override
    public void onBeginningOfSpeech() {
        if (assistantFollowUpListening && recognitionMode == RecognitionMode.TAP) {
            cancelAssistantFollowUpSilenceTimeout();
        }
    }
''',
    "speech-begin silence cancellation",
)

old_error_head = '''    @Override
    public void onError(int error) {
        RecognitionMode failedMode = recognitionMode;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
        face.animate().alpha(1.0f).setDuration(120).start();

        if (failedMode == RecognitionMode.WAKE) {
'''
new_error_head = '''    @Override
    public void onError(int error) {
        RecognitionMode failedMode = recognitionMode;
        boolean failedAssistantFollowUp =
                failedMode == RecognitionMode.TAP && assistantFollowUpListening;
        String fallback = latestAssistantFollowUpPartial;
        cancelAssistantFollowUpSilenceTimeout();
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
        face.animate().alpha(1.0f).setDuration(120).start();

        if (suppressNextRecognizerError) {
            suppressNextRecognizerError = false;
            return;
        }

        if (failedMode == RecognitionMode.WAKE) {
'''
main = replace_once(main, old_error_head, new_error_head, "follow-up error state capture")

main = replace_once(
    main,
    '''        if (failedMode == RecognitionMode.TAP) {
            speak("Speech error " + error + ", " + speechErrorName(error) + ".");
            if (wakeCoordinator != null) {
                wakeCoordinator.onTapFinished();
            }
            return;
        }
''',
    '''        if (failedAssistantFollowUp
                && error == SpeechRecognizer.ERROR_NO_MATCH
                && fallback != null
                && !fallback.isBlank()) {
            if (wakeCoordinator != null) {
                wakeCoordinator.onTapFinished();
            }
            handleRecognizedSpeech(fallback);
            return;
        }

        if (failedAssistantFollowUp
                && (error == SpeechRecognizer.ERROR_NO_MATCH
                || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
            if (wakeCoordinator != null) {
                wakeCoordinator.onTapFinished();
            }
            sleepFaceImmediately();
            return;
        }

        if (failedMode == RecognitionMode.TAP) {
            speak("Speech error " + error + ", " + speechErrorName(error) + ".");
            if (wakeCoordinator != null) {
                wakeCoordinator.onTapFinished();
            }
            return;
        }
''',
    "one-word no-match recovery",
)

old_results_head = '''    @Override
    public void onResults(Bundle results) {
        RecognitionMode completedMode = recognitionMode;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
        face.animate().alpha(1.0f).setDuration(120).start();
'''
new_results_head = '''    @Override
    public void onResults(Bundle results) {
        RecognitionMode completedMode = recognitionMode;
        boolean completedAssistantFollowUp =
                completedMode == RecognitionMode.TAP && assistantFollowUpListening;
        cancelAssistantFollowUpSilenceTimeout();
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        recognitionMode = RecognitionMode.NONE;
        listening = false;
        face.animate().alpha(1.0f).setDuration(120).start();
'''
main = replace_once(main, old_results_head, new_results_head, "follow-up result cleanup")

main = replace_once(
    main,
    '''            speak("I didn't catch that.");
            if (wakeCoordinator != null) {
                wakeCoordinator.onTapFinished();
            }
            return;
        }

        if (best != null) {
''',
    '''            if (wakeCoordinator != null) {
                wakeCoordinator.onTapFinished();
            }
            if (completedAssistantFollowUp) {
                sleepFaceImmediately();
                return;
            }
            speak("I didn't catch that.");
            return;
        }

        if (best != null) {
''',
    "empty follow-up result handling",
)

main = replace_once(
    main,
    '    @Override public void onPartialResults(Bundle partialResults) { }\n',
    '''    @Override
    public void onPartialResults(Bundle partialResults) {
        if (!assistantFollowUpListening || recognitionMode != RecognitionMode.TAP) {
            return;
        }
        ArrayList<String> matches = partialResults.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches == null || matches.isEmpty()) {
            return;
        }
        String candidate = matches.get(0);
        if (candidate == null) {
            return;
        }
        candidate = candidate.trim();
        if (!candidate.isEmpty()) {
            latestAssistantFollowUpPartial = candidate;
        }
    }
''',
    "follow-up partial capture",
)

main = replace_once(
    main,
    '''    protected void onPause() {
        closeWakeAudioSession();
''',
    '''    protected void onPause() {
        assistantFollowUpAfterTts = false;
        assistantFollowUpListening = false;
        latestAssistantFollowUpPartial = null;
        cancelAssistantFollowUpSilenceTimeout();
        closeWakeAudioSession();
''',
    "pause follow-up cleanup",
)

main = replace_once(
    main,
    '''        if (presenceHandler != null) {
            presenceHandler.removeCallbacks(faceIdleRunnable);
            presenceHandler.removeCallbacks(memberBerryRunnable);
            presenceHandler = null;
        }
''',
    '''        if (presenceHandler != null) {
            presenceHandler.removeCallbacks(faceIdleRunnable);
            presenceHandler.removeCallbacks(assistantFollowUpSilenceRunnable);
            presenceHandler.removeCallbacks(memberBerryRunnable);
            presenceHandler = null;
        }
''',
    "destroy timeout cleanup",
)

MAIN.write_text(main, encoding="utf-8")

gradle = GRADLE.read_text(encoding="utf-8")
gradle = replace_once(
    gradle,
    '        versionCode 24\n        versionName "0.4.9-alpha6.5.1"\n',
    '        versionCode 25\n        versionName "0.4.9-alpha6.5.2"\n',
    "version bump",
)
GRADLE.write_text(gradle, encoding="utf-8")
