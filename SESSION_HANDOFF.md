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

Permanent-signed build and artifact verification are next. The installed shared
Shield was not changed, no defaults were applied, and no media cue was sent.
Deployment requires separate approval to avoid overwriting work in progress.

Spec: docs/superpowers/specs/2026-09-12-boop-shield-defaults.md.
Plan: docs/superpowers/plans/2026-09-12-boop-shield-defaults.md.
Historical accepted v135 source/build receipt remains preserved in the base Git
history and original integration branch. Baseline APK run: 34673227727,
artifact 10291770657. Do not confuse that accepted base with this uninstalled feature.
