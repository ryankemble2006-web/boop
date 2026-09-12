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

# BOOP Startup Manager continuation

Updated 2026-09-12. Current scope: **Startup Manager only. Lyrics is WIP and must stay out.** This explicit Ryan correction supersedes the preceding v133 integration plan.

## Owning checkout and branch

- Repository: `ryankemble2006-web/boop`.
- Task branch/worktree: `boop-v125-animation-integration`.
- Primary checkout is not this app's current worktree. Follow root AGENTS.md and fetched main rules.
- Last verified remote before this correction: `7e0d17907f5b3e59ecf108cbcc58911a283ec6c9`.
- The complete previous handoff is preserved byte-for-byte at [docs/history/startup-v133/SESSION_HANDOFF.md](docs/history/startup-v133/SESSION_HANDOFF.md). Its dated release claims are historical, not the current scope.

## Candidate scope

Version `134 / 1.2.134-startup-manager-only`, package `com.boop.alpha1`, permanent signing workflow unchanged.

The app and tests use the Startup Manager repair from `42fe4be8ddfb196a34be73c713d434b235663415`, before the lyrics merge. The Lyrics button, browser/policy classes, bridge entry point, shared-screen additions and lyrics-specific tests are excluded. The `boop-now-playing-lyrics` branch and worktree are not changed.

Existing approved music artwork is retained exactly as already present in the v133 source and the observed installed v132 lineage. No new artwork is created or edited. The only new feature being delivered is Startup Manager.

The approved Startup Manager implementation includes the sidebar, responsive Overview cards, package controls/detail pane, Restore, Android 11-safe parsers, exact original-state recovery, verified package actions, migration, cancellation and minimal recovery protection.

## Verification level

Before this scope correction, the pre-lyrics repair passed local Android compilation, the full 277-test Unified unit gate and 15 focused Startup Manager suites. The synthetic dex harness also ran on the actual Shield Android 11 runtime without changing installed package state. These are recorded prior results, not a claim that v134 has been built or installed.

v134 source is assembled from those exact pre-lyrics Git objects plus its version change and the unchanged approved art. A new permanent-signed build and physical checks are still required.

v128 crashed on `String.lines()` and is not a working package-control checkpoint. v127 rollback was preserved privately. The last actual installed version observed before the connection slipped was v132; re-read installed version and preserve its exact rollback APK before installing anything.

## Connection and next step

Remote Desktop Commander currently answers a ping but rejects terminal/file calls with `Not connected`. No local files or Shield state have been changed during this scope correction. The correction is being published directly through the connected GitHub tools.

After the Yoga bridge reconnects: fetch this branch, preserve any dirty/concurrent work, fast-forward only if clean, run the existing build workflow for the exact current source, verify artifact hash/package/version/permanent signer, preserve the installed APK, then install. Do not install the superseded lyrics-containing v133 artifact. Recheck live branch and installed version immediately before deployment.

Ryan authorized installation and one music-track-change completion signal after the verified candidate is genuinely installed. Do not signal completion while installation or the Package Control crash check remains unfinished. No permission grants, root, signer changes, automatic debloat or arbitrary package disabling are authorized. Destructive package and stock-launcher disable/Restore acceptance remains user-owned.

## v134 installed; large-text correction in progress

The exact permanent-signed v134 APK from source
`56bbb7d5f2fdaf9ede5c46cc8f7c1d0f46bacca0`, run `34672558546`, artifact
`10290724349`, is installed on the Shield. Independent installed-file SHA256
matched `3241628257ae1835271d49c728f6a91f13fb2cfd1d3b209052081585a5e4664d`.
The installation completed concurrently; this review did not reinstall over it.
The actual Package Control screen loaded 148 packages and stayed in
ShieldStartupManagerActivity. This closes the original package-inventory crash
check, not destructive action or user acceptance.

Manual screenshot review found clipping at the user's existing font_scale=1.3:
sidebar two-word labels and two-line background action state. This window is
correcting those exact text containers for v135 without changing Android text
settings, package controls, approved art or excluded lyrics. No package changes
or completion media signal have been sent by this review. Other windows should
leave this view/version pair alone until the v135 check finishes.

Fresh v135 local verification passed: Android compilation, 277 unit tests,
15 Startup Manager suites and Android 11 linkage. Signed build is next.
