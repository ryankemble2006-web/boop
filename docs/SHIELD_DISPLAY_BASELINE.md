# Current Shield display baseline — 4 November 2026

This supersedes the earlier native-4K-UI experiment. Ryan wants system Settings and the task switcher readable, font scale 1.3 retained, and BOOP's deliberately chunky layout unchanged.

## Cause and correction

The NVIDIA developer option **Show all supported modes in custom display modes** had been enabled during earlier native 4K launcher work. After reboot, the UI rendered at 3840x2160 with stock 320 DPI. System Settings and Recents became tiny; BOOP looked correct because its app-local scaling already compensates for resolution.

Removing BOOP's global display writers in v416 was necessary but did not disable this remaining firmware configuration. v417 also fixed background capture focus theft and remains installed.

Turned off the NVIDIA option through the installed Settings UI, accepted its required reboot, and cleared legacy ADB size/density overrides before reboot. Kept 4K HDMI output and font_scale=1.3.

## Verified baseline

- NVIDIA extended display modes: off (`persist.vendor.tegra.hdmi.all.modes=0`). The installed firmware's default constant is 0; its UI warns of app graphical/UI problems with the option enabled.
- HDMI output: 3840x2160, unchanged.
- NVIDIA-managed UI: 1920x1080 (`vendor.sys.ui.resolution.x/y=1920/1080`).
- System density: stock 320 DPI; no saved density override.
- Text scale: 1.3, unchanged across reboot and sleep/wake.
- Window, transition and animator animation scales: all 0.0. Ryan explicitly wants these kept off because Deezer's animated curtain is irritating. Do not enable animations to repair Recents first-open behavior.
- Saved `Settings.Global.display_size_forced` and `Settings.Secure.display_density_forced`: empty after reboot/wake.
- `wm size` reports physical 3840x2160 and runtime override 1920x1080. With the saved forced setting empty, this runtime UI cap is NVIDIA-managed. Do not treat it as the earlier ADB override or reset it back to native 4K.
- Settings and Recents visually checked at normal size after reboot. BOOP visually checked after reboot and wake with its chunky layout intact.
- Confirmed sleep transition (`mWakefulness=Asleep`, display OFF) and wake transition (Awake, display ON); same mode, density, font scale and empty saved overrides afterward.

## Launcher policy

No BOOP code or visual-size preferences changed for this correction, and no new APK was needed. Existing `BoopTvChrome.computeTargetDensity` maps display height to a 675dp viewport: 256 app-local DPI at 1080p, preserving configured apparent button/artwork/text sizes. BOOP v417 matches the standard system mode through this existing adaptive sizing.

Do not shrink BOOP's deliberately chunky layout. Do not change font_scale from 1.3. Do not restore `wm size 3839x2160`, global 640-DPI guards, wake-time display forcing or the extended-mode developer option to repair UI sizing. Let NVIDIA manage system UI geometry and keep launcher styling local to BOOP.

Validation covered one reboot and one sleep/wake cycle. Future HDMI reconnection or additional firmware behavior remains a distinct test; do not claim exhaustive hardware acceptance.
