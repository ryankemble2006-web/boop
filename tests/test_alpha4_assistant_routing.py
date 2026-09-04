import unittest
from pathlib import Path


class Alpha4AssistantRoutingTest(unittest.TestCase):
    def test_no_valid_targets_stays_on_local_house_path(self):
        text = Path('source/HomeAssistantClient.java').read_text(encoding='utf-8')
        self.assertIn('case NO_VALID_TARGETS:', text)
        self.assertIn('return CommandOutcome.noTarget();', text)
        no_target_case = text.split('case NO_VALID_TARGETS:', 1)[1].split('case QUERY_ANSWER:', 1)[0]
        self.assertNotIn('handleAssistantFallback', no_target_case)

    def test_assistant_discovers_configured_assist_pipeline(self):
        text = Path('source/HomeAssistantAssistantClient.java').read_text(encoding='utf-8')
        self.assertIn('assist_pipeline/pipeline/list', text)
        self.assertIn('HomeAssistantPipelineSelector.selectConversationEngine', text)


if __name__ == '__main__':
    unittest.main()
