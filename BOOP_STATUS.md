# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched)
- Candidate version: 6 / `0.6.0-accessibility-home-override`
- Candidate build head: `c4a78ece4000131695af07739b2f0af434f44bdc`
- Workflow: `34231043787` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10057824777`
- APK SHA-256: `2d921ea7c14e91eaaefe8337bed0e580c098a730fb73b0822b036fb28045ad8c`
- Artifact ZIP SHA-256: `3d14b2783f918bd3c06c240b53c14b9a9e5ab5ede85e2c57788740f2b646d811`
- Permanent BOOP signer reused and verified
- Focused Shield HOME tests, signed build, exact package/version, HOME/Leanback, Accessibility service manifest entry, APK integrity and upload green

This remains a standalone Shield experiment pending later AIO merge only after explicit real-device acceptance.

## Physical evidence

- Android TV banner presentation physically liked; preserve.
- 0.2 grab movement physically failed.
- 0.3 parent-level input-routing repair physically succeeded: Ryan confirmed the grabbed favourite moved ("the booger moved :)").
- 0.4 retirement physically failed by targeting SetupWraith rather than the launcher.
- 0.5 correctly targeted `com.google.android.tvlauncher`, but App Info offered **Force stop only, no Disable**. Force stop caused Home to do nothing; reboot restored stock Home. Therefore normal HOME chooser/RoleManager + App Info retirement is a **physical FAIL** on this Shield firmware.

## 0.6 Accessibility HOME override experiment

0.6 switches mechanism instead of retrying the failed normal-HOME path.

`ShieldHomeOverrideService` is a minimal Accessibility service that listens only for window-state changes. When stock Android TV Home becomes foreground, it brings BOOP Shield Home to the front. It does not retrieve screen content, perform gestures, type text or intercept remote keys.

`Home rows` now shows `BOOP Home Override: ON/OFF`; selecting it opens Android Accessibility Settings. A one-time first-run dialog offers `Open Accessibility`. Stock Android TV Home stays installed **and enabled** because its foreground event is the trigger. Do not Force stop it for 0.6 testing.

BOOP deliberately does not intercept Home/KEYCODE_HOME. Preserving Shield double-tap Home -> Recent Apps/task switcher remains a hard physical acceptance requirement.

Regression evidence:
- workflow `34230246946` RED exactly on missing Accessibility override policy/service/settings hooks;
- feature workflow `34230792465` green after production implementation;
- final versioned workflow `34231043787` green end-to-end on `c4a78ece4000131695af07739b2f0af434f44bdc`.

Default HOME surface remains favourites-first. Banners, physically working grab/reorder, Back handling, real top-right Settings, optional-row defaults and the no-ad/Shop/Discover contract are preserved.

LOCKED: **remove the crap, preserve Shield behavior.** Double-tap Home -> Recent Apps/task switcher, volume/CEC, system Settings, Back semantics, system shortcuts, app switching and animations remain physical acceptance requirements.

0.6 is CI/signer green, **Accessibility takeover behavior is not yet physically accepted**. Key real-device questions are whether stock Home flashes before BOOP, whether reboot takeover is acceptably clean, and whether double-tap Home still opens Recent Apps.
