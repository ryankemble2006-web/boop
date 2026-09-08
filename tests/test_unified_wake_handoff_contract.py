from pathlib import Path


MATERIALIZED_MAIN = Path(
    "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java"
)
MATERIALIZED_SHERPA = Path(
    "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopSherpaWakeSpotter.java"
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
