# BOOP combined v146 integration

Updated 2026-09-12. Owner: `boop-unified-v146-integration`.
User explicitly confirmed all source work has ceased and authorized combining
phone iris-colour/menu/crash fixes, Lyrics preflight and Startup Manager defaults.
The same permanent-signed Unified APK is to be installed on Pixel 10 Pro XL and
Shield after checks. This supersedes inherited isolated-branch no-merge limits.
No permission changes, preset Apply/Undo actions, data clear or signer change.

## Frozen inputs

- `boop-v144-devmenu-hue-experimental` at `2c3c69cd8bc0dd9d251cce281dad2ec28370c38d`.
- `boop-shield-defaults` at `503cdb63d64716c9c1a568aadca97ba1d24680cd`.
- `boop-v142-lyrics-fastfail` at `0d2c39863ed63d790ec5a2bb4e45d767867229ff`.

## Verification plan

Preserve phone production code and hue shader, add the proven Lyrics delta,
retain exact Startup Manager/defaults recovery and visible Overview controls.
Run all existing functional suites, phone constructor/menu contracts and Android
compilation. Sign through the existing GitHub workflow. Verify source/package/
signature/hash; preserve both current installed APKs and settings before update.
Install the same artifact on both targets and inspect real launch/feature paths.
Record any screen-lock or acceptance limits without bypassing device security.

## Current state

Three-way source integration complete. Both devices currently run different v145 APKs.
No combined build, deployment or physical acceptance claimed yet.

## Combined local verification

Full materialization and Android compile passed. Unified unit suite: 295 tests
across 85 suites, zero failures/errors/skips. All 18 Lyrics tests included.
12 phone/menu/constructor/shared-library contracts pass; 15 Startup Manager suites
pass; 27 defaults coordinator scenarios, eight safety scenarios and profile/journal
checks pass. Android 11 linkage, canonical media and listener seeding checks pass.
Independent feature files match the frozen inputs; only their shared local bridge
was combined additively. No renderer, artwork or permission redesign.
Both v145 installed APKs were privately pulled and hash-matched to device before
any update; app preference/restore/journal and OS package/settings hashes saved
privately for comparison. No installed version has changed yet.
Only generated side effects in this owned worktree were restored after build.

Next: permanent signed CI, verify artifact, install on both authorized devices,
then physical launch/feature checks and final handoff. No acceptance claim yet.

## CI provenance correction

First run 34682994462 stopped before compilation because the condensed new
SOURCE_HEADS.md omitted the original three Unified provenance hashes. The
original provenance section is restored alongside the new integration inputs.
The existing assertion now passes. No app-code or test weakening was involved.
