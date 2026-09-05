import unittest
from pathlib import Path


class Alpha662FollowUpDiagnosticsTest(unittest.TestCase):
    def test_follow_up_records_ready_begin_end_callbacks(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('private boolean followUpDiagnosticActive = false;', main)
        self.assertIn('private boolean followUpReadyForSpeech = false;', main)
        self.assertIn('private boolean followUpBeginningOfSpeech = false;', main)
        self.assertIn('private boolean followUpEndOfSpeech = false;', main)
        self.assertIn('followUpReadyForSpeech = true;', main)
        self.assertIn('followUpBeginningOfSpeech = true;', main)
        self.assertIn('followUpEndOfSpeech = true;', main)

    def test_no_match_surfaces_callback_state_without_changing_house_logic(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('showFollowUpSpeechDiagnostic(error);', main)
        self.assertIn('Follow-up ASR', main)
        self.assertIn('ready=', main)
        self.assertIn('speech=', main)
        self.assertIn('end=', main)
        self.assertIn('error=', main)
        self.assertIn('Toast.LENGTH_LONG', main)
        self.assertIn('if (failedMode == RecognitionMode.TAP)', main)


if __name__ == '__main__':
    unittest.main()
