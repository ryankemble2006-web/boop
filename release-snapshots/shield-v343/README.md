# BOOP Shield v343

This versioned snapshot contains the complete source and Gradle project for the
BOOP Shield package installed on the NVIDIA Shield TV.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.343-shield` (`versionCode 343`)
- Fix: Lyrics now uses the same left-aligned title and artist text treatment as
  Home Now Playing; its custom one-pass marquee remains in control.
- GitHub Actions runs the unit suite, builds with Gradle 9.6.0, signs with the
  existing BOOP development key stored in GitHub Actions secrets, verifies the
  signer certificate, and uploads the installable APK.
- Visual verification follows installation of the signed APK on the physical Shield.
- Exact Home Assistant area names target every exposed light in that area;
  exact named light groups are also supported before the selected room is applied.

The workflow also rejects source changes unless the Shield version code is
advanced by exactly one from the latest archived Shield snapshot.
