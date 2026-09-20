# BOOP current handoff

Updated 2026-09-20. Owner branch: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221` (kept long-lived so CI cache remains reusable). v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

## Current Shield: v225, signed and ready for Ryan's physical test

The branch name deliberately remains v221 for live iteration. Do not create a fresh branch for every one-pixel/UI tweak unless there is a reason to fork; same-branch follow-ups are what let GitHub reuse this branch-scoped Gradle cache.

Package `com.boop.shieldoverlay`, version `225` / `1.2.225-shield`.

v219's Now Playing alignment remains physically accepted as perfect. v220 accent colour, v217 reordering, v221 weather/focus behaviour and all voice/audio behaviour remain unchanged.

### v225 lyrics-screen corner cleanup

The fullscreen lyrics artwork now uses the same shared rounded clipping/background path as HOME landscape banners: `FocusChrome.filled(..., 9, false)` plus `FocusChrome.clipRounded(artwork, 9)`. This replaces the custom outline-provider path that allowed album art to show beyond the rounded corner at the top-right.

The tiny bottom-right lyric provider/licence credit is no longer drawn. The attribution data remains parsed internally; lyric lookup, timing, status text and transport behaviour are unchanged.

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

Production/build source: `8b252a2250e07187095303b25cf1d27298b715f1`.
Successful GitHub Actions run `35518868544`, job `106099355568`.
Artifact `10607712461`: `BOOP-Shield-v225-Wall-v207-Signed`.

Deliver **BOOP-Shield-v225.apk**, 160485741 bytes.
Shield APK SHA-256: `b6625c5ad25a1b6e3afbd6e6e40ef76ae7db7c59b2c750e8748301dcd258d328`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `e8a31f636ac9e98889ec11f4154d036dba4c42d43dbcd3b8fab1f0937f2456f6`.

Verification passed focused lyrics-presentation checks, inherited v206 checks, split materialization/integration, HA unit tests, both app builds and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

Wall remains v207.

## Physical acceptance pending

Ryan is the visual/ADB tester. Check the fullscreen lyrics artwork corners, especially top-right, and confirm the bottom-right licence/provider text is gone.

Detailed record: `docs/handoffs/2026-09-20-shield-lyrics-corners-v225.md`.
