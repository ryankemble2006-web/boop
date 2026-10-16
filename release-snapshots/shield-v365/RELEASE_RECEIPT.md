# BOOP Shield v365 source snapshot

This file identifies the versioned source snapshot. The GitHub Actions run
generates the signed APK, SHA-256, signer certificate report, and commit receipt
as downloadable artifacts after tests and build verification complete.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.365-shield` (`versionCode 365`)
- Unclipped Tile Focus Borders & Symmetrical Scroll Clearance:
  - Overrode `computeScrollDeltaToGetChildRectOnScreen` on `HorizontalScrollView` in `ShieldRoomPanelView` to apply a persistent `16dp` horizontal breathing room inset (`target.inset(-dp(16), 0)`), preventing Android's default scroll calculation from docking boundary tiles flush against viewport clipping boundaries.
  - Configured `scroller` padding to `dp(16)` horizontal and `dp(6)` vertical with `setClipToPadding(false)` and `setClipChildren(true)` to keep carousel tiles strictly inside viewport while allowing the 1.03x scale and retro orange focus stroke to expand cleanly into padding without edge truncation.
  - Sized all carousel tiles equally via `RoomPanelLayout.tileWidth` across the padded viewport width, configured equal `12dp` internal inter-tile gaps, and set `16dp` trailing rightMargin on the last tile child.
  - Maintained boundary D-pad stop clamps and unclipped bottom vertical card gutter.
- Visual verification: tested live over ADB on NVIDIA Shield TV Pro.
