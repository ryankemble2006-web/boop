# BOOP Lyrics Lab v158: installed on Shield, final refresh check interrupted

Updated 2026-09-13. Owning branch: `boop-lyrics-lab-side-by-side-v157`.
Package: `com.boop.lyricslab`; version `158 / 0.1.158-lyrics-session-sync`.
GitHub remains the source/build authority. No merge into Unified or main.

## Latest user instruction and current physical checkpoint

Ryan said "do it" after the proposed real-Deezer/Shield refresh check. The previous
busy-Shield hold is released for this specific lab test, not for Unified or phones.
The tested v158 APK was installed ONLY as `com.boop.lyricslab` using `adb install -r`.
Installation succeeded; physical package/version and installed base APK hash were
verified against the exact tested candidate. Existing notification access was
already enabled by Ryan. No grant or other permission change was made.

The lab launched successfully on Shield and reported a timed document with 50 lines.
Then the laptop's Desktop Commander connection stopped answering. A command for
initial capture, one Next key and a second capture timed out with UNKNOWN outcome.
Subsequent session-list and ping requests timed out too, despite the device registry
still listing Yoga online. This is not proof of a moved file or changed permission.

Do NOT claim the real-player automatic-refresh gate passed. On reconnection, inspect
owned sessions and existing private captures before sending another playback key.
Do not reinstall: v158 is already installed and hash-verified. No app-code change,
rebuild, signing change, connection upgrade or merge was performed in this stage.
Detailed receipt: `docs/handoffs/2026-09-13-lyrics-shield-v158-install.md`.

## User acceptance and remaining gate

Initial real lyrics and all lab player controls were user-confirmed. Ryan approved
the emulator design. He then reported that lyrics did not reload until exit/reopen;
automatic refresh was NOT accepted. Preserve the approved presentation and controls.
The corrected refresh path passed local tests below, but post-fix real Deezer/Shield
track-change behavior still needs confirmation. Neither phone is a target.

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
independently confirmed to match that hash, in their respective test stages.

## Completed local verification, not physical-refresh acceptance

The exact signed v157 reproduced loss of lyrics after Previous on the dedicated TV
emulator with a real Android MediaSession, production activity and controls.
The exact signed v158 passed **38 runtime assertions in each of two complete runs**:
next/previous lyrics without reopening, 11 transitional states, pause/resume/frozen
clock, remote ten-second seek and matching cues, progress navigation, missing/
recovered metadata, replacement session, rapid changes and leaving/reopening.
Provider session and cached words were synthetic; Android callbacks and installed
app were real. Do not relabel this as a real-Deezer hardware/audio/catalogue pass.

GitHub additionally passed 73 session, 19 identity, 61 timed-data/ownership,
35 transport, two incomplete-timing and seven entry assertions, plus packaging
and permanent-signer validation. Saved red on `5ad0746a` precedes the fix.
The application renderer/artwork are unchanged from the approved v157 lab.
Review so far was in-session, not an independent reviewer.

## Protected state and cleanup

The dedicated AVD is `BOOP_Lyrics_157_8186447`; v158 remains installed there. Its
notification access was enabled through Android Settings in the local test stage.
The temporary emulator-only synthetic player/instrumentation APK was removed after
identity/hash checks. It must never be installed on physical devices or over a real
Deezer app. Do not assume real Deezer is installed on that AVD.

The Shield's separate Unified baseline in this stage was v159 shared-eye-colour;
it was neither edited nor installed over. No final post-timeout hash was obtained.
Do not downgrade Unified or disturb concurrent work. Private runtime captures,
raw diagnostics and APK caches remain outside the public repository.

## Next safe step and merge boundary

Restore responsiveness of the existing pinned Desktop Commander connection, inspect
the uncertain test's outcome, then finish this lab's real-player refresh checks.
Keep its working design. Missing-lyrics/offline and longer real-device sessions
remain separate items; parser tests do not certify the whole remote catalogue.
Only after physical validation AND Ryan's merge approval should the feature be
integrated into the then-current Unified successor. The lab-specific observer is
not a replacement for Unified's own session manager. No automatic merge is approved.

Earlier receipts and repeatable tests: `docs/handoffs/2026-09-13-lyrics-local-refresh-tests.md`,
`tests/runtime/lyrics-session/README.md`, `docs/handoffs/2026-09-13-lyrics-track-refresh.md`.
Historical product/checkpoint memory remains under
`docs/history/lyrics-lab-pre-user-confirmation/`.
