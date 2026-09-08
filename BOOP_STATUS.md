# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched)
- Current physically-green HOME replacement checkpoint: version 8 / `0.8.0-reboot-rearm`
- Build head: `af8ebe1147bd56cc952b874c2e4180bd6a44d15d`
- Workflow: `34239594403` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10061456035`
- APK SHA-256: `7088b4be9dca7cb47bd67c740aaea940d71fd2471da623fb3f0c9fe23d5b2ff0`
- Artifact ZIP SHA-256: `fb9dde5f313ca8345057da26f9ff6ce7a08d43caf9d20d334eb1684ff020d9cb`
- Permanent BOOP signer reused and verified
- 49 focused Shield HOME tests green
- Signed build, exact package/version, HOME/Leanback, Accessibility service/router, APK integrity and upload green

This remains a standalone Shield launcher until Ryan later approves AIO merge.

## Physical state

Physically confirmed on real Shield:

- Android TV banners good;
- grab/reorder works;
- BOOP Home Override is visible and can be enabled through Shield Accessibility settings;
- **single Home -> BOOP launcher**;
- **double Home -> native Nvidia/Shield Recent Apps** and must remain locked;
- 0.7 reboot re-arm failed, but Accessibility off/on restored takeover immediately;
- **0.8 reboot re-arm succeeds**: BOOP Home Override still reports ON after reboot and the original Android TV Home does not reclaim the screen.

Ryan's summary after the 0.8 reboot test: **"we beat it :)"**.

Therefore the **core Shield HOME replacement is physically green on 0.8**. BOOP remains the effective Home surface across reboot without ADB or a repeated Accessibility toggle, while native double-Home Recent Apps survives.

## 0.8 repair

`ShieldHomeOverrideService` now rearms in `onServiceConnected()` so Android reconnecting the already-enabled Accessibility service after boot immediately brings BOOP forward. Existing stock-Home window handling and the 350 ms relaunch guard remain.

No boot receiver, new permission, Home-key interception, global animation setting, or task-switcher replacement was added.

Regression evidence:
- `e8c9adef...` / workflow `34239129796`: RED, 49 tests with exactly one failure for missing `onServiceConnected()`;
- `5ac04b0b...` / workflow `34239319902`: repair green through signed artifact;
- final versioned head `af8ebe11...` / workflow `34239594403`: green end-to-end;
- physical reboot acceptance then confirmed the original Android TV Home did not relaunch as the visible HOME and BOOP Home Override remained ON.

## Locked behavior

**Remove the crap, preserve Shield behavior.**

Physically locked:
- BOOP Home Override persists across reboot;
- stock Android TV Home stays installed but does not reclaim the visible HOME surface;
- single Home -> BOOP;
- double Home -> native Recent Apps/task switcher;
- banners;
- grab/reorder.

Keep unchanged and recheck before final AIO merge:
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations;
- switching BOOP Home Override OFF restores ordinary stock Shield Home;
- any stock-Home flash during normal Home use.

Stock Android TV Home stays installed and enabled as recovery/override trigger. Do not Force stop it during normal use.
