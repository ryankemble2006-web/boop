from pathlib import Path
import struct
import unittest


ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "shield-overlay" / "app" / "src" / "main" / "res"
MANIFEST = ROOT / "shield-overlay" / "app" / "src" / "main" / "AndroidManifest.xml"


def png_size(path: Path):
    data = path.read_bytes()
    if len(data) < 24 or data[:8] != b"\x89PNG\r\n\x1a\n":
        raise AssertionError(f"not a PNG: {path}")
    return struct.unpack(">II", data[16:24])


class ShieldLauncherAssetsTest(unittest.TestCase):
    def test_tv_banner_is_exactly_320_by_180(self):
        path = RES / "drawable-xhdpi" / "boop_shield_banner.png"
        self.assertTrue(path.exists(), f"missing {path.relative_to(ROOT)}")
        self.assertEqual((320, 180), png_size(path))

    def test_tv_launcher_icons_are_square_and_at_least_160_pixels(self):
        for name in ("ic_boop_shield.png", "ic_boop_shield_round.png"):
            path = RES / "mipmap-xhdpi" / name
            self.assertTrue(path.exists(), f"missing {path.relative_to(ROOT)}")
            width, height = png_size(path)
            self.assertEqual(width, height)
            self.assertGreaterEqual(width, 160)

    def test_manifest_uses_the_new_shield_identity(self):
        text = MANIFEST.read_text(encoding="utf-8")
        self.assertIn('android:icon="@mipmap/ic_boop_shield"', text)
        self.assertIn('android:roundIcon="@mipmap/ic_boop_shield_round"', text)
        self.assertIn('android:banner="@drawable/boop_shield_banner"', text)


if __name__ == "__main__":
    unittest.main()
