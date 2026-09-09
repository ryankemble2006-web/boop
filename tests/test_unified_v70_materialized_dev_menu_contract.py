from pathlib import Path


MAIN = Path(
    "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java"
)


def test_spoken_dev_menu_is_materialized_before_ha_and_chat_routing() -> None:
    source = MAIN.read_text(encoding="utf-8")
    voice_settings = source.index("BoopVoiceSettingsIntent.matches(transcript)")
    dev_menu = source.index("BoopDevMenuIntent.matches(transcript)")
    voice_change = source.index("voiceController.maybeChangeVoice(transcript)")
    routed = source.index("commandRouter.process(")

    assert voice_settings < dev_menu < voice_change < routed
    block = source[dev_menu:voice_change]
    assert "openDevMenuSafely();" in block
    assert "startActivity(new Intent().setClassName(" not in block
    assert "return;" in block


def test_dev_menu_launch_is_deferred_until_after_speech_callback_unwinds() -> None:
    source = MAIN.read_text(encoding="utf-8")
    helper_start = source.index("private void openDevMenuSafely()")
    helper_end = source.index("private TextView voiceSettingLabel", helper_start)
    helper = source[helper_start:helper_end]

    assert '"com.boop.alpha1.BoopDevMenuActivity"' in helper
    assert "Runnable launch = () ->" in helper
    assert "presenceHandler.post(launch);" in helper
    assert "startActivity(new Intent().setClassName(" in helper
    assert "catch (RuntimeException" in helper


def test_voice_settings_wraps_existing_content_in_vertical_scroll_container() -> None:
    source = MAIN.read_text(encoding="utf-8")
    show_start = source.index("private void showVoiceSettings()")
    show_end = source.index("private TextView voiceSettingLabel", show_start)
    show = source[show_start:show_end]

    assert "new android.widget.ScrollView(this)" in show
    assert "voiceSettingsScroller.setFillViewport(true);" in show
    assert "voiceSettingsScroller.addView(voiceSettingsOverlay" in show
    assert "interactionSurface.addView(voiceSettingsScroller" in show
    assert "voiceSettingsScroller.bringToFront();" in show

    hide_start = source.index("private void hideVoiceSettings()")
    hide_end = source.index("private void openDevMenuSafely()", hide_start)
    hide = source[hide_start:hide_end]
    assert "voiceSettingsOverlay.getParent()" in hide
    assert "interactionSurface.removeView((View) parent);" in hide
