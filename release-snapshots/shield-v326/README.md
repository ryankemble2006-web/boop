# BOOP Shield v326

This versioned snapshot contains the complete source and Gradle project for the
BOOP Shield package installed on the NVIDIA Shield TV.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.326-shield` (`versionCode 326`)
- GitHub Actions runs the unit suite, builds with Gradle 9.6.0, signs with the
  existing BOOP development key stored in GitHub Actions secrets, verifies the
  signer certificate, and uploads the installable APK.
- Visual checks are intentionally left to the user on the physical Shield.

The workflow also rejects source changes unless the Shield version code is
advanced by exactly one from the latest archived Shield snapshot.
