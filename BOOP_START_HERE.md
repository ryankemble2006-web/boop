# BOOP — start here on either device

Updated 2026-09-07. Repository: [ryankemble2006-web/boop](https://github.com/ryankemble2006-web/boop).

**One repository, separate app branches.** The branches do not need the same
commit ID. Each task must match the current GitHub HEAD of its own app branch
and read the other branches when cross-project context is needed.

| Work | Authoritative branch | Source | Owner / state |
| --- | --- | --- | --- |
| Shield Home + Deezer puppet | [boop-shield-media-puppetry](https://github.com/ryankemble2006-web/boop/tree/boop-shield-media-puppetry) | shield-overlay/ | Laptop; H1 play/pause and lower placement user-tested |
| BOOP Wall voice/eyes | [boop-wall-resurrection](https://github.com/ryankemble2006-web/boop/tree/boop-wall-resurrection) | source/ plus materialization scripts | Working voice/control baseline; preserve it |
| Wall eye-hue candidate | [boop-wall-eye-hue-wip](https://github.com/ryankemble2006-web/boop/tree/boop-wall-eye-hue-wip) | source/, scripts/patch-wall-eye-hue.py, focused tests/workflow | Isolated v31 hue-only experiment layered on reviewed Wall v30; CI/signer green, physical Pixel acceptance pending; read SESSION_HANDOFF.md and BOOP_WALL_EYE_HUE_MEMORY.md |
| Wall Free Chat candidate | [boop-wall-free-chat-wip](https://github.com/ryankemble2006-web/boop/tree/boop-wall-free-chat-wip) | source/, scripts/patch-wall-chat-mode.py, focused tests | Android-led; three-second mode menu. Read its SESSION_HANDOFF.md for exact signed-build and test state; not a physical checkpoint |
| Isolated Native Chat relay candidate | [boop-relay-reviewed-v34](https://github.com/ryankemble2006-web/boop/tree/boop-relay-reviewed-v34) | source/, relay/cloudflare/, focused tests | Current phone-chat implementation of the approved relay plan; read its handoff/receipt for verification and setup limits; NOT merged into the concurrent Wall implementation |
| BOOP Launcher | [boop-launcher-alpha1](https://github.com/ryankemble2006-web/boop/tree/boop-launcher-alpha1) | launcher/ | Android-led development; read launcher/README.md |
| Wall-to-Launcher swipe draft | [boop-wall-launcher-handoff-wip](https://github.com/ryankemble2006-web/boop/tree/boop-wall-launcher-handoff-wip) | source/ and focused tests | Historical preserved draft; Wall v30 now owns the reviewed swipe, with physical acceptance still pending |
| Routine-authoring research | [boop-routine-authoring-v1](https://github.com/ryankemble2006-web/boop/tree/boop-routine-authoring-v1) | docs/superpowers/ and tests/ | Capability evidence/design; do not infer full authoring implementation |
| Older Shield Home lineage | [boop-shield-home-implementation](https://github.com/ryankemble2006-web/boop/tree/boop-shield-home-implementation) | shield-overlay/ | Historical/reference; new Shield work uses puppetry branch |
| Cross-project context | main | AGENTS.md, BOOP_CONTEXT.md, this map | Context hub; app files here can be historical |

## Native Chat relay ownership (2026-09-07)

The relay plan was executed from `boop-wall-free-chat-wip@7aa871f`. Another
session independently implemented the same plan on that branch while this task
worked. Its commits (`bbd50a6`, then `1201678` when inspected) were preserved.
This session's variant is isolated on **boop-relay-reviewed-v34**. These are
separate implementations, not interchangeable copies just because both say v34.
Use the APK, Worker source and build receipt from the SAME branch. Do not deploy
one Worker and install the other variant by version number or blindly merge them.
Read both current handoffs and perform an explicit reconciliation before promotion.

Native Chat is conversation-only: local NO_MATCH -> authenticated Worker -> OpenAI
Responses -> existing BOOP voice/eyes. OpenCode and browser Free Chat stay available.
Empty configuration is a setup candidate, NOT live chat. No provider key belongs
in Android. Configured bearer-token APKs require private build/distribution;
public-repository CI rejects token-bearing builds. Deployment and private account
configuration must be verified separately from a signed APK or mock tests.

## Wall eye-hue ownership (2026-09-07)

The hue experiment belongs to **boop-wall-eye-hue-wip**, forked from the reviewed
Wall v30 resurrection head. It must not be reconstructed by merging Free Chat,
Native Chat relay, launcher draft or other candidate lineages. It adds one
hue-only slider beside voice settings and recolours the existing `boop_eyes`
render path. Default cyan/blue deliberately installs no colour filter. The
protected physically green v29 checkpoint remains authoritative until the v31
candidate passes the recorded physical Pixel acceptance checklist.

## Start a new Work task

Attach this repository and choose the branch for the app you are developing.
Read AGENTS.md and that branch's SESSION_HANDOFF.md before making changes.
Fetch and compare the live branch heads: a downloaded folder is not a live sync.
Read the fetched main versions of this map, BOOP_CONTEXT.md and AGENTS.md.
Main owns shared decisions/contracts; the app branch's SESSION_HANDOFF.md owns
its current implementation and verification evidence. Branch-local shared files
are fallback copies when offline, and must not hide newer main decisions.
Root README files inherited from Alpha 1 do not override the current app map.

For the approved Wall chat-mode work, read the Free Chat candidate branch, not
only the preserved Wall branch. BOOP_CHAT_MODE_MEMORY.txt supplements its dated
BOOP_MEMORY.txt. For Wall eye-colour work, read `boop-wall-eye-hue-wip` plus its
`SESSION_HANDOFF.md` and `BOOP_WALL_EYE_HUE_MEMORY.md`; do not infer it from a
same-numbered APK or another Wall lineage. A Work request to "update memory"
remains documentation-only: fetch current handoffs, reconcile, commit/push
documentation, and verify the live branch. Do not change app code, permissions,
installs or signing without a new explicit request. An already-open Work task
must reread these GitHub records.

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
- Wall v30 includes eyes -> deliberate left swipe -> Launcher. Its owning Wall
  handoff records emulator verification; physical acceptance remains pending.
- Wall v31 eye-hue candidate is isolated on `boop-wall-eye-hue-wip`; do not
  promote it to the physical checkpoint without the recorded Pixel acceptance.
- Existing tap/hold/voice/HA behaviour must survive further gesture work.
- Launcher may open Wall by its package; permissions/return-strip behaviour
  require their own consent and device testing.
- Stable update signing remains in the existing GitHub workflows.

Read BOOP_CONTEXT.md for durable product decisions and each branch's handoff
for current evidence. Do not publish private third-party inspection artifacts.
