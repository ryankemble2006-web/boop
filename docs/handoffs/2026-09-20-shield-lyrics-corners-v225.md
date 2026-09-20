# Shield v225: lyrics artwork corner cleanup

Date: 2026-09-20
Branch: `boop-shield-weather-focus-v221`
Verified build source: `8b252a2250e07187095303b25cf1d27298b715f1`

## Request

Fix album art bleeding through the rounded corners in the fullscreen lyrics screen, especially top-right, using the same treatment as HOME landscape favourites. Remove the small bottom-right lyric provider/licence text.

## Implementation

Lyrics artwork now uses:
- `FocusChrome.filled(context, Color.rgb(16, 24, 29), 9, false)`
- `FocusChrome.clipRounded(artwork, 9)`

The former custom `ViewOutlineProvider` clipping path is no longer used.

The provider/licence credit TextView is no longer created or positioned. The underlying lyric document still retains its credit field, so this is presentation-only.

## Verification

Successful GitHub Actions run `35518868544`, job `106099355568`.
Artifact `10607712461`: `BOOP-Shield-v225-Wall-v207-Signed`.

Shield:
- file: `BOOP-Shield-v225.apk`
- package: `com.boop.shieldoverlay`
- version: 225 / `1.2.225-shield`
- bytes: 160485741
- SHA-256: `b6625c5ad25a1b6e3afbd6e6e40ef76ae7db7c59b2c750e8748301dcd258d328`
- permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Artifact ZIP SHA-256: `e8a31f636ac9e98889ec11f4154d036dba4c42d43dbcd3b8fab1f0937f2456f6`.

Focused lyrics-presentation checks, inherited v206 checks, split materialization/integration, HA tests, both builds and packaged signer/native/art checks passed. All 16 native libraries remain baseline-identical.

Wall remains v207.
