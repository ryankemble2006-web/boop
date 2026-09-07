# BOOP Launcher Alpha 2 status

## Current

Physically confirmed baseline:
- pure-black fullscreen HOME;
- persistent Pixel status-bar clock removed;
- swipe up opens All Apps;
- swipe down from the top of All Apps returns HOME.

Ryan physically rejected `0.3.0` as incomplete because holding the empty HOME screen produced no menu, leaving the widget path unreachable.

`0.3.1` / code `8` fixes the HOME long-press menu and adds the approved page-0 swipe-right return to BOOP Wall.

Application source: `9f49ccfdc23b8dddb8a0173e1f369c79dc051b96`.
GitHub Actions run: `34084595483` — success.
Signed artifact ID: `10004790724`.
APK SHA-256: `294206b2ff3f50c7c0f880b952f454980582bb7970bda47430036bfb4fe86d88`.
Existing permanent BOOP signing identity unchanged.

## Red -> green evidence

Red run `34084147192` reproduced the real complaint: launcher launched and survived, but a 900 ms hold on empty HOME did not expose `Add widget` in the Android UI hierarchy.

Green run `34084595483` passes:
- unit tests and Android lint;
- permanent-signer release build;
- signed APK install and launcher survival on Android 16;
- 900 ms empty-HOME hold -> visible `Add widget` menu entry;
- dismiss menu -> right swipe from HOME -> BOOP Wall handoff path fires;
- BOOP launcher fatal-crash scan.

## 0.3.1 behavior

- HOME menu is positioned from a tiny temporary anchor at the actual hold point instead of anchoring a PopupMenu to the whole fullscreen workspace.
- The widget/page functionality introduced in 0.3.0 remains intact underneath that now-reachable menu.
- Right swipe from Launcher page 0 requests the launch intent for `com.boop.alpha1` and brings BOOP Wall forward.
- If Wall is not installed, Launcher remains open with a plain-English message.
- Right swipe from later content pages still navigates back toward page 0, preserving dynamic page navigation.

## Widget/page fundamentals present

- Real `AppWidgetHostView` rendering.
- Widget pick/bind/configure/cancel cleanup.
- Persisted pending widget flow and stale/orphan cleanup.
- Widget move, resize, remove and persisted size/position/page.
- Dynamic content pages with automatic creation/compaction.
- No permanent page chrome.

## Physical acceptance pending for 0.3.1

Ryan should now verify:
- hold empty HOME -> menu appears;
- Add widget opens Android picker/config and a real widget renders;
- widget move/resize/remove and persistence;
- page spill/navigation;
- Launcher page 0 swipe right -> BOOP Wall;
- Wall swipe left -> Launcher;
- existing fullscreen and drawer gestures still feel unchanged.

## Remaining polish

Drawer motion is still not full Launcher3 direct-finger/spring physics. Third-party widget rotation/process-death quirks remain physical-test territory.

## Protect

- Preserve `boop-launcher-alpha1` as historical fallback until Alpha 2 is fully accepted.
- Keep package `com.boop.launcher` and existing BOOP signing identity.
- Do not publish signing keys/private certificates.
- Keep Wall and Shield app lineages independent.
- CI-green and physically accepted are separate states.
