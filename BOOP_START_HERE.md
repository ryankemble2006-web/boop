# BOOP Launcher v1: signed standalone candidate

Updated 2026-09-12. Owner `boop-shield-launcher-standalone`; project `shield-launcher/`.
Package `com.boop.shieldlauncher`, version `1 / 1.0.0-launcher-tools`.
Signed source `72dde252f4f73487d9b99af52ea189777822a795`; Actions run `34684440925`
succeeded, artifact `10295068406` (BOOP-Launcher), 7,250,154-byte APK.
SHA256 `5293f65d53299bf49b45c10911693f1e3b97c6effd1a87a3734c2ee88037bb7b`.
Permanent signature, downloaded package/version/hash, actual APK dependency closure,
Android build and focused startup/defaults/close tests verified. No physical install
or acceptance on the other Shield. Failed integration work was not imported.
Read docs/verification/2026-09-12-shield-launcher-v1.md for the full receipt and limits.
Shared main product exception published at `af0db837bf9dde26d16c632fe75d8b5bca1f7fc0`.


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
