# BOOP OpenAI relay

This Cloudflare Worker is the server-side conversation boundary for BOOP Wall.
It accepts only authenticated BOOP requests, forwards ordinary conversation to
the OpenAI Responses API, and returns a compact BOOP-specific response envelope.
Home Assistant/media commands never belong here; Android's local router handles
them before the relay can be called.

## Secrets

Never commit either secret. Configure these as Cloudflare Worker secrets:

- `OPENAI_API_KEY` — OpenAI API credential, server-side only.
- `BOOP_RELAY_TOKEN` — separate random bearer token shared only with the private
  Android build configuration.

Optional non-secret variable:

- `OPENAI_MODEL` — defaults to `gpt-5.6-luna` for a cost-conscious spoken fallback.

The Android build receives only the deployed relay URL and the matching relay
token through GitHub Actions secrets named `BOOP_RELAY_URL` and
`BOOP_RELAY_TOKEN`. The OpenAI key must never enter Android, GitHub source, APK
resources, logs or build receipts.

## Test

```sh
npm test
```

Tests use a mocked upstream fetch. They make no real OpenAI calls and verify
relay authentication, request validation, conversation continuation, upstream
error mapping and secret non-disclosure.

## Deploy

From this directory, authenticate Wrangler to the intended Cloudflare account,
then create the Worker secrets and deploy. Do not paste secret values into this
repository or issue logs.

```sh
npx wrangler secret put OPENAI_API_KEY
npx wrangler secret put BOOP_RELAY_TOKEN
npx wrangler deploy
```

After deployment, add the Worker HTTPS URL as repository Actions secret
`BOOP_RELAY_URL`, and add the same relay bearer value as Actions secret
`BOOP_RELAY_TOKEN`. A build without these Android-side values remains valid but
native ChatGPT mode will politely report that chat setup is incomplete.

## Protocol

Request JSON: `{ "text": "...", "conversation_id": "optional response id" }`
with `Authorization: Bearer <BOOP_RELAY_TOKEN>`.

Success: `{ "ok": true, "text": "...", "conversation_id": "resp_..." }`.
Failure uses only the stable vocabulary `auth`, `quota`, `rate_limit`, `timeout`,
`service`, or `bad_request`; raw upstream failure bodies are never returned.
