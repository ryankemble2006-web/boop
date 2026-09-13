"""Native route, immutable transplant and v161 preservation. No visual acceptance tests."""
from pathlib import Path
import hashlib
import subprocess
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'
BASE = '593ad609ff87f651d5273bd17f5a2c0ca3ef5198'
ANDROID = '{http://schemas.android.com/apk/res/android}'
COPIED = {
 'DeezerLyricsBrowser': 'a3495af7aebe83760bc7d5d3a096e811b4d597e4',
 'DeezerLyricsDocument': '2a53afb6c670dec384f86da0cc6b197e1be4a015',
 'DeezerTimedLyricsClient': '2e8ba9c87a70224ea4e6435614a61da8bf108449',
 'LyricsLinesView': '2c8570159f0404681b0b604e83f3eadc8e813b52',
 'LyricsRequestGate': 'e9ecab3e446b0891b30563cab0760229ca2db2a5',
 'NativeLyricsLoader': 'ea66065702d43032ad73e86f2de14c7032e42f77',
 'ShieldLyricsActivity': '79eeeb8f0008386df2094a7b7226f8a16f20e2fa',
 'ShieldLyricsView': 'a19b03369038b55fc25f93e1cff3795f496ea03c',
}

def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT)

class UnifiedLyricsIntegration(unittest.TestCase):
    def test_01_now_playing_routes_to_native_activity_not_the_macro_or_lab(self):
        source = (SRC / 'DeezerLyricsBrowser.java').read_text(encoding='utf-8')
        self.assertIn('new Intent(activity, ShieldLyricsActivity.class)', source,
                      'Now Playing still uses the Deezer foreground macro instead of the native lyrics screen')
        for legacy in ('StartupLocalBridge', 'openNotificationSource', 'openDeezerLyrics', 'com.boop.lyricslab'):
            self.assertNotIn(legacy, source)
        launcher = (SRC / 'ShieldLauncherActivity.java').read_text(encoding='utf-8')
        self.assertIn('onOpenNowPlayingLyrics()', launcher)
        self.assertIn('lyricsBrowser.open(', launcher)

    def test_private_activity_no_new_permissions_no_lab_dependency(self):
        before = ET.fromstring(git('show', BASE + ':unified/shield-home-manifest.xml'))
        after = ET.parse(ROOT / 'unified/shield-home-manifest.xml').getroot()
        activities = [a for a in after.findall('application/activity')
                      if a.get(ANDROID + 'name') == 'com.boop.shieldhome.ShieldLyricsActivity']
        self.assertEqual(1, len(activities))
        self.assertEqual('false', activities[0].get(ANDROID + 'exported'))
        self.assertEqual([], activities[0].findall('intent-filter'))
        self.assertEqual({e.get(ANDROID + 'name') for e in before.findall('uses-permission')},
                         {e.get(ANDROID + 'name') for e in after.findall('uses-permission')})
        self.assertEqual(ET.tostring(before.find('queries')), ET.tostring(after.find('queries')))
        self.assertNotIn('LyricsLab', ET.tostring(after, encoding='unicode'))

    def test_exact_tested_source_transplant(self):
        for name, expected in COPIED.items():
            with self.subTest(source=name):
                path = SRC / (name + '.java')
                self.assertTrue(path.exists(), name + ' is missing')
                content = path.read_bytes()
                digest = hashlib.sha1(b'blob ' + str(len(content)).encode() + b'\0' + content).hexdigest()
                self.assertEqual(expected, digest, 'Transplant must use the reviewed source blob')

    def test_v161_protected_code_is_unchanged(self):
        allowed = {'unified/app-build.gradle', 'unified/shield-home-manifest.xml'}
        allowed.update(str((SRC / (name + '.java')).relative_to(ROOT)) for name in COPIED)
        # This is a source-integrity check, not a rendering or screenshot comparison.
        changed = git('diff', '--name-only', BASE, 'HEAD', '--', 'source', 'shared', 'unified',
                      'launcher', 'shield-overlay', 'scripts/materialize-unified.sh').decode().splitlines()
        self.assertEqual([], [path for path in changed if path not in allowed],
                         'A lyrics integration must not replace accepted colour/speed/artwork or other v161 code')
        for name in ('ShieldNowPlayingManager.java', 'NowPlayingState.java', 'ShieldLauncherActivity.java'):
            relative = str((SRC / name).relative_to(ROOT))
            self.assertEqual(git('show', BASE + ':' + relative), (SRC / name).read_bytes())
        self.assertFalse((ROOT / 'lyrics-lab').exists(), 'Do not import the separate application shell')
        self.assertIn('versionCode 162', (ROOT / 'unified/app-build.gradle').read_text())

if __name__ == '__main__':
    unittest.main(verbosity=2)
