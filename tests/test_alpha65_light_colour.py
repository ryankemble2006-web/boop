import unittest
from pathlib import Path


class Alpha65LightColourIntegrationTest(unittest.TestCase):
    def test_colour_control_runs_before_media_and_ha_conversation(self):
        client = Path('source/HomeAssistantClient.java').read_text(encoding='utf-8')
        colour = client.index('LightColourCommandParser.parseColour(text)')
        media = client.index('directMedia.processIfMedia')
        conversation = client.index('postConversation(')
        self.assertLess(colour, media)
        self.assertLess(colour, conversation)
        self.assertIn('lightColour.setColour(baseUrl, accessToken, colour)', client)

    def test_colour_client_is_room_scoped_brand_agnostic_and_direct(self):
        client = Path('source/HomeAssistantLightColourClient.java').read_text(encoding='utf-8')
        self.assertIn('/api/template', client)
        self.assertIn("area_entities('", client)
        self.assertIn('/api/states', client)
        self.assertIn('/api/services/light/turn_on', client)
        self.assertIn('HomeAssistantLightColourSelector.select', client)
        self.assertIn('HomeAssistantLightColourProtocol.serviceBody', client)
        self.assertNotIn('govee', client.lower())
        self.assertNotIn('hue', client.lower())

    def test_existing_direct_media_client_is_not_used_for_light_colour(self):
        media = Path('source/HomeAssistantDirectMediaClient.java').read_text(encoding='utf-8')
        self.assertNotIn('LightColour', media)
        self.assertNotIn('color_name', media)


if __name__ == '__main__':
    unittest.main()
