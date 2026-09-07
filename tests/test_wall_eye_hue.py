from pathlib import Path


def test_eye_hue_is_one_persisted_slider_on_existing_render_path():
    face = Path("source/BoopFaceView.java").read_text()
    hue = Path("source/BoopEyeHue.java").read_text()
    settings = Path("source/BoopEyeHueSettings.java").read_text()
    patch = Path("scripts/patch-wall-eye-hue.py").read_text()

    assert "BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes)" in face
    assert "paint.setColorFilter(BoopEyeHue.colorFilterForHue(hueDegrees))" in face
    assert face.count("canvas.drawBitmap(faceBitmap") == 3
    assert "canvas.drawColor(Color.BLACK)" in face
    assert "BoopEyeHue.loadHue(context)" in face

    assert 'PREFS_NAME = "boop_eyes"' in hue
    assert 'KEY_HUE_DEGREES = "hue_degrees"' in hue
    assert "preferences.getInt(KEY_HUE_DEGREES" in hue
    assert ".putInt(KEY_HUE_DEGREES" in hue

    assert settings.count("new SeekBar(activity)") == 1
    assert 'label.setText("Eye colour")' in settings
    assert 'slider.setContentDescription("Eye colour hue")' in settings
    assert "BoopEyeHue.saveHue(activity, progress)" in settings
    assert "face.setEyeHueDegrees(progress)" in settings

    assert patch.count("BoopEyeHueSettings.addSlider") == 1


def test_default_blue_is_exact_unfiltered_path_and_no_extra_face_features():
    math = Path("source/BoopEyeHueMath.java").read_text()
    hue = Path("source/BoopEyeHue.java").read_text()
    face = Path("source/BoopFaceView.java").read_text()

    assert "DEFAULT_HUE_DEGREES = 190" in math
    assert "if (bounded == DEFAULT_HUE_DEGREES)" in math
    assert "return null;" in math
    assert "if (values == null)" in hue
    assert "return null;" in hue

    forbidden = ("mouth", "drawOval", "drawCircle", "drawRoundRect")
    lowered = face.lower()
    assert "mouth" not in lowered
    for token in forbidden[1:]:
        assert token not in face


def test_hue_control_does_not_touch_voice_or_gesture_logic():
    patch = Path("scripts/patch-wall-eye-hue.py").read_text()
    assert "Button done = new Button(this);" in patch
    assert "BoopEyeHueSettings.addSlider(this, voiceSettingsOverlay, face);" in patch
    assert "recognizer" not in patch
    assert "BoopLauncherSwipeGesture" not in patch
    assert "memberBerry" not in patch
