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


def test_open_meteo_is_keyless_cached_and_bounded():
    repo = read("ShieldWeatherRepository.java")
    assert "https://api.open-meteo.com/v1/forecast?" in repo
    assert "apikey" not in repo.lower()
    assert "FRESH_MS=30L*60L*1000L" in repo
    assert "STALE_MS=6L*60L*60L*1000L" in repo
    assert "setConnectTimeout(5000)" in repo
    assert "setReadTimeout(5000)" in repo
    assert "Open-Meteo" in read("ShieldWeatherView.java")


def test_weather_fetch_is_background_only_and_reuses_existing_executor():
    activity = read("ShieldLauncherActivity.java")
    assert "executor.execute(() ->" in activity
    assert "weatherRepository.load(System.currentTimeMillis())" in activity
    assert "runOnUiThread(() ->" in activity
    assert "refreshWeather();" in activity


def test_shield_release_advances_past_user_confirmed_209_floor():
    gradle = (ROOT / "split/shield/build.gradle").read_text()
    verify = (ROOT / "split/verify-apks.py").read_text()
    assert "versionCode 210" in gradle
    assert "versionName '1.2.210-shield'" in gradle
    assert "version = 210 if body == 'shield' else 207" in verify


def test_home_view_diff_is_only_the_approved_weather_slot_hunks():
    import subprocess
    rel = "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java"
    base = subprocess.check_output(
        ["git", "show", "39ee5cce3c791ec7ee00082ad8913df3e9ea4e1e:" + rel], cwd=ROOT
    ).replace(b"\r\n", b"\n")
    expected = base
    replacements = [
        (b"    private ShieldNowPlayingView nowPlayingView;\n",
         b"    private ShieldNowPlayingView nowPlayingView;\n    private ShieldWeatherView weatherView;\n"),
        (b"        nowPlayingView = null;\n        homeAssistantPuppet = null;\n",
         b"        nowPlayingView = null;\n        weatherView = null;\n        homeAssistantPuppet = null;\n"),
    ]
    for old, new in replacements:
        assert expected.count(old) == 1
        expected = expected.replace(old, new, 1)
    old = (b"        nowPlayingView = new ShieldNowPlayingView(getContext());\n"
           b"        stageContent.addView(nowPlayingView, new LayoutParams(LayoutParams.MATCH_PARENT, dp(182)));\n")
    new = (b"        FrameLayout heroSlot = new FrameLayout(getContext());\n"
           b"        weatherView = new ShieldWeatherView(getContext());\n"
           b"        heroSlot.addView(weatherView, new FrameLayout.LayoutParams(\n"
           b"                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));\n"
           b"        nowPlayingView = new ShieldNowPlayingView(getContext());\n"
           b"        heroSlot.addView(nowPlayingView, new FrameLayout.LayoutParams(\n"
           b"                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));\n"
           b"        stageContent.addView(heroSlot, new LayoutParams(LayoutParams.MATCH_PARENT, dp(182)));\n")
    assert expected.count(old) == 1
    expected = expected.replace(old, new, 1)
    old = b"        panel.setVisibility(visible ? VISIBLE : INVISIBLE);\n"
    new = old + b"        if (weatherView != null) weatherView.setVisibility(visible ? INVISIBLE : VISIBLE);\n"
    assert expected.count(old) == 1
    expected = expected.replace(old, new, 1)
    marker = b"    @Override public boolean dispatchKeyEvent(KeyEvent event) {\n"
    weather_method = (b"    public void setWeather(WeatherSnapshot snapshot) {\n"
        b"        if (weatherView != null) {\n"
        b"            weatherView.bind(snapshot, System.currentTimeMillis());\n"
        b"            boolean mediaVisible = nowPlayingSnapshot != null\n"
        b"                    && NowPlayingSelectionPolicy.eligible(nowPlayingSnapshot.playbackState());\n"
        b"            weatherView.setVisibility(mediaVisible ? INVISIBLE : VISIBLE);\n"
        b"        }\n    }\n\n")
    assert expected.count(marker) == 1
    expected = expected.replace(marker, weather_method + marker, 1)
    assert (ROOT / rel).read_bytes().replace(b"\r\n", b"\n") == expected.replace(b"\r\n", b"\n")
