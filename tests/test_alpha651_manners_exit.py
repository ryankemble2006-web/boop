import unittest
from pathlib import Path


class Alpha651MannersExitTests(unittest.TestCase):
    def test_conversation_exit_is_checked_before_house_routing(self):
        main = Path("source/MainActivity.java").read_text(encoding="utf-8")
        exit_check = "BoopConversationExitIntent.replyFor(transcript)"
        self.assertIn(exit_check, main)
        self.assertLess(
            main.index(exit_check),
            main.index("if (!tokenStore.hasConnection())"),
        )

    def test_rollback_contains_no_timed_routine_interceptor(self):
        main = Path("source/MainActivity.java").read_text(encoding="utf-8")
        self.assertNotIn("BoopTimedRoutineFlow", main)
        self.assertNotIn("Once or recurring?", main)
        self.assertNotIn("Recurring routines need setup first.", main)


if __name__ == "__main__":
    unittest.main()
