# BOOP v142 lyrics preflight status

Updated 2026-09-12. This status applies only to `boop-v142-lyrics-fastfail`.
Base: v142 animation experiment, commit `5c383c68452766f9ecda059c2891f89c553e652b`.

## Implemented

- Select only the exact native Deezer recording ID from the selected MediaSession.
- Obtain an anonymous guest session; no account token, cookies or login input.
- Request only timed-line type information, not lyric text or audio.
- Available: retain the previously established notification/semantic Lyrics path.
- Unavailable: stay in BOOP and show `No lyrics for this track.`
- Unknown/offline/malformed/ambiguous: stay in BOOP and explain that checking failed.
- One in-flight check, 2.5-second UI deadline, cancellation on leaving Home and
  stale recording/session results discarded before any external launch.
- Existing v137 shortcut wiring imported narrowly; v142 animation files untouched.

## Evidence and boundary

18 focused JVM tests pass: response parsing, guest-only transport, cancellation,
recording/session identity, repeated presses, stale responses and deadline handling.
The real Java client also ran on the Shield through a temporary shell probe:
positive cold check 357 ms; negative check 48 ms; positive warm check 100 ms.
The foreground activity was unchanged throughout that API-only probe.
This is not a full-APK button acceptance test.

The installed Shield remains v143 `1.2.143-boop-shield-defaults`.
No app installation, signing change, permission grant or playback change was made.
Full local app compilation and the 18 focused Gradle/JUnit tests passed.
Signed CI APK build is the remaining publication check. Do not downgrade or overwrite the newer install.
See `SESSION_HANDOFF.md` before continuing.


Local full build/test compile receipt: 45 seconds, 79 Gradle tasks, 18 lyrics tests
with zero failures/errors. Canonical/shared media and listener-seeding checks also
passed. This does not promote the experiment or authorize installing over v143.
