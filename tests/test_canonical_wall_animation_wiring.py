from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FACE = ROOT / "source/BoopCanonicalFaceView.java"
PATCH = ROOT / "scripts/patch-unified-canonical-animations.py"


def test_canonical_wall_face_owns_finished_animation_engine():
    source = FACE.read_text(encoding="utf-8")
    assert "ProductionAnimationController" in source
    assert "CanonicalEyeRenderer" in source
    assert "ObjectAnimator" not in source
    assert "BoopShakeEyeMotion" not in source
    for clip in ("wake", "sleep", "idle", "listening", "thinking",
                 "berry_remember", "berry_curious", "berry_cheeky", "shake_reaction",
                 "notification"):
        assert f'"{clip}"' in source


def test_final_materialization_switches_wall_and_notifications_to_canonical_face():
    patch = PATCH.read_text(encoding="utf-8")
    assert "BoopCanonicalFaceView" in patch
    assert "BoopEyeHueOverlay.java" in patch
    assert "faceView.playNotification()" in patch
