# BOOP Shield v209: signed close-identity repair; Ryan's test pending

Updated 2026-09-17. Owner remains `boop-wall-shield-split-v207`. Fetch its LIVE HEAD before work. Main owns shared workflow/ownership, not the current app implementation.

## Current result

Ryan authorized fixing Close player and Close media after the read-only split regression diagnosis. Started from live owner `4ce305962c04b1bfa25a09bd12d1741679b567ea`, retaining the latest Shield v208 artist refinement. Shared main was checked at `9808322212b4953d3fb4831fd05e9ffa1de806c6`. No intervening owner commits were present at publication checks; all updates were non-force.

Both native close routes now supply their activity's `getPackageName()` to `LocalPlayerCloseGate`, matching the private marker's `getFilesDir()` Context. Only the existing BOOP application IDs are accepted. Unique-device discovery, marker/nonce checks, fresh receipts, native target restrictions, cancellation, session selection and completion confirmation remain intact. The selected Cast route, HA client, UI, Voice, artwork and animation were not changed.

Production fix commit: `6caf25e30c9b0dbffbf1567e01d83b4e603f55e4`. Signed build source: `6292bfe770c93e904e75100f5c4231e022437183`. The latter adds an exact-byte exception for the reviewed close repair to the inherited change-list guard; it does not change app code or remove music tests. Later documentation commits are not new APK builds.

## Verified artifact

Full signed run `35223237787`, job `105208294482`: SUCCESS. All 18 inherited stages, focused source/materialized integration checks, both shell builds and actual packaged APK checks passed. Original certificate, all 16 native-library hashes from accepted v206 and frozen art were verified by the signed pipeline.

Artifact `10498580480`, `BOOP-Shield-v209-Wall-v207-Signed`.
Deliver only `BOOP-Shield-v209.apk`: `com.boop.shieldoverlay`, 209 / `1.2.209-shield`, 160420197 bytes.
APK SHA-256: `a690fefa1bd600c8ddbfc34ca3e1e5d82d6aa775de5ce2dcd20746e847c0220e`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
The actual downloaded artifact ZIP and extracted Shield APK were independently checked against GitHub metadata and the build receipt; both ZIP/ APK CRC checks passed. No rebuild or re-sign occurred outside GitHub.

## Next safe step and limits

Ryan installs the actual APK into his Shield scrcpy window and tests native Deezer/YouTube Close player, then Close media. Physical installation/behavior and Home dismissal remain PENDING; source/CI success is not device acceptance. Selected Cast close remains its separate existing stop path, so do not claim this native identity repair proves every Cast case.

No automatic install, device driving, screen capture or emulator. No permission, data/setup, model, HA or signing-key change. Physical Pixel 10 stays untouched. Wall remains 207 and was built only for shared compatibility/integrity checks; no new Wall installation is requested. Do not replace a previously accepted installed app based only on a historical handoff snapshot.

## History and continuity

Detailed red/green history, exact scope, first-build guard failure and final artifact receipt: `docs/handoffs/2026-09-17-shield-close-identity.md`.
Previous v208 root handoff/status/memory are archived byte-for-byte at `docs/handoffs/2026-09-17-before-close-identity/`.
The v208 white/cyan artist text, no-box/no-outline styling and existing key/click behavior remain intact. Its prior physical acceptance was pending and is not newly claimed. Receipt: `docs/handoffs/2026-09-17-shield-artist-text-focus.md`.
Preserve v207 activity-based assistant routing and Katniss recognition; real Bluetooth speech/command acceptance remains pending. Natural Voice latency/provider/pitch investigation stays frozen. Older v207 install receipts and archives remain unchanged.
