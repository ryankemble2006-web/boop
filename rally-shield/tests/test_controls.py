import pathlib
import shutil
import subprocess
import tempfile
import unittest
ROOT = pathlib.Path(__file__).resolve().parents[1]
class ControlsTests(unittest.TestCase):
    def test_java_behavior(self):
        source = ROOT / 'java/com/boop/rally/Controls.java'
        self.assertTrue(source.exists(), 'Controller mapping has not been implemented')
        self.assertIsNotNone(shutil.which('javac'), 'JDK is required for behavior tests')
        with tempfile.TemporaryDirectory() as tmp:
            subprocess.run(['javac', '-d', tmp, str(source), str(ROOT/'tests/ControlsHarness.java')], check=True)
            result = subprocess.run(['java', '-cp', tmp, 'com.boop.rally.ControlsHarness'], check=True, text=True, capture_output=True)
            self.assertIn('behavioral checks passed', result.stdout)
            print(result.stdout.strip())
