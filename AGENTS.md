# BOOP task startup and handoff

## Explicit standalone Shield Launcher, 2026-09-12

The user requested a launcher-and-advanced-tools-only APK for another Shield and
approved the split, excluding the unfinished integration changes from this task.
Owning branch/worktree: **`boop-shield-launcher-standalone`**. Project:
**`shield-launcher/`**. App label **BOOP Launcher**, package **`com.boop.shieldlauncher`**.
Frozen source input: `boop-shield-defaults@503cdb63d64716c9c1a568aadca97ba1d24680cd`
(the committed v145 launcher/defaults line). Initial split source is `72dde252`.
Read its live SESSION_HANDOFF.md, BOOP_STATUS.md and shield-launcher/SOURCE.md
for current build/signature/artifact and physical-test receipts.

This explicitly approved standalone product is an exception to the general
one-Unified-APK rule, not a rollback or replacement of the combined delivery below.
Keep `com.boop.alpha1` and historical `com.boop.shieldhome` source/install histories
untouched. Do not silently transplant v146 phone/lyrics/integration changes into
this frozen split. It retains launcher/Now Playing/advanced tools, but not voice,
Wall/phone, HA dashboard/credentials, overlay puppet or Animation Lab.
The receiving Shield owns its new ADB identity and exact Restore records. No
permission grant, default-HOME change, Apply/Undo or installation follows from
signing/publishing; those remain explicit user actions. Visual/device acceptance
is separate from compilation, functional tests and permanent-signature checks.

## Current combined device delivery, 2026-09-12

Ryan explicitly authorized the completed phone iris-colour/menu/crash fixes,
Lyrics availability preflight and Startup Manager/defaults to be combined.
Owning branch/worktree: **`boop-unified-v146-integration`**.
Version **146 / 1.2.146-phone-lyrics-startup**, package `com.boop.alpha1`, is
installed on both Pixel 10 Pro XL and Shield; each installed APK hash matches.
Signed build: `c868421e021ab4e0d3775f71c4fd1b484c1a88c4`, Actions `34683100171`.
APK SHA256: `4ee558ddf81be95371f07bf5025f581b397e6f16bc47895727fc91e45f576adc`.

Read that branch's live SESSION_HANDOFF.md, BOOP_STATUS.md and
`docs/verification/v146-combined.md` for exact merge inputs and device evidence.
New combined work must preserve all three fixes rather than start from an older
isolated v142/v145 branch. Former input branches/checkpoints remain rollback and
provenance; the standalone Animation Lab is unchanged. Main remains a context hub.
This is agent-verified installation with scoped physical checks, not blanket user
visual/acoustic, preset Apply/Undo/reboot or all-feature acceptance. Current user
instructions and newer live owning-branch evidence still win.

Ryan approved this cross-device workflow on 2026-09-06. Follow the current user
request first. These rules are for BOOP work, not permission to do unrelated work.

## Before planning or editing

1. Read BOOP_START_HERE.md, BOOP_CONTEXT.md, SESSION_HANDOFF.md (if present),
   BOOP_RULES.md, and the relevant branch memory/status files. Dated historical
   sections are not the current roadmap.
2. **Canonical app work now uses `boop-unified`.** BOOP Wall, Launcher and Shield
   were merged into one signed APK candidate on 2026-09-07. Their former app
   branches remain protected rollback/reference lineages and must not be silently
   deleted, repointed or treated as the normal source for new feature work.
3. Fetch the intended remote branch AND main. Compare live remote HEAD, not a stale
   origin ref. Main owns shared product/ownership/contracts; the owning branch's
   SESSION_HANDOFF.md owns implementation and verification state. For unified app
   work, also read `BOOP_UNIFIED_MEMORY.md` and `unified/SOURCE_HEADS.md`.
4. For a clean task-owned checkout that is only behind its intended branch,
   fast-forward only. If dirty, diverged, offline, or owned by another running
   task, preserve all work and resolve the situation explicitly. Never reset,
   force-push, switch another task's branch or silently overwrite concurrent work.
5. Give Ryan a short starting-point statement: branch, commit, latest verified
   result and next step. Missing context is a reason to read the handoff/history,
   not to ask him to reconstruct everything.
6. Reuse installed build tools/caches. A PATH or sandbox failure does not prove a
   tool is absent. Inspect required SDK components before downloading.

## Unified BOOP contract

- Canonical branch: `boop-unified`. Ryan's selected 2026-09-10 overhaul is owned by
  `boop-canonical-rebuild`; use its live handoff for that scope. Eyes are separate
  work for later transplant. Preserve canonical and standalone rollback state.
- Canonical package: `com.boop.alpha1`.
- One APK contains the Wall, Launcher and Shield bodies.
- Automatic profile contract: Android TV/Leanback -> Shield; Pixel 7 Pro -> Wall;
  other non-TV Android devices with `smallestScreenWidthDp >= 600` -> Wall;
  sub-600dp handheld Android -> Launcher. Preserve the internal recovery override.
- Wall keeps the permanent BOOP signer/package identity for the cleanest update
  path. Old standalone Launcher (`com.boop.launcher`) and Shield
  (`com.boop.shieldoverlay`) package histories remain reference/rollback only.
- Shared behavior should be implemented once in the unified lineage where
  practical. Do not create a new parallel app branch merely to make a small
  feature easier.
- CI-green, signed and physically accepted remain separate states. Never promote
  a unified checkpoint as physically accepted until Ryan says it worked on real
  hardware.

## During work and before ending a session

- **"Update memory" trigger:** When Ryan says `update memory` from any device,
  update the BOOP handoff, context, status, and memory files relevant to the work;
  stage only reviewed documentation changes, commit/push the owning branch (and
  main when shared context changes), then verify live GitHub heads. This is a
  documentation-sync request, not authority to change app code, install software,
  grant permissions, change signing, or publish unrelated work.
- Maintain a concise SESSION_HANDOFF.md as material results arrive. Record exact
  source/build references, tests, physical results, unfinished work and next safe
  step. Preserve CI-green versus physically-green limits.
- Before final implementation/handoff, reconcile branch memory/status, run
  appropriate checks, commit reviewed files, push, and verify live GitHub HEAD.
- For explicitly unfinished or failing work, preserve a clearly named WIP state
  and record failures. Never call it a working checkpoint or replace an accepted
  APK merely because its version number is higher.
- Fetch again before pushing. A remote advance or rejected push requires
  reconciliation; never overwrite another device's commits.
- Update main only when ownership, branch names, product decisions or cross-project
  contracts change. Ordinary progress belongs in `boop-unified` handoff/status.
- Final response: exact pushed branch/commit, verification level, artifact if
  relevant, and anything still local/unshared. Never equate saved with synced.
- User stop/read-only instructions win. No automatic app installation, permission
  grants or device deployment are implied by a build.

## Canonical deployment and rollback rule

- One canonical BOOP APK lineage. Make one intentional functional change per
  version whenever practical; do not bundle unrelated tweaks because a build is
  already open.
- A physically accepted build creates the rollback point. Record exact Git
  commit/tag, workflow run, signed artifact and physical result.
- If the next update breaks, return to the exact last physically accepted Git
  checkpoint/artifact. Never guess from filenames, timestamps or remembered APK
  names.
- Git history/artifacts are the archive. A deployment folder is not an archive.
  After a replacement build is physically accepted, keep only current signed
  `BOOP.apk` and, if useful, one clearly identified last-good APK locally.
- Preserve historical branches/checkpoints in GitHub even after local APK clutter
  is removed. Rollback provenance belongs in Git, not filenames.

## Publication safety

This repository is public. Never publish tokens, passwords, signing keys, private
certificates, .env/local.properties files, private device addresses, personal
screenshots/videos, downloaded third-party APKs/decompiled source, diagnostic raw
dumps, caches or scratch backups. Share sanitized findings. Keep stable signing
inside the existing GitHub workflow. No replacement key. Protect established
physical checkpoints and never repoint old checkpoint tags.
