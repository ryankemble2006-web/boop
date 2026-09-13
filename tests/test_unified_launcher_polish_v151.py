from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SHIELD = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome"


def test_wide_favourites_and_album_art_use_explicit_rounded_clipping():
    card = (SHIELD / "TvAppCardView.java").read_text(encoding="utf-8")
    now = (SHIELD / "ShieldNowPlayingView.java").read_text(encoding="utf-8")
    chrome = (SHIELD / "FocusChrome.java").read_text(encoding="utf-8")
    assert "clipRounded(iconView, HOME_ARTWORK_CORNER_DP)" in card
    assert "clipRounded(artwork, ARTWORK_CORNER_DP)" in now
    assert "static void clipRounded" in chrome
    assert "setOutlineProvider" in chrome


def test_progress_bar_is_focusable_and_seeks_ten_seconds():
    now = (SHIELD / "ShieldNowPlayingView.java").read_text(encoding="utf-8")
    activity = (SHIELD / "ShieldLauncherActivity.java").read_text(encoding="utf-8")
    manager = (SHIELD / "ShieldNowPlayingManager.java").read_text(encoding="utf-8")
    assert "progress.setFocusable(true)" in now
    assert "onNowPlayingSeekBy(-10_000L)" in now
    assert "onNowPlayingSeekBy(10_000L)" in now
    assert "onNowPlayingSeekBy(long deltaMs)" in activity
    assert "nowPlayingManager.seekBy(deltaMs)" in activity
    assert "public void seekBy(long deltaMs)" in manager


def test_settings_hold_stays_250ms_and_last_favourite_stops_at_edge():
    activity = (SHIELD / "ShieldLauncherActivity.java").read_text(encoding="utf-8")
    home = (SHIELD / "ShieldHomeView.java").read_text(encoding="utf-8")
    assert "SHIELD_SETTINGS_HOLD_MS = 250L" in activity
    assert "isLastFavourite" in home
    assert "KEYCODE_DPAD_RIGHT" in home
    assert "return true;" in home


def test_progress_focus_is_reachable_from_transport_controls():
    now = (SHIELD / "ShieldNowPlayingView.java").read_text(encoding="utf-8")
    assert "progress.requestFocus()" in now
    assert "playPauseButton.requestFocus()" in now
