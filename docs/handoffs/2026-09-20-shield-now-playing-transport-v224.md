# Shield v224: simplified Now Playing transport

Date: 2026-09-20
Branch: `boop-shield-weather-focus-v221`
Verified build source: `16fbfdc632d821d7efd3911823a2035f3b8904b9`

## Request

Remove Rew and Fwd from Now Playing because they are always disabled for Deezer. Keep Prev exactly where it is, then bunch the three remaining controls using the existing gap.

## Implementation

Transport is now:
- Prev
- Play/Pause
- Next

The row remains left-anchored with:
`controls.setTranslationX(-dp(4))`

The existing:
`CONTROL_GAP_DP = 10`
is unchanged.

Therefore Prev does not move. Pause and Next simply collapse left into the removed button slots.

Progress bar ±10-second seeking is unchanged.

## Verification

Successful GitHub Actions run `35518326403`, job `106097941527`.
Artifact `10606879001`: `BOOP-Shield-v224-Wall-v207-Signed`.

Shield:
- file: `BOOP-Shield-v224.apk`
- package: `com.boop.shieldoverlay`
- version: 224 / `1.2.224-shield`
- bytes: 160485737
- SHA-256: `ead4ecb9de285499d46d44dca49d3ed24f6322b9e590e1c956dfd9c9a4514a28`
- permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Artifact ZIP SHA-256: `caaa3f168f5a6fef4713c32d4b043111a43042d93f29117a68c966add9293778`.

Focused checks, inherited v206 checks, split materialization/integration, HA tests, both builds and packaged signer/native/art checks passed. All 16 native libraries remain baseline-identical.

Wall remains v207.
