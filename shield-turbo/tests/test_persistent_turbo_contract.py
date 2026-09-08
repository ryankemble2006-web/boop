"""Persistent SHIELD TURBO watchdog and reboot safety contracts."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'app/src/main/java/com/boop/shieldturbo'
PERF = SOURCE / 'performance'
MANIFEST = ROOT / 'app/src/main/AndroidManifest.xml'
ANDROID = '{http://schemas.android.com/apk/res/android}'


class PersistentTurboContractTest(unittest.TestCase):
    def test_manifest_declares_private_special_use_watchdog_and_boot_receiver(self):
        root = ET.parse(MANIFEST).getroot()
        app = root.find('application')
        permissions = {item.get(ANDROID + 'name') for item in root.findall('uses-permission')}
        self.assertIn('android.permission.FOREGROUND_SERVICE', permissions)
        self.assertIn('android.permission.FOREGROUND_SERVICE_SPECIAL_USE', permissions)

        services = {item.get(ANDROID + 'name'): item for item in app.findall('service')}
        watchdog = services.get('.performance.TurboThermalWatchdogService')
        self.assertIsNotNone(watchdog, 'private TURBO thermal watchdog service must exist')
        self.assertEqual('false', watchdog.get(ANDROID + 'exported'))
        self.assertEqual('specialUse', watchdog.get(ANDROID + 'foregroundServiceType'))
        special_use = watchdog.find('property')
        self.assertIsNotNone(special_use, 'specialUse watchdog must explain its subtype')
        self.assertEqual('android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE', special_use.get(ANDROID + 'name'))
        self.assertTrue((special_use.get(ANDROID + 'value') or '').strip())

        receivers = {item.get(ANDROID + 'name'): item for item in app.findall('receiver')}
        boot = receivers.get('.performance.TurboBootReceiver')
        self.assertIsNotNone(boot, 'private TURBO boot receiver must exist')
        self.assertEqual('false', boot.get(ANDROID + 'exported'))
        actions = {item.get(ANDROID + 'name') for item in boot.findall('./intent-filter/action')}
        self.assertIn('android.intent.action.BOOT_COMPLETED', actions)

    def test_boot_receiver_only_arms_verified_desired_turbo(self):
        path = PERF / 'TurboBootReceiver.kt'
        self.assertTrue(path.exists(), 'TurboBootReceiver must exist')
        text = path.read_text()
        self.assertIn('BroadcastReceiver', text)
        self.assertIn('Intent.ACTION_BOOT_COMPLETED', text)
        self.assertIn('TurboPhase.TURBO_VERIFIED', text)
        self.assertIn('desiredTurbo', text)
        self.assertIn('TurboThermalWatchdogService.start', text)
        self.assertNotIn('withAdb(', text)
        self.assertNotIn('withTrustedAdb', text)

    def test_watchdog_checks_current_thermal_state_and_delegates_all_changes(self):
        path = PERF / 'TurboThermalWatchdogService.kt'
        self.assertTrue(path.exists(), 'TurboThermalWatchdogService must exist')
        text = path.read_text()
        self.assertIn('startForeground', text)
        self.assertIn('PowerManager', text)
        self.assertIn('currentThermalStatus', text)
        self.assertIn('addThermalStatusListener', text)
        self.assertIn('TurboRuntime', text)
        self.assertIn('bootReapply()', text)
        self.assertIn('thermal(status)', text)
        self.assertIn('TurboPhase.TURBO_VERIFIED', text)
        self.assertIn('stopSelf()', text)
        self.assertNotIn('setSustainedPerformanceMode', text)
        self.assertNotIn('isSustainedPerformanceModeSupported', text)

    def test_performance_writes_remain_the_physically_proven_stock_actuator_only(self):
        paths = list(PERF.glob('*.kt'))
        combined = '\n'.join(path.read_text() for path in paths)
        for prop in (
            'persist.vendor.sys.phs.cpufreq.boost',
            'persist.vendor.sys.phs.gpufreq.boost',
            'persist.vendor.sys.phs.frt.boost',
            'persist.vendor.sys.phs.frt.min',
        ):
            self.assertNotIn(f'setprop {prop}', combined)
        self.assertNotIn('settings put global', combined.lower())
        self.assertNotIn('settings put secure', combined.lower())
        self.assertNotIn('setSustainedPerformanceMode', combined)
        self.assertNotIn('thermalservice override-status', combined.lower())
        self.assertNotIn('thermalservice reset', combined.lower())

        port = (PERF / 'NvidiaTurboPort.kt').read_text()
        self.assertIn('withTrustedAdb', port)
        self.assertNotIn('withAdb(', port)
        self.assertIn('ProcessorModeActuatorPolicy.writeModeCommand(mode)', port)

    def test_watchdog_does_not_leak_into_clean_start_or_brightness(self):
        clean = (SOURCE / 'cleanstart/CleanStartJobService.kt').read_text()
        brightness = (SOURCE / 'BrightnessService.kt').read_text()
        for text in (clean, brightness):
            self.assertNotIn('TurboRuntime', text)
            self.assertNotIn('TurboThermalWatchdogService', text)
            self.assertNotIn('TurboBootReceiver', text)


if __name__ == '__main__':
    unittest.main(verbosity=2)
