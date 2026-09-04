import unittest
from pathlib import Path


class Alpha64BuildWorkflowTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        path = Path('.github/workflows/build-alpha64.yml')
        cls.workflow = path.read_text(encoding='utf-8') if path.exists() else ''
        verify_path = Path('.github/workflows/verify-alpha6.yml')
        cls.generic = verify_path.read_text(encoding='utf-8') if verify_path.exists() else ''

    def test_dedicated_workflow_targets_alpha64_and_freezes_code16_core(self):
        self.assertIn('alpha6.4-wake-word', self.workflow)
        self.assertIn('3b19abfa9bfce66d0a1ddbcf97f917a765a2759d', self.workflow)
        self.assertIn('git diff --exit-code', self.workflow)
        for allowed in (
            'source/MainActivity.java',
            'source/BoopWakeTranscriptNormalizer.java',
            'source/BoopWakeTriggerGate.java',
            'source/BoopWakeSessionState.java',
            'source/BoopPcmRingBuffer.java',
            'source/BoopWakeRecognitionIntent.java',
            'source/BoopWakeAudioSession.java',
            'source/BoopSherpaWakeSpotter.java',
            'source/BoopWakeWordController.java',
            'source/BoopWakeSessionCoordinator.java',
            'source/app-build.gradle',
        ):
            self.assertIn("':(exclude)" + allowed + "'", self.workflow)

    def test_workflow_verifies_real_wake_assets_and_full_test_stack(self):
        self.assertIn('bash scripts/materialize-android.sh', self.workflow)
        self.assertIn('sherpa-onnx-1.13.7.aar', self.workflow)
        self.assertIn('encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx', self.workflow)
        self.assertIn('decoder-epoch-12-avg-2-chunk-16-left-64.onnx', self.workflow)
        self.assertIn('joiner-epoch-12-avg-2-chunk-16-left-64.int8.onnx', self.workflow)
        self.assertIn('assets/boop-kws/keywords.txt', self.workflow)
        self.assertIn("grep -Fxq '▁BO O P :1.5 #0.25 @BOOP'", self.workflow)
        self.assertIn('python3 -m unittest bridge/test_boop_wyoming_bridge.py -v', self.workflow)
        self.assertIn("python3 -m unittest discover -s tests -p 'test_*.py' -v", self.workflow)
        self.assertIn(':app:testDebugUnitTest', self.workflow)

    def test_workflow_builds_signs_inspects_and_emulator_launches_code17(self):
        self.assertIn("java-version: '17'", self.workflow)
        self.assertIn('platforms;android-36', self.workflow)
        self.assertIn("gradle-version: '9.6.0'", self.workflow)
        self.assertIn(':app:assembleDebug', self.workflow)
        self.assertIn("versionCode='17'", self.workflow)
        self.assertIn("versionName='0.4.4-alpha6.4'", self.workflow)
        self.assertIn('apksigner verify --verbose', self.workflow)
        self.assertIn('unzip -tq', self.workflow)
        self.assertIn('--device "pixel_7_pro"', self.workflow)
        self.assertIn('system-images;android-36;google_apis;x86_64', self.workflow)
        self.assertIn('adb install -r', self.workflow)
        self.assertIn('adb shell pidof com.boop.alpha1', self.workflow)

    def test_workflow_publishes_expected_raw_apk_artifact(self):
        self.assertIn('actions/upload-artifact@v4', self.workflow)
        self.assertIn('BOOP-Alpha6.4-Wake-Word-debug', self.workflow)
        self.assertIn('app-debug.apk', self.workflow)

    def test_generic_alpha6_verifier_keeps_alpha64_branch(self):
        self.assertIn('"alpha6.4-wake-word"', self.generic)
        self.assertIn('"alpha6.3-voice-sliders"', self.generic)
        self.assertIn('"alpha6.2-muppet-voice"', self.generic)


if __name__ == '__main__':
    unittest.main()
