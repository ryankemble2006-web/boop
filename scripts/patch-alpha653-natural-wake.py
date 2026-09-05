#!/usr/bin/env python3
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
    "import android.graphics.Typeface;\nimport android.net.Uri;\n",
    "import android.graphics.Typeface;\nimport android.media.AudioManager;\nimport android.media.ToneGenerator;\nimport android.net.Uri;\n",
    "tone imports",
)

main = replace_once(
    main,
    "    private boolean assistantFollowUpAfterTts = false;\n    private boolean assistantFollowUpListening = false;\n",
    "    private boolean assistantFollowUpAfterTts = false;\n    private boolean sleepFaceAfterTts = false;\n    private boolean assistantFollowUpListening = false;\n",
    "sleep-after-tts field",
)

main = replace_once(
    main,
    "        assistantFollowUpAfterTts = false;\n        assistantFollowUpListening = false;\n",
    "        assistantFollowUpAfterTts = false;\n        sleepFaceAfterTts = false;\n        assistantFollowUpListening = false;\n",
    "pause sleep cleanup",
)

main = replace_once(
    main,
    """    private void finishTtsUtterance() {
        boolean openAssistantFollowUp = assistantFollowUpAfterTts;
        assistantFollowUpAfterTts = false;

        // Preserve the proven hand-off: reserve the tap recognizer before releasing TTS,
""",
    """    private void finishTtsUtterance() {
        boolean openAssistantFollowUp = assistantFollowUpAfterTts;
        boolean sleepAfterTts = sleepFaceAfterTts;
        assistantFollowUpAfterTts = false;
        sleepFaceAfterTts = false;

        // Preserve the proven hand-off: reserve the tap recognizer before releasing TTS,
""",
    "tts terminal flags",
)

main = replace_once(
    main,
    """        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsFinished();
        }
        if (!openAssistantFollowUp) {
            return;
        }

        assistantFollowUpListening = true;
""",
    """        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsFinished();
        }
        if (sleepAfterTts) {
            sleepFaceImmediately();
            return;
        }
        if (!openAssistantFollowUp) {
            return;
        }

        assistantFollowUpListening = true;
""",
    "sleep after tts completion",
)

main = replace_once(
    main,
    """    private void finishAssistantFollowUpSilently() {
""",
    """    private void playWakeAcceptedCue() {
        final ToneGenerator tone;
        try {
            tone = new ToneGenerator(AudioManager.STREAM_SYSTEM, 35);
        } catch (RuntimeException unavailable) {
            return;
        }
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 90);
        if (presenceHandler != null) {
            presenceHandler.postDelayed(() -> {
                try {
                    tone.release();
                } catch (RuntimeException ignored) {
                    // A cue must never interfere with waking BOOP.
                }
            }, 140L);
        } else {
            tone.release();
        }
    }

    private void finishAssistantFollowUpSilently() {
""",
    "wake cue helper",
)

main = replace_once(
    main,
    """                    wakeFaceForInteraction();
                    startWakeRecognition(session);
""",
    """                    wakeFaceForInteraction();
                    playWakeAcceptedCue();
                    startWakeRecognition(session);
""",
    "wake cue call",
)

main = replace_once(
    main,
    """        String exitReply = BoopConversationExitIntent.replyFor(transcript);
        if (exitReply != null) {
            speak(exitReply);
            return;
        }
""",
    """        String exitReply = BoopConversationExitIntent.replyFor(transcript);
        if (exitReply != null) {
            speakThenSleep(exitReply);
            return;
        }
""",
    "manners sleep route",
)

main = replace_once(
    main,
    """    private void speakThenOpenAssistantFollowUp(String text) {
""",
    """    private void speakThenSleep(String text) {
        assistantFollowUpAfterTts = false;
        sleepFaceAfterTts = true;
        speak(text);
    }

    private void speakThenOpenAssistantFollowUp(String text) {
""",
    "speak then sleep helper",
)

MAIN.write_text(main, encoding="utf-8")

gradle = GRADLE.read_text(encoding="utf-8")
gradle = replace_once(
    gradle,
    '        versionCode 25\n        versionName "0.4.9-alpha6.5.2"\n',
    '        versionCode 26\n        versionName "0.4.9-alpha6.5.3"\n',
    "version bump",
)
GRADLE.write_text(gradle, encoding="utf-8")
