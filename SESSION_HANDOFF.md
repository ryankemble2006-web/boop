# BOOP current handoff

Updated 2026-09-20. Owner branch: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221` (kept long-lived so CI cache remains reusable). v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

## Current Shield: v224, signed and ready for Ryan's physical test

The branch name deliberately remains v221 for live iteration. Do not create a fresh branch for every one-pixel/UI tweak unless there is a reason to fork; same-branch follow-ups are what let GitHub reuse this branch-scoped Gradle cache.

Package `com.boop.shieldoverlay`, version `224` / `1.2.224-shield`.

v219's Now Playing alignment remains physically accepted as perfect. v220 accent colour, v217 reordering, v221 weather/focus behaviour and all voice/audio behaviour remain unchanged.

### v224 Now Playing transport simplification

The Now Playing transport row now contains only **Prev / Play-Pause / Next**. Rew and Fwd were removed because Deezer does not expose them usefully and the progress bar already handles ±10-second seeking.

Prev remains exactly on the accepted v219 datum: the row keeps `controls.setTranslationX(-dp(4))`. The surviving buttons keep the existing `CONTROL_GAP_DP = 10`, so Pause and Next collapse left into the removed buttons' spaces without recentering the row.

v223 weather geometry, v221 return-focus behaviour, v220 accent colour, v217 reordering and all voice/audio behaviour remain unchanged.

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

Production/build source: `16fbfdc632d821d7efd3911823a2035f3b8904b9`.
Successful GitHub Actions run `35518326403`, job `106097941527`.
Artifact `10606879001`: `BOOP-Shield-v224-Wall-v207-Signed`.

Deliver **BOOP-Shield-v224.apk**, 160485737 bytes.
Shield APK SHA-256: `ead4ecb9de285499d46d44dca49d3ed24f6322b9e590e1c956dfd9c9a4514a28`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `caaa3f168f5a6fef4713c32d4b043111a43042d93f29117a68c966add9293778`.

Verification passed focused Now Playing/alignment checks, inherited v206 checks, split materialization/integration, HA unit tests, both app builds and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

Wall remains v207.

## Physical acceptance pending

Ryan is the visual/ADB tester. Check that Prev has not moved, Rew/Fwd are gone, and Pause/Next bunch left with the original 10dp gaps.

Detailed record: `docs/handoffs/2026-09-20-shield-now-playing-transport-v224.md`.
