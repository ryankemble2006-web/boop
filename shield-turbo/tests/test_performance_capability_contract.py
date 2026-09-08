"""Stage-1 performance probe contracts: discovery only, no tuning writes."""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'app/src/main/java/com/boop/shieldturbo'


class PerformanceCapabilityContractTest(unittest.TestCase):
    def test_stage_one_is_read_only_and_uses_trusted_adb(self):
        perf_dir = SOURCE / 'performance'
        stage_one_paths = [
            perf_dir / 'CompactAnalysisReport.kt',
            perf_dir / 'PerformanceCapability.kt',
            perf_dir / 'PerformanceCapabilityProbe.kt',
            perf_dir / 'PerformanceDiscoveryPolicy.kt',
            *sorted(perf_dir.glob('ProcessorModeTrace*.kt')),
        ]
        perf = '\n'.join(path.read_text() for path in stage_one_paths)
        self.assertIn('withTrustedAdb', perf)
        self.assertNotIn('withAdb(', perf)
        self.assertNotIn('settings put', perf)
        self.assertNotIn('set-fixed-performance-mode-enabled true', perf)
        self.assertNotRegex(perf, r'(?<![A-Za-z0-9_])su(?![A-Za-z0-9_])')
        self.assertNotRegex(perf, r'echo\s+[^\n]+>')

    def test_stage_one_probe_remains_independent_of_resident_turbo_runtime(self):
        perf_dir = SOURCE / 'performance'
        stage_one_paths = [
            perf_dir / 'CompactAnalysisReport.kt',
            perf_dir / 'PerformanceCapability.kt',
            perf_dir / 'PerformanceCapabilityProbe.kt',
            perf_dir / 'PerformanceDiscoveryPolicy.kt',
            *sorted(perf_dir.glob('ProcessorModeTrace*.kt')),
        ]
        combined = '\n'.join(path.read_text() for path in stage_one_paths)
        self.assertNotIn('TurboThermalWatchdogService', combined)
        self.assertNotIn('TurboBootReceiver', combined)
        self.assertNotIn('TurboRuntime', combined)

    def test_turbo_scan_surfaces_performance_capability_results(self):
        main = (SOURCE / 'MainActivity.kt').read_text()
        self.assertIn('PerformanceCapabilityProbe(context).readResults()', main)
        self.assertIn('snapshot.results + performance + privilege', main)

    def test_completed_scan_opens_compact_report(self):
        main = (SOURCE / 'MainActivity.kt').read_text()
        self.assertIn('CompactAnalysisReport.format(readings)', main)
        self.assertIn('showAnalysisReport(allReadings)', main)

    def test_clean_start_and_brightness_remain_separate(self):
        main = (SOURCE / 'MainActivity.kt').read_text()
        self.assertNotIn('PerformanceCapabilityProbe', (SOURCE / 'cleanstart/CleanStartJobService.kt').read_text())
        self.assertNotIn('PerformanceCapabilityProbe', (SOURCE / 'BrightnessService.kt').read_text())
        self.assertNotRegex(main, r'set-fixed-performance-mode-enabled\s+true')


if __name__ == '__main__':
    unittest.main(verbosity=2)
