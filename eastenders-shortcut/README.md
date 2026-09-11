# EastEnders 1.5 — personal Android TV shortcut

Opens official BBC iPlayer directly to EastEnders. With its optional accessibility helper enabled, presses the programme page's focused **Watch now** button once. It follows the selected iPlayer profile's choice rather than forcing the latest episode.

## Install and use

1. Sideload `EastEnders-1.5.apk` onto the Shield and open the EastEnders tile.
2. To automate Watch now, enable **EastEnders auto-play** in Shield **Settings â†’ Device Preferences â†’ Accessibility** (the exact Settings nesting can vary). The shortcut's **Enable auto-play** button also opens Accessibility settings. Press Back from iPlayer to reach the shortcut's recovery screen if needed.
3. Open the EastEnders tile. If iPlayer asks who's watching, select your profile. Once the EastEnders page appears with Watch now focused, the helper clicks it once.

The helper expires after two minutes and cancels when another app takes the foreground, or when you return to the shortcut. It never chooses a profile or confirms playback warnings. If iPlayer changes its accessible title/button, use the remote to press Watch now manually.

Without the helper, the tile still opens the programme page automatically. It cannot press controls inside another app without the accessibility permission.

If no installed iPlayer accepts the link, the shortcut opens the EastEnders website in an installed browser. If iPlayer accepts but silently ignores the link, press Back to the shortcut and choose **Open in browser**. Android does not report such a silent rejection to the launching application. The Shield inspected for this build had Android's browser placeholder, but no usable browser; the app reports this rather than sending you into that placeholder. Website playback depends on the browser's capabilities and BBC availability.

## Version 1.5 artwork update — 11 September 2026

- Launcher behaviour is unchanged from 1.4; this release changes the Android TV banner and square app icon only.
- The 16:9 banner uses the user-supplied EastEnders map artwork with its existing title centred for Shield Home.
- The square icon is a direct crop of the same supplied source, not regenerated artwork.
- Artwork source supplied by the user: `https://image.tmdb.org/t/p/original/rzfia5PauRCY0NezLhGBpzUjDTy.jpg`.
- The build now packages supplied artwork resources instead of redrawing the old placeholder, and a build regression checks that preparation leaves those resources unchanged.
- This repository records provenance only; it does not claim BBC/TMDB endorsement or ownership of the supplied programme artwork.

## Verified on 10 September 2026

- Physical Shield: installed official Play Store package `com.nvidia.bbciplayer`, `versionName=nvidia 1.8.0`, `versionCode=137`, target SDK 35. Installer was `com.android.vending`. iPlayer was not modified or replaced.
- Its manifest exposes `external.androidtv.psbwrapper.deeplinking.DeepLinkActivity` for `https://www.live.bbctvapps.co.uk` and the BBC off-product catalogue host.
- Its link handler preserves the `deeplink` parameter and sends it to the official BBC TV application.
- BBC's current TV app `42.7.0.0` public link builder provides the programme route `tv/programmes/<programme ID>`.
- Physical Shield test reached the EastEnders programme page, with **Watch now** focused, using:
  `https://www.live.bbctvapps.co.uk/tap/telly/iplayer?deeplink=tv/programmes/b006m86d`
- APK uses the equivalent URL-encoded query value `tv%2Fprogrammes%2Fb006m86d`.
- Physical accessibility inspection confirmed a visible exact `EastEnders` title and an enabled, focused, clickable `Watch now` node.
- Passing the series ID to the playback route produced BBC error `02004`; this invalid route is not shipped.
- Deezer was force-stopped on the Shield at the user's request before continuing verification.
- APK built with Android 36 SDK tools and JDK 17. APK signature verification passed v1/v2/v3. Minimum Android 6 / API 23; target API 35. Contains TV launcher banner and no required touchscreen.
- Plain Java checks passed: Shield package preference, rejected-link fallback, no-handler failure, stable programme URL, click arming, matching title/focus, once-only action, timeout, cancellation, and abort-before-first-player-observation.
- Final APK installed and launched on Android TV emulator. Remote focus verified. Real Android accessibility tests with clearly isolated test fixtures verified: no unarmed click, one armed click, no repeated click, repeat launcher use, no click for a different programme, cancellation after Home. Browser fixture received the exact EastEnders web URL. Missing-handler screen verified. Fixtures removed and emulator accessibility setting restored afterward.
- Independent source review completed; identified cancellation issue was corrected and regression tested.
- **Installed on the physical Shield with the user's approval.** EastEnders auto-play is enabled and bound; existing accessibility helpers were preserved. A Shield-specific WebView compatibility fix replaced text search with bounded traversal of accessible child controls. On 10 September at 12:14:22, the helper found the EastEnders title and logged `Watch now click accepted: true` after profile selection. iPlayer's audio player (UID 10082) entered `started` at 12:14:24; its media session was active. This establishes an actual Shield auto-click/playback test, not user physical acceptance of the overall experience.
- The first Shield tests did not auto-click. The installed and packaged final build includes the verified accessibility traversal fix. The UI inspector was avoided during the successful run; only screenshots and application status logs were used.

## Scope

Package: `uk.local.eastenders`. No ads, network permission, microphone, storage permission, credentials, analytics, or downloaded episode list. The helper reads the active screen only during an armed launch and records no screen contents. It subscribes to events from the known iPlayer packages. Version 1.5 uses the user-supplied programme artwork documented above for launcher presentation; no BBC application assets are bundled.

The Shield-specific package/route is physically verified. Alternative package routes are conditional fallbacks only and were not tested against other iPlayer editions.

## Source and rebuild

Run `build.ps1` in PowerShell with JDK 17 and Android SDK platform 36/build tools 36.0.0 installed. Override SDK location using `-Sdk`. No Gradle or third-party dependencies are needed. The script runs the Java tests, verifies the supplied icon/banner survive build preparation unchanged, builds, signs, and verifies the APK.

The source archive excludes generated build files and signing secrets. The original signing key is retained locally with this project for future updates. Building a fresh source copy generates a new key; such a build cannot update an existing installation signed by the original key.

## Public source references

- BBC Android listing: https://play.google.com/store/apps/details?id=bbc.iplayer.android
- EastEnders website fallback: https://www.bbc.co.uk/iplayer/episodes/b006m86d/eastenders
- BBC TV application: https://www.live.bbctvapps.co.uk/tap/telly/iplayer
- BBC TV route definitions used for verification: https://interactive-tv.files.bbci.co.uk/telly/42.7.0.0/assets/Main-legacy2.js

Installed package inspection, programme landing screen, and accessibility controls were checked directly on the connected Shield; generic online package examples were not assumed to apply to it.
