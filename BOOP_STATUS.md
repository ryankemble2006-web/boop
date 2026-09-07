# BOOP Launcher Alpha 2 status

## Current

Physically confirmed baseline:
- pure-black fullscreen HOME;
- persistent Pixel status-bar clock removed;
- swipe up opens All Apps;
- swipe down from the top of All Apps returns HOME;
- empty-HOME long press exposes the launcher menu;
- Launcher page-0 swipe right opens BOOP Wall;
- `0.3.4` physically removed the unwanted white cross-app swish.

Physical transition notes:
- `0.3.5` smoothed the handoff, but Ryan reported the Android bars returned during the animated transition.
- Ryan chose to remove both cross-app animations for now and design a matched swoosh later.

`0.3.6` / code `13` is the no-animation bridge candidate. Wall source remains unchanged.

Launcher handoff now:
- no Launcher fade-out or fade-in;
- no destination fade;
- `FLAG_ACTIVITY_NO_ANIMATION` plus `overridePendingTransition(0,0)` for Launcher -> Wall;
- Android 14+ Launcher OPEN/CLOSE activity transitions disabled;
- pure-black Launcher window, preview and Android 12+ splash retained;
- system bars explicitly re-hidden in `onResume()`, `onNewIntent()`, focus return, and immediately before launching Wall;
- Launcher content remains alpha 1 with no cross-app animation.

Application/source head: `bdc88de44290470e958ea6e6b53cad6026dc964e`.
GitHub Actions run: `34091910098`.
Signed artifact ID: `10007159008`.
Existing permanent BOOP signing identity unchanged.

## Verification

Run `34091910098` build job passed:
- no-animation immersive-handoff source regression;
- unit tests;
- Android lint;
- permanent-signer release build;
- signed artifact upload.

Android interaction smoke is separate from physical Pixel acceptance. Ryan's device decides whether the bars are truly gone in the real cross-app handoff.

## `0.3.6` behavior retained

- Pure-black fullscreen HOME.
- Swipe up HOME -> All Apps.
- Swipe down All Apps -> HOME.
- Empty-HOME long press menu and widget entry point.
- Launcher page-0 swipe right -> BOOP Wall.
- Dynamic Launcher content pages.
- Existing widget rendering/persistence/editing code.
- No change to BOOP Wall source.

## Widget/page fundamentals present

- Real `AppWidgetHostView` rendering.
- Widget pick/bind/configure/cancel cleanup.
- Persisted pending widget flow and stale/orphan cleanup.
- Widget move, resize, remove and persisted size/position/page.
- Dynamic content pages with automatic creation/compaction.
- No permanent page chrome.

## Physical acceptance pending

Primary `0.3.6` check: with both cross-app animations removed, do the status/navigation bars remain suppressed again?

If yes, keep this as the stable bridge and design one custom matched swoosh for both directions later.

Widget move/resize/remove, real third-party widget behavior, and page spill/persistence still need Ryan's on-device acceptance.

## Protect

- Preserve `boop-launcher-alpha1` as historical fallback until Alpha 2 is fully accepted.
- Keep package `com.boop.launcher` and existing BOOP signing identity.
- Do not publish signing keys/private certificates.
- Keep Wall and Shield app lineages independent.
- CI/build-green and physically accepted are separate states.
