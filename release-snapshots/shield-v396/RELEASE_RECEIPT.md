# BOOP Shield v396 source snapshot

This file identifies the versioned source snapshot. The release handoff records
the locally built APK hash, GitHub signature verification, and installed
package version after completion.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.396-shield` (`versionCode 396`)
- MSConfig-Style Startup & Silent Batch Uninstall Manager:
  - Streamlined single-screen 10-foot OLED table with direct Startup checkboxes and multi-select Rip Out checkboxes.
  - Silent batch package uninstallation via local loopback ADB (`pm uninstall` / `pm uninstall --user current`) with zero user clicking.
  - Preserves recovery-protected system package guards and dynamic user highlight accent colours (`BoopTvChrome.accentColor`).
