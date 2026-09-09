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


def test_pixel_android16_immersive_starts_after_content() -> None:
    activity = read("source/BoopDevMenuActivity.java")
    create_start = activity.index("protected void onCreate")
    create_end = activity.index("    @Override\n    protected void onResume", create_start)
    create = activity[create_start:create_end]
    assert create.index("setContentView(root);") < create.index("applyImmersiveUi")
    assert "root.post(this::applyImmersiveUi);" in create
    assert "getWindow().getInsetsController()" not in activity
    assert "getWindow().getDecorView()" in activity
    assert "decor.getWindowInsetsController()" in activity


def test_lab_preserves_legacy_back_on_android16() -> None:
    script = read("scripts/materialize-animation-lab.py")
    assert 'android:enableOnBackInvokedCallback="false"' in script


def test_lab_manifest_removes_startup_components() -> None:
    script = read("scripts/materialize-animation-lab.py")
    assert 'xmlns:tools="http://schemas.android.com/tools"' in script
    assert 'androidx.startup.InitializationProvider' in script
    assert 'androidx.profileinstaller.ProfileInstallReceiver' in script
    assert 'tools:node="remove"' in script
    assert "android.permission.RECORD_AUDIO" not in script
    assert "NotificationListenerService" not in script
    assert "android.intent.category.LAUNCHER" in script
