# BOOP Shield v393

This versioned snapshot contains the complete source project for the BOOP Shield
package installed on the NVIDIA Shield TV.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.393-shield` (`versionCode 393`)
- Auto-Scaling Invariant 10-Foot UI Density:
  - Dynamically computes and clamps display metrics and configuration to reference 10-foot scale (height * 160 / 675, yielding 256 DPI at 1080p, 512 DPI at 4K).
  - Synchronizes metrics across `Application.onCreate`, `onConfigurationChanged`, `onActivityPreCreated`, `onActivityResumed`, `attachBaseContext`, and `applyOverrideConfiguration`.
  - Re-applies exact layout dimensions to cards, hero slots, and Seren Next Up across TV wake and HDMI EDID renegotiations.
- Signed exclusively via GitHub Actions with `BOOP_DEV_KEYSTORE_B64`.
