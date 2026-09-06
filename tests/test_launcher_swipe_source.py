import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


class LauncherSwipeSourceTest(unittest.TestCase):
    def test_wall_uses_a_deliberate_left_swipe_to_open_the_separate_launcher(self):
        main = (ROOT / "source" / "MainActivity.java").read_text(encoding="utf-8")
        gesture_path = ROOT / "source" / "BoopLauncherSwipeGesture.java"
        manifest = (ROOT / "source" / "AndroidManifest.xml").read_text(encoding="utf-8")

        self.assertTrue(gesture_path.exists())
        gesture = gesture_path.read_text(encoding="utf-8")
        self.assertIn("BoopLauncherSwipeGesture", main)
        self.assertIn("isDeliberateLeftSwipe", main)
        self.assertIn('BOOP_LAUNCHER_PACKAGE = "com.boop.launcher"', main)
        self.assertIn('getLaunchIntentForPackage(BOOP_LAUNCHER_PACKAGE)', main)
        self.assertIn("BOOP Launcher is not installed.", main)
        self.assertIn('android:name="com.boop.launcher"', manifest)
        self.assertIn("MIN_HORIZONTAL_DISTANCE_DP", gesture)
        self.assertIn("DIRECTIONAL_CONFIDENCE", gesture)


if __name__ == "__main__":
    unittest.main()
