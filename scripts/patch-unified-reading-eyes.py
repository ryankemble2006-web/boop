#!/usr/bin/env python3
"""Replace the listening bitmap-patch trick with procedural BOOP irises.

The approved PNG remains the source for the white eye bodies and black lids. The
stationary iris/pupil is neutralised once in an in-memory base bitmap, then one
iris, pupil and catchlight set per eye is drawn with Canvas geometry. Listening
moves those coordinates directly. No shifted PNG eye patch is drawn over another.
Wake/audio/routing ownership is untouched.
"""
from pathlib import Path

FACE = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopFaceView.java')
MARKER = 'BOOP_PROCEDURAL_IRISES_V3'


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected one anchor, found {count}')
    return text.replace(old, new, 1)


def replace_method(text: str, signature: str, replacement: str, label: str) -> str:
    if text.count(signature) != 1:
        raise SystemExit(f'{label}: expected one method, found {text.count(signature)}')
    start = text.index(signature)
    opening = text.index('{', start)
    end = opening + 1
    depth = 1
    while depth and end < len(text):
        depth += (text[end] == '{') - (text[end] == '}')
        end += 1
    if depth:
        raise SystemExit(f'{label}: unclosed method')
    return text[:start] + replacement + text[end:]


def patch(text: str) -> str:
    if MARKER in text:
        required = (
            'buildProceduralEyeBase(',
            'drawProceduralIris(',
            'proceduralIrisHueDegrees',
            'BoopListeningGaze.horizontalSourceOffset(',
            'android.graphics.LinearGradient',
        )
        if not all(fragment in text for fragment in required):
            raise SystemExit('procedural-irises patch: partial or altered prior patch')
        return text

    if 'BOOP_LISTENING_EYES_V1' not in text:
        raise SystemExit('procedural-irises patch: v60 listening state must run first')

    old_fields = '''    // BOOP_LISTENING_EYES_V1: runtime pose only; approved bitmap bytes stay untouched.\n    private static final long LISTENING_CUE_HALF_CYCLE_MS = 520L;\n    private static final float LISTENING_CUE_MIN_SCALE_Y = 1.025f;\n    private static final float LISTENING_CUE_MAX_SCALE_Y = 1.060f;\n    private final BoopListeningCueState listeningCueState = new BoopListeningCueState();\n    private ValueAnimator listeningCueAnimator;\n    private float listeningCueFraction;\n'''
    new_fields = '''    // BOOP_LISTENING_EYES_V1: recognizer lifecycle remains the listening-state owner.\n    // BOOP_PROCEDURAL_IRISES_V3: approved PNG supplies whites/lids; irises are Canvas geometry.\n    private static final Rect FULL_FACE_SOURCE = new Rect(0, 0, 1774, 887);\n    private static final float LEFT_IRIS_SOURCE_X = 535f;\n    private static final float RIGHT_IRIS_SOURCE_X = 1233f;\n    private static final float IRIS_SOURCE_Y = 543f;\n    private static final float IRIS_RADIUS_X = 205f;\n    private static final float IRIS_RADIUS_Y = 205f;\n    private static final float PUPIL_RADIUS_X = 148f;\n    private static final float PUPIL_RADIUS_Y = 153f;\n    private static final float BASE_CLEAN_RADIUS_X = 224f;\n    private static final float BASE_CLEAN_RADIUS_Y = 224f;\n    private final BoopListeningCueState listeningCueState = new BoopListeningCueState();\n    private final android.graphics.Paint proceduralIrisPaint =\n            new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);\n    private final android.graphics.Paint proceduralPupilPaint =\n            new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);\n    private final android.graphics.Paint proceduralHighlightPaint =\n            new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);\n    private Bitmap proceduralBaseBitmap;\n    private int proceduralIrisHueDegrees = BoopEyeHueMath.DEFAULT_HUE_DEGREES;\n    private ValueAnimator listeningCueAnimator;\n    private float listeningCueFraction;\n'''
    text = replace_once(text, old_fields, new_fields, 'procedural iris fields')

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

    hue_method = '''    void setEyeHueDegrees(int hueDegrees) {\n        proceduralIrisHueDegrees = BoopEyeHueMath.clampHue(hueDegrees);\n        faceBitmap = originalFaceBitmap;\n        paint.setColorFilter(null);\n        if (proceduralBaseBitmap == null && originalFaceBitmap != null) {\n            proceduralBaseBitmap = buildProceduralEyeBase(originalFaceBitmap);\n        }\n        invalidate();\n    }'''
    text = replace_method(text, '    void setEyeHueDegrees(int hueDegrees) {', hue_method,
                          'procedural hue setter')

    old_scale_method = '''    private float listeningCueScaleY() {\n        if (!listeningCueState.isActive()) return 1f;\n        return LISTENING_CUE_MIN_SCALE_Y\n                + (LISTENING_CUE_MAX_SCALE_Y - LISTENING_CUE_MIN_SCALE_Y)\n                * listeningCueFraction;\n    }\n\n'''
    new_scale_method = '''    private float listeningCueZoom() {\n        return listeningCueState.isActive() ? BoopListeningGaze.zoom() : 1f;\n    }\n\n    private Bitmap proceduralBase() {\n        if (proceduralBaseBitmap == null && originalFaceBitmap != null) {\n            proceduralBaseBitmap = buildProceduralEyeBase(originalFaceBitmap);\n        }\n        return proceduralBaseBitmap != null ? proceduralBaseBitmap : faceBitmap;\n    }\n\n    private Bitmap buildProceduralEyeBase(Bitmap source) {\n        if (source == null) return null;\n        Bitmap base = source.copy(Bitmap.Config.ARGB_8888, true);\n        Canvas baseCanvas = new Canvas(base);\n        android.graphics.Paint cleanPaint =\n                new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);\n        float sx = base.getWidth() / 1774f;\n        float sy = base.getHeight() / 887f;\n        float[] centres = new float[] { LEFT_IRIS_SOURCE_X, RIGHT_IRIS_SOURCE_X };\n        for (float sourceX : centres) {\n            float cx = sourceX * sx;\n            float cy = IRIS_SOURCE_Y * sy;\n            float rx = BASE_CLEAN_RADIUS_X * sx;\n            float ry = BASE_CLEAN_RADIUS_Y * sy;\n            int white = android.graphics.Color.rgb(252, 252, 252);\n            int mid = android.graphics.Color.rgb(242, 244, 247);\n            int edge = android.graphics.Color.rgb(216, 221, 227);\n            cleanPaint.setShader(new android.graphics.RadialGradient(\n                    cx - 58f * sx, cy - 70f * sy, Math.max(rx, ry) * 1.25f,\n                    new int[] { white, mid, edge },\n                    new float[] { 0f, 0.58f, 1f },\n                    android.graphics.Shader.TileMode.CLAMP));\n            baseCanvas.drawOval(new RectF(cx - rx, cy - ry, cx + rx, cy + ry), cleanPaint);\n            cleanPaint.setShader(null);\n        }\n        return base;\n    }\n\n    private int irisColour(float saturation, float value) {\n        return android.graphics.Color.HSVToColor(new float[] {\n                proceduralIrisHueDegrees, saturation, value });\n    }\n\n    private void drawProceduralIris(\n            Canvas canvas, Rect source, RectF destination, float irisSourceX) {\n        if (source.width() <= 0 || source.height() <= 0) return;\n        float scaleX = destination.width() / source.width();\n        float scaleY = destination.height() / source.height();\n        float offsetX = listeningCueState.isActive()\n                ? BoopListeningGaze.horizontalSourceOffset(listeningCueFraction) : 0f;\n        float offsetY = listeningCueState.isActive()\n                ? BoopListeningGaze.verticalSourceOffset() : 0f;\n        float cx = destination.left + (irisSourceX + offsetX - source.left) * scaleX;\n        float cy = destination.top + (IRIS_SOURCE_Y + offsetY - source.top) * scaleY;\n        float irisRx = IRIS_RADIUS_X * scaleX;\n        float irisRy = IRIS_RADIUS_Y * scaleY;\n\n        int dark = irisColour(0.94f, 0.30f);\n        int body = irisColour(0.92f, 0.72f);\n        int bright = irisColour(0.88f, 1.00f);\n        proceduralIrisPaint.setStyle(android.graphics.Paint.Style.FILL);\n        proceduralIrisPaint.setShader(new android.graphics.LinearGradient(\n                cx, cy - irisRy, cx, cy + irisRy,\n                new int[] { dark, body, bright },\n                new float[] { 0f, 0.52f, 1f },\n                android.graphics.Shader.TileMode.CLAMP));\n        RectF iris = new RectF(cx - irisRx, cy - irisRy, cx + irisRx, cy + irisRy);\n        canvas.drawOval(iris, proceduralIrisPaint);\n        proceduralIrisPaint.setShader(null);\n        proceduralIrisPaint.setStyle(android.graphics.Paint.Style.STROKE);\n        proceduralIrisPaint.setStrokeWidth(Math.max(2f, 7f * Math.min(scaleX, scaleY)));\n        proceduralIrisPaint.setColor(irisColour(0.95f, 0.55f));\n        canvas.drawOval(iris, proceduralIrisPaint);\n        proceduralIrisPaint.setStyle(android.graphics.Paint.Style.FILL);\n\n        proceduralPupilPaint.setColor(android.graphics.Color.BLACK);\n        canvas.drawOval(new RectF(\n                cx - PUPIL_RADIUS_X * scaleX,\n                cy - PUPIL_RADIUS_Y * scaleY,\n                cx + PUPIL_RADIUS_X * scaleX,\n                cy + PUPIL_RADIUS_Y * scaleY), proceduralPupilPaint);\n\n        proceduralHighlightPaint.setColor(android.graphics.Color.WHITE);\n        canvas.drawOval(new RectF(\n                cx - 104f * scaleX, cy - 124f * scaleY,\n                cx - 30f * scaleX, cy - 40f * scaleY), proceduralHighlightPaint);\n        canvas.drawCircle(\n                cx - 88f * scaleX, cy - 20f * scaleY,\n                Math.max(2f, 10f * Math.min(scaleX, scaleY)), proceduralHighlightPaint);\n    }\n\n'''
    text = replace_once(text, old_scale_method, new_scale_method, 'procedural iris renderer')

    old_portrait = '''        canvas.scale(1f, idleBlinkOpenness, getWidth() / 2f, eyelidCenterY);\n        canvas.scale(1f, listeningCueScaleY(), getWidth() / 2f, getHeight() / 2f);\n        canvas.drawBitmap(\n                faceBitmap,\n                null,\n                new RectF(left, top, left + width, top + height),\n                paint);\n        canvas.restoreToCount(eyelidSave);\n'''
    new_portrait = '''        canvas.scale(1f, idleBlinkOpenness, getWidth() / 2f, eyelidCenterY);\n        float listeningZoom = listeningCueZoom();\n        canvas.scale(listeningZoom, listeningZoom, getWidth() / 2f, getHeight() / 2f);\n        RectF faceDestination = new RectF(left, top, left + width, top + height);\n        Bitmap eyeBase = proceduralBase();\n        if (eyeBase != null) canvas.drawBitmap(eyeBase, null, faceDestination, paint);\n        drawProceduralIris(canvas, FULL_FACE_SOURCE, faceDestination, LEFT_IRIS_SOURCE_X);\n        drawProceduralIris(canvas, FULL_FACE_SOURCE, faceDestination, RIGHT_IRIS_SOURCE_X);\n        canvas.restoreToCount(eyelidSave);\n'''
    text = replace_once(text, old_portrait, new_portrait, 'portrait procedural eyes')

    old_landscape = '''        float halfWidth = eye.width() / 2f;\n        float halfHeight = eye.height() * idleBlinkOpenness * listeningCueScaleY() / 2f;\n        RectF destination = new RectF(\n                eye.centerX() - halfWidth,\n                eye.centerY() - halfHeight,\n                eye.centerX() + halfWidth,\n                eye.centerY() + halfHeight);\n        canvas.drawBitmap(faceBitmap, source, destination, paint);\n'''
    new_landscape = '''        float listeningZoom = listeningCueZoom();\n        float halfWidth = eye.width() * listeningZoom / 2f;\n        float halfHeight = eye.height() * idleBlinkOpenness * listeningZoom / 2f;\n        RectF destination = new RectF(\n                eye.centerX() - halfWidth,\n                eye.centerY() - halfHeight,\n                eye.centerX() + halfWidth,\n                eye.centerY() + halfHeight);\n        Bitmap eyeBase = proceduralBase();\n        if (eyeBase != null) canvas.drawBitmap(eyeBase, source, destination, paint);\n        float irisSourceX = source.left < BoopApprovedEyeGeometry.CANVAS_WIDTH / 2\n                ? LEFT_IRIS_SOURCE_X : RIGHT_IRIS_SOURCE_X;\n        drawProceduralIris(canvas, source, destination, irisSourceX);\n'''
    text = replace_once(text, old_landscape, new_landscape, 'landscape procedural eyes')
    return text


def main() -> None:
    original = FACE.read_text(encoding='utf-8')
    result = patch(original)
    temp = FACE.with_suffix('.java.procedural-eyes-tmp')
    temp.write_text(result, encoding='utf-8')
    temp.replace(FACE)
    print('BOOP irises are now procedural Canvas geometry; no moving PNG eye patch remains')


if __name__ == '__main__':
    main()
