from pathlib import Path


def test_unified_dock_mirror_shield_settings_and_wake_name_contract():
    manifest = Path("source/AndroidManifest.xml").read_text()
    materializer = Path("scripts/materialize-unified.sh").read_text()
    dock_patch = Path("scripts/patch-unified-dock-mirror.py").read_text()
    wake_patch = Path("scripts/patch-unified-wake-name.py").read_text()
    dashboard_patch = Path("scripts/patch-unified-shield-dashboard.py").read_text()
    settings = Path("shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvSettingsView.java").read_text()
    home = Path("shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvHomeView.java").read_text()
    dashboard = Path("shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeDashboardController.java").read_text()
    repository = Path("shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantRepository.java").read_text()
    wake_name = Path("source/BoopWakeName.java").read_text()
    wake_store = Path("source/BoopWakeNameStore.java").read_text()
    wake_builder = Path("source/BoopWakeKeywordBuilder.java").read_text()

    assert 'android.permission.CAMERA' in manifest
    assert 'python3 scripts/patch-unified-dock-mirror.py' in materializer
    assert 'python3 scripts/patch-unified-wake-name.py' in materializer
    assert 'python3 scripts/patch-unified-shield-dashboard.py' in materializer
    assert 'wakeCoordinator.setWakeAllowed(docked)' in dock_patch
    assert 'BoopMirrorIntent.actionFor(transcript)' in dock_patch
    assert 'REQ_CAMERA' in dock_patch
    assert 'onPresenceNudge' in dock_patch

    assert 'BOOP SETTINGS' in settings
    assert 'HOUSE' in settings
    assert "BOOP's name" in settings
    assert 'Devices for this room appear on Home' in settings
    assert 'HOME ASSISTANT' not in settings
    assert 'HomeDashboardStateBus.subscribe' not in settings
    assert 'KEYCODE_DPAD_DOWN' in settings and 'KEYCODE_DPAD_UP' in settings
    assert 'ensureVisible' in settings

    assert 'BOOP HOME' in home
    assert 'ROOM' in home and 'DEVICES' in home
    assert 'Favourites' not in home
    assert 'DeviceCard' in home
    assert 'Color.rgb(61,220,255)' in home
    assert 'ensureVisible' in home

    assert 'RoomScopedEntities.keep(room, snapshot.cards())' in dashboard
    assert "I couldn't confirm this room, so I hid the controls." in dashboard
    assert 'favouriteSelector' not in dashboard
    assert 'extract_from_target' in repository
    assert 'config/entity_registry/list_for_display' in repository
    assert '"diagnostic"' in repository and '"config"' in repository

    assert 'HomeDashboardStateBus.publish(room, state)' in dashboard_patch
    assert 'DEFAULT = "BOOP"' in wake_name
    assert 'PREFS = "boop_voice"' in wake_store
    assert 'KEY = "wake_name"' in wake_store
    assert 'BoopWakeNameIntent.parse(transcript)' in wake_patch
    assert 'bpe.model' in wake_patch
    assert '@CUSTOM_WAKE' in wake_builder
    assert '@BOOP' in Path("wake-assets/boop-kws/keywords.txt").read_text()


def test_mirror_parser_has_open_close_and_false_positive_guards():
    parser = Path("source/BoopMirrorIntent.java").read_text()
    assert 'enum Action { NONE, OPEN, CLOSE }' in parser
    assert 'show me the mirror' in parser
    assert 'turn off mirror' in parser
    assert 'what is' not in parser
