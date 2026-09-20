# BOOP current handoff

Updated 2026-09-20. Owner branch: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221` (kept long-lived so CI cache remains reusable). v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

## Current Shield: v232, signed and ready for Ryan's physical test

The branch name deliberately remains v221 for live iteration. Do not create a fresh branch for every one-pixel/UI tweak unless there is a reason to fork; same-branch follow-ups are what let GitHub reuse this branch-scoped Gradle cache.

Package `com.boop.shieldoverlay`, version `232` / `1.2.232-shield`

v219's Now Playing alignment remains physically accepted as perfect. v220 accent colour, v217 reordering, v221 weather/focus behaviour and all voice/audio behaviour remain unchanged.

### v232 lyrics source fallback

Lyrics now use a provider chain: **Deezer timed lyrics first, LRCLIB synced lyrics second**. LRCLIB is queried by current title/artist/album/duration metadata only when Deezer cannot provide an available timed document. Only synced LRCLIB LRC is accepted; plain untimed lyrics are deliberately ignored.

The HOME Lyrics preflight uses the same fallback, so a Deezer miss no longer blocks entry when LRCLIB has synced lyrics. v231 artist action, v230 title rendering, v229 centred column, v228 marquee and v226 hard-mask corners remain retained. v226 hard bitmap-mask corners remain retained and physically accepted by Ryan. v224 transport simplification, v223 weather geometry, v221 return-focus behaviour, v220 accent colour, v217 reordering and all voice/audio behaviour remain unchanged.

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

Production/build source: `ba25861b5bb9547fcf68c2dd816718716bbe6d27`.
Successful GitHub Actions run `35523199556`, job `106110743571`.
Artifact `10609073068`: `BOOP-Shield-v232-Wall-v207-Signed`.

Deliver **BOOP-Shield-v232.apk**, 160502125 bytes.
Shield APK SHA-256: `3f2b77070a95c48b9d69079338002636053a1688dfb1fb4ff1a394d3d9c98d9d`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `5aacedce35779fc1bc50cad88db36984e2d5081ddd896c1140dcf409bf5390e6`.

Focused LRCLIB fallback checks, inherited v206 checks, split materialization/integration, HA unit tests, both app builds and packaged signer/native/art verification passed. All 16 native libraries remain baseline-identical.

Wall remains v207.

## Physical acceptance pending

Ryan is the physical tester. Verify a track with missing Deezer lyrics can open synced lyrics from LRCLIB, and that ordinary Deezer lyric tracks remain unchanged.
