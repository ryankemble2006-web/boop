# BOOP General Assistant Fallback Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Preserve BOOP's existing fast Home Assistant command path and route only unmatched speech to a configured Home Assistant AI conversation agent with multi-turn context.

**Architecture:** `MainActivity` continues to call the existing `HomeAssistantClient` first. A new `HomeAssistantAssistantClient` is invoked only for `NO_MATCH`; it discovers a non-built-in conversation agent via Home Assistant's WebSocket API, then uses the existing REST conversation endpoint with `agent_id` and `conversation_id`. No OpenAI credential is stored in BOOP.

**Tech Stack:** Android Java 17, Home Assistant REST + WebSocket APIs, OkHttp 4.12.0, org.json, JUnit 4.

**Spec:** `docs/superpowers/specs/2026-09-04-boop-general-assistant-fallback-design.md`

## Global Constraints

- Base behaviour is checkpoint `776e75c`.
- Existing local Home Assistant handling stays first and unchanged.
- Only `CommandOutcome.Status.NO_MATCH` may invoke the general assistant.
- Reuse Home Assistant authentication; never embed an OpenAI API key.
- Retain assistant `conversation_id` in memory for follow-ups.
- Single tap, long-hold member berry, and BOOP face behaviour remain unchanged.

---

### Task 1: Lock fallback protocol with failing tests

**Files:**
- Modify: `source-test/HomeAssistantConversationRequestTest.java`
- Create: `source-test/AssistantFallbackPolicyTest.java`
- Create: `source-test/HomeAssistantAgentSelectorTest.java`
- Create: `source-test/HomeAssistantAssistantReplyParserTest.java`

**Interfaces:**
- Produces desired signatures for `HomeAssistantConversationRequest.build(...)`, `AssistantFallbackPolicy.shouldAskAssistant(...)`, `HomeAssistantAgentSelector.select(...)`, and `HomeAssistantAssistantReplyParser.parse(...)`.

- [ ] **Step 1: Write tests that require assistant fields in a conversation request**

```java
JSONObject body = HomeAssistantConversationRequest.build(
        "why is the sky blue?", "en-GB", "ha-device-123",
        "conversation.openai_conversation", "thread-7");
assertEquals("conversation.openai_conversation", body.getString("agent_id"));
assertEquals("thread-7", body.getString("conversation_id"));
```

- [ ] **Step 2: Write tests proving only `NO_MATCH` falls through**

```java
assertTrue(AssistantFallbackPolicy.shouldAskAssistant(CommandOutcome.Status.NO_MATCH));
assertFalse(AssistantFallbackPolicy.shouldAskAssistant(CommandOutcome.Status.SUCCESS));
assertFalse(AssistantFallbackPolicy.shouldAskAssistant(CommandOutcome.Status.UNREACHABLE));
```

- [ ] **Step 3: Write agent-selection tests**

```java
assertEquals(
        "conversation.openai_conversation",
        HomeAssistantAgentSelector.select(List.of(
                new HomeAssistantAgentSelector.Agent("home_assistant", "Home Assistant"),
                new HomeAssistantAgentSelector.Agent("conversation.other", "Other"),
                new HomeAssistantAgentSelector.Agent("conversation.openai_conversation", "OpenAI Conversation"))));
```

- [ ] **Step 4: Write response-parser test for speech and conversation id**

```java
HomeAssistantAssistantReply reply = HomeAssistantAssistantReplyParser.parse(
        "{\"conversation_id\":\"thread-8\",\"response\":{\"response_type\":\"query_answer\",\"speech\":{\"plain\":{\"speech\":\"Because molecules scatter blue light.\"}},\"data\":{}}}");
assertEquals("Because molecules scatter blue light.", reply.speech());
assertEquals("thread-8", reply.conversationId());
```

- [ ] **Step 5: Run Android unit tests and verify RED**

Run: `gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace`

Expected: compilation/test failure because the new production APIs do not exist yet.

### Task 2: Implement pure assistant protocol helpers

**Files:**
- Modify: `source/HomeAssistantConversationRequest.java`
- Create: `source/AssistantFallbackPolicy.java`
- Create: `source/HomeAssistantAgentSelector.java`
- Create: `source/HomeAssistantAssistantReply.java`
- Create: `source/HomeAssistantAssistantReplyParser.java`

**Interfaces:**
- `HomeAssistantConversationRequest.build(String text, String language, String deviceId, String agentId, String conversationId)` returns a `JSONObject`.
- `AssistantFallbackPolicy.shouldAskAssistant(CommandOutcome.Status status)` returns boolean.
- `HomeAssistantAgentSelector.Agent(String id, String name)` and `HomeAssistantAgentSelector.select(List<Agent>)` return the selected id or empty string.
- `HomeAssistantAssistantReplyParser.parse(String json)` returns `HomeAssistantAssistantReply` with `speech()` and `conversationId()`.

- [ ] **Step 1: Add the request overload without changing the existing three-argument builder**

```java
static JSONObject build(String text, String language, String deviceId,
        String agentId, String conversationId) throws Exception {
    JSONObject body = build(text, language, deviceId);
    if (agentId != null && !agentId.isBlank()) body.put("agent_id", agentId);
    if (conversationId != null && !conversationId.isBlank()) body.put("conversation_id", conversationId);
    return body;
}
```

- [ ] **Step 2: Add the strict fallback policy**

```java
static boolean shouldAskAssistant(CommandOutcome.Status status) {
    return status == CommandOutcome.Status.NO_MATCH;
}
```

- [ ] **Step 3: Add deterministic agent selection**

Ignore ids `home_assistant` and `conversation.home_assistant`; prefer id/name containing `openai`, `chatgpt`, or `gpt`, otherwise choose the first remaining agent.

- [ ] **Step 4: Parse assistant speech plus top-level `conversation_id`**

Reuse `HomeAssistantResponseParser.parse(json)` for the speech, and read `conversation_id` from the top-level JSON object.

- [ ] **Step 5: Run Android unit tests and verify GREEN**

Expected: all unit tests pass.

### Task 3: Add Home Assistant AI conversation client

**Files:**
- Create: `source/AssistantOutcome.java`
- Create: `source/HomeAssistantAssistantClient.java`

**Interfaces:**
- `AssistantOutcome.Status`: `REPLY`, `NO_AGENT`, `UNREACHABLE`, `AUTH_REQUIRED`, `FAILED`.
- `HomeAssistantAssistantClient(SecureTokenStore tokenStore, HomeAssistantAuth auth)`.
- `AssistantOutcome ask(String text)`.
- `void close()`.

- [ ] **Step 1: Discover agents over `/api/websocket`**

Use OkHttp WebSocket. Authenticate with the same fresh HA access token; after `auth_ok`, send:

```json
{"id":1,"type":"conversation/agent/list","language":"en-GB"}
```

Convert the returned agents to `HomeAssistantAgentSelector.Agent` and cache the selected id.

- [ ] **Step 2: Post unmatched speech to `/api/conversation/process`**

Use the request overload with transcript, current BCP47 language, BOOP HA `device_id`, selected `agent_id`, and the stored assistant `conversation_id`.

- [ ] **Step 3: Retain conversation context**

When the response contains a non-empty `conversation_id`, store it for the next unmatched transcript. Return `AssistantOutcome.reply(speech)` only for a non-empty spoken reply.

- [ ] **Step 4: Map auth/network/no-agent failures without touching local HA state**

401/403 -> `AUTH_REQUIRED`; WebSocket/HTTP I/O -> `UNREACHABLE`; no selectable agent -> `NO_AGENT`; malformed/empty response -> `FAILED`.

### Task 4: Wire fallback into BOOP speech handling

**Files:**
- Modify: `source/MainActivity.java`

**Interfaces:**
- Uses `AssistantFallbackPolicy.shouldAskAssistant(outcome.status())` and `HomeAssistantAssistantClient.ask(transcript)`.

- [ ] **Step 1: Construct `HomeAssistantAssistantClient` beside the existing HA client**

- [ ] **Step 2: Preserve local-first routing**

```java
CommandOutcome outcome = haClient.process(transcript);
if (AssistantFallbackPolicy.shouldAskAssistant(outcome.status())) {
    AssistantOutcome assistant = assistantClient.ask(transcript);
    runOnUiThread(() -> handleAssistantOutcome(assistant));
    return;
}
```

- [ ] **Step 3: Speak assistant outcomes in plain English**

`REPLY` speaks the returned answer. `NO_AGENT` explains that house control works but no assistant is connected. `UNREACHABLE`/`FAILED` explain that house control still works but the assistant is unavailable. `AUTH_REQUIRED` reuses the existing reconnect flow.

- [ ] **Step 4: Close the assistant client in `onDestroy()`**

### Task 5: Version, build, and verify the installable APK

**Files:**
- Modify: `source/app-build.gradle`
- Modify: `.github/workflows/build-apk.yml`

- [ ] **Step 1: Bump app version**

Set `versionCode 7` and `versionName "0.3.4-alpha4"` so the signed APK installs over the previous build.

- [ ] **Step 2: Build with permanent BOOP signing key**

Run the existing GitHub Actions build on `alpha4-general-assistant`.

- [ ] **Step 3: Verify source regression tests, Android unit tests, APK assembly, APK inspection, and clean-emulator launch all pass**

- [ ] **Step 4: Extract `app-debug.apk` from the Actions artifact and provide the raw `.apk` directly to the user**
