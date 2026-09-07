# BOOP Launcher Alpha 2 status

## Current

Physically confirmed baseline:
- pure-black fullscreen HOME;
- persistent Pixel status-bar clock removed;
- swipe up opens All Apps;
- swipe down from the top of All Apps returns HOME.

`0.3.0` widget/page plumbing was not accepted because the HOME long-press menu was unreachable. `0.3.1` fixed the menu and added Launcher page-0 swipe right -> BOOP Wall; its CI interaction smoke passed.

Ryan then physically reported the Wall/Launcher cross-app page motion still looked the same in both directions. Launcher `0.3.2` used enter-from-right/exit-left; Wall v34 later tried caller-owned transition control, but the visible result remained unchanged.

`0.3.3` / code `10` is the explicit opposite Launcher-only experiment. Wall v34 is unchanged.

Launcher OPEN geometry now is:
- enter Launcher from LEFT: `-100%p -> 0`;
- previous activity exits RIGHT: `0 -> 100%p`.

Application/source test head: `86a3971d173887c2b641f95d175e5fd30426b59f`.
GitHub Actions run: `34088140401`.
Signed artifact ID: `10005926195`.
APK SHA-256: `359dd04b3268d16d88feac1ebfcb9030ed48057feed2c06d9df81f7334599310`.
Existing permanent BOOP signing identity unchanged.

## Verification

The first 0.3.3 run failed because the old regression test still asserted 0.3.2's direction. Production compilation succeeded. The test was updated to assert the newly requested reverse geometry.

Run `34088140401` passed:
- reversed transition-direction regression;
- unit tests;
- Android lint;
- permanent-signer release build;
- signed artifact upload.

This verifies source geometry and build/sign integrity. It does NOT establish that Pixel visually renders the cross-app transition in the expected direction. Ryan's physical observation decides that.

## 0.3.3 behavior retained

- Pure-black fullscreen HOME.
- Swipe up HOME -> All Apps.
- Swipe down All Apps -> HOME.
- Empty-HOME long press menu and widget entry point.
- Launcher page-0 swipe right -> BOOP Wall.
- Dynamic Launcher content pages.
- Existing widget rendering/persistence/editing code.
- No change to BOOP Wall v34.

## Widget/page fundamentals present

- Real `AppWidgetHostView` rendering.
- Widget pick/bind/configure/cancel cleanup.
- Persisted pending widget flow and stale/orphan cleanup.
- Widget move, resize, remove and persisted size/position/page.
- Dynamic content pages with automatic creation/compaction.
- No permanent page chrome.

## Physical acceptance pending

Primary 0.3.3 check: with the same Wall v34 installed, does Wall swipe-left -> Launcher now visually move opposite to Launcher swipe-right -> Wall?

Widget move/resize/remove, real third-party widget behavior, and page spill/persistence still need Ryan's on-device acceptance.

## Remaining polish

Drawer motion is still not full Launcher3 direct-finger/spring physics. Third-party widget rotation/process-death quirks remain physical-test territory.

## Protect

- Preserve `boop-launcher-alpha1` as historical fallback until Alpha 2 is fully accepted.
- Keep package `com.boop.launcher` and existing BOOP signing identity.
- Do not publish signing keys/private certificates.
- Keep Wall and Shield app lineages independent.
- CI-green and physically accepted are separate states.
