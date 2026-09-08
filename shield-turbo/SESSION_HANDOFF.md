# SHIELD TURBO handoff

Owner branch: `shield-turbo-v01`. Package: `com.boop.shieldturbo`.
Current candidate: `0.2.0`, versionCode `2`.

## Current signed ADVANCED candidate, 2026-09-08

Ryan physically retested the corrected STANDARD build and reported the previously unreachable TURBO maintenance row was now selectable. He then reported only one remaining STANDARD bug: SHIELD -> DISPLAY + SOUND did nothing on the real NVIDIA Shield. He explicitly authorised fixing that and moving into the approved advanced/ADB TURBO phase. He also explicitly made physical hardware the visual acceptance authority and instructed GitHub CI not to judge visuals.

Current built source: `81f448c417e3b0b122df55edc6ea6886d1bde5b2`.
GitHub Actions workflow: `Build SHIELD TURBO`, run `34191343078`, job `101949873574`, success.
Signed candidate artifact: `SHIELD-TURBO`, ID `10042388530`, ZIP size `751853` bytes, artifact digest `sha256:db35da41d66a23b1087072e4fd0e1fbc3b4ba4ef4320804a6f20953a720fbb95`.
Test artifact: `SHIELD-TURBO-TESTS`, ID `10042389014`, ZIP size `37814` bytes, digest `sha256:ab3a00f16ec6d73e202447282a8b0d4fc80566dcac0533508441852e64299b71`.
APK path inside artifact: `shield-turbo/app/build/outputs/apk/release/app-release.apk`.
Extracted APK SHA-256: `7e5a769e68c88cf44749f7887c35e64d41369c3b76fe11a35a9473b350582636`.
Signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Fresh verification: 33 Kotlin unit tests passed with zero failures/errors/skips; seven source safety/regression contracts passed; Android lint succeeded; signed release assembly succeeded; package/version/Leanback/non-debuggable checks passed; signer fingerprint and APK archive integrity passed; API 30 install/launch/no-fatal smoke passed; artifact upload succeeded. CI no longer performs UI hierarchy dumps, focus/label assertions, screenshots or any other visual judgement.

## Changes in v0.2.0

### Shield DISPLAY + SOUND route

The dead generic display shortcut was replaced with a Shield-safe settings fallback chain. DISPLAY + SOUND now tries Android main Settings first, then generic display settings, then generic sound settings. The intent is to guarantee a usable NVIDIA Shield settings entry rather than silently doing nothing. This exact firmware route still requires Ryan's physical confirmation.

### ADVANCED / ADB TURBO first slice

A sixth `ADVANCED` control-centre section is present. It is locked unless the app genuinely holds `android.permission.WRITE_SECURE_SETTINGS`.

The setup screen shows the one-time command:

`adb shell pm grant com.boop.shieldturbo android.permission.WRITE_SECURE_SETTINGS`

The app checks the permission itself. It does not auto-grant it, store an ADB address/password/key, or keep an ADB socket/session open. Reinstalling or clearing app data may require the grant again.

Once granted, the first advanced control is Android UI animation speed:
- FAST 0.5x
- ANIMATIONS OFF
- RESTORE 1x

Those actions write only `window_animation_scale`, `transition_animation_scale`, and `animator_duration_scale`. RESTORE 1x is the explicit rollback. No CPU/GPU clock changes, overclocking, process killing, RAM cleaner, cache purge, other-app data clearing or fake boost score was added.

`WRITE_SECURE_SETTINGS` is a protected Android permission, so lint's `ProtectedPermissions` warning is suppressed only on that one manifest declaration with an explanatory comment. Lint remains enabled normally everywhere else.

## Physical evidence and rollback

Brightness remains physically proven on the bedroom NVIDIA Shield. Preserve the original brightness rollback checkpoint: source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK SHA-256 `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.

The first STANDARD control-centre build exposed D-pad/app-launch defects. The corrected STANDARD candidate was source `d277ebe713cdbe5298f6205ef34fa4d493ea2114`, run `34189880390`, artifact `10041897001`, APK SHA-256 `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1`. Ryan's next feedback said the controls were now selectable and identified DISPLAY + SOUND as the only remaining bug he had found, so preserve that corrected STANDARD receipt as the pre-ADVANCED fallback.

The v0.2.0 DISPLAY + SOUND fix and ADB TURBO animation controls are CI/signer green but not physically accepted yet. Ryan is the visual and real-Shield acceptance authority.

## Next physical check

Install v0.2.0. Confirm DISPLAY + SOUND opens a usable Shield settings surface. Open ADVANCED before the ADB grant and confirm it stays locked. Grant `WRITE_SECURE_SETTINGS` once, recheck access, then test 0.5x, animations off and RESTORE 1x. Confirm brightness and existing STANDARD controls did not regress. Record firmware-specific results rather than inferring them from emulator behavior.

Continue using the established secret-backed `boop-dev` signer only. Never expose private key material or BOOP relay credentials. SHIELD TURBO remains independent of unified BOOP.

Documentation-only commits after built source `81f448c417e3b0b122df55edc6ea6886d1bde5b2` do not replace the signed APK receipt above. Distinguish branch documentation HEAD from the built source.
