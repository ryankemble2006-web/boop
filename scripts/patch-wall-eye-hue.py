from pathlib import Path

MAIN = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
MARKER = "        Button done = new Button(this);\n"
INSERT = "        BoopEyeHueSettings.addSlider(this, voiceSettingsOverlay, face);\n\n"

text = MAIN.read_text()
if INSERT in text:
    raise SystemExit(0)
if MARKER not in text:
    raise SystemExit("BOOP eye hue patch marker not found")
MAIN.write_text(text.replace(MARKER, INSERT + MARKER, 1))
