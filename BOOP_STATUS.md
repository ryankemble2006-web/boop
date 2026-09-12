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

The installed Shield is now v145 `1.2.145-boop-defaults-visible`.
No app installation, signing change, permission grant or playback change was made.
Full local app compilation and the 18 focused Gradle/JUnit tests passed.
Signed CI APK build passed; exact receipt follows. Do not downgrade or overwrite the newer install.
See `SESSION_HANDOFF.md` before continuing.


Local full build/test compile receipt: 45 seconds, 79 Gradle tasks, 18 lyrics tests
with zero failures/errors. Canonical/shared media and listener-seeding checks also
passed. This does not promote the experiment or authorize installing over v145.

## Signed candidate and final verification, 2026-09-12

- Source: `fc0b552fe058cfaa0d29668cc1400d7fc8675ed5`.
- Branch: `boop-v142-lyrics-fastfail`; code published and live HEAD verified.
- Version stays `142 / 1.2.142-animation-experimental`, package `com.boop.alpha1`.
- Signed GitHub run `34681420296`: completed successfully.
- Artifact `BOOP-Unified`, ID `10293739817`.
- APK SHA256: `720095d91a5481c49d7fe26aae9576217b5c4623024f4887724aee5d81092abb`.
- Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Download independently checked: built commit, package, version, APK hash,
  APK ZIP integrity and v2 signature all passed. No replacement signer.
- Full local Unified unit task: 295 tests across 85 suites, no failures/errors/skips.
- Focused lyrics subset: 18 tests. Startup Manager: all 15 suites passed.
- Seven canonical/shared media checks and listener-seeding check passed.
- Real-device API-only probe: positive 357 ms cold, negative 48 ms, positive 100 ms
  warm. Screen unchanged. These are measured individual checks, not timing guarantees.

Latest device inspection found v145 `1.2.145-boop-defaults-visible` installed by
another task. The earlier v143 observations are superseded. Preserve v145 and its
visible BOOP-defaults controls. This v142-based signed candidate is NOT installed,
NOT a superset of v145 and NOT full-button physically accepted. Before deployment,
obtain approval for a deliberate lyrics-only forward integration into that newer
lineage; do not downgrade, merge whole app branches, or discard its changes.

All artwork, animation source and permission declarations are unchanged against
the selected v142 base. No device install, grants, setting change or playback
change was performed here. Private probes, third-party inspection, raw evidence,
APK and generated build/cache files remain ignored and unshared. No app code is
left uncommitted. The temporary owned device probe dex files were removed.
