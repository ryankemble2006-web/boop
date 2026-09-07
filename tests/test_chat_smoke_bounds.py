import importlib.util
from pathlib import Path
import unittest
from unittest.mock import patch
import xml.etree.ElementTree as ET

SCRIPT = Path(__file__).resolve().parents[1] / 'scripts/smoke-wall-chat-mode.py'
spec = importlib.util.spec_from_file_location('chat_smoke', SCRIPT)
smoke = importlib.util.module_from_spec(spec)
spec.loader.exec_module(smoke)


def node(bounds, package='com.boop.alpha1', description='BOOP face. Tap anywhere to speak.'):
    return ET.fromstring(f'<hierarchy><node package="{package}" content-desc="{description}" bounds="{bounds}" /></hierarchy>')


class FaceBoundsTest(unittest.TestCase):
    def test_portrait(self):
        self.assertEqual((720, 1560), smoke.face_center(node('[0,0][1440,3120]')))

    def test_rotated_display(self):
        self.assertEqual((1560, 720), smoke.face_center(node('[0,0][3120,1440]')))

    def test_current_window_not_natural_panel(self):
        self.assertEqual((500, 1100), smoke.face_center(node('[20,100][980,2100]')))

    def test_foreign_window_is_not_boop(self):
        with self.assertRaises(AssertionError):
            smoke.face_center(node('[0,0][1440,3120]', package='com.android.systemui'))

    def test_dialog_is_not_face(self):
        with self.assertRaises(AssertionError):
            smoke.face_center(node('[0,0][1440,3120]', description=''))

    def test_invalid_bounds_are_rejected(self):
        for bounds in ('', '[4,4][4,8]', '[9,9][2,2]', 'broken'):
            with self.subTest(bounds=bounds), self.assertRaises(AssertionError):
                smoke.face_center(node(bounds))


def immersive_tutorial(package='com.android.systemui', title_id='immersive_cling_title',
                       button_id='ok', clickable='true'):
    return ET.fromstring(f'''<hierarchy>
        <node package="{package}" text="Viewing full screen"
              resource-id="{package}:id/{title_id}" />
        <node package="{package}" text="Got it"
              resource-id="{package}:id/{button_id}" clickable="{clickable}"
              bounds="[1083,602][1293,728]" />
    </hierarchy>''')


class ImmersiveTutorialTest(unittest.TestCase):
    def test_dismisses_only_observed_system_tutorial(self):
        with patch.object(smoke, 'adb') as adb:
            self.assertTrue(smoke.dismiss_immersive_tutorial(immersive_tutorial()))
            adb.assert_called_once_with('shell', 'input', 'tap', '1188', '665')

    def test_does_not_touch_normal_boop_face(self):
        with patch.object(smoke, 'adb') as adb:
            self.assertFalse(smoke.dismiss_immersive_tutorial(node('[0,0][1440,3120]')))
            adb.assert_not_called()

    def test_does_not_accept_another_apps_lookalike_dialog(self):
        with patch.object(smoke, 'adb') as adb:
            self.assertFalse(smoke.dismiss_immersive_tutorial(immersive_tutorial(package='com.example.other')))
            adb.assert_not_called()

    def test_does_not_accept_an_unrelated_system_dialog(self):
        with patch.object(smoke, 'adb') as adb:
            self.assertFalse(smoke.dismiss_immersive_tutorial(immersive_tutorial(title_id='permission_title')))
            adb.assert_not_called()

    def test_does_not_click_an_unrecognized_control(self):
        for kwargs in ({'button_id': 'allow_button'}, {'clickable': 'false'}):
            with self.subTest(kwargs=kwargs), patch.object(smoke, 'adb') as adb:
                with self.assertRaises(AssertionError):
                    smoke.dismiss_immersive_tutorial(immersive_tutorial(**kwargs))
                adb.assert_not_called()


class HierarchyReadinessTest(unittest.TestCase):
    def test_missing_dump_after_restart_retries_a_fresh_dump(self):
        import subprocess
        missing = subprocess.CalledProcessError(1, ['adb', 'shell', 'cat', smoke.DUMP], stderr='No such file')
        xml = '<?xml version="1.0"?><hierarchy><node text="ready" /></hierarchy>'
        with patch.object(smoke, 'adb', side_effect=['', 'ERROR: could not get idle state', missing, '', 'dumped', xml]) as adb, patch.object(smoke.time, 'sleep'):
            root = smoke.hierarchy('restart')
            self.assertTrue(smoke.has_text(root, 'ready'))
            self.assertEqual(2, sum(call.args == ('shell', 'rm', '-f', smoke.DUMP) for call in adb.call_args_list))

    def test_dump_failure_is_bounded_and_never_a_pass(self):
        import subprocess
        failure = subprocess.CalledProcessError(1, ['adb', 'shell', 'cat', smoke.DUMP], stderr='No such file')
        with patch.object(smoke, 'adb', side_effect=['', 'no idle', failure] * 4) as adb, patch.object(smoke.time, 'sleep'):
            with self.assertRaises(subprocess.CalledProcessError):
                smoke.hierarchy('failed')
            self.assertEqual(12, adb.call_count)

    def test_retries_malformed_dump_but_returns_only_valid_current_xml(self):
        xml = '<?xml version="1.0"?><hierarchy><node text="current" /></hierarchy>'
        with patch.object(smoke, 'adb', side_effect=['', 'dumped', '<invalid', '', 'dumped', xml]), patch.object(smoke.time, 'sleep'):
            self.assertTrue(smoke.has_text(smoke.hierarchy('current'), 'current'))



def mode_menu(text='OPENCODE  ✓', description='Use OpenCode', package='com.boop.alpha1'):
    return ET.fromstring(f'''<hierarchy><node class="android.widget.Button"
        package="{package}" text="{text}" content-desc="{description}"
        clickable="true" enabled="true" bounds="[100,200][500,400]" /></hierarchy>''')


class ModeSelectionTest(unittest.TestCase):
    def test_selection_uses_stable_description_not_transformed_display_case(self):
        for text in ('OpenCode  ✓', 'OPENCODE  ✓'):
            with self.subTest(text=text):
                smoke.assert_selected_mode(mode_menu(text), 'OpenCode')

    def test_unselected_or_wrong_mode_cannot_pass(self):
        for root in (mode_menu('OPENCODE'), mode_menu(description='Use Free Chat'),
                     mode_menu(package='com.example.other')):
            with self.assertRaises(AssertionError):
                smoke.assert_selected_mode(root, 'OpenCode')

    def test_duplicate_description_is_ambiguous(self):
        root = mode_menu()
        root.append(ET.fromstring(ET.tostring(root[0])))
        with self.assertRaises(AssertionError):
            smoke.assert_selected_mode(root, 'OpenCode')

    def test_description_click_is_independent_of_display_case(self):
        with patch.object(smoke, 'adb') as adb:
            smoke.tap_description(mode_menu(), 'Use OpenCode')
            adb.assert_called_once_with('shell', 'input', 'tap', '300', '300')


if __name__ == '__main__':
    unittest.main()
