# BOOP durable project memory

Updated 2026-09-17.

The current consumer apps are split shells around the shared BOOP implementation: Wall uses `com.boop.alpha1`; Shield uses `com.boop.shieldoverlay`.

Shield v209 repaired native Close player / Close media ownership after the package split. Shield v210 introduced a weather panel in the existing 182dp idle Now Playing slot, but Ryan's first physical test found that no weather information loaded and the panel surface did not match Now Playing.

Shield v211 added the missing Shield `android.permission.INTERNET` and locked the weather surface to the exact Now Playing card chrome: RGB 16/16/16 fill, 14dp corner radius, and RGB 48/48/48 1dp stroke. Ryan physically approved that colour match. His v211 screenshot also displayed live footer weather values, including wind, sunrise/sunset and update age, proving the Open-Meteo fetch and cache path were functioning. The main current/hourly/daily content remained blank because those three weighted columns had been created with height `0`.

Shield v212 fixes only that rendering error by using `MATCH_PARENT` height for the current, next-four-hours and three-day columns while retaining the existing 3:4:3 width weights. No user setup is required for weather. Open-Meteo remains keyless; weather still refreshes every 30 minutes, keeps a bounded stale cache, is non-focusable, disappears for eligible Now Playing media, retains the approved 182dp slot and exact card chrome, and leaves the favourites row parked.

Latest signed Shield source: `aef9b05605b2d271d7df9f2698f8431dd42fb97e`. Signed run `35229524975`; artifact `10500159898` (`BOOP-Shield-v212-Wall-v207-Signed`). Target APK `BOOP-Shield-v212.apk`, SHA-256 `01cd1a53a2fc9b9eedffcc6b6601390ab204af6369dbcdd754a4eb9f1c7a3ff1`. Automated weather-layout, exact-chrome, packaged INTERNET-permission, inherited functional, split integration, package/version/signer, native-library and frozen-art checks passed. Ryan owns manual installation and visual/physical acceptance. v212 visual acceptance remains pending; v211's colour is accepted and its weather data plumbing was physically demonstrated. Wall remains on v207.

The earlier GitHub workflow-rule/context experiment was retired on 2026-09-17. The active branch no longer carries the root AGENTS, BOOP_START_HERE, BOOP_CONTEXT, BOOP_RULES or BUILD_ON_GITHUB files, and the current exact-file/source-preservation allowlists were removed. Functional regression coverage, package verification and signer/integrity checks remain.

Historical receipts under `docs/` remain history rather than active instructions.
