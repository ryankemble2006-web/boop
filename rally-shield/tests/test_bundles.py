import importlib.util
import pathlib
import tempfile
import unittest
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]

class BundleTests(unittest.TestCase):
    def setUp(self):
        path = ROOT / 'tools' / 'prepare_games.py'
        self.assertTrue(path.exists(), 'Game bundle preparation is not implemented')
        spec = importlib.util.spec_from_file_location('prepare_games', path)
        self.m = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(self.m)
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = pathlib.Path(self.temp.name)
        self.src = self.root / 'source'
        self.src.mkdir()
        (self.src / 'RALLY.EXE').write_bytes(b'MZ-original-game')
        (self.src / 'TRACKS').mkdir()
        (self.src / 'TRACKS' / 'forest.dat').write_bytes(b'original-track')

    def test_preserves_files_and_adds_controlled_boot(self):
        out = self.root / 'rac93.zip'
        self.m.make_bundle(self.src, out, 'rac93')
        with zipfile.ZipFile(out) as z:
            self.assertEqual(z.read('RALLY.EXE'), b'MZ-original-game')
            self.assertEqual(z.read('TRACKS/forest.dat'), b'original-track')
            self.assertEqual(z.read('DOSBOX.BAT'), b'@echo off\r\nc:\r\nrally.exe\r\nexit\r\n')
            self.assertIn(b'cycles=12000', z.read('DOSBOX.CONF'))
        self.assertEqual((self.src / 'RALLY.EXE').read_bytes(), b'MZ-original-game')
        self.assertFalse((self.src / 'DOSBOX.BAT').exists())

    def test_deterministic_bundle(self):
        a, b = self.root / 'a.zip', self.root / 'b.zip'
        self.m.make_bundle(self.src, a, 'rac93')
        self.m.make_bundle(self.src, b, 'rac93')
        self.assertEqual(a.read_bytes(), b.read_bytes())

    def test_missing_executable_rejected_without_output(self):
        out = self.root / 'missing.zip'
        with self.assertRaises(ValueError):
            self.m.make_bundle(self.src, out, 'rac96')
        self.assertFalse(out.exists())

    def test_output_inside_source_rejected(self):
        with self.assertRaises(ValueError):
            self.m.make_bundle(self.src, self.src / 'bad.zip', 'rac93')

    def test_symbolic_links_rejected(self):
        try:
            (self.src / 'link').symlink_to(self.src / 'RALLY.EXE')
        except OSError:
            self.skipTest('Host does not permit symlinks')
        with self.assertRaises(ValueError):
            self.m.make_bundle(self.src, self.root / 'bad.zip', 'rac93')

    def test_championship_uses_accepted_configuration(self):
        (self.src / 'RAL.EXE').write_bytes(b'MZ-rac96')
        out = self.root / 'rac96.zip'
        self.m.make_bundle(self.src, out, 'rac96')
        with zipfile.ZipFile(out) as z:
            self.assertIn(b'ral.exe', z.read('DOSBOX.BAT'))
            conf = z.read('DOSBOX.CONF')
            for expected in [b'memsize=16', b'sbtype=sb16', b'sbbase=220', b'irq=7', b'cycles=max']:
                self.assertIn(expected, conf)

    def test_case_insensitive_duplicate_rejected(self):
        if (self.src / 'rally.exe').exists():
            self.skipTest('Host filesystem is case-insensitive')
        (self.src / 'rally.exe').write_bytes(b'other')
        with self.assertRaises(ValueError):
            self.m.make_bundle(self.src, self.root / 'bad.zip', 'rac93')

    def test_original_boot_configuration_not_copied(self):
        (self.src / 'dosbox.conf').write_text('mount c /private')
        (self.src / 'dosbox.bat').write_text('wrong.exe')
        out = self.root / 'rac93.zip'
        self.m.make_bundle(self.src, out, 'rac93')
        with zipfile.ZipFile(out) as z:
            self.assertNotIn(b'/private', z.read('DOSBOX.CONF'))
            self.assertNotIn(b'wrong', z.read('DOSBOX.BAT'))

if __name__ == '__main__':
    unittest.main()
