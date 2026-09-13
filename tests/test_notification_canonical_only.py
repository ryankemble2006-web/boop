from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PUPPET = ROOT / "source/BoopNotificationPuppetView.java"
SIGN = ROOT / "unified/animation/java/com/boop/eyes/NotificationSignView.java"

def test_all_notification_surfaces_share_animation_lab_stage():
    text = PUPPET.read_text(encoding="utf-8")
    assert "com.boop.eyes.NotificationSignView" in text
    assert "com.boop.eyes.SignMotion.sample" in text
    assert "com.boop.eyes.CanonicalEyeRenderer" in text
    assert "new BoopFaceView" not in text
    assert "boop_notification_hands" not in text
    assert "HANDS_REST_SCALE" not in text
    assert "OvershootInterpolator" not in text

def test_animation_lab_sign_is_runtime_reusable():
    text = SIGN.read_text(encoding="utf-8")
    assert "public final class NotificationSignView" in text
    assert "public void show(SignMotion.Pose pose,int style)" in text
