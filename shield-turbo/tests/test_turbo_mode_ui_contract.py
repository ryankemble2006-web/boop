"""Remote-first persistent TURBO mode UI contracts."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'app/src/main/java/com/boop/shieldturbo'
PERF = SOURCE / 'performance'
MAIN = SOURCE / 'MainActivity.kt'
STRINGS = ROOT / 'app/src/main/res/values/strings.xml'


class TurboModeUiContractTest(unittest.TestCase):
    def test_remote_panel_exposes_big_mode_control_and_four_plain_readouts(self):
        path = PERF / 'TurboModePanel.kt'
        self.assertTrue(path.exists(), 'TurboModePanel must exist')
        text = path.read_text()
        self.assertIn('modeButton', text)
        for phrase in ('Processor mode:', 'Thermal state:', 'Watchdog:', 'Last change:'):
            self.assertIn(phrase, text)
        self.assertIn('TURBO MODE: ON', STRINGS.read_text())
        self.assertIn('TURBO MODE: OFF', STRINGS.read_text())

    def test_first_enable_explains_persistence_stock_controls_watchdog_and_severe_fallback(self):
        strings = STRINGS.read_text().lower()
        for phrase in ('persists across reboot', 'stock performance controls', 'thermal watchdog', 'severe'):
            self.assertIn(phrase, strings)

    def test_panel_runs_transactions_off_ui_thread_and_controls_watchdog(self):
        text = (PERF / 'TurboModePanel.kt').read_text()
        self.assertIn('ExecutorService', text)
        self.assertIn('worker.submit', text)
        self.assertIn('TurboRuntime', text)
        self.assertIn('.enable()', text)
        self.assertIn('.disable(', text)
        self.assertIn('TurboThermalWatchdogService.start', text)
        self.assertIn('TurboThermalWatchdogService.stop', text)
        self.assertIn('TurboPhase.TURBO_VERIFIED', text)

    def test_panel_rolls_back_if_watchdog_cannot_start(self):
        text = (PERF / 'TurboModePanel.kt').read_text()
        self.assertIn('thermal watchdog could not start', text)
        self.assertIn('runtime.disable(', text)

    def test_main_places_persistent_control_before_diagnostics_and_keeps_remote_focus_path(self):
        main = MAIN.read_text()
        self.assertIn('TurboModePanel', main)
        self.assertIn('turboModePanel', main)
        self.assertLess(main.index('turboModePanel = TurboModePanel'), main.index('analyseButton = button'))
        self.assertIn('turboModePanel.modeButton.nextFocusDownId = analyseButton.id', main)
        self.assertIn('analyseButton.nextFocusUpId = turboModePanel.modeButton.id', main)
        self.assertIn('turboModePanel.modeButton.requestFocus()', main)

    def test_one_shot_actuator_proof_remains_available_beside_persistent_mode(self):
        main = MAIN.read_text()
        self.assertIn('processorActuatorButton = button(R.string.processor_actuator_proof)', main)
        self.assertIn('showProcessorActuatorPrompt()', main)


if __name__ == '__main__':
    unittest.main(verbosity=2)
