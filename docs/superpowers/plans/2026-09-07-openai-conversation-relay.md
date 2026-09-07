# BOOP Secure OpenAI Conversation Relay Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Route only BOOP local `NO_MATCH` utterances through a secure Cloudflare Worker to OpenAI and speak successful replies through BOOP's existing Android response/TTS/puppetry path.

**Architecture:** Preserve `BoopCommandRouter` as the local-first boundary. Add a focused Android relay client implementing the existing assistant processor shape and a Cloudflare Worker that owns `OPENAI_API_KEY`; Android receives only relay URL/token through private build configuration. Initial delivery is request/response, with interfaces kept small enough for later streaming.

**Tech Stack:** Java 17 / Android 36 / OkHttp 4.12, existing BOOP materializer and GitHub Actions signer, Cloudflare Workers JavaScript, OpenAI Responses API, Node built-in test runner or lightweight Worker unit harness.

**Spec:** `docs/superpowers/specs/2026-09-07-openai-conversation-relay-design.md`

## Global Constraints

- Existing speech recognition, eyes, TTS/puppetry and local Home Assistant/media path remain intact.
- Local commands always execute before conversational fallback; only `CommandOutcome.Status.NO_MATCH` may invoke the relay.
- No OpenAI API key, Cloudflare secret or raw provider credential may be committed or embedded directly in source/APK.
- Relay URL/token are supplied only through private build configuration; absent configuration must be graceful.
- Preserve existing OpenCode/browser Free Chat behavior during development; add an explicit native conversational route rather than silently deleting a working fallback.
- No unrelated permissions, redesigns, transcript UI or timed-routine changes.
- Keep accepted Wall, Home, Routines, Launcher and Shield checkpoints untouched.

---

### Task 1: Android relay protocol and error mapping

**Files:**
- Create: `source/OpenAiRelayConfig.java`
- Create: `source/OpenAiRelayAssistantClient.java`
- Modify: `source/CommandOutcome.java`
- Create: `tests/java/OpenAiRelayAssistantClientHarness.java`
- Modify: `tests/test_wall_chat_mode.py`

**Interfaces:**
- Consumes: existing `CommandOutcome.assistantReply(String)` and assistant processor contract `CommandOutcome ask(String)`.
- Produces: `OpenAiRelayConfig.fromBuildConfig()`, `OpenAiRelayAssistantClient.ask(String)`, stable assistant error statuses for setup/auth/quota/rate/timeout/service failures.

- [ ] **Step 1: Write failing Java/source tests for configuration, success and each error mapping.**

Harness cases must assert: missing URL/token returns setup-required without network; HTTP 200 `{ok:true,text,conversation_id}` returns `ASSISTANT_REPLY`; 401/403 -> auth; 408/client timeout -> timeout/unreachable; 429 with `quota` -> quota; other 429 -> rate-limit; 5xx -> service; malformed/empty success -> failed. Assert a returned conversation id is included on the next conversational request.

- [ ] **Step 2: Run focused tests and confirm red.**

Run: `python3 -m unittest tests.test_wall_chat_mode -v` plus `javac`/`java` harness compilation used by existing Java harness tests. Expected: missing relay classes/statuses.

- [ ] **Step 3: Add minimal production implementation.**

`OpenAiRelayConfig` reads generated `BuildConfig.BOOP_RELAY_URL` and `BuildConfig.BOOP_RELAY_TOKEN`, trims values and exposes `configured()`. `OpenAiRelayAssistantClient` uses one OkHttp client with ~5s connect and ~30s call timeout, POSTs JSON `{text,conversation_id}`, bearer-authenticates with the relay token, parses the compact relay envelope, caches only the opaque conversation id in memory, and never logs token/prompt bodies.

Extend `CommandOutcome.Status` with focused conversational statuses such as `ASSISTANT_SETUP_REQUIRED`, `ASSISTANT_AUTH_REQUIRED`, `ASSISTANT_QUOTA`, `ASSISTANT_RATE_LIMIT`, `ASSISTANT_TIMEOUT`, `ASSISTANT_SERVICE` and matching factories. Do not alter existing local statuses.

- [ ] **Step 4: Run focused tests and make them green.**

Run the same Python/Java harness commands. Expected: all relay protocol/error cases pass.

- [ ] **Step 5: Commit the isolated Android protocol layer.**

Commit message: `feat(wall): add secure conversation relay client`.

### Task 2: Preserve local-first routing and wire native conversation into BOOP

**Files:**
- Modify: `source/BoopCommandRouter.java`
- Modify: `source/BoopChatMode.java`
- Modify: `source/BoopChatModeStore.java` only if enum persistence requires it
- Modify: `scripts/patch-wall-chat-mode.py`
- Modify: `scripts/materialize-android.sh`
- Modify: `tests/java/BoopCommandRouterHarness.java` or create it if absent
- Modify: `tests/test_wall_chat_mode.py`

**Interfaces:**
- Consumes: `OpenAiRelayAssistantClient.ask(String)` and existing local processor.
- Produces: explicit `NATIVE_CHAT`/equivalent mode while preserving `OPENCODE` and browser `FREE_CHAT`; existing router still calls local exactly once before any assistant.

- [ ] **Step 1: Write failing routing tests.**

Assert: SUCCESS/TARGET_OFFLINE/NO_TARGET/FAILED/UNREACHABLE/AUTH_REQUIRED never invoke any conversation processor; only `NO_MATCH` does. Assert mode selection chooses existing OpenCode, browser Free Chat, or native relay without changing local-first ordering. Assert mode persists using the existing store pattern.

- [ ] **Step 2: Run focused tests and confirm red.**

Run: `python3 -m unittest tests.test_wall_chat_mode -v` and the router Java harness. Expected: native mode unavailable.

- [ ] **Step 3: Implement the smallest routing integration.**

Retain `BoopCommandRouter`'s local-first semantics. Add the native relay as a separate assistant processor selected only after local `NO_MATCH`. Keep browser Free Chat copy/open path and OpenCode route reversible. Update the long-hold menu label in plain English, for example `ChatGPT` for native relay while keeping `OpenCode`, `Free Chat`, `Cancel` if the existing UI can accommodate it without redesign.

- [ ] **Step 4: Materialize and run focused regressions.**

Run `bash scripts/materialize-android.sh`, then existing source/router/chat tests. Expected: materialized MainActivity uses existing TTS/puppetry path for `ASSISTANT_REPLY`; local commands remain untouched.

- [ ] **Step 5: Commit routing integration.**

Commit message: `feat(wall): route unmatched speech to native chat`.

### Task 3: BOOP-language error speech through existing UI/TTS

**Files:**
- Modify: `scripts/patch-wall-chat-mode.py`
- Modify: relevant focused source tests under `tests/`

**Interfaces:**
- Consumes: new `CommandOutcome` assistant statuses.
- Produces: short user-facing speech strings via the same existing outcome handling/TTS mechanism, with no provider internals exposed.

- [ ] **Step 1: Write failing source assertions for every new status.**

Required meanings: setup incomplete; no network/timeout; relay auth rejected; quota exhausted; rate limited; service temporarily unavailable. Strings must be short and non-technical, and must not say a local command failed when the error came from conversation fallback.

- [ ] **Step 2: Run tests and confirm red.**

Run focused Wall source tests. Expected: missing mappings.

- [ ] **Step 3: Add outcome handling to the existing patched MainActivity path.**

Use existing `speak(...)`/puppetry response handling rather than a new TTS engine or screen. Keep assistant activity start/finish behavior balanced on every outcome.

- [ ] **Step 4: Materialize and verify source behavior.**

Run materializer and focused tests; inspect generated MainActivity for each new status and absence of secrets.

- [ ] **Step 5: Commit error UX.**

Commit message: `feat(wall): speak native chat failures cleanly`.

### Task 4: Cloudflare Worker relay with mocked OpenAI tests

**Files:**
- Create: `relay/cloudflare/src/index.js`
- Create: `relay/cloudflare/test/index.test.js`
- Create: `relay/cloudflare/package.json`
- Create: `relay/cloudflare/wrangler.toml`
- Create: `relay/cloudflare/README.md`

**Interfaces:**
- Consumes environment secrets `OPENAI_API_KEY`, `BOOP_RELAY_TOKEN`; request `{text,conversation_id}`.
- Produces HTTP JSON `{ok:true,text,conversation_id}` or `{ok:false,error}` using the stable error vocabulary from the spec.

- [ ] **Step 1: Write Worker tests against a mocked `fetch` upstream.**

Cases: missing/wrong BOOP bearer token -> 401 without contacting OpenAI; empty/oversized text -> 400; valid request sends bearer OpenAI key only upstream; success extracts Responses API output text; upstream 401/403 -> auth, 429 quota -> quota, other 429 -> rate_limit, timeout/network -> timeout/service, 5xx -> service. Assert response/loggable objects never include either secret.

- [ ] **Step 2: Run Worker tests and confirm red.**

Run: `cd relay/cloudflare && npm test`. Expected: implementation missing.

- [ ] **Step 3: Implement Worker.**

Use `fetch('https://api.openai.com/v1/responses', ...)` with `Authorization: Bearer ${env.OPENAI_API_KEY}` and a compact spoken-answer instruction. Validate `BOOP_RELAY_TOKEN` before parsing/forwarding. Limit request size. Parse output defensively. Use an opaque `previous_response_id`/response id as the conversation id where supported by the chosen Responses API request shape. Never return raw upstream bodies on failure.

- [ ] **Step 4: Run Worker tests to green.**

Run `npm test`. Expected: all mocked auth/protocol/error tests pass without real OpenAI traffic.

- [ ] **Step 5: Commit relay.**

Commit message: `feat(relay): add secure OpenAI worker`.

### Task 5: Private build configuration and secret-leak guard

**Files:**
- Modify: `source/app-build.gradle`
- Modify: `.github/workflows/build-boop-wall-free-chat.yml`
- Create or modify: `tests/test_wall_openai_relay.py`
- Modify: `relay/cloudflare/README.md`

**Interfaces:**
- Consumes GitHub Actions secrets `BOOP_RELAY_URL`, `BOOP_RELAY_TOKEN`.
- Produces generated Android `BuildConfig` string fields; never source literals.

- [ ] **Step 1: Write failing tests/grep guards.**

Assert source contains BuildConfig field names but no values matching OpenAI keys, bearer secrets or committed relay token. Assert workflow passes secret values as environment variables only during build. Assert missing secrets produce empty BuildConfig fields and a buildable APK.

- [ ] **Step 2: Run guards and confirm red.**

Run focused Python tests. Expected: build fields/workflow env absent.

- [ ] **Step 3: Add Gradle and workflow configuration.**

Read `BOOP_RELAY_URL`/`BOOP_RELAY_TOKEN` from environment and escape them into `buildConfigField` values. Workflow supplies `${{ secrets.BOOP_RELAY_URL }}` and `${{ secrets.BOOP_RELAY_TOKEN }}` to the Android build step. Do not echo values. Preserve the existing permanent APK signer.

- [ ] **Step 4: Run secret guards and build without relay secrets.**

Expected: tests pass and configuration-less build succeeds with graceful runtime setup message.

- [ ] **Step 5: Commit secure build wiring.**

Commit message: `build(wall): inject private conversation relay config`.

### Task 6: Full regression, signed APK and handoff

**Files:**
- Modify: `SESSION_HANDOFF.md`
- Modify: `BOOP_STATUS.md`
- Modify: `BOOP_CHAT_MODE_MEMORY.txt`
- Modify: `BOOP_CONTEXT.md` only if shared product state materially changed
- Add build receipt under `docs/` following existing Wall receipt convention

**Interfaces:**
- Consumes completed Android/Worker implementation and existing GitHub signing workflow.
- Produces signed installable APK artifact plus exact commit/run/digest/signing evidence and one smallest external configuration action if credentials remain absent.

- [ ] **Step 1: Run all relevant local/source/Worker regressions.**

Run existing Wall Python/JVM tests, Worker `npm test`, materialization, Android unit tests, and secret scans. Fix any failures before continuing.

- [ ] **Step 2: Review scoped diff.**

Confirm no unrelated UI, permissions, HA/media, wake, eyes, blink, launcher or signer changes. Confirm local-first routing tests prove no OpenAI call for known commands.

- [ ] **Step 3: Push the reviewed owning branch and let existing GitHub Actions build/sign.**

Do not force push. Re-fetch live branch before push/repoint. Use the existing permanent signer and normal verification path, not the no-tests marker.

- [ ] **Step 4: Inspect CI and retrieve the APK artifact.**

Require compile/unit/signature/package/version gates green. If relay secrets are unavailable in GitHub, accept only the configuration-less signed APK as a setup candidate and state that native conversation cannot work until configured; do not pretend a live OpenAI call passed.

- [ ] **Step 5: Update handoff/status/memory and publish documentation-only receipt.**

Record exact app commit, workflow run/job/artifact, APK SHA-256, signer continuity, tests run, physical-test status, relay deployment/config status, and preserved checkpoints. Verify live GitHub branch HEAD after documentation commit.

- [ ] **Step 6: Deliver APK and the single smallest user action.**

If no credentials could safely be created, the action should be one compact configuration step: deploy/configure the provided Worker with `OPENAI_API_KEY` and `BOOP_RELAY_TOKEN`, then add `BOOP_RELAY_URL` and matching `BOOP_RELAY_TOKEN` to the repository Actions secrets. Do not ask for or expose the OpenAI key in chat or source.
