import importlib.util
from pathlib import Path
import unittest
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


if __name__ == '__main__':
    unittest.main()
