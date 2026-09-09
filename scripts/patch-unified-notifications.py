#!/usr/bin/env python3
from pathlib import Path
import subprocess
import sys

path = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
text = path.read_text(encoding="utf-8")
changed = False

settings_marker = "// BOOP_NOTIFICATION_SETTINGS_ENTRY_V1"
if settings_marker not in text:
    anchor = "        Button done = new Button(this);\n"
    if text.count(anchor) != 1:
        raise SystemExit(f"Expected one Voice Done anchor, found {text.count(anchor)}")
    block = '''        // BOOP_NOTIFICATION_SETTINGS_ENTRY_V1
        Button notifications = new Button(this);
        notifications.setText("Notifications");
        notifications.setTextSize(19f);
        notifications.setTextColor(Color.WHITE);
        notifications.setBackgroundColor(Color.rgb(42, 42, 42));
        notifications.setContentDescription("Notification settings");
        notifications.setOnClickListener(v -> {
            hideVoiceSettings();
            startActivity(new Intent().setClassName(
                    getPackageName(), "com.boop.alpha1.BoopNotificationSettingsActivity"));
        });
        LinearLayout.LayoutParams notificationParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        notificationParams.setMargins(0, 0, 0, dp(12));
        voiceSettingsOverlay.addView(notifications, notificationParams);

'''
    text = text.replace(anchor, block + anchor, 1)
    changed = True

host_marker = "// BOOP_NOTIFICATION_IN_PLACE_HOST_V1"
if host_marker not in text:
    field_anchor = "    private FrameLayout interactionSurface;\n"
    if text.count(field_anchor) != 1:
        raise SystemExit(f"Expected one interactionSurface field, found {text.count(field_anchor)}")
    text = text.replace(
            field_anchor,
            field_anchor + "    private BoopNotificationInPlaceController notificationInPlaceController;\n",
            1)

    create_anchor = "        setContentView(interactionSurface);\n        face.showIdleBlackImmediately();\n"
    if text.count(create_anchor) != 1:
        raise SystemExit(f"Expected one Wall content-view anchor, found {text.count(create_anchor)}")
    create_block = '''        setContentView(interactionSurface);
        // BOOP_NOTIFICATION_IN_PLACE_HOST_V1
        notificationInPlaceController = new BoopNotificationInPlaceController(interactionSurface);
        face.showIdleBlackImmediately();
'''
    text = text.replace(create_anchor, create_block, 1)

    resume_anchor = "    protected void onResume() {\n        super.onResume();\n"
    if text.count(resume_anchor) != 1:
        raise SystemExit(f"Expected one onResume anchor, found {text.count(resume_anchor)}")
    resume_block = '''    protected void onResume() {
        super.onResume();
        if (notificationInPlaceController != null) {
            try {
                BoopNotificationRuntime.get(this).registerWallHost(notificationInPlaceController);
            } catch (RuntimeException ignored) {
                // Notification setup is optional; normal Wall behavior continues.
            }
        }
'''
    text = text.replace(resume_anchor, resume_block, 1)

    pause_anchor = "    protected void onPause() {\n"
    if text.count(pause_anchor) != 1:
        raise SystemExit(f"Expected one onPause anchor, found {text.count(pause_anchor)}")
    pause_block = '''    protected void onPause() {
        if (notificationInPlaceController != null) {
            try {
                BoopNotificationRuntime.get(this).unregisterWallHost(notificationInPlaceController);
            } catch (RuntimeException ignored) {
                // Notification setup is optional; normal Wall behavior continues.
            }
        }
'''
    text = text.replace(pause_anchor, pause_block, 1)
    changed = True

if changed:
    path.write_text(text, encoding="utf-8")
    print("Notification settings and Wall presentation host materialized")
else:
    print("Notification settings and Wall presentation host already materialized")

asset_materializer = Path(__file__).with_name("materialize-boop-notification-assets.py")
subprocess.run([sys.executable, str(asset_materializer)], check=True)
