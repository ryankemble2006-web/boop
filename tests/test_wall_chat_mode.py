import pathlib
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]


class WallChatModeSourceTests(unittest.TestCase):
    def read(self, relative):
        return (ROOT / relative).read_text(encoding="utf-8")

    def test_relay_protocol_sources_exist(self):
        config = self.read("source/OpenAiRelayConfig.java")
        client = self.read("source/OpenAiRelayAssistantClient.java")
        transport = self.read("source/OpenAiRelayOkHttpTransport.java")
        outcome = self.read("source/CommandOutcome.java")
        self.assertIn("BuildConfig.BOOP_RELAY_URL", config)
        self.assertIn("BuildConfig.BOOP_RELAY_TOKEN", config)
        self.assertIn("connectTimeout(5", transport)
        self.assertIn("callTimeout(30", transport)
        self.assertIn('header("Authorization", "Bearer " + config.token)', transport)
        for status in (
            "ASSISTANT_SETUP_REQUIRED",
            "ASSISTANT_AUTH_REQUIRED",
            "ASSISTANT_QUOTA",
            "ASSISTANT_RATE_LIMIT",
            "ASSISTANT_TIMEOUT",
            "ASSISTANT_SERVICE",
        ):
            self.assertIn(status, outcome)
        self.assertIn("conversationId = response.conversationId", client)

    def test_relay_sources_do_not_embed_provider_credentials(self):
        combined = "\n".join(
            self.read(path)
            for path in (
                "source/OpenAiRelayConfig.java",
                "source/OpenAiRelayAssistantClient.java",
                "source/OpenAiRelayOkHttpTransport.java",
            )
        )
        self.assertNotIn("sk-proj-", combined)
        self.assertNotIn("OPENAI_API_KEY =", combined)


if __name__ == "__main__":
    unittest.main()
