# BOOP status

Updated 2026-09-20. Owner: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221`.

Shield v224 / `1.2.224-shield` is signed and ready for Ryan's physical test.

v224 Now Playing transport change:
- removed Rew and Fwd;
- remaining controls are Prev / Play-Pause / Next;
- Prev remains exactly on the accepted v219 position via `controls.setTranslationX(-dp(4))`;
- existing 10dp inter-button gap is unchanged;
- progress bar still handles ±10-second seek;
- no row recentering.

Preserved unchanged:
- v223 hourly temperature position;
- v221 return-focus and forecast layout;
- v220 accent colour;
- v219 progress/transport datum;
- v217 favourite/HA hold-to-reorder;
- HA command/latency paths;
- voice/audio, assistant art and native runtime;
- Wall v207.

Build source `16fbfdc632d821d7efd3911823a2035f3b8904b9`.
Successful run `35518326403`, job `106097941527`.
Artifact `10606879001`, `BOOP-Shield-v224-Wall-v207-Signed`.
Shield file `BOOP-Shield-v224.apk`, 160485737 bytes, SHA-256 `ead4ecb9de285499d46d44dca49d3ed24f6322b9e590e1c956dfd9c9a4514a28`.
Permanent signer SHA-256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256 `caaa3f168f5a6fef4713c32d4b043111a43042d93f29117a68c966add9293778`.

All CI gates passed and all 16 native libraries remain baseline-identical.

Physical acceptance of the three-button transport row remains pending with Ryan.
