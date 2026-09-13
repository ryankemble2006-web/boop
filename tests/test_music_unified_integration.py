"""Non-visual v163 transplant checks; release snapshots do not freeze later versions."""
from pathlib import Path
import re
import subprocess
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
BASE = '112d09b5b446d6582954a6d89b3700fe16298ecb'
PROMPT = '7b596a3be376b30f06676997d70b7b50bf8494c0'
HOME = 'unified/shield-home/src/main/java/com/boop/shieldhome/'
ANDROID = '{http://schemas.android.com/apk/res/android}'
BUILD = (ROOT / 'unified/app-build.gradle').read_text()
CODE = int(re.search(r'\bversionCode\s+(\d+)', BUILD).group(1))


def original(ref, path):
    return subprocess.check_output(['git', 'show', ref + ':' + path], cwd=ROOT)


class MusicUnifiedIntegrationTest(unittest.TestCase):
    def test_unified_identity_and_version(self):
        self.assertIn("applicationId 'com.boop.alpha1'", BUILD)
        self.assertGreaterEqual(CODE, 163, 'Requested Unified v163 has not been integrated')
        if CODE == 163:
            self.assertIn('versionName "1.2.163-music-audio-access"', BUILD)

    @unittest.skipUnless(CODE == 163, 'Snapshot applies only to release 163')
    def test_only_permission_entry_and_version_change_v162_app_inputs(self):
        changed = set(subprocess.check_output([
            'git', 'diff', '--name-only', BASE, '--',
            'source', 'unified', 'launcher', 'shield-overlay', 'scripts'
        ], cwd=ROOT).decode().splitlines())
        expected = {HOME + 'MusicAudioPermissionActivity.java',
                    HOME + 'MusicAudioPermissionFlow.java',
                    HOME + 'ShieldHomeSettingsView.java',
                    'unified/shield-home-manifest.xml', 'unified/app-build.gradle'}
        self.assertEqual(expected, changed, 'Unexpected app input changed from accepted v162')

    @unittest.skipUnless(CODE == 163, 'Snapshot applies only to release 163')
    def test_permission_classes_and_settings_are_exact_reviewed_transplant(self):
        for name in ('MusicAudioPermissionActivity.java', 'MusicAudioPermissionFlow.java',
                     'ShieldHomeSettingsView.java'):
            with self.subTest(file=name):
                self.assertEqual(original(PROMPT, HOME + name), (ROOT / (HOME + name)).read_bytes())

    @unittest.skipUnless(CODE == 163, 'Snapshot applies only to release 163')
    def test_existing_lyrics_services_and_permissions_remain_registered(self):
        path = 'unified/shield-home-manifest.xml'
        current = ET.parse(ROOT / path).getroot()
        app = current.find('application')
        added = [a for a in app.findall('activity')
                 if a.get(ANDROID + 'name') == 'com.boop.shieldhome.MusicAudioPermissionActivity']
        self.assertEqual(1, len(added))
        self.assertEqual('false', added[0].get(ANDROID + 'exported'))
        app.remove(added[0])
        baseline = ET.fromstring(original(BASE, path))
        self.assertEqual(ET.canonicalize(ET.tostring(baseline, encoding='unicode'), strip_text=True),
                         ET.canonicalize(ET.tostring(current, encoding='unicode'), strip_text=True))


if __name__ == '__main__':
    unittest.main(verbosity=2)
