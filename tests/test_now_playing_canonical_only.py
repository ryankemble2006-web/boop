from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VIEW = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingPuppetView.java"


def test_now_playing_uses_only_canonical_animation_face():
    text = VIEW.read_text(encoding="utf-8")
    assert "R.drawable.boop_headphones" not in text
    assert "LegacyEyeMaskView" not in text
    assert "NowPlayingPuppetEyePlacement" not in text
    assert "CanonicalEyeRenderer" in text
    assert "ProductionAnimationController" in text


def test_now_playing_motion_ignores_android_animator_scale():
    text = VIEW.read_text(encoding="utf-8")
    assert "ValueAnimator.areAnimatorsEnabled()" not in text
    assert "powerManager == null || !powerManager.isPowerSaveMode()" in text
