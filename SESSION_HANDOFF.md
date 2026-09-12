# BOOP Startup Manager continuation

## Ryan accepted Startup Manager v135, 2026-09-12

Ryan reported that the installed Startup Manager works perfectly after using it
to disable his unwanted packages, including the stock launcher, and prevent that
launcher's startup. This is user-reported physical acceptance of v135's Startup
Manager UI and the package controls he exercised. Preserve his selected setup.

Accepted source: `7408ab85c8c58b4478a3a64f18af834160efb346`; signed run
`34673227727`; artifact `10291770657`; APK SHA256
`b50823e9b902de049ee7e19d919947bcd2062d24bd953ad1616792bfa0485d8f`.
Use this exact v135 artifact as the accepted Startup Manager rollback reference.
The exact disabled-package inventory was not supplied. Do not infer a universal
Android/NVIDIA debloat recipe, measured performance gains, a completed Restore
round trip or reboot persistence from this report. Lyrics remains excluded.
This acceptance update is documentation only; the Shield is left untouched.

Updated 2026-09-12. Current user-accepted Startup Manager: **v135, lyrics excluded**.
Lyrics is WIP and excluded by Ryan's explicit instruction. Do not reintroduce it.

## Owning checkout

Repository `ryankemble2006-web/boop`, branch/worktree
`boop-v125-animation-integration`. Follow root AGENTS.md and fetched main rules.
The primary checkout is not automatically this app's latest source. Preserve the
separate lyrics/music-art worktrees and all dirty or concurrent work.

## Signed and installed receipt

- Version: `135 / 1.2.135-startup-manager-large-text`, package `com.boop.alpha1`.
- Build source: `7408ab85c8c58b4478a3a64f18af834160efb346`.
- Successful permanent-signed run: `34673227727`; artifact `10291770657`.
- APK SHA256: `b50823e9b902de049ee7e19d919947bcd2062d24bd953ad1616792bfa0485d8f`.
- Permanent signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- The installed APK's own SHA256 exactly matches that verified signed artifact.
- Actual installed v134 APK was preserved privately before v135. Its SHA256 is
  `3241628257ae1835271d49c728f6a91f13fb2cfd1d3b209052081585a5e4664d`.

The preceding v134 installation was completed by the concurrent installation
window. This review verified its exact APK and observed the 148-package screen,
then built and installed the narrowly scoped v135 large-text correction.
All four private Startup Manager preference files were byte-identical immediately
across the v135 update. Existing package choices, original-state receipts and
migration state were preserved. Later user changes are not attributed to this
installation. No grants, signer changes, arbitrary package disables or resets.

## Verification and physical scope

Fresh local v135 materialization/Android Java compilation passed. Full Unified
unit task: 277 tests, zero failures/errors/skips. Fifteen Startup Manager behavior
suites and the Android 11 linkage gate passed. Signed CI passed all gates including
68 Shield and 217 Unified focused tests; package/version/signature/ZIP checks passed.
The repaired synthetic Startup suites were also previously run as dex on the
actual Shield Android 11 runtime without mutating installed packages.

Manual v135 screenshot inspection confirmed the live Package Control screen with
148 packages, filter bar, detail pane, visible cyan focus and unclipped sidebar
and Background ON/OFF labels at the existing `font_scale=1.3`. The current BOOP
process had no AndroidRuntime fatal in the inspected log. The original inventory
crash did not recur. No full destructive disable/Restore, reboot or universal
navigation acceptance is claimed from this scoped review; those remain Ryan-owned.

## Delivered behavior and next safe step

The native UI has a fixed sidebar, responsive four-card Overview, package filters,
per-package actions, details and Restore. Original-state recovery, v1 migration,
cancellation and the minimal recovery floor are retained. v135 changes only text
container sizing/labels; Android text size, package-control logic and already
approved music artwork are unchanged. No new artwork was generated. Lyrics stays out.

Ryan has accepted the installed Startup Manager; its route remains Home settings > Startup Manager.
The last media check found no active track, so no completion skip was sent and no
playback was started unasked. A future cue must not be claimed as already sent.
Yoga and Shield access recovered, and this checkout was safely fast-forwarded to
include the concurrent v134 documentation receipt before this final update.

Historical detail remains in Git and in
[docs/history/startup-v133/SESSION_HANDOFF.md](docs/history/startup-v133/SESSION_HANDOFF.md).
Those dated v133/lyrics integration proposals are superseded. v128 was the failed
String.lines candidate and must not be described as a working package console.
