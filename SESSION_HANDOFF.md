# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch: `boop-shield-clean-launcher`.

## Product boundary

This remains a **standalone Nvidia Shield clean-HOME experiment** for physical testing. It is not part of the in-progress unified/AIO APK yet.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate and untouched)
- Stock Android TV Home stays installed and enabled as recovery and as the Accessibility override trigger.
- Normal users must not need ADB, developer options, a laptop, root or Shizuku.
- Merge into AIO only after Ryan explicitly accepts the standalone behavior on real Shield hardware.

## Current candidate: 0.8 reboot re-arm

- Candidate build head: `af8ebe1147bd56cc952b874c2e4180bd6a44d15d`
- Version: 8 / `0.8.0-reboot-rearm`
- Workflow: `34239594403` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10061456035`
- APK SHA-256: `7088b4be9dca7cb47bd67c740aaea940d71fd2471da623fb3f0c9fe23d5b2ff0`
- Artifact ZIP SHA-256: `fb9dde5f313ca8345057da26f9ff6ce7a08d43caf9d20d334eb1684ff020d9cb`
- Permanent BOOP signer reused and verified.
- 49 focused Shield HOME tests passed.
- Signed assembly, exact package/version, HOME/Leanback categories, Accessibility service/router, signer, APK integrity and artifact upload all passed.

CI-green is not physical acceptance.

## Physical evidence from 0.7

Ryan physically confirmed on a real Nvidia Shield:

- Android TV banners are good and must be preserved.
- Long-press grab/reorder physically works (earlier confirmation: "the booger moved :)").
- Manual Shield Accessibility setup successfully exposes and enables **BOOP Home Override**.
- With BOOP Home Override enabled, **single Home launches BOOP Shield Home**.
- With BOOP Home Override enabled, **double-press Home still opens native Nvidia/Shield Recent Apps**. This is a HARD locked acceptance behavior and must never be intercepted or replaced.
- Reboot exposed the remaining fault: the Shield returned to stock Android TV Home.
- Navigating back to Accessibility and toggling BOOP Home Override off then on immediately restored BOOP takeover.

Therefore the Accessibility override mechanism itself is physically working, but **0.7 reboot persistence/re-arm is a physical FAIL**.

## Root cause and 0.8 repair

0.7 `ShieldHomeOverrideService` only reacted to future `TYPE_WINDOW_STATE_CHANGED` events from stock Android TV Home. It had no `onServiceConnected()` re-arm path.

On reboot, Shield can present stock Home before Android finishes binding the already-enabled Accessibility service. If that stock-Home event happens first, BOOP misses the event and remains behind stock Home. Re-toggling Accessibility causes fresh service/window lifecycle activity, explaining why BOOP immediately takes over again.

0.8 makes one bounded change:

- `ShieldHomeOverrideService.onServiceConnected()` now immediately brings `ShieldLauncherActivity` forward when Android binds/rebinds the enabled Accessibility service.
- The existing 350 ms relaunch guard is retained to suppress immediate duplicate window-event launches.
- Normal stock-Home window events still drive later single-Home takeover.
- No BOOT_COMPLETED receiver was added.
- No new permission was added.
- No Home-key interception was added.
- Double-Home/Recent Apps code is untouched.

TDD evidence:

- RED commit `e8c9adef39a067b07cd70d75534aebe960b300b9`, workflow `34239129796`: 49 tests ran, exactly one failed because `ShieldHomeOverrideService.onServiceConnected()` was absent.
- Production repair commit `5ac04b0bc519f5f2235129c5c40c75ba3eddca46`, workflow `34239319902`: full launcher suite and signed build green.
- Version promotion `20a38183af0ca66edd0e29799c6af84e6581cfac`.
- Final 0.8 certification head `af8ebe1147bd56cc952b874c2e4180bd6a44d15d`, workflow `34239594403`: green end-to-end.

## Setup reality on this Shield firmware

The built-in BOOP Accessibility shortcut currently reaches normal Shield Settings, not the exact Accessibility submenu. Do not keep guessing OEM activity class names. For this standalone test, the known working manual path is Shield Settings -> Accessibility -> Services -> **BOOP Home Override**.

Once enabled, leave stock Android TV Home installed and enabled. Do not Force stop it during normal testing.

## Existing behavior to preserve

- HOME favourites prefer Android TV banners with normal icon fallback.
- Long-press favourite enters grabbed mode; Left/Right moves the grabbed tile across the row; Select drops/persists.
- Single Back returns to HOME/favourite item 1 and cancels/restores an active grab.
- Long Back opens real Shield/Android Settings.
- Top-right Settings opens real Shield/Android Settings.
- Optional Play Next/app-content rows remain independently OFF by default and disabled providers do not instantiate.
- No ad, Shop, Discover or sponsored provider exists.
- Do not alter Android global animation scales.

## LOCKED Shield contract

**Remove the crap, preserve Shield behavior.**

Physically confirmed and now locked:
- single Home -> BOOP while override is alive;
- double Home -> native Recent Apps/task switcher;
- banners;
- grab/reorder.

Still required before standalone approval:
- 0.8 must retake BOOP automatically after reboot without Accessibility off/on;
- report whether stock Home visibly flashes during boot or single-Home takeover;
- recheck Back shortcuts, volume/CEC and other system shortcuts;
- disabling BOOP Home Override must cleanly restore ordinary stock Shield Home.

## Next physical test

1. Update 0.7 to standalone 0.8. AIO must remain untouched.
2. Leave **BOOP Home Override enabled** and leave Android TV Home enabled.
3. Confirm single Home -> BOOP and double Home -> native Recent Apps still work.
4. Reboot the Shield **without touching the Accessibility toggle**.
5. Report whether BOOP takes over automatically after boot, and whether stock Home is visible for a flash/delay first.
6. If reboot succeeds, recheck Back, long-Back Settings, volume/CEC, and then disable BOOP Home Override once to prove stock-Home recovery remains simple.

Do not merge into unified until Ryan explicitly approves the standalone behavior on real Shield hardware.
