from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CHROME = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/FocusChrome.java"
CARD = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/TvAppCardView.java"
NOW = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingView.java"


def test_artwork_focus_frame_is_thicker_than_normal_chrome():
    text = CHROME.read_text(encoding="utf-8")
    assert "ARTWORK_BORDER_DP = 8" in text
    assert "artworkOutline" in text


def test_only_artwork_surfaces_use_thick_frame():
    card = CARD.read_text(encoding="utf-8")
    now = NOW.read_text(encoding="utf-8")
    assert "FocusChrome.artworkOutline(getContext(), HOME_ARTWORK_CORNER_DP)" in card
    assert "FocusChrome.artworkOutline(getContext(), ARTWORK_CORNER_DP)" in now
