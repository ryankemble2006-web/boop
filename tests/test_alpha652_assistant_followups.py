import unittest
from pathlib import Path


class Alpha652AssistantFollowUpTests(unittest.TestCase):
    def test_only_assistant_replies_arm_follow_up_listening(self):
        main = Path("source/MainActivity.java").read_text(encoding="utf-8")
        self.assertIn("CommandOutcome.Status.ASSISTANT_REPLY", main)
        self.assertIn("speakThenOpenAssistantFollowUp", main)
        self.assertIn("speak(LocalReply.forOutcome(outcome))", main)

    def test_follow_up_window_is_five_seconds_and_uses_normal_recognizer(self):
        main = Path("source/MainActivity.java").read_text(encoding="utf-8")
        self.assertIn("ASSISTANT_FOLLOW_UP_SILENCE_MS = 5_000L", main)
        self.assertIn("recognitionMode = RecognitionMode.TAP", main)
        self.assertIn("startListening()", main)
        self.assertNotIn("ToneGenerator", main)
        self.assertNotIn("SoundPool", main)

    def test_silence_timeout_is_graceful_and_sleeps_immediately(self):
        main = Path("source/MainActivity.java").read_text(encoding="utf-8")
        self.assertIn("finishAssistantFollowUpSilently", main)
        self.assertIn("face.goIdleBlack()", main)
        self.assertIn("presenceState.idle()", main)
        self.assertIn("suppressNextRecognizerError", main)

    def test_speech_beginning_cancels_silence_timer(self):
        main = Path("source/MainActivity.java").read_text(encoding="utf-8")
        self.assertIn("onBeginningOfSpeech()", main)
        self.assertIn("cancelAssistantFollowUpSilenceTimeout()", main)

    def test_manners_remain_local_and_do_not_reopen_follow_up(self):
        main = Path("source/MainActivity.java").read_text(encoding="utf-8")
        exit_check = "BoopConversationExitIntent.replyFor(transcript)"
        self.assertIn(exit_check, main)
        self.assertIn("speak(exitReply);", main)
        self.assertLess(main.index(exit_check), main.index("commandRouter.process(transcript)"))

    def test_timed_routines_stay_absent(self):
        main = Path("source/MainActivity.java").read_text(encoding="utf-8")
        self.assertNotIn("BoopTimedRoutineFlow", main)
        self.assertNotIn("Once or recurring?", main)


if __name__ == "__main__":
    unittest.main()
