import unittest
from pathlib import Path


class Alpha6RoutingTest(unittest.TestCase):
    def test_main_routes_speech_through_isolated_command_router(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('private BoopCommandRouter commandRouter;', main)
        self.assertIn('new HomeAssistantGeneralAssistantClient(tokenStore, haAuth)', main)
        self.assertIn('new BoopCommandRouter(haClient, generalAssistant)', main)
        self.assertIn('commandRouter.process(transcript)', main)
        self.assertNotIn('CommandOutcome outcome = haClient.process(transcript);', main)

    def test_direct_media_stays_before_ha_conversation_processing(self):
        client = Path('source/HomeAssistantClient.java').read_text(encoding='utf-8')
        direct = client.index('directMedia.processIfMedia')
        conversation = client.index('postConversation(')
        self.assertLess(direct, conversation)

    def test_general_assistant_allows_bridge_response_window(self):
        assistant = Path('source/HomeAssistantGeneralAssistantClient.java').read_text(encoding='utf-8')
        self.assertIn('ASSISTANT_READ_TIMEOUT_MS = 50_000', assistant)
        self.assertIn('connection.setReadTimeout(ASSISTANT_READ_TIMEOUT_MS)', assistant)
        self.assertIn('connection.setConnectTimeout(CONNECT_TIMEOUT_MS)', assistant)

    def test_puppet_touch_and_member_berry_surface_is_preserved(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('interactionSurface.setOnTouchListener(this::onFaceTouch)', main)
        self.assertIn('memberBerryRunnable', main)
        self.assertIn('ViewConfiguration.getLongPressTimeout()', main)
        self.assertIn('face.playMemberBerry(memberBerryVariant++)', main)


if __name__ == '__main__':
    unittest.main()
