# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch for this standalone experiment: `boop-shield-clean-launcher`.

## Product boundary

This clean Nvidia Shield HOME replacement remains a **standalone APK for physical testing**, not an update to the in-progress unified/AIO BOOP app. Merge into unified only after Ryan explicitly accepts the standalone launcher on real Shield hardware.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate and untouched)
- Stock Nvidia/Android TV launcher remains installed as the emergency recovery path.
- Normal users must not need ADB, developer options, a laptop, root, Shizuku or hidden APIs.

## Current candidate

- Candidate build head: `c30f09e45ee4c029ec84ac3717f3bbba1d289e9b`
- Version: 5 / `0.5.0-home-target-fix`
- Workflow: `34228609323` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10056836704`
- APK SHA-256: `d055d65222968fb894a11746f3723eb00a04fb9380e89b75251a8c4d4f763019`
- Artifact ZIP SHA-256: `7e1bf68d919a5aabcb0f713a12fdd81d84a93ec7678f7fce3af0cce8657b195f`
- Permanent BOOP signer reused and verified.
- Focused Shield HOME tests, signed assembly, exact package/version, HOME/Leanback categories, signer, APK integrity and artifact upload all passed on the candidate head.

CI-green is not physical acceptance.

## Physical evidence

Ryan physically confirmed on a real Shield:

- Android TV banners are good and must be preserved.
- 0.2 grab movement failed because normal focus navigation won.
- 0.3 parent-level input-routing repair succeeded: the grabbed favourite actually moved (Ryan: "the booger moved :)"). Treat grab/reorder movement as physically working at this checkpoint.
- 0.4 HOME retirement/default persistence **failed**. `Retire Android TV Home` opened package `com.google.android.tungsten.setupwraith`; after reboot the original Shield UI was still HOME.

The 0.4 failure is authoritative physical evidence. Do not describe 0.4 HOME replacement as accepted or merely untested.

## 0.4 root cause

0.4 selected the first competing system HOME-capable package. On Shield, Google TV Setup / provisioning package `com.google.android.tungsten.setupwraith` can appear HOME-capable enough to satisfy that heuristic, even though it is not the normal Android TV launcher.

Verified package identities for the repair:

- `com.google.android.tungsten.setupwraith` = TV Setup / provisioning. Never select as a stock launcher retirement target.
- `com.google.android.tvlauncher` = Android TV Home and the known stock Shield launcher target for this firmware family.
- `com.google.android.apps.tv.launcherx` is also recognized as a known Google TV HOME package for future compatibility.

0.4 also trusted `RoleManager.isRoleHeld(ROLE_HOME)` whenever available. Ryan's reboot result proved that the consumer flow must verify the package Android actually resolves for `MAIN + CATEGORY_HOME`, not assume role bookkeeping means BOOP is the persisted launcher.

## 0.5 HOME target repair

TDD / regression evidence:

- `HomeReplacementPolicyTest` commit `addf85e2ac37ee9f291b10074f1389fc5c426847`, workflow `34227581884`: RED with only the missing resolver-aware policy APIs. The regression literally places SetupWraith ahead of Android TV Home and requires `com.google.android.tvlauncher` to win.
- Policy fix commit `cf6c91a158615801266515598114c15251bb57a1`: rejects setup/provisioning HOME candidates, prefers the actually resolved eligible stock HOME, then known Android/Google TV HOME packages, while keeping a disabled stock launcher discoverable for recovery.
- `HomeReplacementUiContractTest` commit `df11fb0d323d3e636d21dcda9725c5b69f39fb89`, workflow `34227880751`: RED on the new resolved-HOME/fresh-prompt contract.
- Activity fix commit `957f4ae55e6331324ed2c3a6c3347771575b8294`: actual resolved HOME package is now the truth for whether BOOP is default. The setup prompt key is versioned to `home_prompt_shown_v2`, so upgrading from broken 0.4 gets one fresh setup attempt.
- Feature-head workflow `34228336811`: SUCCESS through tests, signing, package verification and artifact upload before release version bump.
- Final 0.5 release workflow `34228609323`: SUCCESS end-to-end on candidate head `c30f09e45ee4c029ec84ac3717f3bbba1d289e9b`.

0.5 behavior:

- On upgrade from 0.4, BOOP gets a fresh one-time HOME setup attempt because the old prompt flag is intentionally superseded.
- `Make BOOP my Home` prefers Android's explicit `Settings.ACTION_HOME_SETTINGS` chooser on Shield, with the platform HOME role request only as fallback.
- BOOP considers itself default only when Android's resolved HOME package is exactly `com.boop.shieldhome`.
- `Retire Android TV Home` dynamically enumerates HOME candidates but rejects SetupWraith/setup/provisioning packages. The resolved eligible stock HOME wins; otherwise known `com.google.android.tvlauncher` / Google TV HOME wins before generic system launchers.
- `Restore Shield Home` uses the same corrected target logic and can still find the stock HOME when it is disabled.
- BOOP still does not programmatically disable another system package. It opens Android's App Info and the user presses the OS-provided Disable/Enable control if Shield exposes it.

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

1. Update/install standalone 0.5 over 0.4. Confirm AIO `com.boop.alpha1` remains untouched.
2. Confirm the fresh Android HOME chooser appears and explicitly select **BOOP Shield Home**.
3. Press Home from another app and confirm it resolves directly to BOOP.
4. Open `Home rows -> Retire Android TV Home`. It must target `com.google.android.tvlauncher` on this Shield, **never `com.google.android.tungsten.setupwraith`**.
5. If Android exposes Disable, disable Android TV Home and return to BOOP.
6. Reboot. Confirm BOOP is HOME after reboot rather than the original Shield UI.
7. Recheck double-tap Home -> Recent Apps/task switcher, volume/CEC, single/long Back and other system shortcuts.
8. Confirm banners and the physically working grab/reorder remain unchanged.
9. Test `Restore Shield Home`: if Android TV Home is disabled, it should open that package's App Info for Enable, then the HOME chooser can restore stock.

Do not merge into unified until Ryan explicitly approves the standalone behavior on real Shield hardware.
