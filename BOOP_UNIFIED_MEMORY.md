# BOOP durable project memory

Updated 2026-09-18.

The consumer apps remain split shells around shared BOOP code: Wall `com.boop.alpha1` stays on v207; Shield `com.boop.shieldoverlay` is now v214 on `boop-shield-ha-fast-v214`. The owning split branch remains `boop-wall-shield-split-v207`.

## Shield smart-home panel

The native charcoal/cyan lower Home panel uses the existing **Set this device room** choice and existing Home Assistant pairing. It expands into BOOP's lower-right space when BOOP is absent and contracts when he returns without moving the hero/favourites. Home settings has **Smart home panel: ON / OFF**, default ON. D-pad Down enters from favourites, Left/Right stop at row ends, Up returns to the prior favourite. Supported controls remain physical lights, switches/smart plugs, fans and input booleans. Sensors/cameras/thermostat detail remain future presentation work.

### Low-latency rule locked in from v214

Do not put full room discovery or a fresh state subscription in the user-action critical path.

The accepted architecture is:
- room discovery/registry/state loading happens on connection and background refresh;
- a button press validates current generation, selected room and the already room-scoped cached card locally;
- send Home Assistant `call_service` immediately over the existing WebSocket;
- use the service result only as action acknowledgement;
- use the one long-lived `state_changed` stream as authoritative real device state;
- do not invent optimistic on/off state;
- continue blocking duplicate pending presses and rejecting stale room/generation actions.

Reason: v213 did `extract_from_target` + device registry + entity registry + `get_states` before every press, then created a second per-action state subscription with a ten-second timeout. On the real Shield the fan could appear hung while the phone's established HA path acted immediately. v214 removes that latency while retaining room safety.

## Weather alignment

Keep the accepted weather surface: 182dp hero slot, RGB 16/16/16, 14dp corners, RGB 48/48/48 stroke, 3:4:3 column widths, keyless Open-Meteo, 30-minute refresh/cache, non-focusable weather and Now Playing priority.

From v214, current/hourly/daily sections share one fixed-height header grid and vertically centred bodies. The current-condition label belongs in the current body rather than creating an extra header line. Footer wind, sun and update/source text are vertically aligned; the middle sun group is centred.

## Latest verified artifact

Build source `2d07d4307a89dec735b93bdc08a5f6956f5e5ae2`.
Run `35343966769`, job `105596091769`; artifact `10545697780`, `BOOP-Shield-v214-Wall-v207-Signed`.
Deliver `BOOP-Shield-v214.apk`, 160485741 bytes, SHA-256 `28a7f0686308c1907039bdfe46462a08bf6b24593d3b29ccbd90a3879d96f195`.
Permanent certificate `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` unchanged.
Downloaded ZIP SHA-256 `5abca90da2e32b07ceab0f942bcebff0ed3aac77e17c58ebeec2b3bdeb1902ee`.

CI passed focused room/weather checks, inherited regression checks, materialized integration, dedicated low-latency HA tests, both builds and actual APK integrity verification. Independent artifact inspection matched the receipt and all 16 baseline native libraries. Physical Shield acceptance is still Ryan's manual step.

## Preserved history and boundaries

v209 fixed native Close player / Close media after the split. v210 introduced idle weather. v211 added Shield INTERNET and the accepted weather chrome. v212 repaired zero-height weather columns. v213 added the adaptive room panel. v214 is the first latency/alignment correction from real v213 testing.

Voice/provider/pitch work remains deferred and untouched. Wall remains v207 and should not be installed for this Shield-only change. No RDC/device driving, daily Pixel access, permissions or signer changes were made. The retired root AGENTS/START_HERE/CONTEXT/RULES/BUILD_ON_GITHUB experiment remains retired; do not restore it automatically.
