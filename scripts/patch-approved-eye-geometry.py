#!/usr/bin/env python3
"""Wire the approved BOOP eye geometry into the generated Wall sources.

This changes source rectangles/layout constants only. It never inspects, rewrites,
thresholds, masks, or otherwise alters the approved PNG bytes.
"""
from pathlib import Path

ROOT = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1")
FACE = ROOT / "BoopFaceView.java"
LAYOUT = ROOT / "BoopEyeLayout.java"


def replace_once(text, old, new, label):
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one source anchor, found {count}")
    return text.replace(old, new, 1)


def main():
    face = FACE.read_text(encoding="utf-8")
    face = replace_once(
        face,
        "    static final Rect LEFT_SOURCE = new Rect(90, 600, 419, 993);\n"
        "    static final Rect RIGHT_SOURCE = new Rect(525, 600, 854, 993);\n",
        "    static final Rect LEFT_SOURCE = new Rect(BoopApprovedEyeGeometry.LEFT_SOURCE);\n"
        "    static final Rect RIGHT_SOURCE = new Rect(BoopApprovedEyeGeometry.RIGHT_SOURCE);\n",
        "approved face source rectangles",
    )

    layout = LAYOUT.read_text(encoding="utf-8")
    layout = replace_once(
        layout,
        "    static final float SOURCE_WIDTH = 941f;\n"
        "    static final float SOURCE_EYE_CENTRE_DISTANCE = 435f;\n"
        "    static final float SOURCE_EYE_WIDTH = 329f;\n"
        "    static final float SOURCE_EYE_HEIGHT = 393f;\n",
        "    static final float SOURCE_WIDTH = BoopApprovedEyeGeometry.CANVAS_WIDTH;\n"
        "    static final float SOURCE_EYE_CENTRE_DISTANCE = BoopApprovedEyeGeometry.EYE_CENTRE_DISTANCE;\n"
        "    static final float SOURCE_EYE_WIDTH = BoopApprovedEyeGeometry.EYE_WIDTH;\n"
        "    static final float SOURCE_EYE_HEIGHT = BoopApprovedEyeGeometry.EYE_HEIGHT;\n",
        "approved layout geometry",
    )

    # Validate both files before writing either so this remains fail-closed.
    FACE.write_text(face, encoding="utf-8")
    LAYOUT.write_text(layout, encoding="utf-8")
    print("Approved BOOP eye geometry wired; PNG bytes untouched")


if __name__ == "__main__":
    main()
