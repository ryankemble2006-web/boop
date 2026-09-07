# BOOP Launcher Alpha 2

Independent native Android Home app (`com.boop.launcher`), Android 10+ / API 29+, target/compile SDK 36, Java 17. No Internet or microphone permission. Alpha 2 is a clean launcher baseline inspired by modern Pixel Launcher interaction patterns without copying Google proprietary launcher code or assets.

Build with Gradle 9.6.0 and Android SDK 36:

```text
gradle -p launcher :app:testDebugUnitTest :app:lintDebug :app:assembleRelease
```

Release signing uses the existing permanent BOOP GitHub identity through `BOOP_KEYSTORE_PATH`, `BOOP_KEYSTORE_PASSWORD`, `BOOP_KEY_ALIAS`, and `BOOP_KEY_PASSWORD`. Do not substitute another signing key.

## Alpha 2 baseline

The home surface is deliberately pure black and edge-to-edge. There is no permanent clock, At a Glance panel, Google search pill, dock/hotseat, plus button, return-strip overlay, or other launcher furniture.

Swipe upward on empty home to open All Apps. All Apps uses native installed-app icons and labels on black. Tap launches an app. Hold an app in All Apps to add it to Home. Search is contextual: tap the small search affordance to expose local app search; the search field is not permanently present.

Placed app icons persist in the Alpha 2 workspace store. Tap launches. Hold to pick up, then drag to reposition. Releasing in the top removal band removes the item; overlapping drops revert. Alpha 1 workspace preferences are intentionally cleared once on first Alpha 2 start rather than forcing the old schema/interaction architecture into the rebuild.

Home/Back state is explicit: Search backs out to All Apps, All Apps backs out to Home, and Android Home returns the launcher to Home. Android 13+ uses `OnBackInvokedDispatcher`; the legacy Back override remains only as the older-Android fallback.

## Known baseline limits

This signed build is the first physical-feel baseline, not the finished Launcher3-quality target.

- Drawer open/close currently uses a restrained threshold/settle transition rather than full Launcher3 direct-finger spring physics. Pixel 10 Pro XL testing decides the next motion pass.
- Widget selection/configuration plumbing allocates and cleans host IDs, but widget views are not yet rendered/movable/resizable on the Alpha 2 workspace. Do not call widget support complete in this build.
- Multiple dynamic workspace pages, folders, notification dots, icon packs, work-profile customization, backup/restore, BOOP return-strip/voice controls and Home Assistant-specific launcher behavior are not part of this baseline.

## Verified signed build

CI run `34077665229` at source commit `844301bfe264a52b202787e8224fb43452dd3ff6` passed unit tests, Android lint, release compilation and permanent BOOP signing.

Signed artifact: `BOOP-Launcher-Alpha2-signed`, artifact ID `10002643933`.

APK: `BOOP-Launcher-Alpha2.apk`

APK SHA-256: `89a5cd50f97dd87e513d86d79bfae71b5f962d86f760190c57527a92fd289b96`

Package metadata: `com.boop.launcher`, versionCode `3`, versionName `0.2.0`, minSdk `29`, targetSdk `36`.

The APK verifies with one signer using APK Signature Scheme v2. The signer certificate SHA-256 is `c0f4549b7d367f7823a76ef32468f5ef7695e3a3380b2145d5a94ff3b1aa9e61`.

CI-green is not physical acceptance. The first Pixel 10 Pro XL pass should judge the empty black canvas, native icon scale/spacing, swipe-up drawer feel, contextual search, app pin/move/remove, Home/Back behavior and whether the system gesture area visually disappears into the black canvas.
