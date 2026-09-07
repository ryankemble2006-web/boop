# BOOP Secure OpenAI Conversation Relay Design

Date: 2026-09-07
Owning app: BOOP Wall (`com.boop.alpha1`)
Owning branch: `boop-wall-free-chat-wip`

## Goal

Give BOOP normal open-ended conversational answers inside the Android puppet while preserving the existing speech input, eyes, TTS/puppetry, local Home Assistant commands and local media commands. Known local commands always run first. Only a genuine local `NO_MATCH` may leave the local path.

## Architecture

BOOP keeps its existing `BoopCommandRouter` boundary. The local `HomeAssistantClient` remains first and authoritative. A new conversational processor is used only after `NO_MATCH` when the OpenAI conversation mode is selected.

The Android app never contains an OpenAI API key. Instead it calls a small HTTPS Cloudflare Worker relay. The Worker owns the OpenAI credential as an encrypted Worker secret, validates a separate BOOP relay credential, forwards the request to OpenAI's Responses API, and returns a small BOOP-specific JSON envelope. The Android app converts that envelope into the existing `CommandOutcome` model so MainActivity can keep its current response/TTS/eye path.

Data flow:

1. Existing Android speech recognition produces text.
2. Existing local router attempts HA/media/local commands.
3. Non-`NO_MATCH` outcomes return immediately and never reach OpenAI.
4. `NO_MATCH` invokes `OpenAiRelayAssistantClient` when OpenAI mode is selected.
5. Client POSTs the utterance plus an opaque conversation identifier to the relay over HTTPS.
6. Worker calls OpenAI and returns response text plus the next opaque conversation identifier.
7. Android returns `CommandOutcome.assistantReply(text)` to the existing BOOP TTS/puppetry flow.

## Relay contract

Android request:

```json
{
  "text": "why is the sky blue?",
  "conversation_id": "optional opaque id"
}
```

Headers:

- `Authorization: Bearer <BOOP relay token>`
- `Content-Type: application/json`

Success response:

```json
{
  "ok": true,
  "text": "Because shorter blue wavelengths are scattered more strongly...",
  "conversation_id": "opaque id"
}
```

Error response:

```json
{
  "ok": false,
  "error": "quota"
}
```

Stable error values: `offline`, `timeout`, `auth`, `quota`, `rate_limit`, `service`, `bad_request`.

The relay must never return the OpenAI key or raw upstream authentication details. Logs must not contain authorization headers or full user prompts by default.

## Credential boundary

`OPENAI_API_KEY` exists only as a Cloudflare Worker encrypted secret. `BOOP_RELAY_TOKEN` is a separate random bearer secret used only to prevent arbitrary public use of the relay. It is not an OpenAI credential. For this prototype the Android build receives the relay URL/token through the existing private GitHub Actions secret/build-configuration boundary, not committed source. If those two relay values are absent, the app remains buildable and reports that conversation setup is incomplete rather than crashing.

Required external configuration after implementation:

- deploy the Worker;
- set Worker secret `OPENAI_API_KEY`;
- set Worker secret `BOOP_RELAY_TOKEN`;
- provide Android build secret/config for relay URL and the same relay token.

The smallest unavoidable user action should be documented after all code/build work possible without the secrets is complete.

## Conversation behavior

The first version uses a normal request/response call rather than token streaming. This deliberately reuses BOOP's synchronous assistant interface and existing complete-utterance TTS path. It avoids changing puppet speech sequencing and keeps the initial security/behavior change small. The relay and client boundaries must not prevent a later streaming transport.

Conversation continuity is represented by an opaque relay/OpenAI conversation identifier kept in memory by the Android client. A fresh app process may start a fresh conversation. Persistent transcript storage is out of scope.

## Errors and latency

Android connect timeout: approximately 5 seconds. Overall conversational timeout: approximately 30 seconds. Network/timeout/auth/quota/rate/service failures map to distinct `CommandOutcome` statuses or a focused assistant-error result, then to short BOOP-language spoken responses through the existing UI path. No stack traces, raw HTTP errors or provider secrets appear to the user.

The Worker should use a compact system/developer instruction appropriate to BOOP: concise spoken answers by default, honest that BOOP is a puppet/software assistant, no claim of sentience, and no attempt to execute home controls through OpenAI. Home controls remain local-first and are never delegated by the relay.

## Preservation rules

- No redesign of BOOP eyes or interaction model.
- Existing speech recognition stays intact.
- Existing local HA/media behavior stays first and unchanged.
- Existing OpenCode and browser Free Chat work must not be silently destroyed; mode selection can gain a native OpenAI-backed option or the current OpenCode slot can be replaced only if implementation evidence and current product intent clearly support it. Prefer an explicit native conversational mode to preserve reversibility during development.
- No direct OpenAI key in APK, source, logs or public artifacts.
- No unrelated permissions.
- No automatic installation or permission grants.
- Preserve accepted checkpoints and separate Wall/Launcher/Shield branches.

## Testing

Unit/source tests must prove:

- local match never invokes relay;
- local failure that is not `NO_MATCH` never invokes relay;
- genuine `NO_MATCH` invokes relay exactly once;
- success maps into existing assistant reply/TTS path;
- missing relay configuration is graceful;
- timeout/offline/auth/quota/rate/service mappings are stable;
- conversation id is reused only for conversation calls;
- secrets are not present in committed Android source or generated public receipts.

Worker tests use a mocked OpenAI upstream and verify auth rejection, request validation, success parsing, upstream error mapping and absence of leaked secrets.

The existing Android/GitHub build/signing workflow remains authoritative for the installable APK. Existing local-command regressions and relevant Wall tests run before delivery. Physical-device behavior remains a separate acceptance state from CI success.

## Out of scope

- Direct consumer ChatGPT web scraping or cookie extraction.
- Embedding an OpenAI API key in Android.
- Replacing Home Assistant as local authority.
- Persistent transcript/history UI.
- New settings redesign.
- Voice routine redesign.
- Streaming TTS in the first implementation.
