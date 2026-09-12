import unittest
from hashlib import sha256
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
APPROVED_SHA256 = "c86d8fa046d1d6b6c96c15d5a6e38f3c0b89533a65946fc6d59347e7679250e9"
ASSET = ROOT / "shield-clean-launcher/app/src/main/res/drawable-nodpi/boop_music_h1_approved.png"
DRAWABLE = ROOT / "shield-clean-launcher/app/src/main/res/drawable/boop_headphones.xml"


class ApprovedMusicPuppetAssetTest(unittest.TestCase):
    def test_music_puppet_uses_exact_approved_h1_asset(self):
        self.assertTrue(ASSET.is_file())
        self.assertEqual(APPROVED_SHA256, sha256(ASSET.read_bytes()).hexdigest())

    def test_headphones_slot_points_to_approved_h1_not_legacy(self):
        text = DRAWABLE.read_text(encoding="utf-8")
        self.assertIn('@drawable/boop_music_h1_approved', text)
        self.assertNotIn('@drawable/boop_headphones_legacy', text)


if __name__ == "__main__":
    unittest.main()
