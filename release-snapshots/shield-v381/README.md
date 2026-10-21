# BOOP Shield v381

This versioned snapshot contains the complete source project for the BOOP Shield
package installed on the NVIDIA Shield TV.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.381-shield` (`versionCode 381`)
- Native hardware button remapping, double-tap Home Recents restore & TV Task Killer:
  - Added low-latency double-tap detection in `ShieldHomeOverrideService` (280ms threshold).
  - Home Button (Key 3):
    - Single Tap: Return Home (`GLOBAL_ACTION_HOME`).
    - Double Tap: Open Recents / Task Switcher (`GLOBAL_ACTION_RECENTS` / `KEYCODE_APP_SWITCH`).
    - Long Press (400ms): Background Deezer Flow.
  - Added "Double Tap" action support to `BoopKeyMappingStore` and `BoopRemapperActivity`.
  - Added `ACTION_RECENTS` to available action choices in the Button Remapper settings UI.
  - Retains instant loopback ADB `am force-stop` on task switcher dismissals.
- Signed exclusively via GitHub Actions with `BOOP_DEV_KEYSTORE_B64`.
