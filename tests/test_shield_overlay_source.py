from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "shield-overlay" / "app" / "src" / "main"


class ShieldOverlaySourceTest(unittest.TestCase):
    def read(self, relative):
        path = APP / relative
        self.assertTrue(path.exists(), f"missing required file: {path.relative_to(ROOT)}")
        return path.read_text(encoding="utf-8")

    def test_overlay_window_is_noninteractive_and_application_overlay(self):
        source = self.read("java/com/boop/shieldoverlay/OverlayWindowSpec.java")
        self.assertIn("TYPE_APPLICATION_OVERLAY", source)
        self.assertIn("FLAG_NOT_FOCUSABLE", source)
        self.assertIn("FLAG_NOT_TOUCHABLE", source)

    def test_eye_view_is_transparent_and_never_paints_black(self):
        source = self.read("java/com/boop/shieldoverlay/BoopOverlayView.java")
        self.assertIn("Color.TRANSPARENT", source)
        self.assertNotIn("Color.BLACK", source)
        self.assertNotIn("drawColor", source)

    def test_manifest_isolated_from_voice_network_boot_and_accessibility(self):
        manifest = self.read("AndroidManifest.xml")
        self.assertIn("android.permission.SYSTEM_ALERT_WINDOW", manifest)
        self.assertIn("android.permission.FOREGROUND_SERVICE", manifest)
        self.assertIn("android.permission.FOREGROUND_SERVICE_SPECIAL_USE", manifest)
        self.assertIn("android:foregroundServiceType=\"specialUse\"", manifest)
        self.assertIn("persistent_noninteractive_visual_overlay_poc", manifest)
        for forbidden in (
            "RECORD_AUDIO",
            "INTERNET",
            "ACCESS_NETWORK_STATE",
            "RECEIVE_BOOT_COMPLETED",
            "BIND_ACCESSIBILITY_SERVICE",
            "SpeechRecognizer",
            "RecognizerIntent",
            "homeassistant",
        ):
            self.assertNotIn(forbidden, manifest)

    def test_service_promotes_with_declared_special_use_type(self):
        source = self.read("java/com/boop/shieldoverlay/BoopOverlayService.java")
        self.assertIn("FOREGROUND_SERVICE_TYPE_SPECIAL_USE", source)

    def test_display_mode_change_reasserts_existing_eye_surface(self):
        source = self.read("java/com/boop/shieldoverlay/BoopOverlayService.java")
        self.assertIn("DisplayManager.DisplayListener", source)
        self.assertIn("onDisplayChanged", source)
        self.assertIn("registerDisplayListener", source)
        self.assertIn("unregisterDisplayListener", source)
        self.assertIn("postInvalidateOnAnimation", source)
        self.assertNotIn("postDelayed", source)

    def test_java_only_module_disables_builtin_kotlin(self):
        gradle = (ROOT / "shield-overlay" / "app" / "build.gradle").read_text(encoding="utf-8")
        self.assertIn("enableKotlin = false", gradle)

    def test_protected_overlay_runtime_stays_free_of_voice_network_and_ha_code(self):
        protected_sources = (
            "java/com/boop/shieldoverlay/BoopOverlayService.java",
            "java/com/boop/shieldoverlay/BoopOverlayView.java",
            "java/com/boop/shieldoverlay/OverlayGeometry.java",
            "java/com/boop/shieldoverlay/OverlayWindowSpec.java",
        )
        combined = "\n".join(self.read(path) for path in protected_sources).lower()
        for forbidden in (
            "android.speech",
            "android.media.audiorecord",
            "java.net",
            "okhttp",
            "retrofit",
            "homeassistant",
        ):
            self.assertNotIn(forbidden, combined)


if __name__ == "__main__":
    unittest.main()
