from pathlib import Path
import xml.etree.ElementTree as ET

ANDROID = "{http://schemas.android.com/apk/res/android}"
MANIFEST = Path("boop-build/BOOP-Alpha1/app/src/main/AndroidManifest.xml")


def _root():
    return ET.parse(MANIFEST).getroot()


def test_notification_components_and_permissions_are_exact():
    root = _root()
    permissions = {
        item.get(ANDROID + "name")
        for item in root.findall("uses-permission")
    }
    assert "android.permission.SYSTEM_ALERT_WINDOW" in permissions
    assert "android.permission.TURN_SCREEN_ON" in permissions
    assert "android.permission.VIBRATE" in permissions
    assert "android.permission.USE_FULL_SCREEN_INTENT" not in permissions
    assert "android.permission.QUERY_ALL_PACKAGES" not in permissions

    app = root.find("application")
    assert app is not None

    listener = next(
        (item for item in app.findall("service")
         if item.get(ANDROID + "name") == ".BoopNotificationListenerService"),
        None,
    )
    assert listener is not None
    assert listener.get(ANDROID + "permission") == "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
    assert listener.get(ANDROID + "exported") == "false"
    actions = {
        action.get(ANDROID + "name")
        for intent_filter in listener.findall("intent-filter")
        for action in intent_filter.findall("action")
    }
    assert "android.service.notification.NotificationListenerService" in actions

    activities = {
        item.get(ANDROID + "name"): item.get(ANDROID + "exported")
        for item in app.findall("activity")
    }
    for name in (
        ".BoopNotificationOnboardingActivity",
        ".BoopNotificationSettingsActivity",
        ".BoopNotificationLockActivity",
        ".BoopNotificationInboxActivity",
    ):
        assert activities.get(name) == "false"


def test_notification_feature_adds_no_accessibility_or_device_admin_authority():
    root = _root()
    app = root.find("application")
    assert app is not None

    service_actions = {
        action.get(ANDROID + "name")
        for service in app.findall("service")
        for intent_filter in service.findall("intent-filter")
        for action in intent_filter.findall("action")
    }
    assert "android.accessibilityservice.AccessibilityService" not in service_actions

    for receiver in app.findall("receiver"):
        assert receiver.get(ANDROID + "permission") != "android.permission.BIND_DEVICE_ADMIN"
        metadata_names = {
            item.get(ANDROID + "name")
            for item in receiver.findall("meta-data")
        }
        assert "android.app.device_admin" not in metadata_names
