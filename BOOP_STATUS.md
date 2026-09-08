# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, not replaced by this APK)
- Version: 1 / `0.1.0-standalone`
- Green build head: `d6e775de68f0f19661736abff6c0432e25320196`
- Workflow: `34217924617` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10052578743`
- APK SHA-256: `374d86419abbc3aca367f79d2a13e36667ab41e8f975dc3f9b1b12f89ca26c68`
- Artifact ZIP SHA-256: `4530637c0574d394faafe1124eded3cf2febdc6ee9f1ac257a71d236f48f808a`
- Permanent BOOP signer reused and verified

This is intentionally a standalone Shield HOME experiment to test before later merger into unified. It must install beside the AIO and stock launcher.

Default HOME is favourites only. Apps/Settings remain available. Optional Play Next/app-content rows are independently OFF by default. Ads, sponsored content, Shop and Discover have no provider. Disabled rows do not instantiate/fetch providers.

LOCKED: **remove the crap, preserve Shield behavior.** Physical acceptance must preserve double-tap Home -> Recent Apps/task switcher, Back, volume/CEC, Nvidia/Android Settings, system remote shortcuts, app switching and system animations. Fix only what real hardware breaks.

CI verified focused launcher tests, package/version, HOME/Leanback categories, APK integrity and signer. Physical/visual acceptance pending Ryan on real Shield hardware. Do not merge into unified until Ryan explicitly accepts the standalone behavior.
