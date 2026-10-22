# BOOP Shield v391 source snapshot

This file identifies the versioned source snapshot. The release handoff records
the locally built APK hash, GitHub signature verification, and installed
package version after completion.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.391-shield` (`versionCode 391`)
- Media Fast Forward Hardware Key Remapping:
  - Added `KeyEvent.KEYCODE_MEDIA_FAST_FORWARD` (Key 90) to `BoopKeyMappingStore` defaults, watched keys, and `BoopRemapperActivity`.
  - Configured default single-tap to `ACTION_NEXT_TRACK` with full support for custom single-tap, double-tap, and long-press reassignment.
  - Symmetrical pairing with `KEYCODE_MEDIA_REWIND` (Key 89 -> Prev Track) for full media transport hardware control.
