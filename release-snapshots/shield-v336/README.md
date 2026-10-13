# BOOP Shield v336

This versioned snapshot contains the complete source and Gradle project for the
BOOP Shield package installed on the NVIDIA Shield TV.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.336-shield` (`versionCode 336`)
- GitHub Actions runs the unit suite, builds with Gradle 9.6.0, signs with the
  existing BOOP development key stored in GitHub Actions secrets, verifies the
  signer certificate, and uploads the installable APK.
- Visual checks are intentionally left to the user on the physical Shield.
- Exact Home Assistant area names target every exposed light in that area;
  exact named light groups are also supported before the selected room is applied.

The workflow also rejects source changes unless the Shield version code is
advanced by exactly one from the latest archived Shield snapshot.
