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
    assert "openDevMenu();" in block
    assert "return;" in block


def test_dev_menu_launch_finishes_wake_processing_and_uses_explicit_activity() -> None:
    source = MAIN.read_text(encoding="utf-8")
    start = source.index("private void openDevMenu()")
    end = source.index("private TextView voiceSettingLabel", start)
    block = source[start:end]

    assert "wakeCoordinator.finishWakeProcessing();" in block
    assert "new Intent(MainActivity.this, BoopDevMenuActivity.class)" in block
    assert "interactionSurface.post(launch);" in block


def test_voice_settings_is_vertically_scrollable_to_dev_menu_and_done() -> None:
    source = MAIN.read_text(encoding="utf-8")

    assert "import android.widget.ScrollView;" in source
    assert "private ScrollView voiceSettingsScroll;" in source
    assert "voiceSettingsScroll = new ScrollView(this);" in source
    assert "voiceSettingsScroll.setFillViewport(true);" in source
    assert "voiceSettingsScroll.addView(voiceSettingsOverlay" in source
    assert "interactionSurface.addView(voiceSettingsScroll" in source
    assert "interactionSurface.removeView(voiceSettingsScroll);" in source
