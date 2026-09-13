# BOOP External Memory Brain Design

Date: 2026-09-13
Status: design approved in chat; implementation pending written-spec review
Owner: Ryan / BOOP repository

## Goal

Make GitHub the durable external memory for BOOP so a fresh Codex/ChatGPT session can recover the latest state without Ryan re-explaining the project.
The intended interaction is deliberately short: Ryan should be able to say something like `remember lyrics, startup manager and the animation lab notification work; build it` and the agent should know where to look, what is current, what is experimental, what is physically verified, and what must not be resurrected.

This system covers the entire repository ecosystem every time, not only the core BOOP app. It includes Unified, Wall, Shield, Launcher, Animation Lab, Rally, Win7ify, Shield Turbo, Kodi experiments, Johnny Castaway work, TV shortcut apps, one-off utilities, artwork-driven APKs, prototypes, experiments, checkpoints and future BOOP-adjacent branches.

## Non-goals

- Do not merge unrelated app lineages merely to simplify memory.
- Do not copy every APK binary into `main`.
- Do not publish secrets, private device addresses, personal screenshots, third-party APKs, decompiled proprietary source, signing material or raw private diagnostics.
- Do not treat CI success as physical-device success.
- Do not overwrite dirty or concurrent work to make the registry look tidy.

## Core principle

The repository becomes a map of truth, not one giant branch containing all source.
Each app or experiment keeps its owning branch and source history. Shared context on `main` records where the truth lives and how strong the evidence is.
## Context hierarchy

The top layer stays small and mandatory. Deep files can be large.

1. `BOOP_START_HERE.md` becomes the boot router. It says what must be read first, how to resolve a named capability, and which files are authoritative.
2. `AGENTS.md` contains hard operating rules only: fetch live refs, preserve dirty work, verify before claiming completion, obey read-only/stop instructions, and update memory at closeout.
3. `BOOP_CONTEXT.md` contains durable product truth and cross-project contracts.
4. `BOOP_PROJECT_INDEX.md` lists every current or historically relevant project/branch with ownership, source path, HEAD, state and evidence level.
5. `BOOP_CAPABILITY_CATALOG.md` maps plain-English feature names and aliases to their owning branch/files, for commands such as `remember lyrics` or `remember notification tests`.
6. `BOOP_STATUS.md` is the current top-level operational snapshot: newest Unified delivery, newest standalone builds, current devices where verified, and active work.
7. `BOOP_PLANS.md` stores deferred work and future ideas without pretending they are implemented.
8. `BOOP_DECISIONS.md` is an append-only decision ledger with reason, date and superseded decision where applicable.
9. `BOOP_FAILURE_GUARDRAILS.md` stores failure-avoidance rules learned from bad sessions.
10. `BOOP_MEMORY.md` is the long-form encyclopedia for product history, experiments, hardware notes and recovered context.
11. `docs/context/projects/<slug>.md` stores deep per-project history when the root files would otherwise become unreadable.
12. `docs/context/TASK_RECEIPTS.md` records each material task closeout so later agents can see what changed and what was actually verified.

App-specific `SESSION_HANDOFF.md` files remain the most precise authority for unfinished implementation state on their owning branches.

## Mandatory startup protocol

Every BOOP or BOOP-adjacent task starts by fetching `main` and the intended owning branch from the live remote, not trusting stale `origin/*` refs.
The agent reads `AGENTS.md`, `BOOP_START_HERE.md`, `BOOP_PROJECT_INDEX.md`, and `BOOP_CAPABILITY_CATALOG.md`, then reads only the deep project/context files selected by the request.
If the request names several capabilities, the catalog resolves all of them before a build branch is chosen.
If repository context conflicts with remembered chat context, the agent must inspect the newest handoff/commit and report the conflict instead of silently choosing a story.
Missing context means `not recovered yet`, never `it never happened`.

## Project and experiment inventory

The initial migration inventories every local worktree and every live remote branch visible to the repository.
Each entry records: project slug, human name, branch, source path, exact HEAD, last meaningful commit, clean/dirty/WIP state, package/app identity when relevant, latest known version, latest build/CI evidence, latest physical-device evidence, relationship to other branches, and whether it is current, accepted, experimental, superseded, historical or abandoned.

Dirty worktrees are summarized without altering them. Unique local-only files are not published automatically; the registry says that local-only state exists and what must be inspected before cleanup or consolidation.
A branch is never declared obsolete merely because a newer version number exists. Supersession requires evidence or an explicit decision.

## Capability catalog

The catalog is the natural-language bridge between Ryan and the repository.
Each capability can have aliases. Examples include `lyrics`, `direct lyrics`, `startup manager`, `Shield defaults`, `phone iris`, `developer hue`, `notification BOOP`, `Animation Lab notification delivery`, `canonical animations`, `now playing`, `launcher advanced tools`, `Rally`, `Johnny`, `Win7ify`, `Casualty shortcut`, and `EastEnders shortcut`.

Each catalog entry points to the best current source branch plus any experiments that must be considered before composition.
It also records incompatibilities, required preserved behavior, and the strongest verification level available.

When Ryan says `remember X, Y and Z; build it`, the agent resolves X/Y/Z through this catalog, fetches those refs, reads their handoffs/evidence, then chooses or creates an integration branch without modifying the source branches.

## Evidence model

Every status claim uses one of these explicit levels: `idea`, `design`, `source-present`, `tests-green`, `CI-built`, `signed-artifact`, `installed`, `physically-verified`, or `accepted`.
Evidence can move forward only when a receipt exists. A later regression does not erase earlier evidence; it records a newer state and preserves the accepted checkpoint.
`Done` is prohibited unless the requested scope has matching evidence.
## Mandatory closeout protocol

Every material BOOP or BOOP-adjacent task updates memory before it is considered handed off.
The owning branch updates its `SESSION_HANDOFF.md` plus any project-specific status/memory files affected by the work.
The shared memory layer on `main` is updated when the task changes current routing, capability ownership, accepted state, latest build/install evidence, plans, decisions, guardrails, or cross-project relationships.
`docs/context/TASK_RECEIPTS.md` always receives a compact closeout receipt for a material task, even when no shared product decision changed.

The closeout receipt records: date/time, task, owning branch, exact pushed commit, files changed, verification performed, artifact/build reference if any, physical-device result if any, unresolved work, and whether any local-only state remains.
The agent fetches again before push and verifies the live GitHub branch HEAD after push.
If publishing is impossible, the handoff explicitly says `LOCAL ONLY / NOT SYNCED` and does not claim completion.

## Automation support

A repository script should generate the mechanical parts of the inventory from Git rather than relying on memory.
It should enumerate live remote refs, local worktrees, branch HEADs, dirty state and recent commit metadata, then produce a sanitized machine-readable snapshot for review.
Human-authored context still owns meaning: accepted versus experimental, why a decision was made, what Ryan physically observed, and which branch should win when experiments overlap.

The generator must never auto-commit, auto-push, switch dirty branches, reset worktrees, install APKs, grant permissions, or publish untracked private material.
Its output is an evidence aid, not an authority that can overrule a handoff.

## Failure guardrails

The initial guardrail set includes: no diff means no file change; no build receipt means no build claim; no package/version query means no install claim; no device observation means no physical-verification claim; missing context is not proof something never existed; branch names and version numbers do not prove lineage; do not redo a completed experiment until its handoff has been read; never silently replace an accepted asset; and never infer that a local checkout matches GitHub without checking the live remote HEAD.

A failed or partial task still gets a receipt. The point of memory is to prevent the next agent spending another twenty minutes walking into the same wall.
## Initial migration sequence

1. Snapshot live `main` and all remote branch heads.
2. Snapshot all local BOOP worktrees, including dirty/WIP state without modifying them.
3. Read the newest handoff/status/memory documents from every branch that contains unique work or evidence.
4. Build the project index and capability catalog from that evidence.
5. Reconcile overlapping Unified experiments by lineage and verification, not by highest version number alone.
6. Record accepted APK/install evidence separately from newer unverified experiments.
7. Populate durable decisions, plans, guardrails and long-form memory.
8. Add the safe inventory generator and validate that it cannot mutate project state.
9. Review the generated context against live refs and selected dirty worktrees.
10. Publish documentation/tooling changes only, then verify the live GitHub heads.

The migration does not combine application code or produce a new APK. A later request such as `remember X Y Z; build it` uses the completed memory system to perform a scoped integration task.

## Success criteria

A fresh session with repository access can identify the current owner and evidence for any major BOOP or adjacent project from `BOOP_START_HERE.md` plus the catalog, without Ryan narrating its history.
It can distinguish the installed/accepted build from newer experiments and can locate dirty local-only work when running on the laptop.
It can resolve a multi-capability prompt to the correct branches and handoffs before touching source.
At task end it leaves enough durable state that another local Codex or GitHub-connected session can continue from the repository with no conversational handoff.

The system is considered healthy only if its closeout ritual is followed every material task. The memory brain is part of the product workflow, not occasional cleanup.