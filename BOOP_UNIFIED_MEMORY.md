# BOOP durable project memory

Updated 2026-09-18.

The consumer apps remain split shells around shared BOOP code: Wall `com.boop.alpha1` stays v207; Shield `com.boop.shieldoverlay` is v218 on `boop-shield-now-playing-align-v218`. The owning split branch remains `boop-wall-shield-split-v207`.

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

When the physical tile is semantically a fan, first use Home Assistant's WebSocket `conversation/process` with a room-scoped natural command. Accept only an `action_done` response with no failed targets. If conversation cannot act, immediately fall back to the v215 direct service route. This is generic fan behaviour, not a Govee brand special case.

## HA icon rule from v216

Icons represent the physical device, not the implementation entity:
- semantic fan -> fan-blade vector even if backing control is `switch.*`;
- semantic sub/subwoofer -> speaker cabinet with woofer vector;
- light -> bulb;
- remaining switches -> power symbol.

Use display name + device name + entity ID to classify semantic fan/subwoofer identity.

## Weather centring rule from v216

Weather centring is locked to the actual divider/cell geometry:
- no horizontal inset inside the three main 3:4:3 regions;
- every hourly/daily line consumes MATCH_PARENT cell width;
- explicit centre gravity + centre text alignment for every line;
- weather glyph receives a +2dp optical horizontal correction for font side bearing;
- numeric/text rows stay mathematically centred;
- headings and footer stay centred within the same 3:4:3 regions.

Do not reintroduce per-section padding that changes the region centre.

## Grab/reorder rule from v217

HOME favourites and HA controls now share the same remote mental model: hold to grab, left/right to move, OK/Enter to drop.

- Favourite ordinary focus must keep the accepted artwork geometry. Only an active grab receives the stronger 1.14x + Z-depth lift.
- HA tiles receive a 1.10x + Z-depth lift only while grabbed.
- A normal HA click remains a normal device toggle; entering reorder mode must not add a pre-click network gate or change HA latency.
- HA control order is persisted per room. Reconcile saved order against currently available entities, drop vanished IDs, and append newly discovered IDs after the saved order.
- Room changes cancel an in-progress grab rather than moving an entity into another room's ordering.

## Now Playing alignment rule from v218

Use the visible progress bar as the horizontal datum for the media text/transport stack. Title, artist, playback-state label, progress bar and transport row all start on the same details-column left edge. Do not reintroduce the old 8dp transport-only left padding. Lyrics/Close-player remain in the title row's right-side controls and are not part of this datum. Album art and mascot bay geometry remain unchanged.

## Latest verified artifact

Build source `a5f211a4038e208109e0ac31e18452c410a8f455`.
Run `35352592271`, job `105624043955`; artifact `10550157413`, `BOOP-Shield-v218-Wall-v207-Signed`.
Deliver `BOOP-Shield-v218.apk`, 160485741 bytes, SHA-256 `37db129fce83bf8cca062e93d1474533d8f4b7253120da1255912914ff4d8b58`.
Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` unchanged.
Uploaded artifact ZIP SHA-256 `e9ca76c0fecb22d6de60815d63e9939ab021c26b40d07d3485f1890145f3ed9a`.
All 16 native libraries remain baseline-identical.

v217 is the prior signed checkpoint. The first v217 run stopped on the intentionally superseded v205 assertion that a grabbed favourite must remain 1.00x and produced no APK.

Voice/provider/pitch work remains deferred and untouched. Wall stays v207. No device driving, daily Pixel access, permission or signer changes were made.
