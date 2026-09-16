"""Host ownership only. These checks do not certify visual appearance."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingView.java"
HOME_SOURCE = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java"


def test_row_owns_a_persistent_bay_not_the_transient_puppet():
    source = SOURCE.read_text(encoding="utf-8")
    assert "row.addView(puppetView," not in source, "Hiding the puppet must not remove row space"
    assert "LinearLayout puppetBay = new LinearLayout(context);" in source
    assert "row.addView(puppetBay," in source
    assert "puppetBay.addView(puppetView," in source
    assert not re.search(r"puppetBay\.(?:setVisibility|removeAllViews|removeView)\(", source)


def test_bay_does_not_create_a_second_face_or_remote_focus_target():
    source = SOURCE.read_text(encoding="utf-8")
    assert source.count("new ShieldNowPlayingPuppetView(context)") == 1
    assert "puppetBay.setFocusable(false);" in source
    assert "puppetBay.setClickable(false);" in source


def test_home_keeps_one_canonical_assistant_presence_when_media_is_gone():
    home = HOME_SOURCE.read_text(encoding="utf-8")
    assert "private ShieldNowPlayingPuppetView homeAssistantPuppet;" in home
    assert "homeAssistantPuppet = new ShieldNowPlayingPuppetView(getContext());" in home
    assert "homeAssistantPuppet.setPresentationOwner(com.boop.shared.BoopState.Owner.NONE);" in home
    assert "homeAssistantPuppet.setSnapshot(idleAssistantSnapshot());" in home
    assert "homeAssistantPuppet.setHomeVisible(!visible);" in home
    assert "PlaybackState.STATE_BUFFERING" in home, "idle presence should reuse the canonical idle media pose"


def test_home_assistant_sits_above_shield_settings():
    home = HOME_SOURCE.read_text(encoding="utf-8")
    assert "LinearLayout assistantBay = new LinearLayout(getContext());" in home
    assert "assistantBay.setOrientation(VERTICAL);" in home
    assert "assistantBay.addView(homeAssistantPuppet, assistantParams);" in home
    assert "assistantBay.addView(settings, settingsParams);" in home
    assert home.index("assistantBay.addView(homeAssistantPuppet, assistantParams);") < home.index("assistantBay.addView(settings, settingsParams);")
