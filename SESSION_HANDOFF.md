# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch for this standalone experiment: `boop-shield-clean-launcher`.

## Product boundary

This clean Nvidia Shield HOME replacement is a **standalone APK for testing**, not an update to the in-progress unified/AIO BOOP app. Merge into unified only after Ryan explicitly accepts the standalone launcher on real Shield hardware.

Standalone Android package: `com.boop.shieldhome`.
Unified/AIO package remains separately `com.boop.alpha1` and is not replaced by this APK.
Stock Nvidia/Android TV launcher remains installed as recovery.

## Current standalone candidate

Build head: `24ec94fdaa92329091139d55348b189f27fee0f2`.
Version: 2 / `0.2.0-banner-grab`.
Workflow: `34221846770` SUCCESS.
Artifact: `BOOP-Shield-Clean-Launcher`, ID `10054091966`.
APK SHA-256: `7037151fb32cf75b618853199b66ca20872ca1b5f86a7bd4e3b9bc90df260824`.
Artifact ZIP SHA-256: `772612e8e2dec627306487942303375b9de2f480a4b7ae02f06d18813b664f18`.
Permanent BOOP signer is reused and verified against the established signer digest.

CI verified the focused Shield HOME tests, signed standalone assembly, package `com.boop.shieldhome`, version 2 / `0.2.0-banner-grab`, `ShieldLauncherActivity`, HOME and Leanback categories, APK integrity and permanent signer. No emulator/device/visual acceptance was performed by GitHub.

## 0.2 changes

- HOME favourites now prefer the app's Android TV banner. If no banner exists, the normal app icon is used instead.
- Favourite cards are wider/chunkier to suit TV banners. The Apps catalogue keeps normal icon cards.
- Long-press a favourite to grab it. The grabbed card lifts/enlarges and shows a move indicator.
- Release the centre button after grabbing. Use D-pad Left/Right repeatedly to move the same card through any number of positions; the row scrolls to follow it.
- Select/Enter drops the card and persists the new order. Back while grabbed restores the order from before the grab.
- The old one-position Move Left / Move Right HOME menu is no longer the active interaction. Favourite removal remains available from Apps.
- The top-right `Settings` button opens the real Android/Shield Settings via `Settings.ACTION_SETTINGS`.
- Launcher-only optional row controls are separate under `Home rows`; that page still includes the supported Home-app chooser.

Ryan described the 0.1 HOME as "nice and chunky" on real Shield hardware. Treat that as positive presentation evidence for the baseline, not blanket acceptance of 0.2 behavior.

## LOCKED Shield muscle-memory contract

**Remove the crap, preserve Shield behavior.** This launcher replaces the HOME surface, not Shield OS behavior.

Physical acceptance must preserve at least: double-tap Home -> Recent Apps/task switcher; normal Back; volume/CEC; Nvidia/Android Settings; system remote shortcuts; app switching; and system animations. If a shortcut breaks, repair that narrow break rather than globally intercepting remote keys or reimplementing Shield OS.

## Next physical test

1. Update/install the standalone `com.boop.shieldhome` APK. Confirm AIO `com.boop.alpha1` remains untouched.
2. Confirm favourites show TV banners where apps provide them and icon fallback where they do not.
3. Long-press a favourite, release the centre button, move it several positions with Left/Right, then Select to drop it. Reopen HOME and confirm the order persisted.
4. Repeat a grab and press Back; confirm the original order is restored.
5. Open the top-right Settings control and confirm it reaches the real Shield/Android Settings.
6. Verify `Home rows` still controls optional non-ad rows and remembers state.
7. Recheck double-tap Home -> Recent Apps/task switcher, Back, volume/CEC, system shortcuts and app switching.
8. Repeatedly return HOME and confirm no cumulative shrinking.
9. Confirm stock launcher remains selectable.

CI-green is not physical acceptance. Do not merge this launcher into unified until Ryan explicitly approves the standalone behavior.
