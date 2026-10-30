# BOOP Shield v410

- Version:1.2.410-shield, packagecom.boop.shieldoverlay.
- Source baseline:v408. Rebuild the current launcher page after display configuration changes so constructor-sized text/artwork/custom views all use the new density together. Retain existing Home and Settings focus/scroll helpers and all stored preferences.
- Platform setup: NVIDIA Show all supported modes enabled, rebooted; no forced1080psize. Confirmed actual3840x2160launcher and Kodi buffers, including another reboot.
- Build: compile the narrowly scoped launcher method patch in GitHub Actions from pinned tools and the existing v408 APK. Preserve other dex/resources/assets/native libraries byte-for-byte. Sign with existing GitHub secrets and verify certificate continuity.
- Hardware APK verification: pending signed build/install.
