# BOOP Shield v376 source snapshot

This file identifies the versioned source snapshot. The release handoff records
the locally built APK hash, GitHub signature verification, and installed
package version after completion.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.376-shield` (`versionCode 376`)
- Automatically scrolls the Home container to reveal the entire Now Playing widget in full view whenever any playback control or element within Now Playing is focused.
- On a fresh Shield Home launch, quietly opens Deezer on the existing offscreen display if Deezer has no active media session. It sends no playback command and skips explicit Flow and Shield Settings launches.
- Deezer Track Mix: focus the current track title to show the selected orange
  highlight. Selecting it sends Deezer's native `TRACK_MIX&&<track-id>` media ID
  through its active session while BOOP stays onscreen.
- The title action is available only when Deezer metadata supplies a validated
  track ID and the live session supports `playFromMediaId`.
- The native dispatch is silent and does not claim that Deezer has finished
  starting Track Mix playback.
- The lyrics screen title exposes that same focus treatment and Track Mix action.
- Lyrics lookup includes a DPAD-controlled keyboard for editing title and
  artist fields. The active field stays highlighted, and the dialog suppresses
  the Android system input method so it does not prompt for the phone keyboard.
- Living Room carousel edge layout:
  - Extends the horizontal scroller across the panel's `18dp` side insets while
    keeping the panel itself as the clipping boundary.
  - Places real `16dp` leading and trailing gutters in the measured row content,
    with `16dp` between each tile to match the widgets above.
  - Expands focus-scroll rectangles by `16dp` so the first and last tiles retain
    their edge clearance at the horizontal scroll limits.
  - Doubles the focused tile outline from `2dp` to `4dp`.
- Pressing DPAD-down from a favourite now returns that carousel to its starting
  horizontal position when focus successfully moves to Seren or the Living
  Room panel.
- Pressing DPAD-down from the Seren Next Up carousel now smoothly returns its
  horizontal scroll position to the start when focus moves to the Living Room
  panel.
- The spectrum keeps its reversed accent-based hue sequence, with every
  bar using the selected highlight colour's full saturation and brightness.
- Deezer heart requests now create their ownership marker before choosing
  the local ADB or Home Assistant fallback transport, so the bridge can
  verify the request before reading or changing the Deezer heart.

The Track Mix action is not manually triggered during this release because that
would change the user's current playback. Lyrics keyboard keys edit the lookup
fields directly without opening Android's input method.
