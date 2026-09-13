# BOOP Lyrics Lab v158: Skip refresh user-confirmed on Shield

Updated 2026-09-13. Owning branch: `boop-lyrics-lab-side-by-side-v157`.
Package: `com.boop.lyricslab`; version `158 / 0.1.158-lyrics-session-sync`.
GitHub remains the source/build authority. No merge into Unified or main.

## Latest user acceptance

Ryan clarified: "no i meant the adb is fine, ..... yes they do reload when skip is pressed inside the lab.."

Record the specific real-Deezer/Shield result as **USER-CONFIRMED PASS: pressing
Skip inside Lyrics Lab reloads the lyrics without leaving the lab**. This closes
the reported manual-Skip refresh regression on the installed v158. Do not ask Ryan
to repeat this already-confirmed test or keep it marked physically unconfirmed.

Ryan also says ADB is fine. The earlier Desktop Commander timeout is historical,
not a current user-reported ADB fault. Do not require reconnection, reinstalling,
upgrading the pinned connection or troubleshooting before accepting his result.
No fresh tool ping was performed when recording this clarification.

The user's result is not an independently observed automated pass. It does not
establish natural end-of-track advance, both skip directions, missing-lyrics/
offline behavior, longer sessions or every hardware synchronization case. Preserve
those distinctions without reopening the successful manual-Skip check. No new
app code, build, installation, permission, signer or device change accompanies
this documentation update. The success report is not merge authorization.

## Physical deployment and interrupted automated attempt

Ryan's prior "do it" released the busy-Shield hold for this specific lab test,
not for Unified or phones. The tested v158 APK was installed ONLY as
`com.boop.lyricslab` using `adb install -r`. Installation succeeded; physical
package/version and installed base APK hash matched the exact tested candidate.
Existing notification access was already enabled by Ryan. No new grant was made.

The lab launched on Shield and logged a real timed document with 50 lines. A later
command for initial capture, one Next key and a second capture timed out without
process ID/output; subsequent session-list and ping requests timed out too. Its
execution outcome remains UNKNOWN. Ryan's subsequent successful Skip test above
supersedes the pending acceptance gate, but does not retroactively prove that the
automated command completed. Do not blindly replay it. Earlier install and timeout
evidence remains in `docs/handoffs/2026-09-13-lyrics-shield-v158-install.md`.
Its reconnection/pending-refresh instructions are historical, superseded here.

## Approved presentation and regression history

Initial real lyrics and all lab player controls were user-confirmed. Ryan approved
the emulator design. His subsequent report that lyrics failed to reload until
exit/reopen was genuine; do not erase it using the original general success report.
The v158 manual-Skip fix is now user-confirmed on Shield. Preserve the approved
presentation and controls. Neither phone is a target.

## Exact fix and signed artifact

App source: `5970f59aa9173fd8171d2d370b856349d0cf4f17`.
The lab had detached its only per-session callback when a still-active player
entered a temporary non-displayable state. The fix retains observation through
NONE/STOPPED/SKIPPING and similar transitions while still clearing stale content.
It detaches on real removal, lost access or lab pause. No polling, page-reload
macro, renderer redesign, provider change, new app permission or signing change.

GitHub signed build `34766477538`: SUCCESS.
Artifact: `BOOP-Lyrics-Lab`, ID `10320107611`.
Exact APK SHA256:
`c95bb9cc343ec9752fb2be610d0c61c0e4d01eb4faa3c62b8c6b28713db00c09`.
Permanent signer SHA256:
`f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Source receipt, actual package/version, signature and hash were checked after
download. Both the dedicated emulator and the Shield installed base APK were
independently confirmed to match that hash in their respective deployment stages.
These are preserved receipts, not new device checks in this confirmation update.

## Completed local verification

The exact signed v157 reproduced loss of lyrics after Previous on the dedicated TV
emulator with a real Android MediaSession, production activity and controls.
The exact signed v158 passed **38 runtime assertions in each of two complete runs**:
next/previous lyrics without reopening, 11 transitional states, pause/resume/frozen
clock, remote ten-second seek and matching cues, progress navigation, missing/
recovered metadata, replacement session, rapid changes and leaving/reopening.
Provider session and cached words were synthetic; Android callbacks and installed
app were real. Keep that evidence separate from Ryan's real-Deezer Skip test.

GitHub additionally passed 73 session, 19 identity, 61 timed-data/ownership,
35 transport, two incomplete-timing and seven entry assertions, plus packaging
and permanent-signer validation. Saved red on `5ad0746a` precedes the fix.
The application renderer/artwork are unchanged from the approved v157 lab.
Review so far was in-session, not an independent reviewer.

## Protected state and cleanup

The dedicated AVD is `BOOP_Lyrics_157_8186447`; v158 was left installed there.
Its notification access was enabled through Android Settings in the local test
stage. The temporary emulator-only synthetic player/instrumentation APK was
removed after identity/hash checks. It must never be installed on physical devices
or over real Deezer. Do not assume real Deezer is installed on that AVD.

The Shield's separate Unified baseline at lab deployment was v159 shared-eye-colour;
it was neither edited nor installed over. No final post-timeout hash was obtained.
Do not downgrade Unified or disturb concurrent work. Private runtime captures,
raw diagnostics and APK caches remain outside the public repository.

## Merge boundary

Keep the lab separate and the design unchanged. Await Ryan's explicit merge
approval before integrating into the then-current verified Unified successor.
The lab-specific observer is not a replacement for Unified's own session manager.
Do not replace newer Unified with this feature's original v156 base. Missing-lyrics,
offline, natural completion and longer real-device sessions remain separate checks,
not reasons to deny or repeat the confirmed manual-Skip result.

Earlier receipts and repeatable tests: `docs/handoffs/2026-09-13-lyrics-local-refresh-tests.md`,
`tests/runtime/lyrics-session/README.md`, `docs/handoffs/2026-09-13-lyrics-track-refresh.md`.
Historical product/checkpoint memory remains under
`docs/history/lyrics-lab-pre-user-confirmation/`.
