"""Processor-mode before/after tracer contracts: read-only, local, and non-resident."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'app/src/main/java/com/boop/shieldturbo'
PERF = SOURCE / 'performance'
ANDROID = '{http://schemas.android.com/apk/res/android}'


class ProcessorModeTraceContractTest(unittest.TestCase):
    def test_trace_probe_uses_only_already_trusted_local_adb(self):
        probe_path = PERF / 'ProcessorModeTraceProbe.kt'
        self.assertTrue(probe_path.exists(), 'processor trace probe must exist')
        probe = probe_path.read_text()
        self.assertIn('LocalBridge(context)', probe)
        self.assertIn('withTrustedAdb', probe)
        self.assertIn('ProcessorModeTraceCapture.capture', probe)
        self.assertNotIn('withAdb(', probe)

    def test_trace_sources_contain_no_performance_write_commands(self):
        trace = '\n'.join(
            path.read_text() for path in PERF.glob('ProcessorModeTrace*.kt')
        ).lower()
        for forbidden in (
            'settings put', 'settings delete', 'setprop', 'cmd power set',
            'echo >', 'tee ', 'set-fixed-performance-mode-enabled true'
        ):
            self.assertNotIn(forbidden, trace)

    def test_baseline_is_private_local_state_and_codec_backed(self):
        store_path = PERF / 'ProcessorModeTraceStore.kt'
        self.assertTrue(store_path.exists(), 'processor trace store must exist')
        store = store_path.read_text()
        self.assertIn('getSharedPreferences("processor_mode_trace"', store)
        self.assertIn('ProcessorModeTraceCodec.encode', store)
        self.assertIn('ProcessorModeTraceCodec.decode', store)
        self.assertIn('optimized_snapshot', store)

    def test_turbo_page_exposes_two_step_trace_flow_and_compact_diff(self):
        main = (SOURCE / 'MainActivity.kt').read_text()
        self.assertIn('R.string.processor_trace', main)
        self.assertIn('showProcessorTracePrompt()', main)
        self.assertIn('captureProcessorBaseline()', main)
        self.assertIn('captureProcessorMax()', main)
        self.assertIn('ProcessorModeTraceReport.format(changes)', main)
        self.assertIn('trace = worker.submit', main)

    def test_trace_adds_no_service_receiver_or_permission(self):
        manifest = ET.parse(ROOT / 'app/src/main/AndroidManifest.xml').getroot()
        app = manifest.find('application')
        services = {s.get(ANDROID + 'name') for s in app.findall('service')}
        receivers = {r.get(ANDROID + 'name') for r in app.findall('receiver')}
        permissions = {p.get(ANDROID + 'name') for p in manifest.findall('uses-permission')}
        self.assertFalse(any(name and '.performance.' in name for name in services))
        self.assertFalse(any(name and '.performance.' in name for name in receivers))
        self.assertNotIn('android.permission.FOREGROUND_SERVICE', permissions)

    def test_trace_does_not_touch_clean_start_or_brightness(self):
        clean = (SOURCE / 'cleanstart/CleanStartJobService.kt').read_text()
        bright = (SOURCE / 'BrightnessService.kt').read_text()
        self.assertNotIn('ProcessorModeTrace', clean)
        self.assertNotIn('ProcessorModeTrace', bright)


if __name__ == '__main__':
    unittest.main(verbosity=2)
