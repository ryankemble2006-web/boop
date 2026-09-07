# BOOP Launcher Alpha 2 status

## Current

Physically confirmed baseline:
- pure-black fullscreen HOME;
- persistent Pixel status-bar clock removed;
- swipe up opens All Apps;
- swipe down from the top of All Apps returns HOME.

`0.3.0` widget/page plumbing was not accepted because the HOME long-press menu was unreachable. `0.3.1` fixed the menu and added Launcher page-0 swipe right -> BOOP Wall; its CI interaction smoke passed.

Ryan physically reported that the cross-app transition direction in `0.3.1` was asymmetric: Launcher -> Wall looked correct, but Wall -> Launcher replayed the same left-to-right page motion instead of mirroring the left swipe.

`0.3.2` / code `9` fixes that direction on the Launcher side only. Wall branches were not modified.

Application source/version: `3d2066d8bafa2ba69be70984ce3f060c79ff3b78`.
GitHub Actions run: `34085524812` — success.
Signed artifact ID: `10005093325`.
APK SHA-256: `83a1ead52fb1ccdce8fb59912101e80a6fd3c98d85c07fc12002c772af0667bb`.
Existing permanent BOOP signing identity unchanged.

## Red -> green evidence

Red run `34085403670` failed the new transition-direction source regression before the production transition/resources were added.

Green run `34085524812` passes:
- transition-direction regression;
- unit tests and Android lint;
- permanent-signer release build;
- signed APK install and launcher survival on Android 16;
- empty-HOME long press -> visible `Add widget`;
- dismiss menu -> Launcher page-0 right swipe -> BOOP Wall handoff path;
- BOOP launcher fatal-crash scan.

CI validates that Launcher OPEN uses a right-to-left mirrored transition (`enter from right`, prior activity `exit left`). The visual feel remains Ryan's physical acceptance check.

## 0.3.2 behavior

- Wall swipe left -> Launcher should now visually travel right-to-left.
- Launcher page-0 swipe right -> Wall keeps the already-good opposite transition behavior.
- No Wall source/branch changes were made.
- Existing fullscreen, drawer, long-press, widget and dynamic-page code remains intact.

## Widget/page fundamentals present

- Real `AppWidgetHostView` rendering.
- Widget pick/bind/configure/cancel cleanup.
- Persisted pending widget flow and stale/orphan cleanup.
- Widget move, resize, remove and persisted size/position/page.
- Dynamic content pages with automatic creation/compaction.
- No permanent page chrome.

## Physical acceptance pending

Primary 0.3.2 check:
- Wall swipe left -> Launcher animation direction;
- Launcher swipe right -> Wall remains the mirrored opposite.

Widget move/resize/remove, real third-party widget behavior, and page spill/persistence still need Ryan's on-device acceptance.

## Remaining polish

Drawer motion is still not full Launcher3 direct-finger/spring physics. Third-party widget rotation/process-death quirks remain physical-test territory.

## Protect

- Preserve `boop-launcher-alpha1` as historical fallback until Alpha 2 is fully accepted.
- Keep package `com.boop.launcher` and existing BOOP signing identity.
- Do not publish signing keys/private certificates.
- Keep Wall and Shield app lineages independent.
- CI-green and physically accepted are separate states.
