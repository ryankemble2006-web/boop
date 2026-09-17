# BOOP durable memory: split close identity and manual APK delivery

Updated 2026-09-17. Owner remains `boop-wall-shield-split-v207`; this filename retains historical continuity. Continue the current shared implementation with Wall/Shield shells, not older standalone source or unified checkpoints.

## Close player / Close media regression and repair

Ryan reported that both controls worked in unified and broke after the split. The native closing activity writes a per-action nonce marker in its own private `getFilesDir()`. `LocalPlayerCloseGate` still hard-coded `run-as com.boop.alpha1`, but Shield now owns `com.boop.shieldoverlay`; HA's device-identification check consequently looked in the wrong private app storage and refused to proceed. The two buttons share this gate. Selected Cast closing follows the separate existing media-session stop path.

The v209 repair passes the running activity's `getPackageName()` into BOTH native gate routes. The gate accepts only `com.boop.alpha1` or `com.boop.shieldoverlay`, stores the validated identity immutably, and uses it for the marker read and every existing marker recheck. Java package namespaces intentionally remain unchanged. Do not blanket-replace `com.boop.alpha1` across source or weaken the hardware-identity safeguard.

Legacy unified overloads retain compatibility; current app activity routes explicitly supply their Context package and are regression-covered. Preserve the selected native player allowlist, nonce format, current-session matching, Back cancellation, unique-hardware discovery, fresh per-call receipts and final removal confirmation. `LocalPlayerCloseClient`, Cast stopping and Android manifests were not modified.

A non-visual pytest/Java harness compiles the actual gate and exercises both owner identities, both native close paths, selected Deezer/YouTube targets, combined cleanup, invalid owners/nonces, cancellation and session matching. It checks activity ownership wiring and runs again against materialized Java. The pre-fix commit compiled and demonstrated the actual wrong command, rather than failing solely because of a new method signature. Extra invalid-owner cases specify the safety of the NEW input; they do not imply the old no-input API had an injection vulnerability.

## Signed candidate and verification boundary

Production fix `6caf25e30c9b0dbffbf1567e01d83b4e603f55e4`; signed source `6292bfe770c93e904e75100f5c4231e022437183`. Full signed run `35223237787`, job `105208294482`, passed all 18 inherited stages, source/materialized integration checks and actual APK validation. An inherited changed-file guard needed a narrow update: it now accepts exactly the reviewed bytes of both close files. All music behavior assertions and the existing exact artist delta guard remain intact.

Shield candidate: 209 / `1.2.209-shield`, `com.boop.shieldoverlay`. Artifact `10498580480`; `BOOP-Shield-v209.apk`, SHA-256 `a690fefa1bd600c8ddbfc34ca3e1e5d82d6aa775de5ce2dcd20746e847c0220e`, 160420197 bytes. Existing permanent signer, all 16 accepted v206 native libraries and frozen art passed the build's checks. Downloaded ZIP and extracted APK hashes/integrity were independently verified. Wall stays 207 and is not a new delivery request.

Ryan's installation and physical Close player/Close media acceptance remain PENDING. No physical success is implied by source tests, packaged class checks or CI-green. Detailed receipt and run history: `docs/handoffs/2026-09-17-shield-close-identity.md`.

## Preserve artist focus, Voice and prior state

The v208 requirement remains: only the artist label loses its box/outline, appears white normally and progress-bar cyan (`#4DB8FF`) on D-pad focus, with unchanged size/spacing/key/click behavior. `BoopTvChrome.useTextOnlyFocus(subtitle)` uses a weak exemption set, clears the background/default focus highlight and installs focused/default text colors; generic and queued decoration both respect the exemption. Do not extend it to other controls without a user request. The source/property-recording tests are not a physical renderer test. Prior v208 physical acceptance was pending, not newly established here.

Preserve the accepted v206 lineage, frozen natural Voice, model/download choices, tuning, felt art, animations, navigation/media features, caller boundaries and concurrent work. The earlier v207 assistant-role assignment retained the Katniss recognizer; actual Bluetooth speech/commands remain a user check. Do not replay old fresh-install/permission snapshots over newer setup or access state.

## Working boundary and archives

GitHub owns source edits, review, non-visual checks, builds, signing and handoffs. Deliver the actual signed APK. Ryan installs through scrcpy and supplies the eyes. Automatic installs, visual testing, captures, emulator runs and routine RDC device access are OFF. No mode-change demand, blanket device permission or local build is implied. No data/setup, permission, model, HA or signing-key change was made. Physical Pixel 10 remains untouched.

Previous v208 handoff/status/memory are preserved byte-for-byte at `docs/handoffs/2026-09-17-before-close-identity/`. Artist receipt: `docs/handoffs/2026-09-17-shield-artist-text-focus.md`. Earlier v207 and cross-app contracts remain in `docs/handoffs/2026-09-17-before-artist-focus/` and their dated receipts. Main's ownership map is unchanged; final publication must verify the live owning ref, not assume the laptop is synchronized.
