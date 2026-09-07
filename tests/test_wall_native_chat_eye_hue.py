from pathlib import Path


def test_native_chat_lineage_is_preserved_while_adding_eye_hue():
    materialize = Path("scripts/materialize-android.sh").read_text()
    build = Path("source/app-build.gradle").read_text()
    patch = Path("scripts/patch-wall-eye-hue.py").read_text()
    intent = Path("source/BoopEyeHueVoiceIntent.java").read_text()

    assert "patch-wall-chat-mode.py" in materialize
    assert "patch-wall-openai-relay.py" in materialize
    assert "patch-wall-idle-blink.py" in materialize
    assert "patch-wall-eye-hue.py" in materialize
    assert "ActivityOptions.makeCustomAnimation" in materialize

    assert "versionCode 38" in build
    assert 'versionName "0.4.18-wall-eye-hue-local-intent"' in build
    assert "BOOP_RELAY_URL" in build
    assert "BOOP_RELAY_TOKEN" in build

    assert "BoopEyeHueSettings.addSlider" not in patch
    assert "BoopEyeHueOverlay" in patch
    assert "BoopEyeHueVoiceIntent.matches(transcript)" in patch
    assert "showEyeHueControl();" in patch
    assert "postDelayed(eyeHueHoldRunnable" not in patch
    assert "touchesBothEyes(face, event)" not in patch
    assert "eyeHueOverlay.hide()" in patch
    assert "BitmapFactory.decodeResource(getResources(), R.drawable.boop_eyes)" in patch
    assert "paint.setColorFilter(BoopEyeHue.colorFilterForHue(hueDegrees));" in patch
    assert "setEyeHueDegrees(BoopEyeHue.loadHue(context));" in patch
    assert 'value.contains("eye colour")' in intent
    assert 'value.contains("eye color")' in intent
    assert 'value.contains("eye hue")' in intent
    assert 'value.contains("i color")' in intent
    assert "mouth" not in patch.lower()


def test_hue_overlay_is_one_persisted_full_spectrum_control_under_the_eyes():
    hue = Path("source/BoopEyeHue.java").read_text()
    math = Path("source/BoopEyeHueMath.java").read_text()
    overlay = Path("source/BoopEyeHueOverlay.java").read_text()

    assert 'PREFS_NAME = "boop_eyes"' in hue
    assert 'KEY_HUE_DEGREES = "hue_degrees"' in hue
    assert "preferences.getInt(KEY_HUE_DEGREES" in hue
    assert ".putInt(KEY_HUE_DEGREES" in hue

    assert "PROGRESS_MAX = 359" in math
    assert "DEFAULT_HUE_DEGREES = 190" in math
    assert "if (bounded == DEFAULT_HUE_DEGREES)" in math
    assert "return null;" in math

    assert overlay.count("new SeekBar(activity)") == 1
    assert 'slider.setContentDescription("Eye colour hue")' in overlay
    assert "BoopEyeHue.saveHue(activity, progress)" in overlay
    assert "face.setEyeHueDegrees(progress)" in overlay
    assert "eyeBottom" in overlay
    assert "params.topMargin" in overlay
    assert "isSliderTouch" in overlay

    forbidden = ("brightness", "saturation", "opacity", "theme", "effect")
    lowered = overlay.lower()
    for token in forbidden:
        assert token not in lowered
