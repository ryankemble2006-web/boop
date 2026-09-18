# BOOP current handoff

Updated 2026-09-18. Owner branch: `boop-wall-shield-split-v207`. Current Shield implementation branch: `boop-shield-now-playing-align-v218`.

## Current Shield: v218, signed and ready for Ryan's physical test

Package `com.boop.shieldoverlay`, version `218` / `1.2.218-shield`.

v218 is a surgical Now Playing alignment pass based directly on Ryan's Shield screenshot. The visible progress bar is the reference edge. The only production geometry change is removal of the transport row's private 8dp left padding, so Prev/Rew/Pause/Fwd/Next now begin on the same left edge as title, artist, playback-state text and the progress track. Lyrics/Close player, artwork, mascot bay, HOME rows, v217 movement/customisation and HA control behavior are intentionally untouched.

v217 is a focused interaction change on top of the verified v216 weather/icon/fan build. It does not alter voice/audio, HA transport, weather geometry, permissions, signer, Wall behavior or BOOP's accepted HOME spacing.

### Favourite hold now visibly pops

The existing favourite reorder session was already correct: long-press grabs, left/right moves, OK/Enter drops. The visual treatment was not strong enough because HOME favourite artwork deliberately stayed at 1.00x even while grabbed.

v217 leaves ordinary focus at 1.00x and changes only the active grabbed artwork to 1.14x with 10dp Z-depth. This makes the hold transition unmistakable without making normal navigation pulse or changing the 230x129 banner layout.

### HA Controls use the same reorder interaction

The room-control row now reuses the proven `FavouriteGrabSession` model:
- long-press a HA tile to grab it;
- the held tile lifts to 1.10x with 10dp Z-depth;
- left/right moves the tile in the row;
- OK/Enter drops it and persists the order;
- up/down are held while reorder mode owns the tile, avoiding accidental row exits.

A normal, ungrabbed click still toggles the HA device through the existing low-latency path. Reordering therefore adds no network work to ordinary device presses.

Order is stored independently for each HA room. The stored order is reconciled against live entities: disappeared IDs are discarded and new devices append after the saved order. Changing rooms cancels an active grab.

### v216 behavior retained

The exact divider-based weather centring, semantic fan/subwoofer icons, capability-aware fan selection, fan conversation route with direct-service fallback, and live `state_changed` display truth remain intact from v216.

## Verified signed artifact

Production/build source: `a5f211a4038e208109e0ac31e18452c410a8f455`.
Successful GitHub Actions run `35352592271`, job `105624043955`.
Artifact `10550157413`: `BOOP-Shield-v218-Wall-v207-Signed`.

Deliver **BOOP-Shield-v218.apk**, 160485741 bytes.
Shield APK SHA-256: `37db129fce83bf8cca062e93d1474533d8f4b7253120da1255912914ff4d8b58`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Uploaded artifact ZIP SHA-256: `e9ca76c0fecb22d6de60815d63e9939ab021c26b40d07d3485f1890145f3ed9a`.

Verification passed: 74 initial focused checks, inherited v206 regression, materialized split integration, 97 integration checks, HA room/latency unit tests, both app builds, and actual APK identity/certificate/native/art checks. All 16 native libraries remain baseline-identical.

### Prior v217 note

Run `35351182952` stopped before materialization/build because `tests/test_home_visual_spacing_v205.py` still encoded the former contract `HOME_ARTWORK_GRABBED_SCALE = 1.00f`. The user's v217 request intentionally changes that exact grabbed state. The test was updated to require 1.14x plus Z-depth while still requiring ordinary focus to remain 1.00x. No APK was produced by the failed run.

## Physical acceptance still pending

Install `BOOP-Shield-v218.apk` and check the Now Playing panel against the screenshot:
1. Title, artist and Playing retain the progress bar's left edge.
2. Progress bar remains unchanged.
3. Prev now begins exactly on that same left edge, with the rest of the transport row following it.
4. Lyrics/Close player, artwork, eyes, HOME favourites and HA Controls remain where they were.
5. v217 hold/reorder movement remains accepted and unchanged.

Wall remains v207. v217 remains the prior signed checkpoint.

Detailed v218 record: `docs/handoffs/2026-09-18-shield-now-playing-align-v218.md`.
