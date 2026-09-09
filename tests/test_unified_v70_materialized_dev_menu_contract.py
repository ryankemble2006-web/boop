from pathlib import Path


MAIN = Path(
    "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java"
)
DEV_INTENT = Path(
    "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopDevMenuIntent.java"
)
MATERIALIZER = Path("scripts/materialize-unified.sh")


def test_v70_scroll_repair_runs_before_later_unified_settings_patches() -> None:
    source = MATERIALIZER.read_text(encoding="utf-8")
    scroll = source.index("patch-unified-v70-regressions.py")
    notifications = source.index("patch-unified-notifications.py")
    dev_menu = source.index("patch-unified-dev-menu.py")

    assert scroll < notifications < dev_menu


def test_spoken_developer_menu_phrase_replaces_old_dev_menu_phrase() -> None:
    source = DEV_INTENT.read_text(encoding="utf-8")
    assert 'return "developer menu".equals(normalized);' in source
    assert 'return "dev menu".equals(normalized);' not in source


def test_spoken_developer_menu_is_materialized_before_ha_and_chat_routing() -> None:
    source = MAIN.read_text(encoding="utf-8")
    voice_settings = source.index("BoopVoiceSettingsIntent.matches(transcript)")
    dev_menu = source.index("BoopDevMenuIntent.matches(transcript)")
    voice_change = source.index("voiceController.maybeChangeVoice(transcript)")
    routed = source.index("commandRouter.process(")

    assert voice_settings < dev_menu < voice_change < routed
    block = source[dev_menu:voice_change]
    assert "showDeveloperMenu();" in block
    assert "return;" in block


def test_developer_menu_stays_in_main_activity_instead_of_activity_hop() -> None:
    source = MAIN.read_text(encoding="utf-8")
    start = source.index("private void showDeveloperMenu()")
    end = source.index("private TextView voiceSettingLabel", start)
    block = source[start:end]

    assert "startActivity(" not in block
    assert "BoopDevMenuActivity.class" not in block
    assert "interactionSurface.addView(" in block
    assert "developerMenuOverlay" in block


def test_voice_settings_is_vertically_scrollable_to_developer_menu_and_done() -> None:
    source = MAIN.read_text(encoding="utf-8")

    assert "import android.widget.ScrollView;" in source
    assert "private ScrollView voiceSettingsScroll;" in source
    assert "voiceSettingsScroll = new ScrollView(this);" in source
    assert "voiceSettingsScroll.setFillViewport(true);" in source
    assert "voiceSettingsScroll.addView(voiceSettingsOverlay" in source
    assert "interactionSurface.addView(voiceSettingsScroll" in source
    assert "interactionSurface.removeView(voiceSettingsScroll);" in source
    assert 'devMenu.setText("Developer menu");' in source
