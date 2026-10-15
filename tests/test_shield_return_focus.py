from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ACTIVITY = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldLauncherActivity.java"


def source():
    return ACTIVITY.read_text(encoding="utf-8")


def test_external_return_restores_origin():
    activity = source()
    assert "showHome(false);" in activity
    assert "restoreHomeFocus();" in activity
    assert "rememberHomeFocus();" in activity
    assert "home.restoreFocus(homeReturnFocus)" in activity


def test_close_media_restores_origin():
    activity = source()
    close_media = activity.split("@Override public void onCloseMediaApps()", 1)[1].split("}", 1)[0]
    assert "closeMediaApps(ShieldLauncherActivity.this)" in close_media
    assert "restoreHomeFocus();" in close_media


def test_back_on_home_keeps_first_favourite_but_settings_back_restores_origin():
    activity = source()
    assert "((ShieldHomeView) currentView).resetToFirstFavourite();" in activity
    assert "showHome(false);" in activity
