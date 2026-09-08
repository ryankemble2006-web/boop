from pathlib import Path


MATERIALIZED_MAIN = Path(
    "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java"
)
MATERIALIZED_SHERPA = Path(
    "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopSherpaWakeSpotter.java"
)
MATERIALIZED_WAKE_INTENT = Path(
    "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopWakeRecognitionIntent.java"
)


def test_wake_to_command_handoff_does_not_play_artificial_audio_cue():
    text = MATERIALIZED_MAIN.read_text(encoding="utf-8")
    start = text.index("public void onWakeDetected(BoopWakeAudioSession session, long detectedAtMs)")
    end = text.index("public void onWakeFailure(String message)", start)
    wake_callback = text[start:end]

    assert "startWakeRecognition(session);" in wake_callback
    assert "playWakeAcceptedCue();" not in wake_callback
    assert "BOOP_WAKE_COMMAND_SEAM_V1" in wake_callback


def test_default_boop_does_not_require_trailing_silence_before_trigger():
    text = MATERIALIZED_SHERPA.read_text(encoding="utf-8")
    assert "config.setNumTrailingBlanks(0);" in text
    assert "config.setNumTrailingBlanks(1);" not in text


def test_speech_recognition_does_not_mask_offensive_words():
    main_text = MATERIALIZED_MAIN.read_text(encoding="utf-8")
    wake_text = MATERIALIZED_WAKE_INTENT.read_text(encoding="utf-8")
    uncensored = "intent.putExtra(RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS, false);"

    assert uncensored in main_text
    assert uncensored in wake_text
