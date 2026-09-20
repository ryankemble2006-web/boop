from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VIEW = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldLyricsView.java"


def test_lyrics_artwork_uses_shared_home_banner_corner_clipping():
    view = VIEW.read_text(encoding="utf-8")
    assert "FocusChrome.clipRounded(artwork, 9);" in view
    assert "FocusChrome.filled(context, Color.rgb(16, 24, 29), 9, false)" in view
    assert "artwork.setOutlineProvider(new ViewOutlineProvider()" not in view


def test_lyrics_provider_credit_is_not_drawn():
    view = VIEW.read_text(encoding="utf-8")
    assert "credit = label(" not in view
    assert "place(credit," not in view
    assert "credit.setText(" not in view
