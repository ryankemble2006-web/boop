import unittest
from pathlib import Path


class HappyAccidentControlTest(unittest.TestCase):
    def test_control_build_only_bumps_install_version(self):
        gradle = Path('source/app-build.gradle').read_text(encoding='utf-8')
        self.assertIn('versionCode 10', gradle)
        self.assertIn('versionName "0.3.7-happy-accident-control"', gradle)

        ha_client = Path('source/HomeAssistantClient.java').read_text(encoding='utf-8')
        self.assertNotIn('HomeAssistantAssistantClient', ha_client)
        self.assertNotIn('handleAssistantFallback', ha_client)

        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertNotIn('HomeAssistantAssistantClient', main)
        self.assertIn('haClient.process(transcript)', main)


if __name__ == '__main__':
    unittest.main()
