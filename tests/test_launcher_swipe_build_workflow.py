from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]


class LauncherSwipeBuildWorkflowTest(unittest.TestCase):
    def test_candidate_build_is_versioned_signed_and_branch_scoped(self):
        gradle = (ROOT / "source" / "app-build.gradle").read_text(encoding="utf-8")
        workflow_path = ROOT / ".github" / "workflows" / "build-boop-wall-launcher-swipe.yml"
        self.assertTrue(workflow_path.exists())
        workflow = workflow_path.read_text(encoding="utf-8")

        self.assertIn("versionCode 29", gradle)
        self.assertIn('versionName "0.4.9-alpha6.5.6-wall"', gradle)
        self.assertIn('branches: [ "boop-wall-launcher-swipe" ]', workflow)
        self.assertIn("BOOP_DEV_KEYSTORE_B64", workflow)
        self.assertIn("BOOP_SIGNING_STORE_FILE", workflow)
        self.assertIn("versionCode 29/versionCode 30", workflow)
        self.assertIn('0.4.9-alpha6.5.6-wall/0.4.10-wall-launcher-swipe', workflow)
        self.assertIn("versionCode='30'", workflow)
        self.assertIn("BOOP-Wall-Launcher-Swipe-debug", workflow)
        self.assertIn("adb install -r", workflow)
        self.assertIn('KERNEL=="kvm"', workflow)


if __name__ == "__main__":
    unittest.main()
