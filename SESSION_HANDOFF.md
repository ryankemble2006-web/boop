# BOOP current handoff

Updated 2026-09-20. Owner branch: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221` (kept long-lived so CI cache remains reusable). v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

## Current Shield: v231, signed and ready for Ryan's physical test

The branch name deliberately remains v221 for live iteration. Do not create a fresh branch for every one-pixel/UI tweak unless there is a reason to fork; same-branch follow-ups are what let GitHub reuse this branch-scoped Gradle cache.

Package `com.boop.shieldoverlay`, version `231` / `1.2.231-shield`

v219's Now Playing alignment remains physically accepted as perfect. v220 accent colour, v217 reordering, v221 weather/focus behaviour and all voice/audio behaviour remain unchanged.

### v231 lyrics artist action

The fullscreen lyrics artist now clones HOME's artist action exactly: text-only accent focus via `BoopTvChrome.useTextOnlyFocus(artist)`, D-pad navigation through **artwork ↕ artist ↕ progress**, and selecting the artist opens the same `DeezerArtistBrowser` route used on HOME.

v230 title rendering, v229 centred column, v228 marquee and v226 hard-mask corners remain retained. v226 hard bitmap-mask corners remain retained and physically accepted by Ryan. v224 transport simplification, v223 weather geometry, v221 return-focus behaviour, v220 accent colour, v217 reordering and all voice/audio behaviour remain unchanged.

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

Production/build source: `d8fd4251e1788e3d056f40a96b8b5cdd04399ad3`.
Successful GitHub Actions run `35522094041`, job `106107816107`.
Artifact `10608946787`: `BOOP-Shield-v231-Wall-v207-Signed`.

Deliver **BOOP-Shield-v231.apk**, 160485741 bytes.
Shield APK SHA-256: `6ea6f021f272261ff59c01c757db02d8aa8d16c920bfe446819ccaf874007f37`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `b2b25c13bf9e388a599ec133aacec18539f197a4188b2d758d514356cecc5724`.

Focused artist-action/activity checks, inherited v206 checks, split materialization/integration, HA unit tests, both app builds and packaged signer/native/art verification passed. All 16 native libraries remain baseline-identical.

Wall remains v207.

## Physical acceptance pending

Ryan is the visual/ADB tester. Check that lyrics artist focus turns accent orange and selecting it opens the correct Deezer artist page.

Detailed record: `docs/handoffs/2026-09-20-shield-lyrics-artist-v231.md`.
