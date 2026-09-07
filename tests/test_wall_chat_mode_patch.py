"""Verify the materialized UI wiring against the real preserved Wall source."""
from pathlib import Path
import importlib.util
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SPEC = importlib.util.spec_from_file_location('wall_chat_patch', ROOT / 'scripts/patch-wall-chat-mode.py')
PATCH = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(PATCH)


class WallChatModePatchTests(unittest.TestCase):
    def source(self):
        return (ROOT / 'source/MainActivity.java').read_text()

    def test_unrecognized_source_fails_instead_of_guessing(self):
        with self.assertRaisesRegex(ValueError, 'expected one source anchor'):
            PATCH.patch_text('class MainActivity {}')

    def test_real_source_patch_is_complete_and_idempotent(self):
        patched = PATCH.patch_text(self.source())
        self.assertEqual(patched, PATCH.patch_text(patched))
        self.assertIn(PATCH.NEW_TOUCH, patched)
        self.assertIn(PATCH.METHODS, patched)
        self.assertIn('requestChatRevision == chatModeRevision', patched)
        self.assertIn('outcome.status() == CommandOutcome.Status.NO_MATCH', patched)
        self.assertIn('openFreeChat(transcript);', patched)

    def test_changed_touch_baseline_fails_safely(self):
        source = self.source().replace('            scheduleMemberBerryHold();',
                                       '            someoneElsesGesture();', 1)
        with self.assertRaises(ValueError):
            PATCH.patch_text(source)

    def test_partial_marker_is_not_mistaken_for_a_complete_patch(self):
        with self.assertRaisesRegex(ValueError, 'incomplete or altered'):
            PATCH.patch_text(PATCH.MARKER + self.source())

    def test_wake_shake_auth_and_voice_methods_are_preserved(self):
        source = self.source()
        patched = PATCH.patch_text(source)
        boundaries = [
            ('    private void handleShakeWake(', '    private void installTtsListener('),
            ('    private void createWakeObjects(', '    private boolean onFaceTouch('),
            ('    private void startWakeRecognition(', '    private void stopListening('),
            ('    private void handleAuthIntent(', '    @Override\n    protected void onNewIntent('),
        ]
        for start, end in boundaries:
            before = source[source.index(start):source.index(end)]
            after = patched[patched.index(start):patched.index(end)]
            self.assertEqual(before, after, start)

    def test_lifecycle_cancels_the_pending_hold(self):
        patched = PATCH.patch_text(self.source())
        self.assertIn('activityInForeground = false;\n        cancelFaceHolds();', patched)
        self.assertIn('if (!hasFocus) {\n            cancelFaceHolds();', patched)
        self.assertIn('super.onConfigurationChanged(newConfig);\n        cancelFaceHolds();', patched)
        self.assertIn('memberBerryConsumed = true;\n            cancelMemberBerryHold();', patched)

    def test_browser_visibility_does_not_add_permissions(self):
        root = ET.parse(ROOT / 'source/AndroidManifest.xml').getroot()
        name = '{http://schemas.android.com/apk/res/android}name'
        self.assertEqual({n.get(name) for n in root.findall('uses-permission')}, {
            'android.permission.RECORD_AUDIO', 'android.permission.INTERNET',
            'android.permission.NEARBY_WIFI_DEVICES'})
        actions = {n.get(name) for n in root.findall('queries/intent/action')}
        self.assertIn('android.support.customtabs.action.CustomTabsService', actions)
        browser = (ROOT / 'source/BoopFreeChat.java').read_text()
        self.assertIn('Uri.parse("https://chatgpt.com/")', browser)
        self.assertNotIn('evaluateJavascript(', browser)
        self.assertNotIn('addJavascriptInterface(', browser)
        self.assertNotIn('getCookie(', browser)
        self.assertNotIn('FLAG_ACTIVITY_NEW_TASK', browser)
        self.assertIn('IS_SENSITIVE', browser)

    def test_mode_preference_is_separate_from_credentials(self):
        store = (ROOT / 'source/BoopChatModeStore.java').read_text()
        self.assertIn('getSharedPreferences("boop_chat_mode", Context.MODE_PRIVATE)', store)
        self.assertIn('.commit()', store)
        self.assertNotIn('SecureTokenStore', store)


if __name__ == '__main__':
    unittest.main()
