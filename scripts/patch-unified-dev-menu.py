#!/usr/bin/env python3
from pathlib import Path

path = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
text = path.read_text(encoding="utf-8")
changed = False

spoken_marker = "// BOOP_DEV_MENU_SPOKEN_ENTRY_V2"
settings_marker = "// BOOP_DEV_MENU_SETTINGS_ENTRY_V2"
scroll_marker = "// BOOP_VOICE_SETTINGS_SCROLL_V1"
helper_marker = "// BOOP_DEV_MENU_DEFERRED_LAUNCH_V1"

if spoken_marker not in text:
    anchor = '''        if (BoopVoiceSettingsIntent.matches(transcript)) {
            showVoiceSettings();
            return;
        }

'''
    if text.count(anchor) != 1:
        raise SystemExit(f"Expected one local voice-settings speech anchor, found {text.count(anchor)}")
    block = '''        // BOOP_DEV_MENU_SPOKEN_ENTRY_V2
        if (BoopDevMenuIntent.matches(transcript)) {
            openDevMenuSafely();
            return;
        }

'''
    text = text.replace(anchor, anchor + block, 1)
    changed = True

if settings_marker not in text:
    anchor = "        Button done = new Button(this);\n"
    if text.count(anchor) != 1:
        raise SystemExit(f"Expected one Voice Done anchor, found {text.count(anchor)}")

    block = '''        // BOOP_DEV_MENU_SETTINGS_ENTRY_V2
        Button devMenu = new Button(this);
        devMenu.setText("Dev menu");
        devMenu.setTextSize(19f);
        devMenu.setTextColor(Color.WHITE);
        devMenu.setBackgroundColor(Color.rgb(42, 42, 42));
        devMenu.setContentDescription("Open BOOP developer demos");
        devMenu.setOnClickListener(v -> openDevMenuSafely());
        LinearLayout.LayoutParams devMenuParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64));
        devMenuParams.setMargins(0, 0, 0, dp(12));
        voiceSettingsOverlay.addView(devMenu, devMenuParams);

'''
    text = text.replace(anchor, block + anchor, 1)
    changed = True

if scroll_marker not in text:
    attachment = '''        interactionSurface.addView(voiceSettingsOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        voiceSettingsOverlay.bringToFront();
'''
    if text.count(attachment) != 1:
        raise SystemExit(f"Expected one Voice Settings attachment block, found {text.count(attachment)}")
    scroll_block = '''        // BOOP_VOICE_SETTINGS_SCROLL_V1
        android.widget.ScrollView voiceSettingsScroller = new android.widget.ScrollView(this);
        voiceSettingsScroller.setFillViewport(true);
        voiceSettingsScroller.setVerticalScrollBarEnabled(true);
        voiceSettingsScroller.addView(voiceSettingsOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        interactionSurface.addView(voiceSettingsScroller, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        voiceSettingsScroller.bringToFront();
'''
    text = text.replace(attachment, scroll_block, 1)

    hide_signature = "    private void hideVoiceSettings() {\n"
    hide_start = text.find(hide_signature)
    if hide_start < 0:
        raise SystemExit("Expected hideVoiceSettings() method")
    hide_end = text.find("    private int dp(int value) {\n", hide_start)
    if hide_end < 0:
        raise SystemExit("Expected dp() helper after hideVoiceSettings()")
    hide_method = text[hide_start:hide_end]
    hide_block = '''        if (interactionSurface != null && voiceSettingsOverlay != null) {
            interactionSurface.removeView(voiceSettingsOverlay);
        }
'''
    if hide_method.count(hide_block) != 1:
        raise SystemExit(
            "Expected one Voice Settings removal block inside hideVoiceSettings(), "
            f"found {hide_method.count(hide_block)}"
        )
    hide_replacement = '''        if (interactionSurface != null && voiceSettingsOverlay != null) {
            android.view.ViewParent parent = voiceSettingsOverlay.getParent();
            if (parent instanceof View) {
                interactionSurface.removeView((View) parent);
            } else {
                interactionSurface.removeView(voiceSettingsOverlay);
            }
        }
'''
    hide_method = hide_method.replace(hide_block, hide_replacement, 1)
    text = text[:hide_start] + hide_method + text[hide_end:]
    changed = True

if helper_marker not in text:
    anchor = "    private int dp(int value) {\n"
    if text.count(anchor) != 1:
        raise SystemExit(f"Expected one dp helper anchor, found {text.count(anchor)}")
    helper = '''    // BOOP_DEV_MENU_DEFERRED_LAUNCH_V1
    private void openDevMenuSafely() {
        if (voiceSettingsOpen) {
            hideVoiceSettings();
        }
        assistantFollowUpAfterTts = false;
        sleepFaceAfterTts = false;
        cancelAssistantFollowUpSilenceTimeout();
        if (tts != null) {
            tts.stop();
        }

        Runnable launch = () -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            try {
                startActivity(new Intent().setClassName(
                        getPackageName(), "com.boop.alpha1.BoopDevMenuActivity"));
            } catch (RuntimeException unavailable) {
                Toast.makeText(this, "Dev menu isn't available yet.", Toast.LENGTH_SHORT).show();
                wakeFaceForInteraction();
                scheduleFaceIdle();
            }
        };

        // Do not change Activity lifecycle from inside SpeechRecognizer.onResults().
        // Posting the launch lets the recognition callback unwind first.
        if (presenceHandler != null) {
            presenceHandler.post(launch);
        } else {
            runOnUiThread(launch);
        }
    }

'''
    text = text.replace(anchor, helper + anchor, 1)
    changed = True

if changed:
    path.write_text(text, encoding="utf-8")
    print("Internal BOOP dev menu deferred launch and scrollable Voice Settings materialized")
else:
    print("Internal BOOP dev menu hotfix already materialized")
