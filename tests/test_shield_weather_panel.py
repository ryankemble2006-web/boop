from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome"


def read(name):
    return (SRC / name).read_text(encoding="utf-8")


def test_weather_occupies_existing_182dp_hero_slot_without_reflow():
    home = read("ShieldHomeView.java")
    assert "FrameLayout heroSlot = new FrameLayout(getContext());" in home
    assert "ShieldWeatherView(getContext())" in home
    assert "ShieldNowPlayingView(getContext())" in home
    assert "stageContent.addView(heroSlot, new LayoutParams(LayoutParams.MATCH_PARENT, dp(182)))" in home
    assert "stageContent.addView(appRow(safeFavourites, callbacks), new LayoutParams(" in home
    assert "LayoutParams.MATCH_PARENT, dp(215))" in home


def test_now_playing_remains_authoritative_and_weather_never_takes_focus():
    home = read("ShieldHomeView.java")
    weather = read("ShieldWeatherView.java")
    assert "weatherView.setVisibility(visible ? INVISIBLE : VISIBLE)" in home
    assert "mediaVisible ? INVISIBLE : VISIBLE" in home
    assert "setFocusable(false)" in weather
    assert "setClickable(false)" in weather


def test_weather_content_columns_fill_available_height():
    weather = read("ShieldWeatherView.java")
    assert "top.addView(current(s),new LayoutParams(0,LayoutParams.MATCH_PARENT,3f))" in weather
    assert "top.addView(hours(s),new LayoutParams(0,LayoutParams.MATCH_PARENT,4f))" in weather
    assert "top.addView(days(s),new LayoutParams(0,LayoutParams.MATCH_PARENT,3f))" in weather


def test_weather_card_matches_now_playing_surface_exactly():
    weather = read("ShieldWeatherView.java")
    now_playing = read("ShieldNowPlayingView.java")
    for chrome in (
        "setColor(Color.rgb(16,16,16))",
        "setCornerRadius(dp(14))",
        "setStroke(dp(1),Color.rgb(48,48,48))",
    ):
        assert chrome in weather
    assert "background.setColor(Color.rgb(16, 16, 16));" in now_playing
    assert "background.setCornerRadius(dp(14));" in now_playing
    assert "background.setStroke(dp(1), Color.rgb(48, 48, 48));" in now_playing


def test_open_meteo_is_keyless_cached_bounded_and_network_enabled():
    repo = read("ShieldWeatherRepository.java")
    manifest = (ROOT / "split/shield/AndroidManifest.xml").read_text()
    assert "https://api.open-meteo.com/v1/forecast?" in repo
    assert "apikey" not in repo.lower()
    assert "FRESH_MS=30L*60L*1000L" in repo
    assert "STALE_MS=6L*60L*60L*1000L" in repo
    assert "setConnectTimeout(5000)" in repo
    assert "setReadTimeout(5000)" in repo
    assert '<uses-permission android:name="android.permission.INTERNET" />' in manifest
    assert "Open-Meteo" in read("ShieldWeatherView.java")


def test_weather_fetch_is_background_only_and_reuses_existing_executor():
    activity = read("ShieldLauncherActivity.java")
    assert "executor.execute(() ->" in activity
    assert "weatherRepository.load(System.currentTimeMillis())" in activity
    assert "runOnUiThread(() ->" in activity
    assert "refreshWeather();" in activity


def test_shield_release_keeps_aligned_weather_in_v215():
    gradle = (ROOT / "split/shield/build.gradle").read_text()
    verify = (ROOT / "split/verify-apks.py").read_text()
    assert "versionCode 215" in gradle
    assert "versionName '1.2.215-shield'" in gradle
    assert "version = 215 if body == 'shield' else 207" in verify
    assert "android.permission.INTERNET" in verify

def test_weather_sections_share_one_header_grid_and_centered_footer():
    weather = read("ShieldWeatherView.java")
    assert "private static final int HEADER_DP=26;" in weather
    assert 'addHeader(box,s.location);' in weather
    assert 'addHeader(box,"Next 4 hours");' in weather
    assert 'addHeader(box,"3 day forecast");' in weather
    assert "title.setGravity(Gravity.CENTER)" in weather
    assert "body.setGravity(Gravity.CENTER)" in weather
    assert "wind.setGravity(Gravity.CENTER)" in weather
    assert "sun.setGravity(Gravity.CENTER)" in weather
    assert "source.setGravity(Gravity.CENTER)" in weather
    assert "foot.addView(wind,new LayoutParams(0,LayoutParams.MATCH_PARENT,3f))" in weather
    assert "foot.addView(sun,new LayoutParams(0,LayoutParams.MATCH_PARENT,4f))" in weather
    assert "foot.addView(source,new LayoutParams(0,LayoutParams.MATCH_PARENT,3f))" in weather
    assert weather.count("box.setPadding(dp(12),0,dp(12),0)") == 3
