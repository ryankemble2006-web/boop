# BOOP Shield v383

This versioned snapshot contains the complete source project for the BOOP Shield
package installed on the NVIDIA Shield TV.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.383-shield` (`versionCode 383`)
- Native HDMI-CEC Standby Resilience & Locked Chunky 10-Foot UI:
  - Added `configChanges="density|screenSize|screenLayout|orientation|smallestScreenSize|uiMode"` across Shield TV activities to eliminate activity destruction and re-creation on TV sleep, standby, wake, and HDMI EDID hotplug renegotiation.
  - Implemented `onConfigurationChanged` handling in `ShieldLauncherActivity` to gracefully re-layout active views while preserving scroll and focus without restart.
  - Locked tactile, glasses-free 10-foot TV card and weather geometry for 55" QD-OLED.
- Signed exclusively via GitHub Actions with `BOOP_DEV_KEYSTORE_B64`.
