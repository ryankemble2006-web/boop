# BOOP OpenCode ChatGPT Bridge Design

## Goal

Give BOOP a general ChatGPT-style fallback without adding an OpenAI API key, without separate API billing, and without allowing the new AI layer to interfere with the already-working Home Assistant and direct-media paths.

## User-facing setup target

Setup should be as small as practical:

1. In the existing OpenCode Home Assistant add-on, enable **OpenCode LAN server** and **Startup hooks**. Do **not** map port `4096/tcp` to the LAN.
2. Put one supplied hook file at `/config/opencode/startup.d/10-boop-chatgpt.sh` and restart OpenCode.
3. Add one Home Assistant **Wyoming Protocol** integration pointing at the OpenCode add-on's internal hostname on port `10400`.
4. Install the new BOOP APK over Alpha 5. No OpenAI key or additional provider login is required; OpenCode reuses the provider login already configured there.

The hook file is self-contained: on startup it materializes the Python bridge under `/data`, creates/updates a persistent virtual environment, installs pinned Python dependencies when needed, and starts exactly one detached bridge process.

## Architecture

The speech route is strictly ordered:

1. **BOOP direct-media path** — Alpha 5 behavior remains first and unchanged for pause/play/resume/skip/previous/volume commands.
2. **Existing local Home Assistant conversation path** — lights and normal house commands continue through the current `/api/conversation/process` request with BOOP's Home Assistant `device_id`.
3. **General assistant fallback** — only an exact local `NO_INTENT_MATCH` / `CommandOutcome.Status.NO_MATCH` is eligible. Every other local outcome wins immediately: success, no target, target offline, failed, unreachable, and auth-required must never fall through.
4. The fallback calls the OpenCode-backed Wyoming conversation agent through Home Assistant's own `/api/conversation/process`, supplying `agent_id`, BOOP `device_id`, BCP47 language, and the previous `conversation_id` when present.

The new fallback is outside `HomeAssistantClient`; `HomeAssistantClient` remains the local-house/direct-media component. A separate router owns the fallback decision so future AI work cannot silently alter local Home Assistant routing.

## OpenCode / Wyoming bridge

The OpenCode add-on's `enable_server` option starts its server on internal port `4096`. The user must not publish that port in the Supervisor Network settings. The bridge runs in the same add-on container and talks only to `http://127.0.0.1:4096`.

The bridge listens on TCP `0.0.0.0:10400` using Wyoming's `handle` service. Home Assistant Core reaches that port over the Supervisor add-on network; no host/LAN port mapping is required.

For each Wyoming `Transcript`:

- Read `Transcript.context.conversation_id` when Home Assistant supplies one.
- Map that Home Assistant conversation id to one persistent OpenCode session id in memory. A new HA conversation creates a new OpenCode session; follow-up turns reuse it even when HA opens a new TCP connection.
- Send the transcript to `POST /session/{id}/message` with a text part and `agent: "general"`.
- Before prompting, obtain OpenCode's available tool ids from `/experimental/tool/ids` and send every returned tool as `false` in the prompt's `tools` map. BOOP's general fallback is conversational only: it must not edit files, run shell commands, call Home Assistant MCP tools, or mutate the house. House control remains in the local BOOP path.
- Extract text parts from the final OpenCode response and return a Wyoming `Handled` event.
- On OpenCode failure or timeout, return a clean failure to Home Assistant rather than hanging the voice pipeline.

The bridge also answers Wyoming `describe` with one handle program named **BOOP OpenCode** so Home Assistant registers a distinct conversation agent that BOOP can discover reliably.

## OpenCode startup hook

Stable OpenCode startup hooks live in `/config/opencode/startup.d` (the add-on sees this as `/homeassistant/opencode/startup.d`). Startup hooks are already an add-on-supported extension point and run as root, under a bounded runner. Long-running work therefore detaches from the hook.

`10-boop-chatgpt.sh` will:

- Require the OpenCode server to be reachable on `127.0.0.1:4096` before launching the bridge.
- Create `/data/venvs/boop-wyoming` if missing.
- Pin and install `wyoming` and `aiohttp` into that persistent venv only when the dependency marker changes.
- Materialize `/data/boop-wyoming/bridge.py` from the hook's embedded Python payload.
- Use a PID file plus `kill -0` guard to prevent duplicate bridge processes.
- Start the bridge detached with unbuffered Python and logs at `/data/boop-wyoming/bridge.log`.
- Exit quickly so the add-on's startup-hook timeout cannot kill the bridge process group.

No code is written into `/etc`, `/usr`, or other ephemeral add-on paths.

## BOOP Android components

### `BoopCommandRouter`

Consumes the existing `HomeAssistantClient` and a new `HomeAssistantGeneralAssistantClient`.

`process(String text)` first calls the existing Home Assistant client. It returns that result unchanged unless the status is exactly `NO_MATCH`. Only then does it call the general assistant client.

### `HomeAssistantGeneralAssistantClient`

Reuses `SecureTokenStore` and `HomeAssistantAuth` for the same Home Assistant OAuth connection BOOP already has.

It discovers conversation agents over Home Assistant WebSocket command `conversation/agent/list`, selecting only an agent whose id or name contains `boop opencode` or `opencode` (case-insensitive). It does not guess another third-party assistant and does not use the current Assist pipeline selection.

It caches the selected agent id in memory and keeps Home Assistant's returned `conversation_id` in memory for follow-ups. If Home Assistant is restarted or BOOP is killed, a fresh conversation starts; persistence across process restarts is not required for this alpha.

### `MainActivity`

Only the speech-result routing reference changes from the raw `HomeAssistantClient` to `BoopCommandRouter`. Touch handling, member-berry animation, face presence, recognizer behavior, TTS setup, OAuth callback behavior, and device registration remain byte-for-byte unchanged unless compilation requires a field/type rename. CI must explicitly guard the puppet surface against accidental edits.

## Replies and failure behavior

- General assistant reply: speak the returned text normally.
- No OpenCode agent registered: `I can control the house, but I don't have my assistant connected yet.`
- Assistant path unreachable: `I can still control the house, but I can't reach my assistant right now.`
- Assistant returned malformed/empty output: `I can still control the house, but my assistant didn't answer that.`
- Local Home Assistant unavailable/auth-required: preserve the existing local BOOP behavior; do not try the assistant.

## Security boundaries

- No OpenAI API key is stored in the APK, Home Assistant config, or bridge.
- OpenCode port `4096` remains internal to the add-on container; the user must not map it to the LAN for this design.
- OpenCode provider credentials remain owned by OpenCode and are never copied into BOOP or Home Assistant.
- The Wyoming bridge exposes only text conversation handling over the Supervisor internal network.
- Every OpenCode tool advertised by `/experimental/tool/ids` is disabled for BOOP fallback prompts. The general fallback cannot mutate Home Assistant or the filesystem even if a speech transcript asks it to.
- House commands stay local-first and cloud failure cannot remove direct-media or local Home Assistant control.

## Testing

### Bridge tests

Pure Python tests cover:

- HA conversation id reuses the same OpenCode session.
- A different/no conversation id creates an appropriate separate session.
- Tool ids returned by OpenCode are all sent as disabled.
- Text parts are extracted correctly.
- Describe advertises `BOOP OpenCode` as a Wyoming handle agent.
- OpenCode timeout/error returns a bounded failure and does not leave the TCP request hanging.
- Startup hook contains no LAN port mapping and uses a duplicate-process guard.

### Android tests

TDD tests must prove:

- `SUCCESS`, `NO_TARGET`, `TARGET_OFFLINE`, `FAILED`, `UNREACHABLE`, and `AUTH_REQUIRED` never reach the general assistant.
- Only `NO_MATCH` reaches the general assistant.
- Agent discovery chooses an OpenCode/BOOP OpenCode agent and ignores built-in Home Assistant.
- The assistant request carries `agent_id`, BOOP `device_id`, language, and prior `conversation_id`.
- Returned `conversation_id` is reused on the next fallback turn.
- Direct media remains before Home Assistant conversation processing.
- `MainActivity.java` and `BoopFaceView.java` puppet interaction surfaces remain unchanged from the Alpha 5 checkpoint except the minimal command-router field/type seam in `MainActivity`.

### Full APK gate

Before delivery: source regressions, Python bridge tests, Android JVM unit tests, permanent BOOP signing, APK inspection, clean Android 16 emulator boot, install, launch, and process-survival check must all pass on the exact delivered SHA.

## Manual acceptance test

After the one-time OpenCode/Wyoming setup and Alpha 6 install:

1. `lights on` — local HA, immediate, no OpenCode call.
2. `pause` / `play` / `skip` — Alpha 5 direct-media path, immediate, no OpenCode call.
3. `why is the sky blue?` — OpenCode/ChatGPT reply.
4. `explain that simpler` — same conversation context and a sensible follow-up.
5. Stop OpenCode, then test lights/media again — local controls continue working; a general question gives the assistant-unreachable reply.
