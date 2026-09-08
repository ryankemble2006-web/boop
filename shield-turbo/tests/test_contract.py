"""Source-level safety guards; visual acceptance belongs to the physical Shield."""
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'app/src/main/java/com/boop/shieldturbo'
WORKFLOW = ROOT.parent / '.github/workflows/shield-turbo.yml'
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

    def test_turbo_results_do_not_trap_dpad_focus(self):
        text = (SOURCE / 'MainActivity.kt').read_text()
        self.assertIn(
            'card.nextFocusDownId = if (index == cards.lastIndex) freeSpaceButton.id else cards[index + 1].id',
            text,
        )
        self.assertNotIn(
            'card.nextFocusDownId = if (index == cards.lastIndex) card.id else cards[index + 1].id',
            text,
        )

    def test_app_click_launches_directly_without_package_dialog(self):
        text = (SOURCE / 'MainActivity.kt').read_text()
        self.assertIn('actionButton(app.label) { safeStart(AppRoutes.launch(this, app.packageName)) }', text)
        self.assertNotIn('.setMessage(app.packageName)', text)

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
            'android.permission.WRITE_SECURE_SETTINGS',
        }, permissions)
        app = manifest.find('application')
        services = app.findall('service')
        self.assertEqual(1, len(services))
        self.assertEqual('.BrightnessService', services[0].get(ANDROID + 'name'))
        self.assertEqual('false', services[0].get(ANDROID + 'exported'))
        self.assertEqual([], app.findall('receiver'))
        categories = {c.get(ANDROID + 'name') for c in app.findall('.//category')}
        self.assertIn('android.intent.category.LEANBACK_LAUNCHER', categories)

    def test_ci_does_not_judge_visual_ui(self):
        workflow = WORKFLOW.read_text()
        self.assertNotIn('uiautomator dump', workflow)
        self.assertNotIn('check_emulator_ui.py', workflow)
        self.assertNotIn('shield-turbo-home.xml', workflow)
        self.assertNotIn('shield-turbo-ui.xml', workflow)
        self.assertNotIn('shield-turbo-focus.xml', workflow)

if __name__ == '__main__':
    unittest.main(verbosity=2)
