#!/usr/bin/env python3
from pathlib import Path

path = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
text = path.read_text(encoding="utf-8")
spoken_marker = "// BOOP_DEV_MENU_SPOKEN_ENTRY_V1"
settings_marker = "// BOOP_DEV_MENU_SETTINGS_ENTRY_V1"
scroll_marker = "// BOOP_VOICE_SETTINGS_SCROLL_V70_FIX"
launch_marker = "// BOOP_DEV_MENU_SAFE_LAUNCH_V70_FIX"
changed = False

if scroll_marker not in text:
    import_anchor = "import android.widget.SeekBar;\n"
    if text.count(import_anchor) != 1:
        raise SystemExit(f"Expected one SeekBar import anchor, found {text.count(import_anchor)}")
    text = text.replace(
        import_anchor,
        import_anchor + "import android.widget.ScrollView;\n",
        1,
    )

    field_anchor = "    private LinearLayout voiceSettingsOverlay;\n"
    if text.count(field_anchor) != 1:
        raise SystemExit(f"Expected one voice settings field anchor, found {text.count(field_anchor)}")
    text = text.replace(
        field_anchor,
        field_anchor + "    private ScrollView voiceSettingsScroll;\n",
        1,
    )

    gravity_anchor = "        voiceSettingsOverlay.setGravity(Gravity.CENTER);\n"
    if text.count(gravity_anchor) != 1:
        raise SystemExit(f"Expected one voice settings gravity anchor, found {text.count(gravity_anchor)}")
    text = text.replace(
        gravity_anchor,
        "        // BOOP_VOICE_SETTINGS_SCROLL_V70_FIX\n"
        "        voiceSettingsOverlay.setGravity(Gravity.CENTER_HORIZONTAL);\n",
        1,
    )

    add_anchor = '''        interactionSurface.addView(voiceSettingsOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        voiceSettingsOverlay.bringToFront();
'''
    if text.count(add_anchor) != 1:
        raise SystemExit(f"Expected one voice settings add anchor, found {text.count(add_anchor)}")
    add_block = '''        voiceSettingsScroll = new ScrollView(this);
        voiceSettingsScroll.setFillViewport(true);
        voiceSettingsScroll.setBackgroundColor(Color.argb(236, 0, 0, 0));
        voiceSettingsScroll.setContentDescription("Scrollable BOOP voice settings");
        voiceSettingsScroll.addView(voiceSettingsOverlay, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));
        interactionSurface.addView(voiceSettingsScroll, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        voiceSettingsScroll.bringToFront();
'''
    text = text.replace(add_anchor, add_block, 1)

    hide_anchor = '''        if (interactionSurface != null && voiceSettingsOverlay != null) {
            interactionSurface.removeView(voiceSettingsOverlay);
        }
        voiceSettingsOverlay = null;
'''
    if text.count(hide_anchor) != 1:
        raise SystemExit(f"Expected one voice settings hide anchor, found {text.count(hide_anchor)}")
    hide_block = '''        if (interactionSurface != null && voiceSettingsScroll != null) {
            interactionSurface.removeView(voiceSettingsScroll);
        } else if (interactionSurface != null && voiceSettingsOverlay != null) {
            interactionSurface.removeView(voiceSettingsOverlay);
        }
        voiceSettingsScroll = null;
        voiceSettingsOverlay = null;
'''
    text = text.replace(hide_anchor, hide_block, 1)
    changed = True

if spoken_marker not in text:
    anchor = '''        if (BoopVoiceSettingsIntent.matches(transcript)) {
            showVoiceSettings();
            return;
        }

'''
    if text.count(anchor) != 1:
        raise SystemExit(f"Expected one local voice-settings speech anchor, found {text.count(anchor)}")
    block = '''        // BOOP_DEV_MENU_SPOKEN_ENTRY_V1
        if (BoopDevMenuIntent.matches(transcript)) {
            openDevMenu();
            return;
        }

'''
    text = text.replace(anchor, anchor + block, 1)
    changed = True

if settings_marker not in text:
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
            openDevMenu();
        });
        LinearLayout.LayoutParams devMenuParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        devMenuParams.setMargins(0, 0, 0, dp(12));
        voiceSettingsOverlay.addView(devMenu, devMenuParams);

'''
    text = text.replace(anchor, block + anchor, 1)
    changed = True

if launch_marker not in text:
    anchor = "    private TextView voiceSettingLabel(String text, float sizeSp, boolean bold) {\n"
    if text.count(anchor) != 1:
        raise SystemExit(f"Expected one voice label helper anchor, found {text.count(anchor)}")
    block = '''    // BOOP_DEV_MENU_SAFE_LAUNCH_V70_FIX
    private void openDevMenu() {
        // A wake-command result is in PROCESSING at this point. Release that state before
        // MainActivity pauses so the microphone/wake engine cannot be left half-owned.
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
        // Leave the SpeechRecognizer callback cleanly before switching activities.
        if (interactionSurface != null) {
            interactionSurface.post(launch);
        } else {
            launch.run();
        }
    }

'''
    text = text.replace(anchor, block + anchor, 1)
    changed = True

if changed:
    path.write_text(text, encoding="utf-8")
    print("Internal BOOP dev menu speech/settings fixes materialized")
else:
    print("Internal BOOP dev menu speech/settings fixes already materialized")
