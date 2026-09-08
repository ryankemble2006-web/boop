from pathlib import Path


def test_wake_to_command_handoff_does_not_play_artificial_audio_cue():
    text = Path("source/MainActivity.java").read_text(encoding="utf-8")
    start = text.index("public void onWakeDetected(BoopWakeAudioSession session, long detectedAtMs)")
    end = text.index("public void onWakeFailure(String message)", start)
    wake_callback = text[start:end]

    assert "startWakeRecognition(session);" in wake_callback
    assert "playWakeAcceptedCue();" not in wake_callback
