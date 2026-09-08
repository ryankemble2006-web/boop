# SHIELD TURBO durable decisions

Updated 2026-09-08. Read SESSION_HANDOFF.md for exact current/historical receipts and STATUS.md for verification. Current user instructions and fresh hardware evidence supersede older dated implementation policies.

## Identity and workflow

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`. Hosting in BOOP's repository does not make this a unified BOOP body. Confine changes to Turbo and its existing workflow unless explicitly authorised otherwise.

Ryan wants development/signing through connected GitHub tools in chat. Do not insist on Work mode when those tools can perform the task. Fetch live branch/main, preserve concurrent work, publish reviewed scoped changes, update these app docs and verify live remote HEAD. GitHub publication is not laptop sync or physical deployment.

## Physical findings

Bedroom brightness was confirmed on source `192879ba87082b9daf5275c89a706bfd5f1106d2`, run `34129557124`, artifact `10021629767`, APK `3ad1a87f2d007a972d66aa6a3f1ee687596e3903e7038db2b252f5eaf9075a6d`.

Corrected STANDARD source `d277ebe713cdbe5298f6205ef34fa4d493ea2114`, run `34189880390`, artifact `10041897001`, APK `f86ed5b9aac5926d98c09d9fa69b83a8d41e0cd8992ecd7b5bcdebccdbc60cf1`: maintenance controls became selectable. APPS OK is a direct launch, not a package-name dialog.

Developer Options was physically confirmed in v0.2.0. Display & Sound's general-Settings fallback was rejected; v0.2.1's AOSP activity bounced Home then did nothing. Accessibility reported no app installed; v0.3.0 discovery also did not produce an accepted destination. Both native settings routes remain parked. Do not guess more component names or call general Settings a fix.

Latest v0.4.0 feedback: selecting a Startup Manager app produced a package-name/explanation-only dialog. No background restriction, reboot result or manual-launch/Undo cycle was physically established. The v0.4.1 repair still requires Ryan's test.

## Android dialog and label contracts

Do not combine AlertDialog setMessage with setItems/setAdapter/single-choice/multi-choice content in the same builder. Android standard dialog content is message OR list. Use title/list for actions and keep explanatory text on the parent page or in a separate confirmation. The API-use regression is functional; it is not visual certification.

Use meaningful installed launcher/application labels ahead of a package-name fallback. Preserve distinctive launcher names for forks; use the actual package ID if all labels are unavailable. Never invent labels or confuse human-readable name selection with target package identity.

Cancellation must remain callable while a task is busy; only duplicate mutation actions are guarded. Cancellation cannot undo a shell command already sent. Back now invokes cancellation for busy Startup Manager tasks.

## Brightness and ordinary controls

Preserve the 10-100% brightness overlay and private non-exported BrightnessService. 100% removes the overlay; input is not intercepted. New power/startup work must not silently change it.

Normal APPS OK launches directly; hold may show App Info. A visible but unreachable action is a functional failure. Launcher intent filters need not declare CATEGORY_DEFAULT. Native settings shortcuts must never silently substitute unrelated pages.

## Local ADB agreement

ENABLE ADB TURBO may create an app-private per-install RSA authentication identity in noBackupFilesDir, connect only to loopback port 5555, request Android's trust prompt, verify shell UID and grant only this package's WRITE_SECURE_SETTINGS permission after user setup. That authentication identity is not the APK signer and must never be exported, published or backed up.

No off-device target field, LAN scan, listening server, persistent shell or automatic background job. A normal app cannot start a disabled ADB daemon or approve its own trust prompt. Guide the first Network Debugging switch/RSA approval; every shell operation establishes and verifies its transient connection. Existing settings authority is not proof of a currently available shell.

## Startup Manager contract

Ryan's use case is four Kodi forks that he reports wake after Shield boot. Operate only on explicitly selected safe user apps. The normal action remains BLOCK STARTUP / KEEP LAUNCHABLE: save the original state first, set RUN_IN_BACKGROUND and RUN_ANY_IN_BACKGROUND to ignore, and verify read-back. Leave the package enabled. These are background restrictions, not a guarantee against all boot triggers or a free-RAM/speed score.

HARD BLOCK / DISABLE APP is separate and explicitly confirmed because the package will not launch until restored. Never apply hard blocking as the default. Do not clear app data, logins, files or caches.

Before the first mutation, record original app-op modes and package enabled state. First original wins: later Turbo actions cannot replace the rollback point. Per-app Undo verifies restore before removing a ledger entry. Undo All attempts each managed app independently; failed/unverified restores retain records. Preserve the preference format across this patch.

No RECEIVE_BOOT_COMPLETED receiver or startup/background service. Android's app-op/enabled states are persistent system state; Turbo must not become another startup app merely to reapply them. Target discovery retains system/updated-system, NVIDIA, Android, Google-core and BOOP exclusions. No QUERY_ALL_PACKAGES, blanket force-stop, RAM cleaner, pm clear, uninstall, root/bootloader work, overclocking or arbitrary governor changes.

## Existing power tools

Restart is scoped to one selected safe user app and retains its data. Sleep/reboot require confirmation and can interrupt playback or CEC equipment. Animation changes save/read back original values and retain Undo. Diagnostics are on-demand snapshots, not continuous monitoring, benchmark scores or unsupported claims of throttling.

## Verification and delivery

Ryan owns real-device visual/remote acceptance. Do not run GitHub screenshots, UI hierarchy dumps, golden-image checks, layout/appearance judgment or source-string appearance certification. Keep focused API/data/logic/protocol/security checks, lint, compilation, permanent-signer/package/archive verification and basic nonvisual process-launch smoke. Upload and offer the verified signed APK before slower smoke work. Never equate an install/launch pass with a usable menu or effective Kodi boot restriction.

Current v0.4.1/code 6: built source `0961153e5dea94c38027cdf31530f500c5b29573`, run `34204102153`, job `101989443983`, artifact `10047107169`, APK SHA-256 `c7bc147a70378dfe62a14e542aeb5dcb1036fbe6818551e98ddcf6fe061793fa`, size 2283246 bytes. 58 JVM tests and the 13-check Python gate passed; lint 0 errors/22 warnings; permanent-signer/package/archive and post-upload nonvisual launch checks passed. No v0.4.1 physical acceptance yet.

The previous v0.4.0 source `1358925716cf2c834171b767dd94f08d0c49e013`, run `34201159209`, artifact `10045926945`, APK `cf12ccdfec929424ad89f6f5302c86f7b1331ef809ceef336fc0bda5d344657a` was machine-green but its per-app dialog failed physically. Historical receipts remain in the handoff/Git history; do not repoint old checkpoints.

Next acceptance: one Kodi fork, actual action menu, BLOCK STARTUP write/read-back, reboot and check self-start, normal manual launch, then Undo/Cancel checks. Repeat for the other forks only after the first succeeds. Keep HARD BLOCK out of that initial test.

Only existing secret-backed boop-dev signing is allowed. Public certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Never replace/expose the signer or use BOOP relay credentials. Documentation HEAD after the built source does not identify a different APK.
