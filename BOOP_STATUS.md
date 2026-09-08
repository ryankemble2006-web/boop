# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged.

## IMPORTANT architecture correction

Ryan clarified that the clean Nvidia Shield HOME replacement is **not part of the AIO yet**. It is a standalone test app to be physically accepted first and merged later.

Standalone launcher branch: `boop-shield-clean-launcher`.
Standalone package: `com.boop.shieldhome`.
Standalone green build: `d6e775de68f0f19661736abff6c0432e25320196`, workflow `34217924617`, artifact `BOOP-Shield-Clean-Launcher` ID `10052578743`, APK SHA-256 `374d86419abbc3aca367f79d2a13e36667ab41e8f975dc3f9b1b12f89ca26c68`.

Do not install the earlier `com.boop.alpha1` Shield-HOME-labelled candidate as a launcher experiment because it updates the AIO. Do not re-route Shield HOME to `com.boop.shieldhome.ShieldLauncherActivity` inside unified until Ryan explicitly approves a later merge.

## AIO restored

The accidental clean-launcher integration was removed from unified materialisation, the unified Shield manifest and unified CI. `ShieldEntryRouteTest` now locks the boundary: Shield HOME and ordinary Shield launches remain on the existing AIO Shield body (`com.boop.shieldoverlay.MainActivity`).

Restored unified verification run `34218173825` completed successfully after the split: integration contracts, materialisation, preserved Launcher lint, Shield controls, wake/routing/assistant tests, permanent signing, APK assembly, package/version/archive verification and artifact upload all passed.

Current concurrent AIO wake-arm work owns version 48 / `1.2.2-unified-wake-arm`. Preserve that lane and any newer live-head changes from other BOOP chats.

## LOCKED standalone Shield launcher contract

**Remove the crap, preserve Shield behavior.** The standalone launcher replaces the HOME surface only. Physical acceptance must preserve double-tap Home -> Recent Apps/task switcher, Back, volume/CEC, Nvidia/Android Settings, system remote shortcuts, app switching and system animations. Fix narrow hardware breaks later rather than globally intercepting keys or reimplementing Shield OS.

## Other protected AIO state

Approved paired black-lidded eyes remain in the unified materialized build path; preserve approved geometry/alpha, iris-only hue, blink timing/gates, headphones/puppetry and five-digit hands. Blink is user-confirmed working. HA names/Home controls were physically accepted earlier and must stay intact. Shield density scaling remains idempotent/non-cumulative. Assistant remote invocation/audio and custom wake acoustic quality remain physical acceptance boundaries.

Ryan owns visual/device acceptance. No screenshots/golden/aesthetic-source checks, emulator device acceptance, automatic installs/grants or signer/package changes. Protected historical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881` until Ryan explicitly promotes a newer physically accepted checkpoint.
