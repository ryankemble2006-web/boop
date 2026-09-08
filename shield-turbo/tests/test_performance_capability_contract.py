"""Stage-1 performance probe contracts: discovery only, no tuning writes."""
from pathlib import Path
import re
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'app/src/main/java/com/boop/shieldturbo'
ANDROID = '{http://schemas.android.com/apk/res/android}'


class PerformanceCapabilityContractTest(unittest.TestCase):
    def test_stage_one_is_read_only_and_uses_trusted_adb(self):
        perf = '\n'.join(p.read_text() for p in (SOURCE / 'performance').glob('*.kt'))
        self.assertIn('withTrustedAdb', perf)
        self.assertNotIn('withAdb(', perf)
        self.assertNotIn('settings put', perf)
        self.assertNotIn('set-fixed-performance-mode-enabled true', perf)
        self.assertNotRegex(perf, r'(?<![A-Za-z0-9_])su(?![A-Za-z0-9_])')
        self.assertNotRegex(perf, r'echo\s+[^\n]+>')

    def test_stage_one_adds_no_resident_performance_component(self):
        manifest = ET.parse(ROOT / 'app/src/main/AndroidManifest.xml').getroot()
        app = manifest.find('application')
        services = {s.get(ANDROID + 'name') for s in app.findall('service')}
        receivers = {r.get(ANDROID + 'name') for r in app.findall('receiver')}
        permissions = {p.get(ANDROID + 'name') for p in manifest.findall('uses-permission')}
        self.assertNotIn('.performance.ThermalWatchdogService', services)
        self.assertNotIn('.performance.PerformanceBootReceiver', receivers)
        self.assertNotIn('android.permission.FOREGROUND_SERVICE', permissions)

    def test_turbo_scan_surfaces_performance_capability_results(self):
        main = (SOURCE / 'MainActivity.kt').read_text()
        self.assertIn('PerformanceCapabilityProbe(context).readResults()', main)
        self.assertIn('snapshot.results + performance + privilege', main)

    def test_clean_start_and_brightness_remain_separate(self):
        main = (SOURCE / 'MainActivity.kt').read_text()
        self.assertNotIn('PerformanceCapabilityProbe', (SOURCE / 'cleanstart/CleanStartJobService.kt').read_text())
        self.assertNotIn('PerformanceCapabilityProbe', (SOURCE / 'BrightnessService.kt').read_text())
        self.assertNotRegex(main, r'set-fixed-performance-mode-enabled\s+true')


if __name__ == '__main__':
    unittest.main(verbosity=2)
