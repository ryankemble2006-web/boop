# Shared-context hub handoff

Updated 2026-09-10. `main` is the cross-project context hub, not the canonical BOOP application source.

## Exhaustive continuity transfer experiment — 2026-09-10

Ryan approved a documentation-only experiment to make a fresh Codex/Boop recover the accumulated working relationship and project mental model without reconstructing weeks of conversation manually.

The transfer is deliberately split across two main-owned files:

- `BOOP_PERSONALITY.md` = **how to work with Ryan as Boop**: conversational voice, shorthand, humour/pace, evidence habits, read-only/locked/go/cook/poke/update-memory semantics, canary meaning, frustration handling, visual-authority behaviour and cross-session etiquette.
- `BOOP_CONTEXT.md` = **what Boop understands**: product philosophy, Unified/device topology, current Natural Voice gate, Home Assistant/local-first model, protected eyes/hands, privacy/accessibility, product-family concepts, media/launcher/Turbo boundaries, adjacent Kodi/Forki vocabulary, rollback philosophy and continuity precedence.

The old personality note saying `canary` was an unrecovered skin-only joke was corrected. Ryan explicitly confirmed on 2026-09-10 that the useful shared meaning is the broader narrow/sacrificial proof before risking the real target.

Public-repository privacy remains in force: the expanded continuity captures durable working context, not credentials, account data, private addresses, personal secrets, raw chat history or private media.

This change is documentation only. It does not alter app code, permissions, signing, builds, deployment or physical acceptance.

## Current engineering pointer at handoff time

Normal app development still belongs on `boop-unified`.

Observed live `boop-unified` branch HEAD during this update:

`99474d141e7affad17cdbe854e94dd3986076980`

Latest signed Unified candidate recorded by the owning branch:

- v91 / versionCode 91;
- app source/build head `11650313221ae5bf997dbb93b6a905bfdc7da1ed`;
- canonical Unified workflow `34433115316`: SUCCESS;
- v91 CI/build verified, **not physically accepted**.

Current protected usable physical rollback remains:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Natural Voice physical proof remains the next narrow gate before the approved canonical rebuild.

## Startup rule for future sessions

For BOOP work:

1. read `AGENTS.md`, `BOOP_START_HERE.md`, `BOOP_CONTEXT.md`, `BOOP_RULES.md` and `BOOP_PERSONALITY.md`;
2. follow the startup map to the owning branch/worktree;
3. fetch/check live `main` and the owning branch;
4. read that branch's handoff/status/memory files;
5. preserve dirty/concurrent work and accepted checkpoints;
6. do not ask Ryan to reconstruct context that those files already contain.

Availability of the files is not proof another session has read them. If continuity appears wrong, re-read the live files before guessing.
