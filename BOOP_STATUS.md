# BOOP Status

Updated 2026-09-10. `main` is the shared context/contract hub; normal BOOP application work is on `boop-unified` unless the startup map names an explicit exception.

## Shared continuity status

Ryan approved an exhaustive two-file continuity transfer for Codex/Boop:

- `BOOP_PERSONALITY.md` now carries the durable collaboration style, shorthand and reference meanings.
- `BOOP_CONTEXT.md` now carries the broad BOOP/project mental model and adjacent vocabulary.

This is documentation-only. No application source, package, signer, permission, build or deployment state is changed by the continuity transfer.

The corrected `canary` meaning is now recorded as a deliberately narrow/sacrificial proof used to expose a risky assumption before applying the change to the real target. The prior skin-only/unknown note is superseded.

## Current canonical app pointer

Observed live `boop-unified` branch HEAD during this update:

`99474d141e7affad17cdbe854e94dd3986076980`

Latest signed candidate recorded by `boop-unified`:

- versionCode `91`;
- versionName `1.2.91-unified-static-track-state-fix`;
- app source/build head `11650313221ae5bf997dbb93b6a905bfdc7da1ed`;
- canonical Unified workflow `34433115316`: SUCCESS;
- artifact ID `10135283428`;
- v91 remains **not physically accepted**.

## Current physical rollback

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Ryan physically accepted v88 as the usable Android-speech rollback. This does not prove Natural Voices work.

## Immediate app gate

Physical-test v91 Natural Voices. The required finish condition remains more than one speaker preview plus normal routed Natural Voice speech while ordinary Android TTS fallback remains safe.

Only after Natural Voices are physically accepted should the already-approved canonical rebuild be started from the exact voice-working Unified head.

## Explicit exception

Clean Nvidia Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a merge. Do not infer otherwise from Unified's canonical status.

## Acceptance rule

CI/build/signing/integrity evidence and Ryan's physical/visual/acoustic acceptance are separate states. Ryan remains the real-device visual and acoustic authority.
