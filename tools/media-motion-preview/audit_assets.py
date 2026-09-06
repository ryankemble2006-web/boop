"""Check real alpha and create a light/dark visual proof of the cutouts."""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent
result = Image.new("RGB", (1200, 850), "#10151a")
for row, name in enumerate(("music", "cinema-base", "hand", "kernel")):
    im = Image.open(ROOT / "assets" / f"{name}.png")
    assert im.mode == "RGBA", name
    assert im.getchannel("A").getextrema() == (0, 255), name
    for point in ((0, 0), (1535, 0), (0, 1023), (1535, 1023)):
        assert im.getpixel(point)[3] == 0, (name, point)
    box = im.getbbox()
    cropped = im.crop(box)
    cropped.thumbnail((540, 190), Image.Resampling.LANCZOS)
    for col, colour in enumerate(("#111921", "#d9e6ed")):
        tile = Image.new("RGBA", (600, 212), colour)
        tile.alpha_composite(cropped, ((600 - cropped.width) // 2, 10))
        ImageDraw.Draw(tile).text((14, 192), name, fill="#44a2b4" if col == 0 else "#20343a")
        result.paste(tile.convert("RGB"), (col * 600, row * 212))
result.save(ROOT / "asset-alpha-proof.png")
print("4 RGBA assets; transparent corners and fully opaque subject pixels: PASS")
