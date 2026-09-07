"""Source-level safety guards; runtime behavior is tested by Kotlin and emulator checks."""
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'app/src/main/java/com/boop/shieldturbo'
ANDROID = '{http://schemas.android.com/apk/res/android}'

class ContractTest(unittest.TestCase):
    def test_view_ids_are_android_resources(self):
        text = (SOURCE / 'MainActivity.kt').read_text()
        self.assertNotRegex(text, r'\bid\s*=\s*\d+')
        self.assertIn('R.id.analyse_button', text)
        self.assertIn('R.id.analysis_results', text)

    def test_scan_is_not_continuous_or_on_the_ui_thread(self):
        text = (SOURCE / 'MainActivity.kt').read_text()
        self.assertIn('Executors.newSingleThreadExecutor', text)
        self.assertIn('onStop', text)
        self.assertIn('cancel(true)', text)
        self.assertNotIn('FLAG_KEEP_SCREEN_ON', text)

    def test_usage_access_and_su_files_are_not_privilege_proof(self):
        text = (SOURCE / 'privilege/PrivilegeDetector.kt').read_text()
        self.assertNotIn('PACKAGE_USAGE_STATS', text)
        self.assertNotIn('/system/xbin/su', text)
        self.assertIn('Process.myUid()', text)

    def test_manifest_permissions_and_tv_entry(self):
        manifest = ET.parse(ROOT / 'app/src/main/AndroidManifest.xml').getroot()
        permissions = {p.get(ANDROID + 'name') for p in manifest.findall('uses-permission')}
        self.assertEqual({
            'android.permission.ACCESS_NETWORK_STATE',
            'android.permission.SYSTEM_ALERT_WINDOW',
        }, permissions)
        app = manifest.find('application')
        services = app.findall('service')
        self.assertEqual(1, len(services))
        self.assertEqual('.BrightnessService', services[0].get(ANDROID + 'name'))
        self.assertEqual('false', services[0].get(ANDROID + 'exported'))
        self.assertEqual([], app.findall('receiver'))
        categories = {c.get(ANDROID + 'name') for c in app.findall('.//category')}
        self.assertIn('android.intent.category.LEANBACK_LAUNCHER', categories)

if __name__ == '__main__':
    unittest.main(verbosity=2)
