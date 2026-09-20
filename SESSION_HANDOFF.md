# BOOP current handoff

Updated 2026-09-20. Owner branch: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221` (kept long-lived so CI cache remains reusable). v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

## Current Shield: v228, signed and ready for Ryan's physical test

The branch name deliberately remains v221 for live iteration. Do not create a fresh branch for every one-pixel/UI tweak unless there is a reason to fork; same-branch follow-ups are what let GitHub reuse this branch-scoped Gradle cache.

Package `com.boop.shieldoverlay`, version `228` / `1.2.228-shield`.

v219's Now Playing alignment remains physically accepted as perfect. v220 accent colour, v217 reordering, v221 weather/focus behaviour and all voice/audio behaviour remain unchanged.

### v228 one-line lyrics title marquee

The fullscreen lyrics title is back at its original vertical position and is now single-line. Overflowing titles use Android marquee with `setMarqueeRepeatLimit(1)`, so they auto-scroll once; short titles remain static.

The title geometry is now `place(title, left, 448f * unit, 440f * unit, 42f * unit)`. Artist and progress remain fixed at their accepted positions. v226 hard bitmap-mask corners remain retained and physically accepted by Ryan. v224 transport simplification, v223 weather geometry, v221 return-focus behaviour, v220 accent colour, v217 reordering and all voice/audio behaviour remain unchanged.

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

Production/build source: `0dae3cd377f85374f5ca6f01bb01d4a8d5a41cbe`.
Successful GitHub Actions run `35520215512`, job `106102885432`.
Artifact `10607839097`: `BOOP-Shield-v228-Wall-v207-Signed`.

Deliver **BOOP-Shield-v228.apk**, 160485741 bytes.
Shield APK SHA-256: `7a90cef6cc660028d088f382ac3da09e658318ca1934674f41ff18041f324763`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `af9bf5900ed05471749bc3ea63bc2fb65721a630a012defe666031d17c6c02a7`.

Verification passed focused marquee/title checks, inherited v206 checks, split materialization/integration, HA unit tests, both app builds and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

Wall remains v207.

## Physical acceptance pending

Ryan is the visual/ADB tester. Check that short titles remain still and correctly spaced, while an overflowing title scrolls once on one line.

Detailed record: `docs/handoffs/2026-09-20-shield-lyrics-title-marquee-v228.md`.
