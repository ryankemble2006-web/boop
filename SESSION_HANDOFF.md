# BOOP current handoff

Updated 2026-09-20. Owner branch: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221` (kept long-lived so CI cache remains reusable). v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

## Current Shield: v226, signed and ready for Ryan's physical test

The branch name deliberately remains v221 for live iteration. Do not create a fresh branch for every one-pixel/UI tweak unless there is a reason to fork; same-branch follow-ups are what let GitHub reuse this branch-scoped Gradle cache.

Package `com.boop.shieldoverlay`, version `226` / `1.2.226-shield`.

v219's Now Playing alignment remains physically accepted as perfect. v220 accent colour, v217 reordering, v221 weather/focus behaviour and all voice/audio behaviour remain unchanged.

### v226 lyrics-screen hard bitmap mask

The fullscreen lyrics artwork no longer relies on Android outline clipping. The bitmap is now clipped directly inside the artwork ImageView's `onDraw()` using a rounded `Path` and `canvas.clipPath(...)` before `super.onDraw(canvas)`, then the orange focus ring is drawn after the clip is restored.

This specifically targets the remaining square-pixel leak visible at the album-art corners in v225. The bottom-right lyric provider/licence credit remains removed.

v224 transport simplification, v223 weather geometry, v221 return-focus behaviour, v220 accent colour, v217 reordering and all voice/audio behaviour remain unchanged.

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

Production/build source: `4758eaf8b4c74cd27b984af62e5be5d3f50e9280`.
Successful GitHub Actions run `35519275112`, job `106100424396`.
Artifact `10607129240`: `BOOP-Shield-v226-Wall-v207-Signed`.

Deliver **BOOP-Shield-v226.apk**, 160485741 bytes.
Shield APK SHA-256: `eb52fefacfec25c5ce8173c6b6466f14fdabfa35d8175c131b7d2f016a62f0ee`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `55c53470a621c5956e63bb3e440503bd3e5dd031634626a86db3faf6151c66c1`.

Verification passed the hard-mask lyrics presentation check, inherited v206 checks, split materialization/integration, HA unit tests, both app builds and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

Wall remains v207.

## Physical acceptance pending

Ryan is the visual/ADB tester. Check the album-art corners again, especially the top-right corner that still leaked in v225.

Detailed record: `docs/handoffs/2026-09-20-shield-lyrics-hard-mask-v226.md`.
