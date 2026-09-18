from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
HOME = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java"
CARD = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/TvAppCardView.java"


def text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def test_favourite_artwork_starts_at_top_of_reserved_row():
    home = text(HOME)
    assert "favouriteRow.setGravity(Gravity.TOP);" in home
    assert "tile.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);" in home
    assert "tile.setPadding(0, 0, 0, 0);" in home


def test_wide_tiles_use_one_exact_16dp_visible_gap():
    home = text(HOME)
    assert "new LayoutParams(dp(230), dp(185))" in home
    assert "params.rightMargin = dp(16);" in home
    assert "addParams.rightMargin = dp(16);" in home


def test_home_banner_keeps_focus_geometry_but_lifts_when_grabbed():
    card = text(CARD)
    assert "setPadding(horizontal, 0, horizontal, 0);" in card
    assert "content.setGravity(homeFavourite" in card
    assert "? Gravity.TOP | Gravity.CENTER_HORIZONTAL" in card
    assert "HOME_ARTWORK_FOCUSED_SCALE = 1.00f" in card
    assert "HOME_ARTWORK_GRABBED_SCALE = 1.14f" in card
    assert ".translationZ(grabbed ? dp(10) : 0f)" in card


def test_now_playing_to_artwork_gap_remains_the_shared_16dp_reference():
    home = text(HOME)
    assert "stageContent.addView(nowPlayingSpacer, new LayoutParams(1, dp(16)));" in home
    assert "stageContent.addView(appRow(safeFavourites, callbacks), new LayoutParams(" in home
