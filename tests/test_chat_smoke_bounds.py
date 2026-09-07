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


if __name__ == '__main__':
    unittest.main()
