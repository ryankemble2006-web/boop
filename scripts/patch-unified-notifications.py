#!/usr/bin/env python3
from pathlib import Path

path = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
text = path.read_text(encoding="utf-8")
marker = "// BOOP_NOTIFICATION_SETTINGS_ENTRY_V1"
if marker in text:
    print("Notification settings entry already materialized")
    raise SystemExit(0)

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

path.write_text(text.replace(anchor, block + anchor, 1), encoding="utf-8")
print("Notification settings entry materialized")
