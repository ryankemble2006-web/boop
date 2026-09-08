"""Nonvisual lifecycle/safety contract for the static CLEAN START boot indicator."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/src/main/java/com/boop/shieldturbo"


class CleanStartIndicatorContractTest(unittest.TestCase):
    def test_indicator_is_static_noninteractive_overlay(self):
        path = SOURCE / "cleanstart/CleanStartIndicator.kt"
        self.assertTrue(path.exists(), "missing CLEAN START indicator helper")
        text = path.read_text()
        self.assertIn("TYPE_APPLICATION_OVERLAY", text)
        self.assertIn("FLAG_NOT_FOCUSABLE", text)
        self.assertIn("FLAG_NOT_TOUCHABLE", text)
        self.assertIn("Settings.canDrawOverlays", text)
        self.assertIn("SHIELD TURBO · CLEAN START", text)
        self.assertIn("Tidying startup apps", text)
        for forbidden in (
            ".animate()",
            "ObjectAnimator",
            "ValueAnimator",
            "AnimationUtils",
            "setWindowAnimations",
            "updateViewLayout",
            "postDelayed",
        ):
            self.assertNotIn(forbidden, text)

    def test_boot_job_shows_then_always_hides_indicator(self):
        text = (SOURCE / "cleanstart/CleanStartJobService.kt").read_text()
        self.assertIn("CleanStartIndicator(applicationContext)", text)
        self.assertIn("indicator.show()", text)
        self.assertIn("indicator.hide()", text)
        self.assertLess(text.index("indicator.show()"), text.index("executor.submit"))
        self.assertIn("finally", text)
        self.assertLess(text.index("indicator.hide()"), text.index("jobFinished"))

    def test_boot_job_gives_static_indicator_one_frame_preroll_before_cleanup(self):
        text = (SOURCE / "cleanstart/CleanStartJobService.kt").read_text()
        self.assertIn("INDICATOR_PREROLL_MS = 500L", text)
        self.assertIn("Thread.sleep(INDICATOR_PREROLL_MS)", text)
        show_at = text.index("indicator.show()")
        sleep_at = text.index("Thread.sleep(INDICATOR_PREROLL_MS)")
        bridge_at = text.index("LocalBridge(applicationContext)")
        self.assertLess(show_at, sleep_at)
        self.assertLess(sleep_at, bridge_at)

    def test_indicator_does_not_change_clean_start_scheduler(self):
        text = (SOURCE / "cleanstart/CleanStartScheduler.kt").read_text()
        self.assertIn("30_000L", text)
        self.assertIn("60_000L", text)
        self.assertIn("120_000L", text)
        self.assertIn("MAX_ATTEMPTS = 3", text)
        self.assertNotIn("CleanStartIndicator", text)


if __name__ == "__main__":
    unittest.main(verbosity=2)
