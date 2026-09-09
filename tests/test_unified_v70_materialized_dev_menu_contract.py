from pathlib import Path


MAIN = Path(
    "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java"
)
DEV_INTENT = Path(
    "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopDevMenuIntent.java"
)
PUPPET = Path(
    "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopNotificationPuppetView.java"
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


def test_dev_lab_uses_horizontal_shelves_and_restores_shelf_positions() -> None:
    source = MAIN.read_text(encoding="utf-8")

    assert "import android.widget.HorizontalScrollView;" in source
    assert "private int developerAnimationScrollX = 0;" in source
    assert "private int developerNotificationScrollX = 0;" in source
    assert "new HorizontalScrollView(this)" in source
    assert "setHorizontalScrollBarEnabled(false)" in source
    assert "developerAnimationScrollX = scrollX" in source
    assert "developerNotificationScrollX = scrollX" in source
    assert "scrollTo(savedScrollX, 0)" in source


def test_dev_lab_keeps_face_pinned_and_has_no_vertical_page_scroll() -> None:
    source = MAIN.read_text(encoding="utf-8")
    start = source.index("private void showDeveloperMenuContent()")
    end = source.index("private void addDeveloperShelf(", start)
    block = source[start:end]

    assert "new ScrollView(this)" not in block
    assert "developerMenuFace = new BoopFaceView(this);" in block
    assert "column.addView(developerMenuFace" in block
    assert "developerMenuOverlay.addView(column" in block
    assert block.index("column.addView(developerMenuFace") < block.index("addDeveloperShelf(")


def test_animation_selector_controls_pinned_face_in_place() -> None:
    source = MAIN.read_text(encoding="utf-8")
    start = source.index("private void addDeveloperShelf(")
    end = source.index("private void addDeveloperSection(", start)
    block = source[start:end]

    assert "showDeveloperAnimationPreview(" not in block
    assert "runDeveloperAction(action);" in block
    assert "BoopDevMenuModel.Action.STOP" not in block
    assert "developerMenuOverlay.removeAllViews();" not in block
    assert "private void showDeveloperAnimationPreview(" not in source


def test_dev_notification_preview_uses_one_separate_face_renderer_with_real_puppet() -> None:
    source = MAIN.read_text(encoding="utf-8")
    start = source.index("private void showDeveloperNotificationPreview(")
    end = source.index("private void hideDeveloperMenu()", start)
    block = source[start:end]

    assert "developerMenuFace = new BoopFaceView(this);" in block
    assert "new BoopNotificationPuppetView(" in block
    assert "puppet.setFaceVisible(false);" in block

    puppet = PUPPET.read_text(encoding="utf-8")
    assert "void setFaceVisible(boolean visible)" in puppet
