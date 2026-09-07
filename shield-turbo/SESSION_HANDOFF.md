# SHIELD TURBO handoff

Owner branch: `shield-turbo-v01`. Package: `com.boop.shieldturbo`.
Candidate: `0.1.0`, versionCode `1`.
Verification: CI, established signer and installed-release emulator smoke tests passed. Physical NVIDIA Shield acceptance of the new brightness control is pending.

## Brightness live-test candidate, 2026-09-07

This is the current source/build receipt and supersedes the older read-only candidate for live testing.

- Built source: `192879ba87082b9daf5275c89a706bfd5f1106d2`.
- GitHub Actions workflow: `Build SHIELD TURBO`, run `34129557124`, job `101766129743`, completed successfully.
- Run URL: https://github.com/ryankemble2006-web/boop/actions/runs/34129557124
- Signed candidate artifact: `SHIELD-TURBO`, ID `10021629767`, ZIP size `693490` bytes.
- Artifact URL: https://github.com/ryankemble2006-web/boop/actions/runs/34129557124/artifacts/10021629767
- APK in the bundle: `shield-turbo/app/build/outputs/apk/release/app-release.apk`.
- APK SHA-256: `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.
- Artifact ZIP SHA-256: `918c3f47787d13b14ab050ab3d22f3bcb33fb9632f17b6f2355019b39c507ac3`.
- Signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`, matching the established BOOP development signer.
- Test-report artifact: `SHIELD-TURBO-TESTS`, ID `10021630297`, ZIP size `29423` bytes, digest `6f438e3c5af7f0a9e5519de29946bf88f0f536d76e24950822a82794f71f6ca2`.

Fresh run evidence: 17 Kotlin unit tests passed with zero failures/errors/skips; four source safety contracts passed; Android lint finished with zero errors and nine non-blocking warnings; signed release assembly succeeded; package/version/Leanback entry/non-debuggable identity checks passed; signer comparison and APK archive integrity passed. The temporary signing key was removed after assembly.

The release APK installed successfully in an API 30 emulator. Cold launch and warm relaunch succeeded, centre-to-analysis passed (`EMULATOR_RELEASE_ANALYSIS=PASS`), D-pad navigation to a result card passed (`EMULATOR_DPAD_CARD_FOCUS=PASS`), and the smoke script found no fatal exception for `com.boop.shieldturbo`.

The brightness feature is now intentionally outside the original read-only diagnostic boundary. It provides a remote-focusable 10–100% picture-brightness control. `100%` removes/avoids the dim overlay and leaves the picture untouched. Values below 100% require Android's display-over-other-apps permission and are applied by the private, non-exported `BrightnessService` using an application overlay. The chosen percentage is persisted locally. Manifest permissions are now `ACCESS_NETWORK_STATE` plus `SYSTEM_ALERT_WINDOW`; there are no receivers.

CI did not prove actual dimming on NVIDIA Shield hardware, the Shield permission UI, cross-app persistence, Tegra-specific behavior, or recovery across Shield sleep/reboot. Those are the live-test targets in `MEMORY.md`. Do not call the brightness feature physically accepted until Ryan tests this exact APK.

The nine lint warnings are non-blocking polish items: SDK currency, fixed landscape orientation, pluralisation, newer backup configuration, missing dedicated app icon, and percentage-text localisation warnings. No checks were disabled to make the run green.

## Implementation and regression history

Ryan requested GitHub-only chat-mode development, existing signing keys only, and a separately installable utility in this repository. No BOOP runtime or unified-app changes are authorised by this utility task.

The original read-only candidate was built from `7bb3bf8fce1910f20165b3a7649a70a634528dab` in run `34124583278`; its signed artifact was `10019673866`, APK SHA-256 `b203358f8babc094c274096ec9852dd4015769bafb168ecfd8486307d4dad24f`. That exact older APK was later published as prerelease `shield-turbo-v0.1.0`. It does not contain the brightness feature and must not be confused with the current Actions artifact above.

Brightness development used a red test first at `5d131e4d47ae633d31c6753240060e434f1c7942`, where `Brightness` was intentionally unresolved. The first implementation series reached `80b8019a441f710f94ba82c488b2cac548ba5f41`, then CI exposed two concrete integration defects: Android resource parsing of an apostrophe in `access_explanation`, and the old source contract still forbidding all services. Commit `192879ba87082b9daf5275c89a706bfd5f1106d2` fixed those root causes by using a valid Android string and narrowing the contract to allow exactly the private non-exported brightness service. Run `34129557124` is the green verification receipt.

## Product and verification limits

Diagnostics remain on-demand and local. CPU frequency is not CPU load; generic thermal zones are not automatically named CPU/GPU sensors. ADB helper/setup and arbitrary tuning remain deferred. The brightness overlay is the one explicitly authorised setting-changing feature in this candidate.

Physical NVIDIA Shield acceptance is pending. Record Ryan's result against the exact source commit, run, artifact ID and APK checksum above. A successful emulator run is not a hardware rollback checkpoint.

The existing GitHub `BOOP_DEV_KEYSTORE_B64`, `BOOP_DEV_STORE_PASSWORD`, `BOOP_DEV_KEY_PASSWORD` secrets and alias `boop-dev` are used inside the runner. Private signing material is never published; temporary key material is removed after assembly. No relay or other BOOP credentials are used.

## Delivery note

For brightness live testing use Actions artifact `10021629767` from run `34129557124`. The older direct prerelease URL points to the pre-brightness APK and is retained only as historical evidence. Do not repoint that existing tag/release to new bytes.

Documentation-only commits after built source `192879ba87082b9daf5275c89a706bfd5f1106d2` do not rebuild or replace the tested APK. Distinguish the current documentation branch HEAD from the built-source receipt above.
