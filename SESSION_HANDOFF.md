# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch for this standalone experiment: `boop-shield-clean-launcher`.

## Product boundary

This clean Nvidia Shield HOME replacement remains a **standalone APK for physical testing**, not an update to the in-progress unified/AIO BOOP app. Merge into unified only after Ryan explicitly accepts the standalone launcher on real Shield hardware.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate and untouched)
- Stock Nvidia/Android TV launcher remains installed as the emergency recovery path.
- Normal users must not need ADB, developer options, a laptop, root, Shizuku or hidden APIs.

## Current candidate

- Candidate build head: `79f8665dab5135b130af35a52a03d088d6e35b70`
- Version: 7 / `0.7.0-shield-accessibility-route`
- Workflow: `34235512348` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10059749717`
- APK SHA-256: `b56511fc21e6bad5e20cc83b5c79beebaf5c19caa49c6322be915fc5e256d815`
- Artifact ZIP SHA-256: `7925ae9f66ee56904f94d724fd4630502d77d7a083752b4b34ffdde1a6f54f97`
- Permanent BOOP signer reused and verified.
- 48 focused Shield HOME tests passed. Signed assembly, exact package/version, HOME/Leanback categories, Accessibility service, invisible Shield Accessibility router, signer, APK integrity and artifact upload all passed on the candidate head.

CI-green is not blanket physical acceptance.

## Physical evidence

Ryan physically confirmed on a real Shield:

- Android TV banners are good and must be preserved.
- 0.2 grab movement failed because normal focus navigation won.
- 0.3 parent-level input-routing repair succeeded: the grabbed favourite actually moved (Ryan: "the booger moved :)"). Treat grab/reorder movement as physically working at this checkpoint.
- 0.4 HOME retirement/default persistence **failed**. `Retire Android TV Home` opened package `com.google.android.tungsten.setupwraith`; after reboot the original Shield UI was still HOME.
- 0.5 corrected the retirement target to real Android TV Home `com.google.android.tvlauncher`. Its App Info exposed **Force stop only, no Disable button**. Force stop removed stock Home temporarily, pressing Home then did nothing, and reboot restored the original launcher. Treat normal HOME chooser/RoleManager + retirement as a physical FAIL on this Shield firmware.
- 0.6 did **not** reach the Accessibility override test. Selecting `Open Accessibility` produced the Shield message **"you dont have an app that can do this"**, and Ryan stopped. This was a setup-routing failure, not a service failure.
- 0.7 removed that resolver error but its internal router landed on ordinary Shield Settings rather than directly on Accessibility.
- Ryan then manually navigated Shield Settings to Accessibility, enabled the **BOOP Home Override** slider, and physically confirmed the override service works.
- **Single Home now launches BOOP Shield Home.** This is physically working on candidate 0.7 with Accessibility override enabled.
- **Double-press Home still opens Nvidia/Shield Recent Apps.** This locked muscle-memory behavior is physically preserved on candidate 0.7 and must remain unchanged.

The 0.5 result proves this Shield firmware keeps Android TV Home as the persisted/preferred HOME and does not expose a normal consumer Disable route. The 0.6/0.7 setup results prove direct Accessibility deep-linking is firmware-specific. The manual Accessibility path successfully enables the no-ADB override.

## 0.7 Accessibility HOME override: physically working core path

0.7 uses `ShieldHomeOverrideService` as a narrow Accessibility-based HOME override:

- It listens only for `TYPE_WINDOW_STATE_CHANGED` and reacts when stock Android TV Home becomes foreground.
- It does not retrieve screen/window content, type text, perform gestures, or filter/intercept remote keys.
- BOOP does **not** intercept `KEYCODE_HOME`. This is deliberate and is now physically validated by double-press Home -> Recent Apps continuing to work.
- Stock Android TV Home remains installed and enabled as the trigger and emergency recovery path. Do **not** Force stop it during normal override testing.
- The current automatic Accessibility doorway is not good enough yet: BOOP opens regular Shield Settings, then the user must navigate manually to Accessibility and enable BOOP Home Override.

### Physical state of 0.7

**Physically green now:**
- Accessibility service is visible and can be enabled through Shield Settings.
- single Home -> BOOP launcher.
- double Home -> native Shield Recent Apps.
- banners preserved.
- grab/reorder preserved from prior physical acceptance.

**Still pending physical acceptance:**
- whether stock Android TV Home visibly flashes before BOOP on single Home;
- reboot persistence/takeover behavior;
- whether BOOP appears cleanly after boot without unacceptable delay/flash;
- single Back -> favourite item 1 and long Back -> real Settings after override is enabled;
- volume/CEC/system shortcuts after override is enabled;
- turning BOOP Home Override OFF cleanly restores ordinary Shield Home.

Do not call the entire launcher physically accepted until those remaining checks are complete.

## 0.7 TDD / release evidence

- Direct-route RED workflow `34233903841` failed only on the deliberately missing TV Settings route contract.
- Invisible-router RED workflow `34234490629` failed only because `ShieldAccessibilityRouteActivity` did not exist yet.
- Router production + manifest landed through `4ef82287333431a8cd00bbe319adc1eb80487976` and `831193dddbc4b3687325ff770c5a9e769636f7d2`.
- Workflow `34234692956` compiled production but exposed a JVM-only Android `ComponentName` test-stub problem; this was test infrastructure, not a production compile failure.
- JVM-safe route contract commit `31b0c08471c273cc9b601f933caa8264e98c1b19` produced a clean RED with exactly four missing pure-string helper methods.
- Production helper fix `6e0cbf5c007d4352e7de410a197f88b447f368d2`; workflow `34235253856` then passed tests, signing, build, verification and upload before version promotion.
- Final 0.7 release workflow `34235512348` passed end-to-end on candidate head `79f8665dab5135b130af35a52a03d088d6e35b70`.

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

**Remove the crap, preserve Shield behavior.** This launcher replaces the HOME surface experience, not Shield OS behavior.

Physical acceptance must preserve double-tap Home -> Recent Apps/task switcher, Back semantics above, volume/CEC, Nvidia/Android Settings, system remote shortcuts, app switching and system animations. Do not globally intercept Home or rebuild the Nvidia task switcher.

**Double-tap Home -> Recent Apps is now physically confirmed working with BOOP Home Override enabled. Keep it locked.**

## Next physical test

1. Keep standalone 0.7 installed with **BOOP Home Override enabled**. AIO `com.boop.alpha1` remains untouched.
2. Open a normal app and single-press Home. BOOP should appear. Report whether stock Android TV Home is visible even for a flash before BOOP arrives.
3. Double-press Home again occasionally during testing; native Recent Apps must continue to work.
4. Reboot the Shield. Confirm whether BOOP takes over after boot and report any visible stock-Home flash or delay.
5. Recheck single Back -> favourite item 1, long Back -> real Settings, volume/CEC and other system shortcuts.
6. Disable **BOOP Home Override** in Accessibility settings and confirm ordinary stock Shield Home returns, preserving simple emergency recovery.

Do not merge into unified until Ryan explicitly approves the standalone behavior on real Shield hardware.
