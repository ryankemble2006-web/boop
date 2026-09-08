# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch for this standalone experiment: `boop-shield-clean-launcher`.

## Product boundary

Ryan corrected the architecture before physical install: this clean Nvidia Shield HOME replacement is a **standalone APK for testing**, not an update to the in-progress unified/AIO BOOP app. It is intended to be merged into unified later only after standalone physical acceptance.

Standalone Android package: `com.boop.shieldhome`.
Unified/AIO package remains separately `com.boop.alpha1` and must not be overwritten by this launcher APK.
Stock Nvidia/Android TV launcher remains installed as recovery.

## Current standalone build

Branch head used for the first green standalone build: `d6e775de68f0f19661736abff6c0432e25320196`.
Version: 1 / `0.1.0-standalone`.
Workflow: `34217924617` SUCCESS.
Artifact: `BOOP-Shield-Clean-Launcher`, ID `10052578743`.
APK SHA-256: `374d86419abbc3aca367f79d2a13e36667ab41e8f975dc3f9b1b12f89ca26c68`.
Artifact ZIP SHA-256: `4530637c0574d394faafe1124eded3cf2febdc6ee9f1ac257a71d236f48f808a`.
Permanent BOOP signer is reused and verified against the existing signer digest.

CI verified the standalone package/version, `ShieldLauncherActivity`, HOME and Leanback intent categories, focused Shield HOME tests, APK integrity and signer. No emulator/device/visual acceptance was performed.

## Launcher behavior

Default HOME is favourite apps only, with Apps and Settings available. Optional Play Next and app content rows are independently OFF by default and disabled providers are not instantiated. No advertising, sponsored, Shop or Discover provider exists.

Remote-first controls support app launch, favourite add/remove/reorder, smooth local focus/page animation, package refresh on relevant package changes, and supported Android HOME selection. Do not alter Android global animation scales.

## LOCKED Shield muscle-memory contract

**Remove the crap, preserve Shield behavior.** This launcher replaces the HOME surface, not Shield OS behavior.

Physical acceptance must preserve at least: double-tap Home -> Recent Apps/task switcher; normal Back; volume/CEC; Nvidia/Android Settings; system remote shortcuts; app switching; and system animations. If a shortcut breaks, repair that narrow break rather than globally intercepting remote keys or reimplementing Shield OS.

## Physical test order

1. Install this standalone APK alongside `com.boop.alpha1` and the stock launcher.
2. Confirm Android sees it as a separate app/package and does not update BOOP unified.
3. Select `BOOP Shield Home` as HOME through Android's supported chooser.
4. Verify single Home returns to the clean favourites screen.
5. Verify double-tap Home still opens Recent Apps/task switcher.
6. Verify Back, volume/CEC, Settings, system shortcuts and app switching.
7. Verify favourites launch/add/remove/reorder and optional rows remember state.
8. Repeatedly return HOME and check no cumulative shrinking.
9. Confirm the stock launcher can be selected again.

CI-green is not physical acceptance. Do not merge this into unified until Ryan explicitly approves the standalone behavior on real Shield hardware.
