import unittest
from pathlib import Path


class Alpha5DirectMediaIntegrationTest(unittest.TestCase):
    def test_direct_media_runs_before_home_assistant_conversation(self):
        text = Path('source/HomeAssistantClient.java').read_text(encoding='utf-8')
        self.assertIn('HomeAssistantDirectMediaClient', text)
        self.assertIn('directMedia.processIfMedia', text)
        self.assertLess(
            text.find('directMedia.processIfMedia'),
            text.find('postConversation('),
        )

    def test_direct_media_uses_raw_area_entities_states_and_services(self):
        path = Path('source/HomeAssistantDirectMediaClient.java')
        self.assertTrue(path.exists())
        text = path.read_text(encoding='utf-8')
        self.assertIn('/api/template', text)
        self.assertIn('area_entities', text)
        self.assertIn('/api/states', text)
        self.assertIn('/api/services/media_player/', text)
        self.assertIn('HomeAssistantMediaSelector.rank', text)

    def test_media_selection_is_command_capability_aware(self):
        direct = Path('source/HomeAssistantDirectMediaClient.java').read_text(encoding='utf-8')
        selector = Path('source/HomeAssistantMediaSelector.java').read_text(encoding='utf-8')
        self.assertIn('HomeAssistantMediaSelector.rank(areaEntities, states, command)', direct)
        self.assertIn('supported_features', selector)
        self.assertIn('supports(command', selector)

    def test_recognised_media_does_not_fall_through_to_generic_assist(self):
        direct = Path('source/HomeAssistantDirectMediaClient.java').read_text(encoding='utf-8')
        self.assertIn('return CommandOutcome.failed();', direct)
        self.assertIn('MediaCommand command = MediaCommandParser.parse(text)', direct)

    def test_puppet_surface_is_not_part_of_media_patch(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('ViewConfiguration.getLongPressTimeout()', main)
        self.assertIn('memberBerryConsumed', main)
        self.assertIn('commandRouter.process(transcript)', main)
        self.assertIn('new BoopCommandRouter(', main)


if __name__ == '__main__':
    unittest.main()
