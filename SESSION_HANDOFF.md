# BOOP current handoff

Updated 2026-09-18. Owner branch: `boop-wall-shield-split-v207`. Current Shield implementation branch: `boop-shield-now-playing-align-v219`.

## Current Shield: v219, signed and ready for Ryan's physical test

Package `com.boop.shieldoverlay`, version `219` / `1.2.219-shield`.

v219 is the final micro-alignment pass from Ryan's follow-up screenshot. The unfocused transport row is translated 4dp left so Prev visually aligns with the progress track, and the progress bar gets an 8dp right margin so it ends on the visible right edge of Close player. Lyrics/Close player themselves, artwork, mascot bay, HOME rows, v217 movement/customisation and HA behavior are intentionally untouched.

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

Production/build source: `9ad31957cba942f857c4d48588702d9b4ab89cb4`.
Successful GitHub Actions run `35353692917`, job `105627714059`.
Artifact `10551187345`: `BOOP-Shield-v219-Wall-v207-Signed`.

Deliver **BOOP-Shield-v219.apk**, 160485741 bytes.
Shield APK SHA-256: `d6462470ca1b95e8672d038acd902c7bdd57d687aaa8146e1b0d66e2e222bdae`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Uploaded artifact ZIP SHA-256: `4f8f5016bca24475d808c7e9afc0fb3c8d8c07f639986fd084e56639b0070e60`.

Verification passed: 74 initial focused checks, inherited v206 regression, materialized split integration, 97 integration checks, HA room/latency unit tests, both app builds, and actual APK identity/certificate/native/art checks. All 16 native libraries remain baseline-identical.

### Prior v217 note

Run `35351182952` stopped before materialization/build because `tests/test_home_visual_spacing_v205.py` still encoded the former contract `HOME_ARTWORK_GRABBED_SCALE = 1.00f`. The user's v217 request intentionally changes that exact grabbed state. The test was updated to require 1.14x plus Z-depth while still requiring ordinary focus to remain 1.00x. No APK was produced by the failed run.

## Physical acceptance still pending

Install `BOOP-Shield-v219.apk` and check the Now Playing panel:
1. Unfocused Prev visually lines up with the left edge of the progress track.
2. The progress track ends on the visible right edge of Close player.
3. Title, artist and Playing remain unchanged.
4. Lyrics/Close player, artwork, eyes, HOME favourites and HA Controls remain where they were.
5. v217 hold/reorder movement remains accepted and unchanged.

Wall remains v207. v218 remains the prior signed checkpoint.

Detailed v219 record: `docs/handoffs/2026-09-18-shield-now-playing-align-v219.md`.
