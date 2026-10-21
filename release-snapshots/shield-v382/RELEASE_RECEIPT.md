# BOOP Shield v382 source snapshot

This file identifies the versioned source snapshot. The release handoff records
the locally built APK hash, GitHub signature verification, and installed
package version after completion.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.382-shield` (`versionCode 382`)
- Tactile Lyrics Lookup & Suffix Cleaning:
  - Added native `[ ⌫ Del ]` mini-buttons beside Track Title and Artist input fields (single-tap deletes previous character, long-press deletes word).
  - Added native `[ Clean Suffix ]` and `[ Clean ]` mini-buttons with multi-pass regex stripping common track metadata noise (`(Acoustic Version)`, `[Remaster]`, `- Live...`, `feat. ...`).
  - Restored downward D-pad navigation from title into soft keyboard.
  - Forced soft keyboard activation via `SHOW_FORCED`.
