# BOOP current handoff

Updated 2026-09-20. Owner branch: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221` (kept long-lived so CI cache remains reusable). v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

## Current Shield: v227, signed and ready for Ryan's physical test

The branch name deliberately remains v221 for live iteration. Do not create a fresh branch for every one-pixel/UI tweak unless there is a reason to fork; same-branch follow-ups are what let GitHub reuse this branch-scoped Gradle cache.

Package `com.boop.shieldoverlay`, version `227` / `1.2.227-shield`.

v219's Now Playing alignment remains physically accepted as perfect. v220 accent colour, v217 reordering, v221 weather/focus behaviour and all voice/audio behaviour remain unchanged.

### v227 long lyrics title spacing

The fullscreen lyrics title block alone moves up 12 design pixels: `place(title, left, 448f * unit, ...)` -> `place(title, left, 436f * unit, ...)`. Artist, progress, timing and transport geometry remain exactly where they were.

v226 hard bitmap-mask corners remain retained and physically accepted by Ryan. v224 transport simplification, v223 weather geometry, v221 return-focus behaviour, v220 accent colour, v217 reordering and all voice/audio behaviour remain unchanged.

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

Production/build source: `51cf2687d71c7580279163332a72e3955a1f26f6`.
Successful GitHub Actions run `35519761368`, job `106101711931`.
Artifact `10607489121`: `BOOP-Shield-v227-Wall-v207-Signed`.

Deliver **BOOP-Shield-v227.apk**, 160485741 bytes.
Shield APK SHA-256: `99bb4089224f98f7d7b0ae1cae3640447737db02767cb6e94d8dddfba567111f`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `03fb296d6682b1171452a892ea43724bb05b7f66b2400eeeb89dfbae41b5280c`.

Verification passed focused title-geometry checks, inherited v206 checks, split materialization/integration, HA unit tests, both app builds and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

Wall remains v207.

## Physical acceptance pending

Ryan is the visual/ADB tester. Check a two-line long track title for bottom clipping while confirming artist/progress did not move.

Detailed record: `docs/handoffs/2026-09-20-shield-lyrics-title-v227.md`.
