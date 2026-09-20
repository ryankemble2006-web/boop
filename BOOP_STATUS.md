# BOOP status

Updated 2026-09-20. Owner: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221`.

Shield v225 / `1.2.225-shield` is signed and ready for Ryan's physical test.

v225 fullscreen lyrics polish:
- album art uses the exact shared HOME-banner rounded clipping/background path;
- custom lyrics artwork outline provider removed;
- bottom-right lyric provider/licence credit no longer drawn;
- lyric data parsing, loading, timing, status and controls unchanged.

Preserved unchanged:
- v224 Prev / Play-Pause / Next transport;
- v223 hourly temperature position;
- v221 return-focus and forecast layout;
- v220 accent colour;
- v219 progress/transport datum;
- v217 favourite/HA hold-to-reorder;
- HA command/latency paths;
- voice/audio, assistant art and native runtime;
- Wall v207.

Build source `8b252a2250e07187095303b25cf1d27298b715f1`.
Successful run `35518868544`, job `106099355568`.
Artifact `10607712461`, `BOOP-Shield-v225-Wall-v207-Signed`.
Shield file `BOOP-Shield-v225.apk`, 160485741 bytes, SHA-256 `b6625c5ad25a1b6e3afbd6e6e40ef76ae7db7c59b2c750e8748301dcd258d328`.
Permanent signer SHA-256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256 `e8a31f636ac9e98889ec11f4154d036dba4c42d43dbcd3b8fab1f0937f2456f6`.

All CI gates passed and all 16 native libraries remain baseline-identical.

Physical acceptance of the lyrics-screen corner cleanup remains pending with Ryan.
