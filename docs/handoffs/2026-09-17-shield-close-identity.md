# Shield v209 close identity repair: signed and ready for Ryan's test

Updated 2026-09-17. Ryan explicitly authorized fixing the latest Shield after the read-only diagnosis and warned that GitHub had advanced. Starting live owner: `boop-wall-shield-split-v207` at `4ce305962c04b1bfa25a09bd12d1741679b567ea`; shared main: `9808322212b4953d3fb4831fd05e9ffa1de806c6`. The v208 artist focus work and frozen Voice remained the baseline. The isolated task branch `boop-shield-close-identity-v209` was fast-forwarded into the owner after focused green checks and a fresh owner read; no force/reset was used. Pre-final-publication live owner was rechecked at `6292bfe770c93e904e75100f5c4231e022437183`.

## Root cause and bounded implementation

`BoopClosePlayerActivity` creates the private close nonce file with `getFilesDir()`. The native gate previously looked for it using hard-coded `run-as com.boop.alpha1`. Split Shield is `com.boop.shieldoverlay`, so the same-hardware proof looked in the wrong app's private files. Both native Close player and Close media share this gate.

Production fix commit `6caf25e30c9b0dbffbf1567e01d83b4e603f55e4`: the activity supplies `getPackageName()` to both gate paths. The immutable gate validates the exact two BOOP application IDs, then uses the supplied identity in the existing commands. Legacy unified overloads remain for compatibility, but the active app routes are explicitly covered against using their default. Existing player allowlist, nonce validation, cancellation, session matching, unique-hardware discovery, per-call receipts, force-stop targets and removal confirmation are unchanged. Selected Cast close stays on its existing media-session path.

Scoped review verified only the two production Java files changed; the activity diff is three additions/two removals. No UI, Voice, animation, HA client, manifest or permission edits. Shield version advances 208 to 209. Wall's shell stays 207 and is not a new delivery request.

## Test evidence and build history

1. Test-only commit `4b7fcb13f35ea19ab01d8774334a2c2d38c616cf`, focused run `35222305035`, job `105205208176`: EXPECTED RED, 22 failed / 17 passed. Actual production Java compiled. Both Shield native close paths emitted `run-as com.boop.alpha1`; activity runtime-owner wiring was absent. Invalid-owner cases define safe validation for the new parameter, not an old injection vulnerability.
2. Fix commit `6caf25e30c9b0dbffbf1567e01d83b4e603f55e4`, focused run `35222770661`, job `105206748802`: SUCCESS for the same 39-case source regression suite. Owner split-contract run `35222849027`: SUCCESS. Both identity paths, restricted targets, cancellation/session/nonce cases and activity ownership wiring are covered.
3. Initial signed run `35222849116`, job `105207010155`: stopped at the historical change-list guard in `tests/test_playback_dance_v169.py`. The new close/artist tests passed (41 cases); 1,444 music worker/curve checks also passed before the guard flagged the two intentionally modified Java files. No signed artifact was produced by this failed run.
4. Test-only commit `6292bfe770c93e904e75100f5c4231e022437183`: eight added lines permit exactly the reviewed close-file bytes at `6caf25e`, not arbitrary changes. Original music tests and exact artist-delta guard are untouched. Review confirmed this commit changes only that test file.
5. Final signed run `35223237787`, job `105208294482`, built `6292bfe770c93e904e75100f5c4231e022437183`: SUCCESS. All 18 inherited stages ran and passed, including Voice/Home ownership, art/animation preservation, Johnny isolation, lyrics, music/audio and lifecycle guards. Source tests and split materialized integration tests passed. Both shell builds and actual APK package/version/signer/native/art checks passed. No checks were bypassed.

The source regression compiles the real Java gate with a reflection-only legacy API fallback so its pre-fix failure demonstrates the wrong command. It runs again against materialized source; activity source checks cover both runtime routes. These are non-visual logic/integration checks, not live Shield or rendered UI acceptance.

## Actual delivered artifact receipt

Artifact ID: `10498580480`.
Artifact name: `BOOP-Shield-v209-Wall-v207-Signed`.
Archive bytes: 152366602.
GitHub archive SHA-256: `2abf63276e1b3076fc9d12565dc54173245cb49ece3b7dbcf8f78c62cf47c4a0`.
Built source: `6292bfe770c93e904e75100f5c4231e022437183`.

Deliver Shield only:
- `BOOP-Shield-v209.apk`
- Package `com.boop.shieldoverlay`
- Version code 209; version name `1.2.209-shield`
- Size 160420197 bytes
- APK SHA-256 `a690fefa1bd600c8ddbfc34ca3e1e5d82d6aa775de5ce2dcd20746e847c0220e`
- Original permanent certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- All 16 packaged native libraries exactly match the verified accepted v206 baseline; approved assets retain their original bytes.

The artifact was actually downloaded and Shield APK extracted for the chat download. Archive hash/size matched GitHub metadata, `built-commit.txt` matched the receipt/source, and extracted APK hash/size matched the build receipt. Both archive and APK CRC checks passed. Package/version/certificate/native count were checked against the receipt and signer report; package/signer/native/art verification itself ran against the actual APK in CI. No local build or re-sign occurred.

The companion Wall build remains 207 and only demonstrates shared-shell compatibility; no new Wall install was requested. Do not deliver it as a replacement accepted Wall checkpoint.

## Human acceptance and continuity

Physical installation and native Close player/Close media behavior: PENDING Ryan. Ryan installs through Shield scrcpy, tests each button, and reports the result. The app/device was not driven, installed, reset, reconfigured or captured. No emulator, permission, HA, model, app-data/setup, signing-key or physical Pixel 10 change occurred. Selected Cast-only behavior is outside the confirmed cause of this native identity defect.

Prior v208 artist physical acceptance and v207 Bluetooth microphone acceptance remain unclaimed. Preserve natural Voice freeze and all latest accepted layout/art/animation work. Current root handoff/status/memory are reconciled in the final documentation-only publication. Previous v208 roots are archived unchanged at `docs/handoffs/2026-09-17-before-close-identity/`; older artist/v207 receipts and checkpoints remain intact. Main ownership did not change. Record the final documentation tip separately from the built source, and verify its live GitHub ref before declaring publication complete.
