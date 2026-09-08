# SHIELD TURBO handoff

Updated 2026-09-08. Owner branch `shield-turbo-v01`. Independent package `com.boop.shieldturbo`.
Current signed candidate: **v0.4.1 / versionCode 6**, machine-verified. Physical Startup Manager acceptance remains pending.

## Latest physical feedback and diagnosed blocker

Ryan installed v0.4.0 and reached Choose an app, but selecting an app showed its package name and the message beginning "Turbo changes one selected app" with no usable next action. That is a failed per-app action menu, not evidence that startup blocking was applied or tested. The goal remains suppressing four user-selected Kodi forks that he reports wake after Shield boot, while preserving manual launch.

The root cause is in `StartupManagerActivity.choose`: the same Android AlertDialog builder called both `setMessage` and `setItems`. Android's standard dialog supports message OR list content, not both; the message displaced the action list. Primary reference: https://developer.android.com/develop/ui/views/components/dialogs (Adding a list). This is a reproducible API misuse, not another guessed NVIDIA settings component.

Display & Sound and Accessibility remain unresolved and parked. Do not spend this repair on them. Earlier physical evidence remains limited to bedroom brightness, corrected STANDARD maintenance selectability and Developer Options opening correctly.

## Exact signed v0.4.1 receipt

- Built source: `0961153e5dea94c38027cdf31530f500c5b29573`.
- Workflow `Build SHIELD TURBO`: run `34204102153`, job `101989443983`, completed **success**.
- JVM tests: **58 passed**, zero failures/errors/skips, verified from the downloaded JUnit XML.
- Python source/API/security checks: **13**, successful gate, including the message/list regression. No appearance certification.
- Android lint: **0 errors, 22 warnings**, verified from lint XML.
- Signed artifact `SHIELD-TURBO`: ID `10047107169`, ZIP size `744535` bytes.
- Artifact ZIP SHA-256: `4a1f31abe78e7876414c3524d98701d96ec9fbf3a2c46eb27280ab97eb8c6ee5`.
- Test artifact `SHIELD-TURBO-TESTS`: ID `10047154897`, ZIP size `88273` bytes, SHA-256 `f98ef2b3092f56cf3baabb24576d984d95fe7bcb0055a08da3f3e482bff4e752`.
- Delivered APK: `Shield-Turbo-v0.4.1.apk`, **2283246 bytes**.
- APK SHA-256: `c7bc147a70378dfe62a14e542aeb5dcb1036fbe6818551e98ddcf6fe061793fa`.
- Permanent signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Package/version/Leanback/non-debuggable, cryptographic signing and archive checks passed in CI.
- Downloaded artifact digest, extracted APK digest, source/version/signer receipts and both ZIP/APK CRC checks passed before providing an actual APK link.
- Post-upload nonvisual emulator install/cold launch/process/Back/warm launch/no-fatal check passed. The verified APK was linked before waiting for this slower check.
- **No visual tests**: no screenshot, hierarchy dump, image/golden comparison or appearance/layout judgment. Ryan owns real-Shield visual and remote acceptance.

## Changes and scope

1. Per-app actions now use a list-only dialog with the app title. The conflicting package-name information message is removed. Existing Block Startup, separately confirmed Hard Block, Undo, Launch and App Info callbacks are preserved.
2. `StartupAppLabels` prefers a meaningful launcher label, then a meaningful application label, before the exact package ID. A launcher package-name fallback no longer prevents trying the application label. Truly missing labels remain the real package ID; no guessed app names or hardcoded Kodi identities are introduced.
3. Cancel is allowed through the busy-operation guard; ordinary action buttons remain guarded. Remote Back cancels an in-flight Startup Manager task instead of requiring activity exit. Cancellation is not Undo: a command already sent may have taken effect.
4. Version/package verification advanced to v0.4.1/code 6. The workflow retains the same nonvisual gates and signer, with an explicit manual-visual-acceptance comment.

Runtime changes are confined to StartupManagerActivity and the small label-resolution helper. **LocalBridge, AdbWire, StartupPolicy, StartupLedger, manifest permissions, brightness, other power tools and firmware routes are unchanged.** Existing saved Undo records retain their format and preferences. Main and other BOOP bodies were not edited.

## Regression and review evidence

Dialog regression was committed first at `47abd6136448ee7ef6771729584d6471569f6c9f`. Run `34203516856`, job `101987591211`, passed 51 JVM tests and failed exactly one of 13 Python checks: `test_startup_actions_do_not_compete_with_message_content`. Its assertion printed the precise conflicting builder. The checker also tests both call orders and separately valid dialogs; it inspects API usage, not rendering or focus.

The previous naming expression was separately reproduced in a local standalone Kotlin fixture: a package-name launcher fallback masked an available application label. The new helper passed seven standalone data cases and seven JUnit cases in the final build. No Android UI was started for those local checks.

The published diff was reviewed directly against `776560b2a7060e612d6ddfa2cd22d29ab2cdee89`: six files including tests/version/workflow, no unrelated runtime edits. No independent reviewer or physical-device test is implied by that review.

## Startup policy and rollback retained

`BLOCK STARTUP / KEEP LAUNCHABLE` changes only RUN_IN_BACKGROUND and RUN_ANY_IN_BACKGROUND to ignore for one chosen safe user app, after saving its original state. It leaves the package enabled and reads the values back. These are background restrictions, not a universal guarantee against every boot trigger or a free-RAM score. Real Kodi boot behavior still needs testing.

`HARD BLOCK / DISABLE APP` separately confirms disabling the selected package for the current Android user. It cannot launch until restored; no data, files, caches or logins are cleared. Do not make this the default.

The first recorded original app-op/enabled state wins. Per-app Undo verifies restore before removing the ledger entry; failed restores retain records. Undo All attempts each recorded app independently. No boot receiver or background service was added. Android stores the restrictions; Turbo must not become another boot-starting app merely to reapply them.

System/updated-system/NVIDIA/Android/Google-core/BOOP exclusions remain. No QUERY_ALL_PACKAGES, bulk cleaner, fake performance score, root/bootloader/clock/governor changes or automatic hard blocking.

## Next physical check

Install v0.4.1 over v0.4.0. Open ADVANCED -> STARTUP MANAGER, choose ONE Kodi fork and confirm the actual Block Startup / Hard Block / Launch / App Info action list is now available, instead of the old information-only message. Use ENABLE ADB TURBO first if the local debugging setup is not authorised. Choose BLOCK STARTUP / KEEP LAUNCHABLE and report the returned result. Only after read-back succeeds, reboot the Shield, check whether that fork self-starts and then launch it manually from its ordinary icon. Test the other forks only after the first succeeds. Verify Undo and Cancel separately. Do not equate the repaired dialog or CI pass with proven boot suppression.

## Historical receipts

- v0.4.0: source `1358925716cf2c834171b767dd94f08d0c49e013`, run `34201159209`, job `101980019981`, signed artifact `10045926945`, APK `cf12ccdfec929424ad89f6f5302c86f7b1331ef809ceef336fc0bda5d344657a`, 2281902 bytes. 51 JVM tests/10 source checks, lint 0 errors/22 warnings, signing and launch passed, but the per-app dialog then failed physically. Complete earlier startup TDD receipts remain in the handoff at `776560b2a7060e612d6ddfa2cd22d29ab2cdee89`.
- v0.3.0: source `ac5f79cd9138553a27df776e6b47e685f2cbf0ff`, run `34196792381`, artifact `10044276954`, APK `fff6b791235b05938dcb34a886c99a97d4563132cd46db79493dd422b0f25846`.
- v0.2.1: source `0e00f7c44758aa4192e7664b61d477b11494942d`, run `34192942800`, artifact `10042893415`, APK `0572aa2e66481703a91490078f6291350824bd266cbd44a834bfc4015cfada27`; Display & Sound failed physically.
- v0.2.0: source `81f448c417e3b0b122df55edc6ea6886d1bde5b2`, run `34191343078`, artifact `10042388530`, APK `7e5a769e68c88cf44749f7887c35e64d41369c3b76fe11a35a9473b350582636`; Developer Options physically worked, Display & Sound fallback rejected.
- Corrected STANDARD: source `d277ebe713cdbe5298f6205ef34fa4d493ea2114`, run `34189880390`, artifact `10041897001`, APK `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1`; maintenance selectability confirmed.
- Original bedroom brightness: source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`; brightness confirmed.

Use only the existing secret-backed boop-dev signer. Never repoint historical checkpoints. Documentation commits after built source `0961153e5dea94c38027cdf31530f500c5b29573` do not identify a different APK. This chat did not install on a physical Shield, grant device access or synchronise Ryan's laptop; GitHub publication is not device deployment.
