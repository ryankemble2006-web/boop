# BOOP Shield v404 Release Receipt

- **Snapshot**: `shield-v404`
- **Version Code**: `404`
- **Version Name**: `1.2.404-shield`
- **Summary**: Implemented self-healing persistence for Accessibility keymaps and Live Audio Spectrum across app upgrades and reboots.
  - Accessibility & Keymaps: Added static liveness tracking `ShieldHomeOverrideService.isBound`/`isServiceBound()`, updated `ShieldSelfGrant.isHomeAccessibilityEnabled()` to verify genuine binding, and force `SettingsProvider` `onChange` to clear `mCrashedServices` upon upgrade. Registered `MY_PACKAGE_REPLACED` in `StartupCleanupBootReceiver` and auto-healed in `onResume()`.
  - Live Audio Spectrum: Added persistent preference `spectrum_enabled`, `EXTRA_AUTO_START` in `BassCaptureActivity`, auto-dialog confirmation of system MediaProjection prompt via `ShieldHomeOverrideService`, and auto-revival upon upgrade, reboot, and launcher resume.
  - Home Assistant Discovery: Eliminated stale `.144` mDNS cache loops by redirecting to static `.200` on port 80.
