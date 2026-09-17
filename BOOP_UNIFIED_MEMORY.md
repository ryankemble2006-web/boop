# BOOP durable memory: artist-only focus and manual APK delivery

Updated 2026-09-17. Owner remains `boop-wall-shield-split-v207`; the filename retains historical continuity. Maintain the current shared implementation with Wall and Shield shells, not older standalone source.

## Latest accepted requirement, not yet physically accepted implementation

Ryan approved a tiny Shield Now Playing refinement: remove the artist's box and outline completely, display white normally and progress-bar cyan (`#4DB8FF`) on D-pad focus. Keep size, spacing, the artist click and existing key routes.

Root cause: `BoopTvChrome` globally decorated clickable/focusable TextViews, adding a grey button and overriding the artist's local focus callback. `useTextOnlyFocus(subtitle)` now registers only the artist in a weak exemption set before attachment, clears background, disables Android's default focus highlight and installs a focused/default ColorStateList. Both generic eligibility and queued apply-state callbacks respect the exemption. Ordinary button/Voice styling is unchanged; do not extend this exemption to unrelated controls without a user request.

The new non-visual regression harness compiles actual production decoration methods against property-recording doubles, exercises repeated focus/layout and stale callbacks, and checks unaffected normal buttons. It is NOT an Android renderer or physical UI test. Source checks preserve artist key/click binding. The historical music changed-file guard now permits only the exact approved artist hunk, proving all other Now Playing bytes unchanged; its music behavior tests remain intact.

## Candidate identity and status

Shield only advances to `208` / `1.2.208-shield`, package `com.boop.shieldoverlay`. Build source `477f199b6ad8bcf54e9d0eb6256bbb9f9633682c`; full signed run `35217237866` and focused run `35217237801` succeeded. Artifact `10495244358` contains `BOOP-Shield-v208.apk`, SHA-256 `6503557057c1661a37cf4c65f91c63e149808dbde000645ceaea04e3d1eba5a9`. Original signer, all 16 accepted v206 native library hashes and frozen asset checks passed. The downloaded archive and extracted APK were hash-verified. Wall was compiled only as part of the existing shared checks; no new Wall installation is requested.

User installation and physical acceptance remain PENDING. Neither installed identity nor visual behavior of v208 was inspected. Current exact receipt and red/green history: `docs/handoffs/2026-09-17-shield-artist-text-focus.md`.

## Working boundary

GitHub owns source/review/non-visual checks/builds/signing; deliver the actual APK download. Ryan installs through scrcpy and is the eyes. Assistant/CI visual testing, automatic installs, emulator runs, captures and routine RDC device access are OFF. Do not restart the older joint-device loop or demand a mode change. User-supplied screenshots/errors are welcome evidence, not blanket permission to drive devices.

## Preserve earlier work

Both apps share the accepted v206 lineage. Protect felt artwork, animation timing, appearance choices, navigation/media functionality, Voice, package/caller boundaries and concurrent branches. Voice latency/provider/pitch work remains frozen. No app-data, setup, permission, model, HA or signing-key changes accompany this candidate; physical Pixel 10 stays untouched.

The earlier v207 mic assignment restored the BOOP activity-based assistant while retaining the Katniss recognizer. Actual Bluetooth speech/commands remain a user check. Preserve later access/setup/Home state rather than replaying historical fresh-install snapshots. The previous root memory is preserved byte-for-byte at `docs/handoffs/2026-09-17-before-artist-focus/BOOP_UNIFIED_MEMORY.md`, including original v207 delivery, microphone repair and cross-app contracts. Older archives and accepted checkpoints are unchanged.
