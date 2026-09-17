# BOOP Shield v209: close identity fixed in source; signed pipeline running

Updated 2026-09-17. Application owner remains `boop-wall-shield-split-v207`. Fetch its LIVE HEAD before work; main owns the shared workflow, not current app source.

## Current task and evidence

Ryan authorized repairing Close player and Close media after the read-only split regression diagnosis. Started from live owner `4ce305962c04b1bfa25a09bd12d1741679b567ea`, preserving Shield v208 artist text focus and all newer work there. Shared main was checked at `9808322212b4953d3fb4831fd05e9ffa1de806c6`. No later owner commits were present at the pre-publication check; the isolated task branch was fast-forwarded into the owner without force.

Implementation source: `6caf25e30c9b0dbffbf1567e01d83b4e603f55e4`. Both native close paths now pass their activity's `getPackageName()` to the close gate, matching the private marker's `getFilesDir()` Context. Only the existing BOOP application IDs are accepted. Marker/nonce safeguards, unique-device discovery, authenticated HA route, restricted native player targets, fresh receipts, cancellation and completion checks are retained. The selected Cast path is unchanged.

Regression before the fix: commit `4b7fcb13f35ea19ab01d8774334a2c2d38c616cf`, run `35222305035`, job `105205208176`, expected 22 failures / 17 passes, including the actual wrong Shield command. After the fix, focused run `35222770661`, job `105206748802`, succeeded. The owner split-contract run `35222849027` also succeeded. The exact two production-file changes were reviewed; no UI, Voice, animation, HA client or manifest code changed.

Full signed pipeline `35222849116`, job `105207010155`, is still running at this checkpoint. Its initial close/artist checks passed. Do not claim a completed APK or physical result yet. The existing full inherited stages and package/signer/native/art checks remain enabled. New Shield version is 209 / `1.2.209-shield`; Wall remains version 207 and is not a new delivery request.

## Next safe step

Inspect the signed run's job/step summaries first, then only relevant failed logs if needed. Once successful, fetch the actual artifact, verify the downloaded Shield APK against its receipt, deliver the APK itself, and reconcile this handoff, status and memory with the completed evidence. Record build source separately from later documentation commits. Detailed repair history: `docs/handoffs/2026-09-17-shield-close-identity.md`.

Ryan installs through the Shield scrcpy window and owns physical acceptance. No automatic installation, device driving, screen capture or emulator. No permission, setup/data, model, HA, signing-key or physical Pixel 10 changes. A source fix and CI success do not establish device behavior.

## Preserved delivery and acceptance history

The prior Shield v208 artist candidate was signed from `477f199b6ad8bcf54e9d0eb6256bbb9f9633682c`, run `35217237866`, artifact `10495244358`. Its artist label stays transparent/no-outline, white normally and progress-bar cyan on focus; size, spacing, click and navigation remain intact. Its installation/physical artist acceptance was pending in the last handoff and is not newly claimed here. Full receipt: `docs/handoffs/2026-09-17-shield-artist-text-focus.md`.

Preserve the earlier v207 assistant-role repair and the existing Katniss recognizer. Real Bluetooth speech/command acceptance remains pending; Voice latency/provider/pitch investigation remains frozen. Natural voices, downloads, tuning, art and animation are unchanged. Existing v207 install receipts and archived root history remain in `docs/handoffs/2026-09-17-before-artist-focus/` and dated handoffs. Never replay old fresh-install/access snapshots over later user progress.
