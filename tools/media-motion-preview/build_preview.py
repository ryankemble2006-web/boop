"""Build a self-contained preview from the Java export and approved cleaned art."""
import argparse
import base64
import io
import json
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--motion", required=True, type=Path)
    parser.add_argument("--fragment", required=True, type=Path)
    parser.add_argument("--standalone", required=True, type=Path)
    args = parser.parse_args()
    motion = json.loads(args.motion.read_text(encoding="utf-8-sig"))
    sources = {}
    for name in ("music", "cinema-base", "hand", "kernel"):
        im = Image.open(ROOT / "assets" / f"{name}.png")
        im.thumbnail((1152, 768), Image.Resampling.LANCZOS)
        data = io.BytesIO()
        im.save(data, format="WEBP", quality=91, method=6)
        sources[name] = "data:image/webp;base64," + base64.b64encode(data.getvalue()).decode("ascii")
    fragment = (ROOT / "preview.fragment.html").read_text(encoding="utf-8")
    fragment = fragment.replace("__MOTION__", json.dumps(motion, separators=(",", ":")))
    fragment = fragment.replace("__SOURCES__", json.dumps(sources, separators=(",", ":")))
    assert len(fragment.encode("utf-8")) < 1_000_000, "Inline preview must remain under 1MB"
    args.fragment.parent.mkdir(parents=True, exist_ok=True)
    args.fragment.write_text(fragment, encoding="utf-8")
    shell = '''<!doctype html><html lang="en"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>BOOP · H1 and P1 in motion</title><style>body{background:#0c1117;color:#e8eef4;font:16px system-ui;margin:24px auto;padding:0 16px;max-width:920px}button{background:#243543;color:inherit;border:1px solid #658297;border-radius:8px;padding:10px 18px;font:inherit;cursor:pointer}button:focus-visible{outline:3px solid #1dd7ff}.text-small{font-size:13px}</style>'''
    args.standalone.parent.mkdir(parents=True, exist_ok=True)
    args.standalone.write_text(shell + fragment + "</html>\n", encoding="utf-8")
    for phase, milliseconds in (("rest", 0), ("lift", 5900), ("nibble", 6800), ("return", 8200)):
        qa_fragment = fragment.replace('id="boop-media-motion"',
            f'id="boop-media-motion" data-start-paused="true" data-start-ms="{milliseconds}"', 1)
        (args.standalone.parent / f"qa-{phase}.html").write_text(
            shell + qa_fragment + "</html>\n", encoding="utf-8")
    print(f"Preview built: {len(fragment.encode('utf-8')):,} bytes")


if __name__ == "__main__":
    main()
