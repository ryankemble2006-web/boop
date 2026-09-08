# SHIELD TURBO durable decisions

Updated 2026-09-08. Read SESSION_HANDOFF.md for exact current/historical receipts and STATUS.md for verification status. Current user instructions and fresh hardware evidence supersede old dated implementation policies.

## Identity and workflow

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`. Sharing BOOP's repository does not make this a unified BOOP body. Keep changes confined to the Turbo app and its existing workflow unless explicitly authorised otherwise.

Ryan wants development/signing through connected GitHub tools in chat. Do not insist on a Work-mode handoff. Fetch live branch/main, preserve concurrent work, publish reviewed scoped changes, update these app docs and verify live remote HEAD. Do not claim laptop sync or physical deployment from GitHub publication.

## Physical findings to preserve

Bedroom brightness was confirmed on original source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.

Corrected STANDARD source `d277ebe713cdbe5298f6205ef34fa4d493ea2114`, run `34189880390`, artifact `10041897001`, APK `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1`: maintenance controls became selectable. APPS should launch directly on OK, not show package-name dialogs.

v0.2.0 Developer Options was physically confirmed. Its general Android Settings fallback was explicitly rejected as Display & Sound. v0.2.1 AOSP DisplaySoundActivity also failed: first click Home, later clicks nothing. Accessibility reported no app installed. These are unresolved firmware results, not reasons to claim CI proves them fixed. Neither successful Developer Options nor a settings permission proves actual ADB power control.

## Brightness and ordinary controls

Preserve the 10-100% brightness overlay and private non-exported BrightnessService. 100% removes the overlay. Input is not intercepted. New power work must not silently change brightness behavior.

Normal APPS OK launches directly; hold may show App Info. A visible unreachable control is a functional failure. System settings routes must not silently substitute an unrelated screen. Launcher filters need not declare CATEGORY_DEFAULT; do not reintroduce that restrictive discovery filter.

## Explicit new local ADB agreement

Ryan requested one-button ADB enable and meaningful advanced controls on 2026-09-08. This supersedes the older manual-only/no-stored-ADB-key implementation rule **only for an explicit on-device self-ADB client**.

ENABLE ADB TURBO may create an app-private per-install RSA authentication identity in noBackupFilesDir, connect only to loopback port 5555, request Android's standard trust prompt, verify shell UID, and grant only this package's WRITE_SECURE_SETTINGS permission after the user presses setup. This authentication key is not the APK signing key and must never be published/backed up/exported. No off-device target field, LAN scan, app-owned listening server, persistent shell, startup receiver or automatic background job.

A normal app cannot turn on a disabled ADB daemon or approve its own trust prompt. Guide the user through the first Network Debugging switch and RSA Allow confirmation. No laptop command is required by this flow, but do not promise literally one click from a completely unconfigured stock device or permanent approval across reinstalls/revocations.

Network Debugging itself is a device-managed network listener. State the trusted-LAN requirement and leave shutdown to Developer Options when no longer needed. Every shell operation must establish and verify its own transient connection. An actual secure-settings permission and a currently reachable authorised ADB shell are distinct capabilities.

The only new normal manifest permission is INTERNET for loopback socket access. ProtectedPermissions suppression remains narrowly on the intentional WRITE_SECURE_SETTINGS declaration; never hide security issues with a blanket lint baseline.

## Power actions and safety

User-confirmed sleep and reboot may interrupt playback/equipment through CEC. Report command receipts accurately; a reboot disconnect is not proof a reboot completed. Commands must be bounded/cancellable with checked exit markers. Cancellation cannot undo a command already sent.

Selected user-app restart is the explicit scoped exception to the older no-force-stop rule: choose and confirm ONE non-system app, revalidate installed status/package/launch component, then stop and relaunch it. Exclude Android/system apps, NVIDIA services, BOOP and Google core services. Never interpolate an unvalidated shell string. Do not clear app data, disable/uninstall packages or kill background apps in bulk.

Animation controls affect only window_animation_scale, transition_animation_scale and animator_duration_scale. Save original values before the first edit, verify readback and retain an undo path. NORMAL 1x and UNDO saved values are distinct. No CPU/GPU clocks, overclocking, arbitrary governors, fake RAM boosts or speed scores.

Diagnostics are explicit snapshots on-device, not continuous monitoring. CPU frequency is not load; a thermal service/zone is not automatically CPU/GPU temperature. Missing services/readings must stay honestly unavailable.

## Native settings discovery

Do not guess another DisplaySoundActivity class or call general Settings a native-page fix. Discover installed, enabled, exported system activities through scoped package/action queries. Use their actual declared action where available. Show candidates/subpages and local route diagnostics. Save a destination only after Ryan confirms it is the page he wanted, and validate it remains installed before reuse.

The v0.3.0 finder is implemented; **exact NVIDIA Display & Sound and Accessibility destinations are still awaiting physical evidence**. If unsupported, say so and obtain minimal private firmware evidence. Never publish raw device dumps, third-party firmware APKs or personal screenshots. Human confirmation, not class-name resemblance, owns native-route acceptance.

## Verification, signing and delivery

Ryan owns all visual/real-device acceptance. No GitHub screenshots, hierarchy dumps, golden-image checks, layout/appearance judging or source-string visual certification. Keep focused logic/protocol/routing tests, security contracts, lint, compilation, signer/package/archive verification and basic nonvisual process-launch smoke. Upload the signed APK before slower smoke work.

Deliver an actually extracted APK with its exact checksum/commit/version checked. Do not invent sandbox links, relabel a ZIP as APK or claim hardware success from machine checks.

Current candidate v0.3.0/code 4: source `ac5f79cd9138553a27df776e6b47e685f2cbf0ff`, successful run `34196792381`, artifact `10044276954`, APK `fff6b791235b05938dcb34a886c99a97d4563132cd46db79493dd422b0f25846`. 43 unit tests and nine source contracts passed, lint 0 errors/21 warnings, signer/package/archive and nonvisual launch passed. Physical acceptance pending.

Only existing secret-backed boop-dev signing is allowed; public certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. No replacement signing key, relay credentials or checkpoint repointing. Historical receipts remain in the handoff. Documentation HEAD after the built source does not identify a different APK.
