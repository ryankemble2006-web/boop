import unittest
from pathlib import Path


class Alpha61ThinkingPuppetTest(unittest.TestCase):
    def test_main_tracks_thinking_and_prevents_idle_sleep(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('private boolean thinking = false;', main)
        self.assertIn('if (listening || thinking)', main)

    def test_main_wires_assistant_lifecycle_to_face(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('new BoopCommandRouter.AssistantActivity()', main)
        self.assertIn('onAssistantStarted()', main)
        self.assertIn('onAssistantFinished()', main)
        self.assertIn('startAssistantThinking()', main)
        self.assertIn('stopAssistantThinking()', main)
        self.assertIn('face.startThinking()', main)
        self.assertIn('face.stopThinking()', main)

    def test_face_has_repeating_nonverbal_thinking_puppetry(self):
        face = Path('source/BoopFaceView.java').read_text(encoding='utf-8')
        self.assertIn('void startThinking()', face)
        self.assertIn('void stopThinking()', face)
        self.assertIn('ValueAnimator.INFINITE', face)
        self.assertIn('thinkingAnimator', face)
        self.assertIn('resetPuppetTransform()', face)
        self.assertIn('setAlpha(1f)', face)

    def test_thinking_state_never_speaks_a_status_phrase(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        start = main.find('private void startAssistantThinking()')
        stop = main.find('private void stopAssistantThinking()')
        self.assertGreaterEqual(start, 0)
        self.assertGreater(stop, start)
        lifecycle_end = main.find('private ', stop + len('private '))
        lifecycle = main[start:lifecycle_end if lifecycle_end >= 0 else len(main)]
        self.assertNotIn('speak(', lifecycle)
        self.assertNotIn('"I\'m thinking', lifecycle)
        self.assertNotIn('"Thinking', lifecycle)

    def test_sacred_direct_media_client_is_unchanged(self):
        client = Path('source/HomeAssistantClient.java').read_text(encoding='utf-8')
        self.assertIn('directMedia.processIfMedia', client)
        self.assertLess(client.index('directMedia.processIfMedia'), client.index('postConversation('))


if __name__ == '__main__':
    unittest.main()
