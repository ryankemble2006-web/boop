# BOOP status: Shield v208 signed, human test pending

Updated 2026-09-17. Owner: `boop-wall-shield-split-v207`.

## Current evidence

Shield artist-only text focus is implemented: transparent/no-outline artist label, white normally and canonical progress cyan on focus. Existing layout/navigation/artist action and generic button chrome are preserved by scoped review and non-visual regressions.

Build source `477f199b6ad8bcf54e9d0eb6256bbb9f9633682c`. Full signed run `35217237866` / job `105188496531` and focused run `35217237801` succeeded. The new regressions passed against original and materialized sources; all 18 inherited stages remained enabled and passed. Actual APK package/version, original certificate, frozen art and all 16 native library hashes passed verification.

Artifact `10495244358`, `BOOP-Shield-v208-Wall-v207-Signed`. Deliver only `BOOP-Shield-v208.apk`, `com.boop.shieldoverlay`, `208` / `1.2.208-shield`. APK SHA-256: `6503557057c1661a37cf4c65f91c63e149808dbde000645ceaea04e3d1eba5a9`; size 160420201 bytes. The downloaded archive and extracted APK were independently hash-checked against GitHub metadata and the build receipt.

**Installation and visual/physical acceptance: pending Ryan.** No automatic install, RDC/ADB operation, emulator, capture or visual sweep occurred. Ryan drags the actual APK into Shield scrcpy and reports the result. No Wall installation is requested, even though the shared pipeline compiled Wall for integrity checks.

## Limits and preservation

The first artist regression run intentionally reproduced the box. The initial implementation then passed the artist tests but hit a historical changed-file allowlist. The guard now admits exactly the approved artist delta while comparing every other byte of the Now Playing view to its prior source; no music/Voice regression was bypassed. Full details and all run references: `docs/handoffs/2026-09-17-shield-artist-text-focus.md`.

Natural Voice remains frozen, artwork/animation and setup/data/permissions/signing unchanged. The real-remote mic test from v207 is still pending; do not interpret UI build success as voice acceptance. Full previous status and v207 installation evidence are preserved in `docs/handoffs/2026-09-17-before-artist-focus/BOOP_STATUS.md` and the older dated v207 receipts.
