# BOOP status

Updated 2026-09-20. Owner: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221`.

Shield v226 / `1.2.226-shield` is signed and ready for Ryan's physical test.

v226 fullscreen lyrics corner fix:
- album bitmap is hard-clipped with a rounded canvas Path inside `onDraw()`;
- `clipToOutline` is disabled for this artwork;
- orange focus ring is drawn after restoring the clipped canvas;
- bottom-right lyric provider/licence credit remains removed.

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

Build source `4758eaf8b4c74cd27b984af62e5be5d3f50e9280`.
Successful run `35519275112`, job `106100424396`.
Artifact `10607129240`, `BOOP-Shield-v226-Wall-v207-Signed`.
Shield file `BOOP-Shield-v226.apk`, 160485741 bytes, SHA-256 `eb52fefacfec25c5ce8173c6b6466f14fdabfa35d8175c131b7d2f016a62f0ee`.
Permanent signer SHA-256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256 `55c53470a621c5956e63bb3e440503bd3e5dd031634626a86db3faf6151c66c1`.

All CI gates passed and all 16 native libraries remain baseline-identical.

Physical acceptance of the hard-mask lyrics corners remains pending with Ryan.
