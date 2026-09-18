# BOOP status

Updated 2026-09-18. Owner: `boop-wall-shield-split-v207`. Current Shield implementation: `boop-shield-grab-reorder-v217`.

Shield v217 / `1.2.217-shield` is signed and ready for Ryan's physical test.

v217 keeps the verified v216 weather, HA icon and fan-control work unchanged, and adds one consistent remote interaction for rearranging HOME items:

- HOME favourites: long-press/hold now makes the grabbed artwork visibly lift to 1.14x with Z-depth. Ordinary focus remains at the accepted 1.00x artwork scale, so the stronger treatment appears only after the hold has actually engaged.
- HA Controls: long-press a device tile to grab it; the tile visibly lifts to 1.10x with Z-depth. Left/right moves it, and OK/Enter drops it.
- HA order persists per selected room. Missing entities are removed safely, and newly discovered devices append after the user's saved order instead of reshuffling existing controls.
- Normal HA taps still use the existing toggle path. The v214-v216 low-latency/state-stream architecture and fan routing were not changed.

Build source `4fde718e7ee928f87438f452022dc8e38f858163`.
Successful run `35351389519`, job `105620113142`.
Artifact `10549884892`, `BOOP-Shield-v217-Wall-v207-Signed`.
Shield file `BOOP-Shield-v217.apk`, 160485741 bytes, SHA-256 `a6742e4fa9e3457ffe0384c9f1c41d5a1a97b4231c39b7b06688f68122227b3b`.
Permanent signer SHA-256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Uploaded artifact ZIP SHA-256 `51106302b2394af6dd2fc79a47dd1599ed1e9a0015b56866ca02a30615c29aa9`.

Verification passed through focused checks, inherited v206 regression, materialized split integration, HA room/latency tests, both app builds, and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

The first v217 run `35351182952` correctly stopped on an inherited v205 visual test that still asserted the former 1.00x grabbed scale. That test was updated to the newly requested grab contract; no APK was produced by the failed run.

Physical acceptance of the stronger favourite hold and HA reorder interaction remains pending. v216 remains the prior signed checkpoint. Wall stays v207; voice/audio, permissions and signing material were not changed.
