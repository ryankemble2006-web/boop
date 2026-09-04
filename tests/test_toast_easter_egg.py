import unittest
from pathlib import Path


class ToastEasterEggSurfaceTest(unittest.TestCase):
    def test_materializer_installs_hidden_toast_hook(self):
        materializer = Path('scripts/materialize-android.sh').read_text(encoding='utf-8')
        self.assertIn('python3 scripts/patch-toast-easter-egg.py', materializer)

    def test_toast_hook_stays_local_and_animates_before_speaking(self):
        patch = Path('scripts/patch-toast-easter-egg.py').read_text(encoding='utf-8')
        self.assertIn('BoopToastEgg toastEgg', patch)
        self.assertIn('toastEgg.matches(transcript)', patch)
        self.assertIn('face.playMemberBerry(toastMoment.level())', patch)
        self.assertIn('speak(toastMoment.line())', patch)
        self.assertNotIn('commandRouter.process', patch)
        self.assertNotIn('HomeAssistant', patch)

    def test_toast_helper_exists_as_pure_escalating_state(self):
        helper = Path('source/BoopToastEgg.java')
        self.assertTrue(helper.exists())
        text = helper.read_text(encoding='utf-8') if helper.exists() else ''
        self.assertIn('CYCLE_LENGTH', text)
        self.assertIn('TOAST EMERGENCY', text)
        self.assertNotIn('android.', text)
        self.assertNotIn('HomeAssistant', text)


if __name__ == '__main__':
    unittest.main()
