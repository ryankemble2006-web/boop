# SHIELD TURBO durable decisions

Updated 2026-09-08. Read `SESSION_HANDOFF.md` for exact receipts/provenance and `STATUS.md` for the current candidate. This file records durable rules and physical evidence.

## Physical evidence

Ryan confirmed brightness on the bedroom NVIDIA Shield. Preserve the original checkpoint: source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK SHA-256 `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.

The first STANDARD build exposed unreachable TURBO maintenance controls and APPS OK displaying package-name dialogs. Corrected STANDARD: source `d277ebe713cdbe5298f6205ef34fa4d493ea2114`, run `34189880390`, artifact `10041897001`, APK `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1`. Ryan confirmed the controls became selectable and next identified DISPLAY + SOUND as the remaining bug he had found.

Latest v0.2.0 feedback: DISPLAY + SOUND opens general Android Settings, which is the wrong destination. ADVANCED -> Developer Options opens correctly. The latter is real hardware acceptance of that shortcut only, not evidence that the ADB grant or animation actions work. Preserve this distinction.

## Identity and ownership

Independent utility in `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`. The shared BOOP repository is hosting convenience; this is not a BOOP body or a replacement for unified `com.boop.alpha1`. Keep runtime work inside this app and its dedicated workflow.

Ryan approved GitHub chat-mode development/signing. Do not insist on a Work-mode handoff when connected GitHub tools can perform the task. Do not claim that GitHub publication synchronized the laptop or installed anything on physical devices.

## Product rules

Diagnostics are honest on-demand snapshots. No fake speed scores, blanket RAM cleaners, overclocking, arbitrary governor changes, process killing, other-app data clearing or pretend network tweaks. CPU frequency is not load, available memory is not a performance score, and a generic thermal zone is not automatically the CPU/GPU sensor.

Brightness remains 10-100%, with 100% removing the overlay. Below 100% uses display-over-other-apps permission and the private non-exported BrightnessService. Preserve its non-intercepting input behavior and existing operation.

## STANDARD remote and settings behavior

Every useful control must be reachable with the Shield remote. A visible but unreachable button is a functional failure.

APPS OK launches immediately using the Leanback launch intent first and ordinary Android launch as fallback. Hold OK may open App Info. No package-name dialog on normal launch. TURBO maintenance routes to storage, manage-apps and restarting this app; they are not cleaners or process killers.

A button labeled DISPLAY + SOUND must target the combined native TV page. Do NOT silently substitute general Android Settings, display-only settings or sound-only settings and call that a fix. The old v0.2.0 broader-fallback policy was rejected by physical feedback and is superseded.

v0.2.1 explicitly targets `com.android.tv.settings/com.android.tv.settings.device.displaysound.DisplaySoundActivity`. This component is evidenced in AOSP TV Settings. NVIDIA firmware support remains subject to Ryan's physical check. If firmware does not expose it, show an unavailable result rather than an unrelated screen, and obtain minimal private on-device evidence before adding more candidates. Never publish raw device dumps or third-party firmware APKs.

Developer Options is physically confirmed and keeps the existing `android.settings.APPLICATION_DEVELOPMENT_SETTINGS` route. The v0.2.1 change is isolated to Display & Sound routing; no advanced, brightness or UI layout work is bundled into it.

## ADVANCED / ADB TURBO contract

Ryan authorised advanced work after the corrected STANDARD retest. ADB TURBO uses the actual manual grant of `android.permission.WRITE_SECURE_SETTINGS`, not a permanent shell connection:

`adb shell pm grant com.boop.shieldturbo android.permission.WRITE_SECURE_SETTINGS`

Check the permission before enabling advanced actions. Enabled debugging, usage access, a su executable or a root-manager app does not prove authority. Do not auto-grant, store ADB addresses/passwords/private keys or retain an ADB session merely to keep this capability. Grant persistence/reinstallation behavior needs device evidence before promises are made.

Animation presets write only `window_animation_scale`, `transition_animation_scale`, and `animator_duration_scale`: FAST 0.5x, ANIMATIONS OFF (0.0), and RESTORE 1x (1.0). The last button returns to normal speed; it does not restore previously backed-up custom scales. No CPU/GPU tuning, process killing, cache purge or private data clearing is included.

The protected-permission lint suppression is narrowly attached to the intentional WRITE_SECURE_SETTINGS manifest declaration. Do not globally disable ProtectedPermissions or introduce a blanket baseline to hide real problems.

## Verification and delivery

Ryan owns visual and real-Shield acceptance. GitHub must not use screenshots, UI hierarchy dumps, golden-image checks or source-string appearance certification. Keep functional routing/logic tests, safety checks, lint, compilation and signer/package/archive verification. Basic nonvisual process/install/launch smoke is permitted but does not prove firmware routing.

Upload the verified signed APK before slower nonvisual emulator work. Deliver the actual extracted APK, check its path and checksum, and distinguish artifact-release status from later smoke status. Never invent an APK link or infer hardware success from CI.

Current v0.2.1 source `0e00f7c44758aa4192e7664b61d477b11494942d`, run `34192942800`, artifact `10042893415`, APK SHA-256 `0572aa2e66481703a91490078f6291350824bd266cbd44a834bfc4015cfada27`. Required release gates passed; exact native Shield page is not yet physically accepted. The regression first failed on source `4619892e4167a2efff04914c66748e86ef32544e`, run `34192703028`.

Previous v0.2.0 source `81f448c417e3b0b122df55edc6ea6886d1bde5b2`, run `34191343078`, artifact `10042388530`, APK `7e5a769e68c88cf44749f7887c35e64d41369c3b76fe11a35a9473b350582636`. Its machine checks passed, but the Display & Sound destination was later rejected. Full historical receipts remain in the handoff; do not repoint the old pre-brightness prerelease.

Use only the existing secret-backed boop-dev signer; verify public certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Never replace/expose the key, use BOOP relay credentials, or claim device deployment without performing it with explicit permission.
