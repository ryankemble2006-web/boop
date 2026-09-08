"""Persistent TURBO storage/runtime core contracts."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
PERF = ROOT / 'app/src/main/java/com/boop/shieldturbo/performance'


class TurboRuntimeContractTest(unittest.TestCase):
    def test_state_store_is_one_private_atomic_encoded_snapshot(self):
        path = PERF / 'TurboStore.kt'
        self.assertTrue(path.exists(), 'TurboStore must exist')
        text = path.read_text()
        self.assertIn('getSharedPreferences', text)
        self.assertIn('TurboStateCodec.encode', text)
        self.assertIn('TurboStateCodec.decode', text)
        self.assertIn('.commit()', text)
        self.assertNotIn('MODE_MULTI_PROCESS', text)
        self.assertNotIn('external', text.lower())

    def test_nvidia_runtime_port_uses_only_trusted_local_adb_and_proven_policy(self):
        path = PERF / 'NvidiaTurboPort.kt'
        self.assertTrue(path.exists(), 'NvidiaTurboPort must exist')
        text = path.read_text()
        self.assertIn('LocalBridge(context)', text)
        self.assertIn('withTrustedAdb', text)
        self.assertNotIn('withAdb(', text)
        self.assertIn('ProcessorModeActuatorPolicy.readCommands', text)
        self.assertIn('ProcessorModeActuatorPolicy.writeModeCommand(mode)', text)
        self.assertNotIn('setprop', text.lower())
        self.assertNotIn('settings put global', text.lower())
        self.assertNotIn('settings put secure', text.lower())
        for prop in (
            'persist.vendor.sys.phs.cpufreq.boost',
            'persist.vendor.sys.phs.gpufreq.boost',
            'persist.vendor.sys.phs.frt.boost',
            'persist.vendor.sys.phs.frt.min',
        ):
            self.assertNotIn(f'setprop {prop}', text)

    def test_runtime_serializes_transactions_and_uses_android_thermal_status(self):
        path = PERF / 'TurboRuntime.kt'
        self.assertTrue(path.exists(), 'TurboRuntime must exist')
        text = path.read_text()
        self.assertIn('synchronized(lock)', text)
        self.assertIn('PowerManager.THERMAL_STATUS_SEVERE', text)
        self.assertIn('currentThermalStatus', text)
        self.assertIn('NvidiaTurboPort', text)
        self.assertIn('TurboStore', text)
        self.assertNotIn('isSustainedPerformanceModeSupported', text)
        self.assertNotIn('setSustainedPerformanceMode', text)

    def test_runtime_core_does_not_embed_service_or_boot_receiver_behavior(self):
        combined = '\n'.join(
            path.read_text() for path in (
                PERF / 'TurboStateCodec.kt',
                PERF / 'TurboStore.kt',
                PERF / 'NvidiaTurboPort.kt',
                PERF / 'TurboRuntime.kt',
            ) if path.exists()
        )
        self.assertNotRegex(combined, r'\bclass\s+\w*Service\b')
        self.assertNotIn('BroadcastReceiver', combined)
        self.assertNotIn('BOOT_COMPLETED', combined)
        self.assertNotIn('startForeground', combined)


if __name__ == '__main__':
    unittest.main(verbosity=2)
