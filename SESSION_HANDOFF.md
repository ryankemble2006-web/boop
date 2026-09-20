# BOOP current handoff

Updated 2026-09-20. Owner branch: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221` (kept long-lived so CI cache remains reusable). v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

## Current Shield: v223, signed and ready for Ryan's physical test

The branch name deliberately remains v221 for live iteration. Do not create a fresh branch for every one-pixel/UI tweak unless there is a reason to fork; same-branch follow-ups are what let GitHub reuse this branch-scoped Gradle cache.

Package `com.boop.shieldoverlay`, version `223` / `1.2.223-shield`.

v219's Now Playing alignment remains physically accepted as perfect. v220 accent colour, v217 reordering, v221 weather/focus behaviour and all voice/audio behaviour remain unchanged.

### v223 tiny weather polish

Only the four **Next 4 hours** temperature labels changed from v222. Their existing +2dp optical alignment is retained and the temperature text moves **2 more physical pixels right**, changing `temp.setTranslationX(dp(2)+1f)` to `temp.setTranslationX(dp(2)+3f)`.

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

Production/build source: `fceb5659a5fe52d01205641956b834ff5615385e`.
Successful GitHub Actions run `35517619079`, job `106096104834`.
Artifact `10607337069`: `BOOP-Shield-v223-Wall-v207-Signed`.

Deliver **BOOP-Shield-v223.apk**, 160485741 bytes.
Shield APK SHA-256: `2bc6179110d22f3a76243e1914f8f19db7573e74694c5e85e8aa40493d7340d4`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `05e542e8c4e090792781dd24d3f53fff9e33ce4c3a85b2fb65b3fdfee5f5c3bd`.

Verification passed focused Shield checks, inherited v206 checks, split materialization/integration, HA unit tests, both app builds and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

Wall remains v207.

## Physical acceptance pending

Ryan is the visual/ADB tester. Check only that the four hourly temperature values are now 2 physical pixels farther right than v222. Everything else should remain visually identical to v222.

Detailed record: `docs/handoffs/2026-09-20-shield-hour-temp-nudge-v223.md`.
