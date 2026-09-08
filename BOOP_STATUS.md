# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched)
- Protected physically-green HOME checkpoint: version 8 / `0.8.0-reboot-rearm`
- 0.8 build head: `af8ebe1147bd56cc952b874c2e4180bd6a44d15d`
- 0.8 workflow: `34239594403` SUCCESS
- 0.8 APK SHA-256: `7088b4be9dca7cb47bd67c740aaea940d71fd2471da623fb3f0c9fe23d5b2ff0`
- Current visual candidate: version 9 / `0.9.0-floating-cards`
- 0.9 build head: `79919976adebf5f989a0efd86bef525b6273ed44`
- 0.9 workflow: `34244270100` SUCCESS
- 0.9 artifact ID: `10063361724`
- 0.9 APK SHA-256: `7abd913b51329a3c2cef556fa853bfbb8a78007b046d131a12b639daa9bd589b`
- 0.9 artifact ZIP SHA-256: `481f893058ff36b48aa9dec4069d4a3e6dc069486963768ad0cd01d1528ddd1b`
- Permanent BOOP signer reused and verified
- 49 focused Shield HOME tests green

This remains a standalone Shield launcher until Ryan explicitly approves later AIO merge.

## Physical state: protected 0.8 core

Physically confirmed on real Shield:

- Android TV banners good;
- grab/reorder works;
- BOOP Home Override is visible and can be enabled through Shield Accessibility settings;
- **single Home -> BOOP launcher**;
- **double Home -> native Nvidia/Shield Recent Apps** and must remain locked;
- **0.8 reboot re-arm succeeds**: BOOP Home Override remains ON after reboot and original Android TV Home does not reclaim the screen.

Ryan's summary after the 0.8 reboot test: **"we beat it :)"**.

Therefore the core Shield HOME replacement is physically green on 0.8. Do not alter its Accessibility override, service reconnect re-arm or native Home/Recent Apps behavior during visual polish.

## 0.9 floating-card candidate

0.9 changes only shared app-card chrome:

- HOME keeps real wide Android TV banners with existing banner-first lookup and icon fallback.
- Apps drawer keeps real square installed app icons.
- Artwork is not recoloured/tinted/replaced.
- Idle cards are transparent with no dark backing plate.
- Focused, selected or grabbed cards show the existing dark rounded plate.
- Existing focus/grab scale timing is unchanged.
- Home and Apps share the same chrome policy, but retain their existing wide-vs-square artwork geometry.
- Background remains pure black. Background/provider work is deferred and separate.
- Protected 0.8 HOME override/reboot mechanism is untouched.

TDD/release evidence:
- `7895d641...` / workflow `34243722284`: RED only because `AppCardChromePolicy` was missing.
- `d7837b96...`: pure card-chrome policy added.
- `f54101cc...` / workflow `34243916604`: shared card implementation green end-to-end.
- final versioned `79919976...` / workflow `34244270100`: green end-to-end, exact v9 identity, signer, manifest and artifact checks passed.

**0.9 is CI/signer green, visual physical acceptance pending Ryan's real Shield test. 0.8 remains the protected physical checkpoint until 0.9 is accepted.**

## Background / screensaver boundary

Keep BOOP pure black for 0.9. Shield/Google Ambient Mode remains a separate idle/screensaver layer and does not need to be replaced by this launcher. Local/online launcher-background work is deferred to a separate approved change.

## Locked behavior

**Remove the crap, preserve Shield behavior.**

Keep unchanged:
- single Home -> BOOP;
- double Home -> native Recent Apps/task switcher;
- BOOP Home Override across reboot;
- stock Android TV Home installed/enabled as recovery/trigger;
- banners and grab/reorder;
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations.

## Next gate

Install/update to 0.9 and physically inspect Home + Apps card chrome. Confirm idle artwork floats, focus plate follows selection, wide Home banners and square Apps icons remain correctly shaped and unmodified, grab still works, then recheck single Home, double Home and one reboot.
