#!/usr/bin/env python3
"""Wire the optional native OpenAI relay into the already-reviewed Wall chat-mode patch.

This runs after patch-wall-chat-mode.py so the physically tested long-hold/menu
implementation stays isolated. Every anchor must match exactly once.
"""
from pathlib import Path
import sys

TARGET = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
MARKER = '    // BOOP_OPENAI_RELAY_V1: native chat is fallback-only after local NO_MATCH.\n'


def replace_once(text, old, new):
    count = text.count(old)
    if count != 1:
        raise ValueError(f'openai relay patch: expected one source anchor, found {count}: {old[:100]!r}')
    return text.replace(old, new, 1)


def patch_text(text):
    if MARKER in text:
        required = (
            'private OpenAiRelayAssistantClient nativeChatClient;',
            'new OpenAiRelayOkHttpTransport()',
            'chatModeButton(BoopChatMode.NATIVE_CHAT, "ChatGPT")',
            'commandRouter.processWithAssistant(transcript, nativeChatClient::ask)',
        )
        for fragment in required:
            if text.count(fragment) != 1:
                raise ValueError('openai relay patch: incomplete or altered previously patched source')
        return text

    text = replace_once(
        text,
        '    private BoopChatModeStore chatModeStore;\n',
        MARKER
        + '    private BoopChatModeStore chatModeStore;\n'
        + '    private OpenAiRelayAssistantClient nativeChatClient;\n')

    text = replace_once(
        text,
        '        chatModeStore = new BoopChatModeStore(this);\n',
        '        chatModeStore = new BoopChatModeStore(this);\n'
        '        nativeChatClient = new OpenAiRelayAssistantClient(\n'
        '                OpenAiRelayConfig.fromBuildConfig(), new OpenAiRelayOkHttpTransport());\n')

    text = replace_once(
        text,
        '        choices.addView(chatModeButton(BoopChatMode.OPENCODE, "OpenCode"));\n'
        '        choices.addView(chatModeButton(BoopChatMode.FREE_CHAT, "Free Chat"));\n',
        '        choices.addView(chatModeButton(BoopChatMode.OPENCODE, "OpenCode"));\n'
        '        choices.addView(chatModeButton(BoopChatMode.NATIVE_CHAT, "ChatGPT"));\n'
        '        choices.addView(chatModeButton(BoopChatMode.FREE_CHAT, "Free Chat"));\n')

    text = replace_once(
        text,
        '                "Free Chat opens ChatGPT in your browser and copies your question for you to paste. Its own limits apply.",\n',
        '                "ChatGPT answers inside BOOP when its private relay is set up. Free Chat opens the browser and copies your question.",\n')

    text = replace_once(
        text,
        '            CommandOutcome outcome = commandRouter.process(transcript, () ->\n'
        '                    requestChatRevision == chatModeRevision && chatModeStore.load() == BoopChatMode.OPENCODE);\n',
        '            CommandOutcome outcome;\n'
        '            if (requestChatMode == BoopChatMode.NATIVE_CHAT) {\n'
        '                outcome = commandRouter.processWithAssistant(transcript, nativeChatClient::ask);\n'
        '            } else {\n'
        '                outcome = commandRouter.process(transcript, () ->\n'
        '                        requestChatRevision == chatModeRevision\n'
        '                                && chatModeStore.load() == BoopChatMode.OPENCODE);\n'
        '            }\n')
    return text


if __name__ == '__main__':
    path = Path(sys.argv[1]) if len(sys.argv) > 1 else TARGET
    original = path.read_text(encoding='utf-8')
    result = patch_text(original)
    temporary = path.with_suffix('.java.openai-relay-tmp')
    temporary.write_text(result, encoding='utf-8')
    temporary.replace(path)
