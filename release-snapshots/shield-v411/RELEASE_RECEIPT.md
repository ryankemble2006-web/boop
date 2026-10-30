# BOOP Shield v411

- Version:1.2.411-shield, packagecom.boop.shieldoverlay.
- Source baseline:v407. Rebuild the current launcher page after display configuration changes so constructor-sized text/artwork/custom views all use the new density together. Retain existing Home and Settings focus/scroll helpers and all stored preferences.
- Platform setup: NVIDIA Show all supported modes enabled, rebooted; no forced1080psize. Confirmed actual3840x2160launcher and Kodi buffers, including another reboot.
- Build: compile the narrowly scoped launcher method patch in GitHub Actions from pinned tools and the existing v407 APK. Preserve other dex/resources/assets/native libraries byte-for-byte. Sign with existing GitHub secrets and verify certificate continuity.
- Hardware APK verification: pending signed build/install.

Baseline uses the working v407 APK. All non-signature ZIP entries match the v407 pulled from the Shield. The v408 baseline fails Application.onCreate with IllegalAccessError for relocated BoopRoom, so it is excluded. v410 was rolled back without deleting app data.
