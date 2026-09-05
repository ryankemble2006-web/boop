import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
JAVA = ROOT / "shield-overlay" / "app" / "src" / "main" / "java" / "com" / "boop" / "shieldoverlay"
MANIFEST = ROOT / "shield-overlay" / "app" / "src" / "main" / "AndroidManifest.xml"


class ShieldLaunchCrashDiagnosticsSourceTest(unittest.TestCase):
    def test_application_installs_crash_recorder_before_activity_launch(self):
        manifest = MANIFEST.read_text(encoding="utf-8")
        app = (JAVA / "BoopApplication.java").read_text(encoding="utf-8")

        self.assertIn('android:name=".BoopApplication"', manifest)
        self.assertIn("new LaunchCrashRecorder(this)", app)
        self.assertIn("installAsDefaultHandler()", app)

    def test_next_launcher_tap_routes_saved_crash_to_tv_report(self):
        manifest = MANIFEST.read_text(encoding="utf-8")
        launcher = (JAVA / "MainActivity.java").read_text(encoding="utf-8")
        report = (JAVA / "CrashReportActivity.java").read_text(encoding="utf-8")

        self.assertIn('android:name=".CrashReportActivity"', manifest)
        self.assertIn("crashRecorder.consume()", launcher)
        self.assertIn("CrashReportActivity.class", launcher)
        self.assertLess(
            launcher.index("crashRecorder.consume()"),
            launcher.index("startOverlayAndOpenHome()"),
        )
        self.assertIn("Try BOOP again", report)
        self.assertIn("ScrollView", report)


if __name__ == "__main__":
    unittest.main()
