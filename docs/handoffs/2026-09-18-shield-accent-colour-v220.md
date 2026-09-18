# Shield v220 launcher accent-colour checkpoint

Date: 2026-09-18

Branch: `boop-shield-accent-colour-v220`  
Owning split branch: `boop-wall-shield-split-v207`  
Shield package: `com.boop.shieldoverlay`  
Shield version: `220` / `1.2.220-shield`  
Wall remains: v207

## Request

After accepting v219's Now Playing alignment as perfect, Ryan asked for a fun launcher customisation:
- allow the user to replace BOOP's cyan/blue highlights with any hue;
- use a slider;
- place it in Launcher Settings directly below Smart home panel.

## Implementation

### Setting and persistence

`ShieldHomeStore` stores `accent_hue_v1` as an integer hue from 0 to 359.

Default hue is 204. `BoopTvChrome.colorForHue(204)` deliberately returns the exact previous BOOP cyan `#4DB8FF`, preserving appearance for users who never touch the control.

### Launcher Settings UI

`ShieldHomeSettingsView` now places **Highlight colour** immediately below **Smart home panel**.

The hue slider:
- is focusable with the TV remote;
- covers 0-359 degrees;
- uses a rainbow spectrum track;
- previews the selected hue in the label and thumb;
- saves user changes immediately through the activity callback.

### Shared accent propagation

`BoopTvChrome.accentColor(Context)` now resolves the persisted hue and remains the canonical accent source.

v220 explicitly routes these launcher accents through it:
- standard TV focus borders via `FocusChrome`;
- artist text-only focus;
- Now Playing progress bar;
- HA active-state text and device vectors;
- Add favourites;
- HOME navigation vector icons, runtime-tinted instead of relying on baked cyan;
- weather current icon, wind and rain details.

Charcoal fills, black backgrounds, white text, v217 reorder geometry, HA control paths and v219 Now Playing geometry are unchanged.

## Verification

Verified source: `a009b921bf23d018f7edc9ebf2c64a9f89bf8ddd`.

GitHub Actions:
- successful run: `35355454656`
- job: `105634013385`
- artifact: `10552105353`
- artifact name: `BOOP-Shield-v220-Wall-v207-Signed`
- uploaded ZIP SHA-256: `1a6fa09706d06b2bba52becc4123f7640fee15068147fd3b8b38288a11d4cdf1`

Shield APK:
- file: `BOOP-Shield-v220.apk`
- bytes: `160485741`
- SHA-256: `2ac14d16983a7662b09f5338e18f7f43b749cea0666e4af676d86b8e087d34ad`
- signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- all 16 native libraries remain byte-identical to the accepted baseline.

CI passed:
- 77 initial focused checks;
- inherited v206 verification;
- split materialization/integration;
- 100 materialized integration checks;
- HA room/latency tests;
- both APK builds;
- signer/native/art verification.

A prior v220 build source also passed, but the final verified source above includes the review-only line-ending cleanup and the accent test in the post-materialization test set.

## Physical acceptance

Pending Ryan's Shield test:
- Highlight colour appears immediately under Smart home panel;
- remote slider feels usable;
- chosen colour propagates through launcher highlights;
- hue survives restart;
- accepted v217 reordering and v219 alignment remain unchanged.
