"""Non-destructive sprite cleanup, explicitly approved by Ryan on 2026-09-06.

Requires Pillow, NumPy and OpenCV. Preserves source RGB and changes only alpha.
Usage: python clean_layers.py --source source --output assets
"""
import argparse
from pathlib import Path

import cv2
import numpy as np
from PIL import Image


def extract(image, rectangles, seeds=(), chroma_floor=None):
    rgb = np.asarray(image.convert("RGB")).copy()
    mask = np.zeros(rgb.shape[:2], np.uint8)
    for left, top, right, bottom in rectangles:
        mask[top:bottom, left:right] = cv2.GC_PR_FGD
    for x, y, radius in seeds:
        cv2.circle(mask, (x, y), radius, cv2.GC_FGD, -1)
    bg_model, fg_model = np.zeros((1, 65)), np.zeros((1, 65))
    cv2.setRNGSeed(19)
    cv2.grabCut(rgb, mask, None, bg_model, fg_model, 6, cv2.GC_INIT_WITH_MASK)
    binary = ((mask == cv2.GC_FGD) | (mask == cv2.GC_PR_FGD)).astype(np.uint8)
    # Keep subject-sized islands; discard checkerboard speckles, not subject detail.
    count, labels, stats, _ = cv2.connectedComponentsWithStats(binary, 8)
    keep = np.zeros_like(binary)
    for label in range(1, count):
        if stats[label, cv2.CC_STAT_AREA] > 80:
            keep[labels == label] = 255
    if chroma_floor is not None:
        # These isolated yellow/cream objects contain no neutral-white details.
        # Exclude remaining neutral checker squares touching their contours.
        chroma = rgb.max(axis=2).astype(float) - rgb.min(axis=2)
        keep[chroma < chroma_floor] = 0
    # Subpixel-soft edges, restricted to the silhouette (no backdrop halo).
    alpha = cv2.GaussianBlur(keep, (3, 3), 0.45)
    alpha[alpha < 8] = 0
    return Image.fromarray(np.dstack((rgb, alpha)), "RGBA")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--source", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=True)
    jobs = {
        "music": ([(210, 110, 1310, 914)], [(790, 150, 18), (325, 610, 28),
                    (1195, 721, 30), (558, 610, 75), (927, 683, 70)]),
        "cinema-base": ([(470, 126, 767, 458), (840, 148, 1145, 484),
                         (659, 574, 1090, 981)], [(602, 305, 75), (976, 332, 70),
                         (844, 729, 65), (948, 856, 50), (824, 651, 40)]),
        "hand": ([(360, 452, 688, 854)], [(465, 676, 45), (543, 491, 18)]),
        "kernel": ([(598, 442, 694, 541)], [(646, 491, 17)]),
    }
    for name, (rectangles, seeds) in jobs.items():
        source = Image.open(args.source / f"{name}.png")
        result = extract(source, rectangles, seeds,
                         {"hand": 35, "kernel": 12}.get(name))
        result.save(args.output / f"{name}.png", optimize=True)
        print(name, result.getbbox(), result.getchannel("A").getextrema())


if __name__ == "__main__":
    main()
