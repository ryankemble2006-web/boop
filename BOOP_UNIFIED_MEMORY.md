## Verified v134 installation receipt, 2026-09-12

Startup Manager-only v134 was installed successfully and its installed APK bytes
were independently verified against the signed GitHub artifact.

- Build source: `56bbb7d5f2fdaf9ede5c46cc8f7c1d0f46bacca0`.
- Version: `134 / 1.2.134-startup-manager-only`, package `com.boop.alpha1`.
- Successful permanent-signed run: `34672558546`; artifact `10290724349`.
- APK SHA256: `3241628257ae1835271d49c728f6a91f13fb2cfd1d3b209052081585a5e4664d`.
- Permanent signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Installed v132 rollback preserved privately; SHA256
  `46b7c4b6f233c9608d625e4cfa2ecf733a0b88afca0e628e546c3f084cdead46`.

Fresh v134 checks: 15 Startup Manager Java suites and Android 11 linkage gate
passed; signed CI passed all gates, including 68 Shield and 217 Unified focused
functional tests with no failures/errors/skips. Package Control, its populated
list/filter/detail screen and high-impact confirmation were observed on Shield.
The current BOOP process had no AndroidRuntime fatal in the inspected output.
The original inventory crash did not recur in this check. Visual acceptance and
a complete disable/Restore round trip are not claimed by this installation task.

Lyrics is excluded. Existing artwork, signer and permissions were not changed.
Navigation/package selections changed during concurrent review, so this task
stopped sending navigation inputs and did not undo the observed package choices.
No active music session remained at the completion check, so no completion skip
was sent and playback was not started unasked.

A separate live review is preparing v135 for clipping at the existing large-text
setting. Preserve its dirty View/version/handoff and materializer output. This
receipt is published with a private temporary Git index so none of that concurrent
local work is staged, reset or overwritten. The prior offline notes below are
historical: the Yoga terminal and Shield ADB worked for this installation.

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

## v135 large-text UI candidate, 2026-09-12

v134 is installed and its Package Control screen was manually observed loading
148 packages without the old Android 11 crash. The installed APK hash matches
run 34672558546 / artifact 10290724349 exactly. Its on-screen font_scale=1.3
exposed clipping in two-line sidebar labels, package labels and the Background
state controls. v135 uses concise single-line sidebar labels, intrinsic package
label/card heights and action heights calculated from the actual scaled font
metrics. Android's text-size setting is unchanged.

Fresh v135 local materialization/Android Java compilation passed; 277 Unified
unit tests passed with zero failures/errors/skips, 15 Startup Manager behavior
suites passed, and Android 11 linkage checks passed. Signed v135 build/install
and manual large-text confirmation are next. Only Startup Manager UI is changed;
lyrics remains excluded and approved art/package-control behavior is retained.
No destructive package test or music-track signal was sent by this review.
