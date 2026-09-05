# BOOP Routine Authoring v1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let BOOP create one narrow class of enabled Home Assistant automation by voice after an exact, context-bound `yes please`, while ordinary OpenCode conversation remains tool-free.

**Architecture:** Keep Android and Shield runtime code frozen. Extract the OpenCode Wyoming bridge into an importable source file while continuing to ship one generated startup hook, add a per-conversation authoring state machine and strict structured proposal validator, allow only exact read-only Home Assistant tools during planning, and perform the confirmed write inside the bridge through one argument-vector `hab automation create` call. Fail closed when the installed OpenCode tool surface or `hab` command differs from the reviewed capability contract.

**Tech Stack:** Python 3.12, `aiohttp==3.14.3`, `wyoming==1.10.0`, OpenCode HTTP API, OpenCode Home Assistant MCP read tools, Home Assistant Builder (`hab`) CLI, Bash startup hooks, Python `unittest`, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-05-boop-routine-authoring-v1-design.md`

## Global Constraints

- Start execution from commit `db8def098cbd99a25c7a725dbc211a950a5325cb` in a new isolated worktree and feature branch; never move or rewrite `checkpoint-boop-wall-595e1da`.
- The only authoring entry phrase is a normalized transcript beginning with `create a routine`.
- V1 creates new Home Assistant automations only; it never edits, deletes, enables, disables or triggers an existing automation.
- V1 supports one clock or sun trigger, no conditions, and only the exact service allowlist from the spec.
- Ordinary OpenCode conversation keeps every discovered tool disabled.
- OpenCode never receives `hab_run`, shell, edit, write, service-call, update, firmware, dashboard, Zigbee or other mutation tools.
- Only exact `yes please` in the same conversation, within two minutes of the unchanged proposal, can write.
- The bridge invokes `hab` with an argument vector and a mode-0600 temporary file; it never invokes a shell.
- Entity IDs must come from live Home Assistant inspection and pass strict domain validation.
- The created automation is enabled but is not executed during creation.
- Existing local Home Assistant/media routing, wake behaviour, BOOP conversation, QR pairing, Shield Routines and stable signing remain unchanged.
- The discarded Android timed-routine flow remains absent.
- If live read-tool/skill separation or bounded `hab automation create` support is unavailable, stop after Task 1 and report the exact mismatch; do not weaken the design.

---

### Task 1: Prove the live OpenCode and `hab` capability boundary

**Files:**
- Create: `docs/superpowers/evidence/2026-09-05-boop-routine-capabilities.md`
- Test: `tests/test_opencode_routine_capability_evidence.py`

**Interfaces:**
- Consumes: the installed OpenCode Home Assistant add-on and its existing internal server at `127.0.0.1:4096`.
- Produces: a secret-free evidence record proving the exact OpenCode tool IDs, installed skill path, and supported `hab automation` commands used by later tasks.

- [ ] **Step 1: Write the failing evidence contract**

Create `tests/test_opencode_routine_capability_evidence.py`:

```python
import json
import unittest
from pathlib import Path


class RoutineCapabilityEvidenceTest(unittest.TestCase):
    def test_live_capabilities_are_explicit_and_safe(self):
        path = Path("docs/superpowers/evidence/2026-09-05-boop-routine-capabilities.md")
        self.assertTrue(path.exists())
        text = path.read_text(encoding="utf-8")
        for required in (
            "OpenCode version:", "Skill: home-assistant-configuration",
            "hab automation create:", "hab automation list:",
            "hab automation get:", "Exact authoring read tool IDs:",
            "Decision: SAFE TO CONTINUE",
        ):
            self.assertIn(required, text)
        self.assertNotRegex(text, r"(?i)(token|secret|password)\s*[:=]\s*\S+")

        marker = "```json\n"
        payload = text.split(marker, 1)[1].split("\n```", 1)[0]
        ids = json.loads(payload)
        self.assertEqual(ids, sorted(set(ids)))
        self.assertIn("skill", ids)
        self.assertTrue(any(item.endswith("search_entities") for item in ids))
        self.assertNotIn("bash", ids)
        self.assertFalse(any(item.endswith("hab_run") for item in ids))


if __name__ == "__main__":
    unittest.main()
```

- [ ] **Step 2: Run the contract and prove the evidence is absent**

Run:

```powershell
python -m unittest tests.test_opencode_routine_capability_evidence -v
```

Expected: FAIL because the evidence file does not exist.

- [ ] **Step 3: Inspect the live add-on without changing it**

In the existing OpenCode add-on terminal, run these read-only commands:

```bash
opencode --version
ha-mcp tools
curl -fsS http://127.0.0.1:4096/experimental/tool/ids
test -f /data/.config/opencode/skills/home-assistant-configuration/SKILL.md
command -v hab
hab automation --help
hab automation create --help
hab automation list
```

Do not paste or record environment variables, configuration contents, access
tokens, `/data/auth/`, `.storage/`, `secrets.yaml`, keys or certificates.

- [ ] **Step 4: Write the capability evidence and apply the hard stop**

Record the OpenCode version, successful skill-file check, `hab` path, the
presence of `automation create/list/get`, and one sorted JSON array containing
only the exact tool IDs required for authoring reads. The reviewed candidate set
is:

```json
[
  "homeassistant_get_entity_details",
  "homeassistant_get_home_context",
  "homeassistant_get_states",
  "homeassistant_search_entities",
  "skill"
]
```

Use the exact names returned by the live endpoint. Continue only when every
selected Home Assistant tool is read-only, `skill` exists, `hab automation
create/list/get` exist, and neither `hab_run` nor a general write tool is in the
selected array. End the evidence file with `Decision: SAFE TO CONTINUE` only
when all checks pass. Otherwise write `Decision: STOPPED — UNSAFE CAPABILITY
BOUNDARY`, commit the evidence, and stop the plan.

- [ ] **Step 5: Run the evidence contract**

Run:

```powershell
python -m unittest tests.test_opencode_routine_capability_evidence -v
git diff --check
```

Expected: one test passes and the diff check is clean.

- [ ] **Step 6: Commit the capability gate**

```powershell
git add docs/superpowers/evidence/2026-09-05-boop-routine-capabilities.md tests/test_opencode_routine_capability_evidence.py
git commit -m "test: prove BOOP Routine capability boundary"
```

---

### Task 2: Extract one testable bridge source while preserving one-file deployment

**Files:**
- Create: `setup/opencode/boop_opencode_bridge.py`
- Create: `tools/sync_opencode_bridge.py`
- Modify: `setup/opencode/10-boop-chatgpt.sh`
- Create: `tests/test_opencode_bridge_source.py`
- Modify: `tests/test_opencode_hook.py`

**Interfaces:**
- Consumes: the current Python heredoc between `cat >"$BRIDGE" <<'PY'` and the standalone `PY` terminator.
- Produces: `tools.sync_opencode_bridge.extract_payload(text) -> str`, `replace_payload(text, source) -> str`, and an importable bridge module that remains byte-identical to the deployed hook payload.

- [ ] **Step 1: Write failing source-synchronization tests**

Create tests that assert the standalone source exists, the hook payload equals
it exactly after newline normalization, and running the synchronizer twice is
idempotent:

```python
class BridgeSourceTest(unittest.TestCase):
    def test_hook_payload_matches_authoritative_source(self):
        hook = Path("setup/opencode/10-boop-chatgpt.sh").read_text(encoding="utf-8")
        source = Path("setup/opencode/boop_opencode_bridge.py").read_text(encoding="utf-8")
        self.assertEqual(source.rstrip() + "\n", extract_payload(hook))

    def test_replacement_is_idempotent(self):
        hook = HOOK.read_text(encoding="utf-8")
        source = SOURCE.read_text(encoding="utf-8")
        once = replace_payload(hook, source)
        self.assertEqual(once, replace_payload(once, source))
```

- [ ] **Step 2: Run the focused tests and prove the source seam is missing**

```powershell
python -m unittest tests.test_opencode_bridge_source tests.test_opencode_hook -v
```

Expected: FAIL because the standalone source and synchronizer do not exist.

- [ ] **Step 3: Extract the current bridge without behavioural changes**

Move the exact heredoc payload into `setup/opencode/boop_opencode_bridge.py`.
Create `tools/sync_opencode_bridge.py` with strict unique markers:

```python
START = 'cat >"$BRIDGE" <<\'PY\'\n'
END = '\nPY\nchmod 700 "$BRIDGE"'

def extract_payload(text: str) -> str:
    before, payload_and_after = text.split(START, 1)
    payload, after = payload_and_after.split(END, 1)
    if START in payload or END in after:
        raise ValueError("bridge markers are not unique")
    return payload.rstrip() + "\n"

def replace_payload(text: str, source: str) -> str:
    before, payload_and_after = text.split(START, 1)
    _old, after = payload_and_after.split(END, 1)
    return before + START + source.rstrip() + END + after
```

The script's CLI reads the source and hook, applies `replace_payload`, and writes
the hook only when content changes. Use it once to synchronize the deployable
hook. Do not change the bridge logic in this task.

- [ ] **Step 4: Run focused and full regression tests**

```powershell
python -m unittest tests.test_opencode_bridge_source tests.test_opencode_hook -v
python -m unittest discover -s tests -p "test_*.py" -v
git diff --check
```

Expected: focused tests and the full existing suite pass.

- [ ] **Step 5: Commit the testable bridge seam**

```powershell
git add setup/opencode/boop_opencode_bridge.py setup/opencode/10-boop-chatgpt.sh tools/sync_opencode_bridge.py tests/test_opencode_bridge_source.py tests/test_opencode_hook.py
git commit -m "refactor: make BOOP OpenCode bridge testable"
```

---

### Task 3: Add the pure Routine proposal validator and conversation state machine

**Files:**
- Modify: `setup/opencode/boop_opencode_bridge.py`
- Modify: `setup/opencode/10-boop-chatgpt.sh` (generated)
- Create: `tests/test_opencode_routine_state.py`
- Create: `tests/test_opencode_routine_proposal.py`

**Interfaces:**
- Produces: `Phase`, `RoutineProposal`, `PendingRoutine`, `RoutineRegistry`, `normalize_voice(text) -> str`, `starts_authoring(text) -> bool`, `is_confirmation(text) -> bool`, `is_cancel(text) -> bool`, `parse_envelope(text) -> AuthoringEnvelope`, `validate_proposal(raw) -> RoutineProposal`, `proposal_digest(proposal) -> str`, and `render_automation_document(proposal) -> str`.

- [ ] **Step 1: Write failing transcript and state tests**

Cover these exact cases:

```python
self.assertTrue(starts_authoring("Create a routine for sunset."))
self.assertFalse(starts_authoring("Could you make the lights automatic?"))
self.assertTrue(is_confirmation("Yes please!"))
self.assertFalse(is_confirmation("yes"))
self.assertTrue(is_cancel("No thank you"))
self.assertFalse(registry.confirm("other-conversation", "yes please", now=119.0))
self.assertFalse(registry.confirm("same", "yes please", now=121.0))
```

Use a fake monotonic clock. Prove a pending proposal expires at exactly 120
seconds, is tied to one conversation id and digest, and every cancellation or
new authoring start clears the old proposal.

- [ ] **Step 2: Write failing proposal/schema tests**

Use one allowed sun fixture and one allowed time fixture. Assert rejection of:

- missing/extra top-level keys;
- state, device, webhook and location triggers;
- conditions, templates and delays;
- services outside the exact allowlist;
- entity domain/service-domain mismatches;
- malformed or invented-looking entity ids;
- brightness outside 1–100 or attached to any service except `light.turn_on`;
- arbitrary service data; and
- free-form output without the exact `boop-routine-v1` envelope schema.

Assert deterministic canonical JSON and SHA-256 digest. Assert rendered output
uses Home Assistant's modern `triggers`, `conditions`, `actions`, and `action`
keys, includes `mode: single`, and contains no call that executes the automation.

- [ ] **Step 3: Run both suites and prove the model is absent**

```powershell
python -m unittest tests.test_opencode_routine_state tests.test_opencode_routine_proposal -v
```

Expected: FAIL because the types/functions do not exist.

- [ ] **Step 4: Implement the minimal pure model**

Use frozen dataclasses and exact allowlists:

```python
ALLOWED_SERVICES = frozenset({
    "light.turn_on", "light.turn_off",
    "switch.turn_on", "switch.turn_off",
    "media_player.media_play", "media_player.media_pause",
    "media_player.media_next_track", "media_player.media_previous_track",
})

@dataclass(frozen=True)
class RoutineProposal:
    name: str
    slug: str
    trigger: dict[str, Any]
    actions: tuple[dict[str, Any], ...]

@dataclass(frozen=True)
class PendingRoutine:
    proposal: RoutineProposal
    digest: str
    expires_at: float
```

Normalize case, Unicode whitespace and terminal punctuation only; do not fuzzy
match the entry or confirmation phrases. Render the validated proposal with
`json.dumps`, which is valid YAML input and avoids another runtime dependency.

- [ ] **Step 5: Synchronize and run focused tests**

```powershell
python tools/sync_opencode_bridge.py
python -m unittest tests.test_opencode_routine_state tests.test_opencode_routine_proposal tests.test_opencode_bridge_source -v
git diff --check
```

Expected: all focused tests pass.

- [ ] **Step 6: Commit the state and proposal boundary**

```powershell
git add setup/opencode/boop_opencode_bridge.py setup/opencode/10-boop-chatgpt.sh tests/test_opencode_routine_state.py tests/test_opencode_routine_proposal.py
git commit -m "feat: validate BOOP Routine proposals"
```

---

### Task 4: Enforce exact OpenCode read capabilities and structured authoring prompts

**Files:**
- Modify: `setup/opencode/boop_opencode_bridge.py`
- Modify: `setup/opencode/10-boop-chatgpt.sh` (generated)
- Create: `tests/test_opencode_routine_capabilities.py`
- Create: `tests/test_opencode_routine_prompt.py`

**Interfaces:**
- Consumes: exact live IDs recorded in Task 1.
- Produces: `CapabilityPolicy.authoring_tools(discovered) -> dict[str, bool]`, `CapabilityPolicy.chat_tools(discovered) -> dict[str, bool]`, `ROUTINE_AUTHORING_PROMPT`, and `OpenCodeClient.prompt(session_id, text, tools, instruction="") -> Awaitable[str]`.

- [ ] **Step 1: Write failing capability-map tests**

Instantiate `CapabilityPolicy` with the exact sorted IDs from the evidence file.
Assert normal chat maps every discovered ID to `False`. Assert authoring maps
only the recorded read IDs and `skill` to `True`. Assert missing required IDs,
duplicate IDs, and lookalikes such as `homeassistant_search_entities_admin`
raise `UnsafeCapabilityBoundary`. Assert `hab_run`, `bash`, `edit`,
`write_config_safe`, `call_service`, and every unreviewed ID stay false.

- [ ] **Step 2: Write failing prompt and client-body tests**

Use a fake HTTP client and assert:

```python
self.assertEqual(body["agent"], "general")
self.assertFalse(body["tools"]["bash"])
self.assertTrue(body["tools"][EXACT_SEARCH_TOOL_ID])
self.assertIn("home-assistant-configuration", body["parts"][0]["text"])
self.assertIn('"schema":"boop-routine-v1"', compact_prompt)
self.assertNotIn("hab automation create", ordinary_chat_prompt)
```

The authoring instruction must say: load only the named skill; inspect before
proposing; ask one question; never claim a write; return one JSON envelope and
no surrounding prose; use only live entity IDs; obey the exact V1 trigger,
service and domain limits.

- [ ] **Step 3: Run the suites and prove they fail**

```powershell
python -m unittest tests.test_opencode_routine_capabilities tests.test_opencode_routine_prompt -v
```

Expected: FAIL because the policy and authoring prompt do not exist.

- [ ] **Step 4: Implement fail-closed capability maps and prompt injection**

Build the map from the complete discovered list so unselected tools are
explicitly false. Compare required IDs by exact equality. Extend `prompt` to
accept a caller-supplied complete map; remove its ability to independently
enable anything. Prefix authoring input with the constant instruction while
leaving ordinary chat text unchanged.

- [ ] **Step 5: Synchronize and run focused/full tests**

```powershell
python tools/sync_opencode_bridge.py
python -m unittest tests.test_opencode_routine_capabilities tests.test_opencode_routine_prompt tests.test_opencode_hook -v
python -m unittest discover -s tests -p "test_*.py" -v
git diff --check
```

Expected: all tests pass; the original hook assertion is updated to prove
ordinary chat still calls `disabled_tools(await self.tool_ids())` or its exact
new equivalent.

- [ ] **Step 6: Commit the OpenCode read boundary**

```powershell
git add setup/opencode/boop_opencode_bridge.py setup/opencode/10-boop-chatgpt.sh tests/test_opencode_routine_capabilities.py tests/test_opencode_routine_prompt.py tests/test_opencode_hook.py
git commit -m "feat: gate BOOP Routine inspection tools"
```

---

### Task 5: Add the bounded `hab` automation adapter

**Files:**
- Modify: `setup/opencode/boop_opencode_bridge.py`
- Modify: `setup/opencode/10-boop-chatgpt.sh` (generated)
- Create: `tests/test_opencode_hab_automation.py`

**Interfaces:**
- Consumes: validated `RoutineProposal` and rendered automation document.
- Produces: `AutomationRecord`, `HabAutomationClient.find(slug) -> Awaitable[AutomationRecord | None]`, `create_once(proposal) -> Awaitable[AutomationRecord]`, `verify(record, proposal) -> Awaitable[bool]`, and exception types `AutomationExists`, `AutomationCreateFailed`, `AutomationVerificationFailed`.

- [ ] **Step 1: Write failing subprocess-safety tests**

Inject a fake `run_exec(*argv, input=None)` function. Assert the adapter calls
exact argument vectors equivalent to `hab automation get SLUG`, `hab automation
create SLUG -f ABSOLUTE_TEMP_PATH`, and `hab entity get automation.SLUG`.
Assert it never passes `shell=True`, a joined command string, user speech, or an
unvalidated slug. Inspect the temp file inside the fake call and assert mode
`0600`; after return and after exceptions, assert it no longer exists.

- [ ] **Step 2: Write failing idempotency and verification tests**

Cover:

- absent slug creates once;
- existing matching slug returns the verified record without creating again;
- existing conflicting slug raises `AutomationExists`;
- non-zero/malformed `hab` output fails plainly;
- timeout followed by a matching `get` is treated as success;
- timeout followed by absence does not retry automatically;
- created config mismatch fails verification; and
- entity state must be `on` before success is reported.

- [ ] **Step 3: Run and prove the adapter is absent**

```powershell
python -m unittest tests.test_opencode_hab_automation -v
```

Expected: FAIL because `HabAutomationClient` does not exist.

- [ ] **Step 4: Implement the adapter without a shell**

Use `asyncio.create_subprocess_exec` with a 15-second timeout and bounded output.
Create temporary files only below `/data/boop-wyoming/pending`, call
`os.chmod(path, 0o600)`, and unlink in `finally`. Parse JSON output strictly.
Represent inspected results explicitly:

```python
@dataclass(frozen=True)
class AutomationRecord:
    slug: str
    config: dict[str, Any]
    state: str
```

Before create, get the slug. After create or an uncertain timeout, get the slug,
compare canonical trigger/action content, and confirm `automation.SLUG` is
`on`. Never call `hab action`, `automation.trigger`, update or delete.

- [ ] **Step 5: Synchronize and run focused tests**

```powershell
python tools/sync_opencode_bridge.py
python -m unittest tests.test_opencode_hab_automation tests.test_opencode_routine_proposal tests.test_opencode_bridge_source -v
git diff --check
```

Expected: all focused tests pass.

- [ ] **Step 6: Commit the bounded writer**

```powershell
git add setup/opencode/boop_opencode_bridge.py setup/opencode/10-boop-chatgpt.sh tests/test_opencode_hab_automation.py
git commit -m "feat: add bounded BOOP automation writer"
```

---

### Task 6: Integrate the authoring state machine into Wyoming conversation turns

**Files:**
- Modify: `setup/opencode/boop_opencode_bridge.py`
- Modify: `setup/opencode/10-boop-chatgpt.sh` (generated)
- Create: `tests/test_opencode_routine_conversation.py`

**Interfaces:**
- Consumes: `RoutineRegistry`, `CapabilityPolicy`, authoring envelopes, and `HabAutomationClient`.
- Produces: `BoopRoutineCoordinator.handle(conversation_id, text) -> Awaitable[str | None]`; `None` means continue through ordinary tool-free chat.

- [ ] **Step 1: Write failing end-to-end coordinator tests**

Using fake OpenCode and `hab` clients, prove:

1. ordinary chat returns `None` and never enters authoring;
2. `create a routine` enters authoring with read-only tools;
3. clarification envelopes remain in authoring and preserve conversation state;
4. a valid proposal is read back and stored with a two-minute expiry;
5. plain `yes`, another conversation's `yes please`, and expired `yes please`
   do not call `hab`;
6. matching `yes please` calls `create_once` exactly once, reports success, and
   clears state;
7. `no thank you`, `cancel`, `stop`, unrelated speech and a fresh authoring
   request clear the old proposal;
8. unsupported/refusal envelopes never store a proposal; and
9. every exception clears write-capable state and produces short spoken text.

- [ ] **Step 2: Write failing Wyoming handler routing tests**

Assert the handler calls the coordinator before ordinary chat. If the
coordinator returns text, emit that `Handled` response and do not call ordinary
OpenCode. If it returns `None`, preserve the protected ordinary chat path with
all tools false. Assert the original Home Assistant context and conversation id
are returned unchanged.

- [ ] **Step 3: Run and prove integration is absent**

```powershell
python -m unittest tests.test_opencode_routine_conversation -v
```

Expected: FAIL because the coordinator does not exist.

- [ ] **Step 4: Implement the coordinator and handler integration**

Create one coordinator shared by handler instances. Do not let the model handle
confirmation: normalize and match `yes please` in the coordinator, then send the
stored proposal directly to `HabAutomationClient`. Keep all response strings to
one or two short spoken sentences. Increment the Wyoming handle version from
`1` to `2` and update its description to mention create-only Routines.

- [ ] **Step 5: Synchronize and run all bridge regressions**

```powershell
python tools/sync_opencode_bridge.py
python -m unittest tests.test_opencode_routine_conversation tests.test_opencode_hab_automation tests.test_opencode_routine_capabilities tests.test_opencode_routine_prompt tests.test_opencode_routine_state tests.test_opencode_routine_proposal tests.test_opencode_bridge_source tests.test_opencode_hook -v
git diff --check
```

Expected: all focused tests pass.

- [ ] **Step 6: Commit the voice flow**

```powershell
git add setup/opencode/boop_opencode_bridge.py setup/opencode/10-boop-chatgpt.sh tests/test_opencode_routine_conversation.py
git commit -m "feat: create confirmed BOOP Routines by voice"
```

---

### Task 7: Document deployment and enforce the complete regression gate

**Files:**
- Modify: `docs/BOOP-ALPHA6-SETUP.md`
- Modify: `README.md`
- Modify: `BOOP_STATUS.md`
- Modify: `BOOP_MEMORY.txt`
- Modify: `.github/workflows/build-boop-wall-resurrection.yml`
- Create: `tests/test_opencode_routine_deployment.py`

**Interfaces:**
- Consumes: the completed generated hook and full Routine authoring suite.
- Produces: one CI-gated, user-copyable startup hook plus truthful implementation-only documentation.

- [ ] **Step 1: Write the failing deployment contract**

Assert the setup guide contains the exact entry/confirmation phrases, supported
triggers/services, read-only planning boundary, fixed `hab automation create`
writer, rollback instruction, hook verification command, and physical test.
Assert it still says port 4096 is internal and ordinary chat disables tools.
Assert the workflow runs every new Routine test and verifies the hook payload
matches its source.

- [ ] **Step 2: Run and prove docs/workflow are stale**

```powershell
python -m unittest tests.test_opencode_routine_deployment -v
```

Expected: FAIL because deployment and CI do not mention Routine authoring.

- [ ] **Step 3: Update setup, status, memory and CI**

Document copying `setup/opencode/10-boop-chatgpt.sh` to the existing startup hook,
running `ha-hooks run 10-boop-chatgpt.sh`, checking `ha-hooks list` and the bridge
log, and disabling Startup hooks plus restarting OpenCode as rollback. State that
no APK change is expected. Mark the feature implemented/CI-only, not physically
green. Record Forki/Govee as future state-trigger work.

Extend the workflow's source suite if discovery already covers the new tests;
otherwise add one explicit Routine suite step. Add a shell syntax check:

```bash
bash -n setup/opencode/10-boop-chatgpt.sh
python tools/sync_opencode_bridge.py --check
```

- [ ] **Step 4: Run the complete local gate**

```powershell
python -m unittest discover -s tests -p "test_*.py" -v
git diff --check
git status --short
```

Also run `bash -n setup/opencode/10-boop-chatgpt.sh` and the synchronizer's
`--check` mode. Expected: all tests and syntax checks pass; only intended files
are modified.

- [ ] **Step 5: Commit the deployment contract**

```powershell
git add docs/BOOP-ALPHA6-SETUP.md README.md BOOP_STATUS.md BOOP_MEMORY.txt .github/workflows/build-boop-wall-resurrection.yml tests/test_opencode_routine_deployment.py
git commit -m "docs: prepare BOOP Routine authoring deployment"
```

---

### Task 8: Publish, install the hook, and perform physical acceptance

**Files:**
- Modify after evidence: `BOOP_STATUS.md`
- Modify after evidence: `BOOP_MEMORY.txt`
- Live output: existing OpenCode startup hook under `/config/opencode/startup.d/10-boop-chatgpt.sh`

**Interfaces:**
- Consumes: reviewed commits, green CI, the live OpenCode add-on, and user voice tests.
- Produces: deployed Routine authoring bridge and a protected checkpoint only after every physical gate passes.

- [ ] **Step 1: Push the feature branch and require green CI**

```powershell
$routineBranch = git branch --show-current
git push -u origin $routineBranch
$routineRunId = gh run list --branch $routineBranch --workflow build-boop-wall-resurrection.yml --limit 1 --json databaseId --jq '.[0].databaseId'
gh run watch $routineRunId --exit-status
```

Resolve the exact branch created by the worktree skill; do not invent or rename
it during the push. On failure, inspect with `gh run view $routineRunId
--log-failed`, fix under TDD and repeat. Do not deploy a failed hook.

- [ ] **Step 2: Back up and replace only the live startup hook**

Before writing, copy the current live hook to a timestamped `.bak` file in the
same Home Assistant configuration directory. Replace only
`10-boop-chatgpt.sh` with the reviewed repository file. Do not alter OpenCode
credentials, MCP configuration, Home Assistant YAML, Android or Shield apps.

- [ ] **Step 3: Restart only OpenCode and verify the bridge**

Run the hook or restart the add-on once, then verify:

```bash
ha-hooks list
ha-hooks log 10-boop-chatgpt.sh
tail -n 100 /data/boop-wyoming/bridge.log
```

Expected: one bridge process, handle version 2, safe capability preflight, no
secret output and no authentication change.

- [ ] **Step 4: Prove unchanged paths first**

Physically verify wake, ordinary BOOP conversation, one light command, play,
pause and skip. Verify ordinary OpenCode chat still exposes no tools. Any
regression stops acceptance and restores the backed-up hook.

- [ ] **Step 5: Run the harmless create-only voice test**

Choose a real non-security light and a future clock/sun time that will not occur
during the test. Say `create a routine` and supply the allowed request. Verify
entity clarification and the complete spoken read-back. First say plain `yes`
and verify nothing is created. Repeat the proposal, say `yes please`, and verify
exactly one enabled automation appears in Home Assistant and BOOP's Routines
list without running.

- [ ] **Step 6: Prove duplicate, cancellation and cleanup**

Repeat the same create request and verify BOOP reports the duplicate. Start
another proposal and say `no thank you`; verify nothing is created. Remove the
harmless test automation manually in Home Assistant after recording evidence.
Voice deletion remains unavailable.

- [ ] **Step 7: Record physical truth and create a checkpoint only if complete**

Update status/memory with the exact tested branch head, CI run, live hook digest,
automation test details and cleanup result. Commit and push the evidence. Create
an annotated tag named `checkpoint-boop-routine-authoring-` plus the exact first
seven characters of the tested functional commit, pointing to that tested
functional commit. Never reuse, move or infer an existing checkpoint tag.
