"""Non-visual integration contracts. Appearance is accepted by Ryan on hardware."""
from pathlib import Path


def test_unified_dock_mirror_wake_and_room_contracts():
    manifest = Path("source/AndroidManifest.xml").read_text()
    materializer = Path("scripts/materialize-unified.sh").read_text()
    dock = Path("scripts/patch-unified-dock-mirror.py").read_text()
    wake = Path("scripts/patch-unified-wake-name.py").read_text()
    dashboard_patch = Path("scripts/patch-unified-shield-dashboard.py").read_text()
    dashboard = Path("shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeDashboardController.java").read_text()
    assert "android.permission.CAMERA" in manifest
    for patch in ("dock-mirror", "wake-name", "shield-dashboard"):
        assert f"python3 scripts/patch-unified-{patch}.py" in materializer
    assert "wakeCoordinator.setWakeAllowed(docked)" in dock
    assert "BoopMirrorIntent.actionFor(transcript)" in dock
    assert "REQ_CAMERA" in dock
    assert "RoomScopedEntities.keep(room, snapshot.cards())" in dashboard
    assert "HomeDashboardStateBus.publish(room, state)" in dashboard_patch
    assert "patch-unified-room-controls.py" in dashboard_patch
    assert "BoopWakeNameIntent.parse(transcript)" in wake
    assert 'DEFAULT = "BOOP"' in Path("source/BoopWakeName.java").read_text()
    store = Path("source/BoopWakeNameStore.java").read_text()
    assert 'PREFS = "boop_voice"' in store
    assert 'KEY = "wake_name"' in store
    assert "@CUSTOM_WAKE" in Path("source/BoopWakeKeywordBuilder.java").read_text()
    assert "@BOOP" in Path("wake-assets/boop-kws/keywords.txt").read_text()


def test_unified_materialization_uses_real_attempt_wake_gate():
    materializer = Path("scripts/materialize-unified.sh").read_text()
    wake_arm = Path("scripts/patch-unified-wake-arm.py").read_text()
    name_patch = "python3 scripts/patch-unified-wake-name.py"
    arm_patch = "python3 scripts/patch-unified-wake-arm.py"
    assert arm_patch in materializer
    assert materializer.index(name_patch) < materializer.index(arm_patch)
    assert "BoopWakeRecognitionCapability.canAttempt" in wake_arm


def test_mirror_parser_retains_explicit_open_close_commands():
    parser = Path("source/BoopMirrorIntent.java").read_text()
    assert "enum Action { NONE, OPEN, CLOSE }" in parser
    assert "show me the mirror" in parser
    assert "turn off mirror" in parser
