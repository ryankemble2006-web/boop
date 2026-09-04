import unittest
from pathlib import Path


class Alpha4AssistantRoutingTest(unittest.TestCase):
    def test_no_valid_targets_can_reach_assistant_fallback(self):
        text = Path('source/HomeAssistantClient.java').read_text(encoding='utf-8')
        self.assertIn('case NO_VALID_TARGETS:', text)
        self.assertIn('handleAssistantFallback(text, CommandOutcome.noTarget())', text)

    def test_assistant_discovers_configured_assist_pipeline(self):
        text = Path('source/HomeAssistantAssistantClient.java').read_text(encoding='utf-8')
        self.assertIn('assist_pipeline/pipeline/list', text)
        self.assertIn('HomeAssistantPipelineSelector.selectConversationEngine', text)


if __name__ == '__main__':
    unittest.main()
