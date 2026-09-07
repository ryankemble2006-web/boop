from pathlib import Path

ROOT = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1")
MAIN = ROOT / "MainActivity.java"
FACE = ROOT / "BoopFaceView.java"

main_text = MAIN.read_text()
main_marker = "        Button done = new Button(this);\n"
main_insert = "        BoopEyeHueSettings.addSlider(this, voiceSettingsOverlay, face);\n\n"
if main_insert not in main_text:
    if main_marker not in main_text:
        raise SystemExit("BOOP eye hue settings marker not found")
    MAIN.write_text(main_text.replace(main_marker, main_insert + main_marker, 1))

face_text = FACE.read_text()
constructor_marker = (
    "        faceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes);\n"
    "    }\n\n"
)
constructor_replacement = (
    "        faceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes);\n"
    "        setEyeHueDegrees(BoopEyeHue.loadHue(context));\n"
    "    }\n\n"
)
method_insert = (
    "    void setEyeHueDegrees(int hueDegrees) {\n"
    "        paint.setColorFilter(BoopEyeHue.colorFilterForHue(hueDegrees));\n"
    "        invalidate();\n"
    "    }\n\n"
)
method_marker = "    void showIdleBlackImmediately() {\n"

changed = False
if "setEyeHueDegrees(BoopEyeHue.loadHue(context));" not in face_text:
    if constructor_marker not in face_text:
        raise SystemExit("BOOP eye hue face constructor marker not found")
    face_text = face_text.replace(constructor_marker, constructor_replacement, 1)
    changed = True
if method_insert not in face_text:
    if method_marker not in face_text:
        raise SystemExit("BOOP eye hue face method marker not found")
    face_text = face_text.replace(method_marker, method_insert + method_marker, 1)
    changed = True
if changed:
    FACE.write_text(face_text)
