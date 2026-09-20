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


def test_long_track_title_is_raised_without_moving_artist_or_progress():
    view = VIEW.read_text(encoding="utf-8")
    assert "place(title, left, 436f * unit, 440f * unit, 79f * unit);" in view
    assert "place(artist, left, 532f * unit, 440f * unit, 35f * unit);" in view
    assert "place(progress, left, 583f * unit, 397f * unit, 18f * unit);" in view
