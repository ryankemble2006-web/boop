from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
HOME = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome"


def read(name):
    return (HOME / name).read_text(encoding="utf-8")


def test_launcher_settings_places_hue_slider_under_smart_home_panel():
    settings = read("ShieldHomeSettingsView.java")
    panel = settings.index('Smart home panel: ')
    colour = settings.index('Highlight colour')
    hint = settings.index("Uses your existing")
    assert panel < colour < hint
    assert "SeekBar accentSlider" in settings
    assert "accentSlider.setMax(359);" in settings
    assert "callbacks.onSetAccentHue(progress);" in settings


def test_accent_is_persistent_and_defaults_to_existing_boop_cyan():
    store = read("ShieldHomeStore.java")
    chrome = read("BoopTvChrome.java")
    assert 'KEY_ACCENT_HUE = "accent_hue_v1"' in store
    assert "DEFAULT_ACCENT_HUE = 204" in store
    assert "new ShieldHomeStore(context).accentHue()" in chrome
    assert "return Color.rgb(77, 184, 255);" in chrome
    assert "Color.HSVToColor" in chrome


def test_home_icons_weather_and_shared_chrome_use_saved_accent():
    home = read("ShieldHomeView.java")
    weather = read("ShieldWeatherView.java")
    now_playing = read("ShieldNowPlayingView.java")
    room = read("ShieldRoomPanelView.java")
    assert "icon.setTint(FocusChrome.accentColor(getContext()));" in home
    assert "private int accent(){ return FocusChrome.accentColor(getContext()); }" in weather
    assert "CYAN" not in weather
    assert "FocusChrome.accentColor(context)" in now_playing
    assert "FocusChrome.accentColor(getContext())" in room
