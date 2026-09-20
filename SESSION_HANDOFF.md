# BOOP current handoff

Updated 2026-09-20. Owner branch: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221` (kept long-lived so CI cache remains reusable). v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

## Current Shield: v230, signed and ready for Ryan's physical test

The branch name deliberately remains v221 for live iteration. Do not create a fresh branch for every one-pixel/UI tweak unless there is a reason to fork; same-branch follow-ups are what let GitHub reuse this branch-scoped Gradle cache.

Package `com.boop.shieldoverlay`, version `230` / `1.2.230-shield`.

v219's Now Playing alignment remains physically accepted as perfect. v220 accent colour, v217 reordering, v221 weather/focus behaviour and all voice/audio behaviour remain unchanged.

### v230 title clipping micro-fix

The v229 centred lyrics music column is locked. v230 changes only the title TextView's internal font padding: `title.setIncludeFontPadding(false)`.

No geometry moved. Album, title, artist, progress and transport coordinates/gaps remain exactly v229. v228 one-line once-only marquee remains retained. v226 hard bitmap-mask corners remain retained and physically accepted by Ryan. v224 transport simplification, v223 weather geometry, v221 return-focus behaviour, v220 accent colour, v217 reordering and all voice/audio behaviour remain unchanged.

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

Production/build source: `7187d9686cffb52a67d5c3ceb575e565a8827019`.
Successful GitHub Actions run `35521333151`, job `106105827783`.
Artifact `10608133140`: `BOOP-Shield-v230-Wall-v207-Signed`.

Deliver **BOOP-Shield-v230.apk**, 160485741 bytes.
Shield APK SHA-256: `8cfe8808bfe51f3620da2dabd4b31b6f1c2debd0201eac6376530e633831eba3`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `a5ab79705cf26f08f99be6babc1e5196d31568a03651c39bf784d44fa3e625b1`.

Verification passed focused no-geometry-change/title-padding checks, inherited v206 checks, split materialization/integration, HA unit tests, both app builds and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

Wall remains v207.

## Physical acceptance pending

Ryan is the visual/ADB tester. Check only that the tiny title-glyph clipping is gone; spacing must remain visually identical to v229.

Detailed record: `docs/handoffs/2026-09-20-shield-title-font-padding-v230.md`.
