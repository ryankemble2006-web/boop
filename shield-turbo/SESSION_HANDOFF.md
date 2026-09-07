# SHIELD TURBO handoff

Owner branch: `shield-turbo-v01`. Package: `com.boop.shieldturbo`.
Candidate: `0.1.0`, versionCode `1`.
Verification: CI, established signer and installed-release emulator smoke tests passed. Physical NVIDIA Shield acceptance is pending.

## Verified release receipt, 2026-09-07

- Built source: `7bb3bf8fce1910f20165b3a7649a70a634528dab`.
- GitHub Actions run: `34124583278`, job `101750141430`, completed successfully.
- Run URL: https://github.com/ryankemble2006-web/boop/actions/runs/34124583278
- Signed candidate artifact: `SHIELD-TURBO`, ID `10019673866`, ZIP size `689584` bytes.
- Artifact URL: https://github.com/ryankemble2006-web/boop/actions/runs/34124583278/artifacts/10019673866
- APK in the bundle: `shield-turbo/app/build/outputs/apk/release/app-release.apk`.
- APK SHA-256: `b203358f8babc094c274096ec9852dd4015769bafb168ecfd8486307d4dad24f`.
- Artifact ZIP SHA-256: `b963906192f9d12e907835f7496e24cfe8c6a2d717dc8489d011bcbf9c1501b2`.
- Signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`, matching the established BOOP development signer.
- Test-report artifact: `SHIELD-TURBO-TESTS`, ID `10019674284`.

Observed in the completed job logs: 15 Kotlin unit tests passed with zero failures/errors/skips, four source safety guards passed, Android lint finished with zero errors and six non-blocking warnings, release assembly succeeded, package/version/TV entry/non-debuggable identity checks passed, signer comparison passed and APK archive integrity passed. The temporary signing key was removed after assembly.

The signed release APK was installed in an API 30 emulator with a handheld `pixel_2` profile. Cold launch succeeded; centre activated analysis (`EMULATOR_RELEASE_ANALYSIS=PASS`); D-pad Down reached a measurement card (`EMULATOR_DPAD_CARD_FOCUS=PASS`); Back followed by a warm relaunch succeeded. The script found no fatal exception associated with this app during that smoke test. This is not an NVIDIA Shield hardware or Tegra sensor test, an exhaustive navigation test, or a power/performance benchmark.

The six lint warnings concern target/compile SDK currency, fixed orientation, pluralisation, newer backup configuration and the missing dedicated application icon. No checks were disabled to turn the build green. Keep these as polish work rather than claiming a warning-free build.

This documentation-only receipt does not rebuild or replace the tested APK. Subsequent documentation heads must be distinguished from the built-source commit above. The artifact bundle was retrieved through the GitHub connector for delivery in chat; no local build, physical deployment or laptop synchronisation is claimed.

## Implementation and regression history

Ryan requested GitHub-only chat-mode development, existing signing keys only, and a separately installable utility in this repository. No BOOP runtime or unified-app changes are authorised by this utility task.

The initial candidate `5f9e636b7851f0512792b6296aacc12dcd007f53` compiled and passed its four initial tests, but run `34123083174` failed Android lint on hard-coded view IDs. Signing was not reached; that run did not produce a signed candidate.

Regression commit `2d0d5776d5e83ee5c15eb8999b272390a0af3591` added real edge-case tests, source guards and installed-release remote-input verification. A concurrent worker then pushed `b7a9ee279be97d9b10f744f3c899ff8468b23065`, fixing numeric IDs with generated IDs. That change is preserved in ancestry; stable resource IDs now identify persistent controls and generated IDs identify result cards.

Red-test evidence: run `34123950576`, job `101748196618`, executed 15 Kotlin tests with seven failures (invalid CPU value, temperature units/non-finite values, isolated probe exceptions, privilege evidence failure) and four source guards with three failures. Repair `7bb3bf8` addressed those reproduced cases, moved scanning off the UI thread, removed unnecessary screen retention and made result cards D-pad focusable. It retained the original app permission boundary. The green run above verifies the repaired candidate.

The commit comparison against base `92576982a74f7beff0056e35bd1a6e6df19534cf` confirmed that implementation changes were confined to `shield-turbo/**` and `.github/workflows/shield-turbo.yml`. Do not merge inherited old BOOP runtime from main into `boop-unified`.

## Product and verification limits

v0.1 is read-only diagnostics, not an optimisation release. ADB setup/helper and actual tuning are deferred. Tier detection reports app authority, not guessed device root status. Only network-state permission is requested. No app installations or permission changes on Ryan's physical devices are part of this task.

Physical NVIDIA Shield acceptance is pending. The next safe step is the real-device checklist in `MEMORY.md`: launcher visibility, remote scan/detail/navigation, genuine exposed CPU/thermal sources, leaving and reopening the app, and normal sleep/media/BOOP behaviour. Record Ryan's physical result against the exact APK and source receipt, not just the latest filename.

The existing GitHub `BOOP_DEV_KEYSTORE_B64`, `BOOP_DEV_STORE_PASSWORD`, `BOOP_DEV_KEY_PASSWORD` secrets and alias `boop-dev` are used inside the runner. Private signing material is never published; temporary key material is removed after assembly. No relay or other BOOP credentials are used.

## Direct APK publication, 2026-09-07

Delivery is now available without unpacking the Actions bundle:

- Direct APK: https://github.com/ryankemble2006-web/boop/releases/download/shield-turbo-v0.1.0/SHIELD-TURBO-v0.1.0.apk
- Prerelease: https://github.com/ryankemble2006-web/boop/releases/tag/shield-turbo-v0.1.0
- Release ID `384094141`; APK asset ID `548800195`; APK size `2142650` bytes.
- Publication workflow `.github/workflows/shield-turbo-publish-v01.yml` added at `cbfd286427fa164f20d0ff0d97af19e69a20722a`.
- Publication run `34125252347` completed successfully. It validated the successful source run, pinned source SHA, original artifact ID/digest and exact APK checksum before publishing the existing bytes. It did not rebuild, re-sign, access signing secrets or deploy to a physical device.
- GitHub's uploaded APK asset digest was fetched after publication and equals the original APK SHA-256 `b203358f8babc094c274096ec9852dd4015769bafb168ecfd8486307d4dad24f`.
- `SHA256SUMS.txt` and sanitized `BUILD-RECEIPT.txt` accompany the APK. No diagnostic dumps, signing material or unrelated assets were published by this step.

The new tag targets the exact built source `7bb3bf8fce1910f20165b3a7649a70a634528dab`. Publication uses a separate prerelease with `--latest=false`; it neither repoints an old checkpoint nor replaces BOOP's latest stable release. The publisher refuses to overwrite an existing release/tag. Do not rerun it to modify this checkpoint.

Current application source and build verification remain the receipt above. Only delivery workflow and documentation changed in this publication pass. Concurrent documentation commits were preserved. Physical acceptance is still pending.
