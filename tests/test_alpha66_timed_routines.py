import unittest
from pathlib import Path


class Alpha66TimedRoutinesIntegrationTest(unittest.TestCase):
    def test_main_intercepts_timed_flow_before_normal_router(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('private BoopTimedRoutineFlow timedRoutineFlow;', main)
        self.assertIn('timedRoutineFlow.process(transcript, LocalDateTime.now())', main)
        timed = main.index('timedRoutineFlow.process(transcript, LocalDateTime.now())')
        normal = main.index('commandRouter.process(transcript)')
        self.assertLess(timed, normal)
        self.assertIn('speakThenListen("Once or recurring?")', main)

    def test_once_uses_ha_assist_only_not_direct_media_or_opencode(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        client = Path('source/HomeAssistantClient.java').read_text(encoding='utf-8')
        self.assertIn('haClient.processTimed(timed.haCommand())', main)
        self.assertIn('CommandOutcome processTimed(String text)', client)
        start = client.index('CommandOutcome processTimed(String text)')
        end = client.index('private static HomeAssistantResponse postConversation', start)
        timed_method = client[start:end]
        self.assertIn('postConversation(', timed_method)
        self.assertNotIn('lightColour.setColour', timed_method)
        self.assertNotIn('directMedia.processIfMedia', timed_method)
        self.assertNotIn('generalAssistant', timed_method)

    def test_question_automatically_listens_for_the_one_word_answer(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('private boolean listenAfterTts = false;', main)
        self.assertIn('private void speakThenListen(String text)', main)
        self.assertIn('listenAfterTts = true;', main)
        self.assertIn('private void finishTtsUtterance()', main)
        finish = main.index('private void finishTtsUtterance()')
        next_method = main.index('\n    private void ', finish + 20)
        finish_body = main[finish:next_method]
        self.assertIn('listenAfterTts = false;', finish_body)
        self.assertIn('beginTapToSpeak();', finish_body)

    def test_recurring_is_a_safe_future_seam_not_tool_enablement(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        bridge = Path('bridge/boop_wyoming_bridge.py').read_text(encoding='utf-8')
        self.assertIn('RECURRING_REQUESTED', main)
        self.assertIn('Recurring routines need setup first.', main)
        self.assertIn('disabled_tools', bridge)
        self.assertIn('"tools": tool_map', bridge)


if __name__ == '__main__':
    unittest.main()
