from pathlib import Path
import sys
import unittest

root = Path(sys.argv.pop(1))
directory = root / 'shield-home-lib/src/main/java/com/boop/shieldhome'

class RecentsRoute(unittest.TestCase):
    def test_home_double_tap_cannot_bypass_preload(self):
        source = (directory / 'ShieldHomeOverrideService.java').read_text(encoding='utf-8')
        self.assertNotIn('performGlobalAction(GLOBAL_ACTION_RECENTS)', source)
        self.assertIn('BoopKeyActionExecutor.execute(this, BoopKeyMappingStore.ACTION_RECENTS, null);', source)

    def test_mapping_uses_native_preload(self):
        source = (directory / 'BoopKeyActionExecutor.java').read_text(encoding='utf-8')
        case = source.split('case BoopKeyMappingStore.ACTION_RECENTS:', 1)[1].split('case BoopKeyMappingStore.ACTION_CUSTOM_APP:', 1)[0]
        self.assertIn('RecentsPreload.open(context);', case)
        self.assertNotIn('performGlobalAction(', case)

    def test_helper_has_no_display_or_animation_mutations(self):
        file = directory / 'RecentsPreload.java'
        self.assertTrue(file.is_file(), 'Native preload helper is required')
        text = file.read_text(encoding='utf-8')
        for forbidden in ('settings put', 'wm size', 'wm density', 'animation_scale', 'font_scale', 'setHwcProperty'):
            self.assertNotIn(forbidden, text)

if __name__ == '__main__':
    unittest.main()
