#!/usr/bin/env python3
"""v66 experimental polish for procedural eyes.

1. Sleep closes the eye geometry and fades out without ever collapsing the whole
   face View into the historical IDLE_SCALE_Y strip.
2. The hue overlay explicitly drives the procedural iris state for every progress
   update and re-syncs the saved hue whenever the slider opens.

Wake/audio/routing are untouched.
"""
from pathlib import Path

ROOT = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1')
FACE = ROOT / 'BoopFaceView.java'
OVERLAY = ROOT / 'BoopEyeHueOverlay.java'
FACE_MARKER = 'BOOP_V66_GEOMETRIC_SLEEP'
HUE_MARKER = 'BOOP_V66_PROCEDURAL_HUE_SYNC'


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected one anchor, found {count}')
    return text.replace(old, new, 1)


def patch_face(text: str) -> str:
    if FACE_MARKER in text:
        return text
    if 'BOOP_PROCEDURAL_IRISES_V3' not in text:
        raise SystemExit('v66 sleep fix requires procedural irises')
    old = '''                sleepCharmAnimator = null;\n                idleBlinkOpenness = 1f;\n                resetPuppetTransform();\n                setPivotX(getWidth() / 2f);\n                setPivotY(getHeight() / 2f);\n                setScaleY(IDLE_SCALE_Y);\n                setAlpha(0f);\n                invalidate();\n'''
    new = '''                sleepCharmAnimator = null;\n                // BOOP_V66_GEOMETRIC_SLEEP: finish with the eyelids closed.\n                // Never squash the whole face View into a horizontal strip.\n                idleBlinkOpenness = BoopSleepCharm.openness(1f);\n                resetPuppetTransform();\n                setPivotX(getWidth() / 2f);\n                setPivotY(getHeight() / 2f);\n                setScaleY(1f);\n                setAlpha(0f);\n                invalidate();\n'''
    return replace_once(text, old, new, 'sleep completion')


def patch_overlay(text: str) -> str:
    if HUE_MARKER in text:
        return text
    old_progress = '''            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {\n                if (!fromUser) return;\n                BoopEyeHue.saveHue(activity, progress);\n                face.setEyeHueDegrees(progress);\n            }\n'''
    new_progress = '''            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {\n                // BOOP_V66_PROCEDURAL_HUE_SYNC: the procedural iris owns colour now.\n                // Always push the displayed slider value into the renderer; only\n                // persist it when the user actually moved the control.\n                face.setEyeHueDegrees(progress);\n                if (fromUser) BoopEyeHue.saveHue(activity, progress);\n            }\n'''
    text = replace_once(text, old_progress, new_progress, 'procedural hue progress')
    old_show = '''    void show() {\n        slider.setProgress(BoopEyeHue.loadHue(activity));\n        FrameLayout.LayoutParams params = sliderLayoutParams();\n'''
    new_show = '''    void show() {\n        int savedHue = BoopEyeHue.loadHue(activity);\n        face.setEyeHueDegrees(savedHue);\n        slider.setProgress(savedHue);\n        FrameLayout.LayoutParams params = sliderLayoutParams();\n'''
    return replace_once(text, old_show, new_show, 'procedural hue show sync')


def main() -> None:
    face = FACE.read_text(encoding='utf-8')
    overlay = OVERLAY.read_text(encoding='utf-8')
    FACE.write_text(patch_face(face), encoding='utf-8')
    OVERLAY.write_text(patch_overlay(overlay), encoding='utf-8')
    print('v66 sleep stays geometric and hue slider directly drives procedural irises')


if __name__ == '__main__':
    main()
