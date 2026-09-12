# BOOP current status

Updated 2026-09-12. Owning branch: `boop-v125-animation-integration`.

## Current delivery

**Startup Manager only. Lyrics is work in progress and excluded by Ryan's explicit instruction.** Candidate version is `134 / 1.2.134-startup-manager-only`.

The Startup Manager repair/UI is retained from `42fe4be8ddfb196a34be73c713d434b235663415`. Existing approved music art remains unchanged. The lyrics branch is untouched. v133's proposed combined integration is superseded and must not be installed for this request.

## Evidence and pending work

- Prior repair checks: 15 focused suites; local Android compilation; 277 Unified unit tests with no failures/errors/skips; synthetic Android 11 dex-harness checks with no installed-package mutations.
- Current v134: source scope corrected; new signed build and installation not yet verified.
- Last observed Shield install: v132. Recheck live before installing and preserve its exact rollback APK.
- Yoga terminal/files are unavailable despite a successful ping. GitHub is accessible. Local checkout synchronization is not claimed.
- No completion track change has been sent for this corrected candidate.

Follow [SESSION_HANDOFF.md](SESSION_HANDOFF.md) for the next safe step. The previous detailed status is preserved unchanged at [docs/history/startup-v133/BOOP_STATUS.md](docs/history/startup-v133/BOOP_STATUS.md); its earlier v133 integration plan is historical.

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
