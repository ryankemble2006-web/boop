"""Nonvisual contract for silent CLEAN START startup and its one-time warning."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/src/main/java/com/boop/shieldturbo"
STRINGS = ROOT / "app/src/main/res/values/strings.xml"


class SilentCleanStartContractTest(unittest.TestCase):
    def test_boot_cleanup_has_no_visual_indicator_attempt(self):
        indicator = SOURCE / "cleanstart/CleanStartIndicator.kt"
        self.assertFalse(indicator.exists(), "CLEAN START must not create a boot overlay")

        job = (SOURCE / "cleanstart/CleanStartJobService.kt").read_text()
        for forbidden in (
            "CleanStartIndicator",
            "indicator.show()",
            "indicator.hide()",
            "awaitPresented",
            "INDICATOR_PRESENT_TIMEOUT_MS",
            "recordIndicatorDiagnostic",
        ):
            self.assertNotIn(forbidden, job)

    def test_first_install_warning_explains_silent_startup_and_brief_pause(self):
        application_path = SOURCE / "TurboApplication.kt"
        self.assertTrue(application_path.exists(), "missing first-run startup note host")
        application = application_path.read_text()
        strings = STRINGS.read_text().lower()
        manifest = (ROOT / "app/src/main/AndroidManifest.xml").read_text()
        self.assertIn('android:name=".TurboApplication"', manifest)
        self.assertIn("Application.ActivityLifecycleCallbacks", application)
        self.assertIn("activity !is MainActivity", application)
        self.assertIn("first_install_startup_note_shown", application)
        self.assertIn("first_install_startup_title", application)
        self.assertIn("first_install_startup_message", application)
        self.assertIn("starts silently", strings)
        self.assertIn("brief", strings)
        self.assertIn("startup", strings)

    def test_old_boot_presentation_evidence_is_retired_on_app_start(self):
        application = (SOURCE / "TurboApplication.kt").read_text()
        self.assertIn('remove("last_indicator_diagnostic")', application)
        self.assertNotIn("recordIndicatorDiagnostic", (SOURCE / "cleanstart/CleanStartJobService.kt").read_text())

    def test_silent_cleanup_keeps_existing_scheduler_and_timing_evidence(self):
        scheduler = (SOURCE / "cleanstart/CleanStartScheduler.kt").read_text()
        job = (SOURCE / "cleanstart/CleanStartJobService.kt").read_text()
        self.assertIn("30_000L", scheduler)
        self.assertIn("60_000L", scheduler)
        self.assertIn("120_000L", scheduler)
        self.assertIn("MAX_ATTEMPTS = 3", scheduler)
        self.assertIn("store.recordTimingDiagnostic", job)
        self.assertIn("noticeMs = 0L", job)
        self.assertIn("LocalBridge(applicationContext)", job)

    def test_failed_boot_clean_start_still_surfaces_actual_adb_reason(self):
        job = (SOURCE / "cleanstart/CleanStartJobService.kt").read_text()
        startup = (SOURCE / "startup/StartupManagerActivity.kt").read_text()
        self.assertIn("failure.javaClass.simpleName", job)
        self.assertIn("failure.message", job)
        self.assertIn("LAST CLEAN START DETAIL:", startup)
        self.assertIn("item.detail", startup)


if __name__ == "__main__":
    unittest.main(verbosity=2)
