#!/usr/bin/env python3
from pathlib import Path

path = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
text = path.read_text(encoding="utf-8")
marker = "// BOOP_DEV_MENU_SETTINGS_ENTRY_V1"

if marker in text:
    print("Internal BOOP dev menu entry already materialized")
    raise SystemExit(0)

anchor = "        Button done = new Button(this);\n"
if text.count(anchor) != 1:
    raise SystemExit(f"Expected one Voice Done anchor, found {text.count(anchor)}")

block = '''        // BOOP_DEV_MENU_SETTINGS_ENTRY_V1
        Button devMenu = new Button(this);
        devMenu.setText("Dev menu");
        devMenu.setTextSize(19f);
        devMenu.setTextColor(Color.WHITE);
        devMenu.setBackgroundColor(Color.rgb(42, 42, 42));
        devMenu.setContentDescription("Open BOOP developer demos");
        devMenu.setOnClickListener(v -> {
            hideVoiceSettings();
            startActivity(new Intent().setClassName(
                    getPackageName(), "com.boop.alpha1.BoopDevMenuActivity"));
        });
        LinearLayout.LayoutParams devMenuParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        devMenuParams.setMargins(0, 0, 0, dp(12));
        voiceSettingsOverlay.addView(devMenu, devMenuParams);

'''

path.write_text(text.replace(anchor, block + anchor, 1), encoding="utf-8")
print("Internal BOOP dev menu entry materialized")
