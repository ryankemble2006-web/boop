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

Live read found Shield v143 `1.2.143-boop-shield-defaults`, not the earlier v137.
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
focused Gradle/JUnit tests. Signed CI APK build is the remaining publication check.
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
