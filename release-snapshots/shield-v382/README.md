# BOOP Shield v382

This versioned snapshot contains the complete source project for the BOOP Shield
package installed on the NVIDIA Shield TV.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.382-shield` (`versionCode 382`)
- Tactile Lyrics Lookup & Suffix Cleaning:
  - Added native `[ ⌫ Del ]` mini-buttons beside Track Title and Artist input fields (single-tap deletes previous character, long-press deletes word).
  - Added native `[ Clean Suffix ]` and `[ Clean ]` mini-buttons with multi-pass regex stripping common track metadata noise:
    - Parenthetical versions e.g. `(Acoustic Version)`, `(2008 Remaster)`, `(Live...)`
    - Bracketed editions e.g. `[Deluxe Edition]`, `[2011 Remaster]`
    - Dashed suffixes e.g. `- Live from Wembley`, `- Acoustic Version`, `- Remastered`
    - Featuring credits e.g. `feat. ...`, `ft. ...`, `featuring ...`
  - Fixed D-pad navigation down into soft keyboard: removed the artificial `KEYCODE_DPAD_DOWN` key listener on title that intercepted downward navigation.
  - Forced soft keyboard display (`SHOW_FORCED`) when activating input fields directly.
- Signed exclusively via GitHub Actions with `BOOP_DEV_KEYSTORE_B64`.
