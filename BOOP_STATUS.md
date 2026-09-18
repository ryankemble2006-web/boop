# BOOP status

Updated 2026-09-18. Owner: `boop-wall-shield-split-v207`. Current Shield implementation: `boop-shield-now-playing-align-v219`.

Shield v219 / `1.2.219-shield` is signed and ready for Ryan's physical test.

v219 finishes the Now Playing visual alignment from Ryan's v218 screenshot: the transport row is shifted 4dp further left for visual alignment in its unfocused state, and the progress bar now has an 8dp right margin so its visible end lines up with the visible right edge of Close player. Lyrics/Close player positions, album art, mascot bay, favourite/HA reorder behavior and HA latency paths are unchanged.

v217 keeps the verified v216 weather, HA icon and fan-control work unchanged, and adds one consistent remote interaction for rearranging HOME items:

- HOME favourites: long-press/hold now makes the grabbed artwork visibly lift to 1.14x with Z-depth. Ordinary focus remains at the accepted 1.00x artwork scale, so the stronger treatment appears only after the hold has actually engaged.
- HA Controls: long-press a device tile to grab it; the tile visibly lifts to 1.10x with Z-depth. Left/right moves it, and OK/Enter drops it.
- HA order persists per selected room. Missing entities are removed safely, and newly discovered devices append after the user's saved order instead of reshuffling existing controls.
- Normal HA taps still use the existing toggle path. The v214-v216 low-latency/state-stream architecture and fan routing were not changed.

Build source `9ad31957cba942f857c4d48588702d9b4ab89cb4`.
Successful run `35353692917`, job `105627714059`.
Artifact `10551187345`, `BOOP-Shield-v219-Wall-v207-Signed`.
Shield file `BOOP-Shield-v219.apk`, 160485741 bytes, SHA-256 `d6462470ca1b95e8672d038acd902c7bdd57d687aaa8146e1b0d66e2e222bdae`.
Permanent signer SHA-256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Uploaded artifact ZIP SHA-256 `4f8f5016bca24475d808c7e9afc0fb3c8d8c07f639986fd084e56639b0070e60`.

Verification passed through 74 focused checks, inherited v206 regression, materialized split integration, 97 integration checks, HA room/latency tests, both app builds, and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

The first v217 run `35351182952` correctly stopped on an inherited v205 visual test that still asserted the former 1.00x grabbed scale. That test was updated to the newly requested grab contract; no APK was produced by the failed run.

Physical acceptance of the final Now Playing edge alignment remains pending. v218 is the prior signed checkpoint. Wall stays v207; voice/audio, permissions and signing material were not changed.
