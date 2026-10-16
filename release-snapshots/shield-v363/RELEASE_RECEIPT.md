# BOOP Shield v363 source snapshot

This file identifies the versioned source snapshot. The GitHub Actions run
generates the signed APK, SHA-256, signer certificate report, and commit receipt
as downloadable artifacts after tests and build verification complete.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.363-shield` (`versionCode 363`)
- Anti-Clipping & Visual Breathing Room on Home Assistant Widget:
  - Enabled `setClipChildren(false)` and `setClipToPadding(false)` on `ShieldRoomPanelView` so scaled tile cards (1.03x) and 5dp focus strokes draw cleanly into the panel padding without border truncation.
  - Added horizontal scroller end padding (`dp(14)`) and `dp(8)` rightMargin on the last tile child in `resizeTiles()` so the right border of the last tile never slams against the scroll boundary.
  - Added focus rect inset (`target.inset(-dp(14), -dp(6))`) in `focus.requestRectangleOnScreen` and `Tile.setOnFocusChangeListener` to guarantee the full 5dp orange focus outline and rounded corners are completely within viewport when navigating all the way to the right.
  - Increased `panelHeight` to `dp(118)`, increased `bottomGutter` to `dp(32)`, and added `dp(28)` bottom delta in `computeScrollDeltaToGetChildRectOnScreen` so the entire Home Assistant widget card has generous breathing room above the bottom screen boundary when focused.
- Visual verification: tested live over ADB on NVIDIA Shield TV Pro.
