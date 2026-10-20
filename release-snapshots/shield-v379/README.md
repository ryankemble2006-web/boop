# BOOP Shield v379

This versioned snapshot contains the complete source project for the BOOP Shield
package installed on the NVIDIA Shield TV.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.379-shield` (`versionCode 379`)
- Fix background Deezer Flow reliability and seamless shortcut triggering:
  - Eliminate premature virtual display teardown during active playback and warm-up in BackgroundDeezerFlow. Keeping the virtual display alive prevents Android ActivityTaskManager from removing the task and assassinating the Deezer process / foreground audio service.
  - Wire virtual display release strictly to session death, explicit media closure, or startup failure.
  - DeezerFlowShortcutActivity directly triggers ShieldNowPlayingManager.playDeezerFlow() without launching or foregrounding ShieldLauncherActivity, allowing hold-Home Deezer Flow triggering from any app (Kodi, YouTube, screensaver, etc.) completely in the background without pulling the user back to Home.
- The locally built APK is sent through the GitHub signing handoff, which uses
  the existing BOOP development key stored in GitHub secrets and returns the
  signed installable APK with signature verification evidence.
- Visual verification follows installation of the signed APK on the physical Shield.
- Exact Home Assistant area names target every exposed light in that area;
  exact named light groups are also supported before the selected room is applied.

The workflow also rejects source changes unless the Shield version code is
advanced by exactly one from the latest archived Shield snapshot.
