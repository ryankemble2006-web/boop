# BOOP Shield v372

This versioned snapshot contains the complete source and Gradle project for the
BOOP Shield package installed on the NVIDIA Shield TV.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.372-shield` (`versionCode 372`)
- Deezer Track Mix: focus the current track title to show the selected orange
  highlight. Selecting it sends Deezer's native `TRACK_MIX&&<track-id>` media ID
  through its active media session while BOOP stays onscreen.
- The lyrics screen title uses the same focused Track Mix action.
- Lyrics lookup opens Shield's on-screen keyboard when a title or artist field
  receives focus, and keeps it available while moving between those fields.
- GitHub Actions runs the unit suite, builds with Gradle 9.6.0, signs with the
  existing BOOP development key stored in GitHub Actions secrets, verifies the
  signer certificate, and uploads the installable APK.
- Visual verification follows installation of the signed APK on the physical Shield.
- Exact Home Assistant area names target every exposed light in that area;
  exact named light groups are also supported before the selected room is applied.

The workflow also rejects source changes unless the Shield version code is
advanced by exactly one from the latest archived Shield snapshot.
