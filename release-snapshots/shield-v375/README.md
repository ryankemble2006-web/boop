# BOOP Shield v375

This versioned snapshot contains the complete source project for the BOOP Shield
package installed on the NVIDIA Shield TV.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.375-shield` (`versionCode 375`)
- On a fresh Shield Home launch, quietly opens Deezer on the existing offscreen display if Deezer has no active media session. It sends no playback command and skips explicit Flow and Shield Settings launches.
- Deezer Track Mix: focus the current track title to show the selected orange
  highlight. Selecting it sends Deezer's native `TRACK_MIX&&<track-id>` media ID
  through its active media session while BOOP stays onscreen.
- The lyrics screen title uses the same focused Track Mix action.
- Lyrics lookup includes a DPAD-controlled keyboard for title and artist edits.
  The active field stays highlighted and the dialog suppresses the system IME,
  so typing does not prompt for the phone keyboard.
- The locally built APK is sent through the GitHub signing handoff, which uses
  the existing BOOP development key stored in GitHub secrets and returns the
  signed installable APK with signature verification evidence.
- Visual verification follows installation of the signed APK on the physical Shield.
- Exact Home Assistant area names target every exposed light in that area;
  exact named light groups are also supported before the selected room is applied.

The workflow also rejects source changes unless the Shield version code is
advanced by exactly one from the latest archived Shield snapshot.
