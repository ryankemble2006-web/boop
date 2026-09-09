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
    assert '"com.boop.alpha1.BoopDevMenuActivity"' in block
    assert "startActivity(new Intent().setClassName(" in block
    assert "return;" in block
