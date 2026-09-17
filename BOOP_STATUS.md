# BOOP status

Updated 2026-09-17. Active app branch: `boop-wall-shield-split-v207`.

Shield is on the v210 release line. v209 repaired Close player / Close media after the split; Ryan reported the close behavior working. v210 adds the weather panel in the existing idle Now Playing slot while keeping the favourites row parked.

v210 signed source: `a3618c613fd53e75577e8d9a608bb1b2b732e007`.
Signed run: `35226569308`.
Artifact: `10499332037`, `BOOP-Shield-v210-Wall-v207-Signed`.
Target APK: `BOOP-Shield-v210.apk`, package `com.boop.shieldoverlay`.

Wall remains v207 / `com.boop.alpha1`.

Repository governance cleanup on 2026-09-17 removed the former BOOP root workflow-rule/context files and the current source-preservation/changed-file allowlist gates. Functional behavior tests, build checks, signing verification and package integrity checks remain available.
