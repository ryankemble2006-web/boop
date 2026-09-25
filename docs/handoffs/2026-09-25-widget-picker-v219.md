# Phone widget chooser, Wall v219

Ryan requested that the cramped widget chooser fill the phone screen while preserving his deliberately enlarged text. This follows the physically accepted pocket-polish build on `boop-pocket-polish-v251`; it does not change the phone's copied shortcuts or font settings.

The launcher previously invoked Android's stock `ACTION_APPWIDGET_PICK` chooser. BOOP now opens a non-exported `WidgetPickerActivity` with a full-screen, inset-aware scrolling list. Rows use a separate icon, a 16dp icon-to-text gap, vertical padding, and content-driven label height. The originating app appears below each widget name. A persistent Close control cancels selection.

`WidgetController` retains ownership of allocated IDs and its existing Android bind-permission/configuration/result flow. The new chooser returns the selected provider and original ID through the same PICK request. Back/Close return cancellation. No widget access grant is automated in production.

## Verification before signed delivery

- Standalone launcher Android36 debug build passed.
- 20 existing launcher-swipe, launcher-polish, weather/version and split checks passed.
- Yoga Pixel10 emulator: full-screen chooser visually checked at the phone's 1344x2992 size, density544 and font1.5; font2.0 also remained readable.
- Selecting Analog reached Android's normal bind permission, then Clock configuration, then returned a saved widget to the launcher. Pending widget ID was cleared after success.
- Back and Close cancellation both cleared the pending ID while retaining the existing widget. Landscape at font2.0 also remained usable. Independent code review found no important regressions.

## Signed and physical delivery

[Signed CI36196531200](https://github.com/ryankemble2006-web/boop/actions/runs/36196531200) passed at `01b911d8e79316db1b1f42865b93b5fc0945b9de`. The independently verified Wall219 APK SHA256 is `40ea16d0c44c58daea6675f5b71eb5995dae31f8282f1164b51f9b176f5e6279`. Permanent signing and every asset/native byte match the accepted Wall218 baseline.

Installed in place on the physical Pixel10; installed APK hash matches. App ID and first-install time are unchanged. All22 copied shortcut destinations/labels remain present. Initial comparison found their positions unchanged apart from float serialization. Ryan subsequently added widgets and moved an icon during verification; those newer edits were preserved. Other saved settings are unchanged apart from notification-channel discovery metadata, and the notification listener is bound. Font1.5 and display density544 were retained.

The full-screen picker was opened and visually checked on the actual phone. Ryan explicitly accepted it as “great widget menu”, then reported a separate pre-existing widget resizing/centering problem. That interaction repair is the next scoped task; picker acceptance does not establish that the old widget manipulation controls work well.

The production version is Wall219. The existing two-shell CI also builds Shield251, but only the phone update is in scope for installation. Artwork, voice, notifications and the permanent signing identity are unchanged.
