from pathlib import Path
import xml.etree.ElementTree as ET

ANDROID = "{http://schemas.android.com/apk/res/android}"
MANIFEST = Path("source/AndroidManifest.xml")


def _root():
    return ET.parse(MANIFEST).getroot()


def test_notification_runtime_permissions_are_declared():
    root = _root()
    permissions = {
        item.get(ANDROID + "name")
        for item in root.findall("uses-permission")
    }
    assert "android.permission.SYSTEM_ALERT_WINDOW" in permissions
    assert "android.permission.TURN_SCREEN_ON" in permissions


def test_notification_listener_service_is_bound_by_android_only():
    root = _root()
    app = root.find("application")
    assert app is not None
    service = next(
        (item for item in app.findall("service")
         if item.get(ANDROID + "name") == ".BoopNotificationListenerService"),
        None,
    )
    assert service is not None
    assert service.get(ANDROID + "permission") == "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
    assert service.get(ANDROID + "exported") == "false"
    actions = {
        action.get(ANDROID + "name")
        for intent_filter in service.findall("intent-filter")
        for action in intent_filter.findall("action")
    }
    assert "android.service.notification.NotificationListenerService" in actions


def test_notification_activities_are_private_components():
    root = _root()
    app = root.find("application")
    assert app is not None
    activities = {
        item.get(ANDROID + "name"): item
        for item in app.findall("activity")
    }
    for name in (
        ".BoopNotificationOnboardingActivity",
        ".BoopNotificationSettingsActivity",
        ".BoopNotificationLockActivity",
        ".BoopNotificationInboxActivity",
    ):
        assert name in activities
        assert activities[name].get(ANDROID + "exported") == "false"
