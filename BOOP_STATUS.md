# BOOP status

Updated 2026-09-18. Owner: `boop-wall-shield-split-v207`. Current Shield implementation: `boop-shield-now-playing-align-v218`.

Shield v218 / `1.2.218-shield` is signed and ready for Ryan's physical test.

v218 is a deliberately tiny Now Playing geometry fix on top of accepted v217. The progress bar is now the horizontal datum for the title/artist/state/transport stack: the transport row's old 8dp left inset was removed, so Prev starts on the exact same left edge as the progress track and the title/artist/Playing stack. Lyrics, Close player, album art, mascot bay, favourite/HA reorder behavior and HA latency paths are unchanged.

v217 keeps the verified v216 weather, HA icon and fan-control work unchanged, and adds one consistent remote interaction for rearranging HOME items:

- HOME favourites: long-press/hold now makes the grabbed artwork visibly lift to 1.14x with Z-depth. Ordinary focus remains at the accepted 1.00x artwork scale, so the stronger treatment appears only after the hold has actually engaged.
- HA Controls: long-press a device tile to grab it; the tile visibly lifts to 1.10x with Z-depth. Left/right moves it, and OK/Enter drops it.
- HA order persists per selected room. Missing entities are removed safely, and newly discovered devices append after the user's saved order instead of reshuffling existing controls.
- Normal HA taps still use the existing toggle path. The v214-v216 low-latency/state-stream architecture and fan routing were not changed.

Build source `a5f211a4038e208109e0ac31e18452c410a8f455`.
Successful run `35352592271`, job `105624043955`.
Artifact `10550157413`, `BOOP-Shield-v218-Wall-v207-Signed`.
Shield file `BOOP-Shield-v218.apk`, 160485741 bytes, SHA-256 `37db129fce83bf8cca062e93d1474533d8f4b7253120da1255912914ff4d8b58`.
Permanent signer SHA-256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Uploaded artifact ZIP SHA-256 `e9ca76c0fecb22d6de60815d63e9939ab021c26b40d07d3485f1890145f3ed9a`.

Verification passed through 74 focused checks, inherited v206 regression, materialized split integration, 97 integration checks, HA room/latency tests, both app builds, and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

The first v217 run `35351182952` correctly stopped on an inherited v205 visual test that still asserted the former 1.00x grabbed scale. That test was updated to the newly requested grab contract; no APK was produced by the failed run.

Physical acceptance of the Now Playing alignment remains pending. v217 is the prior signed checkpoint. Wall stays v207; voice/audio, permissions and signing material were not changed.
