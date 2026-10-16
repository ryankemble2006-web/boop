# BOOP Shield v365 source snapshot

This file identifies the versioned source snapshot. The GitHub Actions run
generates the signed APK, SHA-256, signer certificate report, and commit receipt
as downloadable artifacts after tests and build verification complete.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.365-shield` (`versionCode 365`)
- Living Room carousel edge layout:
  - Extends the horizontal scroller across the panel's `18dp` side insets while
    keeping the panel itself as the clipping boundary.
  - Places real `16dp` leading and trailing gutters in the measured row content,
    with `16dp` between each tile to match the widgets above.
  - Expands focus-scroll rectangles by `16dp` so the first and last tiles retain
    their edge clearance at the horizontal scroll limits.
  - Doubles the focused tile outline from `2dp` to `4dp`.
- Pressing DPAD-down from a favourite now returns that carousel to its starting
  horizontal position when focus successfully moves to Seren or the Living
  Room panel.
