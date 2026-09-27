# Shield254: LRCLIB first with bounded transient recovery

Ryan requested repair of Home's lyrics lookup and LRCLIB as the primary service, with Deezer as fallback. Base: accepted Shield253 owner `boop-shield-seren-v252` at `b065b911120b6a4451151a6a07571a5611304aa6`. Fix branch: `boop-shield-lyrics-v254`.

Read-only physical investigation found David Essex / Gonna Make You a Star / Best Of David Essex, duration218seconds, on Shield253. LRCLIB's search endpoint returned HTTP503 directly on the Shield and HTTP200 on a repeat; matching timed candidates exist. The exact result can contain only untimed lyrics, requiring the search route. This establishes a transient service failure, not a metadata rejection.

The lookup now gives LRCLIB the first6seconds and Deezer the subsequent2.5seconds inside the existing9second outer deadline. Each LRCLIB HTTP request gets at most one retry on transport failures or HTTP500/502/503/504. Permanent HTTP errors are not retried. Cancellation and remaining time are checked before requests and while reading. Only matching timed lyrics are accepted; artist/title and3.5second duration tolerance are unchanged. A failure from either provider remains uncertain unless the other succeeds; two clean misses report no lyrics.

New executable JVM regressions exercise the production loader, both provider clients and HTTP transport with deterministic response boundaries. They failed before the fix on503/timeout recovery and provider ordering, and pass after it. Initial focused result:14tests passed, including18 resilience assertions, Home entry, Activity, queue and presentation tests. Independent review found no actionable issues. Bare local pytest stopped at collection because the environment lacks `aiohttp` for `bridge/test_boop_wyoming_bridge.py`. Initial CI36351683593 passed137checks and failed the old253 release-version assertion; the test and APK verifier are updated together for254. Signed CI and installed-device validation are pending. No phone deployment is planned.

## Signed delivery and installed verification

Final source `7b8740c2cdb66e8e3de8bd9939cb00a2756215e0` passed the complete maintained [signed CI36351789171](https://github.com/ryankemble2006-web/boop/actions/runs/36351789171), including new resilience regressions, inherited checks, clean materialization, integration checks, HA unit tests, signed compilation and APK/art/native verification.

Shield254 is installed in place. APK SHA256: `cfedb77a0629a844cd375327f4b93932d7e0b03874ff1967dadfabecb54788a4`. Permanent signer remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Installed hash and version match the signed artifact. All29assets and16native libraries match installed253; UID, first-install history and every preference file are unchanged. Rollback APK and private preference backups are retained locally. Only Shield was deployed.

Three independent fresh processes under the installed app UID invoked the actual installed NativeLyricsLoader with the original David Essex metadata (218seconds). All returned AVAILABLE /44timed lines in522,492and432ms. This verifies installed Android networking and the production lookup/callback path; it is not a claim of manual remote-button or visual timing acceptance. The user's foreground had changed to Kodi with Deezer paused, so no playback was resumed or replaced for a UI test. A direct attempt to launch the private Home Activity was rejected as not exported and changed no UI. Temporary probe files were removed. Ryan's next normal Home Lyrics press remains the physical UI acceptance point.

Deliverables: `C:/Users/ryank/Documents/Codex/2026-09-27/on-x20/outputs/BOOP-Shield-Lyrics-v254`. Private diagnostics and rollback remain under this task's `work/`.
