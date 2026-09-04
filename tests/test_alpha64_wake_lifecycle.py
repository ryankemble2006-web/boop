import unittest
from pathlib import Path


class Alpha64WakeLifecycleTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.main = Path('source/MainActivity.java').read_text(encoding='utf-8')

    def test_foreground_session_refreshes_permission_and_support(self):
        self.assertIn('protected void onResume()', self.main)
        resume = self.main[self.main.index('protected void onResume()'):]
        self.assertIn('wakeCoordinator.beginForegroundSession()', resume)
        self.assertIn('wakeCoordinator.setMicrophonePermission(', resume)
        self.assertIn('checkSelfPermission(Manifest.permission.RECORD_AUDIO)', resume)
        self.assertIn('checkWakeRecognitionSupport()', resume)

    def test_pause_closes_wake_audio_and_disarms_before_super(self):
        self.assertIn('protected void onPause()', self.main)
        block = self.main[self.main.index('protected void onPause()'):]
        close = block.index('closeWakeAudioSession()')
        end = block.index('wakeCoordinator.endForegroundSession()')
        parent = block.index('super.onPause()')
        self.assertLess(close, end)
        self.assertLess(end, parent)

    def test_voice_settings_block_and_restore_wake(self):
        show = self.main[self.main.index('private void showVoiceSettings()'):self.main.index('private TextView voiceSettingLabel')]
        hide = self.main[self.main.index('private void hideVoiceSettings()'):self.main.index('private int dp')]
        self.assertIn('wakeCoordinator.setVoiceSettingsOpen(true)', show)
        self.assertIn('wakeCoordinator.setVoiceSettingsOpen(false)', hide)

    def test_tts_blocks_wake_before_speak_and_releases_on_terminal_callbacks(self):
        self.assertIn('UtteranceProgressListener', self.main)
        speak = self.main[self.main.index('private void speak(String text)'):self.main.index('private void keepAwakeAndHideSystemUi')]
        blocker = speak.index('wakeCoordinator.onTtsStarting()')
        actual = speak.index('tts.speak(')
        self.assertLess(blocker, actual)
        self.assertIn('wakeCoordinator.onTtsFinished()', self.main)
        self.assertIn('onDone(String utteranceId)', self.main)
        self.assertIn('onError(String utteranceId)', self.main)
        self.assertIn('onStop(String utteranceId, boolean interrupted)', self.main)

    def test_tap_blocker_is_set_before_stopping_tts(self):
        begin = self.main[self.main.index('private void beginTapToSpeak()'):self.main.index('private void createRecognizer()')]
        self.assertIn('wakeCoordinator.onTapStarted()', begin)
        start = self.main[self.main.index('private void startListening()'):self.main.index('private void startWakeRecognition')]
        self.assertIn('tts.stop()', start)
        # beginTapToSpeak sets the tap blocker before startListening can stop TTS.
        self.assertLess(begin.index('wakeCoordinator.onTapStarted()'), begin.index('startListening()'))

    def test_permission_result_refreshes_wake_permission_and_support(self):
        block = self.main[self.main.index('if (requestCode == REQ_RECORD_AUDIO)'):]
        self.assertIn('wakeCoordinator.setMicrophonePermission(granted)', block)
        self.assertIn('checkWakeRecognitionSupport()', block)

    def test_destroy_closes_session_and_shuts_down_wake_engine(self):
        destroy = self.main[self.main.index('protected void onDestroy()'):]
        self.assertIn('closeWakeAudioSession()', destroy)
        self.assertIn('wakeCoordinator.shutdown()', destroy)
        self.assertIn('wakeCoordinator = null', destroy)
        self.assertIn('wakeWordController = null', destroy)


if __name__ == '__main__':
    unittest.main()
