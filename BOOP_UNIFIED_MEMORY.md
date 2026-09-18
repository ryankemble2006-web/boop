# BOOP durable project memory

Updated 2026-09-18.

The consumer apps remain split shells around shared BOOP code: Wall `com.boop.alpha1` stays v207; Shield `com.boop.shieldoverlay` is v215 on `boop-shield-fan-capability-v215`. The owning split branch remains `boop-wall-shield-split-v207`.

## Shield room-panel control rules

The charcoal/cyan Home panel still uses the existing **Set this device room** and existing HA pairing. The accepted v214 latency architecture remains locked:
- room discovery/registry/state loading happens on connect/background refresh, not before every press;
- validate current generation, selected room and cached room-scoped card locally;
- send `call_service` immediately over the existing WebSocket;
- service result is acknowledgement only;
- the one long-lived `state_changed` stream is authoritative for real displayed state;
- do not invent optimistic on/off state;
- reject stale room/generation actions and duplicate pending presses.

Ryan's v214 physical test proved Sonoff subwoofer and Govee right-speaker light were instant, while the Govee fan tile still did nothing. Because phone BOOP controlled that fan immediately, treat this as an entity-selection/capability problem unless new evidence shows otherwise.

### Fan capability rule from v215

Do not blindly prefer a `fan.*` entity simply because it exists.

BOOP now carries HA state `supported_features` with each room card. Current Home Assistant fan power flags are TURN_OFF = 16 and TURN_ON = 32. When a feature mask is known, a native fan entity must contain both bits (48) to qualify as BOOP's simple on/off device control.

If it does not:
- exclude that fan entity from simple binary control;
- consider other actionable entities on the same physical device;
- prefer a switch whose entity ID or entity name identifies it as `power` or `on/off` over feature/settings switches such as oscillation;
- retain the physical device's friendly name on the tile.

Do not brand-special-case Govee. This is a generic Home Assistant capability rule. If feature metadata is genuinely absent/unknown, preserve legacy behaviour rather than assuming the fan is invalid.

## Weather centring rule from v215

The weather surface remains: 182dp hero slot, RGB 16/16/16, 14dp corners, RGB 48/48/48 stroke, 3:4:3 main regions, keyless Open-Meteo, 30-minute refresh/cache, non-focusable and Now Playing priority.

When centring weather, use the visible divider geometry as the reference:
- current/hourly/daily headings centred inside their own 3:4:3 regions;
- symmetric horizontal padding inside each region;
- current icon + temperature text centred as one visual group;
- forecast cells centred;
- footer must use the same 3:4:3 weights, not equal thirds;
- wind, sun and update/source text centred within those corresponding footer regions.

## Latest verified artifact

Build source `376bb700f2aedc9ffdd659c036bd1abfb14a9948`.
Run `35345512061`, job `105601126131`; artifact `10546950922`, `BOOP-Shield-v215-Wall-v207-Signed`.
Deliver `BOOP-Shield-v215.apk`, 160485741 bytes, SHA-256 `6297d060874ab7b06fc61f4b29f8e3179b40528cd53377681c6f9811ff01ffc0`.
Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` unchanged.
Downloaded ZIP SHA-256 `b39b83515876efe3508419b326d22c7e4f84ba44eb9d39996ac4e39117e95c2f`.
18 copied HA unit tests passed with zero failures/errors/skips; all 16 native libraries remain baseline-identical.

The first v215 code commit had a caught constructor typo and failed before APK build; it is superseded and must not be delivered. Physical Shield acceptance is still Ryan's test.

Voice/provider/pitch work remains deferred and untouched. Wall stays v207. No RDC/device driving, daily Pixel access, permissions or signer changes were made.
