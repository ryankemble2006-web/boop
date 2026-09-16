"""Integration contract for both Shield iPlayer shortcut helpers."""
from pathlib import Path
import xml.etree.ElementTree as ET

android = "{http://schemas.android.com/apk/res/android}"
for programme in ("eastenders", "casualty"):
    root = Path(programme + "-shortcut")
    config = ET.parse(root / "res/xml/watch_now_service.xml").getroot()
    assert not config.get(android + "packageNames"), "Settings and Home window changes must reach the helper"
    assert "typeWindowStateChanged" in config.get(android + "accessibilityEventTypes", "")
    flags = config.get(android + "accessibilityFlags", "")
    assert "flagReportViewIds" in flags
    assert "flagRetrieveInteractiveWindows" in flags, "Guided Force-stop confirmation must be visible to the helper"

    service = (root / f"src/uk/local/{programme}/WatchNowService.java").read_text()
    manifest = (root / "AndroidManifest.xml").read_text()
    assert "APPLICATION_DETAILS_SETTINGS" in service, "Cold cleanup must open Android TV App info"
    assert "guidedactions_item_title" in service, "Force-stop confirmation must use the Shield's stable action view id"
    assert "getWindows()" in service, "Cleanup must inspect Android TV guided confirmation windows, not only the active root"
    assert "PlayerBridgeClient" not in service, "Shortcut cleanup must not depend on a private ADB approval"
    assert "PlayerControlService" not in manifest, "Obsolete ADB broker must not be exported"
    assert "android.permission.INTERNET" not in manifest, "Settings force-stop does not need network permission"

print("PASS: both shortcuts can inspect Shield App info and guided Force-stop confirmation windows")
