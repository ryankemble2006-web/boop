from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "shield-overlay" / "app"
MAIN = APP / "src" / "main"


class ShieldPairingGateUiSourceTest(unittest.TestCase):
    def read_main(self, relative):
        path = MAIN / relative
        self.assertTrue(path.exists(), f"missing required file: {path.relative_to(ROOT)}")
        return path.read_text(encoding="utf-8")

    def test_qr_renderer_uses_zxing_and_returns_square_bitmap(self):
        gradle = (APP / "build.gradle").read_text(encoding="utf-8")
        self.assertIn("com.google.zxing:core:3.5.3", gradle)

        source = self.read_main("java/com/boop/shieldoverlay/QrCodeBitmap.java")
        self.assertIn("BarcodeFormat.QR_CODE", source)
        self.assertIn("MultiFormatWriter", source)
        self.assertIn("Bitmap.createBitmap", source)
        self.assertIn("size, size", source)

    def test_launcher_starts_existing_overlay_then_opens_home_pairing_gate(self):
        launcher = self.read_main("java/com/boop/shieldoverlay/MainActivity.java")
        home = self.read_main("java/com/boop/shieldoverlay/BoopHomeActivity.java")

        self.assertIn(
            "startForegroundService(new Intent(this, BoopOverlayService.class));",
            launcher,
        )
        self.assertIn("startActivity(new Intent(this, BoopHomeActivity.class))", launcher)
        self.assertNotIn("PairingGateController", launcher)

        self.assertIn("PairingGateController", home)
        self.assertIn("showPairingGate", home)
        self.assertIn("State.QR_READY", home)
        self.assertIn("State.STALE", home)
        self.assertIn("State.FAILED", home)

    def test_pairing_gate_has_big_plain_english_states_and_remote_retry(self):
        strings = self.read_main("res/values/strings.xml")
        for text in (
            "I found your house",
            "Scan this with BOOP Wall.",
            "Found it.",
            "Try again",
        ):
            self.assertIn(text, strings)

        home = self.read_main("java/com/boop/shieldoverlay/BoopHomeActivity.java")
        pairing_view = self.read_main("java/com/boop/shieldoverlay/TvPairingView.java")
        combined = home + "\n" + pairing_view
        self.assertIn("setOnClickListener", combined)
        self.assertIn("requestFocus", combined)
        self.assertIn("postDelayed", home)


if __name__ == "__main__":
    unittest.main()
