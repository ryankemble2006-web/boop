"""Source-level contract for the read-only TURBO+ HEADROOM photo test."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/src/main/java/com/boop/shieldturbo"
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"


class HeadroomContractTest(unittest.TestCase):
    def test_probe_exists_and_is_strictly_read_only(self):
        path = SOURCE / "performance/HeadroomProbe.kt"
        self.assertTrue(path.exists(), "missing read-only HeadroomProbe")
        text = path.read_text()
        self.assertIn("withTrustedAdb", text)
        for evidence in (
            "/sys/devices/system/cpu/online",
            "scaling_cur_freq",
            "scaling_max_freq",
            "scaling_governor",
            "gpu",
            "emc",
            "thermal",
            "cooling_device",
            "nv_power_mode",
        ):
            self.assertIn(evidence, text.lower())
        for forbidden in (
            "settings put",
            "setprop ",
            "chmod ",
            "su ",
            "echo 0 >",
            "echo 1 >",
        ):
            self.assertNotIn(forbidden, text.lower())

    def test_report_is_full_screen_large_and_photo_friendly(self):
        path = SOURCE / "performance/HeadroomActivity.kt"
        self.assertTrue(path.exists(), "missing full-screen HeadroomActivity")
        text = path.read_text()
        self.assertIn("FLAG_FULLSCREEN", text)
        self.assertIn("Color.BLACK", text)
        self.assertIn("textSize = 32f", text)
        self.assertIn("TURBO+ • RESULT", text)
        self.assertIn("PHOTOGRAPH THIS • BACK TO CLOSE", text)
        self.assertIn("ADB NOT READY", text)
        self.assertIn("CPU", text)
        self.assertIn("GPU", text)
        self.assertIn("MEMORY", text)
        self.assertIn("COOLING", text)
        self.assertIn("EXTRA STOCK CONTROLS", text)

    def test_turbo_panel_has_one_big_headroom_button(self):
        text = (SOURCE / "performance/TurboModePanel.kt").read_text()
        self.assertIn("TURBO+ HEADROOM TEST", text)
        self.assertIn("HeadroomActivity", text)
        self.assertIn("minHeight = dp(74)", text)

    def test_headroom_activity_is_private(self):
        text = MANIFEST.read_text()
        self.assertIn('.performance.HeadroomActivity', text)
        marker = '<activity android:name=".performance.HeadroomActivity"'
        self.assertIn(marker, text)
        tail = text.split(marker, 1)[1].split('/>', 1)[0]
        self.assertIn('android:exported="false"', tail)


if __name__ == "__main__":
    unittest.main(verbosity=2)
