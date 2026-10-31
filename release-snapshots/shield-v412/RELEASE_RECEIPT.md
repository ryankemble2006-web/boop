# BOOP Shield v412 Release Receipt

- **Shield Version**: `1.2.412-shield` (`versionCode=412`)
- **Wall Version**: `1.2.222-wall` (`versionCode=222`)
- **Highlights**:
  1. **Instant SofaBaton X1S (`Sofabaton 0D30`) Auto-Reconnect on Boot & Wake**: Automatically checks `InputManager` when BOOP Home starts, resumes, or wakes from sleep (`ACTION_SCREEN_ON`) and fires `button.x1s_hub_home` via Home Assistant (with a 4s verification retry) so the Bluetooth HID remote reconnects in ~1s without waiting 15 seconds.
  2. **Self-Healing Remote Keymap Accessibility Service (`BoopHomeOverrideService`)**: `ShieldSelfGrant.isHomeAccessibilityEnabled()` now verifies `ShieldHomeOverrideService.isServiceBound()` directly and automatically re-binds `BoopHomeOverrideService` and `BoopCastVisibilityService` on `onCreate()`, `onNewIntent()`, `onResume()`, and `ACTION_SCREEN_ON`.
  3. **Self-Healing Native 4K (`3839x2160`) + Chunky `640` DPI**: Automatically enforces `wm size 3839x2160; wm density 640` via BOOP's local loopback `AdbWire` (`127.0.0.1:5555`) whenever NVIDIA's boot init resets density to `320` or HDMI hotplug resets size to `1920x1080`.
  4. **True Physical Display Metrics in `BoopTvChrome`**: Reads `WindowManager.getDefaultDisplay().getRealMetrics()` in `computeTargetDensity()` and `applyToResources()` and runs `super.onConfigurationChanged()` before `BoopTvChrome.applyShieldMetrics()` so live resolution switches never rebuild the 4K launcher at `256` DPI.
