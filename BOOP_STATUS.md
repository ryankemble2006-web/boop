# BOOP Status

Updated 2026-09-10. `main` is the shared context/contract hub. `boop-unified` is the accepted Unified baseline; the currently selected overhaul work is on `boop-canonical-rebuild`.

## Shared continuity status

Ryan approved an exhaustive two-file continuity transfer for Codex/Boop:

- `BOOP_PERSONALITY.md` carries the durable collaboration style, shorthand and reference meanings.
- `BOOP_CONTEXT.md` carries the broad BOOP/project mental model and adjacent vocabulary.

This is documentation-only. No application source, package, signer, permission, build or deployment state is changed by the continuity transfer.

The corrected `canary` meaning is recorded as a deliberately narrow/sacrificial proof used to expose a risky assumption before applying the change to the real target. The prior skin-only/unknown note is superseded.

## Reconciled live app state

A concurrent Codex session advanced the owning app docs while the first continuity commit was being published. Shared context was reconciled to the fresher state.

Current observed `boop-unified` documentation HEAD:

`771b68a00ac95b40ed6e17cffac60af77528a934`

Physically accepted Natural Voice baseline:

- versionCode `91`;
- versionName `1.2.91-unified-static-track-state-fix`;
- app source/build head `11650313221ae5bf997dbb93b6a905bfdc7da1ed`;
- canonical Unified workflow `34433115316`: SUCCESS;
- artifact ID `10135283428`;
- protected checkpoint `checkpoint-boop-unified-v91-natural-voices-accepted` -> `11650313221ae5bf997dbb93b6a905bfdc7da1ed`.

Ryan physically confirmed Natural Voices are installed/selectable, demos speak and normal BOOP speech used the selected Natural Voice. That is voice-specific acceptance, not blanket physical acceptance of unrelated behaviour.

Historical rollback remains preserved:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

## Active selected canonical rebuild

Owning branch:

`boop-canonical-rebuild`

Observed candidate HEAD during reconciliation:

`a3eb768641e339ed59a6e2e86cb74f64bccf5979`

Base:

`boop-unified@99474d141e7affad17cdbe854e94dd3986076980`

Candidate version 92. Selected scope is items 1, 3, 6, 7, 8 and 10: shared speech/room/media state; dynamic configured-room and generic exposed HA discovery; one media corner/Home Now Playing owner; Deezer/local session transport; HA Back escape; manual/automatic profiles.

Eyes/blink are handled separately for later transplant. Turbo redesign is outside this branch. The candidate was not merged or physically accepted at this snapshot. Re-fetch its live handoff/status/memory before continuing.

## Acceptance rule

CI/build/signing/integrity evidence and Ryan's physical/visual/acoustic acceptance are separate states. Ryan remains the real-device visual and acoustic authority.
