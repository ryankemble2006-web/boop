# Shield v229: centre fullscreen lyrics music column

Date: 2026-09-20
Branch: `boop-shield-weather-focus-v221`
Verified build source: `e95bbeb88b32e9134da47100d0cd598df45752d7`

## Geometry

The 397-design-pixel progress bar is the master centre.

- artwork centred on progress midpoint;
- title and artist use progress width and centred gravity;
- album bottom to title = 30 design pixels;
- title bottom to artist = 30 design pixels;
- transport reduced to Prev / Play-Pause / Next;
- transport group centred on progress midpoint;
- progress retains ±10-second seek.

## Verification

Run `35520822777`, job `106104476205`.
Artifact `10608905132`: `BOOP-Shield-v229-Wall-v207-Signed`.
Shield `BOOP-Shield-v229.apk`, 160485741 bytes, SHA-256 `b4fa6a00ec74b0b1a2ab865adc8df8d82229143ad2ac1e204db5522e2ff79836`.
Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256 `364bdcc540838ee3184f5fd780e5a836b6d674ac2acd133d7b9fcc5216a48668`.
All CI gates passed; all 16 native libraries remain baseline-identical.
