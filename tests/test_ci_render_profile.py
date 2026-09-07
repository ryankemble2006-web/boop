"""The render profile is confined to the disposable emulator, not a phone."""
import importlib.util
from pathlib import Path
import subprocess
import unittest
from unittest.mock import patch

PATH = Path(__file__).with_name('ci_render_profile.py')

class RenderProfileTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        spec = importlib.util.spec_from_file_location('ci_render_profile', PATH)
        cls.module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(cls.module)

    def test_preserves_density_independent_layout(self):
        self.assertEqual((720, 1560, 280), self.module.half_profile(1440, 3120, 560))
        self.assertEqual((540, 1170, 210), self.module.half_profile(1080, 2340, 420))

    def test_rejects_lossy_or_invalid_halving(self):
        for args in [(1441,3120,560), (1440,3120,561), (0,3120,560), (1440,-1,560), (1440,3120,0)]:
            with self.assertRaises(ValueError): self.module.half_profile(*args)

    def run_profile(self, serial='emulator-5554', qemu='1', verify=True):
        commands = []
        def adb(*args):
            commands.append(args)
            replies = {
                ('get-serialno',): serial,
                ('shell','getprop','ro.kernel.qemu'): qemu,
                ('shell','wm','size'): 'Physical size: 1440x3120',
                ('shell','wm','density'): 'Physical density: 560',
                ('shell','wm','size','720x1560'): '',
                ('shell','wm','density','280'): '',
            }
            if args == ('shell','wm','size') and ('shell','wm','size','720x1560') in commands and verify:
                return replies[args]+'\nOverride size: 720x1560'
            if args == ('shell','wm','density') and ('shell','wm','density','280') in commands and verify:
                return replies[args]+'\nOverride density: 280'
            return replies[args]
        with patch.object(self.module, 'adb', side_effect=adb):
            self.module.main()
        return commands

    def test_only_scales_confirmed_emulator_and_verifies_result(self):
        commands=self.run_profile()
        self.assertIn(('shell','wm','size','720x1560'), commands)
        self.assertIn(('shell','wm','density','280'), commands)
        self.assertFalse(any('settings' in command for command in commands))
        self.assertEqual(2,commands.count(('shell','wm','size')))
        self.assertEqual(2,commands.count(('shell','wm','density')))

    def test_never_modifies_physical_devices(self):
        with self.assertRaises(RuntimeError): self.run_profile(serial='SAMPLE-PHYSICAL-DEVICE')
        with self.assertRaises(RuntimeError): self.run_profile(qemu='0')

    def test_cannot_claim_unapplied_profile(self):
        with self.assertRaises(RuntimeError): self.run_profile(verify=False)
