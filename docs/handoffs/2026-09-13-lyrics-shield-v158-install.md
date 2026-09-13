# Lyrics Lab v158 Shield installation: verified, refresh check interrupted

Date: 2026-09-13. Branch: `boop-lyrics-lab-side-by-side-v157`.
Ryan answered "do it" to the proposed real-Deezer/Shield refresh check after the
local-only results. This releases the Shield hold for this specific lab deployment
and test. It does not authorize a Unified/main merge, phone access, new permissions,
redesign, or installation of any synthetic test provider on physical hardware.

## Confirmed physical operations

- Read the live lab branch at `732f4fc53681cb8622a78bc8faf2744f7ec93038` and its
  current handoff. Rechecked signed GitHub artifact `10320107611` from run
  `34766477538`, source `5970f59aa9173fd8171d2d370b856349d0cf4f17`.
- Shield was reachable over its authorized ADB connection. Original lab was v157
  with installed APK hash `5edd429190f4b8ca4c37390b91851a2b30f1ce265a6a3c49be3512bef2ff2e91`.
- Existing Unified was `159 / 1.2.159-shared-eye-colour`, with baseline APK hash
  `80e86119d4771624ff47617373df2cebc03c0454aa47f10a0996a6d507b68353`.
  It was not an installation or edit target. No final post-test hash was obtained.
- The installed native Deezer app was `301000101 / 1.0.1.1`, with baseline APK hash
  `6ae269a08a4a5d3c66084cdd056b5f7339780549a19e2939351588f9a5a1df15`.
  It had an active PLAYING session and was the foreground app before lab launch.
  It was not replaced or modified.
- The lab's existing notification-listener access was already enabled. No grant,
  settings change or imported permission was performed.
- Revalidated the cached signed APK's exact package/version, built-source receipt,
  SHA256 and permanent signer before installation. `adb install -r` succeeded for
  ONLY `com.boop.lyricslab`.
- Physical package now reported `158 / 0.1.158-lyrics-session-sync`. Its installed
  base APK SHA256 exactly matched the tested candidate:
  `c95bb9cc343ec9752fb2be610d0c61c0e4d01eb4faa3c62b8c6b28713db00c09`.
- Permanent signer checked before install:
  `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Launch returned `Status: ok`; `LyricsLabActivity` was resumed. The lab process
  reported `Timed document ready: lines=50` at 17:53:17 device-local time. This
  confirms a real timed-document load, not automatic track-change verification.

## Interrupted runtime step

A private laptop-only test helper was written under the temporary directory. It
uses an explicit Shield serial and checks that LyricsLabActivity is foreground
before any input. Capture data is private; song lyric text is never printed or
published. The next submitted command requested an initial capture, one Next
key, then a second capture.

Desktop Commander returned "Timeout - no response from device" without a process
ID or command output. The command's execution outcome is UNKNOWN: it may or may
not have captured data or sent that one Next. Do not replay it blindly. Subsequent
session-list and explicit ping requests also timed out. The connected-device
registry still listed Yoga as online with Desktop Commander 0.2.47, but that is
not a successful live ping. No cause such as moved files, revoked permissions,
SDK failure or app regression was established.

No track-change assertion, real-player seek/pause/reopen result, screenshot
review, final foreground state or final Unified hash was confirmed after that
point. Keep the earlier 38x2 emulator result distinct from this physical stage.
No new app code, build, signer, permissions, connection version or command was
changed to work around the timeout. No merge was attempted.

## Resume without duplicating side effects

Once the existing Desktop Commander connection responds, inspect owned terminal
sessions and the private captures `01-initial` / `02-next` under the task's local
temporary test cache before sending playback commands. The tested v158 is already
installed and hash-verified; do not reinstall merely to resume. Check current
foreground/player state and finish the real next/previous automatic-refresh gate.
Use only the separate lab. Do not install the emulator fixture on the Shield.
Neither phone is a target. The no-merge boundary remains in force until Ryan's
separate approval after physical refresh validation.
