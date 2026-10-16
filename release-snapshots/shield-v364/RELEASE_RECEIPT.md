# BOOP Shield v364 source snapshot

This file identifies the versioned source snapshot. The GitHub Actions run
generates the signed APK, SHA-256, signer certificate report, and commit receipt
as downloadable artifacts after tests and build verification complete.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.364-shield` (`versionCode 364`)
- Anti-Collision & Unclipped Focus Borders on Home Assistant Widget:
  - Enforced strict container clipping (`setClipChildren(true)`, `setClipToPadding(true)`) on `ShieldRoomPanelView` to guarantee tiles never spill outside the Living Room card onto the OLED black background.
  - Configured `scroller` (`HorizontalScrollView`) with `setClipChildren(true)` to clip off-screen carousel tiles at the scroller viewport edge, eliminating left/right boundary collisions.
  - Enabled `setClipToPadding(false)` and configured balanced `10dp` horizontal / `5dp` vertical scroller padding (`setPadding(dp(10), dp(5), dp(10), dp(5))`) so focused tiles' 1.03x scale and 5dp retro orange focus outline expand cleanly into padding without border clipping on either the first tile (left boundary) or the last tile (right boundary).
  - Maintained generous bottom breathing room (`panelHeight = dp(118)`, `bottomGutter = dp(32)`, `+dp(28)` bottom delta in scroll computation) above the bottom TV screen boundary.
- Visual verification: tested live over ADB on NVIDIA Shield TV Pro.
