# BOOP current handoff

Updated 2026-09-20. Owner branch: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221` (kept long-lived so CI cache remains reusable). v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

## Current Shield: v229, signed and ready for Ryan's physical test

The branch name deliberately remains v221 for live iteration. Do not create a fresh branch for every one-pixel/UI tweak unless there is a reason to fork; same-branch follow-ups are what let GitHub reuse this branch-scoped Gradle cache.

Package `com.boop.shieldoverlay`, version `229` / `1.2.229-shield`.

v219's Now Playing alignment remains physically accepted as perfect. v220 accent colour, v217 reordering, v221 weather/focus behaviour and all voice/audio behaviour remain unchanged.

### v229 centred lyrics music column

The fullscreen lyrics left music column now uses the 397-design-pixel progress bar as its horizontal datum.

- Album art is centred on the progress-bar midpoint.
- Title and artist use the progress-bar width and centred text.
- Album-bottom -> title gap is 30 design pixels.
- Title-bottom -> artist gap is also 30 design pixels.
- Lyrics-screen transport is now **Prev / Play-Pause / Next** only.
- The 3-button group is centred on the progress-bar midpoint.
- Progress-bar left/right seeking remains ±10 seconds.

v228 one-line once-only marquee remains retained. v226 hard bitmap-mask corners remain retained and physically accepted by Ryan. v224 transport simplification, v223 weather geometry, v221 return-focus behaviour, v220 accent colour, v217 reordering and all voice/audio behaviour remain unchanged.

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

Production/build source: `e95bbeb88b32e9134da47100d0cd598df45752d7`.
Successful GitHub Actions run `35520822777`, job `106104476205`.
Artifact `10608905132`: `BOOP-Shield-v229-Wall-v207-Signed`.

Deliver **BOOP-Shield-v229.apk**, 160485741 bytes.
Shield APK SHA-256: `b4fa6a00ec74b0b1a2ab865adc8df8d82229143ad2ac1e204db5522e2ff79836`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `364bdcc540838ee3184f5fd780e5a836b6d674ac2acd133d7b9fcc5216a48668`.

Verification passed focused music-column/transport checks, inherited v206 checks, split materialization/integration, HA unit tests, both app builds and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

Wall remains v207.

## Physical acceptance pending

Ryan is the visual/ADB tester. Check the left column against the progress-bar centre, matching 30-pixel vertical gaps, and the centred 3-button transport row.

Detailed record: `docs/handoffs/2026-09-20-shield-lyrics-centred-column-v229.md`.
