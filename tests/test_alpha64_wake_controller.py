import unittest
from pathlib import Path


class Alpha64WakeControllerTest(unittest.TestCase):
    def test_sherpa_spotter_uses_pinned_mobile_assets_and_boop_only(self):
        path = Path('source/BoopSherpaWakeSpotter.java')
        self.assertTrue(path.exists(), 'BoopSherpaWakeSpotter.java is missing')
        text = path.read_text(encoding='utf-8')
        self.assertIn('KeywordSpotter', text)
        self.assertIn('encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx', text)
        self.assertIn('decoder-epoch-12-avg-2-chunk-16-left-64.onnx', text)
        self.assertNotIn('decoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx', text)
        self.assertIn('joiner-epoch-12-avg-2-chunk-16-left-64.int8.onnx', text)
        self.assertIn('boop-kws/tokens.txt', text)
        self.assertIn('boop-kws/keywords.txt', text)
        self.assertIn('equalsIgnoreCase("BOOP")', text)
        self.assertIn('stream.acceptWaveform', text)
        self.assertIn('spotter.reset(stream)', text)

    def test_wake_controller_owns_one_audio_record_and_pre_roll_pipe(self):
        path = Path('source/BoopWakeWordController.java')
        self.assertTrue(path.exists(), 'BoopWakeWordController.java is missing')
        text = path.read_text(encoding='utf-8')
        self.assertIn('AudioRecord', text)
        self.assertIn('BoopPcmRingBuffer', text)
        self.assertIn('BoopWakeTriggerGate', text)
        self.assertIn('ParcelFileDescriptor.createPipe()', text)
        self.assertIn('ParcelFileDescriptor.AutoCloseOutputStream', text)
        self.assertIn('boop-wake-audio', text)
        self.assertIn('3_000L', text)
        self.assertIn('BoopWakeAudioSession', text)

    def test_wake_controller_is_transport_only_not_house_routing(self):
        text = Path('source/BoopWakeWordController.java').read_text(encoding='utf-8')
        for forbidden in ('HomeAssistant', 'OpenAI', 'BoopCommandRouter'):
            self.assertNotIn(forbidden, text)


if __name__ == '__main__':
    unittest.main()
