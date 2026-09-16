from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def text(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def test_build_a_boop_exposes_voice_pitch_and_speed_sliders():
    appearance = text("source/BoopAppearanceActivity.java")
    assert '"Voice pitch"' in appearance
    assert '"Voice speed"' in appearance
    assert "BoopVoiceTuning.progressFromPitch" in appearance
    assert "BoopVoiceTuning.progressFromRate" in appearance
    assert "voiceController.setPitch" in appearance
    assert "voiceController.setSpeechRate" in appearance


def test_voice_profile_protocol_carries_backend_natural_voice_pitch_and_rate():
    path = ROOT / "source/SharedVoiceProfileProtocol.java"
    assert path.is_file(), "voice profile protocol is not implemented"
    protocol = path.read_text(encoding="utf-8")
    assert 'PREFIX = "BOOP_VOICE_V1|"' in protocol
    assert "backend" in protocol
    assert "naturalVoiceKey" in protocol
    assert "pitchMilli" in protocol
    assert "rateMilli" in protocol


def test_shared_voice_runtime_is_initialized_with_unified_app():
    app = text("unified/UnifiedApplication.java")
    assert "BoopSharedVoiceProfileRuntime.initialize(this)" in app


def test_voice_controller_can_reload_a_profile_received_from_another_device():
    controller = text("source/BoopVoiceController.java")
    assert "refreshProfileFromPreferences" in controller
    assert "naturalBackendSelectedAndUsable()" in controller
    assert "selectedNaturalVoice()" in controller


def test_v198_workflow_runs_the_focused_voice_contract():
    workflow = text(".github/workflows/build-boop-v191-hand-colour.yml")
    assert "tests/test_v198_voice_profile_controls.py" in workflow
