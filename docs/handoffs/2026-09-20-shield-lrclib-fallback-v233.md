# Shield v233: harden LRCLIB fallback timing and search

Date: 2026-09-20
Branch: `boop-shield-weather-focus-v221`
Verified build source: `9e319d7336e7b52d54c080ed8d3bd596c805ae3d`

v232 physically returned “Couldn't check lyrics just now” on the fallback case.

v233 keeps Deezer primary but gives LRCLIB a separate 6-second fallback budget after the 2.5-second Deezer window. LRCLIB broad search now uses `track_name + q`, treats 404 as a clean miss, and accepts harmless metadata cosmetics while retaining duration matching and synced-only LRC.

Run `35523758443`, job `106112227680`.
Artifact `10608914071`: `BOOP-Shield-v233-Wall-v207-Signed`.
Shield SHA-256 `dfcc8522ca8137f3755abefe1e8b23eecab68a9122816089cec7f544700920d0`.
Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256 `eed3fd8f0fe5051ca3d2d03656d1446ffc04ad4ef949822b17c08fa55d103506`.
All CI gates passed; all 16 native libraries remain baseline-identical.
