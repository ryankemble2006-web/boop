# Lyrics refresh regression: local-only verification receipt

Date: 2026-09-13. Owner: `boop-lyrics-lab-side-by-side-v157`.
Current user boundary: stay off the busy Shield and use locals. No phone access,
no Unified install, no merge. Initial lyrics/controls and the emulator design
remain user-approved; automatic refresh failed in the user's test.

## Sources and build receipts

Baseline app source: `9a52f1c66a6322584e99350056fb4b9bcb645a2b`.
Baseline signed run: `34763076696`; artifact `10319767160`.
Baseline APK SHA256:
`5edd429190f4b8ca4c37390b91851a2b30f1ce265a6a3c49be3512bef2ff2e91`.
The emulator's pre-update base APK hash matched the downloaded original artifact.

Fixed app source: `5970f59aa9173fd8171d2d370b856349d0cf4f17`.
Fixed signed run: `34766477538`; artifact `BOOP-Lyrics-Lab` / `10320107611`.
Package/version: `com.boop.lyricslab`, `158 / 0.1.158-lyrics-session-sync`.
Fixed APK SHA256:
`c95bb9cc343ec9752fb2be610d0c61c0e4d01eb4faa3c62b8c6b28713db00c09`.
Permanent signer SHA256:
`f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Signature, source receipt, package/version and APK hash independently checked after
download. `adb install -r` succeeded on the dedicated emulator; installed base APK
hash matched exactly, both after installation and after fixture cleanup.

Runtime fixture source: `9bde0f609bee52eede04d88e69150c9317c6eb66`.
Fixture build: `34767793652`, artifact `10321148278`, SUCCESS.
Fixture APK SHA256:
`33c271719ff82c3d88750f41359c1cb536be3ec406b20359e7a92472184a3446`.
It is a test-only synthetic player with a real Android MediaSession and an
instrumentation runner targeting the installed production Lyrics Lab. It seeds
invented documents into the existing memory cache using test-only reflection;
there are no production test hooks or replacement renderers. The helper declares
no INTERNET permission and refuses non-emulator hardware. Its test provider
identity is never to be installed on a physical device or over a real Deezer app.

## Root cause and existing test-first evidence

The old lab treated presentation eligibility as observation lifetime. A temporary
NONE/STOPPED/SKIPPING state removed its only callback although the session token
was still active. A change-of-active-sessions-list listener cannot substitute
for the missing metadata/playback callback. The v158 fix retains the observed
token through transitions and still suppresses non-displayable content.

Saved red: `34766399230`, source `5ad0746a`, expected next recording 456, got null.
Saved green: `34766477538`, source `5970f59a`: 73 session assertions, 19 identity,
61 timed-document/ownership, 35 transport, two incomplete-timing and seven entry
assertions pass, plus packaging/signing. The session suite uses controlled Android
boundaries; real Android behavior was additionally checked locally below.

## Local baseline and fixed runs

AVD: `BOOP_Lyrics_157_8186447`, Android TV API36. Only this AVD was operated.
Normal Android Settings enabled the lab's notification access on the emulator.
No physical-device consent, APK or settings were read or changed.

Baseline v157: initial document and cue displayed; Next and gap clearing passed.
Previous then reproduced loss of the selected recording and lyrics. The actual
lab showed its empty prompt despite the live synthetic player having completed
the transition. Instrumentation failed after four assertions with recording and
document empty. No Activity restart was used to mask the failure.

Fixed v158: all **38** assertions passed in **two complete runs**, with the exact
same fixture and no production UI changes. Both returned `checksPassed=38` and
`INSTRUMENTATION_CODE: -1`. Do not use the shell process's exit code alone: Android
instrumentation can return shell exit 0 even when assertions failed.

Coverage:
- Next and Previous replace the actual lab document/title without leaving.
- Old lyrics clear through the gap; callbacks survive 11 temporary states,
  including null, NONE, STOPPED, ERROR, all three skip states, seek states,
  BUFFERING and CONNECTING.
- Pause/resume controls receive actual callbacks; paused elapsed time stays fixed.
- D-pad ten-second forward/back controls update the session and matching lyric cue.
- Progress-bar Right seeks ten seconds; Down returns focus to Play/Pause.
- Missing metadata clears old content; later metadata recovers automatically.
- Replacement session and rapid successive changes show the latest recording.
- The same visible Activity remains foreground throughout those transitions.
- Leaving and reopening synchronizes the latest recording and cue.

Baseline private result hash:
`a305c5a5d03b24bf3699615f609402a417b27153cddabb860cbbc1b2fb4d55e0`.
Each deterministic complete green result log has SHA256:
`6878d21e7f4b4ae948db2466fae8d43fc5ca25506eda0962dd3d64b52c897826`.
The two runs were separately executed; identical assertion output is expected.
Raw files stay private in the laptop's task artifact cache, not in this repository.

## Excluded setup failures, not product success or regression proof

An initial instrumentation launch waited because the earlier Android consent
Settings activity still topped the lab task. Only the owned local lab process
was stopped, and its activity task was recreated without clearing app data.
The baseline run above then executed and produced the genuine refresh failure.

The first v158 attempt passed all transition/pause assertions, then failed a
programmatic focus request because the tap-based consent setup had left Android
in touch mode. A normal D-pad key entered remote mode. No app code or expected
assertion was changed; the complete identical fixture then passed twice.

The dedicated emulator disappeared during setup. Local process/ADB inspection
confirmed no emulators remained; only this existing task-owned AVD was restarted.
No other worktree, emulator, physical device or connection service was altered.

## Scope, cleanup and remaining gates

The app diff from the approved lab changes only the observer lifetime and version;
shared renderer/artwork/provider/permission/signing files remain unchanged.
Review was an in-session source/diff review, not an independent reviewer.
The test helper was uninstalled only from the dedicated emulator after AVD,
version and hash checks. Lab v158 remains installed and hash-verified there.

This proves the local refresh regression/fix with real Android session events and
the actual production presentation. Synthetic cached words deliberately isolate
external availability. It does NOT prove post-fix behavior on real Deezer/Shield,
audio synchronization on hardware, offline/no-lyrics runtime cases or full catalogue
coverage. Existing parser/transport tests for absent/error data remain green.
The Shield hold and no-merge boundary remain active. Recheck real track changes
there only once Ryan releases it; do not redo the approved design.

References checked during diagnosis:
https://developer.android.com/reference/android/media/session/MediaController
https://developer.android.com/reference/android/media/session/MediaSessionManager
