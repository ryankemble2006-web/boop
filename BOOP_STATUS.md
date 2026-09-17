# BOOP status: Shield v209 signed, close-button human test pending

Updated 2026-09-17. Owner: `boop-wall-shield-split-v207`.

## Current evidence

The split private-marker package regression is repaired for native Close player and Close media. Both activity routes now pass their actual application package into an exact-allowlisted close gate. All existing identity/nonce, native target, cancellation/session, fresh receipt and completion checks remain enabled. Selected Cast stopping follows its unchanged separate route.

Signed build source `6292bfe770c93e904e75100f5c4231e022437183`; production code fix `6caf25e30c9b0dbffbf1567e01d83b4e603f55e4`. Full signed run `35223237787` / job `105208294482`: SUCCESS, including all 18 inherited stages, source/materialized integration tests, both shell builds, original signer, accepted v206 native-byte equality and frozen art checks. The close regression previously reproduced the wrong Shield package before the fix and passed after it.

Artifact `10498580480`, `BOOP-Shield-v209-Wall-v207-Signed`. Actual Shield delivery: `BOOP-Shield-v209.apk`, `com.boop.shieldoverlay`, 209 / `1.2.209-shield`, 160420197 bytes. APK SHA-256 `a690fefa1bd600c8ddbfc34ca3e1e5d82d6aa775de5ce2dcd20746e847c0220e`. Downloaded artifact and extracted APK checksums matched their GitHub metadata/build receipt; archive integrity checks passed.

**Installation and physical close-button behavior: pending Ryan.** No automatic installation, visual test, emulator, screenshot or device control was performed. Ryan uses his Shield scrcpy window. Wall stays 207 and is not a new delivery request.

## Scope and remaining acceptance

The first full run stopped at the inherited historical production-file allowlist, after 1,444 music worker/curve assertions had passed. An eight-line test-only change now permits exactly the reviewed bytes of the two close files, not arbitrary changes. The original music checks and artist-specific exact-byte guard remain intact. The complete subsequent pipeline passed; no failing check was bypassed. Full evidence: `docs/handoffs/2026-09-17-shield-close-identity.md`.

Shield v208's artist text focus and latest navigation/layout are preserved. Voice remains frozen, artwork/animation untouched, and no setup/data/permission/HA/model/signing-key change accompanies this repair. Prior v208 visual acceptance and v207 real-remote microphone acceptance remain unclaimed. Previous root status is archived unchanged at `docs/handoffs/2026-09-17-before-close-identity/BOOP_STATUS.md`; earlier receipts remain authoritative for their own historical tests only.
