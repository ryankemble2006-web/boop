# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch: `boop-shield-clean-launcher`.

## Product boundary

This remains a **standalone Nvidia Shield clean-HOME experiment** for physical testing. It is not part of the in-progress unified/AIO APK yet.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate and untouched)
- Stock Android TV Home stays installed and enabled as recovery and as the Accessibility override trigger.
- Normal users must not need ADB, developer options, a laptop, root or Shizuku.
- Merge into AIO only after Ryan explicitly accepts the standalone behavior on real Shield hardware.

## Current physically-green HOME replacement checkpoint: 0.8

- Candidate build head: `af8ebe1147bd56cc952b874c2e4180bd6a44d15d`
- Version: 8 / `0.8.0-reboot-rearm`
- Workflow: `34239594403` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10061456035`
- APK SHA-256: `7088b4be9dca7cb47bd67c740aaea940d71fd2471da623fb3f0c9fe23d5b2ff0`
- Artifact ZIP SHA-256: `fb9dde5f313ca8345057da26f9ff6ce7a08d43caf9d20d334eb1684ff020d9cb`
- Permanent BOOP signer reused and verified.
- 49 focused Shield HOME tests passed.
- Signed assembly, exact package/version, HOME/Leanback categories, Accessibility service/router, signer, APK integrity and artifact upload all passed.

## Physical evidence

Ryan physically confirmed on a real Nvidia Shield:

- Android TV banners are good and must be preserved.
- Long-press grab/reorder physically works (earlier confirmation: "the booger moved :)").
- Manual Shield Accessibility setup successfully exposes and enables **BOOP Home Override**.
- With BOOP Home Override enabled, **single Home launches BOOP Shield Home**.
- With BOOP Home Override enabled, **double-press Home still opens native Nvidia/Shield Recent Apps**. This is a HARD locked acceptance behavior and must never be intercepted or replaced.
- 0.7 reboot persistence failed: Shield returned to stock Android TV Home until BOOP Home Override was toggled off/on.
- 0.8 repairs that reboot race. After updating to 0.8 with BOOP Home Override already enabled, Ryan rebooted the Shield and physically confirmed:
  - **BOOP Home Override still reports ON after reboot**;
  - **the original Android TV Home did not launch**;
  - BOOP remained the effective HOME without any Accessibility off/on ritual.

Ryan's acceptance message for the core objective: **"we beat it :)"**.

Therefore the **core HOME replacement mechanism is physically green on 0.8**: BOOP survives reboot, remains the effective Home surface, and native double-Home Recent Apps is preserved.

## Root cause and 0.8 repair

0.7 `ShieldHomeOverrideService` only reacted to future `TYPE_WINDOW_STATE_CHANGED` events from stock Android TV Home. It had no `onServiceConnected()` re-arm path.

On reboot, Shield could present stock Home before Android finished binding the already-enabled Accessibility service. BOOP then missed the one stock-Home event it needed. Re-toggling Accessibility caused fresh lifecycle/window activity, explaining why BOOP immediately took over again.

0.8 makes one bounded change:

- `ShieldHomeOverrideService.onServiceConnected()` immediately brings `ShieldLauncherActivity` forward whenever Android binds/rebinds the already-enabled Accessibility service.
- The existing 350 ms relaunch guard remains in place to suppress immediate duplicate window-event launches.
- Normal stock-Home window events still drive later single-Home takeover.
- No `BOOT_COMPLETED` receiver was added.
- No new permission was added.
- No Home-key interception was added.
- Double-Home/Recent Apps code is untouched.

TDD evidence:

- RED commit `e8c9adef39a067b07cd70d75534aebe960b300b9`, workflow `34239129796`: 49 tests ran, exactly one failed because `ShieldHomeOverrideService.onServiceConnected()` was absent.
- Production repair commit `5ac04b0bc519f5f2235129c5c40c75ba3eddca46`, workflow `34239319902`: full launcher suite and signed build green.
- Version promotion `20a38183af0ca66edd0e29799c6af84e6581cfac`.
- Final 0.8 certification head `af8ebe1147bd56cc952b874c2e4180bd6a44d15d`, workflow `34239594403`: green end-to-end.

## Setup reality on this Shield firmware

The built-in BOOP Accessibility shortcut currently reaches normal Shield Settings, not the exact Accessibility submenu. Do not keep guessing OEM activity class names.

Known working first-time setup on this Shield:

**Shield Settings -> Accessibility -> Services -> BOOP Home Override -> ON**

Once enabled, it persists across reboot on 0.8 and no retoggle is required. Stock Android TV Home remains installed/enabled as recovery and as the override trigger.

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
- BOOP Home Override remains enabled across reboot on 0.8;
- stock Android TV Home does not reclaim the screen after reboot;
- single Home -> BOOP;
- double Home -> native Recent Apps/task switcher;
- banners;
- grab/reorder.

Still worth checking before final standalone-to-AIO merge approval:
- single Back -> favourite item 1 and long Back -> real Settings;
- top-right Settings -> real Shield Settings;
- volume/CEC and other native Shield shortcuts;
- switching BOOP Home Override OFF cleanly restores ordinary stock Shield Home;
- any visible stock-Home flash during ordinary single-Home use.

The HOME replacement mechanism itself is physically accepted at this checkpoint. Do not regress it when polishing or later merging into AIO.
