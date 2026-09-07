"""Behavioral JVM checks for the explicit chat fallback, with no Android runtime."""
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]


class WallFreeChatBehaviorTests(unittest.TestCase):
    def test_hold_and_local_first_routing(self):
        names = ['BoopChatMode.java', 'BoopChatModeHold.java', 'BoopCommandRouter.java', 'CommandOutcome.java']
        for name in names:
            self.assertTrue((ROOT / 'source' / name).is_file(), f'{name}: chat fallback is not implemented')
        with tempfile.TemporaryDirectory() as tmp:
            tmp = Path(tmp)
            # Constructor adapter stubs live in the harness so this invocation
            # stays identical to the standalone workflow harness compilation.
            args = ['javac', '--release', '17', '-d', str(tmp)]
            args += [str(ROOT / 'source' / n) for n in names]
            args += [str(ROOT / 'tests/java/BoopChatModeHarness.java')]
            compiled = subprocess.run(args, text=True, capture_output=True)
            self.assertEqual(0, compiled.returncode, compiled.stderr)
            ran = subprocess.run(['java', '-cp', str(tmp), 'com.boop.alpha1.BoopChatModeHarness'],
                                 text=True, capture_output=True)
            self.assertEqual(0, ran.returncode, ran.stdout + ran.stderr)
            self.assertIn('PASS', ran.stdout)


if __name__ == '__main__':
    unittest.main()
