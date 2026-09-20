# Shield v226: hard-mask fullscreen lyrics artwork

Date: 2026-09-20
Branch: `boop-shield-weather-focus-v221`
Verified build source: `4758eaf8b4c74cd27b984af62e5be5d3f50e9280`

## Request

v225 still showed square album-art pixels leaking at the rounded corners. Use a different clipping method.

## Implementation

The lyrics artwork ImageView now creates a rounded `Path` each draw, clips the Canvas with `canvas.clipPath(artworkClip)`, draws the bitmap via `super.onDraw(canvas)`, restores the canvas, then draws the orange focus stroke.

`clipToOutline` is disabled for this artwork. This makes the bitmap itself obey the rounded mask instead of depending on outline clipping.

The bottom-right lyric provider/licence credit remains removed.

## Verification

Successful GitHub Actions run `35519275112`, job `106100424396`.
Artifact `10607129240`: `BOOP-Shield-v226-Wall-v207-Signed`.

Shield:
- file: `BOOP-Shield-v226.apk`
- package: `com.boop.shieldoverlay`
- version: 226 / `1.2.226-shield`
- bytes: 160485741
- SHA-256: `eb52fefacfec25c5ce8173c6b6466f14fdabfa35d8175c131b7d2f016a62f0ee`
- permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Artifact ZIP SHA-256: `55c53470a621c5956e63bb3e440503bd3e5dd031634626a86db3faf6151c66c1`.

Focused hard-mask presentation checks, inherited v206 checks, split materialization/integration, HA tests, both builds and packaged signer/native/art checks passed. All 16 native libraries remain baseline-identical.

Wall remains v207.
