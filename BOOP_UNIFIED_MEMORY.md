# BOOP durable project memory

Updated 2026-09-18.

The consumer apps remain split shells around shared BOOP code: Wall `com.boop.alpha1` stays v207; Shield `com.boop.shieldoverlay` is v216 on `boop-shield-weather-icon-fan-v216`. The owning split branch remains `boop-wall-shield-split-v207`.

## Shield room-panel control rules

The accepted low-latency architecture remains:
- discovery/registry/state loading is connection/background work, never a pre-click gate;
- validate current generation/room/cached card locally;
- keep one long-lived `state_changed` stream as the display truth;
- do not invent optimistic state;
- reject stale room/generation work and duplicate pending actions.

Sonoff and light controls proved instant on the v214/v215 direct `call_service` path and must remain there.

### Fan control rule from v216

v215 capability-aware entity selection remains required: native fan entities need the current HA power flags when feature metadata is known, otherwise choose the same device's actual power/on-off switch rather than oscillation/settings entities.

In addition, when the physical tile is semantically a fan, first use Home Assistant's WebSocket `conversation/process` with a room-scoped natural command (for example `turn off Govee Fan in Living Room`). This deliberately lets Home Assistant resolve the fan through the same Assist/conversation machinery that BOOP's phone command proved functional. Accept only an `action_done` response with no failed targets. If conversation cannot act, immediately fall back to the v215 direct service route. This is generic fan behaviour, not a Govee brand special case.

## HA icon rule from v216

Icons represent the physical device, not the implementation entity:
- semantic fan -> fan-blade vector even if backing control is `switch.*`;
- semantic sub/subwoofer -> speaker cabinet with woofer vector;
- light -> bulb;
- remaining switches -> power symbol.

Use display name + device name + entity ID to classify semantic fan/subwoofer identity.

## Weather centring rule from v216

The screenshot demonstrated that centred containers plus wrap-content children are not sufficient. Weather centring is now locked to the actual divider/cell geometry:
- no horizontal inset inside the three main 3:4:3 regions;
- every hourly/daily line consumes MATCH_PARENT cell width;
- explicit centre gravity + centre text alignment for every line;
- weather glyph receives a +2dp optical horizontal correction for font side bearing;
- numeric/text rows stay mathematically centred;
- headings and footer stay centred within the same 3:4:3 regions.

Do not reintroduce per-section padding that changes the region centre.

## Latest verified artifact

Build source `e60a3521524e1ccdb19fcac73f4ad4c3033bf618`.
Run `35347668181`, job `105607970217`; artifact `10547503847`, `BOOP-Shield-v216-Wall-v207-Signed`.
Deliver `BOOP-Shield-v216.apk`, 160485741 bytes, SHA-256 `a81dfd6f143ebe32952e48924184b52325644ed64b8ed217443cb1a6dd460004`.
Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` unchanged.
Downloaded ZIP SHA-256 `3cfaf36dd7936ecf4a57d5c5c57ffa16d0d2347837fa388020d13e13f23fa293`.
20 copied HA unit tests passed with zero failures/errors/skips; all 16 native libraries remain baseline-identical.

v215 is the prior signed checkpoint. Earlier v216 failed runs did not produce install candidates: one caught a weather inset invariant; another suffered an external 504 and then caught a unit-test exception declaration. Only the verified build source above should be delivered.

Voice/provider/pitch work remains deferred and untouched. Wall stays v207. No device driving, daily Pixel access, permissions or signer changes were made.
