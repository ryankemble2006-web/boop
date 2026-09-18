# BOOP current handoff

Updated 2026-09-18. Owner branch: `boop-wall-shield-split-v207`. Current Shield implementation branch: `boop-shield-grab-reorder-v217`.

## Current Shield: v217, signed and ready for Ryan's physical test

Package `com.boop.shieldoverlay`, version `217` / `1.2.217-shield`.

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

Production/build source: `4fde718e7ee928f87438f452022dc8e38f858163`.
Successful GitHub Actions run `35351389519`, job `105620113142`.
Artifact `10549884892`: `BOOP-Shield-v217-Wall-v207-Signed`.

Deliver **BOOP-Shield-v217.apk**, 160485741 bytes.
Shield APK SHA-256: `a6742e4fa9e3457ffe0384c9f1c41d5a1a97b4231c39b7b06688f68122227b3b`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Uploaded artifact ZIP SHA-256: `51106302b2394af6dd2fc79a47dd1599ed1e9a0015b56866ca02a30615c29aa9`.

Verification passed: 73 initial focused checks, inherited v206 regression, materialized split integration, 96 integration checks, HA room/latency unit tests, both app builds, and actual APK identity/certificate/native/art checks. All 16 native libraries remain baseline-identical.

### Superseded v217 run

Run `35351182952` stopped before materialization/build because `tests/test_home_visual_spacing_v205.py` still encoded the former contract `HOME_ARTWORK_GRABBED_SCALE = 1.00f`. The user's v217 request intentionally changes that exact grabbed state. The test was updated to require 1.14x plus Z-depth while still requiring ordinary focus to remain 1.00x. No APK was produced by the failed run.

## Physical acceptance still pending

Install only `BOOP-Shield-v217.apk` for this checkpoint and check:
1. Hold a favourite: it should clearly lift/pop compared with ordinary focus.
2. While held, left/right still reorders it and OK drops it.
3. Hold a HA control: it should visibly lift; left/right should move it; OK should drop it.
4. Leave/re-enter HOME or restart the app: HA order should remain.
5. Normal HA taps should still toggle immediately and should not enter reorder mode.

Wall remains v207. v216 remains the prior signed checkpoint.

Detailed v217 record: `docs/handoffs/2026-09-18-shield-grab-reorder-v217.md`.
