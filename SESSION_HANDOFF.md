# BOOP v142 lyrics preflight continuation

Updated 2026-09-12. Owning branch/worktree: `boop-v142-lyrics-fastfail`.
Base: `5c383c68452766f9ecda059c2891f89c553e652b` (v142 animation experiment).
Current request: add only the no-lyrics preflight to this isolated v142 candidate.
The inherited Startup Manager-only handoff described a different, earlier task.
Do not overwrite its accepted checkpoint or concurrent animation/defaults work.

## New evidence

- Deezer permits a fresh anonymous session at its `login/anonymous?jo=p` endpoint.
- This does not read or reuse the user's account token, cookie or login.
- The guest bearer can query Pipe's exact track-ID synchronized-lyrics fields.
- A formerly false-negative public `LYRICS_ID=0` track returned both timed lists.
- A separate instrumental returned explicit null timed lists, without API errors.
- The actual Shield MediaSession exposes the exact playable ID and type through
  `com.deezer.METADATA_KEY_PLAYABLE_IDENTIFIER_ID` and `_TYPE` (`TRACK`).
- No fuzzy title/album search is necessary. Missing identity must remain unknown.
- The old `/lyrics/<id>` URL test did not prove a new route: it could restore an
  already-open screen. Preserve the established notification/semantic-button path.

## Deployment boundary

Latest live read found Shield v145 `1.2.145-boop-defaults-visible`, not the earlier v137.
No app installation, grants, settings or playback changes made in this continuation.
Keep the newer installed build. No v142 downgrade or cross-task merge is authorized.

## Implemented and tested

The v137 positive shortcut delta was applied cleanly onto v142, excluding its old
version, documentation, artwork and other task changes. New preflight classes are
DeezerLyricsClient, DeezerLyricsHttp and DeezerLyricsGate; DeezerLyricsBrowser runs
checks off the main thread, bounds UI waiting to 2.5 seconds, coalesces duplicate
presses, and invalidates work on lifecycle/track changes. Only a fresh AVAILABLE
result may reach the existing notification launch and semantic-button action.
UNAVAILABLE and UNKNOWN do not construct the local ADB bridge or open Deezer.
The established positive bridge still uses its existing UI verification; this
change eliminates that path entirely for negative/unknown preflight outcomes.

18 focused JVM tests pass after test-first failure and implementation. The exact
Java client ran on Shield as an API-only shell probe: 357 ms cold positive, 48 ms
negative, 100 ms warm positive. Foreground stayed unchanged. This is not an APK
button test. Local full materialization and app/test compilation passed, followed by all 18
focused Gradle/JUnit tests. Signed CI APK build has passed; see the final receipt below.
Private evidence and third-party inspection remain ignored under private-inspect/.
Do not stage that directory, device addresses, tokens, logs, images or APKs.


## Local verification receipt

- Clean materialization through `scripts/materialize-unified.sh`: exit 0.
- Full Unified compile and `:app:testDebugUnitTest --tests '*DeezerLyrics*Test'`:
  BUILD SUCCESSFUL in 45 seconds, 79 tasks, 18 tests with no failures/errors.
- `scripts/test-canonical-core.py`: all seven shared/lyrics behavior checks pass.
- `tests/test_lyrics_listener_seed_contract.py`: PASS (direct script execution).
- Existing Android/Gradle deprecation warnings remain; no new dependency added.
- Manual diff review: exact-ID binding, error/unknown semantics, cancellation,
  API response-size/deadline guards, lifecycle ownership and scoped imports checked.
- This review was in-session, not a separate reviewer or visual acceptance test.
- Original local Bash launch inherited an interactive stdin and advanced slowly;
  only that owned process tree was stopped. Re-running with stdin=DEVNULL completed.
  No other worktree/process was stopped or modified.
- All private probe/decompilation data remains outside Git under private-inspect/.

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
