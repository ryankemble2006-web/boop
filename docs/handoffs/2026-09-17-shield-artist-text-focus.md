# Shield artist text focus: approved, regression-first work in progress

Date: 2026-09-17. Owner: `boop-wall-shield-split-v207`; started from live `1b6816f611f88db67abf548eebe67057c01f5bab`. Main workflow at `9808322212b4953d3fb4831fd05e9ffa1de806c6` applies.

Ryan approved removing only the Now Playing artist's grey box/outline: white normally, cyan matching the progress bar while focused. Preserve its size, spacing, artist click and remote navigation. The shared TV decorator currently restyles all clickable/focusable TextViews, overriding the artist's existing focus colour.

This first checkpoint adds non-visual regression checks and runs them before expensive build steps. An intentional red result is expected against the unchanged app source. It is not a new installable candidate or an accepted fix. Next: inspect that actual failure, apply the narrow artist-only exemption, preserve normal button chrome, then run the existing permanent-signed build and deliver only the Shield candidate APK.

Ryan installs by dragging the actual downloaded APK into his Shield scrcpy window and owns visual/physical acceptance. No RDC, ADB, assistant screen capture, emulator or automated visual test is authorized by this task. Keep Voice, permissions, app data, accepted artwork, unrelated apps and concurrent work untouched.
