# Shield v221: forecast optical centring and first-favourite return focus

Date: 2026-09-20
Branch: `boop-shield-weather-focus-v221`
Verified build source: `9bf329a24631310261843b654f9fe0716a59aa2e`

## Requested physical correction

Ryan supplied a Shield screenshot showing the forecast text still reading slightly off-centre relative to each sun/weather glyph. He requested the weather icon's visual centre as the anchor for time, temperature and rain percentage. The 3-day block also needed its visible whitespace balanced between the middle divider and the outer right card border.

Ryan also requested Favourite entry 1 whenever the launcher is regained from an app, including Shield task-manager returns and the Close media action.

## Weather alignment

The accepted weather glyph correction remains +2dp. v221 applies the same translation to every related forecast line, so labels/time, temperature or high-low and rain percentage share the glyph's optical centre.

The 3-day forecast container receives a +9dp translation. The weather card has an 18dp outer horizontal inset, so half that inset balances the visible right-panel centre from its divider to the card's outer border without adding per-cell padding.

The 3:4:3 divider weighting, headers, footer, current-weather region, Now Playing geometry and card dimensions are otherwise unchanged.

## Return focus

`ShieldLauncherActivity` now:
- rebuilds HOME with first-favourite focus for new HOME intents;
- requests first-favourite focus on resume when HOME is active, covering ordinary app and Shield task-manager returns;
- requests first-favourite focus after Close media;
- retains the existing short-Back first-favourite rule.

## Verification

Successful GitHub Actions run `35515284079`, job `106090068972`.
Artifact `10606343049`: `BOOP-Shield-v221-Wall-v207-Signed`.

Shield:
- file: `BOOP-Shield-v221.apk`
- package: `com.boop.shieldoverlay`
- version: 221 / `1.2.221-shield`
- bytes: 160485741
- SHA-256: `d0e5111efd97dc948890b2181e3212ce20e67b4e43cadc0df900a06969295f8a`
- permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Artifact ZIP SHA-256: `20a5fbf892d2139d4e45239515bfcee0f2f2773af4c86f0543c42440e2dc664c`.

Focused tests, inherited v206 checks, split materialization/integration, HA tests, both builds and packaged signer/native/art checks all passed. All 16 native libraries remain baseline-identical.

Wall remains v207. v219 Now Playing alignment and v220 accent-colour behaviour remain untouched.

## Physical acceptance

Ryan will perform visual and ADB validation. No automated visual or device control was used.
