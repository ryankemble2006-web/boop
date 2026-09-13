from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "source" / "BoopCanonicalFaceView.java"

def test_canonical_face_ignores_android_animator_scale():
    text = SOURCE.read_text(encoding="utf-8")
    assert "ValueAnimator.areAnimatorsEnabled()" not in text
    assert "import android.animation.ValueAnimator;" not in text
    assert "BoopMotionPolicy.shouldAnimate" in text