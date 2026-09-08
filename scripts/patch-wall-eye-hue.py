from pathlib import Path

ROOT = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1")
MAIN = ROOT / "MainActivity.java"
FACE = ROOT / "BoopFaceView.java"

main_text = MAIN.read_text()

# The eye-colour control is deliberately not part of Voice Settings. It is
# summoned by voice and drawn beneath the eyes so colour changes are visible
# live. A tap anywhere outside the slider dismisses it.
fields_marker = "    private boolean faceGestureMoved;\n"
fields_insert = fields_marker + "    private BoopEyeHueOverlay eyeHueOverlay;\n"
if "private BoopEyeHueOverlay eyeHueOverlay;" not in main_text:
    if main_text.count(fields_marker) != 1:
        raise SystemExit("BOOP eye hue fields marker not found")
    main_text = main_text.replace(fields_marker, fields_insert, 1)

create_marker = """        interactionSurface.addView(face, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(interactionSurface);
"""
create_replacement = """        interactionSurface.addView(face, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        eyeHueOverlay = new BoopEyeHueOverlay(this, interactionSurface, face);
        setContentView(interactionSurface);
"""
if "eyeHueOverlay = new BoopEyeHueOverlay(this, interactionSurface, face);" not in main_text:
    if main_text.count(create_marker) != 1:
        raise SystemExit("BOOP eye hue overlay creation marker not found")
    main_text = main_text.replace(create_marker, create_replacement, 1)

old_touch_start = """    private boolean onFaceTouch(View view, MotionEvent event) {
        if (voiceSettingsOpen || chatModeOpen) return true;
        int action = event.getActionMasked();
"""
new_touch_start = """    private boolean onFaceTouch(View view, MotionEvent event) {
        if (voiceSettingsOpen || chatModeOpen) return true;
        int action = event.getActionMasked();
        if (eyeHueOverlay != null && eyeHueOverlay.isVisible()) {
            if (action == MotionEvent.ACTION_DOWN && !eyeHueOverlay.isSliderTouch(event)) {
                eyeHueOverlay.hide();
                if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(false);
                wakeFaceForInteraction();
                scheduleFaceIdle();
            }
            return true;
        }
"""
if new_touch_start not in main_text:
    if main_text.count(old_touch_start) != 1:
        raise SystemExit("BOOP eye hue touch-start marker not found")
    main_text = main_text.replace(old_touch_start, new_touch_start, 1)

handle_marker = """    private void handleRecognizedSpeech(String transcript) {
        if (BoopVoiceSettingsIntent.matches(transcript)) {
"""
handle_replacement = """    private void handleRecognizedSpeech(String transcript) {
        if (BoopEyeHueVoiceIntent.matches(transcript)) {
            showEyeHueControl();
            return;
        }
        if (BoopVoiceSettingsIntent.matches(transcript)) {
"""
if handle_replacement not in main_text:
    if main_text.count(handle_marker) != 1:
        raise SystemExit("BOOP eye hue recognized-speech marker not found")
    main_text = main_text.replace(handle_marker, handle_replacement, 1)

methods_marker = "    private void cancelFaceHolds() {\n"
methods_insert = """    private void showEyeHueControl() {
        if (eyeHueOverlay == null || eyeHueOverlay.isVisible() || voiceSettingsOpen || chatModeOpen) {
            return;
        }
        cancelFaceHolds();
        assistantFollowUpAfterTts = false;
        sleepFaceAfterTts = false;
        cancelAssistantFollowUpSilenceTimeout();
        if (listening) {
            suppressNextRecognizerError = true;
            stopListening();
        }
        if (tts != null) tts.stop();
        wakeFaceForInteraction();
        interactionSurface.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        eyeHueOverlay.show();
        if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(true);
        scheduleFaceIdle();
    }

""" + methods_marker
if "private void showEyeHueControl()" not in main_text:
    if main_text.count(methods_marker) != 1:
        raise SystemExit("BOOP eye hue method marker not found")
    main_text = main_text.replace(methods_marker, methods_insert, 1)

idle_marker = "        if (listening || thinking || voiceSettingsOpen || chatModeOpen) {\n"
idle_replacement = "        if (listening || thinking || voiceSettingsOpen || chatModeOpen\n                || (eyeHueOverlay != null && eyeHueOverlay.isVisible())) {\n"
if idle_replacement not in main_text:
    if main_text.count(idle_marker) != 1:
        raise SystemExit("BOOP eye hue idle marker not found")
    main_text = main_text.replace(idle_marker, idle_replacement, 1)

MAIN.write_text(main_text)

face_text = FACE.read_text()

# Preserve the original eye artwork and recolour only the saturated cyan/blue
# iris pixels. Whites, pupils, outlines and neutral shading are left byte-for-byte
# untouched. This avoids the old whole-bitmap ColorFilter behaviour.
field_marker = "    private final Bitmap faceBitmap;\n"
field_replacement = "    private final Bitmap originalFaceBitmap;\n    private Bitmap faceBitmap;\n"
if field_replacement not in face_text:
    if face_text.count(field_marker) != 1:
        raise SystemExit("BOOP eye hue bitmap field marker not found")
    face_text = face_text.replace(field_marker, field_replacement, 1)

constructor_marker = (
    "        faceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes);\n"
    "    }\n\n"
)
constructor_replacement = (
    "        originalFaceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes);\n"
    "        faceBitmap = originalFaceBitmap;\n"
    "        setEyeHueDegrees(BoopEyeHue.loadHue(context));\n"
    "    }\n\n"
)
if "originalFaceBitmap = BitmapFactory.decodeResource" not in face_text:
    if face_text.count(constructor_marker) != 1:
        raise SystemExit("BOOP eye hue face constructor marker not found")
    face_text = face_text.replace(constructor_marker, constructor_replacement, 1)

method_marker = "    void showIdleBlackImmediately() {\n"
method_insert = """    void setEyeHueDegrees(int hueDegrees) {
        if (originalFaceBitmap == null) {
            return;
        }
        int hue = BoopEyeHueMath.clampHue(hueDegrees);
        if (hue == BoopEyeHueMath.DEFAULT_HUE_DEGREES) {
            faceBitmap = originalFaceBitmap;
            paint.setColorFilter(null);
            invalidate();
            return;
        }

        Bitmap recoloured = originalFaceBitmap.copy(Bitmap.Config.ARGB_8888, true);
        int width = recoloured.getWidth();
        int height = recoloured.getHeight();
        int[] pixels = new int[width * height];
        recoloured.getPixels(pixels, 0, width, 0, 0, width, height);
        float[] hsv = new float[3];
        for (int i = 0; i < pixels.length; i++) {
            int colour = pixels[i];
            int alpha = Color.alpha(colour);
            if (alpha == 0) {
                continue;
            }
            Color.RGBToHSV(Color.red(colour), Color.green(colour), Color.blue(colour), hsv);
            boolean irisPixel = hsv[1] >= 0.30f
                    && hsv[2] >= 0.18f
                    && hsv[0] >= 155f
                    && hsv[0] <= 235f;
            if (!irisPixel) {
                continue;
            }
            hsv[0] = hue;
            pixels[i] = Color.HSVToColor(alpha, hsv);
        }
        recoloured.setPixels(pixels, 0, width, 0, 0, width, height);
        faceBitmap = recoloured;
        paint.setColorFilter(null);
        invalidate();
    }

"""
if "boolean irisPixel = hsv[1] >= 0.30f" not in face_text:
    if face_text.count(method_marker) != 1:
        raise SystemExit("BOOP eye hue face method marker not found")
    face_text = face_text.replace(method_marker, method_insert + method_marker, 1)

FACE.write_text(face_text)
