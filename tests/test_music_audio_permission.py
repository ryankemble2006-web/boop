"""Non-visual checks for the music-only audio permission entry point."""
from pathlib import Path
import subprocess
import tempfile
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'
ANDROID = '{http://schemas.android.com/apk/res/android}'


class MusicAudioPermissionTest(unittest.TestCase):
    def test_permission_flow_behaviour(self):
        source = JAVA / 'MusicAudioPermissionFlow.java'
        self.assertTrue(source.is_file(), 'Music audio permission flow is missing')
        with tempfile.TemporaryDirectory() as output:
            subprocess.run(['javac', '-d', output, str(source), str(ROOT / 'tests/java/MusicAudioPermissionHarness.java')], check=True)
            subprocess.run(['java', '-cp', output, 'com.boop.shieldhome.MusicAudioPermissionHarness'], check=True)

    def test_music_settings_has_an_explicit_entry(self):
        settings = (JAVA / 'ShieldHomeSettingsView.java').read_text()
        self.assertIn('MusicAudioPermissionActivity.class', settings)
        self.assertIn('Music audio access:', settings)
        self.assertIn('MusicAudioPermissionActivity.hasAudioAccess(getContext())', settings)

    def test_permission_activity_is_private_and_permissions_are_declared(self):
        manifest = ET.parse(ROOT / 'unified/shield-home-manifest.xml').getroot()
        activities = manifest.find('application').findall('activity')
        matches = [a for a in activities if a.get(ANDROID + 'name') == 'com.boop.shieldhome.MusicAudioPermissionActivity']
        self.assertEqual(1, len(matches), 'Music audio permission activity must be registered once')
        self.assertEqual('false', matches[0].get(ANDROID + 'exported'))
        permissions = {p.get(ANDROID + 'name') for p in manifest.findall('uses-permission')}
        wall = ET.parse(ROOT / 'source/AndroidManifest.xml').getroot()
        permissions.update(p.get(ANDROID + 'name') for p in wall.findall('uses-permission'))
        self.assertIn('android.permission.RECORD_AUDIO', permissions)
        self.assertIn('android.permission.MODIFY_AUDIO_SETTINGS', permissions)

    def test_prompt_does_not_start_audio_or_speech(self):
        path = JAVA / 'MusicAudioPermissionActivity.java'
        self.assertTrue(path.is_file(), 'Music audio permission prompt is missing')
        source = path.read_text()
        self.assertIn('requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}', source)
        self.assertIn('hasAudioAccess(this)', source)
        self.assertIn('Settings.ACTION_APPLICATION_DETAILS_SETTINGS', source)
        self.assertIn('"Not now"', source)
        self.assertIn('onSaveInstanceState', source)
        self.assertIn('flow.result(hasAudioAccess(this))', source)
        for forbidden in ('AudioRecord', 'MediaRecorder', 'SpeechRecognizer', 'new Visualizer', 'startService(', 'startForegroundService(', 'grantRuntimePermission', 'pm grant', 'java.net.', 'okhttp'):
            self.assertNotIn(forbidden, source)


if __name__ == '__main__':
    unittest.main(verbosity=2)
