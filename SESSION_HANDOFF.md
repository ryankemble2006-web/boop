# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch for this standalone experiment: `boop-shield-clean-launcher`.

## Product boundary

This clean Nvidia Shield HOME replacement remains a **standalone APK for physical testing**, not an update to the in-progress unified/AIO BOOP app. Merge into unified only after Ryan explicitly accepts the standalone launcher on real Shield hardware.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate and untouched)
- Stock Nvidia/Android TV launcher remains installed as the emergency recovery path.

## Current candidate

- Build head: `2e5dcca8fd2a7c635b0c54faa4b10fb07880f6fd`
- Version: 4 / `0.4.0-home-replacement`
- Workflow: `34226492186` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10055945427`
- APK SHA-256: `64c9c92603a481ffbc5d66df0a5826269823f6e9dbe94889a8294fdaa1d29b4f`
- Artifact ZIP SHA-256: `303ad4f81c1e47558fcf052908a14c0c5392f661201a3097cbbe827e9f26c7ab`
- Permanent BOOP signer reused and verified.
- Focused Shield HOME suite: 42 tests, green. Signed assembly, package/version, HOME/Leanback, signer, APK integrity and artifact upload all green.

CI-green is not physical acceptance.

## Latest physical evidence

Ryan physically confirmed on the real Shield:

- the Android TV banners are good and must be preserved;
- 0.2 grab movement failed because normal focus navigation won;
- after the 0.3 parent-level input-routing repair, **the grabbed favourite actually moved** (Ryan: "the booger moved :)"). Treat grab/reorder movement as physically working at this checkpoint.

Single-Back -> favourite item 1 and long-Back -> real Shield Settings were implemented in 0.3 but are not recorded as physically accepted until Ryan explicitly confirms them.

## 0.4 no-ADB HOME replacement flow

Ryan rejected ADB/developer-option setup for normal users. The locked simplicity rule is now: **install -> Android confirmation -> BOOP is Home**, with recovery available from BOOP itself.

0.4 adds:

- On first ordinary launch, if BOOP is not already the HOME role and the prompt has not already been shown, BOOP asks Android's own `RoleManager.ROLE_HOME` confirmation. It does not repeatedly nag after the first automatic prompt.
- `Home rows` now exposes `Make BOOP my Home`, which reopens the Android-owned HOME-role/chooser flow manually at any time.
- `Retire Android TV Home` dynamically discovers the competing **system HOME launcher** and opens that exact package's Android App Info page. The user can press Android's own **Disable** button if Shield firmware exposes it. BOOP does not use ADB, root, Shizuku, hidden APIs or privileged package-disable permissions.
- `Restore Shield Home` dynamically finds the stock system HOME again, including a disabled package. If it is disabled, BOOP opens its App Info so the user can press **Enable**. If it is enabled, BOOP opens Android's HOME chooser so it can be selected again.
- The standalone manifest explicitly queries HOME candidates so the recovery flow can discover stock HOME components.
- Stock launcher package names are not hard-coded. Recovery selects another system HOME package rather than BOOP itself or an arbitrary third-party launcher.

The intended consumer sequence is: **install/open BOOP -> accept Make BOOP Home -> Home rows -> Retire Android TV Home -> Disable**. After that, BOOP should be the HOME surface and the stock launcher should remain installed but disabled for emergencies. Real Shield firmware must confirm that the stock App Info page actually exposes Disable.

## Existing launcher behavior to preserve

- HOME favourites prefer Android TV banners with normal icon fallback.
- Long-press favourite enters grabbed mode; parent-level key dispatch owns Left/Right before Android focus navigation; Select drops/persists.
- Single Back returns to HOME/favourite item 1 and cancels/restores an active grab.
- Long Back opens real `Settings.ACTION_SETTINGS` and suppresses short Back on release.
- Top-right Settings opens real Shield/Android Settings.
- Optional Play Next/app-content rows remain independently OFF by default and disabled providers do not instantiate.
- No ad, Shop, Discover or sponsored provider exists.
- Do not alter Android global animation scales.

## LOCKED Shield muscle-memory contract

**Remove the crap, preserve Shield behavior.** This launcher replaces the HOME surface, not Shield OS behavior.

Physical acceptance must preserve double-tap Home -> Recent Apps/task switcher, Back semantics above, volume/CEC, Nvidia/Android Settings, system remote shortcuts, app switching and system animations. Do not globally intercept Home or rebuild the Nvidia task switcher.

## Next physical test

1. Update/install standalone 0.4. Confirm `com.boop.alpha1` AIO remains untouched.
2. Confirm the first-run Android HOME confirmation appears (unless BOOP is already default) and choose BOOP.
3. Press Home from several apps and confirm it returns directly to BOOP rather than loading stock Android TV Home first.
4. Open `Home rows -> Retire Android TV Home`. Confirm it opens the real stock launcher App Info. If Android exposes **Disable**, disable it and return to BOOP.
5. Reboot/wake and confirm BOOP remains the HOME surface with stock launcher disabled.
6. Recheck double-tap Home -> Recent Apps/task switcher, volume/CEC and other system shortcuts.
7. Confirm single Back -> favourite item 1 and long Back -> real Settings.
8. Test `Restore Shield Home`: if stock is disabled it should open its App Info for Enable; once enabled, the HOME chooser should allow returning to stock.
9. Confirm the already-liked banners and physically working grab/reorder remain unchanged.

Do not merge into unified until Ryan explicitly approves the standalone behavior on real Shield hardware.
