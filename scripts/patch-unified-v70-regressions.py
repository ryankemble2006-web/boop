#!/usr/bin/env python3
from pathlib import Path

MAIN = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
MARKER = "// BOOP_VOICE_SETTINGS_SCROLL_V70_FIX"


def one_line_index(lines: list[str], needle: str, label: str) -> int:
    matches = [i for i, line in enumerate(lines) if needle in line]
    if len(matches) != 1:
        raise SystemExit(f"{label}: expected one line, found {len(matches)}")
    return matches[0]


text = MAIN.read_text(encoding="utf-8")
if MARKER in text:
    print("v70 Voice Settings scroll fix already materialized")
    raise SystemExit(0)

if "import android.widget.ScrollView;\n" not in text:
    lines = text.splitlines(keepends=True)
    seek_import = one_line_index(lines, "import android.widget.SeekBar;", "SeekBar import")
    lines.insert(seek_import + 1, "import android.widget.ScrollView;\n")
    text = "".join(lines)

lines = text.splitlines(keepends=True)
field = one_line_index(lines, "private LinearLayout voiceSettingsOverlay;", "Voice Settings field")
lines.insert(field + 1, "    private ScrollView voiceSettingsScroll;\n")

add_start = one_line_index(
    lines,
    "interactionSurface.addView(voiceSettingsOverlay,",
    "Voice Settings root attachment start",
)
add_end = one_line_index(lines, "voiceSettingsOverlay.bringToFront();", "Voice Settings root attachment end")
if add_end < add_start:
    raise SystemExit("Voice Settings root attachment order is invalid")
lines[add_start:add_end + 1] = [
    "        // BOOP_VOICE_SETTINGS_SCROLL_V70_FIX\n",
    "        voiceSettingsScroll = new ScrollView(this);\n",
    "        voiceSettingsScroll.setFillViewport(true);\n",
    "        voiceSettingsScroll.setBackgroundColor(Color.argb(236, 0, 0, 0));\n",
    "        voiceSettingsScroll.setContentDescription(\"Scrollable BOOP voice settings\");\n",
    "        voiceSettingsScroll.addView(voiceSettingsOverlay, new ScrollView.LayoutParams(\n",
    "                ScrollView.LayoutParams.MATCH_PARENT,\n",
    "                ScrollView.LayoutParams.WRAP_CONTENT));\n",
    "        interactionSurface.addView(voiceSettingsScroll, new FrameLayout.LayoutParams(\n",
    "                FrameLayout.LayoutParams.MATCH_PARENT,\n",
    "                FrameLayout.LayoutParams.MATCH_PARENT));\n",
    "        voiceSettingsScroll.bringToFront();\n",
]

hide_start = one_line_index(
    lines,
    "if (interactionSurface != null && voiceSettingsOverlay != null)",
    "Voice Settings dismissal start",
)
hide_end = one_line_index(lines, "voiceSettingsOverlay = null;", "Voice Settings dismissal end")
if hide_end < hide_start:
    raise SystemExit("Voice Settings dismissal order is invalid")
lines[hide_start:hide_end + 1] = [
    "        if (interactionSurface != null && voiceSettingsScroll != null) {\n",
    "            interactionSurface.removeView(voiceSettingsScroll);\n",
    "        } else if (interactionSurface != null && voiceSettingsOverlay != null) {\n",
    "            interactionSurface.removeView(voiceSettingsOverlay);\n",
    "        }\n",
    "        voiceSettingsScroll = null;\n",
    "        voiceSettingsOverlay = null;\n",
]

MAIN.write_text("".join(lines), encoding="utf-8")
print("v70 Voice Settings is vertically scrollable")
