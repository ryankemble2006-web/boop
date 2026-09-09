from hashlib import sha256
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE_HANDS = ROOT / "unified/assets/boop-notifications/boop-yellow-hands-approved.png"
EXPECTED_HANDS_SHA256 = "26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1"
MATERIALIZER = ROOT / "scripts/materialize-boop-notification-assets.py"
PUPPET = ROOT / "source/BoopNotificationPuppetView.java"
SURFACES = (
    ROOT / "source/BoopNotificationInPlaceController.java",
    ROOT / "source/BoopNotificationOverlayController.java",
    ROOT / "source/BoopNotificationLockActivity.java",
)


def test_locked_hand_asset_remains_exact():
    assert SOURCE_HANDS.is_file()
    assert sha256(SOURCE_HANDS.read_bytes()).hexdigest() == EXPECTED_HANDS_SHA256


def test_notification_hands_have_an_exact_byte_materializer():
    text = MATERIALIZER.read_text(encoding="utf-8")
    assert EXPECTED_HANDS_SHA256 in text
    assert "boop_notification_hands.png" in text


def test_all_notification_surfaces_share_one_puppet_view():
    puppet = PUPPET.read_text(encoding="utf-8")
    assert "final class BoopNotificationPuppetView" in puppet
    assert "interface Callback" in puppet
    assert "BoopFaceView" in puppet
    assert "R.drawable.boop_notification_hands" in puppet
    for surface in SURFACES:
        assert "BoopNotificationPuppetView" in surface.read_text(encoding="utf-8")


def test_shared_puppet_declares_emphasized_hands_raised_banner_pose():
    puppet = PUPPET.read_text(encoding="utf-8")
    assert "BOOP_NOTIFICATION_PUPPET_EMPHASIZED_POSE_V1" in puppet
