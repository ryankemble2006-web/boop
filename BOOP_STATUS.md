# BOOP status

Updated 2026-09-20. Owner: `boop-wall-shield-split-v207`. Current Shield implementation: `boop-shield-weather-focus-v221`.

Shield v221 / `1.2.221-shield` is signed and ready for Ryan's physical test.

v221 changes only Shield weather alignment and HOME return focus:
- hourly and 3-day forecast text rows now share the accepted +2dp optical centre of their weather glyph;
- the 3-day forecast region is translated +9dp so its visible space is balanced between the middle divider and outer right card border;
- returning from an external app, including via Shield task manager, requests Favourite entry 1;
- Close media returns focus to Favourite entry 1 after the existing close-media action.

Preserved unchanged:
- v219 Now Playing geometry, physically accepted as perfect;
- v220 user-selectable launcher highlight colour;
- v217 favourite/HA hold-to-reorder;
- HA command/latency paths;
- voice/audio, assistant art and native runtime;
- Wall v207.

Build source `9bf329a24631310261843b654f9fe0716a59aa2e`.
Successful run `35515284079`, job `106090068972`.
Artifact `10606343049`, `BOOP-Shield-v221-Wall-v207-Signed`.
Shield file `BOOP-Shield-v221.apk`, 160485741 bytes, SHA-256 `d0e5111efd97dc948890b2181e3212ce20e67b4e43cadc0df900a06969295f8a`.
Permanent signer SHA-256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256 `20a5fbf892d2139d4e45239515bfcee0f2f2773af4c86f0543c42440e2dc664c`.

All CI gates passed, including focused Shield checks, inherited v206 checks, materialized split integration, HA unit tests, both app builds and actual APK signer/native/art verification. All 16 native libraries remain baseline-identical.

Physical weather/focus acceptance remains pending with Ryan as the visual/ADB tester.
