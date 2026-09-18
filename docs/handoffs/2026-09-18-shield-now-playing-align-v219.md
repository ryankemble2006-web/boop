# Shield v219 Now Playing alignment checkpoint

Date: 2026-09-18

Branch: `boop-shield-now-playing-align-v219`  
Owning split branch: `boop-wall-shield-split-v207`  
Shield package: `com.boop.shieldoverlay`  
Shield version: `219` / `1.2.219-shield`  
Wall remains: v207

## Request

Ryan's v218 screenshot showed two remaining visual offsets in Now Playing:
- unfocused Prev still needed to move slightly left relative to the progress track;
- the progress track needed to finish on the visible right edge of Close player.

## Change

Only `ShieldNowPlayingView` geometry changed:

- `controls.setTranslationX(-dp(4))` moves the transport row 4dp left visually;
- `progressParams.rightMargin = dp(8)` shortens the progress track by the same trailing space used by the Close player button.

The existing zero transport padding from v218 remains.

No changes were made to title/artist/state positions, Lyrics/Close player layout, artwork, mascot bay, weather, HA transport, or v217 reorder/customisation.

## Verification

Verified source: `9ad31957cba942f857c4d48588702d9b4ab89cb4`.

GitHub Actions:
- successful run: `35353692917`
- job: `105627714059`
- artifact: `10551187345`
- artifact name: `BOOP-Shield-v219-Wall-v207-Signed`
- uploaded ZIP SHA-256: `4f8f5016bca24475d808c7e9afc0fb3c8d8c07f639986fd084e56639b0070e60`

Shield APK:
- file: `BOOP-Shield-v219.apk`
- bytes: `160485741`
- SHA-256: `d6462470ca1b95e8672d038acd902c7bdd57d687aaa8146e1b0d66e2e222bdae`
- signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- all 16 native libraries remain byte-identical to the accepted baseline.

CI passed 74 focused checks, inherited v206 verification, split materialization/integration, 97 integration checks, HA tests, both APK builds, and signer/native/art verification.

## Physical acceptance

Pending Ryan's Shield visual check:
- unfocused Prev visually shares the progress-track left edge;
- progress track ends on Close player's visible right edge;
- all accepted v217/v218 layout and reorder behavior remains unchanged.
