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

    def test_native_mode_is_additive_and_persistent(self):
        mode = self.read("source/BoopChatMode.java")
        patch = self.read("scripts/patch-wall-openai-relay.py")
        materialize = self.read("scripts/materialize-android.sh")
        self.assertIn('NATIVE_CHAT("native_chat")', mode)
        self.assertIn('chatModeButton(BoopChatMode.NATIVE_CHAT, "ChatGPT")', patch)
        self.assertIn("commandRouter.processWithAssistant(transcript, nativeChatClient::ask)", patch)
        self.assertIn("patch-wall-chat-mode.py", materialize)
        self.assertIn("patch-wall-openai-relay.py", materialize)
        self.assertLess(materialize.index("patch-wall-chat-mode.py"), materialize.index("patch-wall-openai-relay.py"))

    def test_router_keeps_local_first_boundary(self):
        router = self.read("source/BoopCommandRouter.java")
        local_index = router.index("CommandOutcome localOutcome = local.process(text);")
        no_match_index = router.index("localOutcome.status() != CommandOutcome.Status.NO_MATCH")
        assistant_index = router.index("return selectedAssistant.ask(text);")
        self.assertLess(local_index, no_match_index)
        self.assertLess(no_match_index, assistant_index)
        self.assertIn("processWithAssistant", router)

    def test_native_chat_failures_use_short_non_house_speech(self):
        reply = self.read("source/LocalReply.java")
        expected = {
            "ASSISTANT_SETUP_REQUIRED": "Chat mode needs setting up first.",
            "ASSISTANT_AUTH_REQUIRED": "Chat mode needs reconnecting.",
            "ASSISTANT_QUOTA": "Chat mode has no allowance left right now.",
            "ASSISTANT_RATE_LIMIT": "Chat is busy. Try again in a moment.",
            "ASSISTANT_TIMEOUT": "Chat took too long to answer.",
            "ASSISTANT_SERVICE": "I can't reach chat right now.",
        }
        for status, speech in expected.items():
            self.assertIn(f"case {status}:", reply)
            self.assertIn(f'return "{speech}";', reply)
        relay_section = reply[reply.index("case ASSISTANT_SETUP_REQUIRED:"):reply.index("case ASSISTANT_FAILED:")]
        self.assertNotIn("house", relay_section.lower())

    def test_relay_sources_do_not_embed_provider_credentials(self):
        combined = "\n".join(
            self.read(path)
            for path in (
                "source/OpenAiRelayConfig.java",
                "source/OpenAiRelayAssistantClient.java",
                "source/OpenAiRelayOkHttpTransport.java",
                "scripts/patch-wall-openai-relay.py",
            )
        )
        self.assertNotIn("sk-proj-", combined)
        self.assertNotIn("OPENAI_API_KEY =", combined)


if __name__ == "__main__":
    unittest.main()
