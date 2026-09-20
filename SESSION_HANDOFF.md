# BOOP current handoff

Updated 2026-09-20. Owner branch: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221` (kept long-lived so CI cache remains reusable). v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

## Current Shield: v222, signed and ready for Ryan's physical test

The branch name deliberately remains v221 for live iteration. Do not create a fresh branch for every one-pixel/UI tweak unless there is a reason to fork; same-branch follow-ups are what let GitHub reuse this branch-scoped Gradle cache.

Package `com.boop.shieldoverlay`, version `222` / `1.2.222-shield`.

v219's Now Playing alignment remains physically accepted as perfect. v220 accent colour, v217 reordering, v221 weather/focus behaviour and all voice/audio behaviour remain unchanged.

### v222 tiny weather polish

Only the four **Next 4 hours** temperature labels are changed. Their existing +2dp optical alignment is retained and the temperature text receives an additional **+1 physical pixel** horizontal nudge to the right.

Times, weather glyphs, rain percentages, 3-day forecast geometry, current weather and footer are unchanged.

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

Production/build source: `5bab9dab597161a0a53ab5e6f85ef0f3d913bee0`.
Successful GitHub Actions run `35516142829`, job `106092308080`.
Artifact `10606488920`: `BOOP-Shield-v222-Wall-v207-Signed`.

Deliver **BOOP-Shield-v222.apk**, 160485741 bytes.
Shield APK SHA-256: `3eee980f533a64b09d4fa0a25177c9c13bcbab2df17ec46f01c407f4c8f98bc5`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `649641d807fbcab638ad2b65ef5ea35aed5aefd0a70d34e7a4e9c6036667fe30`.

Verification passed focused Shield checks, inherited v206 checks, split materialization/integration, HA unit tests, both app builds and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

Wall remains v207.

## Physical acceptance pending

Ryan is the visual/ADB tester. Check only that the four hourly temperature values are now one pixel farther right. Everything else should remain visually identical to v221.

Detailed record: `docs/handoffs/2026-09-20-shield-hour-temp-nudge-v222.md`.
