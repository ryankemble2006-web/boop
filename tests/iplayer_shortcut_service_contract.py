"""Integration contract: cancellation needs events from outside the player."""
from pathlib import Path
import xml.etree.ElementTree as ET
android = "{http://schemas.android.com/apk/res/android}"
for programme in ("eastenders", "casualty"):
    config = ET.parse(Path(programme + "-shortcut/res/xml/watch_now_service.xml")).getroot()
    assert not config.get(android + "packageNames"), "Outside-app window changes must reach the Home cancellation guard"
    assert "typeWindowStateChanged" in config.get(android + "accessibilityEventTypes", "")
print("PASS: both Home guards receive foreground changes outside iPlayer")
