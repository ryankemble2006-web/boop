from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ACTIVITY = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldLauncherActivity.java"


def source():
    return ACTIVITY.read_text(encoding="utf-8")


def test_external_return_resets_focus_to_first_favourite():
    activity = source()
    assert "showHome(true);" in activity
    assert "focusFirstFavourite();" in activity
    assert "home.post(home::resetToFirstFavourite);" in activity


def test_close_media_also_resets_focus_to_first_favourite():
    activity = source()
    close_media = activity.split("@Override public void onCloseMediaApps()", 1)[1].split("}", 1)[0]
    assert "closeMediaApps(ShieldLauncherActivity.this)" in close_media
    assert "focusFirstFavourite();" in close_media


def test_internal_short_back_retains_existing_first_favourite_rule():
    activity = source()
    assert "((ShieldHomeView) currentView).resetToFirstFavourite();" in activity
    assert "showHome(true);" in activity
