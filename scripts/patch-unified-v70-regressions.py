#!/usr/bin/env python3
from pathlib import Path

MAIN = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
MARKER = "// BOOP_VOICE_SETTINGS_SCROLL_V70_FIX"


def insert_after_once(text: str, needle: str, insertion: str, label: str) -> str:
    pos = text.find(needle)
    if pos < 0 or text.find(needle, pos + len(needle)) >= 0:
        raise SystemExit(f"{label}: expected one anchor")
    return text[: pos + len(needle)] + insertion + text[pos + len(needle) :]


text = MAIN.read_text(encoding="utf-8")
if MARKER in text:
    print("v70 Voice Settings scroll fix already materialized")
    raise SystemExit(0)

if "import android.widget.ScrollView;\n" not in text:
    text = insert_after_once(
        text,
        "import android.widget.SeekBar;\n",
        "import android.widget.ScrollView;\n",
        "ScrollView import",
    )

if "private ScrollView voiceSettingsScroll;" not in text:
    text = insert_after_once(
        text,
        "    private LinearLayout voiceSettingsOverlay;\n",
        "    private ScrollView voiceSettingsScroll;\n",
        "Voice Settings field",
    )

show_start = text.find("    private void showVoiceSettings() {")
show_end = text.find("    private TextView voiceSettingLabel", show_start)
if show_start < 0 or show_end < 0:
    raise SystemExit("Voice Settings method bounds not found")
show = text[show_start:show_end]

attach_start = show.rfind("        interactionSurface.addView(")
if attach_start < 0:
    raise SystemExit("Voice Settings final root attachment not found")
attach_end = show.find(");", attach_start)
if attach_end < 0:
    raise SystemExit("Voice Settings final root attachment terminator not found")
attach_end += 2

# If the old content view explicitly brings the LinearLayout forward, consume that
# statement too. Some older materializers omit it, so it is deliberately optional.
bring = "\n        voiceSettingsOverlay.bringToFront();"
if show.startswith(bring, attach_end):
    attach_end += len(bring)

replacement = '''        // BOOP_VOICE_SETTINGS_SCROLL_V70_FIX
        voiceSettingsScroll = new ScrollView(this);
        voiceSettingsScroll.setFillViewport(true);
        voiceSettingsScroll.setBackgroundColor(Color.argb(236, 0, 0, 0));
        voiceSettingsScroll.setContentDescription("Scrollable BOOP voice settings");
        voiceSettingsScroll.addView(voiceSettingsOverlay, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        interactionSurface.addView(voiceSettingsScroll, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        voiceSettingsScroll.bringToFront();'''
show = show[:attach_start] + replacement + show[attach_end:]
text = text[:show_start] + show + text[show_end:]

hide_start = text.find("    private void hideVoiceSettings() {")
hide_end = text.find("    private int dp(", hide_start)
if hide_start < 0 or hide_end < 0:
    raise SystemExit("Voice Settings dismissal method bounds not found")
hide = text[hide_start:hide_end]

old_remove = "interactionSurface.removeView(voiceSettingsOverlay);"
if old_remove not in hide:
    raise SystemExit("Voice Settings old dismissal target not found")
hide = hide.replace(old_remove, "interactionSurface.removeView(voiceSettingsScroll);", 1)

old_null = "        voiceSettingsOverlay = null;"
if old_null not in hide:
    raise SystemExit("Voice Settings overlay nulling not found")
hide = hide.replace(
    old_null,
    "        voiceSettingsScroll = null;\n        voiceSettingsOverlay = null;",
    1,
)
text = text[:hide_start] + hide + text[hide_end:]

MAIN.write_text(text, encoding="utf-8")
print("v70 Voice Settings is vertically scrollable")
