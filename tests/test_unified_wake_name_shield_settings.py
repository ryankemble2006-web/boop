from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def text(path):
    return (ROOT / path).read_text(encoding="utf-8")


def test_wake_name_is_spoken_alias_only_and_boop_identity_stays_fixed():
    name = text("source/BoopWakeName.java")
    store = text("source/BoopWakeNameStore.java")
    build = text("unified/app-build.gradle")
    manifest = text("source/AndroidManifest.xml")
    assert 'DEFAULT = "BOOP"' in name
    assert 'PREFS = "boop_voice"' in store
    assert 'KEY = "wake_name"' in store
    assert "applicationId 'com.boop.alpha1'" in build
    assert 'package="com.boop.alpha1"' in manifest


def test_natural_custom_wake_reuses_boop_phrase_family_and_fallback():
    builder = text("source/BoopWakeKeywordBuilder.java")
    spotter = text("source/BoopSherpaWakeSpotter.java")
    base = text("wake-assets/boop-kws/keywords.txt")
    for phrase in [
        'phrases.add(name);', 'phrases.add("HEY " + name);',
        'phrases.add("GOOD MORNING " + name);', 'phrases.add("WAKE UP " + name);',
        'phrases.add(name + " WAKE UP");', 'phrases.add("COME ON " + name);',
        'phrases.add("YOU THERE " + name);', 'phrases.add("ARE YOU THERE " + name);',
        'phrases.add("LISTEN " + name);', 'phrases.add("EXCUSE ME " + name);',
    ]:
        assert phrase in builder
    assert '@BOOP' in base
    assert 'combinedKeywords' in spotter
    assert 'Custom wake name could not be prepared; BOOP fallback remains active' in spotter


def test_voice_ui_verbal_rename_and_shield_share_one_preference():
    patch = text("scripts/patch-unified-wake-name.py")
    intent = text("source/BoopWakeNameIntent.java")
    shield = text("shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvSettingsView.java")
    assert "BOOP's name" in patch
    assert 'BoopWakeNameStore.load(this)' in patch
    assert 'BoopWakeNameStore.save(this, requestedName)' in patch
    for phrase in ['your new name is ', 'your name is ', "i'm calling you ",
                   "from now on you're ", 'reset your name', 'go back to boop']:
        assert phrase in intent
    assert 'VOICE_PREFS = "boop_voice"' in shield
    assert 'WAKE_NAME_KEY = "wake_name"' in shield
    assert "BOOP's name" in shield


def test_runtime_keyword_generation_installs_sentencepiece_model_after_materialization():
    patch = text("scripts/patch-unified-wake-name.py")
    workflow = text(".github/workflows/build-boop-unified.yml")
    assert 'bpe.model' in patch
    assert 'scripts/patch-unified-wake-name.py' in workflow


def test_shield_settings_show_live_room_scoped_home_assistant_inventory():
    settings = text("shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvSettingsView.java")
    bus = text("shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeDashboardStateBus.java")
    patch = text("scripts/patch-unified-shield-dashboard.py")
    room_filter = text("shield-overlay/app/src/main/java/com/boop/shieldoverlay/RoomScopedEntities.java")
    assert 'BOOP SETTINGS' in settings
    assert 'HOME ASSISTANT' in settings
    assert 'renderDashboard(HomeDashboardController.ViewState state)' in settings
    assert 'card.displayName()' in settings
    assert 'Only " + room.name() + " items are shown' in settings
    assert 'HomeDashboardStateBus.subscribe' in settings
    assert 'HomeDashboardStateBus.publish(room, state)' in patch
    assert 'shield-lib/src/main/java' in patch
    assert 'room.id().equals(card.areaId())' in room_filter
    assert 'Map<String, HomeDashboardController.ViewState>' in bus


def test_ci_checks_finished_apk_for_visible_settings_and_identity_markers():
    workflow = text(".github/workflows/build-boop-unified.yml")
    assert 'BOOP SETTINGS' in workflow
    assert 'HOME ASSISTANT' in workflow
    assert "BOOP's name" in workflow
    assert "package: name='com.boop.alpha1'" in workflow
