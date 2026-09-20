from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
VIEW = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingView.java"


def test_now_playing_stack_uses_progress_bar_left_edge_as_datum():
    view = VIEW.read_text(encoding="utf-8")
    assert "progressParams = new LinearLayout.LayoutParams(" in view
    assert "LayoutParams.MATCH_PARENT, dp(8)" in view
    assert "controls.setPadding(0, 0, 0, 0);" in view
    assert "controls.setTranslationX(-dp(4));" in view
    assert "progressParams.rightMargin = dp(8);" in view
    assert "controls.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);" in view
    assert "details.addView(stateLabel, stateParams);" in view
    assert "details.addView(progress, progressParams);" in view
    assert "details.addView(controls, controlsParams);" in view
    assert "controls.setPadding(dp(8), 0, 0, 0);" not in view


def test_transport_keeps_prev_fixed_and_uses_three_buttons_with_existing_gap():
    view = VIEW.read_text(encoding="utf-8")
    assert 'private static final int CONTROL_GAP_DP = 10;' in view
    assert 'controls.setTranslationX(-dp(4));' in view
    assert 'previousButton = controlButton("Prev"' in view
    assert 'playPauseButton = controlButton("Play"' in view
    assert 'nextButton = controlButton("Next"' in view
    assert 'controlButton("Rew"' not in view
    assert 'controlButton("Fwd"' not in view
    controls = view[view.index('addControl(controls, previousButton);'):view.index('installEdgeFocusNavigation();')]
    assert controls.count("addControl(controls,") == 3
    assert "addControl(controls, previousButton);" in controls
    assert "addControl(controls, playPauseButton);" in controls
    assert "addControl(controls, nextButton);" in controls
