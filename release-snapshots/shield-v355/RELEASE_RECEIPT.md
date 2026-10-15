# BOOP Shield v355 source snapshot

This file identifies the versioned source snapshot. The GitHub Actions run
generates the signed APK, SHA-256, signer certificate report, and commit receipt
as downloadable artifacts after tests and build verification complete.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.355-shield` (`versionCode 355`)
- Launcher Window & Recents Translucency: convertFromTranslucent reflection fallback with mTranslucent synchronization, explicit PixelFormat.OPAQUE window format, black background, DIM_BEHIND clearance, and transition suppression on resume, focus change, and new intent.
- Visual verification: pending manual check on the physical Shield after install.
