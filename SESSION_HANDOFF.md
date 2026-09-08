# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch for this standalone experiment: `boop-shield-clean-launcher`.

## Product boundary

This clean Nvidia Shield HOME replacement remains a **standalone APK for physical testing**, not an update to the in-progress unified/AIO BOOP app. Merge into unified only after Ryan explicitly accepts the standalone launcher on real Shield hardware.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate and untouched)
- Stock Nvidia/Android TV launcher remains installed as the emergency recovery path.
- Normal users must not need ADB, developer options, a laptop, root, Shizuku or hidden APIs.

## Current candidate

- Candidate build head: `c4a78ece4000131695af07739b2f0af434f44bdc`
- Version: 6 / `0.6.0-accessibility-home-override`
- Workflow: `34231043787` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10057824777`
- APK SHA-256: `2d921ea7c14e91eaaefe8337bed0e580c098a730fb73b0822b036fb28045ad8c`
- Artifact ZIP SHA-256: `3d14b2783f918bd3c06c240b53c14b9a9e5ab5ede85e2c57788740f2b646d811`
- Permanent BOOP signer reused and verified.
- Focused Shield HOME tests, signed assembly, exact package/version, HOME/Leanback categories, Accessibility service manifest entry, signer, APK integrity and artifact upload all passed on the candidate head.

CI-green is not physical acceptance.

## Physical evidence

Ryan physically confirmed on a real Shield:

- Android TV banners are good and must be preserved.
- 0.2 grab movement failed because normal focus navigation won.
- 0.3 parent-level input-routing repair succeeded: the grabbed favourite actually moved (Ryan: "the booger moved :)"). Treat grab/reorder movement as physically working at this checkpoint.
- 0.4 HOME retirement/default persistence **failed**. `Retire Android TV Home` opened package `com.google.android.tungsten.setupwraith`; after reboot the original Shield UI was still HOME.
- 0.5 corrected the retirement target to the real Android TV Home package `com.google.android.tvlauncher`. Its App Info exposed **Force stop only, no Disable button**. Force stop removed stock Home temporarily, then pressing Home did nothing. Reboot restored the original launcher. Treat 0.5 normal-HOME replacement as a physical FAIL.

The 0.5 result proves this Shield firmware keeps Android TV Home as the persisted/preferred HOME and does not expose a normal consumer Disable route. Do not describe the Android HOME chooser/RoleManager path as sufficient on this firmware.

## 0.6 no-ADB Accessibility HOME override experiment

0.6 deliberately changes mechanism instead of pretending another HOME chooser attempt will work.

- BOOP registers `ShieldHomeOverrideService` as a minimal Android Accessibility service.
- The service listens only for `TYPE_WINDOW_STATE_CHANGED` and reacts only when the foreground package is stock Android TV Home (`com.google.android.tvlauncher`, with legacy Leanback launcher compatibility).
- When stock Home surfaces, the service immediately brings `ShieldLauncherActivity` to the foreground.
- It does **not** retrieve screen/window content, type text, perform gestures, or filter/intercept remote keys.
- BOOP does **not** intercept `KEYCODE_HOME`. This is deliberate because double-tap Home -> Recent Apps/task switcher is a locked Shield muscle-memory requirement.
- `Home rows` shows `BOOP Home Override: ON/OFF`. Selecting it opens Android Accessibility Settings, where the user enables `BOOP Home Override` once.
- First launch presents one plain setup dialog with `Open Accessibility` / `Not now`; it does not automatically launch the broken HOME chooser.
- `Android TV Home info` still opens the real stock launcher's App Info for recovery/inspection.
- `Restore Shield Home` routes to Accessibility Settings so the override can be switched off; stock Home remains installed and enabled.

**Important for 0.6 testing:** leave Android TV Home enabled and do not Force stop it. Its foreground window is the trigger that tells the Accessibility service to swap BOOP in front. This is not a privileged package-disable mechanism and must not be described as one.

The product-quality question is now physical: does the switch happen quickly enough that stock Home is effectively invisible, and does double-tap Home still open Recent Apps? If stock visibly flashes, Home feels delayed, or system shortcuts regress, this route fails the "uncannily native" bar and must not be merged into AIO.

## Regression / implementation evidence

Earlier target-repair evidence retained:

- `HomeReplacementPolicyTest` commit `addf85e2ac37ee9f291b10074f1389fc5c426847`, workflow `34227581884`: RED on SetupWraith/resolved-HOME policy APIs.
- Policy fix `cf6c91a158615801266515598114c15251bb57a1`: SetupWraith rejected; real Android TV Home preferred.
- `HomeReplacementUiContractTest` commit `df11fb0d323d3e636d21dcda9725c5b69f39fb89`, workflow `34227880751`: RED on resolved-HOME/fresh-prompt contract.
- 0.5 activity repair `957f4ae55e6331324ed2c3a6c3347771575b8294`; final 0.5 workflow `34228609323` passed CI but then failed physical HOME persistence as recorded above.

0.6 TDD / implementation:

- `HomeOverridePolicyTest` / Accessibility setup contracts landed through `3bc2a418f6c29fcb1a2c7810131a177d289f6931` and `5fcdd4b278503d696063d99c1223d4459e1ccfb7`.
- RED workflow `34230246946` failed exactly on missing `HomeOverridePolicy`, `ShieldHomeOverrideService`, and Accessibility settings hooks.
- Production policy/service/manifest/config/settings/activity landed through `db5853bc37ee0d0fcb3ea97677092b8353a20348`, `7b34b08114d1a0ca34808a9132d074b3eafc778e`, `9c9a91e8c8d56f0167437e1ed7e4e4b6c1345b42`, `6427073081a9b7fe0a9479a4a4efe5979ca4bb08`, and `7991857e2e53a568a16027058ac2b470ea88c0d1`.
- Feature-head workflow `34230792465` passed end-to-end before the release version bump.
- Final 0.6 release workflow `34231043787` passed end-to-end on `c4a78ece4000131695af07739b2f0af434f44bdc`.

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

1. Update/install standalone 0.6 over 0.5. Confirm AIO `com.boop.alpha1` remains untouched.
2. **Do not Force stop Android TV Home. Leave it enabled.**
3. When BOOP shows `Use BOOP as Shield Home`, choose `Open Accessibility`.
4. In Accessibility Settings enable **BOOP Home Override**, then return to BOOP. `Home rows` should report `BOOP Home Override: ON`.
5. Open another app and press Home once. Confirm BOOP appears. Report whether Android TV Home is visibly seen even for a flash before BOOP arrives.
6. Double-tap Home and confirm Shield Recent Apps/task switcher still appears. This is a HARD acceptance requirement.
7. Reboot. Confirm BOOP takes over after boot and report any visible stock-Home flash or delay.
8. Confirm single Back -> favourite item 1, long Back -> real Settings, volume/CEC and other system shortcuts.
9. Confirm banners and physically working grab/reorder remain unchanged.
10. Disable `BOOP Home Override` in Accessibility Settings and confirm ordinary stock Shield Home returns, proving emergency recovery remains simple.

Do not merge into unified until Ryan explicitly approves the standalone behavior on real Shield hardware.
