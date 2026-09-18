# BOOP current handoff

Updated 2026-09-18. Owner branch: `boop-wall-shield-split-v207`. Current Shield implementation branch: `boop-shield-accent-colour-v220`.

## Current Shield: v220, signed and ready for Ryan's physical test

Package `com.boop.shieldoverlay`, version `220` / `1.2.220-shield`.

Ryan confirmed v219's Now Playing alignment is **absolutely perfect**. Do not alter that geometry unless explicitly requested.

### User-selectable highlight colour

v220 adds **Highlight colour** immediately below **Smart home panel** in Launcher Settings.

The control is a single hue slider:
- range 0-359;
- current BOOP cyan is the default at hue 204;
- hue 204 deliberately returns the exact legacy `#4DB8FF` colour;
- the slider label and thumb preview the hue while Ryan moves it;
- changes persist immediately in `ShieldHomeStore`.

The saved accent is resolved through `BoopTvChrome.accentColor(Context)`. Existing `FocusChrome` consumers therefore inherit the selected colour without separate per-screen settings.

Explicitly covered in v220:
- TV focus outlines;
- text-only artist focus;
- Now Playing progress accent;
- HA active state text and device icons;
- Add favourites accent;
- HOME navigation icons, now runtime-tinted;
- weather current icon, wind and rain accents.

The charcoal/black/white visual language is unchanged. This is accent replacement only.

### Accepted behavior retained

v217 favourite/HA hold-to-reorder remains accepted and unchanged:
- hold lifts;
- left/right reorders;
- OK drops;
- HA order persists per room;
- normal HA toggles stay on the low-latency path.

v219 Now Playing remains accepted and unchanged:
- transport row -4dp visual correction;
- progress right margin 8dp;
- Lyrics/Close player, title/artist/Playing, artwork and eyes remain where accepted.

## Verified signed artifact

Production/build source: `a009b921bf23d018f7edc9ebf2c64a9f89bf8ddd`.
Successful GitHub Actions run `35355454656`, job `105634013385`.
Artifact `10552105353`: `BOOP-Shield-v220-Wall-v207-Signed`.

Deliver **BOOP-Shield-v220.apk**, 160485741 bytes.
Shield APK SHA-256: `2ac14d16983a7662b09f5338e18f7f43b749cea0666e4af676d86b8e087d34ad`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Uploaded artifact ZIP SHA-256: `1a6fa09706d06b2bba52becc4123f7640fee15068147fd3b8b38288a11d4cdf1`.

Verification passed: 77 initial focused checks, inherited v206 regression, materialized split integration with 100 checks, HA room/latency unit tests, both app builds, and actual APK identity/certificate/native/art checks. All 16 native libraries remain baseline-identical.

## Physical acceptance pending

Install `BOOP-Shield-v220.apk` and check:
1. Launcher Settings shows Highlight colour directly below Smart home panel.
2. Slider is D-pad focusable and moves smoothly through colours.
3. Leaving settings for HOME applies the selected colour to nav icons, focus outlines, Now Playing progress and HA accents.
4. Weather accent details follow the same colour when weather is visible.
5. Returning/restarting preserves the selected hue.
6. v217 movement/customisation and v219 Now Playing alignment remain unchanged.

Wall remains v207. v219 is the prior accepted signed checkpoint.

Detailed v220 record: `docs/handoffs/2026-09-18-shield-accent-colour-v220.md`.
