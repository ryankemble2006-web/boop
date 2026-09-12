# BOOP defaults feature handoff

Updated 2026-09-12. Owning branch/worktree: `boop-shield-defaults` /
`.worktrees/boop-shield-defaults`. Base: accepted v135 source with acceptance docs
at `582bd0d404a3d4ca61abd718551f7af5ef20aabe`. Other app, lyrics, integration and
animation worktrees are not part of this change. Do not merge or deploy by implication.

## Candidate

`143 / 1.2.143-boop-shield-defaults`, package `com.boop.alpha1`, permanent signer
and permissions unchanged. This is the isolated defaults candidate, not a superset
of unrelated later-numbered lyrics/animation branches. The version was selected
after sibling reservations at 136, 137 and 142 were observed read-only.

Implemented Overview actions: **Use BOOP defaults** and **Undo BOOP defaults**.
The review screen shows exact matching packages, consequences and whole-row
selection so individual entries can be excluded. Opening it does not apply changes.
The existing Overview content scrolls instead of being squeezed by the new actions.
No personal name appears in the feature's UI or profile data.

## Preset and recovery

Frozen profile `boop-shield-defaults-v1`: 14 exact package IDs, nine disables,
nine boot-cleanup selections and six paired background restrictions. No vendor-
prefix rule. Three historical disables and Kodi's old single restriction stay
excluded. Missing entries, foreground apps, protected recovery components and
active input/accessibility providers are skipped with reasons. The NVIDIA-TV,
setup/user/recovery checks are repeated during execution. Stock launcher is the
last package and requires an enabled BOOP HOME route. ADB/grants are not enabled
or changed by the preset. The ordinary manual package console remains unchanged.

Apply uses the established verified individual controller. Before each mutation,
a separate bounded/versioned journal stores the receiving device's exact pre-
preset state and individual Restore record. The journal is atomic and app-private
under getNoBackupFilesDir; no source-device Restore data is shipped or backed up
as a portable preset. Group Undo changes only dimensions this preset actually
changed and reinstates the exact previous individual record. Existing global
cleanup switches and unrelated selections are preserved. Partial work/process
death remains recoverable. Repeated Apply cannot replace the initial baseline or
broaden the selection. Later-state/ledger conflicts are kept rather than silently
overwritten; the review reports them. Explicit Keep current settings only drops
grouped ownership, leaving packages and individual Restore records unchanged.

## Verification

Fresh local final candidate: real Android Java compilation and full Unified unit
task passed, 277 tests / zero failures, errors or skips. Existing 15 Startup
Manager suites and Android 11 linkage passed. New tests: 27 coordinator scenarios,
eight recovery/safety scenarios and profile/journal structural checks. Canonical
shared-state/media contracts passed. Observed RED-to-GREEN includes missing new
behavior, partial recovery, mid-package external drift and forged review flags.
Code review was performed locally with adversarial tests; no independent reviewer
or physical preset acceptance is claimed. No GitHub visual tests were introduced.

## Signed candidate receipt, 2026-09-12

- Version: `143 / 1.2.143-boop-shield-defaults`, package `com.boop.alpha1`.
- Exact build source: `696a1b2249b2549c12c4832baf7bc2c30b913aab`.
- Successful permanent-signed workflow run: `34676381862`.
- Artifact: `BOOP-Unified`, ID `10291778955`.
- APK SHA256: `4c91bf3b54675afb11f3a0c2f574b1eb0da5b7747d2919016e3d3145cb77f2a1`.
- Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- GitHub-reported artifact archive digest: `sha256:677de8185388cab4b418318a32f29e3057c70a5dbcd6de9028dc8dc371489a9d`.

The signed download was independently checked against its included APK hash and
built-commit receipt. Package/version, permanent signer and ZIP integrity passed.
The APK contains the preset coordinator/review screen and both BOOP-branded
buttons; the unfinished Lyrics browser is absent. GitHub passed the existing
nonvisual gates, including 68 Shield and 217 Unified focused tests with zero
failures/errors/skips, plus the new defaults behavior suites.

**Not installed, not applied, not merged into ongoing work.** No live package,
permission, media or default-HOME changes were made in this feature task.
The preset's real-device layout, Apply/Undo round trip and firmware behavior
remain unverified; signed/tested does not imply physical acceptance. Obtain
separate deployment approval, inspect the shared device's current build first,
and preserve its rollback. Do not overwrite newer unrelated branch work merely
because this isolated candidate has versionCode143.


Spec: docs/superpowers/specs/2026-09-12-boop-shield-defaults.md.
Plan: docs/superpowers/plans/2026-09-12-boop-shield-defaults.md.
Historical accepted v135 source/build receipt remains preserved in the base Git
history and original integration branch. Baseline APK run: 34673227727,
artifact 10291770657. Do not confuse that accepted base with this uninstalled feature.
