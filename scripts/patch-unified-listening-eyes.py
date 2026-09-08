#!/usr/bin/env python3
"""Add a listening pose to the existing approved BOOP eyes without changing artwork.

This patch is deliberately visual-state only. It does not edit PNG bytes, eye hue,
blink timing, wake detection, microphone ownership, recognition timing, routing or
TTS. The recognizer's existing `listening` lifecycle starts/stops a gentle vertical
pulse in the already-approved eye renderer.
"""
from pathlib import Path

ROOT = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1')
MAIN = ROOT / 'MainActivity.java'
FACE = ROOT / 'BoopFaceView.java'
MARKER = 'BOOP_LISTENING_EYES_V1'


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected one anchor, found {count}')
    return text.replace(old, new, 1)


def patch_face(text: str) -> str:
    if MARKER in text:
        required = (
            'void startListeningCue()',
            'void stopListeningCue()',
            'listeningCueScaleY()',
            'eye.height() * idleBlinkOpenness * listeningCueScaleY() / 2f',
        )
        if not all(part in text for part in required):
            raise SystemExit('listening-eyes patch: partial or modified face patch')
        return text

    fields_anchor = '''    private boolean awakeForBlink;\n    private long idleBlinkBlockedUntil;\n'''
    fields = fields_anchor + '''\n    // BOOP_LISTENING_EYES_V1: runtime pose only; approved bitmap bytes stay untouched.\n    private static final long LISTENING_CUE_HALF_CYCLE_MS = 520L;\n    private static final float LISTENING_CUE_MIN_SCALE_Y = 1.025f;\n    private static final float LISTENING_CUE_MAX_SCALE_Y = 1.060f;\n    private final BoopListeningCueState listeningCueState = new BoopListeningCueState();\n    private ValueAnimator listeningCueAnimator;\n    private float listeningCueFraction;\n'''
    text = replace_once(text, fields_anchor, fields, 'listening cue fields')

    methods_anchor = '    void showIdleBlackImmediately() {\n'
    methods = '''    void startListeningCue() {\n        if (!listeningCueState.start()) return;\n        cancelIdleBlinkFrame();\n        listeningCueFraction = 0f;\n        if (!ValueAnimator.areAnimatorsEnabled()) {\n            listeningCueFraction = 1f;\n            invalidate();\n            return;\n        }\n        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);\n        listeningCueAnimator = animator;\n        animator.setDuration(LISTENING_CUE_HALF_CYCLE_MS);\n        animator.setRepeatCount(ValueAnimator.INFINITE);\n        animator.setRepeatMode(ValueAnimator.REVERSE);\n        animator.setInterpolator(new AccelerateDecelerateInterpolator());\n        animator.addUpdateListener(frame -> {\n            if (listeningCueAnimator != frame || !listeningCueState.isActive()) return;\n            listeningCueFraction = (float) frame.getAnimatedValue();\n            invalidate();\n        });\n        animator.start();\n    }\n\n    void stopListeningCue() {\n        boolean changed = listeningCueState.stop();\n        ValueAnimator animator = listeningCueAnimator;\n        listeningCueAnimator = null;\n        if (animator != null) animator.cancel();\n        if (changed || listeningCueFraction != 0f) {\n            listeningCueFraction = 0f;\n            invalidate();\n        }\n    }\n\n    private float listeningCueScaleY() {\n        if (!listeningCueState.isActive()) return 1f;\n        return LISTENING_CUE_MIN_SCALE_Y\n                + (LISTENING_CUE_MAX_SCALE_Y - LISTENING_CUE_MIN_SCALE_Y)\n                * listeningCueFraction;\n    }\n\n'''
    text = replace_once(text, methods_anchor, methods + methods_anchor,
                        'listening cue methods')

    # Sleep charm runs after idle blink and owns the immediate-idle lead-in.
    text = replace_once(
        text,
        '    void showIdleBlackImmediately() {\n        cancelSleepCharm();\n        stopIdleBlinking();\n',
        '    void showIdleBlackImmediately() {\n        cancelSleepCharm();\n        stopListeningCue();\n        stopIdleBlinking();\n',
        'immediate idle stops listening cue')
    text = replace_once(
        text,
        '    void goIdleBlack() {\n        stopIdleBlinking();\n',
        '    void goIdleBlack() {\n        stopListeningCue();\n        stopIdleBlinking();\n',
        'animated idle stops listening cue')
    text = replace_once(
        text,
        '    void startThinking() {\n        deferIdleBlink(THINKING_DURATION_MS);\n',
        '    void startThinking() {\n        stopListeningCue();\n        deferIdleBlink(THINKING_DURATION_MS);\n',
        'thinking stops listening cue')

    portrait_anchor = '''        canvas.scale(1f, idleBlinkOpenness, getWidth() / 2f, eyelidCenterY);\n        canvas.drawBitmap(\n'''
    portrait_new = '''        canvas.scale(1f, idleBlinkOpenness, getWidth() / 2f, eyelidCenterY);\n        canvas.scale(1f, listeningCueScaleY(), getWidth() / 2f, getHeight() / 2f);\n        canvas.drawBitmap(\n'''
    text = replace_once(text, portrait_anchor, portrait_new,
                        'portrait listening scale')

    landscape_anchor = '        float halfHeight = eye.height() * idleBlinkOpenness / 2f;\n'
    landscape_new = ('        float halfHeight = eye.height() * idleBlinkOpenness '
                     '* listeningCueScaleY() / 2f;\n')
    text = replace_once(text, landscape_anchor, landscape_new,
                        'landscape listening scale')
    return text


def patch_main(text: str) -> str:
    # A second manual invocation should validate and leave the completed tree alone.
    if 'face.startListeningCue();' in text:
        if text.count('face.startListeningCue();') != 2:
            raise SystemExit('listening start hooks: partial or modified prior patch')
        if text.count('face.stopListeningCue();') != 6:
            raise SystemExit('listening stop hooks: partial or modified prior patch')
        if 'face.animate().alpha(0.78f).setDuration(120).start();' in text:
            raise SystemExit('listening start hooks: old dim cue survived prior patch')
        return text

    start = '        face.animate().alpha(0.78f).setDuration(120).start();\n'
    if text.count(start) != 2:
        raise SystemExit(f'listening start hooks: expected two anchors, found {text.count(start)}')
    text = text.replace(start, '        face.startListeningCue();\n')

    stop = '        face.animate().alpha(1.0f).setDuration(120).start();\n'
    if text.count(stop) != 4:
        raise SystemExit(f'listening stop hooks: expected four anchors, found {text.count(stop)}')
    text = text.replace(stop, '        face.stopListeningCue();\n')

    pause_anchor = '''        cancelAssistantFollowUpSilenceTimeout();\n        closeWakeAudioSession();\n'''
    pause_new = '''        cancelAssistantFollowUpSilenceTimeout();\n        if (face != null) face.stopListeningCue();\n        closeWakeAudioSession();\n'''
    text = replace_once(text, pause_anchor, pause_new, 'pause listening cleanup')

    destroy_anchor = '''        if (face != null) {\n            face.stopThinking();\n        }\n'''
    destroy_new = '''        if (face != null) {\n            face.stopListeningCue();\n            face.stopThinking();\n        }\n'''
    text = replace_once(text, destroy_anchor, destroy_new, 'destroy listening cleanup')
    return text


def main() -> None:
    main_text = MAIN.read_text(encoding='utf-8')
    face_text = FACE.read_text(encoding='utf-8')

    # Validate both complete transformations before writing either file.
    patched_main = patch_main(main_text)
    patched_face = patch_face(face_text)
    MAIN.write_text(patched_main, encoding='utf-8')
    FACE.write_text(patched_face, encoding='utf-8')
    print('Approved BOOP eyes now show active recognizer listening with a runtime pulse')


if __name__ == '__main__':
    main()
