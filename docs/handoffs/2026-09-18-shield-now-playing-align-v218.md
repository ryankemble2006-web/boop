# Shield v218 Now Playing alignment checkpoint

Date: 2026-09-18

Branch: `boop-shield-now-playing-align-v218`  
Owning split branch: `boop-wall-shield-split-v207`  
Shield package: `com.boop.shieldoverlay`  
Shield version: `218` / `1.2.218-shield`  
Wall remains: v207

## Request

Ryan approved the v217 favourite/HA movement and customisation and asked for a Now Playing alignment pass using the visible progress bar as the reference.

The screenshot showed title, artist, playback-state text and progress bar sharing the details-column left edge, while the transport row alone had an additional 8dp left inset.

## Change

In `ShieldNowPlayingView`, the transport container padding changed from:

`controls.setPadding(dp(8), 0, 0, 0)`

to:

`controls.setPadding(0, 0, 0, 0)`

No other production geometry was changed.

This means title, artist, playback-state label, progress bar and Prev/Rew/Pause/Fwd/Next now share the same left datum.

Lyrics/Close player, album art, mascot/eyes bay, HOME spacing, weather, HA controls, fan routing, and v217 reorder behavior are intentionally unchanged.

## Verification

Verified source: `a5f211a4038e208109e0ac31e18452c410a8f455`.

GitHub Actions:
- successful run: `35352592271`
- job: `105624043955`
- artifact: `10550157413`
- artifact name: `BOOP-Shield-v218-Wall-v207-Signed`
- uploaded ZIP SHA-256: `e9ca76c0fecb22d6de60815d63e9939ab021c26b40d07d3485f1890145f3ed9a`

Shield APK:
- file: `BOOP-Shield-v218.apk`
- bytes: `160485741`
- SHA-256: `37db129fce83bf8cca062e93d1474533d8f4b7253120da1255912914ff4d8b58`
- signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- all 16 native libraries remain byte-identical to the accepted baseline.

CI passed:
- 74 focused checks;
- inherited v206 regression;
- split materialization/integration;
- 97 integration checks;
- HA room/latency tests;
- both APK builds;
- signer/native/art verification.

## Physical acceptance

Pending Ryan's Shield visual test:
- title, artist, Playing, progress bar and transport row share one left edge;
- progress bar itself is unchanged;
- Lyrics/Close player and eye bay did not move;
- v217 favourite/HA reordering remains unchanged.
