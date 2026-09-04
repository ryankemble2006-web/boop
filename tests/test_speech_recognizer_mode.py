import unittest
from pathlib import Path


SOURCE = Path("source/MainActivity.java").read_text(encoding="utf-8")


class SpeechRecognizerModeTest(unittest.TestCase):
    def test_default_recognizer_does_not_force_missing_on_device_language_model(self):
        self.assertNotIn("createOnDeviceSpeechRecognizer", SOURCE)
        self.assertNotIn("RecognizerIntent.EXTRA_PREFER_OFFLINE", SOURCE)
        self.assertIn("SpeechRecognizer.createSpeechRecognizer(this)", SOURCE)

    def test_no_match_reports_ready_and_speech_detection_state(self):
        self.assertIn("speechReadyForInput", SOURCE)
        self.assertIn("speechStarted", SOURCE)
        self.assertIn("onReadyForSpeech", SOURCE)
        self.assertIn("onBeginningOfSpeech", SOURCE)
        self.assertIn("Ready " + " + (speechReadyForInput ? \"yes\" : \"no\")", SOURCE)
        self.assertIn("heard speech " + " + (speechStarted ? \"yes\" : \"no\")", SOURCE)


if __name__ == "__main__":
    unittest.main()
