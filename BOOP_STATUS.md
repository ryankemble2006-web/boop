# BOOP Launcher Alpha 2 status

## Current

Physically confirmed baseline:
- pure-black fullscreen HOME;
- persistent Pixel status-bar clock removed;
- swipe up opens All Apps;
- swipe down from the top of All Apps returns HOME.

`0.3.0` widget/page plumbing was not accepted because the HOME long-press menu was unreachable. `0.3.1` fixed the menu and added Launcher page-0 swipe right -> BOOP Wall.

Ryan physically confirmed that reversing the cross-app slide geometry did not change the visible result. `0.3.3` still produced the same unwanted white swish on the Pixel.

`0.3.4` / code `11` replaces that animation strategy rather than reversing it again. Wall v34 is unchanged.

Launcher now uses a black-safe handoff:
- Android 14+ activity OPEN/CLOSE transitions disabled;
- pure-black Launcher window background;
- pure-black Android 12+ splash background;
- window preview disabled;
- old directional transition XML removed;
- Launcher -> Wall: 110 ms Launcher-content fade to black, then no-animation Wall launch and zero pending transition;
- Launcher appearance: 140 ms content fade in from black.

Application/source head before documentation: `61606736fea597e86c37c9c9a5259fdb54617abb`.
GitHub Actions run: `34089318255`.
Signed artifact ID: `10006292363`.
APK SHA-256: `412cd7162e6c7562a93ce62534559ab255bf6f5b040145941c586c743b4474e9`.
Existing permanent BOOP signing identity unchanged.

## Verification

Run `34089318255` build job passed:
- black-safe transition source regression;
- unit tests;
- Android lint;
- permanent-signer release build;
- signed artifact upload.

The first Android 16 smoke attempt installed and cold-launched the signed APK successfully, then failed because UIAutomator returned a null root node while dumping the HOME hierarchy. That prevented the menu/swipe assertions from running; the failure log did not show a Launcher fatal crash. The smoke was explicitly retried, so check its latest state before reporting full smoke-green status.

This build verifies the new source strategy and build/sign integrity. It does NOT establish that Pixel has stopped drawing the white swish. Ryan's physical observation decides that.

## `0.3.4` behavior retained

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

Primary `0.3.4` check: with the same Wall v34 installed, does the new black fade/cut handoff remove the white swish in both directions?

Widget move/resize/remove, real third-party widget behavior, and page spill/persistence still need Ryan's on-device acceptance.

## Remaining polish

Drawer motion is still not full Launcher3 direct-finger/spring physics. Third-party widget rotation/process-death quirks remain physical-test territory.

## Protect

- Preserve `boop-launcher-alpha1` as historical fallback until Alpha 2 is fully accepted.
- Keep package `com.boop.launcher` and existing BOOP signing identity.
- Do not publish signing keys/private certificates.
- Keep Wall and Shield app lineages independent.
- CI/build-green and physically accepted are separate states.
