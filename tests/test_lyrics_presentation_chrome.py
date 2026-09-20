from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VIEW = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldLyricsView.java"


def test_lyrics_artwork_hard_clips_bitmap_on_canvas():
    view = VIEW.read_text(encoding="utf-8")
    assert "artworkClip.addRoundRect(artworkBounds, 9f * unit, 9f * unit, Path.Direction.CW);" in view
    assert "canvas.clipPath(artworkClip);" in view
    assert "canvas.restoreToCount(clipped);" in view
    assert "FocusChrome.clipRounded(artwork, 9);" not in view
    assert "artwork.setClipToOutline(false);" in view


def test_lyrics_provider_credit_is_not_drawn():
    view = VIEW.read_text(encoding="utf-8")
    assert "credit = label(" not in view
    assert "place(credit," not in view
    assert "credit.setText(" not in view
