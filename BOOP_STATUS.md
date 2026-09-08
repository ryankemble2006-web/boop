# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched)
- Candidate version: 8 / `0.8.0-reboot-rearm`
- Candidate build head: `af8ebe1147bd56cc952b874c2e4180bd6a44d15d`
- Workflow: `34239594403` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10061456035`
- APK SHA-256: `7088b4be9dca7cb47bd67c740aaea940d71fd2471da623fb3f0c9fe23d5b2ff0`
- Artifact ZIP SHA-256: `fb9dde5f313ca8345057da26f9ff6ce7a08d43caf9d20d334eb1684ff020d9cb`
- Permanent BOOP signer reused and verified
- 49 focused Shield HOME tests green
- Signed build, exact package/version, HOME/Leanback, Accessibility service/router, APK integrity and upload green

This remains a standalone Shield experiment pending later AIO merge only after explicit physical acceptance.

## Physical state

Physically confirmed on 0.7:

- banners good;
- grab/reorder works;
- BOOP Home Override is visible and can be enabled through Shield Accessibility settings;
- **single Home -> BOOP launcher**;
- **double Home -> native Nvidia/Shield Recent Apps** and must remain locked;
- reboot returned to stock Android TV Home;
- toggling BOOP Home Override off then on restored BOOP takeover immediately.

Therefore 0.7 Accessibility takeover is physically working during the session, but **reboot re-arm failed**.

## 0.8 targeted repair

Root cause in source: `ShieldHomeOverrideService` listened only for future stock-Home window-state events and had no `onServiceConnected()` re-arm. At boot the stock Home event can occur before Accessibility finishes binding, so BOOP misses it.

0.8 adds only `onServiceConnected()` re-arm using the existing launcher-forward path and relaunch guard. It adds no boot receiver, no new permission and no Home-key interception. Double-Home/Recent Apps handling is untouched.

Regression evidence:
- `e8c9adef...` / workflow `34239129796`: RED, 49 tests with exactly one failure for missing `onServiceConnected()`;
- `5ac04b0b...` / workflow `34239319902`: repair green through signed artifact;
- final versioned head `af8ebe11...` / workflow `34239594403`: green end-to-end.

## Locked behavior

**Remove the crap, preserve Shield behavior.**

Keep:
- single Home -> BOOP while override enabled;
- double Home -> native Recent Apps/task switcher;
- banners;
- grab/reorder;
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations.

Stock Android TV Home stays installed and enabled as recovery/override trigger. Do not Force stop it during normal testing.

## Next gate

Install/update to 0.8 with BOOP Home Override already enabled, confirm Home/Recent Apps, then reboot **without touching Accessibility**. The key question is whether BOOP automatically retakes the front after Android reconnects the service. Also report any visible stock-Home flash/delay.

0.8 is CI/signer green, **reboot behavior is not physically accepted until Ryan tests it**.
