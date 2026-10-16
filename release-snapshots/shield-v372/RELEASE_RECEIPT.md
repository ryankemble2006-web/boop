# BOOP Shield v372 source snapshot

This file identifies the versioned source snapshot. The GitHub Actions run
generates the signed APK, SHA-256, signer certificate report, and commit receipt
as downloadable artifacts after tests and build verification complete.

- Package: `com.boop.shieldoverlay`
- Version: `1.2.372-shield` (`versionCode 372`)
- Deezer Track Mix: focus the current track title to show the selected orange
  highlight. Selecting it sends Deezer's native `TRACK_MIX&&<track-id>` media ID
  through its active session while BOOP stays onscreen.
- The title action is available only when Deezer metadata supplies a validated
  track ID and the live session supports `playFromMediaId`.
- The native dispatch is silent and does not claim that Deezer has finished
  starting Track Mix playback.
- The lyrics screen title exposes that same focus treatment and Track Mix action.
- Lyrics lookup explicitly opens the Android TV input method for the focused
  title or artist field; moving between those fields leaves the keyboard open.
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
would change the user's current playback. The lyrics lookup keyboard is requested
through Android's input method manager when its dialog opens.
