from pathlib import Path

ROOT = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1")
MAIN = ROOT / "MainActivity.java"
FACE = ROOT / "BoopFaceView.java"

main_text = MAIN.read_text()

# The eye-colour control is deliberately not part of Voice Settings. It is a
# face interaction: hold both visible eyes for one second, then adjust the
# slider beneath the eyes. A tap anywhere outside the slider dismisses it.
fields_marker = "    private boolean faceGestureMoved;\n"
fields_insert = fields_marker + """    private BoopEyeHueOverlay eyeHueOverlay;
    private boolean eyeHueChordActive;
    private boolean eyeHueGestureConsumed;
    private final Runnable eyeHueHoldRunnable = () -> {
        if (!eyeHueChordActive || !faceTouchActive || voiceSettingsOpen || chatModeOpen
                || eyeHueOverlay == null || eyeHueOverlay.isVisible()
                || isFinishing() || isDestroyed()) return;
        eyeHueChordActive = false;
        eyeHueGestureConsumed = true;
        memberBerryConsumed = true;
        cancelFaceHolds();
        interactionSurface.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        wakeFaceForInteraction();
        eyeHueOverlay.show();
        if (wakeCoordinator != null) wakeCoordinator.setVoiceSettingsOpen(true);
    };
"""
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
                scheduleFaceIdle();
            }
            return true;
        }
"""
if new_touch_start not in main_text:
    if main_text.count(old_touch_start) != 1:
        raise SystemExit("BOOP eye hue touch-start marker not found")
    main_text = main_text.replace(old_touch_start, new_touch_start, 1)

old_down = """        if (action == MotionEvent.ACTION_DOWN) {
            cancelFaceHolds();
            faceTouchActive = true;
            faceGestureMoved = false;
            memberBerryConsumed = false;
            swipeHadMultiplePointers = false;
"""
new_down = """        if (action == MotionEvent.ACTION_DOWN) {
            cancelFaceHolds();
            cancelEyeHueHold();
            eyeHueGestureConsumed = false;
            faceTouchActive = true;
            faceGestureMoved = false;
            memberBerryConsumed = false;
            swipeHadMultiplePointers = false;
"""
if new_down not in main_text:
    if main_text.count(old_down) != 1:
        raise SystemExit("BOOP eye hue ACTION_DOWN marker not found")
    main_text = main_text.replace(old_down, new_down, 1)

old_pointer = """        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            swipeHadMultiplePointers = true;
            cancelFaceHolds();
            return true;
        }
"""
new_pointer = """        if (action == MotionEvent.ACTION_POINTER_DOWN) {
            swipeHadMultiplePointers = true;
            cancelFaceHolds();
            cancelEyeHueHold();
            if (BoopEyeHueOverlay.touchesBothEyes(face, event)) {
                eyeHueChordActive = true;
                if (presenceHandler != null) {
                    presenceHandler.postDelayed(eyeHueHoldRunnable, 1_000L);
                }
            }
            return true;
        }
"""
if new_pointer not in main_text:
    if main_text.count(old_pointer) != 1:
        raise SystemExit("BOOP eye hue ACTION_POINTER_DOWN marker not found")
    main_text = main_text.replace(old_pointer, new_pointer, 1)

old_move = """        if (action == MotionEvent.ACTION_MOVE) {
            chatModeHold.move(event.getX(), event.getY(), event.getPointerCount());
            float movedX = Math.abs(event.getX() - faceTouchDownX);
"""
new_move = """        if (action == MotionEvent.ACTION_MOVE) {
            if (eyeHueChordActive) {
                if (!BoopEyeHueOverlay.touchesBothEyes(face, event)) cancelEyeHueHold();
                return true;
            }
            chatModeHold.move(event.getX(), event.getY(), event.getPointerCount());
            float movedX = Math.abs(event.getX() - faceTouchDownX);
"""
if new_move not in main_text:
    if main_text.count(old_move) != 1:
        raise SystemExit("BOOP eye hue ACTION_MOVE marker not found")
    main_text = main_text.replace(old_move, new_move, 1)

old_cancel = """        if (action == MotionEvent.ACTION_CANCEL) {
            cancelFaceHolds();
            faceTouchActive = false;
"""
new_cancel = """        if (action == MotionEvent.ACTION_POINTER_UP) {
            cancelEyeHueHold();
            return true;
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            cancelFaceHolds();
            cancelEyeHueHold();
            faceTouchActive = false;
"""
if new_cancel not in main_text:
    if main_text.count(old_cancel) != 1:
        raise SystemExit("BOOP eye hue cancel marker not found")
    main_text = main_text.replace(old_cancel, new_cancel, 1)

old_release = """        if (action != MotionEvent.ACTION_UP) return true;
        cancelFaceHolds();
        if (!faceTouchActive) return true;
"""
new_release = """        if (action != MotionEvent.ACTION_UP) return true;
        cancelFaceHolds();
        cancelEyeHueHold();
        if (eyeHueGestureConsumed) {
            eyeHueGestureConsumed = false;
            faceTouchActive = false;
            return true;
        }
        if (!faceTouchActive) return true;
"""
if new_release not in main_text:
    if main_text.count(old_release) != 1:
        raise SystemExit("BOOP eye hue release marker not found")
    main_text = main_text.replace(old_release, new_release, 1)

methods_marker = "    private void cancelFaceHolds() {\n"
methods_insert = """    private void cancelEyeHueHold() {
        eyeHueChordActive = false;
        if (presenceHandler != null) presenceHandler.removeCallbacks(eyeHueHoldRunnable);
    }

""" + methods_marker
if "private void cancelEyeHueHold()" not in main_text:
    if main_text.count(methods_marker) != 1:
        raise SystemExit("BOOP eye hue method marker not found")
    main_text = main_text.replace(methods_marker, methods_insert, 1)

# Keep the face awake while the hue control is visible, just as other transient
# Wall interaction surfaces do.
idle_marker = "        if (listening || thinking || voiceSettingsOpen || chatModeOpen) {\n"
idle_replacement = "        if (listening || thinking || voiceSettingsOpen || chatModeOpen\n                || (eyeHueOverlay != null && eyeHueOverlay.isVisible())) {\n"
if idle_replacement not in main_text:
    if main_text.count(idle_marker) != 1:
        raise SystemExit("BOOP eye hue idle marker not found")
    main_text = main_text.replace(idle_marker, idle_replacement, 1)

MAIN.write_text(main_text)

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
