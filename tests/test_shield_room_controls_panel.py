from pathlib import Path
import importlib.util

ROOT = Path(__file__).resolve().parents[1]
HOME = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome"
OVERLAY = ROOT / "shield-overlay/app/src/main/java/com/boop/shieldoverlay"
PATCH_PATH = ROOT / "scripts/patch-shield-room-panel.py"


def read(path):
    return path.read_text(encoding="utf-8")


def load_patch():
    spec = importlib.util.spec_from_file_location("boop_room_panel_patch", PATCH_PATH)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader is not None
    spec.loader.exec_module(module)
    return module


def test_launcher_room_controls_reuse_existing_room_and_home_assistant_stack():
    session = read(OVERLAY / "LauncherRoomControlsSession.java")
    assert "new BoopPreferences" in session
    assert ".selectedRoom()" in session
    assert "HomeAssistantSession" in session
    assert "HomeAssistantRepository" in session
    assert "HomeDashboardController" in session
    assert "selected_area_id_v1" not in session
    assert "selected_area_name_v1" not in session


def test_launcher_settings_can_disable_the_panel_without_a_second_room_preference():
    store = read(HOME / "ShieldHomeStore.java")
    settings = read(HOME / "ShieldHomeSettingsView.java")
    assert "smartHomePanelEnabled()" in store
    assert "setSmartHomePanelEnabled(boolean enabled)" in store
    assert "getBoolean(KEY_SMART_HOME_PANEL_ENABLED, true)" in store
    assert "Smart home panel: " in settings
    assert "onSetSmartHomePanelEnabled" in settings
    assert "Set this device room" in settings


def test_room_panel_is_boop_charcoal_remote_first_and_has_no_stock_android_menu():
    panel = read(HOME / "ShieldRoomControlsView.java")
    assert "Color.rgb(42, 42, 42)" in panel or "Color.rgb(42,42,42)" in panel
    assert "FocusChrome" in panel
    assert "setFocusable(true)" in panel
    assert "AlertDialog" not in panel
    assert "PopupMenu" not in panel


def test_room_panel_expands_when_boop_leaves_and_reserves_his_bay_when_he_returns():
    patch = load_patch()
    home = patch.patch_home(read(HOME / "ShieldHomeView.java"))
    puppet = patch.patch_puppet(read(HOME / "ShieldNowPlayingPuppetView.java"))
    assert "MASCOT_RESERVED_DP" in home
    assert "setVisibilityListener" in home
    assert "syncRoomPanelMascotSpace" in home
    assert "VisibilityListener" in puppet
    assert "onPuppetVisibilityChanged" in puppet


def test_launcher_refreshes_room_controls_on_resume_so_room_changes_take_effect():
    patch = load_patch()
    activity = patch.patch_activity(read(HOME / "ShieldLauncherActivity.java"))
    assert "LauncherRoomControlsSession" in activity
    assert "smartHomePanelEnabled()" in activity
    assert "refreshRoomControls" in activity
    assert "roomControlsSession.refresh()" in activity
    assert "roomControlsSession.stop()" in activity
    assert "boop_choose_room" in activity


def test_unified_materialization_runs_the_room_panel_patch():
    integration = read(ROOT / "scripts/patch-unified-room-controls.py")
    assert "patch-shield-room-panel.py" in integration
    assert "runpy.run_path" in integration
