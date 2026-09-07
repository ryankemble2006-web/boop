import pathlib
import re
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]


class WallOpenAiRelayBuildTests(unittest.TestCase):
    def read(self, relative):
        return (ROOT / relative).read_text(encoding="utf-8")

    def test_gradle_reads_private_relay_values_from_environment(self):
        gradle = self.read("source/app-build.gradle")
        self.assertIn("System.getenv('BOOP_RELAY_URL') ?: ''", gradle)
        self.assertIn("System.getenv('BOOP_RELAY_TOKEN') ?: ''", gradle)
        self.assertIn("buildConfigField 'String', 'BOOP_RELAY_URL'", gradle)
        self.assertIn("buildConfigField 'String', 'BOOP_RELAY_TOKEN'", gradle)
        self.assertIn("buildConfig true", gradle)

    def test_configuration_less_build_is_explicitly_supported(self):
        gradle = self.read("source/app-build.gradle")
        self.assertGreaterEqual(gradle.count("?: ''"), 2)
        config = self.read("source/OpenAiRelayConfig.java")
        client = self.read("source/OpenAiRelayAssistantClient.java")
        self.assertIn("boolean configured()", config)
        self.assertIn("CommandOutcome.assistantSetupRequired()", client)

    def test_workflow_injects_only_actions_relay_secrets(self):
        workflow = self.read(".github/workflows/build-boop-wall-free-chat.yml")
        self.assertIn("BOOP_RELAY_URL: ${{ secrets.BOOP_RELAY_URL }}", workflow)
        self.assertIn("BOOP_RELAY_TOKEN: ${{ secrets.BOOP_RELAY_TOKEN }}", workflow)
        self.assertNotIn("OPENAI_API_KEY: ${{", workflow)
        self.assertIn("npm test", workflow)
        self.assertIn("OpenAiRelayAssistantClientHarness", workflow)
        self.assertIn("versionCode='34'", workflow)
        self.assertIn("versionName='0.4.14-wall-native-chat'", workflow)

    def test_production_and_public_config_contain_no_embedded_secrets(self):
        paths = [
            "source/OpenAiRelayConfig.java",
            "source/OpenAiRelayAssistantClient.java",
            "source/OpenAiRelayOkHttpTransport.java",
            "source/app-build.gradle",
            "scripts/patch-wall-openai-relay.py",
            "relay/cloudflare/src/index.js",
            "relay/cloudflare/wrangler.toml",
            ".github/workflows/build-boop-wall-free-chat.yml",
        ]
        combined = "\n".join(self.read(path) for path in paths)
        self.assertIsNone(re.search(r"sk-(?:proj-)?[A-Za-z0-9_-]{20,}", combined))
        self.assertNotIn("openai-test-secret", combined)
        self.assertNotIn("relay-test-secret", combined)
        self.assertNotRegex(combined, r"BOOP_RELAY_TOKEN\s*=\s*['\"][^'\"]+['\"]")


if __name__ == "__main__":
    unittest.main()
