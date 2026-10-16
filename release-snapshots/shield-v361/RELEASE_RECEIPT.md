# BOOP Shield v361 source snapshot

This file identifies the versioned source snapshot. The GitHub Actions run
generates the signed APK, SHA-256, signer certificate report, and commit receipt
as downloadable artifacts after tests and build verification complete.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.361-shield` (`versionCode 361`)
- Transparent Slider Foreground & High-Contrast Active Mode: eliminated opaque foreground covering the SeekBar track and thumb when selected. Foreground now strictly renders a 100% transparent center outline (2dp crisp highlight when navigable, 5dp bold focus stroke when adjusting). When active, sets a glowing pure white thumb (`#FFFFFF`) against the track with subtle warm accent card fill drawn safely behind the track on `setBackground()`. Increased D-pad increment steps across voice pitch/cadence, appearance colours, and launcher RGB sliders for clear, satisfying visible movement on 4K OLED.
- Visual verification: verified before/after states via physical screencap on NVIDIA Shield TV Pro.
