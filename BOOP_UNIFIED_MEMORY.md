
## Signed candidate and final verification, 2026-09-12

- Source: `fc0b552fe058cfaa0d29668cc1400d7fc8675ed5`.
- Branch: `boop-v142-lyrics-fastfail`; code published and live HEAD verified.
- Version stays `142 / 1.2.142-animation-experimental`, package `com.boop.alpha1`.
- Signed GitHub run `34681420296`: completed successfully.
- Artifact `BOOP-Unified`, ID `10293739817`.
- APK SHA256: `720095d91a5481c49d7fe26aae9576217b5c4623024f4887724aee5d81092abb`.
- Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Download independently checked: built commit, package, version, APK hash,
  APK ZIP integrity and v2 signature all passed. No replacement signer.
- Full local Unified unit task: 295 tests across 85 suites, no failures/errors/skips.
- Focused lyrics subset: 18 tests. Startup Manager: all 15 suites passed.
- Seven canonical/shared media checks and listener-seeding check passed.
- Real-device API-only probe: positive 357 ms cold, negative 48 ms, positive 100 ms
  warm. Screen unchanged. These are measured individual checks, not timing guarantees.

Latest device inspection found v145 `1.2.145-boop-defaults-visible` installed by
another task. The earlier v143 observations are superseded. Preserve v145 and its
visible BOOP-defaults controls. This v142-based signed candidate is NOT installed,
NOT a superset of v145 and NOT full-button physically accepted. Before deployment,
obtain approval for a deliberate lyrics-only forward integration into that newer
lineage; do not downgrade, merge whole app branches, or discard its changes.

All artwork, animation source and permission declarations are unchanged against
the selected v142 base. No device install, grants, setting change or playback
change was performed here. Private probes, third-party inspection, raw evidence,
APK and generated build/cache files remain ignored and unshared. No app code is
left uncommitted. The temporary owned device probe dex files were removed.

## 2026-09-12: isolated v142 lyrics preflight

This task's owner is `boop-v142-lyrics-fastfail`, based on `5c383c68`.
Ryan approved only the no-lyrics behavior carried into the v142 experiment.
Older startup-only exclusions below describe their original task, not this one.

The authoritative check is now proven without personal credentials: anonymous
Deezer guest JWT plus Pipe synchronizedLines/synchronizedWordByWordLines by exact
MediaSession playable ID. Old `LYRICS_ID` and explicit-content flags are not usable.
Do not repeat the public web-flag hunt or retrieve account tokens. Do not repeat
provider probes: the normal published media metadata supplies the exact track ID.
The `/lyrics/<id>` URI experiment was not conclusive; use the established positive
notification/semantic-button path, not an alleged new route.

Real Java API-only probe on Shield passed both outcomes without moving its screen.
Newer v145 BOOP-defaults-visible APK is installed. Preserve it; this v142 candidate is not
installed or physically button-tested. Current checks and receipts belong in the
owning handoff/status; concurrent animation and defaults branches stay separate.

# BOOP Unified continuation memory

Updated 2026-09-12. This is the current scoped supplement. The complete preceding memory is preserved byte-for-byte at [docs/history/startup-v133/BOOP_UNIFIED_MEMORY.md](docs/history/startup-v133/BOOP_UNIFIED_MEMORY.md). Read it for protected checkpoints, wider app history and earlier decisions; current user instructions and the owning handoff take precedence over its dated progress entries.

## Current explicit scope

Ryan said: lyrics is WIP; leave it out; Startup Manager only; finish more quickly. Do not merge, debug or deploy lyrics as part of this delivery. Preserve that separate branch/worktree. The previous proposal to preserve lyrics inside v133 is superseded.

The corrected candidate is v134, retaining the Startup Manager-only repair from `42fe4be8ddfb196a34be73c713d434b235663415` and the already-established approved music artwork without editing or regenerating it. No additional feature work is authorized by this correction.

## Protected engineering facts

One Unified package remains `com.boop.alpha1`, using the permanent GitHub signer. Local-first controls, established profile routing, approved eyes/hands, audio behavior and rollback checkpoints remain protected. GitHub performs nonvisual functional/build/integrity checks; visual acceptance belongs to Ryan.

Startup Manager must preserve exact original enabled/background/boot state before a persistent change. Undo must repair a partial write even when successful-action flags were never recorded. Reject unknown or mismatched package state, preserve damaged receipts, bind commands to the verified Android user, cancel stale callbacks and retain the minimal recovery floor. The stock Google TV launcher is manageable, not blanket-protected by vendor prefix.

Java 17 compilation alone did not prove Android 11 runtime compatibility: v128 crashed on String.lines. The repaired parsers and synthetic Startup Manager suites were subsequently exercised on the actual Android 11 runtime without changing installed packages. CI success, signed artifact, agent device checks and Ryan's physical acceptance remain separate evidence levels.

## Current delivery boundary

The new no-lyrics source still requires its exact signed build, install and Package Control check. Last observed installed Shield version was v132. Re-read it and preserve the actual installed APK before deployment. Ryan authorized one music-track-change completion signal only after the requested delivery is genuinely installed. Do not send it for a source-only update.

The Yoga bridge currently rejects filesystem/terminal calls. Publishing via GitHub does not synchronize the local checkout automatically. On reconnection, fetch and reconcile without overwriting dirty or concurrent work. See [SESSION_HANDOFF.md](SESSION_HANDOFF.md).
