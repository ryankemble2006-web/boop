# BOOP Launcher standalone status

Updated 2026-09-12. Branch `boop-shield-launcher-standalone`.
Source project `shield-launcher/`, package `com.boop.shieldlauncher`, version 1.
Extraction is implemented from committed v145 defaults source, not v146.
Local unsigned Android assembly, actual APK dependency/integrity verification and
all focused tests pass after the final private Shield settings-router repair.
Permanent-signed CI artifact remains pending.
No second-Shield installation or physical/visual acceptance. No Unified changes.
See SESSION_HANDOFF.md and shield-launcher/SOURCE.md for provenance and boundaries.

## Inherited history below, not this branch routing

# BOOP — start here on either device

Updated 2026-09-06. Repository: [ryankemble2006-web/boop](https://github.com/ryankemble2006-web/boop).

**One repository, separate app branches.** The branches do not need the same
commit ID. Each task must match the current GitHub HEAD of its own app branch
and read the other branches when cross-project context is needed.

| Work | Authoritative branch | Source | Owner / state |
| --- | --- | --- | --- |
| Shield Home + Deezer puppet | [boop-shield-media-puppetry](https://github.com/ryankemble2006-web/boop/tree/boop-shield-media-puppetry) | shield-overlay/ | Laptop; H1 play/pause and lower placement user-tested |
| BOOP Wall voice/eyes | [boop-wall-resurrection](https://github.com/ryankemble2006-web/boop/tree/boop-wall-resurrection) | source/ plus materialization scripts | Working voice/control baseline; preserve it |
| BOOP Launcher | [boop-launcher-alpha1](https://github.com/ryankemble2006-web/boop/tree/boop-launcher-alpha1) | launcher/ | Android-led development; read launcher/README.md |
| Wall-to-Launcher swipe draft | [boop-wall-launcher-handoff-wip](https://github.com/ryankemble2006-web/boop/tree/boop-wall-launcher-handoff-wip) | source/ and focused tests | WIP snapshot, not a verified installable update |
| Routine-authoring research | [boop-routine-authoring-v1](https://github.com/ryankemble2006-web/boop/tree/boop-routine-authoring-v1) | docs/superpowers/ and tests/ | Capability evidence/design; do not infer full authoring implementation |
| Older Shield Home lineage | [boop-shield-home-implementation](https://github.com/ryankemble2006-web/boop/tree/boop-shield-home-implementation) | shield-overlay/ | Historical/reference; new Shield work uses puppetry branch |
| Cross-project context | main | AGENTS.md, BOOP_CONTEXT.md, this map | Context hub; app files here can be historical |

## Start a new Work task

Attach this repository and choose the branch for the app you are developing.
Read AGENTS.md and that branch's SESSION_HANDOFF.md before making changes.
Fetch and compare the live branch heads: a downloaded folder is not a live sync.
Read the fetched main versions of this map, BOOP_CONTEXT.md and AGENTS.md.
Main owns shared decisions/contracts; the app branch's SESSION_HANDOFF.md owns
its current implementation and verification evidence. Branch-local shared files
are fallback copies when offline, and must not hide newer main decisions.
Root README files inherited from Alpha 1 do not override the current app map.

Cross-project reference does not require merging the apps. For example, a
Launcher task can read Wall's current manifest/source from the Wall branch and
Shield's notes from the Shield branch. Use explicit refs in shallow/single-branch
environments, for example:

    git fetch --no-tags origin refs/heads/boop-wall-resurrection:refs/remotes/origin/boop-wall-resurrection
    git show origin/boop-wall-resurrection:SESSION_HANDOFF.md

Do not check out another branch over dirty work. If connectivity or permissions
prevent synchronization, state that immediately and do not claim up-to-date.

## End a session / hand over

Update SESSION_HANDOFF.md, branch memory/status, and relevant decisions; run
appropriate checks; commit reviewed files; push the owning branch; verify the
live GitHub HEAD matches. Label unverified work as WIP and retain its failures.
A task is not synced merely because a file was saved on one laptop.

This is a task startup/handoff workflow, not a real-time folder mirror or a
guarantee that a chat without repository access can read files. Already-open
tasks must explicitly reread new instructions. Abrupt shutdowns/offline work
can still leave unpushed changes, so record/publish at useful milestones.

## Cross-app contract

- Wall: com.boop.alpha1. Launcher: com.boop.launcher. Shield: com.boop.shieldoverlay.
- Wall and Launcher stay independently launchable. Do not merge packages.
- Proposed Wall eyes -> deliberate left swipe -> Launcher is still WIP.
- Existing tap/hold/voice/HA behaviour must survive the swipe work.
- Launcher may open Wall by its package; permissions/return-strip behaviour
  require their own consent and device testing.
- Stable update signing remains in the existing GitHub workflows.

Read BOOP_CONTEXT.md for durable product decisions and each branch's handoff
for current evidence. Do not publish private third-party inspection artifacts.
