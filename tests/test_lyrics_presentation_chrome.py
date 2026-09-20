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


def test_long_track_title_is_single_line_marquee_once_at_original_position():
    view = VIEW.read_text(encoding="utf-8")
    assert "title.setSingleLine(true);" in view
    assert "title.setHorizontallyScrolling(true);" in view
    assert "title.setEllipsize(TextUtils.TruncateAt.MARQUEE);" in view
    assert "title.setMarqueeRepeatLimit(1);" in view
    assert "title.setSelected(true);" in view
    assert "place(title, left, 448f * unit, 440f * unit, 42f * unit);" in view
    assert "place(artist, left, 532f * unit, 440f * unit, 35f * unit);" in view
    assert "place(progress, left, 583f * unit, 397f * unit, 18f * unit);" in view


def test_left_music_column_uses_progress_centre_and_three_transport_buttons():
    view = VIEW.read_text(encoding="utf-8")
    assert "float progressWidth = 397f * unit;" in view
    assert "float progressCenter = left + progressWidth * 0.5f;" in view
    assert "float artLeft = progressCenter - artSize * 0.5f;" in view
    assert "place(artwork, artLeft, 116f * unit, artSize, artSize);" in view
    assert "place(title, left, 448f * unit, progressWidth, 42f * unit);" in view
    assert "place(artist, left, 520f * unit, progressWidth, 35f * unit);" in view
    assert "title.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);" in view
    assert "artist.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);" in view
    assert "private final TransportButton[] buttons = new TransportButton[3];" in view
    assert "transportLeft = progressCenter - transportSpan * 0.5f;" in view
    assert "Rewind ten seconds" not in view
    assert "Forward ten seconds" not in view
