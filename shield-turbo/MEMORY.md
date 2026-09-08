# SHIELD TURBO durable decisions

Updated 2026-09-08. Read `SESSION_HANDOFF.md` for exact build receipts and `STATUS.md` for the current candidate. This file records durable rules and physical evidence.

## Physical evidence and accepted behavior

Ryan physically installed SHIELD TURBO on the bedroom NVIDIA Shield and confirmed brightness worked. Preserve the original working brightness checkpoint: source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK SHA-256 `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.

The first STANDARD control-centre candidate exposed two real Shield UX defects: the TURBO maintenance row was unreachable by D-pad and APPS OK opened a package-name dialog instead of launching directly. The corrected STANDARD candidate was source `d277ebe713cdbe5298f6205ef34fa4d493ea2114`, run `34189880390`, artifact `10041897001`, APK SHA-256 `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1`. Ryan's next physical report said the controls were now selectable and identified SHIELD -> DISPLAY + SOUND as the only remaining bug he had found in that pass. Preserve that corrected STANDARD build as the pre-ADVANCED fallback.

## Identity and ownership

SHIELD TURBO is an independently installed app, package `com.boop.shieldturbo`, in `shield-turbo/` on branch `shield-turbo-v01`. The repository is shared with BOOP for convenience. It is not another BOOP body or a replacement for unified `com.boop.alpha1`.

Ryan approved development/signing through GitHub using the existing BOOP development signer. Keep Shield Turbo work confined to its own app/workflow unless explicitly authorised otherwise.

## Product rules

Diagnostics remain honest snapshots on demand. No speed-up scores, blanket RAM cleaning, overclocking, arbitrary governor changes, process killing, other-app data clearing or pretend network tweaks. CPU frequency is not load; available memory is not a performance score; a generic thermal zone is not automatically a CPU/GPU sensor.

Brightness remains a deliberately simple reversible picture control: 10–100%, with 100% removing the overlay and leaving the picture untouched. Below 100% uses Android display-over-other-apps permission through the private non-exported `BrightnessService`.

## STANDARD remote behavior

All useful controls must be genuinely reachable with the Shield remote. A visible but unreachable button is a functional bug.

APPS is primarily a launcher: OK launches immediately using the Leanback launch intent first and Android's ordinary launch intent as fallback. Hold OK may open Android App Info. Do not interpose package-name dialogs on normal launch.

TURBO maintenance controls are safe routes only: storage/free-space settings, Android manage-apps settings and restarting SHIELD TURBO itself. They are not process killers or cleaners.

SHIELD settings shortcuts are firmware-dependent. If a generic nested intent does nothing on NVIDIA Shield, prefer a known usable broader Shield/Android settings entry over a dead shortcut. v0.2.0 therefore makes DISPLAY + SOUND begin at Android main Settings, then fall back to generic display and sound settings. Real Shield behavior is authoritative.

## ADVANCED / ADB TURBO direction

Ryan explicitly authorised moving into advanced work after the corrected STANDARD retest. ADB TURBO is based on a one-time ADB grant, not a permanent ADB connection.

Current ADB TURBO permission:

`android.permission.WRITE_SECURE_SETTINGS`

One-time setup command:

`adb shell pm grant com.boop.shieldturbo android.permission.WRITE_SECURE_SETTINGS`

The app must check that permission before enabling advanced actions. It must not invent authority from enabled debugging, usage access, a su binary or a root-management app. It must not store an ADB address, password or private key, and must not keep an ADB socket/session open merely to retain the capability.

The first approved advanced action is Android UI animation speed. It is deliberately reversible and measurable:
- FAST 0.5x writes 0.5 to all three Android animation scales.
- ANIMATIONS OFF writes 0.0.
- RESTORE 1x writes 1.0 and is the explicit rollback.

The only keys written are `window_animation_scale`, `transition_animation_scale`, and `animator_duration_scale`. Do not broaden this into CPU/GPU clocks, overclocking, process massacre, cache purges, blanket RAM cleaners or data clearing without separate evidence and approval.

`WRITE_SECURE_SETTINGS` is a protected permission normally unavailable to ordinary apps. Its manifest lint warning is intentionally suppressed only on that single declaration because the product explicitly relies on a manual ADB development grant. Do not create a global lint baseline or disable `ProtectedPermissions` project-wide.

## CI and visual acceptance

Ryan explicitly instructed that GitHub must not judge visuals. Physical NVIDIA Shield testing is the visual and remote-interaction authority.

The SHIELD TURBO workflow must not use UI hierarchy dumps, screenshots, image comparisons, focus-label visual assertions or other appearance tests. CI may and should continue unit tests, source safety contracts, Android lint, signed release build, package/signature/archive checks and a basic install/launch/no-fatal smoke test.

Do not infer visual correctness or Shield firmware navigation from emulator success.

## Current v0.2.0 receipt

Built source `81f448c417e3b0b122df55edc6ea6886d1bde5b2`, run `34191343078`, job `101949873574`, signed artifact `SHIELD-TURBO` ID `10042388530`, APK SHA-256 `7e5a769e68c88cf44749f7887c35e64d41369c3b76fe11a35a9473b350582636`, signer certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Fresh machine verification: 33 Kotlin tests passed, seven source contracts passed, lint succeeded, signed build succeeded, package/signature/archive checks succeeded, and basic install/launch/no-fatal smoke succeeded. There were no visual assertions.

This v0.2.0 candidate is not physically accepted yet. Ryan should verify DISPLAY + SOUND on the real Shield, confirm ADVANCED remains locked before the grant, apply the one-time ADB grant, test 0.5x/off/restore 1x, and recheck brightness/standard controls for regression.

Reuse the existing secret-backed `boop-dev` signer and verify public certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Never create a replacement key, expose secret values, use BOOP relay credentials or claim physical acceptance from CI.
