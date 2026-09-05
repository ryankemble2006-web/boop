import json
import unittest
from pathlib import Path


class RoutineCapabilityEvidenceTest(unittest.TestCase):
    def test_live_capabilities_are_explicit_and_safe(self):
        path = Path("docs/superpowers/evidence/2026-09-05-boop-routine-capabilities.md")
        self.assertTrue(path.exists())
        text = path.read_text(encoding="utf-8")
        for required in (
            "OpenCode version:", "Skill: home-assistant-configuration",
            "hab automation create:", "hab automation list:",
            "hab automation get:", "Exact authoring read tool IDs:",
            "Decision: SAFE TO CONTINUE",
        ):
            self.assertIn(required, text)
        self.assertNotRegex(text, r"(?i)(token|secret|password)\s*[:=]\s*\S+")

        marker = "```json\n"
        payload = text.split(marker, 1)[1].split("\n```", 1)[0]
        ids = json.loads(payload)
        self.assertEqual(ids, sorted(set(ids)))
        self.assertIn("skill", ids)
        self.assertTrue(any(item.endswith("search_entities") for item in ids))
        self.assertNotIn("bash", ids)
        self.assertFalse(any(item.endswith("hab_run") for item in ids))


if __name__ == "__main__":
    unittest.main()
