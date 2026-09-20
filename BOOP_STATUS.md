# BOOP status

Updated 2026-09-20. Owner: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221`; v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

Shield v222 / `1.2.222-shield` is signed and ready for Ryan's physical test. The live iteration branch intentionally stays on `boop-shield-weather-focus-v221` so subsequent tiny UI builds can reuse the same branch-scoped Gradle cache.

v222 is deliberately tiny:
- only the four **Next 4 hours** temperature values are nudged +1 physical pixel to the right;
- their prior +2dp weather optical correction remains intact;
- times, glyphs, rain, 3-day forecast, current weather and footer are unchanged.

Preserved unchanged:
- v221 first-favourite return focus and forecast layout;
- v220 user-selectable launcher highlight colour;
- v219 Now Playing geometry, physically accepted as perfect;
- v217 favourite/HA hold-to-reorder;
- HA command/latency paths;
- voice/audio, assistant art and native runtime;
- Wall v207.

CI caching is now verified working on the active Shield branch. Run `35516142829` restored the Gradle cache and the app build reported **77 tasks from cache**, 69 executed and 8 up-to-date, finishing the Gradle app build in **26 seconds**. Rapid superseded builds now cancel automatically.

Build source `5bab9dab597161a0a53ab5e6f85ef0f3d913bee0`.
Successful run `35516142829`, job `106092308080`.
Artifact `10606488920`, `BOOP-Shield-v222-Wall-v207-Signed`.
Shield file `BOOP-Shield-v222.apk`, 160485741 bytes, SHA-256 `3eee980f533a64b09d4fa0a25177c9c13bcbab2df17ec46f01c407f4c8f98bc5`.
Permanent signer SHA-256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256 `649641d807fbcab638ad2b65ef5ea35aed5aefd0a70d34e7a4e9c6036667fe30`.

All CI gates passed and all 16 native libraries remain baseline-identical.

Physical acceptance of the one-pixel temperature nudge remains pending with Ryan.
