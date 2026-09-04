import unittest
from pathlib import Path


SOURCE = Path("source/MainActivity.java").read_text(encoding="utf-8")


class SpeechRecognizerModeTest(unittest.TestCase):
    def test_default_recognizer_does_not_force_missing_on_device_language_model(self):
        self.assertNotIn("createOnDeviceSpeechRecognizer", SOURCE)
        self.assertNotIn("RecognizerIntent.EXTRA_PREFER_OFFLINE", SOURCE)
        self.assertIn("SpeechRecognizer.createSpeechRecognizer(this)", SOURCE)


if __name__ == "__main__":
    unittest.main()
