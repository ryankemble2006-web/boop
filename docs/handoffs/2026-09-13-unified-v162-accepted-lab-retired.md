# Unified v162 acceptance and standalone Lyrics Lab retirement

2026-09-13. Owner: `boop-unified-eye-sync-safe-v159`.

Ryan replied "perfection" to the delivered Unified v162 / Now Playing -> Lyrics
and Skip confirmation. This is recorded as user acceptance of the integrated
lyrics feature on Shield. It satisfies his earlier instruction to remove the
standalone Lyrics Lab after confirmation. No further blanket physical results
are inferred, and the same successful gate should not be requested again.

## Fresh cleanup evidence

Only the authorized Shield was addressed, using the existing pinned connection.
A live model read identified SHIELD Android TV. Package/version/base-APK hashes
were read before removal and checked again immediately before the uninstall:

- Unified: `com.boop.alpha1`, `162 / 1.2.162-native-lyrics`.
  SHA256 `cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171`.
- Standalone Lab: `com.boop.lyricslab`, `159 / 0.1.159-lyrics-footer`.
  SHA256 `fb373b09572379976dcf6b15dd54ce307f7153933ca61cd3d7f8a9204d716503`.

The exact single-package command `adb -s <authorized-Shield> uninstall com.boop.lyricslab`
returned Success and exit0. A subsequent package-list query returned no exact
`package:com.boop.lyricslab` entry. Unified's version and installed-base SHA256
were independently re-read and matched the accepted v162 above without change.
No data from user accounts or app preferences was inspected. No app launch,
playback command, manual permission/settings change, phone/emulator operation,
new install, rebuild, app-source edit or other package cleanup was performed.
Ordinary Android cleanup of the removed Lab belongs to that explicit uninstall.

## Provenance and boundaries

The original v162 artifact is run `34773509395`, artifact `10322553107`, built
from `1e0136ffa9732353035f88ca7a7cb131f7481557`, permanent signer unchanged.
Installation/merge details remain in `2026-09-13-unified-v162-lyrics.md`.
Earlier green test counts are preserved, not rerun in this acceptance update.
New physical behavior evidence is Ryan's report; new assistant evidence is only
the scoped cleanup and identity readback. No new visual/audio sweep occurred.

Both the Unified source and historical Lab branch remain on GitHub. Do not delete
recovery history, reinstall the retired helper or replace newer Unified with the
old Lab base. The integrated Lyrics screen is independent of that removed app.
