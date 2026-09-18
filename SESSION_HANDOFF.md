# BOOP current handoff

Updated 2026-09-18. Owner branch: `boop-wall-shield-split-v207`. Current Shield implementation branch: `boop-shield-weather-icon-fan-v216`.

## Current Shield: v216, signed and ready for Ryan's physical test

Package `com.boop.shieldoverlay`, version `216` / `1.2.216-shield`.

v216 starts from the verified v215 lineage and addresses Ryan's next physical screenshot/test findings:
- weather text/icons were still visually offset inside the 3:4:3 divider regions, especially temperatures/rain percentages under weather glyphs;
- fan control became much quicker in v215 but still lagged behind the instant phone command;
- HA device tiles needed semantic icons, specifically a fan symbol for the fan even when its working power entity is a switch, and a subwoofer symbol for the sub.

### Exact weather centring

The screenshot showed that weighted cells and centred LinearLayouts were not enough because each weather TextView was still wrap-content. Different glyph/text widths and emoji side bearings could therefore make a cloud and the temperature beneath it appear on different horizontal centres even inside the same cell.

v216 makes each hourly/daily line consume the full cell width and applies explicit centre gravity plus centre text alignment. All three section boxes now have zero horizontal inset so the visible divider geometry is the sole horizontal coordinate system. Weather glyphs receive a small +2dp optical correction for the font's glyph side bearing, while numeric/text rows remain mathematically centred. Current/hourly/daily headings remain centred, and the footer retains matching 3:4:3 centred regions.

Card chrome/data behaviour is unchanged: 182dp hero slot, RGB 16/16/16 fill, 14dp corners, RGB 48/48/48 stroke, keyless Open-Meteo, cache/refresh policy, non-focusable weather and Now Playing priority.

### Fan path now follows Home Assistant intent resolution

v214's low-latency direct WebSocket path remains the default for already-proven controls such as Sonoff and lights. v215's capability-aware fan entity selection also remains as a fallback.

For a semantic fan tile only, v216 first sends Home Assistant's WebSocket `conversation/process` command using an explicit room-scoped intent such as `turn off Govee Fan in Living Room`. This intentionally asks Home Assistant to resolve the same fan target through its own Assist/conversation routing that is already proven to work from the phone. If conversation processing is unavailable or does not return `action_done`, BOOP immediately falls back to the v215 direct `call_service` route. Displayed state still comes from the existing long-lived `state_changed` stream; BOOP does not invent optimistic state.

This route is generic for semantic fan devices, not Govee-specific. Non-fan Sonoff/light controls continue on the direct service path and were not slowed by this change.

### Semantic HA icons

The room panel now chooses icons from physical device identity, not merely the selected HA entity domain:
- names/device IDs identifying a fan render the existing fan-blade vector even if BOOP selected a `switch.*` power entity;
- names/device IDs identifying `subwoofer` or `sub` render a new speaker cabinet/woofer vector;
- lights remain bulbs;
- other switches retain the power symbol.

## Verified signed artifact

Production/build source: `e60a3521524e1ccdb19fcac73f4ad4c3033bf618`.
Successful GitHub Actions run `35347668181`, job `105607970217`.
Artifact `10547503847`: `BOOP-Shield-v216-Wall-v207-Signed`.

Deliver **BOOP-Shield-v216.apk**, 160485741 bytes.
Shield APK SHA-256: `a81dfd6f143ebe32952e48924184b52325644ed64b8ed217443cb1a6dd460004`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Downloaded artifact ZIP SHA-256: `3cfaf36dd7936ecf4a57d5c5c57ffa16d0d2347837fa388020d13e13f23fa293`.

Verification passed: focused room/weather/source checks, inherited v206 regression, materialized split integration, HA room/latency/fan-conversation unit tests, both app builds, and actual APK identity/certificate/native/art checks. Independent extraction matched the CI receipt and Shield APK hash. All 16 native libraries remain baseline-identical. Four copied HA XML suites report 20 tests, zero failures/errors/skips.

### Superseded v216 runs

- `c134eb8a81d9b062bd88bd858d130459cc920983` / run `35347165456`: 71 focused checks passed and one weather invariant correctly caught the remaining current-section 12dp inset. No APK built.
- `d44a2f23ac854f058b3e381e07b4b9db9656ee5d` / run `35347246526`: first attempt reached inherited checks and was interrupted by repeated external HTTP 504s downloading an unchanged wake dependency. The rerun passed inherited checks, then caught a missing `throws Exception` on a new JSON-building unit test. No APK built.
- `e60a3521524e1ccdb19fcac73f4ad4c3033bf618` fixes only that test declaration and is the verified v216 install source above.

## Physical acceptance still pending

Ryan should install only `BOOP-Shield-v216.apk` and check:
1. Next-4-hours cloud/rain/temperature rows visually share one centre per cell.
2. Three-day labels/icons/high-low/rain rows are centred between the outer edge and divider lines.
3. Fan response now matches or closely approaches BOOP's phone command.
4. Sonoff and speaker light remain instant.
5. Fan tile shows a fan-blade icon and the subwoofer tile shows a woofer/speaker icon.

No automatic device install, emulator, permission change, phone/Wall feature change, voice/audio edit or signer change occurred. Wall remains v207. v215 remains the prior signed checkpoint.

Detailed v216 record: `docs/handoffs/2026-09-18-shield-weather-icons-fan-v216.md`.
