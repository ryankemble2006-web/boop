# SHIELD TURBO durable decisions

Updated 2026-09-08. Read SESSION_HANDOFF.md for exact current/historical receipts and STATUS.md for verification status. Current user instructions and fresh hardware evidence supersede old dated implementation policies.

## Identity and workflow

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`. Sharing BOOP's repository does not make this a unified BOOP body. Keep changes confined to the Turbo app and its existing workflow unless explicitly authorised otherwise.

Ryan wants development/signing through connected GitHub tools in chat. Do not insist on Work mode when connected GitHub tools can perform the task. Fetch live branch/main, preserve concurrent work, publish reviewed scoped changes, update these app docs and verify live remote HEAD. Do not claim laptop sync or physical deployment from GitHub publication.

## Physical findings to preserve

Bedroom brightness was confirmed on original source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.

Corrected STANDARD source `d277ebe713cdbe5298f6205ef34fa4d493ea2114`, run `34189880390`, artifact `10041897001`, APK `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1`: maintenance controls became selectable. APPS OK launches directly, not a package-name dialog.

v0.2.0 Developer Options was physically confirmed. Its general Android Settings fallback was rejected as Display & Sound. v0.2.1 AOSP DisplaySoundActivity also failed: first click Home, later clicks nothing. Accessibility reported no app installed. Those firmware routes remain unresolved; do not let them block useful advanced work and do not claim CI fixes them.

## Brightness and ordinary controls

Preserve the 10-100% brightness overlay and private non-exported BrightnessService. 100% removes the overlay. Input is not intercepted. New power/startup work must not silently change brightness behavior.

Normal APPS OK launches directly; hold may show App Info. A visible unreachable control is a functional failure. System settings routes must not silently substitute an unrelated screen. Launcher filters need not declare CATEGORY_DEFAULT.

## Local ADB agreement

ENABLE ADB TURBO may create an app-private per-install RSA authentication identity in noBackupFilesDir, connect only to loopback port 5555, request Android's standard trust prompt, verify shell UID, and grant only this package's WRITE_SECURE_SETTINGS permission after the user presses setup. The authentication key is not the APK signing key and must never be published/backed up/exported. No off-device target field, LAN scan, app-owned listening server, persistent shell or automatic background job.

A normal app cannot turn on a disabled ADB daemon or approve its own trust prompt. Guide the user through the first Network Debugging switch and RSA Allow confirmation. Every shell operation establishes and verifies its own transient connection.

## Startup Manager durable contract

Ryan's concrete use case is four Kodi forks that wake after Shield boot and later need force-closing. Startup Manager is approved to suppress selected user apps without becoming another startup app itself.

The normal/default action is **BLOCK STARTUP / KEEP LAUNCHABLE**. For exactly one selected safe user app, Turbo records the original state before first mutation, then uses Android background app-ops:
- `RUN_IN_BACKGROUND -> ignore`
- `RUN_ANY_IN_BACKGROUND -> ignore`

The package remains enabled, so manual launch from its ordinary icon should remain available. This reduces unwanted background/startup execution; never describe it as a guaranteed free-RAM or speed score.

**HARD BLOCK / DISABLE APP** is a separate stronger action using current-user package disable. It requires explicit confirmation because the app will not launch until restored. It must never be the automatic/default Startup Manager action.

### Exact undo ledger

Before first Turbo mutation, save the package's original `RUN_IN_BACKGROUND`, `RUN_ANY_IN_BACKGROUND` and package enabled state. First original wins: later Turbo changes must not overwrite the rollback point.

Per-app UNDO restores the saved values and verifies read-back before removing the ledger entry. UNDO ALL independently restores every managed app; entries whose restore is not verified remain in the ledger. Never guess that the prior state was simply `allow` or `enabled` when exact state was captured.

The Startup Manager adds no `RECEIVE_BOOT_COMPLETED` receiver and no startup/background service. Android app-op and enabled-state changes persist themselves, so Turbo must not wake at boot merely to reapply them.

Default target discovery excludes Turbo itself, BOOP, system/updated-system apps, NVIDIA/Android system packages and Google core services. No QUERY_ALL_PACKAGES. No bulk action that force-stops every background app.

Never add `pm clear`, package uninstall, cache/data/logins clearing, root, bootloader, overclocking, arbitrary governors or blanket RAM cleaning under the Startup Manager label. App-specific changes stay user-selected and reversible.

## Power actions and diagnostics retained

Selected user-app restart remains scoped to ONE user-selected safe app and retains app data. Sleep and reboot require confirmation and may interrupt playback/CEC equipment. Animation changes save original values, read back writes and retain exact undo. Diagnostics are snapshots, not continuous monitoring or performance scores.

## Native settings discovery

Display & Sound and Accessibility remain physically unresolved. Do not guess more NVIDIA/AOSP class names or call general Settings a fix. Discovery/confirmation may remain available, but these rabbit holes are lower priority than user-approved ADB/Startup functionality until new firmware evidence is supplied.

## Verification, signing and delivery

Ryan owns all visual/real-device acceptance. No GitHub screenshots, hierarchy dumps, golden-image checks, layout/appearance judging or source-string visual certification. Keep focused logic/protocol/security tests, lint, compilation, permanent-signer/package/archive verification and basic nonvisual process-launch smoke. Upload the signed APK before slower smoke work.

Current candidate v0.4.0/code 5: built source `1358925716cf2c834171b767dd94f08d0c49e013`, run `34201159209`, job `101980019981`, signed artifact `10045926945`, APK SHA-256 `cf12ccdfec929424ad89f6f5302c86f7b1331ef809ceef336fc0bda5d344657a`, size `2281902` bytes. 51 JVM tests and 10 source/security contracts passed; lint passed with 0 errors and 22 warnings; permanent signer/package/archive checks and nonvisual install/launch passed. Startup Manager is not physically accepted until Ryan tests it on a Shield.

First physical Startup Manager acceptance: choose ONE Kodi fork, BLOCK STARTUP, reboot Shield, confirm it does not self-start, then manually launch the same fork normally. Only then repeat for the remaining forks. HARD BLOCK is not needed for that first test.

Only existing secret-backed boop-dev signing is allowed; public certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. No replacement signing key, relay credentials or checkpoint repointing. Documentation HEAD after built source does not identify a different APK.