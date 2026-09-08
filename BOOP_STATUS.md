# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched)
- Candidate version: 7 / `0.7.0-shield-accessibility-route`
- Candidate build head: `79f8665dab5135b130af35a52a03d088d6e35b70`
- Workflow: `34235512348` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10059749717`
- APK SHA-256: `b56511fc21e6bad5e20cc83b5c79beebaf5c19caa49c6322be915fc5e256d815`
- Artifact ZIP SHA-256: `7925ae9f66ee56904f94d724fd4630502d77d7a083752b4b34ffdde1a6f54f97`
- Permanent BOOP signer reused and verified
- 48 focused Shield HOME tests green
- Signed build, exact package/version, HOME/Leanback, Accessibility service, invisible Shield Accessibility router, APK integrity and upload green

This remains a standalone Shield experiment pending later AIO merge only after explicit real-device acceptance.

## Physical evidence

- Android TV banner presentation physically liked; preserve.
- 0.2 grab movement physically failed.
- 0.3 parent-level input-routing repair physically succeeded: Ryan confirmed the grabbed favourite moved ("the booger moved :)").
- 0.4 retirement physically failed by targeting SetupWraith rather than the launcher.
- 0.5 correctly targeted `com.google.android.tvlauncher`, but App Info offered **Force stop only, no Disable**. Force stop caused Home to do nothing; reboot restored stock Home. Normal HOME chooser/RoleManager + retirement is therefore a physical FAIL on this Shield firmware.
- 0.6 Accessibility **setup routing** physically failed before the override service was enabled: `Open Accessibility` produced **"you dont have an app that can do this"**. The override service itself remains physically untested, not failed.
- 0.7 removes the old resolver failure and opens real Shield Settings, but Ryan physically confirmed it lands on the ordinary top-level Shield Settings screen rather than Accessibility. Direct Accessibility setup is therefore still **not physically achieved**, and the override service remains physically untested. Do not infer which explicit TV Settings component succeeded or redirected without further evidence.

## 0.7 Shield Accessibility route result

0.7 changed only the settings doorway for the existing no-ADB Accessibility override experiment.

An internal `ShieldAccessibilityRouteActivity` catches BOOP's Accessibility-settings request and attempts Shield/Android TV Settings directly. The old resolver error is gone on hardware, proving the request now reaches a real Shield Settings surface, but the candidate does not land directly on Accessibility on Ryan's current Shield firmware.

AOSP Android TV history explains why hard-coded activity names are unreliable here: some TvSettings generations expose Accessibility as a dedicated activity while others attach an Accessibility fragment to the normal Settings hierarchy. Do not keep guessing OEM activity class names.

For the next hardware test, use the native Shield path manually: **Settings -> Device Preferences -> Accessibility -> Services -> BOOP Home Override**. Leave Android TV Home enabled and do not Force stop it. If `BOOP Home Override` is absent from Services, stop and investigate service registration. If it is present, enable it and only then test the takeover mechanism.

The Accessibility override service remains intentionally narrow: window-state events only, no screen-content retrieval, gestures, typing or key filtering. BOOP still does not intercept Home/KEYCODE_HOME.

Regression/release evidence:
- `34233903841` RED on missing direct Shield Accessibility route;
- `34234490629` RED on missing invisible router;
- `34234692956` exposed only a JVM Android-stub test issue after production compiled;
- `34234924895` clean RED on four deliberately missing JVM-safe route helpers;
- `34235253856` green after production helper fix;
- final `34235512348` green end-to-end on version 7 release head.

Default launcher surface remains favourites-first. Banners, physically working grab/reorder, Back handling, real top-right Settings, optional-row defaults and the no-ad/Shop/Discover contract are preserved.

LOCKED: **remove the crap, preserve Shield behavior.** Double-tap Home -> Recent Apps/task switcher, volume/CEC, system Settings, Back semantics, system shortcuts, app switching and animations remain physical acceptance requirements.

0.7 is CI/signer green but **not physically accepted**. The next gate is manual enabling of `BOOP Home Override` through Shield's own Accessibility menu. If that succeeds: open another app, single-press Home and report takeover/stock-Home flash, then double-press Home and confirm native Recent Apps still opens.
