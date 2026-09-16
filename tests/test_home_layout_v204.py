from pathlib import Path

# Source-level layout contract only. Ryan retains visual acceptance on Shield.
ROOT = Path(__file__).resolve().parents[1]
HOME = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java"


def source() -> str:
    return HOME.read_text(encoding="utf-8")


def method(text: str, signature: str) -> str:
    start = text.index(signature)
    opening = text.index("{", start)
    depth = 0
    for index in range(opening, len(text)):
        if text[index] == "{":
            depth += 1
        elif text[index] == "}":
            depth -= 1
            if depth == 0:
                return text[start:index + 1]
    raise AssertionError("Unclosed method: " + signature)


def test_all_four_home_buttons_stay_in_the_top_nav_row():
    nav = method(source(), "private View navRow(Callbacks callbacks)")
    for caption in ("Apps", "Home settings", "Close media", "Shield settings"):
        assert f'navButton("{caption}"' in nav
    assert "row.addView(settings, settingsParams);" in nav
    assert "assistantBay" not in nav


def test_favourites_are_parked_under_the_now_playing_slot_with_matching_gaps():
    text = source()
    render = method(text, "public void render(\n            List<TvAppEntry> favourites")
    assert 'sectionTitle("Favourite apps")' not in render
    assert '"Favourite apps"' not in render
    assert "addView(navRow(callbacks)" in render
    assert "addSpacer(dp(16));" in render
    assert "new LayoutParams(LayoutParams.MATCH_PARENT, dp(182))" in render
    assert "new LayoutParams(1, dp(16))" in render
    assert "stageContent.addView(appRow(safeFavourites, callbacks), new LayoutParams(" in render
    assert "LayoutParams.MATCH_PARENT, dp(215)));" in render


def test_now_playing_hides_without_collapsing_its_reserved_slot():
    current = method(source(), "public void setNowPlaying(NowPlayingSnapshot snapshot)")
    assert "panel.setVisibility(visible ? VISIBLE : INVISIBLE);" in current
    assert "nowPlayingSpacer.setVisibility" not in current
    assert "homeAssistantPuppet.setHomeVisible(!visible);" in current


def test_idle_boop_is_a_separate_bottom_right_overlay():
    text = source()
    render = method(text, "public void render(\n            List<TvAppEntry> favourites")
    assert "FrameLayout homeStage = new FrameLayout(getContext());" in render
    assert "FrameLayout.LayoutParams assistantParams" in render
    assert "Gravity.END | Gravity.BOTTOM" in render
    assert "homeStage.addView(homeAssistantPuppet, assistantParams);" in render
