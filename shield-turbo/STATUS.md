# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; independent package `com.boop.shieldturbo`.

## Current signed candidate

**v0.3.0 / versionCode 4 is built, signed and machine-verified. Physical Shield acceptance remains pending.**

| Evidence | Receipt |
| --- | --- |
| Built source | `ac5f79cd9138553a27df776e6b47e685f2cbf0ff` |
| Actions run / job | `34196792381` / `101966198551`: success |
| JVM tests | 43 passed, 0 failures/errors/skips |
| Source/security contracts | 9 passed |
| Android lint | 0 errors, 21 warnings; gate passed |
| Signed artifact | `SHIELD-TURBO`, `10044276954` |
| Artifact ZIP SHA-256 | `a279948eb1cf626955814d215c99642ba8cb655d5c7a230834d1b04aa8c5e6ce` |
| Delivered APK | `Shield-Turbo-v0.3.0.apk`, 2245278 bytes |
| APK SHA-256 | `fff6b791235b05938dcb34a886c99a97d4563132cd46db79493dd422b0f25846` |
| Permanent signer certificate SHA-256 | `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` |
| Test artifact | `10044314369` |
| Package/signature/archive checks | Passed; non-debuggable release, expected package/version/entry |
| Nonvisual emulator smoke | Install/launch/relaunch/no-fatal passed, after signed artifact upload |
| Visual tests | None; Ryan owns visual acceptance |

## New functionality in this candidate

One in-app ENABLE ADB TURBO action replaces typed laptop grant commands. Initial Network Debugging activation and Android RSA approval still require the user. Only loopback is contacted; authentication key remains in app no-backup private storage; each operation closes its socket.

ADVANCED includes shell diagnostics, selected non-system app restart, sleep, reboot, verified animation 0.5x/off/normal and exact saved-value undo. Interrupting actions require confirmation. No root/bootloader changes, bulk cleaner, package disabling, data clearing or CPU/GPU clock changes.

Brightness implementation is unchanged. Developer Options keeps the confirmed action. App discovery no longer wrongly requires CATEGORY_DEFAULT on launcher entries.

## Two firmware shortcuts are NOT yet accepted

Ryan rejected v0.2.1 Display & Sound (Home bounce, then no action) and reported Accessibility saying no app installed. The automatic hardcoded routes were replaced with actual installed-system-activity discovery and a user-confirmed saved destination. Correct native NVIDIA pages remain to be tested. General Settings is an explicitly separate manual option, not a claimed fix.

## Physical evidence

Accepted findings so far: original bedroom brightness works; corrected STANDARD maintenance controls selectable; v0.2.0 Developer Options opens correctly. No v0.3.0 ADB connection, permission grant, power action, exact undo or native-page discovery is physically confirmed yet. Refer to SESSION_HANDOFF.md for exact historical receipts and safe retest flow.

Only the existing secret-backed boop-dev signer was used. No app installation or device permission change was performed remotely by this chat. Repository publication is not laptop/device synchronisation.
