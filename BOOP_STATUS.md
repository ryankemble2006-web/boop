# BOOP status

Updated 2026-09-17. Active app branch: `boop-wall-shield-split-v207`.

Shield is on the v212 release line. v209 repaired Close player / Close media after the split; Ryan reported the close behavior working. v210 was the first idle-weather candidate, but its first physical test found no weather information loaded and the panel colour did not match Now Playing.

v211 added the missing Shield INTERNET permission and matched the weather card exactly to Now Playing. Ryan physically approved the colour. His v211 screenshot also showed wind, sunrise/sunset and update age in the footer, proving Open-Meteo data was loading, but the main current/hourly/daily area remained blank.

That v211 blank area was a rendering bug rather than user setup: all three weighted weather content columns had height `0`. v212 changes those column heights to `MATCH_PARENT`, preserving their existing 3:4:3 width weights, the approved 182dp hero slot, favourites-row position, weather source/cache/refresh behaviour, and the approved Now Playing card chrome.

v212 signed source: `aef9b05605b2d271d7df9f2698f8431dd42fb97e`.
Signed run: `35229524975` (success).
Artifact: `10500159898`, `BOOP-Shield-v212-Wall-v207-Signed`.
Target APK: `BOOP-Shield-v212.apk`, package `com.boop.shieldoverlay`.
APK SHA-256: `01cd1a53a2fc9b9eedffcc6b6601390ab204af6369dbcdd754a4eb9f1c7a3ff1`.
Automated weather-layout, chrome, INTERNET-permission, inherited functional, split integration, package/version/signer, native-library and frozen-art verification all passed. v212 manual Shield visual acceptance remains Ryan-owned and pending. v211's colour is accepted and its weather data fetch was physically demonstrated.

Wall remains v207 / `com.boop.alpha1`.

Repository governance cleanup on 2026-09-17 removed the former BOOP root workflow-rule/context files and the current source-preservation/changed-file allowlist gates. Functional behavior tests, build checks, signing verification and package integrity checks remain available.
