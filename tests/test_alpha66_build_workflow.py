import unittest
from pathlib import Path


class Alpha66BuildWorkflowTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.path = Path('.github/workflows/build-alpha66.yml')
        cls.workflow = cls.path.read_text(encoding='utf-8') if cls.path.exists() else ''

    def test_workflow_targets_alpha66_and_freezes_alpha65_core(self):
        self.assertIn('alpha6.6-timed-routines', self.workflow)
        self.assertIn('0ed27456afb8f7cb94529341e70501d8d53ee0bb HEAD', self.workflow)
        for seam in (
            'source/MainActivity.java',
            'source/HomeAssistantClient.java',
            'source/BoopTimedRoutineFlow.java',
            'source/app-build.gradle',
        ):
            self.assertIn("':(exclude)" + seam + "'", self.workflow)

    def test_workflow_runs_tests_signs_and_verifies_code20(self):
        self.assertIn("python3 -m unittest discover -s tests -p 'test_*.py' -v", self.workflow)
        self.assertIn(':app:testDebugUnitTest', self.workflow)
        self.assertIn('BOOP_KEYSTORE_BASE64', self.workflow)
        self.assertIn('versionCode=\'20\'', self.workflow)
        self.assertIn("versionName='0.4.7-alpha6.6'", self.workflow)
        self.assertIn('apksigner verify --verbose', self.workflow)
        self.assertIn('unzip -tq', self.workflow)

    def test_workflow_boots_pixel_with_wake_mic_and_publishes_apk(self):
        self.assertIn('pixel_7_pro', self.workflow)
        self.assertIn('android.permission.RECORD_AUDIO', self.workflow)
        self.assertIn('pidof com.boop.alpha1', self.workflow)
        self.assertIn('BOOP-Alpha6.6-Timed-One-Shot-Routines-debug', self.workflow)
        self.assertIn('actions/upload-artifact@v4', self.workflow)


if __name__ == '__main__':
    unittest.main()
