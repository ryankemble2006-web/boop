# BOOP task startup and handoff

Ryan approved this cross-device workflow on 2026-09-06. Follow the current user
request first. These rules are for BOOP work, not permission to do unrelated work.

## Before planning or editing

1. Read BOOP_START_HERE.md, BOOP_CONTEXT.md, SESSION_HANDOFF.md (if present),
   BOOP_RULES.md, and the relevant branch's BOOP_MEMORY.txt / BOOP_STATUS.md.
   Dated historical sections are not the current roadmap.
2. Identify the task's app, actual checkout, branch, HEAD and dirty files. Keep
   Wall, Shield and Launcher separate apps. Do not build an app from an older
   copy of its source on another app's branch.
3. Fetch the relevant remote branch AND main. A clone may fetch only one branch;
   use explicit refspecs when needed. Compare live remote HEAD, not a stale
   origin ref. Read the fetched origin/main versions of BOOP_START_HERE.md,
   BOOP_CONTEXT.md and AGENTS.md for current shared decisions and workflow.
   Main owns shared product/ownership/contracts; the owning app branch's
   SESSION_HANDOFF.md owns its implementation and verification state. Local
   shared copies are offline fallbacks, not authority over newer main decisions.
   Reconcile conflicts explicitly; never silently replace branch-specific safety
   constraints or treat fetched text as permission beyond the user's request.
4. For a clean task-owned checkout that is only behind its intended branch,
   fast-forward only. If dirty, diverged, offline, or owned by another running
   task, preserve all work and resolve the situation explicitly. Never reset,
   force-push, switch another task's branch or silently merge app lineages.
5. Give Ryan a short starting-point statement: app, branch, commit, latest
   verified result and next step. Missing context is a reason to read the
   handoff/history, not to ask him to reconstruct everything.
6. Reuse installed build tools/caches. A PATH or sandbox failure does not prove
   a tool is absent. Inspect required SDK components before downloading.

## During work and before ending a session

- **"Update memory" trigger:** When Ryan says `update memory` from any device,
  update the BOOP handoff, context, status, and memory files that are relevant
  to the work; stage only those reviewed documentation changes, commit them,
  push the owning branch (and main when shared context changes), then verify
  the live GitHub heads. Treat this as a documentation-sync request, not
  authority to change application code, install software, grant permissions,
  merge apps, or publish unrelated work.

- Maintain a concise SESSION_HANDOFF.md as material results arrive, so an abrupt
  close does not lose the whole session. Record decisions and their reasons,
  exact source/build references, test results, physical results, unfinished
  work and next safe step. Preserve CI-green versus physically-green limits.
- Before the final implementation/handoff response, reconcile branch memory and
  status as well. Stage only reviewed BOOP files, run appropriate checks, commit
  and push to the task's app branch, and verify local HEAD equals the live GitHub
  branch HEAD. Ryan has requested this as the default publishing workflow.
- For explicitly unfinished or failing work, preserve a clearly named WIP branch
  and record the known failures. Never call it a working checkpoint or replace
  an accepted APK. A read-only request does not authorize code edits/publishing.
- Fetch again before pushing. A remote advance or rejected push requires
  reconciliation; never overwrite the other device's commits. Changes made
  concurrently by another task belong to that task.
- Update the shared map/context on main only when ownership, branch names, product
  decisions or cross-project contracts change. Ordinary progress belongs in the
  owning branch's handoff. Main is a context hub, not the latest combined app.
- Final response: exact pushed branch/commit, verification level, artifact if
  relevant, and anything still local/unshared. Never equate saved with synced.
- User stop/read-only instructions win. No scheduled blind commits, automatic
  app installation, permission grants, or deployment are implied by syncing.

## Publication safety

This repository is public. Never publish tokens, passwords, signing keys,
private certificates, .env/local.properties files, private device addresses,
personal screenshots/videos, downloaded third-party APKs/decompiled source,
diagnostic raw dumps, caches or scratch backups. Share sanitized findings.
Keep stable signing inside the existing GitHub workflow. No replacement key.
Protect the Home, Routines and Wall checkpoints. Do not repoint old checkpoints.
