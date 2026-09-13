"""Packaging, source preservation and exact-media routing checks. No visual tests."""
from pathlib import Path
import importlib.util
import subprocess
import tempfile
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ANDROID = '{http://schemas.android.com/apk/res/android}'
SHARED = ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome'

class LyricsLabChecks(unittest.TestCase):
    def test_side_by_side_package_has_no_home_or_privileged_components(self):
        gradle = ROOT / 'lyrics-lab/app/build.gradle'
        self.assertTrue(gradle.exists(), 'A separately installable Lyrics Lab app is missing')
        text = gradle.read_text(encoding='utf-8')
        self.assertIn("applicationId 'com.boop.lyricslab'", text)
        self.assertNotIn("applicationId 'com.boop.alpha1'", text)
        manifest = ET.parse(ROOT / 'lyrics-lab/app/src/main/AndroidManifest.xml').getroot()
        permissions = {e.get(ANDROID + 'name') for e in manifest.findall('uses-permission')}
        self.assertEqual({'android.permission.INTERNET'}, permissions)
        application = manifest.find('application')
        self.assertEqual('BOOP Lyrics Lab', application.get(ANDROID + 'label'))
        self.assertEqual([], application.findall('provider'))
        self.assertEqual([], application.findall('receiver'))
        categories = {e.get(ANDROID + 'name') for e in application.findall('.//category')}
        self.assertNotIn('android.intent.category.HOME', categories)
        self.assertIn('android.intent.category.LEANBACK_LAUNCHER', categories)
        self.assertEqual([], application.findall('.//data'))
        services = application.findall('service')
        self.assertEqual(1, len(services))
        self.assertEqual('android.permission.BIND_NOTIFICATION_LISTENER_SERVICE', services[0].get(ANDROID + 'permission'))
        self.assertNotIn('sharedUserId', ET.tostring(manifest, encoding='unicode'))

    def test_materialization_keeps_approved_renderer_and_lyrics_code_byte_identical(self):
        path = ROOT / 'scripts/materialize-lyrics-lab.py'
        self.assertTrue(path.exists(), 'The isolated build adapter is missing')
        spec = importlib.util.spec_from_file_location('lyrics_lab_materializer', path)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        with tempfile.TemporaryDirectory() as folder:
            module.materialize(Path(folder))
            for name in ('ShieldLyricsView', 'LyricsLinesView', 'DeezerLyricsDocument', 'DeezerTimedLyricsClient', 'NativeLyricsLoader'):
                self.assertEqual((SHARED / (name + '.java')).read_bytes(),
                    (Path(folder) / 'java/com/boop/shieldhome' / (name + '.java')).read_bytes())
            self.assertFalse((Path(folder) / 'java/com/boop/shieldhome/ShieldLauncherActivity.java').exists())
            self.assertFalse((Path(folder) / 'java/com/boop/shieldhome/ShieldNowPlayingManager.java').exists())

    def test_only_exact_native_deezer_recordings_are_eligible(self):
        policy = ROOT / 'lyrics-lab/app/src/main/java/com/boop/shieldhome/LyricsLabMediaPolicy.java'
        self.assertTrue(policy.exists(), 'Lab media isolation policy is missing')
        harness = '''package com.boop.shieldhome;
public class LabPolicyCheck {
 static int checks;
 static void same(String want,String got) {if(!want.equals(got))throw new AssertionError(want+" != "+got);checks++;}
 public static void main(String[] args) {
  same("123",LyricsLabMediaPolicy.recordingId("deezer.android.app","TRACK","123"));
  for(String pkg:new String[]{null,"","com.boop.alpha1","com.google.android.apps.mediashell","other.deezer.android.app"})
   same("",LyricsLabMediaPolicy.recordingId(pkg,"TRACK","123"));
  for(String id:new String[]{null,"","0","-1"," 123","123;456","123/4","9999999999999999999999"})
   same("",LyricsLabMediaPolicy.recordingId("deezer.android.app","TRACK",id));
  for(String type:new String[]{null,"","PODCAST","RADIO","track"})
   same("",LyricsLabMediaPolicy.recordingId("deezer.android.app",type,"123"));
  System.out.println("PASS: "+checks+" exact native Deezer identity checks.");
 }
}'''
        with tempfile.TemporaryDirectory() as folder:
            file = Path(folder) / 'LabPolicyCheck.java'
            file.write_text(harness, encoding='utf-8')
            subprocess.run(['javac', '-encoding', 'UTF-8', '-d', folder, str(policy), str(file)], check=True)
            subprocess.run(['java', '-cp', folder, 'com.boop.shieldhome.LabPolicyCheck'], check=True)

if __name__ == '__main__':
    unittest.main(verbosity=2)
