#!/usr/bin/env python3
from pathlib import Path
import re

path = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
text = path.read_text(encoding="utf-8")
spoken_marker = "// BOOP_DEV_MENU_SPOKEN_ENTRY_V1"
settings_marker = "// BOOP_DEV_MENU_SETTINGS_ENTRY_V1"
scroll_marker = "// BOOP_VOICE_SETTINGS_SCROLL_V70_FIX"
launch_marker = "// BOOP_DEV_MENU_SAFE_LAUNCH_V70_FIX"
changed = False


def replace_once_literal(source: str, old: str, new: str, label: str) -> str:
    count = source.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one anchor, found {count}")
    return source.replace(old, new, 1)


if scroll_marker not in text:
    if "import android.widget.ScrollView;\n" not in text:
        text = replace_once_literal(
            text,
            "import android.widget.SeekBar;\n",
            "import android.widget.SeekBar;\nimport android.widget.ScrollView;\n",
            "voice settings ScrollView import",
        )

    if "    private ScrollView voiceSettingsScroll;\n" not in text:
        text = replace_once_literal(
            text,
            "    private LinearLayout voiceSettingsOverlay;\n",
            "    private LinearLayout voiceSettingsOverlay;\n"
            "    private ScrollView voiceSettingsScroll;\n",
            "voice settings ScrollView field",
        )

    add_pattern = re.compile(
        r"(?P<indent>[ \t]*)interactionSurface\.addView\(voiceSettingsOverlay,\s*"
        r"new FrameLayout\.LayoutParams\(\s*"
        r"FrameLayout\.LayoutParams\.MATCH_PARENT,\s*"
        r"FrameLayout\.LayoutParams\.MATCH_PARENT\)\);\s*"
        r"voiceSettingsOverlay\.bringToFront\(\);"
    )
    add_matches = list(add_pattern.finditer(text))
    if len(add_matches) != 1:
        raise SystemExit(
            f"voice settings ScrollView add block: expected one anchor, found {len(add_matches)}"
        )
    indent = add_matches[0].group("indent")
    add_block = (
        f'{indent}// BOOP_VOICE_SETTINGS_SCROLL_V70_FIX\n'
        f'{indent}voiceSettingsScroll = new ScrollView(this);\n'
        f'{indent}voiceSettingsScroll.setFillViewport(true);\n'
        f'{indent}voiceSettingsScroll.setBackgroundColor(Color.argb(236, 0, 0, 0));\n'
        f'{indent}voiceSettingsScroll.setContentDescription("Scrollable BOOP voice settings");\n'
        f'{indent}voiceSettingsScroll.addView(voiceSettingsOverlay, new ScrollView.LayoutParams(\n'
        f'{indent}        ScrollView.LayoutParams.MATCH_PARENT,\n'
        f'{indent}        ScrollView.LayoutParams.WRAP_CONTENT));\n'
        f'{indent}interactionSurface.addView(voiceSettingsScroll, new FrameLayout.LayoutParams(\n'
        f'{indent}        FrameLayout.LayoutParams.MATCH_PARENT,\n'
        f'{indent}        FrameLayout.LayoutParams.MATCH_PARENT));\n'
        f'{indent}voiceSettingsScroll.bringToFront();'
    )
    text = add_pattern.sub(add_block, text, count=1)

    hide_pattern = re.compile(
        r"(?P<indent>[ \t]*)if \(interactionSurface != null && voiceSettingsOverlay != null\) \{\s*"
        r"interactionSurface\.removeView\(voiceSettingsOverlay\);\s*"
        r"\}\s*"
        r"voiceSettingsOverlay = null;"
    )
    hide_matches = list(hide_pattern.finditer(text))
    if len(hide_matches) != 1:
        raise SystemExit(
            f"voice settings ScrollView hide block: expected one anchor, found {len(hide_matches)}"
        )
    indent = hide_matches[0].group("indent")
    hide_block = (
        f'{indent}if (interactionSurface != null && voiceSettingsScroll != null) {{\n'
        f'{indent}    interactionSurface.removeView(voiceSettingsScroll);\n'
        f'{indent}}} else if (interactionSurface != null && voiceSettingsOverlay != null) {{\n'
        f'{indent}    interactionSurface.removeView(voiceSettingsOverlay);\n'
        f'{indent}}}\n'
        f'{indent}voiceSettingsScroll = null;\n'
        f'{indent}voiceSettingsOverlay = null;'
    )
    text = hide_pattern.sub(hide_block, text, count=1)
    changed = True

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
    text = replace_once_literal(
        text, anchor, anchor + block, "local voice-settings speech"
    )
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
    text = replace_once_literal(text, anchor, block + anchor, "Voice Done button")
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
    text = replace_once_literal(text, anchor, block + anchor, "voice label helper")
    changed = True

if changed:
    path.write_text(text, encoding="utf-8")
    print("Internal BOOP dev menu speech/settings fixes materialized")
else:
    print("Internal BOOP dev menu speech/settings fixes already materialized")
