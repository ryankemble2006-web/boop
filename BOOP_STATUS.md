# BOOP status

Updated 2026-09-20. Owner: `boop-wall-shield-split-v207`. Current Shield iteration branch: `boop-shield-weather-focus-v221`.

Shield v233 / `1.2.233-shield` is signed and ready for Ryan's physical test.

v233 hardens the LRCLIB fallback after v232 physically returned “Couldn't check lyrics just now”:
- Deezer still gets first attempt;
- LRCLIB gets a separate 6-second fallback window;
- broad search uses LRCLIB `track_name + q`;
- search 404 is treated as no match rather than service failure;
- harmless leading “The”/trailing qualifiers are tolerated in metadata;
- duration remains bounded and only synced LRC is accepted;
- no lyrics UI/layout behavior changed.

Build source `9e319d7336e7b52d54c080ed8d3bd596c805ae3d`.
Successful run `35523758443`, job `106112227680`.
Artifact `10608914071`, `BOOP-Shield-v233-Wall-v207-Signed`.
Shield file `BOOP-Shield-v233.apk`, 160502125 bytes, SHA-256 `dfcc8522ca8137f3755abefe1e8b23eecab68a9122816089cec7f544700920d0`.
Permanent signer unchanged; all 16 native libraries remain baseline-identical.

Physical fallback re-test pending with Ryan.
