# Shield v222: one-pixel hourly temperature nudge

Date: 2026-09-20
Branch: `boop-shield-hour-temp-nudge-v222`
Verified build source: `5bab9dab597161a0a53ab5e6f85ef0f3d913bee0`

## Request

Ryan supplied the v221 Shield screenshot and requested only the four hourly temperature values under **Next 4 hours** (`20°, 20°, 20°, 18°`) to move one pixel to the right.

## Implementation

The existing weather stack stays on the accepted +2dp optical centre. The hourly temperature TextView is now explicitly retained after centring and receives:

`temp.setTranslationX(dp(2)+1f)`

That means the requested delta is one physical pixel, not another density-independent pixel.

No other weather line or launcher geometry changed.

## CI cache trial

This was also the first tiny UI iteration used to prove the newly enabled Gradle cache.

Successful run `35516142829` reported:
- `GRADLE_BUILD_ACTION_CACHE_RESTORED=true`;
- 154 actionable app-build tasks;
- 77 `FROM-CACHE`;
- 8 up-to-date;
- 69 executed;
- Gradle app build completed in 26 seconds.

The workflow now cancels superseded in-progress builds during rapid commit sequences, preventing old UI revisions from consuming a full CI build after a newer one is pushed.

## Verification

Successful GitHub Actions run `35516142829`, job `106092308080`.
Artifact `10606488920`: `BOOP-Shield-v222-Wall-v207-Signed`.

Shield:
- file: `BOOP-Shield-v222.apk`
- package: `com.boop.shieldoverlay`
- version: 222 / `1.2.222-shield`
- bytes: 160485741
- SHA-256: `3eee980f533a64b09d4fa0a25177c9c13bcbab2df17ec46f01c407f4c8f98bc5`
- permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Artifact ZIP SHA-256: `649641d807fbcab638ad2b65ef5ea35aed5aefd0a70d34e7a4e9c6036667fe30`.

Focused checks, inherited v206 checks, split materialization/integration, HA tests, both builds and packaged signer/native/art checks all passed. All 16 native libraries remain baseline-identical.

Wall remains v207.
