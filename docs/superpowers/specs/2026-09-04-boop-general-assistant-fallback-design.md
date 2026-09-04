# BOOP General Assistant Fallback Design

**Date:** 2026-09-04
**Base checkpoint:** `776e75c` (`alpha3-ha-device-identity`)

## Goal

Give BOOP a general-assistant path without slowing or weakening the local Home Assistant behaviour that already works well.

## Behaviour

1. Every transcript goes through the existing `HomeAssistantClient.process(text)` path first.
2. Existing Home Assistant outcomes remain authoritative. Success, offline target, missing target, unreachable HA, authentication failure, and other handled failures do **not** invoke the cloud assistant.
3. Only `CommandOutcome.Status.NO_MATCH` falls through to the general assistant.
4. BOOP discovers the configured Home Assistant conversation agents through the authenticated `/api/websocket` command `conversation/agent/list` rather than hard-coding an entity id.
5. BOOP ignores the built-in Home Assistant agent when choosing the fallback. It prefers an agent whose id or name contains `openai`, `chatgpt`, or `gpt`; otherwise it uses the first non-built-in agent returned by Home Assistant.
6. The chosen agent id is cached in memory for the running app session.
7. BOOP calls `/api/conversation/process` again with the original transcript plus `agent_id`, `device_id`, language, and the prior `conversation_id` when one exists.
8. BOOP speaks the response speech returned by Home Assistant and retains the returned `conversation_id` so follow-up questions stay in the same conversation.
9. The assistant conversation id is in-memory only. Restarting BOOP starts a fresh assistant conversation.

## Failure behaviour

- If no non-built-in conversation agent is configured, BOOP says it can control the house but does not have an assistant connected yet.
- If the assistant path cannot be reached or returns an unusable reply, BOOP gives a short plain-English assistant failure response.
- If Home Assistant rejects authorization, BOOP uses the existing reconnect path.
- A broken or unavailable AI service must never disable the existing local Home Assistant control path.

## UI and interaction constraints

- No new settings screen, button, mode, or wake phrase.
- Single tap remains tap-to-speak.
- Long hold remains the hidden N64 member-berry puppet upset behaviour from `776e75c`.
- Existing BOOP face/presence behaviour is unchanged.

## Security

- Do not embed an OpenAI API key in the APK.
- Reuse the existing Home Assistant OAuth/access-token flow.
- The OpenAI/general-assistant credential remains configured inside Home Assistant.

## Non-goals

- This does not import the current ChatGPT app conversation or ChatGPT memory into BOOP.
- This does not replace local Home Assistant command resolution with an LLM.
- This does not add a user-facing assistant picker in this build.
