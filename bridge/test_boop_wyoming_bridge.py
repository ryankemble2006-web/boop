import unittest

from bridge.boop_wyoming_bridge import SessionRegistry, disabled_tools, extract_text


class SessionRegistryTest(unittest.IsolatedAsyncioTestCase):
    async def test_same_conversation_reuses_session(self):
        created = []

        async def create_session():
            value = f"session-{len(created) + 1}"
            created.append(value)
            return value

        registry = SessionRegistry()
        first = await registry.session_for("ha-conversation-1", create_session)
        second = await registry.session_for("ha-conversation-1", create_session)

        self.assertEqual("session-1", first)
        self.assertEqual(first, second)
        self.assertEqual(["session-1"], created)

    async def test_different_conversations_get_different_sessions(self):
        created = []

        async def create_session():
            value = f"session-{len(created) + 1}"
            created.append(value)
            return value

        registry = SessionRegistry()
        first = await registry.session_for("ha-a", create_session)
        second = await registry.session_for("ha-b", create_session)

        self.assertEqual("session-1", first)
        self.assertEqual("session-2", second)

    async def test_missing_conversation_id_does_not_reuse_session(self):
        created = []

        async def create_session():
            value = f"session-{len(created) + 1}"
            created.append(value)
            return value

        registry = SessionRegistry()
        first = await registry.session_for(None, create_session)
        second = await registry.session_for(None, create_session)

        self.assertNotEqual(first, second)
        self.assertEqual(2, len(created))


class BridgeHelpersTest(unittest.TestCase):
    def test_all_opencode_tools_are_disabled(self):
        self.assertEqual(
            {"shell": False, "edit": False, "homeassistant": False},
            disabled_tools(["shell", "edit", "homeassistant"]),
        )

    def test_extract_text_joins_text_parts(self):
        response = {
            "parts": [
                {"type": "text", "text": "Rayleigh scattering"},
                {"type": "tool", "state": {"status": "completed"}},
                {"type": "text", "text": "makes shorter blue wavelengths scatter more."},
            ]
        }
        self.assertEqual(
            "Rayleigh scattering\nmakes shorter blue wavelengths scatter more.",
            extract_text(response),
        )

    def test_extract_text_rejects_empty_reply(self):
        with self.assertRaises(ValueError):
            extract_text({"parts": [{"type": "text", "text": "   "}]})


if __name__ == "__main__":
    unittest.main()
