"""Structural HOME stage contract only; this does not certify pixel placement."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java"


def source_text():
    return SOURCE.read_text(encoding="utf-8")


def test_primary_home_elements_have_independent_stage_ownership():
    source = source_text()
    assert "FrameLayout stage = new FrameLayout(getContext());" in source
    assert "stage.addView(navRow(callbacks), navParams);" in source
    assert "stage.addView(nowPlayingView, nowPlayingParams);" in source
    assert "stage.addView(appRow(safeFavourites, callbacks), favouritesParams);" in source


def test_favourites_are_centered_without_a_heading():
    source = source_text()
    assert 'sectionTitle("Favourite apps")' not in source
    assert "favouritesParams.gravity = Gravity.CENTER_VERTICAL;" in source


def test_now_playing_can_hide_without_reflowing_home():
    source = source_text()
    assert "panel.setVisibility(visible ? VISIBLE : INVISIBLE);" in source


def test_idle_assistant_has_a_separate_bottom_right_dock():
    source = source_text()
    assert "LinearLayout assistantDock = new LinearLayout(getContext());" in source
    assert "assistantDock.addView(homeAssistantPuppet" in source
    assert "assistantDockParams.gravity = Gravity.END | Gravity.BOTTOM;" in source
