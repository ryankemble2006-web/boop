# BOOP External Memory Brain Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a durable GitHub-backed BOOP memory system that inventories every BOOP-adjacent branch/worktree, resolves plain-English capability names to authoritative sources, records evidence honestly, and forces a memory closeout after every material task.

**Architecture:** Keep `main` as a documentation/context hub, not a merged application branch. A small mandatory router (`AGENTS.md` + `BOOP_START_HERE.md`) points to structured root memory files and per-project deep context under `docs/context/`; a safe Python inventory generator produces mechanical Git evidence without mutating worktrees or remotes.

**Tech Stack:** Markdown, Git, Python 3 standard library, PowerShell/Git CLI for verification.

**Spec:** `docs/superpowers/specs/2026-09-13-boop-external-memory-brain-design.md`

## Global Constraints

- Cover the entire BOOP repository ecosystem every time, including Unified, Wall, Shield, Launcher, Animation Lab, Rally, Win7ify, Shield Turbo, Kodi experiments, Johnny Castaway, TV shortcuts, utilities, artwork APKs, prototypes and future adjacent branches.
- Do not merge unrelated app lineages merely to simplify memory.
- Do not publish secrets, private device addresses, personal screenshots, third-party APKs, decompiled proprietary source, signing material or raw private diagnostics.
- Do not overwrite dirty or concurrent work.
- Distinguish `idea`, `design`, `source-present`, `tests-green`, `CI-built`, `signed-artifact`, `installed`, `physically-verified`, and `accepted` evidence levels.
- Every material task updates its owning handoff and app memory/status plus the shared memory layer and `docs/context/TASK_RECEIPTS.md` before handoff.
- The generator must never auto-commit, auto-push, switch dirty branches, reset worktrees, install APKs, grant permissions, or publish untracked private material.

---
### Task 1: Safe mechanical inventory generator

**Files:**
- Create: `scripts/context_inventory.py`
- Create: `tests/test_context_inventory.py`
- Create: `docs/context/generated/.gitkeep`

**Interfaces:**
- Consumes: repository root path and Git CLI output only.
- Produces: `collect_inventory(repo: Path) -> dict`, `render_markdown(snapshot: dict) -> str`, and CLI output to stdout or an explicitly named file.

- [ ] **Step 1: Write failing tests for non-mutating inventory behavior**

Create tests using `unittest.mock` that require the collector to enumerate `git for-each-ref refs/remotes/origin`, `git worktree list --porcelain`, `git status --porcelain`, and `git log -1 --format=...` without invoking checkout, reset, commit, push, add, install or ADB commands.

- [ ] **Step 2: Run the focused tests and confirm RED**

Run: `python -m unittest tests.test_context_inventory -v`
Expected: FAIL because `scripts.context_inventory` does not exist.

- [ ] **Step 3: Implement the minimal read-only collector**

Implement `run_git(repo, *args)` with `subprocess.run(..., check=True, capture_output=True, text=True)`; parse remote refs into branch/head pairs; parse worktree porcelain into path/head/branch records; query each worktree's dirty status and last commit metadata; never read untracked file contents.

- [ ] **Step 4: Add sanitised Markdown rendering and CLI**

The renderer must include branch, exact HEAD, worktree path relative to the repository where possible, dirty boolean/count, and last commit timestamp/subject. CLI flags: `--repo`, `--json-out`, `--markdown-out`; no write occurs unless an output flag is supplied.

- [ ] **Step 5: Run tests and a real dry inventory**

Run: `python -m unittest tests.test_context_inventory -v`
Run: `python scripts/context_inventory.py --repo .`
Expected: tests PASS; stdout inventory contains current refs/worktrees and `git status --porcelain` remains unchanged afterward.

- [ ] **Step 6: Commit the generator and tests**

Commit: `feat(context): add read-only BOOP inventory generator`

### Task 2: Mandatory startup router and failure guardrails

**Files:**
- Create: `AGENTS.md`
- Replace: `BOOP_START_HERE.md`
- Create: `BOOP_FAILURE_GUARDRAILS.md`
- Create: `BOOP_CONTEXT.md`

**Interfaces:**
- Consumes: the approved design and the inventory/catalog files created by later tasks.
- Produces: a deterministic startup reading order and hard rules that every future BOOP task can follow.

- [ ] **Step 1: Write a documentation contract test**

Add `tests/test_context_contract.py` asserting that startup docs contain the mandatory phrases/paths: live fetch of `main` and owning branch, `BOOP_PROJECT_INDEX.md`, `BOOP_CAPABILITY_CATALOG.md`, `SESSION_HANDOFF.md`, dirty-work preservation, missing-context rule, closeout receipt rule, and live remote HEAD verification.

- [ ] **Step 2: Run the contract test and confirm RED**

Run: `python -m unittest tests.test_context_contract -v`
Expected: FAIL because the new mandatory files/rules are absent on current `main`.

- [ ] **Step 3: Create hard operating rules in `AGENTS.md`**

Keep it concise. Require: fetch live refs before edits; inspect exact worktree/branch/HEAD/dirty state; resolve named capabilities through the catalog; never infer lineage from version numbers; never rewrite another task's dirty work; no success claim without matching evidence; obey stop/read-only; update app handoff/memory/status and shared receipt at closeout; fetch again before push; verify live GitHub HEAD.

- [ ] **Step 4: Rebuild `BOOP_START_HERE.md` as the small boot router**

Reading order: `AGENTS.md` -> `BOOP_PROJECT_INDEX.md` -> `BOOP_CAPABILITY_CATALOG.md` -> requested project deep file(s) -> owning `SESSION_HANDOFF.md` -> relevant status/memory. Include the `remember X, Y and Z; build it` resolution procedure and explicitly state that missing context means `not recovered yet`.

- [ ] **Step 5: Create durable context and guardrail files**

`BOOP_CONTEXT.md` stores product/cross-project truths only. `BOOP_FAILURE_GUARDRAILS.md` records the initial rules from the spec, including no diff/no change claim, no build receipt/no build claim, no package query/no install claim, no device observation/no physical claim, no silent asset replacement, and no stale-origin sync claims.

- [ ] **Step 6: Run contract tests and commit**

Run: `python -m unittest tests.test_context_contract -v`
Expected: PASS.
Commit: `docs(context): restore mandatory BOOP startup contract`

### Task 3: Repository-wide project index and deep project records

**Files:**
- Create: `BOOP_PROJECT_INDEX.md`
- Create: `docs/context/projects/README.md`
- Create: `docs/context/projects/<project-slug>.md` for each current or historically relevant family discovered by the inventory.
- Generate for review: `docs/context/generated/inventory.json`
- Generate for review: `docs/context/generated/inventory.md`

**Interfaces:**
- Consumes: Task 1 inventory output plus live branch handoff/status/memory files and local dirty-state metadata.
- Produces: one row per project family with branch/source/HEAD/state/evidence/deep-file pointers; deep records preserve lineage and unique experiments.

- [ ] **Step 1: Generate a fresh live/local mechanical snapshot**

Fetch `main` and all visible remote refs without checking them out, then run:
`python scripts/context_inventory.py --repo <BOOP-root> --json-out docs/context/generated/inventory.json --markdown-out docs/context/generated/inventory.md`
Record the pre/post status of every dirty worktree to prove the generator did not mutate them.

- [ ] **Step 2: Group branches into project families without deleting history**

Create families at minimum for Unified, Wall, Shield Home/overlay, standalone Shield Launcher, Launcher, Animation Lab/animation experiments, audio experiments, lyrics/startup experiments, Rally, Win7ify, Shield Turbo, Kodi/game work, Johnny Castaway, TV shortcuts/artwork APKs, relay/site/setup utilities, and historical checkpoints. Any additional live branch with unique evidence gets its own family or explicit historical entry.

- [ ] **Step 3: Read each family's newest authoritative handoff/status/memory evidence**

Use `git show <live-ref>:SESSION_HANDOFF.md` and equivalent status/memory paths where present. For dirty laptop worktrees, read only tracked handoff/status plus `git diff --stat`/status unless the task-specific context requires more; never publish private untracked contents automatically.

- [ ] **Step 4: Write `BOOP_PROJECT_INDEX.md`**

Columns: project/family, current owner branch, exact live HEAD, source path/package where known, state (`current`, `accepted`, `experimental`, `superseded`, `historical`, `abandoned`, `local-WIP`), strongest evidence level, latest known delivery/version where evidenced, local dirty-state note, deep context link.

- [ ] **Step 5: Write per-project deep records**

Each record includes aliases, branch lineage, accepted checkpoints, current experiments, latest verified evidence, dirty/local-only warnings, known incompatibilities, important files/handoffs, and next safe step. Do not invent supersession or physical verification.

- [ ] **Step 6: Cross-check the index against both inventory surfaces and commit**

Verify every live remote branch is represented directly or grouped under a documented family, and every local worktree is represented or explicitly classified as duplicate/historical scratch. Commit: `docs(context): inventory all BOOP project lineages`.

### Task 4: Capability catalog and composable memory

**Files:**
- Create: `BOOP_CAPABILITY_CATALOG.md`
- Create: `BOOP_STATUS.md`
- Create: `BOOP_PLANS.md`
- Create: `BOOP_DECISIONS.md`
- Create: `BOOP_MEMORY.md`

**Interfaces:**
- Consumes: Task 3 project/deep records and verified branch evidence.
- Produces: natural-language alias resolution for future multi-capability build requests plus durable status/history/decision context.

- [ ] **Step 1: Extend the documentation contract test for capability resolution**

Require catalog entries for known aliases including `lyrics`, `direct lyrics`, `startup manager`, `Shield defaults`, `phone iris`, `developer hue`, `notification BOOP`, `Animation Lab notification delivery`, `canonical animations`, `now playing`, `launcher advanced tools`, `Rally`, `Johnny`, `Win7ify`, `Casualty shortcut`, and `EastEnders shortcut`.

- [ ] **Step 2: Run the focused contract test and confirm RED**

Run: `python -m unittest tests.test_context_contract -v`
Expected: FAIL because the catalog/status/plan/decision/memory files do not yet contain the required mappings.

- [ ] **Step 3: Build `BOOP_CAPABILITY_CATALOG.md`**

For each capability record: canonical name; aliases/phrases; owning branch/project; exact evidence pointer; integration candidates/experiments; preserved behavior; known conflicts; strongest verification level; deep-context link. Include a worked resolution example for `remember lyrics, startup manager and the animation lab notification work; build it` that resolves refs first and creates a separate integration branch rather than modifying source branches.

- [ ] **Step 4: Populate operational and durable memory files**

`BOOP_STATUS.md` records latest evidenced deliveries and active WIP. `BOOP_PLANS.md` separates deferred ideas from implemented work. `BOOP_DECISIONS.md` uses dated append-only entries with decision/reason/supersedes. `BOOP_MEMORY.md` preserves long-form history and recovered project knowledge, linking deep records rather than duplicating giant code descriptions.

- [ ] **Step 5: Run contract tests and manually resolve sample prompts**

Run: `python -m unittest tests.test_context_contract -v`.
Manually resolve at least: `remember lyrics + startup manager`; `remember notification BOOP + animation lab delivery`; `remember Johnny + Rally`; `remember launcher advanced tools + Shield defaults`. Each must yield explicit refs and evidence without relying on chat memory.

- [ ] **Step 6: Commit capability memory**

Commit: `docs(context): add BOOP capability catalog and durable memory`.

### Task 5: Closeout receipts and permanent update ritual

**Files:**
- Create: `docs/context/TASK_RECEIPTS.md`
- Create: `docs/context/README.md`
- Modify: `AGENTS.md`
- Modify: `BOOP_START_HERE.md`
- Modify: relevant project/deep memory files from Tasks 3-4

**Interfaces:**
- Consumes: material task outcome, owning branch commit/evidence, shared context changes.
- Produces: a durable receipt that lets the next session resume without conversational explanation.

- [ ] **Step 1: Add receipt-schema assertions to the contract test**

Require the receipt document to define fields for timestamp, task, owning branch, exact pushed commit, changed files, verification, artifact/build reference, physical-device result, unresolved work, local-only state, shared-context update, and live-remote verification.

- [ ] **Step 2: Run the test and confirm RED**

Run: `python -m unittest tests.test_context_contract -v`
Expected: FAIL until the receipt schema and closeout instructions exist.

- [ ] **Step 3: Create the context maintenance guide and receipt ledger**

`docs/context/README.md` explains which files are mechanical vs human-authored and the exact startup/closeout workflow. `TASK_RECEIPTS.md` begins with the schema and records this external-memory migration as its first material receipt once publishing evidence exists.

- [ ] **Step 4: Wire the ritual into hard startup rules**

Ensure `AGENTS.md` states that every material BOOP-adjacent task updates the owning `SESSION_HANDOFF.md`, applicable status/memory files, and shared receipt before final handoff. Shared root files are updated whenever ownership, routing, evidence, plans, decisions, guardrails or cross-project relationships changed. Documentation-only memory updates are committed and pushed, then live remote HEAD is verified.

- [ ] **Step 5: Run all context tests**

Run: `python -m unittest tests.test_context_inventory tests.test_context_contract -v`
Expected: PASS with zero failures.

- [ ] **Step 6: Commit closeout protocol**

Commit: `docs(context): require BOOP memory closeout receipts`.

### Task 6: Migration verification and publication

**Files:**
- Modify: `docs/context/TASK_RECEIPTS.md`
- Modify: `BOOP_STATUS.md`
- Modify: generated inventory snapshots if live refs changed during implementation.

**Interfaces:**
- Consumes: completed Tasks 1-5 and current live GitHub refs.
- Produces: a published, remote-verified context brain on `main` with a truthful migration receipt.

- [ ] **Step 1: Fetch again and detect concurrent remote movement**

Run an explicit fetch of `main` plus all remote heads. Compare implementation base with live `origin/main`. If `main` advanced, reconcile without force and re-run inventory/context checks before publication.

- [ ] **Step 2: Audit for forbidden/private material**

Review staged paths and text for private addresses, credentials/tokens, signing material, personal screenshot paths, APK/decompiled-source payloads and raw diagnostic dumps. Generated inventory may contain branch/worktree metadata only; it must not include untracked file contents.

- [ ] **Step 3: Run full verification**

Run: `python -m unittest tests.test_context_inventory tests.test_context_contract -v`.
Run: `git diff --check`.
Run the inventory generator twice and compare normalized outputs apart from expected timestamps; verify representative live refs and dirty worktrees against `git` directly.

- [ ] **Step 4: Write the migration receipt with exact evidence**

Record the implementation branch/commit(s), tests run, generated inventory scope, live GitHub comparison, explicit statement that no app code/APK/install/permission/signing work occurred, unresolved local dirty work, and final publication target.

- [ ] **Step 5: Publish documentation/tooling only**

Fetch once more. Push the reviewed implementation branch, then fast-forward or merge it into `main` using a non-force operation only after confirming no conflicting remote advance. Do not touch app branches or dirty worktrees.

- [ ] **Step 6: Verify live GitHub HEAD and re-fetch key files**

Use both local `git ls-remote origin refs/heads/main` and the GitHub connector to confirm live `main` HEAD and retrieve `BOOP_START_HERE.md`, `BOOP_PROJECT_INDEX.md`, and `BOOP_CAPABILITY_CATALOG.md`. The task is complete only when those live files reflect the new memory system.

- [ ] **Step 7: Final receipt amendment if publication SHA differs**

If publication creates a merge/reconciliation commit, update the receipt with the exact live `main` SHA in a documentation-only commit, push, and verify that final SHA again.
