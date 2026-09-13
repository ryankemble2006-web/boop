"""Non-visual v164 gates. Never installs or launches Android."""
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
HOME = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'
BASE = '112d09b5b446d6582954a6d89b3700fe16298ecb'

class MusicBounceTest(unittest.TestCase):
    def test_real_level_and_smoothing_behaviour(self):
        source = HOME / 'MusicBounceEnvelope.java'
        self.assertTrue(source.is_file(), 'Real music bounce envelope is missing')
        with tempfile.TemporaryDirectory() as out:
            subprocess.run(['javac', '-d', out, str(source), str(ROOT/'tests/java/MusicBounceHarness.java')], check=True)
            subprocess.run(['java', '-cp', out, 'com.boop.shieldhome.MusicBounceHarness'], check=True)

    def test_actual_audio_source_and_visible_renderer_are_connected(self):
        source = HOME / 'MusicBounceSource.java'
        self.assertTrue(source.is_file(), 'No Android Visualizer audio source exists')
        text = source.read_text()
        for required in ('new Visualizer(0)', 'getWaveForm(', 'setEnabled(true)', 'release()', 'HandlerThread', 'MusicBounceEnvelope.levelOf'):
            self.assertIn(required, text)
        for forbidden in ('AudioRecord', 'MediaRecorder', 'SpeechRecognizer', 'requestAudioFocus', 'setStreamVolume', 'MediaProjection'):
            self.assertNotIn(forbidden, text)
        view = (HOME/'ShieldNowPlayingPuppetView.java').read_text()
        for required in ('MusicBounceSource', 'musicSource.setActive(', 'musicEnvelope.update(', 'setBounceHeight(', 'new MusicBounceRenderer(eyeRenderer)', 'eyeSurface.setRenderer(musicRenderer)', 'musicSource.stop()'):
            self.assertIn(required, view)
        self.assertIn('speed -> animation.setSpeed(speed, SystemClock.uptimeMillis())', view)
        renderer = (HOME/'MusicBounceRenderer.java').read_text()
        self.assertIn('GLES20.glViewport', renderer)
        self.assertIn('delegate.onDrawFrame', renderer)

    def test_accepted_v162_inputs_are_not_regressed(self):
        allowed = {'unified/app-build.gradle', 'unified/shield-home-manifest.xml',
                   'unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingPuppetView.java',
                   'unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeSettingsView.java'}
        # Every previously tracked app/build input is immutable except these four narrow adapters.
        prefixes = ['source', 'source-test', 'unified', 'scripts', 'launcher', 'shield-overlay', 'shield-clean-launcher', 'BOOP-Alpha1-project.zip', 'gradle.properties']
        changed = subprocess.check_output(['git','diff','--name-only','--diff-filter=MDR',BASE,'HEAD','--',*prefixes], cwd=ROOT, text=True).splitlines()
        self.assertFalse(set(changed)-allowed, 'Unexpected change to accepted v162 input: '+str(set(changed)-allowed))
        path = 'unified/app-build.gradle'
        old = subprocess.check_output(['git','show',BASE+':'+path], cwd=ROOT, text=True)
        expected = old.replace('versionCode 162','versionCode 164').replace('1.2.162-native-lyrics','1.2.164-music-bounce')
        self.assertEqual(expected, (ROOT/path).read_text(), 'Only the two version fields may change in build configuration')

    def test_permission_entry_does_not_use_voice_callback(self):
        activity = HOME/'MusicAudioPermissionActivity.java'
        self.assertTrue(activity.is_file(), 'Conditional audio permission entry is missing')
        self.assertIn('requestPermissions(', activity.read_text())
        self.assertNotIn('SpeechRecognizer', activity.read_text())
        self.assertIn('MusicAudioPermissionActivity.class', (HOME/'ShieldHomeSettingsView.java').read_text())
        self.assertIn('com.boop.shieldhome.MusicAudioPermissionActivity', (ROOT/'unified/shield-home-manifest.xml').read_text())

if __name__ == '__main__':
    unittest.main(verbosity=2)
