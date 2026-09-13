# BOOP Lyrics Lab v158: refresh fix verified locally, Shield on hold

Updated 2026-09-13. Owning branch: `boop-lyrics-lab-side-by-side-v157`.
Package: `com.boop.lyricslab`; fixed version `158 / 0.1.158-lyrics-session-sync`.
GitHub remains the source/build authority. No merge into Unified or main.

## Latest user instructions and acceptance

Ryan initially enabled notification access on Shield and confirmed lyrics worked.
He approved the emulator design, then tested the lab player controls: all work,
but lyrics do not reload until exiting and refreshing. Initial lyrics/controls
and the existing design remain accepted; automatic refresh was NOT accepted.

Latest instruction: continue with locals and stay off the busy Shield. This is
an active deployment/testing hold. Do not query, install, issue media commands to,
or otherwise touch the Shield until Ryan releases it. Neither phone is a target.
Do not confuse this with permission to replace the installed Unified package.

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
download. The dedicated emulator's installed base APK matches that hash exactly.

## Verification completed in the local-only continuation

The actual old v157 lab was exercised on the dedicated Android TV emulator using
a real Android MediaSession and the production activity/controls/renderer. It
reproduced the refresh failure after Previous: the session continued, but the lab
lost the recording and showed its empty prompt. No activity restart occurred.
Its installed hash matched the original `9a52f1c6` signed artifact exactly.

The exact v158 APK then passed **38 runtime assertions in each of two complete
runs**, including automatic next/previous lyrics, 11 transitional states, pause/
resume and frozen paused clock, remote ten-second seek in both directions with
matching lyric cues, progress-bar navigation, missing/recovered metadata, replaced
session, rapid changes, same visible activity and leave/reopen synchronization.

These were not the old synthetic renderer preview: the real installed lab and
Android Binder callbacks were exercised. The provider session and cached words
were deliberately synthetic to isolate the refresh bug. This is local Android
runtime evidence, NOT post-fix real-Deezer/Shield/audio/catalogue acceptance.

GitHub additionally passed 73 session assertions, 19 identity assertions, 61
timed-data/ownership checks, 35 transport checks, two incomplete-timing checks,
seven real-entry control-flow checks, packaging and permanent-signer validation.
The saved red regression on `5ad0746a` failed before the fix. All app presentation,
artwork and shared renderer files are unchanged from the approved v157 lab.

## Current local state and cleanup

Dedicated AVD: `BOOP_Lyrics_157_8186447`. Lab v158 remains installed there.
Lab notification access was enabled through normal Android Settings on this
emulator only, without importing or changing physical-device permissions.
The temporary emulator-only synthetic player/instrumentation APK was removed
at the end after checking its exact package, test version, AVD and hash. Do not
mistake it for a real Deezer install. Other emulators/worktrees remained untouched.
Private artifact caches/logs remain on the laptop, outside the public repository.

## Next safe step

Do not rebuild or redesign this tested fix merely to resume. When Ryan releases
the Shield, install ONLY the verified `com.boop.lyricslab` v158 artifact and
confirm automatic new lyrics while staying on the real Deezer lyrics screen.
Missing-lyrics/offline behavior and longer real-device sessions remain separate
acceptance items; parser tests do not certify the entire remote catalogue.

Only after the real-device refresh check and Ryan's merge approval should the
native-lyrics feature be integrated into the then-current Unified successor.
The lab-specific observer is not a replacement for Unified's own session manager.
Keep unrelated eye-colour/speed work and existing Unified installations intact.

Detailed receipts, setup exclusions and repeatable test instructions:
`docs/handoffs/2026-09-13-lyrics-local-refresh-tests.md` and
`tests/runtime/lyrics-session/README.md`. Earlier Shield investigation is in
`docs/handoffs/2026-09-13-lyrics-track-refresh.md`. Historical product/checkpoint
memory remains under `docs/history/lyrics-lab-pre-user-confirmation/`.
