# Shared-context hub handoff

Updated 2026-09-10. `main` is the cross-project context hub, not the canonical BOOP application source.

## Exhaustive continuity transfer experiment — 2026-09-10

Ryan approved a documentation-only experiment to make a fresh Codex/Boop recover the accumulated working relationship and project mental model without reconstructing weeks of conversation manually.

The transfer is deliberately split across two main-owned files:

- `BOOP_PERSONALITY.md` = **how to work with Ryan as Boop**: conversational voice, shorthand, humour/pace, evidence habits, read-only/locked/go/cook/poke/update-memory semantics, canary meaning, frustration handling, visual-authority behaviour and cross-session etiquette.
- `BOOP_CONTEXT.md` = **what Boop understands**: product philosophy, Unified/device topology, Natural Voice history/current acceptance, Home Assistant/local-first model, protected eyes/hands, privacy/accessibility, product-family concepts, media/launcher/Turbo boundaries, adjacent Kodi/Forki vocabulary, rollback philosophy and continuity precedence.

The old personality note saying `canary` was an unrecovered skin-only joke was corrected. Ryan explicitly confirmed on 2026-09-10 that the useful shared meaning is the broader narrow/sacrificial proof before risking the real target.

Public-repository privacy remains in force: the expanded continuity captures durable working context, not credentials, account data, private addresses, personal secrets, raw chat history or private media.

This change is documentation only. It does not alter app code, permissions, signing, builds, deployment or physical acceptance.

## Concurrent reconciliation during publication

While the first continuity commit was being published, another Codex session advanced `boop-unified` from `99474d141e7affad17cdbe854e94dd3986076980` to `771b68a00ac95b40ed6e17cffac60af77528a934` with `docs: accept v91 voices and point to scoped rebuild`.

The shared context was therefore reread/reconciled before completion rather than leaving the first snapshot stale.

Fresh accepted baseline from the owning branch:

- v91 app source `11650313221ae5bf997dbb93b6a905bfdc7da1ed`;
- Ryan physically confirmed Natural Voices installed/selectable, demos speaking and normal routed Natural Voice reply;
- protected checkpoint `checkpoint-boop-unified-v91-natural-voices-accepted` -> exact v91 source;
- v88 checkpoint remains preserved as historical usable Android-TTS rollback.

Active selected overhaul now belongs to:

- branch `boop-canonical-rebuild`;
- base `boop-unified@99474d141e7affad17cdbe854e94dd3986076980`;
- candidate HEAD observed during reconciliation `a3eb768641e339ed59a6e2e86cb74f64bccf5979`;
- candidate version 92;
- selected scope includes shared room/speech/media state, dynamic exposed HA discovery, one Now Playing owner, Deezer/local transport, HA Back escape and device profiles;
- eyes/blink are being handled separately for later transplant and Turbo redesign is outside this selected branch;
- candidate was not yet merged/physically accepted at this snapshot.

Re-fetch that branch before continuing because it is actively moving.

## Startup rule for future sessions

For BOOP work:

1. read `AGENTS.md`, `BOOP_START_HERE.md`, `BOOP_CONTEXT.md`, `BOOP_RULES.md` and `BOOP_PERSONALITY.md`;
2. follow the startup map to the owning branch/worktree;
3. fetch/check live `main` and the owning branch;
4. read that branch's handoff/status/memory files;
5. preserve dirty/concurrent work and accepted checkpoints;
6. do not ask Ryan to reconstruct context that those files already contain.

Availability of the files is not proof another session has read them. If continuity appears wrong, re-read the live files before guessing.
