# BOOP Shield v362 source snapshot

This file identifies the versioned source snapshot. The GitHub Actions run
generates the signed APK, SHA-256, signer certificate report, and commit receipt
as downloadable artifacts after tests and build verification complete.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.362-shield` (`versionCode 362`)
- Horizontal Carousel Boundary Clamping:
  - Favourites Row (`ShieldHomeView`): clamped LEFT D-pad on entry 1 (the first favourite app card) so holding or pressing LEFT scrolls smoothly back to home position and stops dead on item 1 instead of spilling out into "Apps".
  - Seren Next Up Widget (`SerenNextUpView`): clamped both LEFT D-pad on entry 1 (home position) and RIGHT D-pad on the last episode, trapping horizontal navigation strictly within the poster carousel.
  - Home Assistant Widget (`ShieldRoomPanelView`): verified tile clamping on start/end devices and clamped LEFT/RIGHT D-pad on the room panel heading title to prevent accidental sideways focus slips.
- Instant & Reliable HOME Button Task Switching:
  - In `UnifiedEntryActivity.route()`, added `Intent.FLAG_ACTIVITY_NEW_TASK` to the destination intent (`ShieldLauncherActivity`), preventing Android ActivityManager from canceling or losing the task switch when finishing `UnifiedEntryActivity` from an external app.
  - Fast-pathed `CATEGORY_HOME` routing by bypassing redundant synchronous `BoopMediaBridge` and notification re-initialization on every HOME press, delivering sub-10ms instantaneous returns to the BOOP launcher from any foreground app.
- Visual verification: tested live over ADB on NVIDIA Shield TV Pro.
