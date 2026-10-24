# BOOP Shield v397

This versioned snapshot contains the complete source project for the BOOP Shield
package installed on the NVIDIA Shield TV.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.397-shield` (`versionCode 397`)
- Hardened System Whitelist & 0ms In-Place MSConfig Table:
  - Enforces a complete hardware/OS untouchable whitelist (`StartupRecoveryPolicy`) derived from the NVIDIA Shield TV Pro package audit.
  - Strictly forbids uninstalling ANY system package (`canUninstall` requires `!state.systemApp()`).
  - Updates `Rip Out` checkboxes and counter in-place in 0ms without rebuilding the view tree.
- Signed exclusively via GitHub Actions with `BOOP_DEV_KEYSTORE_B64`.
