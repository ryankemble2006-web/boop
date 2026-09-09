#!/usr/bin/env python3
from pathlib import Path

MAIN = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
MARKER = "// BOOP_VOICE_SETTINGS_SCROLL_V70_FIX"


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one anchor, found {count}")
    return text.replace(old, new, 1)


text = MAIN.read_text(encoding="utf-8")
if MARKER in text:
    print("v70 Voice Settings scroll fix already materialized")
    raise SystemExit(0)

if "import android.widget.ScrollView;\n" not in text:
    text = replace_once(
        text,
        "import android.widget.SeekBar;\n",
        "import android.widget.SeekBar;\nimport android.widget.ScrollView;\n",
        "ScrollView import",
    )

text = replace_once(
    text,
    "    private LinearLayout voiceSettingsOverlay;\n",
    "    private LinearLayout voiceSettingsOverlay;\n"
    "    private ScrollView voiceSettingsScroll;\n",
    "Voice Settings field",
)

old_add = '''        interactionSurface.addView(voiceSettingsOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        voiceSettingsOverlay.bringToFront();
'''
new_add = '''        // BOOP_VOICE_SETTINGS_SCROLL_V70_FIX
        voiceSettingsScroll = new ScrollView(this);
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
text = replace_once(text, old_add, new_add, "Voice Settings root attachment")

old_hide = '''        if (interactionSurface != null && voiceSettingsOverlay != null) {
            interactionSurface.removeView(voiceSettingsOverlay);
        }
        voiceSettingsOverlay = null;
'''
new_hide = '''        if (interactionSurface != null && voiceSettingsScroll != null) {
            interactionSurface.removeView(voiceSettingsScroll);
        } else if (interactionSurface != null && voiceSettingsOverlay != null) {
            interactionSurface.removeView(voiceSettingsOverlay);
        }
        voiceSettingsScroll = null;
        voiceSettingsOverlay = null;
'''
text = replace_once(text, old_hide, new_hide, "Voice Settings dismissal")

MAIN.write_text(text, encoding="utf-8")
print("v70 Voice Settings is vertically scrollable")
