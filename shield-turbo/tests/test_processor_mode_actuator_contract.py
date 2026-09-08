"""One-shot NVIDIA Processor Mode actuator proof contracts."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'app/src/main/java/com/boop/shieldturbo'
PERF = SOURCE / 'performance'
ANDROID = '{http://schemas.android.com/apk/res/android}'


class ProcessorModeActuatorContractTest(unittest.TestCase):
    def test_actuator_uses_trusted_local_adb_and_exact_nv_power_mode_writes(self):
        probe_path = PERF / 'ProcessorModeActuatorProbe.kt'
        policy_path = PERF / 'ProcessorModeActuatorPolicy.kt'
        self.assertTrue(probe_path.exists(), 'actuator probe must exist')
        self.assertTrue(policy_path.exists(), 'actuator policy must exist')
        probe = probe_path.read_text()
        policy = policy_path.read_text()
        self.assertIn('LocalBridge(context)', probe)
        self.assertIn('withTrustedAdb', probe)
        self.assertNotIn('withAdb(', probe)
        self.assertIn('settings put system nv_power_mode', policy)
        self.assertNotIn('setprop', policy.lower())
        self.assertNotIn('settings put global', policy.lower())
        self.assertNotIn('settings put secure', policy.lower())

    def test_vendor_boost_properties_are_read_only_evidence(self):
        actuator = '\n'.join(path.read_text() for path in PERF.glob('ProcessorModeActuator*.kt'))
        for prop in (
            'persist.vendor.sys.phs.cpufreq.boost',
            'persist.vendor.sys.phs.gpufreq.boost',
            'persist.vendor.sys.phs.frt.boost',
            'persist.vendor.sys.phs.frt.min',
        ):
            self.assertIn(f'getprop {prop}', actuator)
            self.assertNotIn(f'setprop {prop}', actuator)
        self.assertNotIn('echo ', actuator.lower())
        self.assertNotIn(' tee ', actuator.lower())

    def test_proof_requires_optimized_baseline_and_restores_after_max_attempt(self):
        proof_path = PERF / 'ProcessorModeActuatorProof.kt'
        self.assertTrue(proof_path.exists(), 'actuator proof logic must exist')
        proof = proof_path.read_text()
        self.assertIn('EXPECTED_OPTIMIZED', proof)
        self.assertIn('EXPECTED_MAX', proof)
        self.assertIn('writeMode(0)', proof)
        self.assertIn('writeMode(1)', proof)
        self.assertIn('restoreVerified', proof)

    def test_turbo_page_exposes_one_shot_proof_not_persistent_turbo(self):
        main = (SOURCE / 'MainActivity.kt').read_text()
        self.assertIn('R.string.processor_actuator_proof', main)
        self.assertIn('showProcessorActuatorPrompt()', main)
        self.assertIn('runProcessorActuatorProof()', main)
        self.assertIn('ProcessorModeActuatorProbe(applicationContext).runProof()', main)
        self.assertNotIn('ThermalWatchdogService', main)
        self.assertNotIn('PerformanceBootReceiver', main)

    def test_actuator_adds_no_resident_component_or_foreground_permission(self):
        manifest = ET.parse(ROOT / 'app/src/main/AndroidManifest.xml').getroot()
        app = manifest.find('application')
        services = {s.get(ANDROID + 'name') for s in app.findall('service')}
        receivers = {r.get(ANDROID + 'name') for r in app.findall('receiver')}
        permissions = {p.get(ANDROID + 'name') for p in manifest.findall('uses-permission')}
        self.assertFalse(any(name and '.performance.' in name for name in services))
        self.assertFalse(any(name and '.performance.' in name for name in receivers))
        self.assertNotIn('android.permission.FOREGROUND_SERVICE', permissions)

    def test_clean_start_and_brightness_remain_untouched(self):
        clean = (SOURCE / 'cleanstart/CleanStartJobService.kt').read_text()
        bright = (SOURCE / 'BrightnessService.kt').read_text()
        self.assertNotIn('ProcessorModeActuator', clean)
        self.assertNotIn('ProcessorModeActuator', bright)


if __name__ == '__main__':
    unittest.main(verbosity=2)
