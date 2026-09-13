"""Host ownership only. These checks do not certify visual appearance."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingView.java"


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
