# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch for this standalone experiment: `boop-shield-clean-launcher`.

## Product boundary

This clean Nvidia Shield HOME replacement is a **standalone APK for testing**, not an update to the in-progress unified/AIO BOOP app. Merge into unified only after Ryan explicitly accepts the standalone launcher on real Shield hardware.

Standalone Android package: `com.boop.shieldhome`.
Unified/AIO package remains separately `com.boop.alpha1` and is not replaced by this APK.
Stock Nvidia/Android TV launcher remains installed as recovery.

## Current standalone candidate

Build head: `821369cd2e51f82922b8f7cfbc71aa3edb93fb0e`.
Version: 3 / `0.3.0-input-routing`.
Workflow: `34223973168` SUCCESS.
Artifact: `BOOP-Shield-Clean-Launcher`, ID `10054934429`.
APK SHA-256: `5c38cdb2b56397cf55e7d2f889e5b5c66b5f2719f556cb7087c3eea5fe2ee3e8`.
Artifact ZIP SHA-256: `e7ab325772bc1240eb7d38ba0cd86411d565267a8c315b773578272910355a8a`.
Permanent BOOP signer was reused and verified against the established signer digest.

CI verified the focused Shield HOME suite, signed standalone assembly, package `com.boop.shieldhome`, version 3 / `0.3.0-input-routing`, `ShieldLauncherActivity`, HOME and Leanback categories, APK integrity and permanent signer. No emulator/device/visual acceptance was performed by GitHub.

## Latest physical evidence

Ryan physically tested 0.2 on a real Shield:

- Android TV banners are good and should be preserved.
- Long-pressing a favourite visually entered grab behavior, but pressing Right only moved normal navigation/focus one position. The actual favourite/icon did **not** reorder. Treat 0.2 grab movement as a physical FAIL.
- Ryan requested Shield-style Back behavior: **single Back returns focus to favourite item 1; press-and-hold Back opens real Shield/Android Settings**.

This evidence supersedes the earlier assumption that the 0.2 per-card key listener would own D-pad movement.

## 0.3 input-routing repair

TDD evidence:

- `BackPressGestureTest` RED at commit `bb566bf538bde32b0de20c0e29af8111c0ce70cf`, workflow `34223227200`: compile failed only because `BackPressGesture` did not exist.
- Pure Back gesture controller added in `d604ee1d786af8314d7cfc11447effcdaa33e75c`; focused Shield HOME test gate then passed.
- `ShieldInputRoutingTest` RED at commit `50d35bbdbbb5f31c37cd519de5ef9540e45742d0`, workflow `34223430669`: 37 tests ran, exactly 3 failed, all expected missing parent input-routing/reset methods.
- Production input routing landed through `b4833a4cedcc02081feeb8f3764fbe5f361c071e` and `bd0b1c3f78d55a0f693865750d73af19d586cc1d`. Workflow `34223747577` then passed tests, signer, signed assembly, package checks and artifact upload before the release version bump.

0.3 behavior:

- Grabbed favourite D-pad handling now lives at `ShieldHomeView.dispatchKeyEvent()` rather than on each card. While grabbed, Left/Right are consumed before Android focus navigation and move the grabbed component through the row; Up/Down cannot escape the row.
- Repeated centre-button events from the long-press that initiated the grab are swallowed, preventing an immediate pick-up/drop cycle. Select/Enter after the hold drops and persists.
- Single Back is owned at Activity dispatch. On HOME it cancels any transient grab, restores its pre-grab order, scrolls to the start and focuses favourite item 1. From Apps/Home rows it returns to HOME focused on favourite item 1.
- Long Back is timed at Activity level using the platform long-press timeout. Once the hold fires it suppresses short-Back behavior and opens real `Settings.ACTION_SETTINGS`.
- The top-right Settings control still opens real Shield/Android Settings.
- HOME favourites continue to prefer Android TV banners with normal-icon fallback. Apps catalogue still uses icon cards.

These changes do not alter Android global animation scales, volume/CEC, Home-key handling or system remote shortcuts.

## LOCKED Shield muscle-memory contract

**Remove the crap, preserve Shield behavior.** This launcher replaces the HOME surface, not Shield OS behavior.

Physical acceptance must preserve at least: double-tap Home -> Recent Apps/task switcher; single Back -> favourite item 1; long Back -> real Shield Settings; volume/CEC; Nvidia/Android Settings; system remote shortcuts; app switching; and system animations. If a shortcut breaks, repair that narrow break rather than globally intercepting unrelated remote keys or reimplementing Shield OS.

## Next physical test

1. Update/install standalone `com.boop.shieldhome` 0.3 and confirm AIO `com.boop.alpha1` remains untouched.
2. Confirm the already-liked TV banners remain unchanged.
3. Long-press a favourite, release centre, press Left/Right several times and confirm the **tile itself** moves rather than focus simply walking away. Select to drop; return HOME and confirm order persisted.
4. Start another grab and press single Back. Confirm grab is cancelled/restored and focus lands on favourite item 1.
5. From ordinary HOME navigation, press single Back and confirm focus returns to favourite item 1.
6. Press-and-hold Back and confirm real Shield/Android Settings opens, with no short-Back action firing on release.
7. Recheck double-tap Home -> Recent Apps/task switcher, volume/CEC, other system shortcuts and app switching.
8. Confirm the top-right Settings button still opens real Settings and `Home rows` remains separate.
9. Repeatedly return HOME and confirm no cumulative shrinking; confirm stock launcher remains selectable.

CI-green is not physical acceptance. Do not merge this launcher into unified until Ryan explicitly approves the standalone behavior.
