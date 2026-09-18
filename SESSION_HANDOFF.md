# BOOP current handoff

Updated 2026-09-18. Owner branch: `boop-wall-shield-split-v207`. Current Shield implementation branch: `boop-shield-fan-capability-v215`.

## Current Shield: v215, signed and ready for Ryan's physical test

Package `com.boop.shieldoverlay`, version `215` / `1.2.215-shield`.

v215 starts from the documented v214 branch and responds to Ryan's physical v214 findings:
- weather was much better, but all content should be centred according to the vertical divider boxes;
- Sonoff subwoofer and the Govee right-speaker light responded instantly through the new direct HA path;
- the Govee fan still did not respond from its Shield tile, while BOOP on the phone controlled that fan immediately.

### Fan capability fix

The v214 low-latency WebSocket path remains unchanged. The fault was narrowed to entity selection for multi-entity fan devices.

BOOP Home previously preferred any `fan.*` entity over switch entities on the same physical device. Current Home Assistant fan entities explicitly advertise power capabilities in `supported_features`: `TURN_OFF = 16`, `TURN_ON = 32`. A known fan feature mask that does not contain both flags must not be selected as BOOP's simple power control.

v215 now reads `supported_features` from the HA state snapshot and carries it with the room card. For a fan device:
- a native fan entity with both power flags (`48`) remains the preferred control;
- a known fan entity without both flags is not exposed as a simple on/off tile;
- BOOP falls back to another controllable entity on the same device;
- when several switches exist, an entity/name containing `power` or `on/off` outranks settings such as oscillation;
- unknown/legacy feature metadata preserves prior behaviour rather than assuming failure.

There is no Govee brand special case. This capability rule applies to any Home Assistant fan device with a partial/decorative fan entity and a separate working power switch.

The v214 direct command architecture stays locked: validate the current room/card locally, send `call_service` immediately over the existing WebSocket, clear pending state from the service acknowledgement, and let the one long-lived `state_changed` stream remain authoritative for displayed real state. Sonoff/light behaviour is therefore not routed through a new path.

### Weather centring fix

The accepted card chrome/data behaviour remains unchanged: 182dp hero slot, RGB 16/16/16 fill, 14dp corners, RGB 48/48/48 stroke, 3:4:3 divider widths, keyless Open-Meteo, cache/refresh behaviour, non-focusable weather and Now Playing priority.

v215 centres against the divider geometry:
- current/hourly/daily headings are centred within their own 3:4:3 boxes;
- all three boxes use symmetric 12dp internal horizontal padding so their visual centre does not drift;
- the current icon + temperature/condition group is centred as one unit;
- hourly and daily cells remain centred;
- footer groups now use the same 3:4:3 widths as the boxes above, with wind, sun and source/update text centred inside their corresponding regions.

## Verified signed artifact

Production/build source: `376bb700f2aedc9ffdd659c036bd1abfb14a9948`.
Successful GitHub Actions run `35345512061`, job `105601126131`.
Artifact `10546950922`: `BOOP-Shield-v215-Wall-v207-Signed`.

Deliver **BOOP-Shield-v215.apk**, 160485741 bytes.
Shield APK SHA-256: `6297d060874ab7b06fc61f4b29f8e3179b40528cd53377681c6f9811ff01ffc0`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Downloaded artifact ZIP SHA-256: `b39b83515876efe3508419b326d22c7e4f84ba44eb9d39996ac4e39117e95c2f`.

Verification passed: focused room/weather gate, inherited v206 regression checks, materialized split integration, HA room/command/capability unit tests, both app builds, and actual APK identity/certificate/native/art verification. Independent extraction matched the CI receipt and APK hash. All 16 native libraries match the accepted baseline. The four copied HA XML suites report 18 tests, zero failures/errors/skips.

An earlier superseded v215 commit `4b5ab35dccbf72c53528025129af862edc6a2846` had a constructor-delegation compile typo caught immediately; run `35345462718` failed at the first focused gate before any APK build. The corrected source above is the only v215 artifact to install.

## Physical acceptance still pending

Ryan should install only `BOOP-Shield-v215.apk` and check:
1. Fan tile now controls the actual Govee fan immediately.
2. Sonoff and right-speaker Govee light remain instant.
3. Fan tile state follows Home Assistant's actual state.
4. Weather headings, current block, forecast cells and footer look centred between the visible divider lines.

No automatic device install, emulator, screenshot, permission change, phone/Wall feature change, voice/audio edit or signer change occurred. Wall remains v207.

v214 remains the prior signed checkpoint. Detailed v215 record: `docs/handoffs/2026-09-18-shield-fan-capability-weather-v215.md`.
