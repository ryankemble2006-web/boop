import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
PROJECT = ROOT / "shield-hdr-debug"
JAVA = PROJECT / "app/src/main/java/com/boop/shieldhdrdebug"


class ShieldHdrDebugSourceTest(unittest.TestCase):
    def read(self, relative):
        path = ROOT / relative
        self.assertTrue(path.exists(), f"missing {relative}")
        return path.read_text(encoding="utf-8")

    def test_debug_build_is_a_separate_package(self):
        gradle = self.read("shield-hdr-debug/app/build.gradle")
        manifest = self.read("shield-hdr-debug/app/src/main/AndroidManifest.xml")
        strings = self.read("shield-hdr-debug/app/src/main/res/values/strings.xml")
        self.assertIn("applicationId 'com.boop.shieldhdrdebug'", gradle)
        self.assertIn("BOOP HDR Debug", strings)
        self.assertNotIn("com.boop.shieldoverlay", gradle)
        self.assertIn("FOREGROUND_SERVICE_SPECIAL_USE", manifest)

    def test_debug_manifest_keeps_original_isolation(self):
        manifest = self.read("shield-hdr-debug/app/src/main/AndroidManifest.xml")
        for forbidden in (
            "RECORD_AUDIO", "INTERNET", "ACCESS_NETWORK_STATE",
            "RECEIVE_BOOT_COMPLETED", "BIND_ACCESSIBILITY_SERVICE",
        ):
            self.assertNotIn(forbidden, manifest)
        for required in (
            "SYSTEM_ALERT_WINDOW", "FOREGROUND_SERVICE",
            "FOREGROUND_SERVICE_SPECIAL_USE",
        ):
            self.assertIn(required, manifest)

    def test_journal_persists_session_heartbeat_and_rolling_events(self):
        journal = self.read("shield-hdr-debug/app/src/main/java/com/boop/shieldhdrdebug/DiagnosticJournal.java")
        self.assertIn("SharedPreferences", journal)
        self.assertIn("session_counter", journal)
        self.assertIn("last_heartbeat_wall", journal)
        self.assertIn("last_heartbeat_elapsed", journal)
        self.assertIn("previous_session", journal)
        self.assertIn("MAX_EVENTS", journal)
        self.assertIn("appendEvent", journal)

    def test_service_reports_lifecycle_display_and_overlay_attachment(self):
        service = self.read("shield-hdr-debug/app/src/main/java/com/boop/shieldhdrdebug/BoopHdrDebugService.java")
        for required in (
            "DisplayManager.DisplayListener", "onDisplayChanged",
            "onConfigurationChanged", "onTrimMemory", "onTaskRemoved",
            "onDestroy", "isAttachedToWindow", "OnAttachStateChangeListener",
            "FOREGROUND_SERVICE_TYPE_SPECIAL_USE", "START_STICKY",
            "heartbeat", "DisplaySnapshot",
        ):
            self.assertIn(required, service)

    def test_overlay_geometry_uses_current_window_metrics_not_physical_output_mode(self):
        service = self.read("shield-hdr-debug/app/src/main/java/com/boop/shieldhdrdebug/BoopHdrDebugService.java")
        self.assertIn("getCurrentWindowMetrics", service)
        self.assertNotIn("getMode().getPhysicalWidth()", service)
        self.assertNotIn("getMode().getPhysicalHeight()", service)

    def test_panel_is_photo_friendly_and_contains_previous_session(self):
        panel = self.read("shield-hdr-debug/app/src/main/java/com/boop/shieldhdrdebug/DiagnosticPanelView.java")
        for required in (
            "BOOP HDR DEBUG", "PREVIOUS SESSION", "CURRENT SESSION",
            "DISPLAY", "OVERLAY", "EVENT JOURNAL", "drawText",
        ):
            self.assertIn(required, panel)

    def test_java_sources_have_no_voice_network_or_ha_code(self):
        self.assertTrue(JAVA.exists(), "missing debug Java source directory")
        joined = "\n".join(p.read_text(encoding="utf-8") for p in JAVA.glob("*.java"))
        for forbidden in (
            "android.speech", "SpeechRecognizer", "RECORD_AUDIO",
            "HttpURLConnection", "java.net.", "homeassistant", "Home Assistant",
        ):
            self.assertNotIn(forbidden, joined)


if __name__ == "__main__":
    unittest.main()
