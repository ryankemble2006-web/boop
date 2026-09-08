# SHIELD TURBO handoff

Owner branch: `shield-turbo-v01`. Package: `com.boop.shieldturbo`.
Current candidate: `0.2.1`, versionCode `3`.

## Latest real-Shield feedback, 2026-09-08

Ryan tested v0.2.0 and reported that SHIELD -> DISPLAY + SOUND opened general Android Settings, not the proper NVIDIA Shield Display & Sound page. This is a failed destination, not a successful fix. He separately confirmed ADVANCED -> Developer Options opens correctly. Preserve that working route. This feedback does not establish that the ADB permission grant or animation controls have been tested.

Earlier bedroom-Shield evidence remains valid: brightness worked, and the corrected STANDARD maintenance row became selectable. Ryan is the visual and real-device acceptance authority. GitHub must not judge visuals.

## Current signed v0.2.1 candidate

- Built source: `0e00f7c44758aa4192e7664b61d477b11494942d`.
- Workflow: `Build SHIELD TURBO`, run `34192942800`, job `101954559616`.
- Required release gates observed successful: Kotlin unit tests, source safety contracts, Android lint, signed release assembly, package/version/Leanback/non-debuggable checks, signer fingerprint and archive verification.
- Signed artifact: `SHIELD-TURBO`, ID `10042893415`, ZIP size `709601` bytes.
- Artifact ZIP SHA-256: `2b4e8870ac728f44b5708c58710ad7844802fb3686a2113e8b85f3c3d8161331`.
- APK inside ZIP: `shield-turbo/app/build/outputs/apk/release/app-release.apk`.
- Delivered filename: `Shield-Turbo-v0.2.1.apk`, size `2198446` bytes.
- APK SHA-256: `0572aa2e66481703a91490078f6291350824bd266cbd44a834bfc4015cfada27`.
- Signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The downloaded ZIP digest, APK digest, built-source receipt, package/version/signing receipts and both ZIP/archive CRC checks were verified before delivering the actual APK link. The signed candidate was uploaded before the separate nonvisual emulator install/launch check; that check was still running at initial delivery. Do not infer its final result from the artifact. Read the run when a later status is needed.

## What this patch changes

The v0.2.0 root cause was explicit: `SystemShortcut.DISPLAY_SOUND` listed `android.settings.SETTINGS` first, so the resolver chose general Settings immediately. Reordering generic fallbacks would not establish the correct combined page.

v0.2.1 instead constructs an explicit MAIN intent targeting:

`com.android.tv.settings/com.android.tv.settings.device.displaysound.DisplaySoundActivity`

There are no general Settings, display-only or sound-only fallbacks for this button. The existing activity launch handler reports an unavailable shortcut if firmware rejects the component. Explicit starts do not require a package-visibility preflight; the actual Android launch and exception path are authoritative. No new permissions or manifest queries were added.

Runtime edits are confined to `system/SystemShortcut.kt` and `system/SystemRoutes.kt`. Developer Options retains `android.settings.APPLICATION_DEVELOPMENT_SETTINGS`. MainActivity, brightness, APPS, diagnostics, permission handling and ADB animation controls are unchanged. Build/version checks were advanced to v0.2.1/code 3, and artifact upload was moved ahead of the slower nonvisual smoke step.

The regression was verified RED before the fix: source `4619892e4167a2efff04914c66748e86ef32544e`, run `34192703028`, job `101953850917`, ran 34 Kotlin tests with exactly one assertion failure, `displaySoundMustNotSilentlyOpenUnrelatedSettings`. All seven source contracts passed. The production patch passes the unit-test gate and also tests the exact component and the unchanged other shortcuts. These are routing tests, not visual acceptance.

## Evidence boundary and next physical check

The target component is evidenced by AOSP TV Settings source, including `LineageOS/android_packages_apps_TvSettings`, `lineage-18.1`, `Settings/src/com/android/tv/settings/device/displaysound/DisplaySoundActivity.java`. The exported activity is also declared in that repository's `Settings/AndroidManifest.xml` at `0e62bf2a4a6801b5b15b68cc49b04ea1d501acd6`. Android's package-visibility use-cases documentation supports attempting the explicit start and handling launch failure.

This is NOT a capture of Ryan's stock NVIDIA firmware. The exact native NVIDIA page remains physically unconfirmed on v0.2.1. Install over v0.2.0 and press SHIELD -> DISPLAY + SOUND. A general settings page is not acceptance. If the direct component is unavailable, obtain minimal on-device component/export evidence privately before inventing further class names or adding broader fallbacks. Do not commit raw device dumps or third-party firmware APKs.

Developer Options is already physically confirmed on v0.2.0 and was deliberately left untouched. ADB animation actions and grant persistence still require separate physical evidence; do not mark them accepted merely because Developer Options opens.

## ADVANCED contract retained

The sixth ADVANCED section remains locked unless `android.permission.WRITE_SECURE_SETTINGS` is genuinely granted. Setup command:

`adb shell pm grant com.boop.shieldturbo android.permission.WRITE_SECURE_SETTINGS`

No auto-grant, ADB address/password/key storage or persistent ADB socket/session. FAST 0.5x, ANIMATIONS OFF and RESTORE 1x write only `window_animation_scale`, `transition_animation_scale` and `animator_duration_scale`. RESTORE 1x is an explicit normal-speed preset, not a backup of a user's prior custom values. The protected-permission lint suppression stays limited to that one intentional manifest declaration.

No CPU/GPU changes, overclocking, process killing, blanket RAM cleaning, cache purge, other-app data clearing or fake performance score is present. Preserve the existing 10-100% brightness overlay behavior and private non-exported service.

## Historical receipts and rollback

- v0.2.0: source `81f448c417e3b0b122df55edc6ea6886d1bde5b2`, run `34191343078`, job `101949873574`, success. Signed artifact `10042388530`, ZIP `751853` bytes, digest `db35da41d66a23b1087072e4fd0e1fbc3b4ba4ef4320804a6f20953a720fbb95`; test artifact `10042389014`, digest `ab3a00f16ec6d73e202447282a8b0d4fc80566dcac0533508441852e64299b71`. APK `7e5a769e68c88cf44749f7887c35e64d41369c3b76fe11a35a9473b350582636`. Machine checks passed 33 Kotlin tests, seven source contracts, lint, signer/package and basic launch. Its general-Settings Display & Sound fallback was subsequently rejected by Ryan.
- Corrected pre-ADVANCED STANDARD: source `d277ebe713cdbe5298f6205ef34fa4d493ea2114`, run `34189880390`, artifact `10041897001`, APK `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1`. Fixed maintenance focus and package-name app dialogs. Ryan confirmed controls became selectable.
- Physically proven bedroom brightness checkpoint: source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.
- The old `shield-turbo-v0.1.0` prerelease is pre-brightness and must not be repointed. Older-version APKs are historical rollback sources; do not promise in-place downgrade installation.

Continue with the existing secret-backed `boop-dev` signer only. Never expose private keys or BOOP relay credentials. SHIELD TURBO remains independent of unified BOOP. No laptop or physical-device synchronization was performed by this chat session.

Documentation-only commits after built source `0e00f7c44758aa4192e7664b61d477b11494942d` do not replace this APK. Distinguish branch documentation HEAD from built source. Handoff, status and memory on this app branch are the applicable context for this patch; the main ownership routing is unchanged.
