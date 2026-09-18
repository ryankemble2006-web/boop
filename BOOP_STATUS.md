# BOOP status

Updated 2026-09-18. Owner: `boop-wall-shield-split-v207`. Current Shield implementation: `boop-shield-ha-fast-v214`.

Shield v214 / `1.2.214-shield` is signed and ready for Ryan's physical test.

The v213 smart-home panel was functionally correct in CI but its real-device click path was over-defensive: each press reloaded room membership/state and then created another state subscription before accepting success. v214 keeps the existing room/generation safety gates but sends the Home Assistant `call_service` action immediately over the existing WebSocket. The session's one long-lived `state_changed` subscription remains the source of truth for displayed device state. This fast path applies to the supported binary room controls: lights, switches/plugs, fans and input booleans. No optimistic success state is painted.

Weather layout is also corrected without changing the previously accepted card chrome or data source. Current, hourly and daily sections now share one header grid and vertically aligned bodies; the footer is consistently centred.

Build source `2d07d4307a89dec735b93bdc08a5f6956f5e5ae2`.
Successful run `35343966769`, job `105596091769`.
Artifact `10545697780`, `BOOP-Shield-v214-Wall-v207-Signed`.
Shield file `BOOP-Shield-v214.apk`, 160485741 bytes, SHA-256 `28a7f0686308c1907039bdfe46462a08bf6b24593d3b29ccbd90a3879d96f195`.
Permanent signer SHA-256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Downloaded artifact ZIP SHA-256 `5abca90da2e32b07ceab0f942bcebff0ed3aac77e17c58ebeec2b3bdeb1902ee`.

Verification passed: focused room-panel/weather tests, inherited v206 checks, materialized split integration, dedicated HA low-latency tests, both app builds, and packaged identity/certificate/native/art checks. The downloaded artifact receipt and extracted APK were independently matched; all 16 native libraries remain baseline-identical. The copied HA unit XML reports 15 tests, zero failures/errors.

Manual Shield installation and live acceptance remain PENDING. No automatic install, emulator, screenshot, device driving, permission change, voice/audio change, phone change or Wall feature change occurred. v213 remains the previous signed checkpoint.
