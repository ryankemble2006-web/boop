# Phone widget chooser, Wall v219

Ryan requested that the cramped widget chooser fill the phone screen while preserving his deliberately enlarged text. This follows the physically accepted pocket-polish build on `boop-pocket-polish-v251`; it does not change the phone's copied shortcuts or font settings.

The launcher previously invoked Android's stock `ACTION_APPWIDGET_PICK` chooser. BOOP now opens a non-exported `WidgetPickerActivity` with a full-screen, inset-aware scrolling list. Rows use a separate icon, a 16dp icon-to-text gap, vertical padding, and content-driven label height. The originating app appears below each widget name. A persistent Close control cancels selection.

`WidgetController` retains ownership of allocated IDs and its existing Android bind-permission/configuration/result flow. The new chooser returns the selected provider and original ID through the same PICK request. Back/Close return cancellation. No widget access grant is automated in production.

## Verification before signed delivery

- Standalone launcher Android36 debug build passed.
- 20 existing launcher-swipe, launcher-polish, weather/version and split checks passed.
- Yoga Pixel10 emulator: full-screen chooser visually checked at the phone's 1344x2992 size, density544 and font1.5; font2.0 also remained readable.
- Selecting Analog reached Android's normal bind permission, then Clock configuration, then returned a saved widget to the launcher. Pending widget ID was cleared after success.
- Signed CI, cancellation checks and physical in-place installation are still pending at this source checkpoint.

The production version is Wall219. The existing two-shell CI also builds Shield251, but only the phone update is in scope for installation. Artwork, voice, notifications and the permanent signing identity are unchanged.
