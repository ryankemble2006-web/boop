from __future__ import annotations

import hashlib
from pathlib import Path
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ANDROID = "{http://schemas.android.com/apk/res/android}"


def read(relative: str) -> str:
    return (ROOT / relative).read_text(encoding="utf-8")


def test_unified_package_version_and_private_dev_activity_contract() -> None:
    build = read("unified/app-build.gradle")
    assert "applicationId 'com.boop.alpha1'" in build
    code = re.search(r"^\s*versionCode\s+(\d+)\s*$", build, re.MULTILINE)
    name = re.search(r'^\s*versionName\s+"([^"]+)"\s*$', build, re.MULTILINE)
    assert code and int(code.group(1)) >= 70
    assert name and name.group(1).startswith("1.2.")

    manifest = ET.parse(ROOT / "source/AndroidManifest.xml").getroot()
    activities = [
        item
        for item in manifest.findall(".//activity")
        if item.get(ANDROID + "name") == ".BoopDevMenuActivity"
    ]
    assert len(activities) == 1
    assert activities[0].get(ANDROID + "exported") == "false"


def test_exact_approved_notification_hands_remain_byte_locked() -> None:
    asset = ROOT / "unified/assets/boop-notifications/boop-yellow-hands-approved.png"
    payload = asset.read_bytes()
    assert len(payload) == 1_809_990
    assert hashlib.sha256(payload).hexdigest() == (
        "26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1"
    )


def test_finished_procedural_eye_stack_stays_in_order_with_no_late_hue_patch() -> None:
    materialize = read("scripts/materialize-unified.sh")
    names = [
        "patch-unified-reading-eyes.py",
        "patch-v64-procedural-sclera.py",
        "patch-v65-feathered-sclera.py",
    ]
    positions = [materialize.index(name) for name in names]
    assert positions == sorted(positions)
    suffix = materialize[positions[-1] + len(names[-1]) :]
    assert not re.search(r"patch-[^\n]*(?:eye[^\n]*hue|hue[^\n]*eye)", suffix, re.IGNORECASE)


def test_every_v70_dev_action_is_backed_by_real_dispatch_or_local_presentation() -> None:
    model = read("source/BoopDevMenuModel.java")
    activity = read("source/BoopDevMenuActivity.java")
    preview = read("source/BoopDevNotificationPreview.java")

    animation_actions = [
        "WAKE", "THINK", "STOP", "BERRY_1", "BERRY_2", "BERRY_3", "SHAKE", "SLEEP"
    ]
    notification_actions = [
        "NOTIFICATION_FACEBOOK",
        "NOTIFICATION_WHATSAPP",
        "NOTIFICATION_GMAIL",
        "NOTIFICATION_X",
        "NOTIFICATION_YOUTUBE",
        "NOTIFICATION_MESSENGER",
        "NOTIFICATION_INSTAGRAM",
        "NOTIFICATION_DISCORD",
        "NOTIFICATION_SPOTIFY",
        "NOTIFICATION_REDDIT",
        "NOTIFICATION_LOCKED",
        "NOTIFICATION_BUNDLE",
    ]
    for action in animation_actions:
        assert action in model
        assert f"case {action}:" in activity
    for action in notification_actions:
        assert action in model
        assert f"case {action}:" in activity
        assert f"case {action}:" in preview


def test_notification_demos_stay_local_and_use_real_puppet_face_and_locked_hands() -> None:
    forbidden = ("NotificationManager", "BoopNotificationRuntime", "BoopNotificationListenerService")
    for relative in (
        "source/BoopDevMenuActivity.java",
        "source/BoopDevNotificationPreview.java",
        "source/BoopDevNotificationIdentity.java",
        "source/BoopDevNotificationIconDrawable.java",
    ):
        source = read(relative)
        for token in forbidden:
            assert token not in source

    puppet = read("source/BoopNotificationPuppetView.java")
    assert "new BoopFaceView(context)" in puppet
    assert "R.drawable.boop_notification_hands" in puppet
    assert "BoopDevNotificationIdentity.forPackage(packageName)" in puppet
    assert "new BoopDevNotificationIconDrawable(devIdentity)" in puppet


def test_dev_lab_declares_fullscreen_and_real_animation_calls_without_visual_assertions() -> None:
    activity = read("source/BoopDevMenuActivity.java")
    assert "WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars()" in activity
    assert "WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE" in activity
    assert "View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY" in activity
    for call in (
        "wakeFromIdle()",
        "startThinking()",
        "stopThinking()",
        "playMemberBerry(variant)",
        "playShakeMuppet(0.85f)",
        "goIdleBlack()",
    ):
        assert call in activity

    workflow = read(".github/workflows/build-boop-unified.yml").lower()
    for forbidden in ("screencap", "pixelmatch", "golden-image", "paparazzi"):
        assert forbidden not in workflow
