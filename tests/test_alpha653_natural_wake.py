import unittest
from pathlib import Path


class Alpha653NaturalWakeTests(unittest.TestCase):
    def test_natural_wake_phrase_family_is_declared(self):
        raw = Path("wake-assets/boop-kws/keywords_raw.txt").read_text(encoding="utf-8")
        expected = (
            "BOOP",
            "HEY BOOP",
            "EY BOOP",
            "HI BOOP",
            "HELLO BOOP",
            "YO BOOP",
            "OI BOOP",
            "OK BOOP",
            "OKAY BOOP",
            "HEY THERE BOOP",
            "HELLO THERE BOOP",
            "WAKE UP BOOP",
            "BOOP WAKE UP",
            "COME ON BOOP",
            "YOU THERE BOOP",
            "ARE YOU THERE BOOP",
            "BOOP YOU THERE",
        )
        for phrase in expected:
            self.assertIn(f"{phrase} :1.5 #0.25 @BOOP", raw)

    def test_all_tokenized_wake_variants_collapse_to_boop(self):
        keywords = Path("wake-assets/boop-kws/keywords.txt").read_text(encoding="utf-8")
        lines = [line.strip() for line in keywords.splitlines() if line.strip()]
        self.assertGreaterEqual(len(lines), 17)
        for line in lines:
            self.assertTrue(line.endswith("@BOOP"), line)

    def test_wake_acceptance_plays_one_open_cue_before_recognition(self):
        main = Path("source/MainActivity.java").read_text(encoding="utf-8")
        wake_block = main[main.index("public void onWakeDetected"):main.index("public void onWakeFailure")]
        self.assertIn("playWakeAcceptedCue();", wake_block)
        self.assertLess(wake_block.index("playWakeAcceptedCue();"), wake_block.index("startWakeRecognition(session);"))

    def test_manners_reply_sleeps_face_after_tts(self):
        main = Path("source/MainActivity.java").read_text(encoding="utf-8")
        self.assertIn("speakThenSleep(exitReply);", main)
        self.assertIn("sleepFaceAfterTts", main)
        self.assertIn("sleepFaceImmediately();", main)

    def test_house_and_assistant_routes_keep_existing_behavior(self):
        main = Path("source/MainActivity.java").read_text(encoding="utf-8")
        self.assertIn("speakThenOpenAssistantFollowUp(LocalReply.forOutcome(outcome));", main)
        self.assertIn("speak(LocalReply.forOutcome(outcome));", main)
        self.assertNotIn("BoopTimedRoutineFlow", main)

    def test_build_is_alpha654(self):
        gradle = Path("source/app-build.gradle").read_text(encoding="utf-8")
        self.assertIn("versionCode 27", gradle)
        self.assertIn('versionName "0.4.9-alpha6.5.4"', gradle)


if __name__ == "__main__":
    unittest.main()
