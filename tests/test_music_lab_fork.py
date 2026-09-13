"""Non-visual fork identity and non-interference checks, run on GitHub only."""
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

BASE = Path('boop-build/BOOP-Alpha1')
LAB = Path('boop-music-build/BOOP-Music-Lab')
ANDROID = '{http://schemas.android.com/apk/res/android}'

class MusicLabForkTest(unittest.TestCase):
    def test_has_independent_application_and_namespace(self):
        self.assertTrue((LAB/'app/build.gradle').is_file(), 'Separate Music Lab application has not been materialized')
        text = (LAB/'app/build.gradle').read_text()
        self.assertRegex(text, r"applicationId\s+['\"]com\.boop\.musiclab['\"]")
        self.assertRegex(text, r"namespace\s+['\"]com\.boop\.musiclab['\"]")
        self.assertRegex(text, r'versionCode\s+1\b')
        self.assertIn('0.1.1-v161-audio-prompt', text)

    def test_parent_is_not_rewritten(self):
        text = (BASE/'app/build.gradle').read_text()
        self.assertIn("applicationId 'com.boop.alpha1'", text)
        self.assertIn('versionCode 161', text)
        self.assertIn('1.2.161-lab-scale-independent', text)

    def test_no_home_assistant_or_boot_registration_and_unique_identity(self):
        paths = list(LAB.glob('*/src/main/AndroidManifest.xml'))
        self.assertGreaterEqual(len(paths), 5, 'All fork modules must exist')
        for path in paths:
            root = ET.parse(path).getroot()
            self.assertNotIn(ANDROID+'sharedUserId', root.attrib)
            for element in root.iter():
                name = element.get(ANDROID+'name', '')
                self.assertNotIn(name, ('android.intent.category.HOME', 'android.intent.action.ASSIST', 'android.intent.action.BOOT_COMPLETED', 'android.permission.RECEIVE_BOOT_COMPLETED'))
                self.assertNotEqual('boop', element.get(ANDROID+'scheme'))
                affinity = element.get(ANDROID+'taskAffinity')
                if affinity: self.assertTrue(affinity.startswith('com.boop.musiclab'))
                authority = element.get(ANDROID+'authorities')
                if authority: self.assertTrue(authority.startswith(('com.boop.musiclab', '${applicationId}')))
        app = ET.parse(LAB/'app/src/main/AndroidManifest.xml').getroot().find('application')
        self.assertEqual('BOOP Music Lab', app.get(ANDROID+'label'))

    def test_permission_entry_and_artwork_survive(self):
        relative = 'shield-home-lib/src/main/java/com/boop/shieldhome/MusicAudioPermissionActivity.java'
        self.assertTrue((LAB/relative).is_file(), 'Conditional permission entry is absent')
        self.assertEqual((BASE/relative).read_bytes(), (LAB/relative).read_bytes())
        pairs = [(a, LAB/a.relative_to(BASE)) for a in BASE.rglob('*') if a.is_file() and (a.suffix in ('.png','.webp','.frag','.vert') or '/assets/' in str(a))]
        self.assertGreater(len(pairs), 10)
        for original, copy in pairs:
            self.assertTrue(copy.is_file(), str(copy))
            self.assertEqual(original.read_bytes(), copy.read_bytes(), str(copy))

    def test_no_unified_app_identity_left_in_fork_sources(self):
        paths = [p for p in LAB.rglob('*') if p.is_file() and p.suffix in ('.java','.xml','.gradle')]
        self.assertGreater(len(paths), 100)
        for path in paths:
            self.assertNotIn('com.boop.alpha1', path.read_text(), str(path))

if __name__ == '__main__': unittest.main(verbosity=2)
