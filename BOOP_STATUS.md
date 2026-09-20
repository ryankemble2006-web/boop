# BOOP status

Updated 2026-09-20. Owner: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221`.

Shield v232 / `1.2.232-shield` is signed and ready for Ryan's physical test.

v232 lyrics fallback:
- Deezer remains primary timed-lyrics source;
- LRCLIB is the synced-lyrics fallback;
- fallback matches current title/artist and bounds duration when known;
- only synced LRC is accepted, never fake timing from plain text;
- HOME Lyrics preflight also uses the fallback;
- all v231 and earlier accepted lyrics UI behavior is unchanged.

Build source `ba25861b5bb9547fcf68c2dd816718716bbe6d27`.
Successful run `35523199556`, job `106110743571`.
Artifact `10609073068`, `BOOP-Shield-v232-Wall-v207-Signed`.
Shield SHA-256 `3f2b77070a95c48b9d69079338002636053a1688dfb1fb4ff1a394d3d9c98d9d`.
Permanent signer unchanged; all 16 native libraries remain baseline-identical.

Physical fallback test pending with Ryan.
