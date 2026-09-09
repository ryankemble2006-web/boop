#!/usr/bin/env python3
from pathlib import Path

path = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
text = path.read_text(encoding="utf-8")
spoken_marker = "// BOOP_DEV_MENU_SPOKEN_ENTRY_V1"
settings_marker = "// BOOP_DEV_MENU_SETTINGS_ENTRY_V1"
launch_marker = "// BOOP_DEV_MENU_SAFE_LAUNCH_V70_FIX"
changed = False


def replace_once(source: str, old: str, new: str, label: str) -> str:
    count = source.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one anchor, found {count}")
    return source.replace(old, new, 1)


if spoken_marker not in text:
    anchor = '''        if (BoopVoiceSettingsIntent.matches(transcript)) {
            showVoiceSettings();
            return;
        }

'''
    block = '''        // BOOP_DEV_MENU_SPOKEN_ENTRY_V1
        if (BoopDevMenuIntent.matches(transcript)) {
            openDevMenu();
            return;
        }

'''
    text = replace_once(text, anchor, anchor + block, "local voice-settings speech")
    changed = True

if settings_marker not in text:
    anchor = "        Button done = new Button(this);\n"
    block = '''        // BOOP_DEV_MENU_SETTINGS_ENTRY_V1
        Button devMenu = new Button(this);
        devMenu.setText("Dev menu");
        devMenu.setTextSize(19f);
        devMenu.setTextColor(Color.WHITE);
        devMenu.setBackgroundColor(Color.rgb(42, 42, 42));
        devMenu.setContentDescription("Open BOOP developer demos");
        devMenu.setOnClickListener(v -> {
            hideVoiceSettings();
            openDevMenu();
        });
        LinearLayout.LayoutParams devMenuParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        devMenuParams.setMargins(0, 0, 0, dp(12));
        voiceSettingsOverlay.addView(devMenu, devMenuParams);

'''
    text = replace_once(text, anchor, block + anchor, "Voice Done button")
    changed = True

if launch_marker not in text:
    anchor = "    private TextView voiceSettingLabel(String text, float sizeSp, boolean bold) {\n"
    block = '''    // BOOP_DEV_MENU_SAFE_LAUNCH_V70_FIX
    private void openDevMenu() {
        // A wake-command result is in PROCESSING here. Release it before MainActivity
        // pauses so the wake/microphone state is not left half-owned during the hop.
        if (wakeCoordinator != null) {
            wakeCoordinator.finishWakeProcessing();
        }
        Runnable launch = () -> {
            try {
                startActivity(new Intent(MainActivity.this, BoopDevMenuActivity.class));
            } catch (RuntimeException launchFailure) {
                Toast.makeText(MainActivity.this, "BOOP Dev couldn't open.", Toast.LENGTH_SHORT).show();
                wakeFaceForInteraction();
            }
        };
        // Leave the SpeechRecognizer callback before switching activities.
        if (interactionSurface != null) {
            interactionSurface.post(launch);
        } else {
            launch.run();
        }
    }

'''
    text = replace_once(text, anchor, block + anchor, "voice label helper")
    changed = True

if changed:
    path.write_text(text, encoding="utf-8")
    print("Internal BOOP dev menu speech/settings entries materialized")
else:
    print("Internal BOOP dev menu speech/settings entries already materialized")
