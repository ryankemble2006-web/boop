# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched by this APK)
- Version: 2 / `0.2.0-banner-grab`
- Green build head: `24ec94fdaa92329091139d55348b189f27fee0f2`
- Workflow: `34221846770` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10054091966`
- APK SHA-256: `7037151fb32cf75b618853199b66ca20872ca1b5f86a7bd4e3b9bc90df260824`
- Artifact ZIP SHA-256: `772612e8e2dec627306487942303375b9de2f480a4b7ae02f06d18813b664f18`
- Permanent BOOP signer reused and verified

This remains intentionally standalone for Shield testing before any later AIO merge. Ryan described the 0.1 presentation as nice and chunky; 0.2 physical behavior is pending.

0.2 changes: HOME favourites prefer Android TV banners with icon fallback; long-press enters grabbed reorder mode; Left/Right can move the grabbed item across the whole row; Select drops/persists; Back cancels/restores. The top-right Settings control opens real Android/Shield Settings. Launcher-specific optional row controls remain separately under `Home rows`.

Default HOME remains favourites-first. Optional Play Next/app-content rows are independently OFF by default. Ads, sponsored content, Shop and Discover have no provider. Disabled rows do not instantiate/fetch providers.

LOCKED: **remove the crap, preserve Shield behavior.** Physical acceptance must preserve double-tap Home -> Recent Apps/task switcher, Back, volume/CEC, Nvidia/Android Settings, system remote shortcuts, app switching and system animations. Fix only what real hardware breaks.

CI verified focused launcher tests, signed build, package/version, HOME/Leanback categories, APK integrity and signer. Physical/visual acceptance of 0.2 is pending Ryan on real Shield hardware. Do not merge into unified until Ryan explicitly accepts the standalone behavior.
