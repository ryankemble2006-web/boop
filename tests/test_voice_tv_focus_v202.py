"""Non-visual TV focus contracts; Shield proves actual rendering/navigation."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CHROME = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/BoopTvChrome.java"
VOICE_PATCH = ROOT / "scripts/patch-unified-v200-voice-ui.py"


def test_voice_focus_is_connected_after_scroll_attachment():
    patch = VOICE_PATCH.read_text()
    call = "com.boop.shieldhome.BoopTvChrome.prepareVoiceSettings("
    assert call in patch
    assert "voiceSettingsScroll, voiceSettingsOverlay, pitchSlider" in patch


def test_home_blue_is_the_single_tv_focus_colour_for_buttons_and_editors():
    chrome = CHROME.read_text()
    # Home's canonical blue remains the one source of truth.
    assert "return Color.rgb(77, 184, 255);" in chrome
    assert "setStroke(dp(context, BORDER_DP), accentColor(context))" in chrome
    # Ordinary clickable TextViews/buttons use the same canonical focused drawable.
    assert "applyState((TextView) newFocus, true)" in chrome
    assert "filled(view.getContext(), NORMAL_FILL, CORNER_DP, focused)" in chrome
    # Native editors/sliders must use that blue as their control colour as well,
    # not merely receive a box around an otherwise teal slider.
    assert "setThumbTintList(focusAccent" in chrome
    assert "setProgressTintList(focusAccent" in chrome
    assert "setProgressBackgroundTintList(focusAccent" in chrome
    assert "accentColor(slider.getContext())" in chrome
    # The visible focus box is still present around sliders/editors.
    assert "StateListDrawable outline" in chrome
    assert "android.R.attr.state_focused" in chrome
    # Do not replace key/touch/focus handlers belonging to the controls themselves.
    assert "setOnKeyListener" not in chrome
    assert "setOnTouchListener" not in chrome
    assert "setOnFocusChangeListener" not in chrome


def test_voice_containers_cannot_steal_dpad_focus_and_pitch_is_initial_target():
    chrome = CHROME.read_text()
    assert "setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS)" in chrome
    assert "group.setFocusable(false)" in chrome
    assert "initialFocus.requestFocus()" in chrome
