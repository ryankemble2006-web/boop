# BOOP Shield v396

This versioned snapshot contains the complete source project for the BOOP Shield
package installed on the NVIDIA Shield TV.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.396-shield` (`versionCode 396`)
- MSConfig-Style Startup & Silent Batch Uninstall Manager:
  - Streamlined single-screen 10-foot OLED table with direct Startup checkboxes and multi-select Rip Out checkboxes.
  - Silent batch package uninstallation via local loopback ADB (`pm uninstall` / `pm uninstall --user current`) with zero user clicking.
  - Preserves recovery-protected system package guards and dynamic user highlight accent colours (`BoopTvChrome.accentColor`).
- Signed exclusively via GitHub Actions with `BOOP_DEV_KEYSTORE_B64`.
