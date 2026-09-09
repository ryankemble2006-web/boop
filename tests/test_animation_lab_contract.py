from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def read(path: str) -> str:
    return (ROOT / path).read_text(encoding="utf-8")


def test_animation_lab_materializer_contract() -> None:
    script = read("scripts/materialize-animation-lab.py")
    for token in (
        "com.boop.animationlab",
        "BOOP Animation Lab",
        "IDLE_BLINK",
        "LISTENING",
        "playSingleIdleBlink",
        "startListeningCue",
        "BoopDevMenuActivity",
    ):
        assert token in script


def test_lab_keeps_all_current_v70_notification_doods() -> None:
    script = read("scripts/materialize-animation-lab.py")
    for label in (
        "Facebook",
        "WhatsApp",
        "Gmail",
        "X / Twitter",
        "YouTube",
        "Messenger",
        "Instagram",
        "Discord",
        "Spotify",
        "Reddit",
        "Locked",
        "Bundle",
    ):
        assert label in script


def test_lab_manifest_is_single_purpose() -> None:
    script = read("scripts/materialize-animation-lab.py")
    assert "android.permission.RECORD_AUDIO" not in script
    assert "NotificationListenerService" not in script
    assert "<service" not in script
    assert "<receiver" not in script
    assert "<provider" not in script
    assert "android.intent.category.LAUNCHER" in script
