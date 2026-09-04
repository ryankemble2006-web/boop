import unittest
from pathlib import Path


class Alpha64WakeIntegrationTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.main = Path('source/MainActivity.java').read_text(encoding='utf-8')

    def test_main_has_distinct_tap_and_wake_recognition_modes(self):
        self.assertIn('enum RecognitionMode', self.main)
        for mode in ('NONE', 'TAP', 'WAKE'):
            self.assertIn(mode, self.main)
        self.assertIn('BoopWakeSessionCoordinator', self.main)
        self.assertIn('BoopWakeTranscriptNormalizer.stripLeadingWakeWord', self.main)
        self.assertIn('BoopWakeRecognitionIntent.build', self.main)

    def test_sacred_tap_recognizer_and_existing_router_are_preserved(self):
        self.assertIn('SpeechRecognizer.createSpeechRecognizer(this)', self.main)
        self.assertNotIn('createOnDeviceSpeechRecognizer', self.main)
        self.assertIn('commandRouter.process(transcript)', self.main)

    def test_existing_local_voice_order_stays_before_house_router(self):
        settings = self.main.index('BoopVoiceSettingsIntent.matches(transcript)')
        voice = self.main.index('voiceController.maybeChangeVoice(transcript)')
        router = self.main.index('commandRouter.process(transcript)')
        self.assertLess(settings, voice)
        self.assertLess(voice, router)

    def test_wake_recognition_uses_same_transcript_handler(self):
        self.assertIn('startWakeRecognition', self.main)
        self.assertIn('handleRecognizedSpeech(normalized)', self.main)
        self.assertIn('wakeAudioSession.finishCapture()', self.main)
        self.assertIn('SpeechRecognizer.ERROR_NO_MATCH', self.main)
        self.assertIn('SpeechRecognizer.ERROR_SPEECH_TIMEOUT', self.main)

    def test_supplied_audio_support_is_probed_without_becoming_on_device_only(self):
        self.assertIn('checkRecognitionSupport', self.main)
        self.assertIn('ParcelFileDescriptor.createPipe()', self.main)
        self.assertIn('Build.VERSION.SDK_INT', self.main)
        self.assertIn('Build.VERSION_CODES.TIRAMISU', self.main)

    def test_diagnostic_apk_surfaces_wake_recognizer_result_error_and_partial(self):
        patch = Path('scripts/patch-wake-diagnostic.py').read_text(encoding='utf-8')
        materializer = Path('scripts/materialize-android.sh').read_text(encoding='utf-8')
        self.assertIn('WAKE ERR ', patch)
        self.assertIn('WAKE HEARD ', patch)
        self.assertIn('WAKE PART ', patch)
        self.assertIn('RecognizerIntent.EXTRA_PARTIAL_RESULTS, true', patch)
        self.assertIn('Toast.LENGTH_LONG', patch)
        self.assertIn('python3 scripts/patch-wake-diagnostic.py', materializer)


if __name__ == '__main__':
    unittest.main()
