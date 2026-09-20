# Shield v223: two-pixel hourly temperature nudge

Date: 2026-09-20
Branch: `boop-shield-weather-focus-v221`
Verified build source: `fceb5659a5fe52d01205641956b834ff5615385e`

## Request

Move only the four hourly temperature values under **Next 4 hours** 2 more physical pixels right than v222.

## Implementation

The accepted +2dp weather stack remains unchanged. Only the hourly temperature TextView changed:

`temp.setTranslationX(dp(2)+1f)` -> `temp.setTranslationX(dp(2)+3f)`

No other weather line or launcher geometry changed.

## Verification

Successful GitHub Actions run `35517619079`, job `106096104834`.
Artifact `10607337069`: `BOOP-Shield-v223-Wall-v207-Signed`.

Shield:
- file: `BOOP-Shield-v223.apk`
- package: `com.boop.shieldoverlay`
- version: 223 / `1.2.223-shield`
- bytes: 160485741
- SHA-256: `2bc6179110d22f3a76243e1914f8f19db7573e74694c5e85e8aa40493d7340d4`
- permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Artifact ZIP SHA-256: `05e542e8c4e090792781dd24d3f53fff9e33ce4c3a85b2fb65b3fdfee5f5c3bd`.

Focused checks, inherited v206 checks, split materialization/integration, HA tests, both builds and packaged signer/native/art checks passed. All 16 native libraries remain baseline-identical.

Wall remains v207.
