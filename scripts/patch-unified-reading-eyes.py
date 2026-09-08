#!/usr/bin/env python3
"""Upgrade v60's approved-eye listening cue into a clear reading gaze.

Runs after patch-unified-listening-eyes.py. It keeps the exact approved bitmap and
all recognizer/wake ownership unchanged. Active listening zooms the existing eyes,
clears the stationary iris aperture, then redraws one shifted iris/pupil layer from
the same runtime bitmap with a smooth left/right, slightly downward reading offset.
"""
from pathlib import Path

FACE = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopFaceView.java')
MARKER = 'BOOP_LISTENING_READING_EYES_V2'


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected one anchor, found {count}')
    return text.replace(old, new, 1)


def patch(text: str) -> str:
    if MARKER in text:
        required = (
            'BoopListeningGaze.HALF_SWEEP_MS',
            'private float listeningCueZoom()',
            'private void drawListeningGazePatch(',
            'listeningGazeClipPath.addOval(',
            'listeningGazeClearPaint.setBlendMode(android.graphics.BlendMode.CLEAR);',
            'canvas.saveLayer(faceDestination, null)',
            'drawListeningGazePatch(canvas, FULL_FACE_SOURCE',
            'drawListeningGazePatch(canvas, source, destination, irisSourceX);',
        )
        if not all(fragment in text for fragment in required):
            raise SystemExit('reading-eyes patch: partial or altered prior patch')
        return text

    if 'BOOP_LISTENING_EYES_V1' not in text:
        raise SystemExit('reading-eyes patch: v60 listening-eye patch must run first')

    old_fields = '''    // BOOP_LISTENING_EYES_V1: runtime pose only; approved bitmap bytes stay untouched.\n    private static final long LISTENING_CUE_HALF_CYCLE_MS = 520L;\n    private static final float LISTENING_CUE_MIN_SCALE_Y = 1.025f;\n    private static final float LISTENING_CUE_MAX_SCALE_Y = 1.060f;\n    private final BoopListeningCueState listeningCueState = new BoopListeningCueState();\n    private ValueAnimator listeningCueAnimator;\n    private float listeningCueFraction;\n'''
    new_fields = '''    // BOOP_LISTENING_EYES_V1: runtime pose only; approved bitmap bytes stay untouched.\n    // BOOP_LISTENING_READING_EYES_V2: same bitmap, clearer zoom + single-layer reading gaze.\n    private static final Rect FULL_FACE_SOURCE = new Rect(0, 0, 1774, 887);\n    private static final float LEFT_IRIS_SOURCE_X = 535f;\n    private static final float RIGHT_IRIS_SOURCE_X = 1233f;\n    private static final float IRIS_SOURCE_Y = 543f;\n    private final BoopListeningCueState listeningCueState = new BoopListeningCueState();\n    private final android.graphics.Path listeningGazeClipPath = new android.graphics.Path();\n    private final android.graphics.Paint listeningGazeClearPaint =\n            new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);\n    private ValueAnimator listeningCueAnimator;\n    private float listeningCueFraction;\n'''
    text = replace_once(text, old_fields, new_fields, 'reading cue fields')

    text = replace_once(
        text,
        '            listeningCueFraction = 1f;\n',
        '            listeningCueFraction = 0.5f;\n',
        'static reading pose when animations are disabled')
    text = replace_once(
        text,
        '        animator.setDuration(LISTENING_CUE_HALF_CYCLE_MS);\n',
        '        animator.setDuration(BoopListeningGaze.HALF_SWEEP_MS);\n',
        'reading sweep duration')

    old_scale_method = '''    private float listeningCueScaleY() {\n        if (!listeningCueState.isActive()) return 1f;\n        return LISTENING_CUE_MIN_SCALE_Y\n                + (LISTENING_CUE_MAX_SCALE_Y - LISTENING_CUE_MIN_SCALE_Y)\n                * listeningCueFraction;\n    }\n\n'''
    new_scale_method = '''    private float listeningCueZoom() {\n        return listeningCueState.isActive() ? BoopListeningGaze.zoom() : 1f;\n    }\n\n    private void drawListeningGazePatch(\n            Canvas canvas, Rect source, RectF destination, float irisSourceX) {\n        if (!listeningCueState.isActive() || source.width() <= 0 || source.height() <= 0) return;\n\n        float scaleX = destination.width() / source.width();\n        float scaleY = destination.height() / source.height();\n        float centreX = destination.left + (irisSourceX - source.left) * scaleX;\n        float centreY = destination.top + (IRIS_SOURCE_Y - source.top) * scaleY;\n        float radius = BoopListeningGaze.patchRadiusSource();\n        RectF clip = new RectF(\n                centreX - radius * scaleX,\n                centreY - radius * scaleY,\n                centreX + radius * scaleX,\n                centreY + radius * scaleY);\n        float offsetX = BoopListeningGaze.horizontalSourceOffset(listeningCueFraction) * scaleX;\n        float offsetY = BoopListeningGaze.verticalSourceOffset() * scaleY;\n        RectF shifted = new RectF(\n                destination.left + offsetX,\n                destination.top + offsetY,\n                destination.right + offsetX,\n                destination.bottom + offsetY);\n\n        listeningGazeClipPath.reset();\n        listeningGazeClipPath.addOval(clip, android.graphics.Path.Direction.CW);\n        if (BoopListeningGaze.clearBaseApertureBeforeShiftedPatch()) {\n            listeningGazeClearPaint.setBlendMode(android.graphics.BlendMode.CLEAR);\n            canvas.drawOval(clip, listeningGazeClearPaint);\n        }\n\n        int save = canvas.save();\n        canvas.clipPath(listeningGazeClipPath);\n        canvas.drawBitmap(faceBitmap, source, shifted, paint);\n        canvas.restoreToCount(save);\n    }\n\n'''
    text = replace_once(text, old_scale_method, new_scale_method, 'reading gaze renderer')

    old_portrait = '''        canvas.scale(1f, idleBlinkOpenness, getWidth() / 2f, eyelidCenterY);\n        canvas.scale(1f, listeningCueScaleY(), getWidth() / 2f, getHeight() / 2f);\n        canvas.drawBitmap(\n                faceBitmap,\n                null,\n                new RectF(left, top, left + width, top + height),\n                paint);\n        canvas.restoreToCount(eyelidSave);\n'''
    new_portrait = '''        canvas.scale(1f, idleBlinkOpenness, getWidth() / 2f, eyelidCenterY);\n        float listeningZoom = listeningCueZoom();\n        canvas.scale(listeningZoom, listeningZoom, getWidth() / 2f, getHeight() / 2f);\n        RectF faceDestination = new RectF(left, top, left + width, top + height);\n        int listeningLayer = listeningCueState.isActive()\n                && BoopListeningGaze.clearBaseApertureBeforeShiftedPatch()\n                ? canvas.saveLayer(faceDestination, null) : -1;\n        canvas.drawBitmap(faceBitmap, null, faceDestination, paint);\n        drawListeningGazePatch(canvas, FULL_FACE_SOURCE, faceDestination, LEFT_IRIS_SOURCE_X);\n        drawListeningGazePatch(canvas, FULL_FACE_SOURCE, faceDestination, RIGHT_IRIS_SOURCE_X);\n        if (listeningLayer >= 0) canvas.restoreToCount(listeningLayer);\n        canvas.restoreToCount(eyelidSave);\n'''
    text = replace_once(text, old_portrait, new_portrait, 'portrait reading gaze')

    old_landscape = '''        float halfWidth = eye.width() / 2f;\n        float halfHeight = eye.height() * idleBlinkOpenness * listeningCueScaleY() / 2f;\n        RectF destination = new RectF(\n                eye.centerX() - halfWidth,\n                eye.centerY() - halfHeight,\n                eye.centerX() + halfWidth,\n                eye.centerY() + halfHeight);\n        canvas.drawBitmap(faceBitmap, source, destination, paint);\n'''
    new_landscape = '''        float listeningZoom = listeningCueZoom();\n        float halfWidth = eye.width() * listeningZoom / 2f;\n        float halfHeight = eye.height() * idleBlinkOpenness * listeningZoom / 2f;\n        RectF destination = new RectF(\n                eye.centerX() - halfWidth,\n                eye.centerY() - halfHeight,\n                eye.centerX() + halfWidth,\n                eye.centerY() + halfHeight);\n        int listeningLayer = listeningCueState.isActive()\n                && BoopListeningGaze.clearBaseApertureBeforeShiftedPatch()\n                ? canvas.saveLayer(destination, null) : -1;\n        canvas.drawBitmap(faceBitmap, source, destination, paint);\n        float irisSourceX = source.left < faceBitmap.getWidth() / 2\n                ? LEFT_IRIS_SOURCE_X : RIGHT_IRIS_SOURCE_X;\n        drawListeningGazePatch(canvas, source, destination, irisSourceX);\n        if (listeningLayer >= 0) canvas.restoreToCount(listeningLayer);\n'''
    text = replace_once(text, old_landscape, new_landscape, 'landscape reading gaze')
    return text


def main() -> None:
    original = FACE.read_text(encoding='utf-8')
    result = patch(original)
    temp = FACE.with_suffix('.java.reading-eyes-tmp')
    temp.write_text(result, encoding='utf-8')
    temp.replace(FACE)
    print('Approved BOOP eyes now use one moving reading layer while active listening')


if __name__ == '__main__':
    main()
