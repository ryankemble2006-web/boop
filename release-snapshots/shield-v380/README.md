# BOOP Shield v380

This versioned snapshot contains the complete source project for the BOOP Shield
package installed on the NVIDIA Shield TV.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.380-shield` (`versionCode 380`)
- Native hardware button remapping and true-kill task switcher dismiss:
  - Upgrade ShieldHomeOverrideService accessibility service to intercept hardware key events (flagRequestFilterKeyEvents).
  - Out-of-the-box native hardware mappings with zero Button Mapper dependency:
    - Key 89 (MEDIA_REWIND) -> Previous track
    - Key 199 (BUTTON_12 / Netflix button) -> Sleep TV Timer Pro
    - Key 3 (HOME long press) -> Background Deezer Flow
    - Key 82 (MENU / Burger button) -> Single tap: Next track, Long press: Previous track
    - Key 85 (MEDIA_PLAY_PAUSE) -> Play/Pause
  - Button Remapper TV Settings UI: Allows customizing key actions or resetting to defaults.
  - Recents Task Force-Stop: Intercepts dismiss_icon clicks in Android TV recents and executes loopback ADB am force-stop, terminating Kodi and background processes immediately.
- The locally built APK is sent through the GitHub signing handoff, which uses
  the existing BOOP development key stored in GitHub secrets and returns the
  signed installable APK with signature verification evidence.
- Visual verification follows installation of the signed APK on the physical Shield.
- Exact Home Assistant area names target every exposed light in that area;
  exact named light groups are also supported before the selected room is applied.

The workflow also rejects source changes unless the Shield version code is
advanced by exactly one from the latest archived Shield snapshot.
