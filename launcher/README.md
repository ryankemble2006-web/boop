# BOOP Launcher Alpha 1

Independent native Android Home app (`com.boop.launcher`), Android 10+, SDK 36, Java 17. No changes to BOOP. No Internet or microphone permission.

Build with Gradle 9.6.0 and Android SDK 36:

```
gradle -p launcher :app:testDebugUnitTest :app:lintDebug :app:assembleRelease
```

Release signing requires `BOOP_KEYSTORE_PATH`, `BOOP_KEYSTORE_PASSWORD`, `BOOP_KEY_ALIAS`, `BOOP_KEY_PASSWORD`; the isolated CI workflow supplies the existing release identity. Never ship unsigned or substitute debug signing. APK: `app/build/outputs/apk/release/app-release.apk`.

## Use

Install APK, open BOOP Launcher, choose it under Android Home settings when ready. Start dismisses the introduction. Swipe up from empty home to search installed launcher apps; tap launches, long press adds to the current page and opens edit mode. Long press empty home enters editing. Drag items to empty space; overlapping drops revert. Tap an item in editing for size, page and remove controls. Swipe a dragged item above the home area to remove, or swipe downward on empty canvas to finish. Done also finishes. Swipe left on empty canvas for widgets, right to return; Back closes editing/drawer first and then follows visited pages. Home returns the main canvas. Bail out in editing opens Android Home settings. The optional second widget page can be enabled in the editor.

Choose Add widget in editing for real installed Android widget providers. Android handles binding consent and provider configuration. Canceled binding/configuration releases its host ID. An interrupted pending configuration can be discarded on the next Add widget. Widget state is managed by Android; launcher positions/sizes persist in private preferences. Normalized rectangles preserve bounds and no overlap on rotation. Widgets update their size options as the canvas changes. Widget providers may impose their own content constraints.

Open BOOP is in the introduction and editor. BOOP must already be installed as `com.boop.alpha1`. Optional right-edge return requests Android display-over-apps permission. After granting, return and tap Open BOOP again. A foreground service owns only a 20dp-wide, 180dp-tall strip at the right center. Swipe left from it to return. The service notification provides return/stop controls (allow notifications to expose them). It stops on launcher resume, screen off, or Stop. It stays active if BOOP opens other apps; it does not inspect foreground apps, use a microphone, or read HA configuration. Open without strip works without overlay permission. Android Home is always available.

## Verification

Unit tests cover overlap rejection, edge contact, clamping, deterministic free placement, full-page failure and navigation ordering. CI builds/tests/lints and separately exercises the installed APK in an emulator. Device checks still required: bind/configure a Home Assistant widget, cancel each consent screen, edit/move/resize, rotate and force-stop/reopen, tap widget controls; on Pixel 7 Pro and Pixel 10 Pro XL, validate the BOOP strip and notification controls, voice/touch, permission denial, screen-off removal and Home recovery. Alpha does not support folders, icon packs, work-profile catalogs or backup/restore.
