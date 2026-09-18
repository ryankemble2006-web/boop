# Shield v216: exact weather centring, semantic HA icons and fan intent routing

Date: 2026-09-18
Branch: `boop-shield-weather-icon-fan-v216`
Verified build source: `e60a3521524e1ccdb19fcac73f4ad4c3033bf618`

## Physical evidence

Ryan supplied a Shield screenshot after v215. It showed:
- under Next 4 hours, the rows still read visually to the right within their cells;
- temperatures/rain percentages did not look centred under cloud/weather glyphs;
- the same issue existed in the 3-day section relative to inner/outer divider lines;
- fan response was much quicker than before but still delayed compared with BOOP on the phone;
- the fan tile displayed a generic power icon because v215 correctly selected a switch power entity;
- the subwoofer also needed a physical-device-specific icon.

## Weather correction

The remaining issue was inside the forecast cells. The LinearLayout cells were centred, but their child TextViews were wrap-content. Emoji/weather font side bearings plus different text widths therefore produced optical drift even though the parent cell was mathematically centred.

v216 removes horizontal insets from all three main weather regions and makes every hourly/day row MATCH_PARENT within its own weighted cell. Each line receives explicit centre gravity and centre text alignment. Weather glyph lines get a +2dp optical correction, while times, temperatures, high/low and rain rows stay at the exact mathematical cell centre. Headings and footer remain centred against the same 3:4:3 divider geometry.

## Fan route

The v214/v215 direct transport remains proven for Sonoff and lights and is unchanged for non-fan devices.

For semantic fan tiles, v216 first sends WebSocket `conversation/process` with a room-scoped command derived from the tile's real state and physical name. This asks Home Assistant's own Assist/conversation resolver to choose the control path, matching the successful phone behaviour. BOOP accepts only `action_done` with no failed targets. If that route is unavailable or does not act, BOOP immediately falls back to v215's capability-aware direct entity `call_service`.

Live state remains driven by the existing `state_changed` subscription.

## Semantic icons

The room panel classifies icon identity from display name, device name and entity ID. Fan tiles render the existing fan-blade vector regardless of whether the backing control is `fan.*` or `switch.*`. Sub/subwoofer devices render a new speaker cabinet with tweeter/woofer circles. Light and generic switch vectors remain unchanged.

## Verification

Successful GitHub Actions run `35347668181`, job `105607970217`.
Artifact `10547503847`: `BOOP-Shield-v216-Wall-v207-Signed`.

Shield:
- file: `BOOP-Shield-v216.apk`
- package: `com.boop.shieldoverlay`
- version: 216 / `1.2.216-shield`
- bytes: 160485741
- SHA-256: `a81dfd6f143ebe32952e48924184b52325644ed64b8ed217443cb1a6dd460004`
- permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Downloaded artifact ZIP SHA-256: `3cfaf36dd7936ecf4a57d5c5c57ffa16d0d2347837fa388020d13e13f23fa293`.

Focused/source tests, inherited v206 checks, split materialization/integration, HA tests, both app builds and actual APK integrity checks passed. Four HA suites contain 20 tests total, zero failures/errors/skips. Independent extraction matched the receipt and APK SHA; all 16 native libraries match the accepted baseline.

## Earlier v216 failures

- Initial production commit `c134eb8a81d9b062bd88bd858d130459cc920983`: focused tests deliberately caught one remaining 12dp current-section inset (71 passed, one failed). No APK.
- Commit `d44a2f23ac854f058b3e381e07b4b9db9656ee5d`: first run attempt was interrupted by repeated external HTTP 504s fetching an unchanged wake dependency. Rerun passed inherited checks and then caught a missing checked-exception declaration in a new unit test. No APK.
- `e60a3521524e1ccdb19fcac73f4ad4c3033bf618`: fixed the test signature only and passed every gate; this is the install source.

## Physical acceptance

Install only the verified v216 APK. Confirm forecast rows line up visually with glyphs/dividers, fan response approaches the phone command, Sonoff/light remain instant, and fan/subwoofer use their semantic icons.
