# Shield v209 close identity repair: implementation awaiting green checks

Updated 2026-09-17. Ryan explicitly authorized fixing the latest Shield app after the read-only diagnosis and warned that GitHub had advanced. Live owner read: `boop-wall-shield-split-v207` at `4ce305962c04b1bfa25a09bd12d1741679b567ea`; shared main read: `9808322212b4953d3fb4831fd05e9ffa1de806c6`. The v208 artist-only text focus, frozen Voice and later accepted work remain the baseline. Isolated task branch: `boop-shield-close-identity-v209`.

## Reproduced before the fix

Test-only commit `4b7fcb13f35ea19ab01d8774334a2c2d38c616cf`, focused run `35222305035`, job `105205208176`: expected failure, 22 failed / 17 passed. The actual production Java gate compiled successfully. Native Close player and Close media emitted `run-as com.boop.alpha1` when tested for the split Shield owner; the activity also failed the Context-package wiring check. This is source/logic evidence, not a physical-device test. The extra owner-validation cases define the required safe new parameter, not a claim that the old gate accepted arbitrary package input.

## Scoped implementation

`BoopClosePlayerActivity` now supplies its own `getPackageName()` to BOTH native gate paths, matching the same Context used by `getFilesDir()`. The immutable gate accepts only the two existing BOOP application IDs, then uses that validated identity in its existing private-marker commands. Legacy unified overloads retain their previous unified identity for compatibility; the active application routes are explicitly covered against using that default. Player allowlist, nonce format, cancellation, selected-session checks, unique-hardware discovery, fresh receipts, force-stop targets and removal confirmation remain unchanged. Selected Cast close remains on the original media-session route.

Shield candidate advances to `209` / `1.2.209-shield`; Wall stays 207 and is not a new delivery request. The signed workflow retains every existing inherited stage and adds the new close regression before building and against materialized Java. Actual APK verification retains original signer/native/art checks and additionally checks that the close classes and runtime owner validation are packaged.

Pending: focused green run, scoped review, reconciliation with LIVE owning HEAD, full existing signed pipeline, actual APK download/hash verification, root handoff/status/memory reconciliation. No installation, screen capture, emulator, device control, permission, HA or signing-key change. Ryan installs the signed Shield APK through scrcpy and owns physical acceptance. Preserve the v208 artist test and v207 microphone acceptance statuses without claiming new physical verification.
