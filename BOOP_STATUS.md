# BOOP status

Updated 2026-09-17. Active app branch: `boop-wall-shield-split-v207`.

Shield is on the v211 release line. v209 repaired Close player / Close media after the split; Ryan reported the close behavior working. v210 was the first idle-weather candidate, but Ryan's physical test found no weather information loaded and the panel colour did not match Now Playing.

v211 fixes both reported faults while preserving the approved 182dp layout and favourites-row position. The Shield app now packages `android.permission.INTERNET` for the existing Open-Meteo request, and the weather card uses the exact Now Playing surface values: RGB 16/16/16 fill, 14dp corners, and a 1dp RGB 48/48/48 stroke.

v211 signed source: `3d1c62b965fbe02aee58e8d39dd6649721611fef`.
Signed run: `35228355008` (success).
Artifact: `10500275652`, `BOOP-Shield-v211-Wall-v207-Signed`.
Target APK: `BOOP-Shield-v211.apk`, package `com.boop.shieldoverlay`.
APK SHA-256: `cd059052f7dfe1b398b8d1aeba21f49d333d4e637b455e87776367578ea6874d`.
Automated build/package/regression verification passed. Manual Shield installation and visual acceptance remain Ryan-owned and are not yet recorded for v211.

Wall remains v207 / `com.boop.alpha1`.

Repository governance cleanup on 2026-09-17 removed the former BOOP root workflow-rule/context files and the current source-preservation/changed-file allowlist gates. Functional behavior tests, build checks, signing verification and package integrity checks remain available.
