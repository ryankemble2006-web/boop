#!/usr/bin/env python3
"""Feather v64's neutral sclera socket into the approved PNG shading.

v64 removed the old baked-in iris cleanly, but its replacement sclera remained an
opaque oval, which looked like a pale contact lens against the approved grey eye
shading. Keep the old iris fully covered in the centre, then fade the cleanup to
transparent before the socket edge so the original sclera shading takes over
smoothly. Procedural iris/pupil motion remains unchanged.
"""
from pathlib import Path

FACE = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopFaceView.java')
MARKER = 'BOOP_PROCEDURAL_IRIS_FEATHERED_SCLERA_V5'


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected one anchor, found {count}')
    return text.replace(old, new, 1)


def main() -> None:
    text = FACE.read_text(encoding='utf-8')
    if MARKER in text:
        print('v65 feathered sclera already present')
        return
    if 'BOOP_PROCEDURAL_IRIS_CLEAN_SCLERA_V4' not in text:
        raise SystemExit('v65 feathered sclera requires v64 cleanup')

    old = '''            int white = android.graphics.Color.rgb(252, 252, 252);\n            int mid = android.graphics.Color.rgb(249, 250, 252);\n            int edge = android.graphics.Color.rgb(244, 247, 251);\n            cleanPaint.setShader(new android.graphics.RadialGradient(\n                    cx - 58f * sx, cy - 70f * sy, Math.max(rx, ry) * 1.25f,\n                    new int[] { white, mid, edge },\n                    new float[] { 0f, 0.72f, 1f },\n                    android.graphics.Shader.TileMode.CLAMP));\n'''
    new = '''            // BOOP_PROCEDURAL_IRIS_FEATHERED_SCLERA_V5: fully hide the old\n            // baked-in iris, then fade into the approved sclera instead of\n            // leaving an opaque white contact-lens edge.\n            int white = android.graphics.Color.argb(255, 252, 252, 252);\n            int mid = android.graphics.Color.argb(255, 249, 250, 252);\n            int innerEdge = android.graphics.Color.argb(255, 247, 249, 251);\n            int feather = android.graphics.Color.argb(150, 247, 249, 251);\n            int clear = android.graphics.Color.argb(0, 247, 249, 251);\n            cleanPaint.setShader(new android.graphics.RadialGradient(\n                    cx, cy, Math.min(rx, ry),\n                    new int[] { white, mid, innerEdge, feather, clear },\n                    new float[] { 0f, 0.55f, 0.82f, 0.92f, 1f },\n                    android.graphics.Shader.TileMode.CLAMP));\n'''
    text = replace_once(text, old, new, 'feathered procedural sclera')
    FACE.write_text(text, encoding='utf-8')
    print('v65 procedural sclera now feathers into the approved grey eye shading')


if __name__ == '__main__':
    main()
