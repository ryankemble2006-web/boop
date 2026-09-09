#!/usr/bin/env python3
"""Widen and soften the procedural-eye neutral sclera socket.

v63 proved the moving iris/pupil is genuinely Canvas geometry, but the original
neutralisation socket was only barely larger than the iris. During the reading
sweep that exposed the socket edge / remnants of the baked-in eye interior as a
visible crescent. This experimental v64 patch enlarges the neutral sclera area
beyond the full motion envelope and removes the dark socket-edge gradient.
"""
from pathlib import Path

FACE = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopFaceView.java')
MARKER = 'BOOP_PROCEDURAL_IRIS_CLEAN_SCLERA_V4'


def replace_once(text: str, old: str, new: str, label: str) -> str:
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'{label}: expected one anchor, found {count}')
    return text.replace(old, new, 1)


def main() -> None:
    text = FACE.read_text(encoding='utf-8')
    if MARKER in text:
        print('v64 procedural sclera cleanup already present')
        return
    if 'BOOP_PROCEDURAL_IRISES_V3' not in text:
        raise SystemExit('v64 procedural sclera cleanup requires v63 procedural irises')

    text = replace_once(
        text,
        '    private static final float BASE_CLEAN_RADIUS_X = 224f;\n'
        '    private static final float BASE_CLEAN_RADIUS_Y = 224f;\n',
        '    // BOOP_PROCEDURAL_IRIS_CLEAN_SCLERA_V4: cover the complete reading-motion envelope.\n'
        '    private static final float BASE_CLEAN_RADIUS_X = 300f;\n'
        '    private static final float BASE_CLEAN_RADIUS_Y = 280f;\n',
        'procedural sclera coverage')

    text = replace_once(
        text,
        '            int white = android.graphics.Color.rgb(252, 252, 252);\n'
        '            int mid = android.graphics.Color.rgb(242, 244, 247);\n'
        '            int edge = android.graphics.Color.rgb(216, 221, 227);\n',
        '            int white = android.graphics.Color.rgb(252, 252, 252);\n'
        '            int mid = android.graphics.Color.rgb(249, 250, 252);\n'
        '            int edge = android.graphics.Color.rgb(244, 247, 251);\n',
        'procedural sclera blend')

    text = replace_once(
        text,
        '                    new float[] { 0f, 0.58f, 1f },\n',
        '                    new float[] { 0f, 0.72f, 1f },\n',
        'procedural sclera gradient')

    FACE.write_text(text, encoding='utf-8')
    print('v64 procedural sclera now fully covers the moving iris envelope without a dark socket edge')


if __name__ == '__main__':
    main()
