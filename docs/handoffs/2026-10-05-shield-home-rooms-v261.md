# Shield261: persistent Home room picker and complete Seren posters

Ryan approved making the Home room heading remote-focusable in the chosen
accent colour, opening a room list on click, and retaining the chosen room until
explicitly changed back. Seren must show only whole posters across the available
width. Base is the live Shield260 owning branch boop-shield-heart-layout-v260,
a0b495a6525c660ccf9ef5d0121053799488ba0e. Work uses a clean task-owned clone.

## Implementation

ShieldRoomPanelView exposes text-only accent focus on the heading. Down traverses
Seren -> room heading -> controls; Up reverses that path. Optional Home rows keep
their normal order. Live refresh and room changes retain heading focus. Selecting
a room in the native scrollable dialog saves BoopPreferences.selectedRoom and
restarts the active RoomPanelSession immediately. The existing controller clears
old cards and invalidates generations before loading the new room. This is the
same persistent room selection used by BOOP device setup. No temporary override
or automatic reset is introduced.

The dialog loads the existing authenticated HA area list, shows loading/error/retry,
marks the current room, and cancels work on dismiss/pause/destroy/new Home intent.
Room-list connection drops become errors instead of escaping on the UI thread.

SerenPosterLayout fits an integer number of existing 2:3 posters plus spacing and
focus padding. Horizontal focus/scroll settles at poster boundaries, including the
last window. Enabling clipping at the resized scroll viewport prevents the next
poster from drawing past its edge. Poster height, artwork, episode playback and
existing device-tile sizes are preserved. Shield becomes261; Wall remains220.

## Verification

New geometry test failed before implementation. The unchanged signed260 emulator
fixture failed heading focus/accent/click/persistence checks. Corrected tests cover
43 focused room, Seren, playback, accent, weather and navigation checks; all pass.

The local Android preview compiled. The real Android UI fixture passes on
BOOP_Android_TV_API_36 / emulator-5554 using isolated test preferences and synthetic
HA rooms: heading turns orange, click opens the real dialog, remote selects Kitchen,
selection survives a new RoomPanelSession, and switching back is saved. It also
checks all12 posters, horizontal traversal, widths reduced17/57/111px, unchanged
HA tile dimensions, return focus, exact episode selection, empty-feed focus and
artwork retry. The fixture injects HA data; it does not prove live HA transport or
physical Shield acceptance. Static independent review found one connection-drop
exception path, now fixed and reviewed with no remaining actionable issues.

Full signed CI and independent APK checks are pending. No physical installation
is authorised by this implementation request. Installed Shield260 remains intact.
