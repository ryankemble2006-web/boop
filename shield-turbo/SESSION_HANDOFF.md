# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current result

The latest delivered APK remains **v0.4.1 / versionCode 6**. The Startup Manager action menu and normal manual launch have physical confirmation. **The requested startup suppression has failed the user's acceptance test.** Do not substitute machine-green or saved app-op values for the missing outcome.

Ryan clarified the previous question: the four Kodi forks remain visible in the **Shield task manager**, and swiping/force-closing them makes the Shield noticeably faster. This is not merely his observation that App Info has an enabled Force Stop button. Do not ask that question again or dismiss the reported performance difference. He wants only essential Android/Shield functionality starting automatically, with optional apps available when deliberately opened.

Task-list entries alone are still not a measured process/RAM inventory. Use the supplied feedback as the failed acceptance result, and obtain process/service evidence in the app rather than making Ryan repeatedly demonstrate the same problem. Actual installed fork package IDs and a post-reboot shell/process snapshot have not been supplied. Never invent either.

## Mechanism mismatch and next proposal

The installed BLOCK STARTUP implementation sets only RUN_IN_BACKGROUND and RUN_ANY_IN_BACKGROUND to ignore. It does not force-stop the selected package or prove absence of processes after boot. Repeating or renaming that same write cannot establish the requested behavior.

A proposed **CLEAN START** repair has two separately verifiable stages:

1. User-triggered stop and verification for the explicitly chosen app/group. Use real current-user ActivityManager force-stop via the existing authorised loopback ADB connection. Check package identity, stopped/enabled state and process/service evidence. Keep the installed package and its data; do not disable/uninstall it. Report unknown or failed verification honestly. Protect a manually launched foreground app and active playback from automatic cleanup.
2. Optional, explicitly enabled one-shot post-boot enforcement of the saved group, with a protected-essential/KEEP RUNNING list. A bounded boot task would need to wait for previously authorised local ADB readiness, stop only reviewed eligible packages, record results and finish. No continuous killer, recurring RAM sweep, indefinite retries, or automatic new RSA prompt at boot. ADB unavailable means visibly not applied, never a success badge. New installed apps require review before joining a destructive action group.

**This is a proposal, not an implemented or physically proven feature.** No new boot receiver, service, permission, shell command, workflow or APK was added in this feedback/research update. Current v0.4.1 still has no automatic boot task. Introducing one would be an explicit design change from the previous no-boot-task implementation and must be visible in the app and release notes.

Post-boot cleanup is not a guarantee that an app never starts momentarily during boot. Force-stop is stronger than a background restriction but is not a permanent manual-only security policy: deliberate launch releases stopped state, and some explicit activation paths can bypass ordinary stopped-package broadcast filtering. Do not promise universal never-start behavior on stock Shield while preserving every ordinary launcher path. Do not silently escalate to root, device-owner provisioning, modified third-party APKs or HARD BLOCK.

Essentials must be protected by verified roles/dependencies, not by a package-name prefix alone. The next design must account for Android/system services, the active launcher, remote/input/accessibility, networking/VPN where applicable, media/DRM and necessary NVIDIA services. Optional vendor/system packages require individual evidence and review; being preinstalled does not by itself prove essential, but being non-Android does not prove safe to stop. Existing system exclusions stay until reviewed replacement logic exists.

## Primary references checked for this clarification

- Android ADB ActivityManager command documentation: `am force-stop` stops everything associated with one package. https://developer.android.com/tools/adb#am
- Stopped-package launch/broadcast controls, including explicit include-stopped exceptions and first launch: https://developer.android.com/about/versions/android-3.1#launchcontrols
- Background optimization: manufacturer-dependent restrictions, jobs/services and version-dependent boot behavior. https://developer.android.com/topic/performance/background-optimization
- Android Recents is a task/activity list, not a process inventory: https://developer.android.com/guide/components/activities/recents
- Stock PackageManager shell restrictions prevent promising arbitrary per-component boot-receiver disable for ordinary non-test apps: https://android.googlesource.com/platform/frameworks/base/+/fae78ad75ae4e72133d44ae821399ef9a8236321/services/core/java/com/android/server/pm/PackageManagerService.java

These primary sources establish platform mechanisms, not the installed forks' exact startup trigger or measured Shield behavior. Earlier upstream Kodi receiver research remains historical candidate evidence, not proof about the user's installed APKs.

## Exact latest APK receipt (unchanged)

- Built source: `0961153e5dea94c38027cdf31530f500c5b29573`.
- Workflow: Build SHIELD TURBO, run `34204102153`, job `101989443983`, success.
- Historical machine verification: 58 JVM tests passed; 13 Python API/safety checks passed; lint 0 errors / 22 warnings; established signer, package/version/Leanback/non-debuggable/archive verification and nonvisual install/cold/warm launch/no-fatal smoke passed.
- Signed artifact: `SHIELD-TURBO`, ID `10047107169`, ZIP 744535 bytes.
- ZIP SHA-256: `4a1f31abe78e7876414c3524d98701d96ec9fbf3a2c46eb27280ab97eb8c6ee5`.
- Test artifact: `10047154897`, ZIP 88273 bytes, SHA-256 `f98ef2b3092f56cf3baabb24576d984d95fe7bcb0055a08da3f3e482bff4e752`.
- Delivered APK: `Shield-Turbo-v0.4.1.apk`, 2283246 bytes.
- APK SHA-256: `c7bc147a70378dfe62a14e542aeb5dcb1036fbe6818551e98ddcf6fe061793fa`.
- Permanent signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- No screenshots, hierarchy dumps, image/golden checks or visual acceptance ran. Ryan owns physical visuals and remote acceptance.

## Preserve working behavior and historical evidence

Bedroom brightness, corrected STANDARD maintenance selectability and Developer Options have prior physical confirmation. The 10-100% brightness overlay, non-exported BrightnessService and 100% removal behavior must remain unchanged. Display & Sound and Accessibility remain unresolved and parked.

The v0.4.1 fix removed conflicting AlertDialog message/list content, improved installed app-label fallback, and made Cancel/Back work while busy. Menu and manual launch are now confirmed; full startup, Undo and cancellation outcomes are not blanket-accepted.

Maintain the original-state ledger and preference format: first original wins, restore/read back before removing entries, retain failed entries, never describe force-stop as restoring killed playback or unsaved work. HARD BLOCK remains separately confirmed package disable and prevents normal launch until restored. No data/cache/logins clearing, uninstall, broad system killing, rooting, overclocking or arbitrary governor changes.

Complete v0.4.1 diagnosis/TDD and earlier source/run/artifact/physical receipts are retained at `d64d51db1fd6b7201429ce484be3e93973ff622c:shield-turbo/SESSION_HANDOFF.md`. The v0.4.0 detailed startup-policy/ledger TDD record remains at `776560b2a7060e612d6ddfa2cd22d29ab2cdee89:shield-turbo/SESSION_HANDOFF.md`. Do not repoint any checkpoint.

This update reconciles the latest physical result in handoff, STATUS.md and MEMORY.md. It changes documentation only, with CI skipped. No new app signing, installation, permission grant or Windows synchronization is claimed. The Windows checkout was not mounted in this chat; connected GitHub reads were used, and concurrent main-branch work was left untouched.
