# Shield room panel v213

## Scope and starting point
Ryan approved the room-derived, remote-first lower Home panel, expanding into BOOP's lower-right space when he is absent and contracting when he returns. It uses the existing "Set this device room" selection and existing HA credentials. A persistent Home settings switch hides it completely. New UI is BOOP charcoal/cyan, not a stock Android menu. Sensors remain a later task.

After repeated interrupted sessions Ryan requested a fresh implementation, autonomous completion, and a signed APK link. This work starts directly from the live v212 owner commit `2ab0db655368089b19f9c705fba2cd404a1cd4e7`, not from the incomplete v211 panel branch or its uncommitted trees. Work branch: `boop-shield-room-panel-v213`.

## Implementation
- New pure-Java RoomPanelController, with cancellable connection/load/action deadlines, bounded reconnection and generation-checked callbacks. A click revalidates room membership immediately before using the existing HA repository's confirmed on/off service path. Pending requests cannot double-toggle, and late confirmations cannot overwrite newer observed state.
- RoomPanelSession adapts the existing BoopPreferences, HomeAssistantSession, HomeAssistantWebSocket and HomeAssistantRepository. All view callbacks return on the main thread. Connections, subscriptions, retries and pending actions stop outside launcher Home or when disabled. No duplicate room keys or pairing flow.
- Native charcoal ShieldRoomPanelView retains stable entity tiles and focus on state updates. Buttons expand with available width; overflow scrolls horizontally. D-pad Down enters from favourites, Up returns to the previous favourite; Left/Right stop at row ends. No new stock dialog or popup.
- Layout measures the visible favourite captions, then fills the available lower area. Existing hero slot, favourite positions and BOOP corner geometry remain intact. The panel animates its reserved right margin over 200 ms, respecting disabled system animations.
- Existing supported physical device controls are lights, switches/smart plugs and fans. The existing repository's one-primary-control-per-device and hidden/config/diagnostic filtering are preserved. Thermostat detail pages, cameras and sensor readings are not claimed.

## Verification in progress
New deterministic controller tests cover no-room setup, confirmed commands, rejection, duplicate clicks, passive state changes, unavailable/unknown devices, stop/room-change stale callbacks, fresh membership, wrong-room replies, snapshot races, reconnection, reauthentication, request deadlines, cancellation and late command confirmation. Numerical layout tests cover expanded/contracted width, minimum space, overflow and many screen bounds. No emulator, image generation, screenshot or physical device was used.

The current v212 weather network permission, column-height repair and approved card chrome are retained. Voice, audio, artwork and native libraries are not edited. Wall remains v207 and is built for compatibility only, not requested for installation. Signed build and actual APK verification remain pending until recorded below. Ryan retains manual installation and physical/visual acceptance.
