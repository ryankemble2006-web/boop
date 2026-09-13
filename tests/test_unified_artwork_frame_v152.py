"""Artwork-only focus contract, updated after the rejected thick-frame trial."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SHIELD = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome"


def test_artwork_focus_frame_is_thin_and_matches_the_clip_corner():
    text = (SHIELD / "FocusChrome.java").read_text(encoding="utf-8")
    assert "ARTWORK_BORDER_DP = 4;" in text
    assert "BORDER_DP = 4;" in text
    method = text.split("static GradientDrawable artworkOutline(", 1)[1].split(
        "static GradientDrawable outline(", 1)[0]
    assert "artworkStrokeRadius" in method
    assert "outline.setStroke(strokePx, accentColor(context))" in method
    normal = text.split("static GradientDrawable outline(", 1)[1]
    assert "setCornerRadius(dp(context, cornerRadiusDp))" in normal
    assert "setStroke(dp(context, BORDER_DP), accentColor(context))" in normal


def test_only_artwork_surfaces_use_the_reshaped_frame():
    card = (SHIELD / "TvAppCardView.java").read_text(encoding="utf-8")
    now = (SHIELD / "ShieldNowPlayingView.java").read_text(encoding="utf-8")
    assert "FocusChrome.artworkOutline(getContext(), HOME_ARTWORK_CORNER_DP)" in card
    assert "FocusChrome.artworkOutline(getContext(), ARTWORK_CORNER_DP)" in now
