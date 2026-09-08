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

    def test_indicator_uses_display_bound_window_context_on_android_11_plus(self):
        text = (SOURCE / "cleanstart/CleanStartIndicator.kt").read_text()
        self.assertIn("DisplayManager", text)
        self.assertIn("Display.DEFAULT_DISPLAY", text)
        self.assertIn("createDisplayContext", text)
        self.assertIn(
            "createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null)",
            text,
        )
        self.assertIn("LinearLayout(windowContext)", text)
        self.assertIn("windowContext.getSystemService", text)

    def test_indicator_uses_brightness_style_full_screen_host_surface(self):
        text = (SOURCE / "cleanstart/CleanStartIndicator.kt").read_text()
        self.assertIn("FrameLayout", text)
        self.assertIn("val host = FrameLayout(windowContext)", text)
        self.assertIn("host.addView(card", text)
        self.assertGreaterEqual(text.count("WindowManager.LayoutParams.MATCH_PARENT"), 2)
        self.assertIn("FLAG_LAYOUT_NO_LIMITS", text)
        self.assertIn("armPresentationSignal(host)", text)

    def test_boot_job_shows_then_always_hides_indicator(self):
        text = (SOURCE / "cleanstart/CleanStartJobService.kt").read_text()
        self.assertIn("CleanStartIndicator(applicationContext)", text)
        self.assertIn("indicator.show()", text)
        self.assertIn("indicator.hide()", text)
        self.assertLess(text.index("indicator.show()"), text.index("executor.submit"))
        self.assertIn("finally", text)
        self.assertLess(text.index("indicator.hide()"), text.index("jobFinished"))

    def test_boot_job_waits_for_committed_indicator_frame_before_cleanup(self):
        indicator = (SOURCE / "cleanstart/CleanStartIndicator.kt").read_text()
        job = (SOURCE / "cleanstart/CleanStartJobService.kt").read_text()
        self.assertIn("CountDownLatch", indicator)
        self.assertIn("registerFrameCommitCallback", indicator)
        self.assertIn("fun awaitPresented", indicator)
        self.assertIn("TimeUnit.MILLISECONDS", indicator)
        self.assertIn("INDICATOR_PRESENT_TIMEOUT_MS", job)
        self.assertIn("indicator.awaitPresented(INDICATOR_PRESENT_TIMEOUT_MS)", job)
        self.assertNotIn("Thread.sleep(INDICATOR_PREROLL_MS)", job)
        show_at = job.index("indicator.show()")
        await_at = job.index("indicator.awaitPresented(INDICATOR_PRESENT_TIMEOUT_MS)")
        bridge_at = job.index("LocalBridge(applicationContext)")
        self.assertLess(show_at, await_at)
        self.assertLess(await_at, bridge_at)

    def test_android_10_plus_arms_commit_only_after_window_attachment(self):
        text = (SOURCE / "cleanstart/CleanStartIndicator.kt").read_text()
        self.assertIn("FLAG_HARDWARE_ACCELERATED", text)
        self.assertIn("addOnAttachStateChangeListener", text)
        self.assertIn("onViewAttachedToWindow", text)
        self.assertIn("armAttachedPresentationSignal", text)
        self.assertIn("if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)", text)
        self.assertIn("if (!card.isHardwareAccelerated)", text)
        self.assertIn("registerFrameCommitCallback", text)
        self.assertNotIn(
            "Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && card.isHardwareAccelerated",
            text,
        )

    def test_failed_indicator_presentation_fails_open_within_half_second(self):
        text = (SOURCE / "cleanstart/CleanStartJobService.kt").read_text()
        self.assertIn("INDICATOR_PRESENT_TIMEOUT_MS = 500L", text)
        self.assertIn("val presented = indicator.awaitPresented(INDICATOR_PRESENT_TIMEOUT_MS)", text)
        self.assertIn("if (!presented) indicator.hide()", text)

    def test_boot_indicator_records_local_diagnostics_for_physical_followup(self):
        indicator = (SOURCE / "cleanstart/CleanStartIndicator.kt").read_text()
        store = (SOURCE / "cleanstart/CleanStartStore.kt").read_text()
        job = (SOURCE / "cleanstart/CleanStartJobService.kt").read_text()
        startup = (SOURCE / "startup/StartupManagerActivity.kt").read_text()
        self.assertIn("fun diagnostic(presented: Boolean): CleanStartIndicatorDiagnostic", indicator)
        self.assertIn("data class CleanStartIndicatorDiagnostic", store)
        self.assertIn("fun recordIndicatorDiagnostic", store)
        self.assertIn("fun lastIndicatorDiagnostic", store)
        self.assertIn("store.recordIndicatorDiagnostic(indicator.diagnostic(presented))", job)
        self.assertIn("cleanStore.lastIndicatorDiagnostic()", startup)
        self.assertIn("STARTUP NOTICE DIAGNOSTIC:", startup)

    def test_failed_boot_clean_start_surfaces_the_actual_adb_reason(self):
        job = (SOURCE / "cleanstart/CleanStartJobService.kt").read_text()
        startup = (SOURCE / "startup/StartupManagerActivity.kt").read_text()
        self.assertIn("failure.javaClass.simpleName", job)
        self.assertIn("failure.message", job)
        self.assertIn("LAST CLEAN START DETAIL:", startup)
        self.assertIn("item.detail", startup)

    def test_indicator_does_not_change_clean_start_scheduler(self):
        text = (SOURCE / "cleanstart/CleanStartScheduler.kt").read_text()
        self.assertIn("30_000L", text)
        self.assertIn("60_000L", text)
        self.assertIn("120_000L", text)
        self.assertIn("MAX_ATTEMPTS = 3", text)
        self.assertNotIn("CleanStartIndicator", text)


if __name__ == "__main__":
    unittest.main(verbosity=2)
