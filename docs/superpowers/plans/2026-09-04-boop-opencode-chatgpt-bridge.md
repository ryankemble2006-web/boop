# BOOP OpenCode ChatGPT Bridge Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a zero-extra-key ChatGPT fallback to BOOP by bridging Home Assistant's Wyoming conversation agent to the already-authenticated OpenCode add-on while preserving Alpha 5 direct-media and local Home Assistant behavior.

**Architecture:** Keep `HomeAssistantClient` as the local-house/direct-media core. Add a separate `BoopCommandRouter` that calls local first and only invokes a new `HomeAssistantGeneralAssistantClient` on exact `NO_MATCH`. Supply one self-contained OpenCode startup hook that materializes a pinned Wyoming bridge inside the add-on and exposes a distinct `BOOP OpenCode` conversation agent to Home Assistant.

**Tech Stack:** Android Java 17, Home Assistant REST/WebSocket conversation APIs, Python 3, `aiohttp`, `wyoming`, OpenCode HTTP server API, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-04-boop-opencode-chatgpt-bridge-design.md`

## Global Constraints

- Alpha 5 direct-media routing remains first and unchanged.
- Only `CommandOutcome.Status.NO_MATCH` may reach the general assistant.
- No OpenAI API key or separate OpenAI API billing.
- OpenCode provider credentials remain inside OpenCode.
- OpenCode port `4096` must not be mapped to the LAN for this design.
- BOOP general-chat turns disable every OpenCode tool.
- `MainActivity` touch/member-berry/presence behavior must remain unchanged except the minimal command-router seam.
- Full APK delivery requires signed build, Android 16 clean-launch verification, and raw `.apk` extraction.

---

### Task 1: OpenCode Wyoming bridge core

**Files:**
- Create: `bridge/boop_wyoming_bridge.py`
- Create: `bridge/test_boop_wyoming_bridge.py`

**Interfaces:**
- Produces: `SessionRegistry.session_for(conversation_id) -> str`, `disabled_tools(tool_ids) -> dict[str, bool]`, `extract_text(response) -> str`, `OpenCodeClient.create_session()`, `OpenCodeClient.prompt(session_id, text)`, and Wyoming TCP `main()` on port `10400`.

- [ ] **Step 1: Write failing bridge unit tests**

Tests must assert that the same HA conversation id reuses an OpenCode session, different ids do not collide, `disabled_tools(["shell","edit"]) == {"shell": False, "edit": False}`, text parts are joined cleanly, and malformed/no-text replies are rejected.

- [ ] **Step 2: Run the bridge tests and confirm RED**

Run:

```bash
python3 -m unittest bridge/test_boop_wyoming_bridge.py -v
```

Expected: failures because bridge helpers do not exist yet.

- [ ] **Step 3: Implement minimal bridge helpers and OpenCode client**

Use OpenCode `POST /session`, `GET /experimental/tool/ids`, and `POST /session/{id}/message`. Prompt body uses:

```json
{
  "agent": "general",
  "tools": {"<every returned tool id>": false},
  "parts": [{"type": "text", "text": "<transcript>"}]
}
```

`aiohttp.ClientTimeout(total=45)` bounds each OpenCode turn.

- [ ] **Step 4: Add Wyoming protocol handling**

The server listens on `0.0.0.0:10400`, answers `describe` with one `handle` program named `BOOP OpenCode`, consumes Transcript events, maps `Transcript.context.conversation_id` through `SessionRegistry`, and returns a final Handled response containing the extracted OpenCode text.

- [ ] **Step 5: Run bridge tests and confirm GREEN**

```bash
python3 -m unittest bridge/test_boop_wyoming_bridge.py -v
```

Expected: all tests pass.

- [ ] **Step 6: Commit**

```bash
git add bridge/
git commit -m "feat: add BOOP OpenCode Wyoming bridge"
```

---

### Task 2: Self-contained OpenCode startup hook

**Files:**
- Create: `setup/opencode/10-boop-chatgpt.sh`
- Create: `tests/test_opencode_hook.py`

**Interfaces:**
- Produces: one user-copied hook for `/config/opencode/startup.d/10-boop-chatgpt.sh`.
- Consumes: `bridge/boop_wyoming_bridge.py` embedded verbatim in a quoted heredoc.

- [ ] **Step 1: Write failing hook tests**

Tests assert the hook contains `127.0.0.1:4096`, port `10400`, persistent venv `/data/venvs/boop-wyoming`, PID guard using `kill -0`, pinned `wyoming` and `aiohttp`, detached `nohup`, and no host/LAN port-mapping commands.

- [ ] **Step 2: Run source tests and confirm RED**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
```

- [ ] **Step 3: Implement the self-contained hook**

The hook must:

```bash
set -euo pipefail
BASE=/data/boop-wyoming
VENV=/data/venvs/boop-wyoming
PID="$BASE/bridge.pid"
LOG="$BASE/bridge.log"
```

It waits up to 30 seconds for `http://127.0.0.1:4096/global/health`, creates the venv if needed, installs pinned dependencies only when a requirements marker differs, writes embedded `bridge.py`, kills no healthy existing process, removes only stale PID files, starts `python -u bridge.py` using `nohup setsid`, writes the child PID, and exits quickly.

- [ ] **Step 4: Run source tests and confirm GREEN**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
```

- [ ] **Step 5: Commit**

```bash
git add setup/opencode/10-boop-chatgpt.sh tests/test_opencode_hook.py
git commit -m "feat: add one-file OpenCode startup hook"
```

---

### Task 3: Android general-assistant client and exact fallback policy

**Files:**
- Create: `source/BoopCommandRouter.java`
- Create: `source/HomeAssistantGeneralAssistantClient.java`
- Create: `source/HomeAssistantOpenCodeAgentSelector.java`
- Create: `source/HomeAssistantAssistantReply.java`
- Create: `source/HomeAssistantAssistantReplyParser.java`
- Modify: `source/HomeAssistantConversationRequest.java`
- Modify: `source/CommandOutcome.java`
- Modify: `source/LocalReply.java`
- Create tests under `source-test/` for every new class/policy.

**Interfaces:**
- `BoopCommandRouter(HomeAssistantClient local, HomeAssistantGeneralAssistantClient assistant)`
- `CommandOutcome process(String text)`
- `HomeAssistantGeneralAssistantClient.ask(String text) -> CommandOutcome`
- `HomeAssistantOpenCodeAgentSelector.select(JSONArray agents) -> String?`

- [ ] **Step 1: Write failing Android tests**

Tests must prove only `NO_MATCH` calls the assistant; `SUCCESS`, `NO_TARGET`, `TARGET_OFFLINE`, `FAILED`, `UNREACHABLE`, and `AUTH_REQUIRED` return untouched. Selector accepts only names/ids containing `opencode` and ignores built-in `home_assistant`. Conversation request includes `agent_id`, BOOP `device_id`, language, and previous `conversation_id` when non-empty. Parser returns speech and new `conversation_id`.

- [ ] **Step 2: Run JVM tests and confirm RED**

Materialize and run:

```bash
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

- [ ] **Step 3: Implement exact fallback router**

Core rule:

```java
CommandOutcome localOutcome = local.process(text);
if (localOutcome.status() != CommandOutcome.Status.NO_MATCH) {
    return localOutcome;
}
return assistant.ask(text);
```

No assistant logic is added inside `HomeAssistantClient`.

- [ ] **Step 4: Implement HA agent discovery and conversation call**

Use Home Assistant WebSocket `conversation/agent/list`, select only the OpenCode agent, cache its id in memory, then POST `/api/conversation/process` with original text, BCP47 language, BOOP device id, selected `agent_id`, and prior `conversation_id`. Save returned `conversation_id` for the next fallback turn.

- [ ] **Step 5: Implement plain-English outcomes**

Add assistant outcomes and replies:

```text
I can control the house, but I don't have my assistant connected yet.
I can still control the house, but I can't reach my assistant right now.
I can still control the house, but my assistant didn't answer that.
```

- [ ] **Step 6: Run JVM tests and confirm GREEN**

```bash
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

- [ ] **Step 7: Commit**

```bash
git add source source-test
git commit -m "feat: add isolated OpenCode assistant fallback"
```

---

### Task 4: Minimal MainActivity seam and regression guards

**Files:**
- Modify: `source/MainActivity.java`
- Create/modify: `tests/test_alpha6_routing.py`

**Interfaces:**
- Replace the command execution field with `BoopCommandRouter` while preserving construction of the existing `HomeAssistantClient`.

- [ ] **Step 1: Write failing source regression test**

The test must assert `MainActivity` calls `commandRouter.process(transcript)`, direct-media remains inside `HomeAssistantClient` before conversation POST, and member-berry/touch signatures remain present.

- [ ] **Step 2: Confirm RED**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
```

- [ ] **Step 3: Make the minimal MainActivity edit**

Construct local client, assistant client, then router in `onCreate`; replace only the speech dispatch call. Do not alter touch/listening/TTS/presence code.

- [ ] **Step 4: Confirm source + JVM GREEN**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

- [ ] **Step 5: Commit**

```bash
git add source/MainActivity.java tests/test_alpha6_routing.py
git commit -m "feat: route unmatched speech to OpenCode assistant"
```

---

### Task 5: Setup guide and Alpha 6 full APK gate

**Files:**
- Create: `docs/BOOP-ALPHA6-SETUP.md`
- Modify: `source/app-build.gradle`
- Create: `.github/workflows/build-alpha6.yml`

**Interfaces:**
- Version: `versionCode 13`, `versionName "0.4.0-alpha6"`.
- Artifact: `BOOP-Alpha6-OpenCode-debug`.

- [ ] **Step 1: Write the setup guide**

Keep user steps to:

1. OpenCode add-on → Configuration → enable **OpenCode LAN server** and **Startup hooks**; leave Network port `4096` unmapped.
2. Put `10-boop-chatgpt.sh` in `/config/opencode/startup.d/` and restart OpenCode.
3. Home Assistant → Add Integration → **Wyoming Protocol** → host `f5588468-ha-opencode`, port `10400` (if the user's installed add-on hostname differs, obtain it from the add-on/network diagnostics rather than guessing).
4. Confirm `BOOP OpenCode` appears as a conversation agent.
5. Install Alpha 6 APK.

Include `ha-hooks list`, `ha-hooks log 10-boop-chatgpt.sh`, and bridge log path for troubleshooting.

- [ ] **Step 2: Bump Android install version**

Set code 13 / `0.4.0-alpha6`.

- [ ] **Step 3: Create full build workflow**

Workflow must run Python source/bridge tests, materialize project, Java 17/Android 36 setup, JVM tests, permanent signing, APK build and `aapt` inspection, clean Android 16 emulator boot, install, launch, process-survival check, and upload artifact.

- [ ] **Step 4: Run the exact full workflow SHA and require success**

Do not deliver from a stale run.

- [ ] **Step 5: Download artifact ZIP, extract the single APK, verify ZIP/APK integrity and SHA-256, and deliver raw `.apk`**

- [ ] **Step 6: Manual acceptance after user setup**

Test in this order:

```text
lights on
pause
skip
why is the sky blue?
explain that simpler
```

Then stop OpenCode and reconfirm lights/media still work.
