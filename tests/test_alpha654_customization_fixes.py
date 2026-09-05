import unittest
from pathlib import Path


class Alpha654CustomizationFixesTests(unittest.TestCase):
    def test_voice_settings_contains_persisted_wake_sensitivity_slider(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('Wake sensitivity', main)
        self.assertIn('BoopWakeSensitivity.PROGRESS_MAX', main)
        self.assertIn('BoopWakeSensitivity.loadProgress(this)', main)
        self.assertIn('BoopWakeSensitivity.saveProgress(MainActivity.this, progress)', main)

    def test_default_sensitivity_is_exactly_alpha653_and_slider_is_monotonic(self):
        tuning = Path('source/BoopWakeSensitivity.java').read_text(encoding='utf-8')
        self.assertIn('DEFAULT_PROGRESS = 50', tuning)
        self.assertIn('DEFAULT_KEYWORD_SCORE = 1.5f', tuning)
        self.assertIn('MIN_KEYWORD_SCORE', tuning)
        self.assertIn('MAX_KEYWORD_SCORE', tuning)
        self.assertIn('scoreFromProgress', tuning)

    def test_spotter_uses_persisted_sensitivity_without_changing_threshold(self):
        spotter = Path('source/BoopSherpaWakeSpotter.java').read_text(encoding='utf-8')
        self.assertIn('BoopWakeSensitivity.keywordScore(context)', spotter)
        self.assertIn('config.setKeywordsThreshold(0.25f);', spotter)
        self.assertNotIn('config.setKeywordsScore(1.5f);', spotter)

    def test_closing_settings_reloads_spotter_before_rearming(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        reload_call = 'wakeWordController.reloadSensitivity();'
        rearm_call = 'wakeCoordinator.setVoiceSettingsOpen(false);'
        self.assertIn(reload_call, main)
        self.assertIn(rearm_call, main)
        self.assertLess(main.index(reload_call), main.index(rearm_call))

    def test_expected_cancel_error_cannot_reopen_sleeping_eyes(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        suppress = 'if (suppressNextRecognizerError) {'
        restore = 'face.animate().alpha(1.0f).setDuration(120).start();'
        on_error = main.index('public void onError(int error)')
        suppress_at = main.index(suppress, on_error)
        restore_at = main.index(restore, on_error)
        self.assertLess(suppress_at, restore_at)
        suppress_block = main[suppress_at:restore_at]
        self.assertIn('sleepFaceImmediately();', suppress_block)

    def test_wake_cue_uses_audible_media_stream_before_wake_recognition(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('new ToneGenerator(AudioManager.STREAM_MUSIC', main)
        wake_block_start = main.index('public void onWakeDetected')
        cue = main.index('playWakeAcceptedCue();', wake_block_start)
        recognition = main.index('startWakeRecognition(session);', wake_block_start)
        self.assertLess(cue, recognition)


if __name__ == '__main__':
    unittest.main()
