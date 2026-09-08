# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched)
- Candidate version: 5 / `0.5.0-home-target-fix`
- Candidate build head: `c30f09e45ee4c029ec84ac3717f3bbba1d289e9b`
- Workflow: `34228609323` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10056836704`
- APK SHA-256: `d055d65222968fb894a11746f3723eb00a04fb9380e89b75251a8c4d4f763019`
- Artifact ZIP SHA-256: `7e1bf68d919a5aabcb0f713a12fdd81d84a93ec7678f7fce3af0cce8657b195f`
- Permanent BOOP signer reused and verified

This remains a standalone Shield experiment pending later AIO merge only after explicit real-device acceptance.

## Physical evidence

- Android TV banner presentation physically liked; preserve.
- 0.2 grab movement physically failed.
- 0.3 parent-level input-routing repair physically succeeded: Ryan confirmed the grabbed favourite moved ("the booger moved :)").
- 0.4 HOME retirement/default persistence physically **failed**: Retire opened `com.google.android.tungsten.setupwraith`, and reboot still returned to the original Shield UI.

## 0.5 repair

`com.google.android.tungsten.setupwraith` is treated as TV Setup/provisioning and is never a retirement target. Known Android TV Home `com.google.android.tvlauncher` is preferred when appropriate; the actually resolved eligible stock HOME wins when Android exposes it, and disabled stock HOME remains recoverable.

BOOP no longer trusts `RoleManager.isRoleHeld()` as proof that it is the persisted launcher. Actual `MAIN + CATEGORY_HOME` resolution is the source of truth. `Make BOOP my Home` now prefers Android's explicit HOME chooser on Shield. The setup prompt key was advanced to `home_prompt_shown_v2` so users upgrading from broken 0.4 get a fresh selection attempt.

Regression evidence:
- workflow `34227581884` RED on SetupWraith/resolved-HOME policy APIs;
- workflow `34227880751` RED on fresh-prompt/resolved-HOME activity contract;
- workflow `34228336811` green on the production repair before version bump;
- final workflow `34228609323` green through tests, signing, signed build, exact package/version, HOME/Leanback, APK integrity and artifact upload.

Default HOME remains favourites-first. Banners, physically working grab/reorder, Back handling, real top-right Settings, optional-row defaults and the no-ad/Shop/Discover contract are preserved.

LOCKED: **remove the crap, preserve Shield behavior.** Double-tap Home -> Recent Apps/task switcher, volume/CEC, system Settings, Back semantics, system shortcuts, app switching and animations remain physical acceptance requirements.

0.5 is CI/signer green, **physical HOME chooser/retire/reboot acceptance pending Ryan on a real Shield**. Retire must point to `com.google.android.tvlauncher` on the current Shield and never SetupWraith.
