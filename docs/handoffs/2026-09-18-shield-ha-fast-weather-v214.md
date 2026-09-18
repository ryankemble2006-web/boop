# Shield v214: immediate Home Assistant controls and weather alignment

Date: 2026-09-18
Branch: `boop-shield-ha-fast-v214`
Parent: v213 tip `1fddf88050f004860c98e50860e1780e82415a86`
Production/build source: `2d07d4307a89dec735b93bdc08a5f6956f5e5ae2`

## Reported physical symptoms

Ryan's first v213 Shield test found two issues:
- the new Home Assistant fan control could do nothing visibly and remain hung, while saying “fan off” to BOOP on the phone changed it immediately;
- weather data was present but the current/hourly/daily content needed proper alignment.

## Root cause and correction

The Shield panel did substantially more work than the existing direct phone path. Every click first called `loadDashboard`, which performed target extraction, device registry loading, entity registry loading and a complete state fetch. Only after that did it send the service call. `toggleBinary` then created another state subscription for that one click and waited for the exact expected state, with a ten-second confirmation timeout.

That sequence was not needed because `RoomPanelSession` already keeps a long-lived WebSocket and `state_changed` subscription while the panel is active.

v214 keeps the local safety checks and moves network discovery off the critical user-action path. A click now validates the rendered generation, current selected room and cached room-scoped entity, then immediately sends `call_service`. The service result clears pending/error state. Actual displayed device state remains driven by the existing session-wide `state_changed` stream. The 30-second background refresh remains for membership reconciliation.

This matches the current Home Assistant WebSocket API guidance: `call_service` is the service-action command, and clients interested in entity changes listen for `state_changed` events.

## Weather

The card's approved dimensions/colour/stroke and 3:4:3 column proportions are preserved. All three sections now use the same 26dp header slot and vertically centred body. The current condition no longer occupies a second header row. Footer groups have explicit start/centre/end vertical alignment.

## Verification and signed delivery

GitHub Actions run `35343966769`, job `105596091769`: SUCCESS.
Artifact `10545697780`: `BOOP-Shield-v214-Wall-v207-Signed`.

Shield:
- file: `BOOP-Shield-v214.apk`
- package: `com.boop.shieldoverlay`
- version: 214 / `1.2.214-shield`
- bytes: 160485741
- SHA-256: `28a7f0686308c1907039bdfe46462a08bf6b24593d3b29ccbd90a3879d96f195`
- permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Downloaded artifact ZIP SHA-256: `5abca90da2e32b07ceab0f942bcebff0ed3aac77e17c58ebeec2b3bdeb1902ee`.

The run passed the focused room/weather gate, inherited v206 checks, materialized split integration, dedicated HA unit tests, both app builds and actual APK verification. Independent extraction confirmed the receipt's source/hash and all 16 baseline-identical native libraries. Four HA XML suites contain 15 tests total, zero failures/errors.

## Acceptance boundary

No emulator, screenshot, automatic installation, ADB/device driving, permission change, phone/Wall feature change, voice/audio edit or signer replacement was performed. Ryan's real Shield test remains authoritative for perceived button latency and final weather geometry.
