# BOOP status

Updated 2026-09-20. Owner: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221`; v222 snapshot branch: `boop-shield-hour-temp-nudge-v222`.

Shield v223 / `1.2.223-shield` is signed and ready for Ryan's physical test. The live iteration branch intentionally stays on `boop-shield-weather-focus-v221` so subsequent tiny UI builds can reuse the same branch-scoped Gradle cache.

v223 is deliberately tiny:
- only the four **Next 4 hours** temperature values move 2 more physical pixels right than v222;
- source changes from `temp.setTranslationX(dp(2)+1f)` to `temp.setTranslationX(dp(2)+3f)`;
- times, glyphs, rain, 3-day forecast, current weather and footer are unchanged.

Preserved unchanged:
- v221 first-favourite return focus and forecast layout;
- v220 user-selectable launcher highlight colour;
- v219 Now Playing geometry, physically accepted as perfect;
- v217 favourite/HA hold-to-reorder;
- HA command/latency paths;
- voice/audio, assistant art and native runtime;
- Wall v207.

Build source `fceb5659a5fe52d01205641956b834ff5615385e`.
Successful run `35517619079`, job `106096104834`.
Artifact `10607337069`, `BOOP-Shield-v223-Wall-v207-Signed`.
Shield file `BOOP-Shield-v223.apk`, 160485741 bytes, SHA-256 `2bc6179110d22f3a76243e1914f8f19db7573e74694c5e85e8aa40493d7340d4`.
Permanent signer SHA-256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256 `05e542e8c4e090792781dd24d3f53fff9e33ce4c3a85b2fb65b3fdfee5f5c3bd`.

All CI gates passed and all 16 native libraries remain baseline-identical.

Physical acceptance of the +2 physical-pixel v223 temperature nudge remains pending with Ryan.
