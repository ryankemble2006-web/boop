# SHIELD TURBO local ADB power tools implementation plan

Goal: replace manual grant instructions with one in-app connection action and deliver actual user-triggered power tools while preserving brightness.
Base: shield-turbo-v01@4c881ea401fa4b481229c039c2239050bf910148. No Windows checkout is mounted in this task; repository reads/writes/build/signing use the connected GitHub app. Local Java protocol testing is separate from Android CI.

## Requested scope

Ryan reports v0.2.1 Accessibility says no app installed and Display & Sound first bounces Home, then does nothing. The hard-coded AOSP activity is rejected by physical evidence. He requests one-button ADB setup, real advanced functionality, web research, no GitHub visual confirmation, and a signed APK.

1. Write bounded ADB wire/crypto/exit-status tests, observe RED, implement a synchronous loopback-only Java transport, and verify GREEN locally and in CI. The initial local frame assertion failed on a stub before transport implementation. The expanded local fake-daemon authentication/command test passed before publication.
2. Add private on-device key storage and one-button setup. Network Debugging and Android's RSA approval cannot be bypassed. Authenticate shell UID before commands; grant this package only after an explicit setup click. No persistent background connection.
3. Add a non-exported PowerActivity for diagnostics, confirmed sleep/reboot, selected non-system app restart, verified animation presets and exact undo. Preserve MainActivity's brightness/standard behavior while handing its Advanced entry to the new activity.
4. Replace guessed settings starts with actual on-device package/intent discovery and explicit route diagnostics. Never call general Settings the correct Display & Sound destination. Save a user-selected route only after confirmation. Record unsupported firmware outcomes.
5. Run focused logic, source/security contracts, lint, signed build and integrity checks. No screenshots, UI hierarchy or appearance tests. Upload signed artifact before optional nonvisual smoke. Download, checksum, extract APK, update branch handoff/status/memory and verify live HEAD.

No mass killing, overclocking, package disabling/uninstalling, data clearing, Internet endpoints, downloaded runtime binaries or new APK signing key. Existing repository contracts prohibiting stored ADB keys/manual-only setup are superseded only for this user-requested self-ADB feature: a fresh per-install authentication identity stays in app no-backup private storage and connects only to loopback. It is not the APK signer. Selected user-app restart is an explicit confirmed exception to the old no-force-stop policy; system apps, NVIDIA and BOOP are excluded.

## Research and limits

Android official ADB guide: https://developer.android.com/tools/adb . User activation of debugging and device RSA approval are required. Do not promise one-click activation from a stock device with debugging off.

Wire/authentication reference inspected: https://github.com/tananaev/adblib at 4b3d25641933b92b729ee67d17791992158d20be, especially AdbConnection.java and AdbCrypto.java. This implementation is a new bounded synchronous client with no library dependency; it follows the legacy ADB checksum protocol and RSA public-key structure. Original AdbLib: https://github.com/cgutman/AdbLib . Additional protocol reference: https://tangoadb.dev/internal/connection/ .

NVIDIA documents Display & Sound as its own settings surface. AOSP component existence does not prove stock NVIDIA behavior. This release provides installed-firmware discovery and human-confirmed routing, not a claim that the exact NVIDIA page has already been fixed/tested. A missing or non-exported component must remain explicitly unavailable.

Network Debugging enables a device-managed listener beyond this app's control. The app itself contacts only loopback; users should use a trusted LAN and disable Network Debugging when finished. Every command has a bounded response and closes its connection. A missing result or reboot disconnect must not become invented success.
