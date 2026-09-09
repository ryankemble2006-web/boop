#!/usr/bin/env python3
"""Inspect the two black Photoshop leftovers above BOOP's real eyelids.

This script is deliberately diagnostic only. It reads the exact Shield headphones
PNG copied into Animation Lab and emits evidence, not replacement artwork. That
lets us locate the unwanted flattened pixels without regenerating or reinterpreting
BOOP's approved character.
"""
from __future__ import annotations

from collections import deque
from hashlib import sha256
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "assets" / "boop-headphones-source.png"
ANALYSIS = ROOT / "analysis"
REPORT = ANALYSIS / "artifact-map.txt"

# Broad upper-eye windows derived from the real Shield renderer geometry and the
# 2026-09-09 physical video. They intentionally stop above the lower face.
ROIS = {
    "left": (360, 360, 760, 660),
    "right": (760, 360, 1160, 660),
}


def rgba_max(pixel: tuple[int, int, int, int]) -> int:
    return max(pixel[0], pixel[1], pixel[2])


def components(image: Image.Image, roi: tuple[int, int, int, int], threshold: int = 42):
    x0, y0, x1, y1 = roi
    w, h = x1 - x0, y1 - y0
    data = image.load()
    dark = bytearray(w * h)
    for y in range(h):
        for x in range(w):
            p = data[x + x0, y + y0]
            if p[3] >= 32 and rgba_max(p) <= threshold:
                dark[y * w + x] = 1

    seen = bytearray(w * h)
    out = []
    for sy in range(h):
        for sx in range(w):
            start = sy * w + sx
            if not dark[start] or seen[start]:
                continue
            q = deque([(sx, sy)])
            seen[start] = 1
            minx = maxx = sx
            miny = maxy = sy
            area = 0
            while q:
                x, y = q.popleft()
                area += 1
                minx, maxx = min(minx, x), max(maxx, x)
                miny, maxy = min(miny, y), max(maxy, y)
                for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1),
                               (x - 1, y - 1), (x + 1, y - 1),
                               (x - 1, y + 1), (x + 1, y + 1)):
                    if 0 <= nx < w and 0 <= ny < h:
                        idx = ny * w + nx
                        if dark[idx] and not seen[idx]:
                            seen[idx] = 1
                            q.append((nx, ny))
            out.append({
                "area": area,
                "bbox": (minx + x0, miny + y0, maxx + 1 + x0, maxy + 1 + y0),
            })
    return sorted(out, key=lambda item: item["area"], reverse=True)


def ascii_map(image: Image.Image, roi: tuple[int, int, int, int], cell: int = 5) -> list[str]:
    x0, y0, x1, y1 = roi
    px = image.load()
    rows: list[str] = []
    for y in range(y0, y1, cell):
        chars = []
        for x in range(x0, x1, cell):
            samples = []
            for yy in range(y, min(y + cell, y1)):
                for xx in range(x, min(x + cell, x1)):
                    p = px[xx, yy]
                    if p[3] >= 24:
                        samples.append(rgba_max(p))
            if not samples:
                chars.append(" ")
                continue
            core = sum(v <= 42 for v in samples) / len(samples)
            dark = sum(v <= 80 for v in samples) / len(samples)
            if core >= 0.35:
                chars.append("#")
            elif dark >= 0.35:
                chars.append("+")
            else:
                chars.append(".")
        rows.append("".join(chars).rstrip())
    return rows


def row_counts(image: Image.Image, roi: tuple[int, int, int, int]) -> list[str]:
    x0, y0, x1, y1 = roi
    px = image.load()
    out = []
    for y in range(y0, y1):
        core = dark = opaque = 0
        for x in range(x0, x1):
            p = px[x, y]
            if p[3] < 24:
                continue
            opaque += 1
            m = rgba_max(p)
            core += m <= 42
            dark += m <= 80
        if core or dark:
            out.append(f"{y:04d}: core42={core:3d} dark80={dark:3d} opaque={opaque:3d}")
    return out


def write_visual_crop(image: Image.Image, name: str, roi: tuple[int, int, int, int]) -> None:
    # A neutral background makes transparent gaps and the flat-black leftovers
    # visible in a small GitHub-viewable diagnostic. This never touches source art.
    background = Image.new("RGBA", image.size, (184, 184, 184, 255))
    preview = Image.alpha_composite(background, image)
    crop = preview.crop(roi).convert("RGB")
    crop.thumbnail((320, 240), Image.Resampling.LANCZOS)
    crop.save(ANALYSIS / f"{name}-eye-inspection.jpg", "JPEG", quality=82, optimize=True)


def main() -> None:
    raw = SOURCE.read_bytes()
    image = Image.open(SOURCE).convert("RGBA")
    ANALYSIS.mkdir(parents=True, exist_ok=True)
    lines = [
        "BOOP Shield eyelid-artifact inspection",
        "========================================",
        f"source={SOURCE.relative_to(ROOT)}",
        f"sha256={sha256(raw).hexdigest()}",
        f"size_bytes={len(raw)}",
        f"image={image.width}x{image.height} RGBA",
        f"alpha_bbox={image.getchannel('A').getbbox()}",
        "",
        "Legend: # = mostly <=42 RGB, + = mostly <=80 RGB, . = opaque brighter pixel, space = transparent.",
        "This is diagnostic evidence only. No source pixels are changed by this script.",
        "",
    ]
    for name, roi in ROIS.items():
        write_visual_crop(image, name, roi)
        lines.append(f"[{name}] roi={roi}")
        comps = components(image, roi)
        lines.append("largest dark connected components (threshold <=42):")
        for item in comps[:12]:
            lines.append(f"  area={item['area']:6d} bbox={item['bbox']}")
        lines.append("ascii map, 5 source pixels per character:")
        for row in ascii_map(image, roi):
            lines.append("|" + row)
        lines.append("row counts where dark pixels exist:")
        lines.extend(row_counts(image, roi))
        lines.append("")

    REPORT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(REPORT)


if __name__ == "__main__":
    main()
