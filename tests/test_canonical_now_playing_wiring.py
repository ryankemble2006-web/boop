from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VIEW = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingPuppetView.java"


def test_now_playing_uses_canonical_production_animation_engine():
    source = VIEW.read_text(encoding="utf-8")
    assert "ProductionAnimationController" in source
    assert "CanonicalMediaAnimationPolicy" in source
    assert "EyeMotion.Pose" in source
    assert "NowPlayingPuppetMotion" not in source
    assert "NowPlayingPuppetBlink" not in source
