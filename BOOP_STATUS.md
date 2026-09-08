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

## 0.7 Shield Accessibility route repair

0.7 changes only the settings doorway for the existing no-ADB Accessibility override experiment.

An internal `ShieldAccessibilityRouteActivity` catches BOOP's Accessibility-settings request and forwards it directly into Shield/Android TV Settings, trying the TV Accessibility activities first and `com.android.tv.settings.MainSettings` as recovery fallback. It is invisible, internal, no-history and excluded from Recents.

The Accessibility override service remains intentionally narrow: window-state events only, no screen-content retrieval, gestures, typing or key filtering. BOOP still does not intercept Home/KEYCODE_HOME.

For 0.7 testing, **leave Android TV Home enabled and do not Force stop it**. Its foreground event is the trigger for the override.

Regression/release evidence:
- `34233903841` RED on missing direct Shield Accessibility route;
- `34234490629` RED on missing invisible router;
- `34234692956` exposed only a JVM Android-stub test issue after production compiled;
- `34234924895` clean RED on four deliberately missing JVM-safe route helpers;
- `34235253856` green after production helper fix;
- final `34235512348` green end-to-end on version 7 release head.

Default launcher surface remains favourites-first. Banners, physically working grab/reorder, Back handling, real top-right Settings, optional-row defaults and the no-ad/Shop/Discover contract are preserved.

LOCKED: **remove the crap, preserve Shield behavior.** Double-tap Home -> Recent Apps/task switcher, volume/CEC, system Settings, Back semantics, system shortcuts, app switching and animations remain physical acceptance requirements.

0.7 is CI/signer green. **Physical acceptance starts with one thing: does Open Accessibility now reach real Shield Accessibility/Settings without the old resolver error?** Only after Ryan can enable `BOOP Home Override` can takeover speed, reboot behavior and double-tap Home be evaluated.
