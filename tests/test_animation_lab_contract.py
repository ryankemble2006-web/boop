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
        "BoopShieldPreviewView",
        "MediaPuppetMotion.java",
        "FullscreenPuppetMotion.java",
        "boop_headphones.png",
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


def test_lab_includes_real_shield_runtime_motion_and_saved_wip() -> None:
    script = read("scripts/materialize-animation-lab.py")
    preview = read("source/BoopShieldPreviewView.java")
    for label in (
        "Shield Groove",
        "Track Change",
        "Pause Settle",
        "Cinema Hand",
        "Open Palms",
        "Wave",
        "Point",
        "Grip",
        "Earcup Adjust",
        "One-Cup Listen",
        "Gaze + Depth",
        "Headphone Recoil",
    ):
        assert label in script
    for mode in (
        "SHIELD_GROOVE",
        "SHIELD_TRACK_CHANGE",
        "SHIELD_PAUSE_SETTLE",
        "SHIELD_CINEMA_HAND",
        "WIP_OPEN_PALMS",
        "WIP_WAVE",
        "WIP_POINT",
        "WIP_GRIP",
        "WIP_EARCUP_ADJUST",
        "WIP_ONE_CUP_LISTEN",
        "WIP_GAZE_DEPTH",
        "WIP_HEADPHONE_RECOIL",
    ):
        assert mode in preview
    assert "BoopShieldMotionBridge" in preview
    assert "R.drawable.boop_notification_hands" in preview
    assert "R.drawable.boop_headphones" in preview
    assert "ValueAnimator" in preview
    assert "INFINITE" in preview


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


def test_github_never_performs_visual_acceptance() -> None:
    workflow = read(".github/workflows/build-boop-animation-lab.yml").lower()
    for forbidden in (
        "screenshot",
        "screencap",
        "golden-image",
        "golden image",
        "pixelmatch",
        "paparazzi",
        "image comparison",
        "visual acceptance",
    ):
        assert forbidden not in workflow
