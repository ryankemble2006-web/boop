# BOOP Launcher Alpha 2 status

## Current

Physically confirmed baseline:
- pure-black fullscreen HOME;
- persistent Pixel status-bar clock removed;
- swipe up opens All Apps;
- swipe down from the top of All Apps returns HOME;
- empty-HOME long press exposes the launcher menu;
- Launcher page-0 swipe right opens BOOP Wall;
- `0.3.4` physically removed the unwanted white cross-app swish on Ryan's Pixel.

Ryan's physical note on `0.3.4`: the white swish is fixed, but BOOP Wall appears too abruptly after the black handoff.

`0.3.5` / code `12` is the focused polish candidate. Wall v34/source remains unchanged.

Launcher now uses this handoff:
- Android 14+ default Launcher OPEN/CLOSE transitions remain disabled;
- Launcher window, preview and Android 12+ splash remain pure black;
- Launcher fades its own content to black over 120 ms;
- Launcher then starts Wall with `ActivityOptions.makeCustomAnimation(..., android.R.anim.fade_in, 0)`, giving the destination a fade-in instead of an instant cut or directional slide;
- Launcher stays black underneath instead of restoring its content during the destination transition;
- when Launcher resumes/returns, its own content fades in from black over 180 ms;
- no custom directional slide resources are used.

Application/source head: `56633b3c35e0ed0f01df76bb7ea15fae5c1fcde0`.
GitHub Actions run: `34090705961`.
Signed artifact ID: `10006748214`.
APK SHA-256: `3e8515f662cc4e83a5e23384b698b8c79687bd897de02c9ace901fe8e8178fa5`.
Existing permanent BOOP signing identity unchanged.

## Verification

Run `34090705961` build job passed:
- black-safe fade source regression;
- unit tests;
- Android lint;
- permanent-signer release build;
- signed artifact upload.

Android 16 smoke was still running when this status was written. It verifies launcher launch/menu/return-swipe survival, not the subjective Pixel transition appearance. Ryan's physical Pixel observation remains authoritative for whether the new fade feels smooth enough.

## `0.3.5` behavior retained

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

Primary `0.3.5` check: does BOOP Wall now fade in smoothly from the already-accepted black handoff, with no return of the white swish?

Widget move/resize/remove, real third-party widget behavior, and page spill/persistence still need Ryan's on-device acceptance.

## Protect

- Preserve `boop-launcher-alpha1` as historical fallback until Alpha 2 is fully accepted.
- Keep package `com.boop.launcher` and existing BOOP signing identity.
- Do not publish signing keys/private certificates.
- Keep Wall and Shield app lineages independent.
- CI/build-green and physically accepted are separate states.
