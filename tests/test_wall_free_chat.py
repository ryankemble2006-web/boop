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
            # Only constructor type adapters are stubbed. The harness exercises the
            # real router and real local/assistant function interfaces below.
            (tmp / 'HomeAssistantClient.java').write_text(
                'package com.boop.alpha1; class HomeAssistantClient { '
                'CommandOutcome process(String s) { throw new AssertionError("unused adapter"); } }')
            (tmp / 'HomeAssistantGeneralAssistantClient.java').write_text(
                'package com.boop.alpha1; class HomeAssistantGeneralAssistantClient { '
                'CommandOutcome ask(String s) { throw new AssertionError("unused adapter"); } }')
            args = ['javac', '--release', '17', '-d', str(tmp)]
            args += [str(ROOT / 'source' / n) for n in names]
            args += [str(tmp / 'HomeAssistantClient.java'), str(tmp / 'HomeAssistantGeneralAssistantClient.java'),
                     str(ROOT / 'tests/java/BoopChatModeHarness.java')]
            compiled = subprocess.run(args, text=True, capture_output=True)
            self.assertEqual(0, compiled.returncode, compiled.stderr)
            ran = subprocess.run(['java', '-cp', str(tmp), 'com.boop.alpha1.BoopChatModeHarness'],
                                 text=True, capture_output=True)
            self.assertEqual(0, ran.returncode, ran.stdout + ran.stderr)
            self.assertIn('PASS', ran.stdout)


if __name__ == '__main__':
    unittest.main()
