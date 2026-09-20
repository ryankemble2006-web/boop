# BOOP current handoff

Updated 2026-09-20. Owner branch: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221` (kept long-lived so CI cache remains reusable). v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

## Current Shield: v233, signed and ready for Ryan's physical test

The branch name deliberately remains v221 for live iteration. Do not create a fresh branch for every one-pixel/UI tweak unless there is a reason to fork; same-branch follow-ups are what let GitHub reuse this branch-scoped Gradle cache.

Package `com.boop.shieldoverlay`, version `233` / `1.2.233-shield`

v219's Now Playing alignment remains physically accepted as perfect. v220 accent colour, v217 reordering, v221 weather/focus behaviour and all voice/audio behaviour remain unchanged.

### v233 hardened lyrics fallback

Lyrics still use **Deezer timed lyrics first, LRCLIB synced lyrics second**, but v233 fixes the fallback failure seen on physical test.

LRCLIB now receives its own 6-second window after Deezer's 2.5-second primary window. The broad search uses LRCLIB's documented `track_name + q` form; a search 404 is treated as a clean miss rather than a service error. Matching still requires track/artist identity and duration when known, while harmless metadata cosmetics such as a leading “The” are tolerated.

v231 artist action, v230 title rendering, v229 centred column, v228 marquee and v226 hard-mask corners remain retained. v226 hard bitmap-mask corners remain retained and physically accepted by Ryan. v224 transport simplification, v223 weather geometry, v221 return-focus behaviour, v220 accent colour, v217 reordering and all voice/audio behaviour remain unchanged.

### CI iteration speed

The signed Shield/Wall workflow now:
- restores and saves writable Gradle caches on the active Shield branch;
- invokes Gradle with `--build-cache`;
- cancels superseded in-progress builds during rapid UI iteration.

The v222 production run confirmed the cache is genuinely active:
- `GRADLE_BUILD_ACTION_CACHE_RESTORED=true`;
- app build: **154 actionable tasks: 69 executed, 77 from cache, 8 up-to-date**;
- Gradle app build completed in **26 seconds**.

This does not shrink the APK delivered to Ryan, but it substantially reduces repeated CI compilation/dependency work.

## Verified signed artifact

Production/build source: `9e319d7336e7b52d54c080ed8d3bd596c805ae3d`.
Successful GitHub Actions run `35523758443`, job `106112227680`.
Artifact `10608914071`: `BOOP-Shield-v233-Wall-v207-Signed`.

Deliver **BOOP-Shield-v233.apk**, 160502125 bytes.
Shield APK SHA-256: `dfcc8522ca8137f3755abefe1e8b23eecab68a9122816089cec7f544700920d0`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `eed3fd8f0fe5051ca3d2d03656d1446ffc04ad4ef949822b17c08fa55d103506`.

Verification passed focused fallback-window/search checks, inherited v206 checks, split materialization/integration, HA unit tests, both app builds and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

Wall remains v207.

## Physical acceptance pending

Ryan is the physical tester. Re-test the same track that returned “Couldn't check lyrics just now”, especially Ying Tong. Ordinary Deezer lyric tracks should remain unchanged.

Detailed record: `docs/handoffs/2026-09-20-shield-lrclib-fallback-v233.md`.
