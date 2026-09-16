from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BACKEND = ROOT / "source/BoopNaturalSpeechBackend.java"


def test_natural_voice_latency_has_stage_markers():
    source = BACKEND.read_text(encoding="utf-8")
    assert '"BOOP-VoiceLatency"' in source
    assert '"request_queued"' in source
    assert '"model_ready"' in source
    assert '"synthesis_done"' in source
    assert '"playback_start"' in source
    assert "SystemClock.elapsedRealtime()" in source
