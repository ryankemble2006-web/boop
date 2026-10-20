# BOOP Shield v379 source snapshot

This file identifies the versioned source snapshot. The release handoff records
the locally built APK hash, GitHub signature verification, and installed
package version after completion.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.379-shield` (`versionCode 379`)
- Fix background Deezer Flow reliability and seamless shortcut triggering:
  - Eliminate premature virtual display teardown during active playback and warm-up in BackgroundDeezerFlow. Keeping the virtual display alive prevents Android ActivityTaskManager from removing the task and assassinating the Deezer process / foreground audio service.
  - Wire virtual display release strictly to session death, explicit media closure, or startup failure.
  - DeezerFlowShortcutActivity directly triggers ShieldNowPlayingManager.playDeezerFlow() without launching or foregrounding ShieldLauncherActivity, allowing hold-Home Deezer Flow triggering from any app (Kodi, YouTube, screensaver, etc.) completely in the background without pulling the user back to Home.
