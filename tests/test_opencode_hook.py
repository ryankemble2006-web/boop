import unittest
from pathlib import Path


class OpenCodeHookTest(unittest.TestCase):
    def setUp(self):
        self.path = Path('setup/opencode/10-boop-chatgpt.sh')
        self.text = self.path.read_text(encoding='utf-8') if self.path.exists() else ''

    def test_hook_uses_internal_opencode_and_wyoming_ports(self):
        self.assertIn('127.0.0.1:4096', self.text)
        self.assertIn('10400', self.text)
        self.assertNotIn('supervisor/network', self.text)
        self.assertNotIn('4096/tcp:', self.text)

    def test_hook_uses_persistent_venv_and_pinned_dependencies(self):
        self.assertIn('/data/venvs/boop-wyoming', self.text)
        self.assertIn('wyoming==1.10.0', self.text)
        self.assertIn('aiohttp==3.14.3', self.text)

    def test_hook_detaches_and_guards_duplicate_processes(self):
        self.assertIn('kill -0', self.text)
        self.assertIn('nohup', self.text)
        self.assertIn('setsid', self.text)
        self.assertIn('/data/boop-wyoming/bridge.pid', self.text)
        self.assertIn('/data/boop-wyoming/bridge.log', self.text)

    def test_hook_embeds_the_bridge_payload(self):
        self.assertIn("cat >\"$BRIDGE\" <<'PY'", self.text)
        self.assertIn('class BoopWyomingHandler', self.text)
        self.assertIn('BOOP OpenCode', self.text)
        self.assertIn('disabled_tools(await self.tool_ids())', self.text)


if __name__ == '__main__':
    unittest.main()
