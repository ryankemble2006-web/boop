# SHIELD TURBO status

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Package: `com.boop.shieldturbo`.

## Current live-test candidate

**v0.2.0 (versionCode 2): DISPLAY + SOUND firmware-route fix plus the first ADB TURBO advanced controls are built, signed, CI-verified and basic install/launch smoke-tested. Physical NVIDIA Shield acceptance is pending.**

| Evidence | Result |
| --- | --- |
| Built source | `81f448c417e3b0b122df55edc6ea6886d1bde5b2` |
| GitHub Actions | Run `34191343078`, job `101949873574`: success |
| Kotlin unit tests | 33 passed, 0 failures/errors/skips |
| Source safety/regression contracts | 7 passed |
| Android lint | success; one narrowly documented `ProtectedPermissions` suppression on intentional ADB grant declaration |
| Signed release | Established secret-backed `boop-dev` signer; certificate match passed |
| APK checks | Package `com.boop.shieldturbo`, v0.2.0/versionCode 2, Leanback entry, non-debuggable release and archive integrity passed |
| Emulator | Install, launch, relaunch and no app fatal exception passed; no visual assertions performed |
| Signed artifact | `SHIELD-TURBO`, ID `10042388530`, digest `sha256:db35da41d66a23b1087072e4fd0e1fbc3b4ba4ef4320804a6f20953a720fbb95` |
| Test artifact | `SHIELD-TURBO-TESTS`, ID `10042389014`, digest `sha256:ab3a00f16ec6d73e202447282a8b0d4fc80566dcac0533508441852e64299b71` |
| APK SHA-256 | `7e5a769e68c88cf44749f7887c35e64d41369c3b76fe11a35a9473b350582636` |
| Signer certificate SHA-256 | `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` |

## Physical evidence

Brightness is physically proven on the bedroom NVIDIA Shield. Ryan then physically retested the corrected STANDARD build and reported the maintenance controls were now selectable. His next report identified only one remaining bug found in that pass: SHIELD -> DISPLAY + SOUND did nothing.

v0.2.0 changes DISPLAY + SOUND to a Shield-safe fallback route beginning at Android main Settings. Real firmware confirmation of that route is pending.

## ADVANCED / ADB TURBO

Ryan explicitly authorised moving into advanced work. v0.2.0 adds an `ADVANCED` section that remains locked until `android.permission.WRITE_SECURE_SETTINGS` is genuinely granted with:

`adb shell pm grant com.boop.shieldturbo android.permission.WRITE_SECURE_SETTINGS`

After the one-time grant, SHIELD TURBO exposes three reversible animation controls: 0.5x, off, and restore 1x. Only Android's three animation scale settings are written. No persistent ADB connection, root invocation, CPU/GPU tuning, process killing, blanket RAM cleaning, cache purge, other-app data clearing or fake boost scoring is present.

## CI visual boundary

By explicit user instruction, GitHub is not the visual acceptance authority. The workflow contains no UI hierarchy dumps, screenshot checking, focus/label visual assertions or appearance tests. CI still performs unit/source tests, lint, signed build, package/signature/archive verification and basic install/launch/no-crash smoke. Ryan performs real Shield visual and remote testing.

## Rollback receipts

Physically proven brightness checkpoint: source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK SHA-256 `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.

Pre-ADVANCED corrected STANDARD candidate: source `d277ebe713cdbe5298f6205ef34fa4d493ea2114`, run `34189880390`, artifact `10041897001`, APK SHA-256 `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1`.

The older published `shield-turbo-v0.1.0` prerelease is pre-brightness and historical only. Do not repoint it.
