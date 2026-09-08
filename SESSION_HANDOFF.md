# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch: `boop-shield-clean-launcher`.

## Product boundary

This remains a **standalone Nvidia Shield clean-HOME experiment** for physical testing. It is not part of the in-progress unified/AIO APK yet.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate and untouched)
- Stock Android TV Home stays installed and enabled as recovery and as the Accessibility override trigger.
- Normal users must not need ADB, developer options, a laptop, root or Shizuku.
- Merge into AIO only after Ryan explicitly accepts the standalone behavior on real Shield hardware.

## Protected physically-green HOME replacement checkpoint: 0.8

- Build head: `af8ebe1147bd56cc952b874c2e4180bd6a44d15d`
- Version: 8 / `0.8.0-reboot-rearm`
- Workflow: `34239594403` SUCCESS
- Artifact ID: `10061456035`
- APK SHA-256: `7088b4be9dca7cb47bd67c740aaea940d71fd2471da623fb3f0c9fe23d5b2ff0`
- Artifact ZIP SHA-256: `fb9dde5f313ca8345057da26f9ff6ce7a08d43caf9d20d334eb1684ff020d9cb`
- Permanent BOOP signer reused and verified.

Ryan physically confirmed on a real Nvidia Shield:

- Android TV banners are good and must be preserved.
- Long-press grab/reorder works ("the booger moved :)").
- Manual Shield Accessibility setup exposes and enables **BOOP Home Override**.
- Single Home launches BOOP Shield Home.
- Double-press Home still opens native Nvidia/Shield Recent Apps. This is HARD locked and must never be intercepted or replaced.
- 0.7 reboot persistence failed, but 0.8 repaired the Accessibility service reconnect race.
- On 0.8 after reboot, **BOOP Home Override remained ON**, **the original Android TV Home did not launch/reclaim the screen**, and BOOP returned automatically without an Accessibility off/on ritual.

Ryan's acceptance message for the core objective: **"we beat it :)"**.

Therefore 0.8 remains the protected physical checkpoint for the HOME replacement mechanism. Later visual polish must not change the Accessibility override, reboot re-arm or native double-Home behavior.

## Current visual candidate: 0.9 floating cards

0.9 changes only app-card chrome around the protected 0.8 mechanism.

- Candidate build head: `79919976adebf5f989a0efd86bef525b6273ed44`
- Version: 9 / `0.9.0-floating-cards`
- Workflow: `34244270100` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10063361724`
- APK SHA-256: `7abd913b51329a3c2cef556fa853bfbb8a78007b046d131a12b639daa9bd589b`
- Artifact ZIP SHA-256: `481f893058ff36b48aa9dec4069d4a3e6dc069486963768ad0cd01d1528ddd1b`
- Permanent BOOP signer reused and verified.
- Full focused Shield HOME suite passed (49 tests).
- Signed assembly, exact package/version, HOME/Leanback categories, Accessibility service/router, signer, APK integrity and artifact upload passed.

**0.9 is CI/signer green only. Its visual appearance is not physically accepted until Ryan tests it on the real Shield.**

### Approved 0.9 visual contract

- HOME favourites keep the real installed Android TV **wide banners**, using the existing banner-first lookup and icon fallback.
- App drawer keeps the real installed app **square icons**.
- BOOP does not recolour, tint or replace installed artwork.
- Idle app/favourite cards have **no charcoal backing plate**, giving the artwork a floating look.
- Focused, selected or grabbed cards restore the existing dark rounded backing plate.
- Existing focus scale remains `1.08`; grabbed scale remains `1.14`; focus duration remains 120 ms.
- The same shared card chrome drives HOME and the Apps drawer, so the focus treatment stays consistent while their artwork aspect ratios remain different.
- Background remains **pure black** for 0.9. Background-provider work is separate and deferred.
- The protected 0.8 Accessibility HOME override, reboot re-arm and native double-Home/Recent Apps behavior were not changed.

### 0.9 TDD / implementation evidence

- RED contract commit `7895d6417a705329cf803af293efc111cb8d1bdc`, workflow `34243722284`: production compiled, unit-test compilation failed only because `AppCardChromePolicy` did not yet exist.
- Policy commit `d7837b96f83aad780ce80651af4c8784e426e760`.
- Shared-card implementation commit `f54101cc05189f63409685032d06a9a417f1cd28`; intermediate workflow `34243916604` passed end-to-end.
- Version promotion `eac3cb867fe9b975d9abfb670e5764a9c020cd7c`.
- The intermediate version-bump run used the still-v8 verifier, so tests/build passed and its expected old identity check rejected v9. This was superseded by the corrected final certification workflow and is not a production regression.
- Final 0.9 certification head `79919976adebf5f989a0efd86bef525b6273ed44`, workflow `34244270100`: green end-to-end.

## Background / Ambient Mode boundary

0.9 deliberately keeps BOOP black. The Google imagery already present on Shield is the device's Ambient Mode/screensaver system, not a supported general launcher-wallpaper feed. BOOP can leave the existing Shield/Google screensaver behavior alone after inactivity while separately owning the HOME surface. Do not add a cloud image dependency or embedded third-party API credential merely to decorate the launcher.

Future background work can separately consider `Black` as the zero-network default plus a user-chosen local image. Online-provider selection needs its own explicit design/approval.

## Setup reality on this Shield firmware

The built-in BOOP Accessibility shortcut reaches normal Shield Settings rather than the exact Accessibility submenu. Do not keep guessing OEM activity class names.

Known working first-time setup:

**Shield Settings -> Accessibility -> Services -> BOOP Home Override -> ON**

Once enabled, 0.8 proved it persists across reboot without retoggle. Stock Android TV Home remains installed/enabled as recovery and as the override trigger.

## Existing behavior to preserve

- Long-press favourite enters grabbed mode; Left/Right moves the grabbed tile across the row; Select drops/persists.
- Single Back returns to HOME/favourite item 1 and cancels/restores an active grab.
- Long Back opens real Shield/Android Settings.
- Top-right Settings opens real Shield/Android Settings.
- Optional Play Next/app-content rows remain independently OFF by default and disabled providers do not instantiate.
- No ad, Shop, Discover or sponsored provider exists.
- Do not alter Android global animation scales.

## LOCKED Shield contract

**Remove the crap, preserve Shield behavior.**

Physically confirmed and locked from 0.8:
- BOOP Home Override remains enabled across reboot;
- stock Android TV Home does not reclaim the screen after reboot;
- single Home -> BOOP;
- double Home -> native Recent Apps/task switcher;
- banners;
- grab/reorder.

## Next physical test: 0.9

1. Update 0.8 to standalone 0.9. AIO `com.boop.alpha1` remains untouched.
2. HOME: confirm idle wide banners float with no dark card backing.
3. Move focus: the dark rounded plate should appear only behind the focused banner and disappear from the old one; supplied banner colours/artwork must be unchanged.
4. Grab a favourite: existing drag/reorder must still work and the grabbed card should keep its stronger emphasized state.
5. Apps drawer: confirm square installed icons float when idle and the selected/focused icon gets the same dark rounded plate; icon colours must be unchanged.
6. Confirm single Home -> BOOP and double Home -> native Recent Apps.
7. Reboot once and confirm BOOP Home Override remains ON and BOOP still wins startup.

Do not merge into unified until Ryan explicitly approves the standalone behavior on real Shield hardware.
