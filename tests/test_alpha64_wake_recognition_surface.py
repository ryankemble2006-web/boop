import unittest
from pathlib import Path


class Alpha64WakeRecognitionSurfaceTest(unittest.TestCase):
    def test_wake_recognition_uses_supplied_audio_extras(self):
        path = Path('source/BoopWakeRecognitionIntent.java')
        self.assertTrue(path.exists(), 'BoopWakeRecognitionIntent.java is missing')
        text = path.read_text(encoding='utf-8')
        for extra in (
            'RecognizerIntent.EXTRA_AUDIO_SOURCE',
            'RecognizerIntent.EXTRA_AUDIO_SOURCE_CHANNEL_COUNT',
            'RecognizerIntent.EXTRA_AUDIO_SOURCE_ENCODING',
            'RecognizerIntent.EXTRA_AUDIO_SOURCE_SAMPLING_RATE',
        ):
            self.assertIn(extra, text)
        self.assertIn('SAMPLE_RATE_HZ = 16_000', text)
        self.assertIn('CHANNEL_COUNT = 1', text)
        self.assertIn('AudioFormat.ENCODING_PCM_16BIT', text)

    def test_tap_path_keeps_default_android_recognizer(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('SpeechRecognizer.createSpeechRecognizer(this)', main)
        self.assertNotIn('createOnDeviceSpeechRecognizer', main)

    def test_audio_session_has_explicit_finish_and_close_ownership(self):
        path = Path('source/BoopWakeAudioSession.java')
        self.assertTrue(path.exists(), 'BoopWakeAudioSession.java is missing')
        text = path.read_text(encoding='utf-8')
        self.assertIn('ParcelFileDescriptor audioSource()', text)
        self.assertIn('void finishCapture()', text)
        self.assertIn('void close()', text)
        self.assertIn('AtomicBoolean', text)


if __name__ == '__main__':
    unittest.main()
