# BOOP conversation relay (private prototype)

This Worker handles conversation only. BOOP Wall still attempts every local
house/media command first, and only `NO_MATCH` can reach the selected assistant.
The new **Native Chat** mode speaks the reply through the existing puppet/TTS.
OpenCode and browser **Free Chat** remain available and reversible in the hold menu.

## Credentials and deployment

The OpenAI API key belongs **only in the Worker's encrypted secrets**. Never paste
it into chat, commit it, or put it in an Android build. OpenAI API usage is separately
billed; this relay does not use or restore consumer ChatGPT/Codex credits.

Using your Cloudflare account, deploy this directory as a Worker. For a local
Wrangler session, the standard commands are:

```sh
npx wrangler login
npx wrangler deploy
npx wrangler secret put OPENAI_API_KEY
npx wrangler secret put BOOP_RELAY_TOKEN
```

Enter secrets only into the private secret prompts/dashboard. Generate a different
random relay token (at least 32 random bytes encoded as base64url); do not reuse the
OpenAI key. Deployment without secrets rejects requests and cannot call OpenAI.
The default model is `gpt-4.1-mini`; change the nonsecret `OPENAI_MODEL` Worker
variable to another Responses-compatible model when appropriate for your account.

Before injecting a relay token, make the repository private or use an equivalent
private build/distribution route. **The workflow refuses configured builds in a
public repository** because a configured APK would expose the bearer token to
any artifact recipient. It does not change repository visibility for you. Builds
with empty relay configuration remain safe to publish as setup candidates.

Then add these **repository Actions secrets** through GitHub's private interface:
`BOOP_RELAY_URL` = the deployed HTTPS Worker URL (no query/fragment/userinfo), and
`BOOP_RELAY_TOKEN` = that same separate relay token. Re-run the normal Wall build
and install its signed APK as an update. Hold the eyes for three seconds and choose
**Native Chat**. Missing relay configuration produces a short setup message, never
a crash or a switch away from local commands.

This repository contains no deployment account access, real credentials or live
API-test receipt. A configuration-less APK is a setup candidate, not a live-chat
success. Tests mock all provider traffic and require no paid account.

## Prototype security boundary

Build-time injection keeps the relay token **out of committed source**, not out of
the APK. Any recipient of a configured APK can extract its token and spend through
this Worker. Keep configured APKs and their Actions artifacts private to your own
trusted devices, rotate the relay token if shared, and restrict repository artifact
access accordingly. The OpenAI key remains server-side. This is not multi-user
production authentication; use per-device enrollment, revocation, rate limiting
and enforceable cost controls before distributing to friends or the public.

Configure conservative provider/project limits and Cloudflare access/rate rules
before live use. A provider budget alert alone may not stop spending. This Worker
bounds each request and reply, but the bearer credential is not a cost cap. There
is no automatic retry of paid requests. Do not put either secret in `[vars]`,
tracked `.env`/`.dev.vars`, CLI arguments, screenshots or build receipts.

## Conversation and privacy

Request: authenticated POST JSON `{ "text": "...", "conversation_id": "optional" }`.
Only `text` and `conversation_id` are accepted. The relay owns the model, instructions
and fixed OpenAI endpoint; clients cannot add tools or choose another upstream URL.
It never executes house commands. Input is limited to 4,000 UTF-16 code units and
16 KiB encoded JSON; upstream response bodies to 64 KiB; spoken output to 8,000
code units and a 600-token generation cap. Upstream timeout is 25 seconds, inside
Android's 30-second overall timeout. Incomplete/malformed replies are not spoken.

OpenAI's Responses API is called with `store: true` and `previous_response_id` to
continue a conversation. That means conversation content is sent to and stored by
OpenAI under your API account's applicable data-retention settings. Conversation
context can increase subsequent usage costs. The Android app keeps only the opaque
response ID in memory: a fresh process starts fresh; there is no new local history
UI. IDs are not separate authentication and are for this single-owner prototype,
not isolation between unrelated users. After a rejected/stale ID the next manual
question starts fresh; the client does not silently re-submit a paid question.

Stable failure vocabulary: `offline`, `timeout`, `auth`, `quota`, `rate_limit`,
`service`, `bad_request`. Errors never contain raw upstream bodies or credentials.
Default application observability is disabled; no prompt/auth logging is added.
Provider and platform metadata/retention policies still apply.

## Tests

```sh
npm test
```

Node 22+ built-in tests; no installed packages or live upstream traffic. Tests
exercise bearer rejection before body parsing, limits/types, the real response
parser, continuity, sanitized errors and both credential boundaries.

Official contracts used for implementation (reviewed 2026-09-07):
- https://developers.openai.com/api/docs/guides/conversation-state
- https://developers.openai.com/api/docs/models/gpt-4.1-mini
- https://developers.cloudflare.com/workers/configuration/secrets/
- https://developers.cloudflare.com/workers/wrangler/configuration/
