from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def text(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def test_voice_settings_expose_pitch_and_speed_sliders():
    main = text("source/MainActivity.java")
    assert '"Pitch"' in main
    assert '"Speed"' in main
    assert "BoopVoiceTuning.progressFromPitch" in main
    assert "BoopVoiceTuning.progressFromRate" in main
    assert "voiceController.setPitch" in main
    assert "voiceController.setSpeechRate" in main


def test_voice_profile_protocol_carries_natural_voice_pitch_and_rate():
    protocol = text("source/SharedVoiceProfileProtocol.java")
    assert 'PREFIX = "BOOP_VOICE_V1|"' in protocol
    assert "naturalVoiceKey" in protocol
    assert "pitchMilli" in protocol
    assert "rateMilli" in protocol


def test_shared_voice_runtime_is_initialized_with_unified_app():
    app = text("source/UnifiedApplication.java")
    assert "BoopSharedVoiceProfileRuntime.initialize(this)" in app
