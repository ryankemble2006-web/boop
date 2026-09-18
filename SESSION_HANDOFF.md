# BOOP current handoff

Updated 2026-09-18. Owner branch: `boop-wall-shield-split-v207`. Current Shield implementation branch: `boop-shield-ha-fast-v214`.

## Current Shield: v214, signed and ready for Ryan's physical test

Package `com.boop.shieldoverlay`, version `214` / `1.2.214-shield`.

v214 starts exactly from the signed/documented v213 branch tip `1fddf88050f004860c98e50860e1780e82415a86`. It fixes the first physical-test findings from the new room panel: Home Assistant buttons could hang or feel slow, especially the fan, while the existing phone voice path remained immediate; the idle weather content also needed proper vertical alignment.

### Home Assistant latency fix

The v213 Shield click path unnecessarily refreshed the entire room membership before every action: `extract_from_target`, device registry, entity registry, then `get_states`. It then created a second per-click `state_changed` subscription and waited up to ten seconds for an exact expected state. The session already owned a long-lived state stream.

v214 removes both delays. A valid rendered room tile is checked against the current selected room and cached room-scoped card, then sends `call_service` immediately over the already-open Home Assistant WebSocket. The service result clears the pending press; the existing long-lived `state_changed` stream remains authoritative for the displayed real state. No optimistic on/off state is invented. Duplicate presses while the action is pending, stale generations, room changes and unavailable entities remain guarded. The normal 30-second background room refresh remains for membership reconciliation.

This follows Home Assistant's current WebSocket API: service calls are sent with `call_service`; clients interested in resulting entity changes listen to `state_changed` events. It applies to the panel's supported binary controls: lights, switches/plugs, fans and input booleans.

### Weather alignment fix

The accepted weather chrome remains unchanged: 182dp hero slot, RGB 16/16/16 fill, 14dp corners, RGB 48/48/48 stroke, 3:4:3 column widths, Open-Meteo data, non-focusable behaviour and Now Playing priority.

v214 gives all three weather sections one fixed-height header grid, vertically centres their bodies, moves the current-condition label into the current body so it no longer pushes that column down, and centres the footer content consistently. Weather/network behaviour and caching were not changed.

## Verified signed artifact

Production/build source: `2d07d4307a89dec735b93bdc08a5f6956f5e5ae2`.
Successful GitHub Actions run `35343966769`, job `105596091769`.
Artifact `10545697780`: `BOOP-Shield-v214-Wall-v207-Signed`.

Deliver **BOOP-Shield-v214.apk**, 160485741 bytes.
Shield APK SHA-256: `28a7f0686308c1907039bdfe46462a08bf6b24593d3b29ccbd90a3879d96f195`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Downloaded artifact ZIP SHA-256: `5abca90da2e32b07ceab0f942bcebff0ed3aac77e17c58ebeec2b3bdeb1902ee`.

The focused room-panel/weather gate passed, inherited v206 checks passed, materialized split integration passed, the dedicated HA low-latency unit suite passed, both apps built with the unchanged permanent signer, and actual APK identity/certificate/native/art verification passed. The downloaded artifact was unpacked independently: its receipt names source `2d07d430...`; the extracted Shield APK hash matches the receipt; all 16 native-library hashes match the accepted baseline. HA unit XML contains 15 tests total across the four copied suites, with zero failures/errors.

## Physical acceptance still pending

Ryan should install only `BOOP-Shield-v214.apk` on the Shield and check:
1. Fan/light/switch button response feels immediate and no tile remains hung.
2. Real tile state follows the actual Home Assistant state after each action.
3. Repeated presses are safely blocked only while the service acknowledgement is pending.
4. Weather current / next-hours / three-day columns and footer look aligned on the real TV.

No device was driven, no emulator or screenshot was used, no permissions were changed, and no phone/Wall code, voice/audio work or signing material was altered. Wall remains v207 for compatibility only.

v213 remains the prior signed checkpoint. Detailed v214 record: `docs/handoffs/2026-09-18-shield-ha-fast-weather-v214.md`.
