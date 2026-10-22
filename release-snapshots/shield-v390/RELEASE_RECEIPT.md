# BOOP Shield v390 source snapshot

This file identifies the versioned source snapshot. The release handoff records
the locally built APK hash, GitHub signature verification, and installed
package version after completion.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.390-shield` (`versionCode 390`)
- Bedroom Lazy Torch (Double-Tap Netflix Button) & Brightness Restoration:
  - Double-tapping Netflix button (Key 199) triggers fullscreen pure opaque white (100% full panel brightness) torch overlay.
  - Temporarily suppresses software dimmer scrim during torch mode so white shines with maximum lumens.
  - Pressing BACK (or DPAD_CENTER / ENTER / Netflix) instantly dismisses the torch, restores previous brightness level, and consumes the event so whatever screen, video, or app was underneath remains completely untouched.
  - Consumes directional D-pad keys during torch mode to guard against blind navigation in the dark.
  - Integrated `ACTION_TORCH` into `BoopKeyMappingStore`, `BoopKeyActionExecutor`, and `BoopRemapperActivity`.
