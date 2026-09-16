"""Integration contract for both standalone Shield iPlayer shortcut helpers."""
from pathlib import Path

for programme in ("eastenders", "casualty"):
    root = Path(programme + "-shortcut")
    package = f"uk.local.{programme}"
    src = root / f"src/uk/local/{programme}"

    manifest = (root / "AndroidManifest.xml").read_text()
    service = (src / "WatchNowService.java").read_text()
    main = (src / "MainActivity.java").read_text()
    bridge = (src / "PlayerBridgeClient.java").read_text()
    control = (src / "PlayerControlService.java").read_text()
    reset = (src / "PlayerReset.java").read_text()

    assert "android.permission.INTERNET" in manifest, "Loopback ADB needs socket permission"
    assert 'android:name=".PlayerControlService"' in manifest, "Each shortcut must package its own cleanup controller"
    assert 'android:exported="false"' in manifest, "Standalone cleanup controller must remain app-private"

    assert "PlayerBridgeClient" in service, "Accessibility helper must use the app-local silent cleanup controller"
    assert "APPLICATION_DETAILS_SETTINGS" not in service, "Cleanup must not open Android App info"
    assert "guidedactions_item_title" not in service, "Cleanup must not click the Settings confirmation UI"

    assert f'new ComponentName("{package}","{package}.PlayerControlService")' in bridge, "Bridge must bind to its own APK"
    other = "uk.local.casualty" if programme == "eastenders" else "uk.local.eastenders"
    assert other not in bridge, "Standalone shortcut must not depend on its sibling APK"

    assert "am force-stop --user current com.nvidia.bbciplayer" in reset
    assert "iPlayer is still running" in reset, "Force-stop must verify the process is really gone"
    assert "iplayer-local-adb.key" in control, "Each APK must keep its own private ADB identity"
    assert "allowApproval ? 120000 : 5000" in control, "First launch may request one-time local ADB trust"

    assert "ACTION_APPLICATION_DEVELOPMENT_SETTINGS" in main, "First-run failure must offer Shield debugging setup"
    assert "Open debugging settings" in main

    assert "node.isFocused()" not in service, "Profile choice must not depend on whichever avatar currently has focus"
    assert "page.profile == null" in service and "UiPolicy.isExistingProfile(viewId, text)" in service, "Traversal must pick the first real profile tile"

print("PASS: both shortcuts are standalone, silently reset iPlayer, and choose the first real profile")
