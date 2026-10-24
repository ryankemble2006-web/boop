# BOOP Shield v397 source snapshot

This file identifies the versioned source snapshot. The release handoff records
the locally built APK hash, GitHub signature verification, and installed
package version after completion.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.397-shield` (`versionCode 397`)
- Hardened System Whitelist & 0ms In-Place MSConfig Table:
  - Enforces a complete hardware/OS untouchable whitelist (`StartupRecoveryPolicy`) derived from the NVIDIA Shield TV Pro package audit (protecting all Android framework, `networkstack`, Google Play/permissions/katniss, NVIDIA audio/Dolby/remote/CEC/platform services, and BOOP packages).
  - Strictly forbids uninstalling ANY system package (`canUninstall` requires `!state.systemApp()`); locks `SELECT RIP OUT` as `🔒 System` for all system apps so `pm uninstall` can never strip updates or remove `--user 0` system services.
  - Updates `Rip Out` checkboxes, row borders, status pills, and the bottom counter button in-place in 0ms without calling `setContentView()`, eliminating UI lag and background music blips.
  - Streams batch user-app uninstalls in a single pass without redundant inventory reloads.
