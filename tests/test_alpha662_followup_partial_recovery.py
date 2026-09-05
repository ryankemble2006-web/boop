from pathlib import Path
import unittest


class Alpha662FollowUpPartialRecoveryTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.main = Path("source/MainActivity.java").read_text(encoding="utf-8")
        cls.wake_patch = Path("scripts/patch-wake-partial-fallback.py").read_text(encoding="utf-8")

    def test_only_follow_up_listening_requests_partial_results(self):
        self.assertIn("private boolean followUpRecognitionActive = false;", self.main)
        self.assertIn("private String latestFollowUpPartial = null;", self.main)
        self.assertIn(
            "intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, followUpRecognitionActive);",
            self.main,
        )
        self.assertIn("followUpRecognitionActive = true;", self.main)

    def test_no_match_uses_latest_follow_up_partial_as_the_answer(self):
        self.assertIn("public void onPartialResults(Bundle partialResults)", self.main)
        self.assertIn("latestFollowUpPartial = candidate;", self.main)
        self.assertIn("error == SpeechRecognizer.ERROR_NO_MATCH", self.main)
        self.assertIn("String fallback = latestFollowUpPartial;", self.main)
        self.assertIn("handleRecognizedSpeech(fallback);", self.main)

    def test_wake_materializer_can_extend_existing_partial_results_method(self):
        self.assertIn(
            "partial_method_anchor = '    public void onPartialResults(Bundle partialResults) {\\n'",
            self.wake_patch,
        )
        self.assertIn("recognitionMode == RecognitionMode.WAKE", self.wake_patch)
        self.assertIn("wakeTranscriptAccumulator.rememberPartial", self.wake_patch)

    def test_diagnostic_toast_is_removed_from_release_fix(self):
        self.assertNotIn("Follow-up ASR", self.main)
        self.assertNotIn("showFollowUpSpeechDiagnostic", self.main)


if __name__ == "__main__":
    unittest.main()
