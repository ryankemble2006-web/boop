# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical state

CLEAN START's force-stop/read-back core is physically accepted from earlier Shield tests. Stale Recents/task-manager cards can remain after force-stop, while the apps themselves are not loaded and reload only when focused. Normal deliberate launch still works.

Latest v0.5.4 physical result: **no static startup sign**, but the v0.5.3 slowdown was removed. Ryan had Shield navigation control again within roughly **one second**. Treat v0.5.4 as physically positive for fast fail-open/navigation responsiveness and negative for notice visibility. The latest v0.5.4 report did not separately re-check target stopped state, so do not invent v0.5.4-specific cleanup acceptance.

Presentation history: v0.5.1 flashed only at the end; v0.5.2 showed nothing; v0.5.3 showed nothing and stretched Turbo to almost eight seconds; v0.5.4 still showed nothing but restored fast control. No arbitrary timing tweak should be attempted again without diagnostic evidence.

Earlier physical evidence retained: bedroom brightness works; corrected STANDARD maintenance items are selectable; Developer Options opens; v0.4.1 Startup Manager menu and normal manual Kodi launch work. The old v0.4 app-op startup restriction failed. Display & Sound and Accessibility remain parked.

## Current candidate

**v0.5.5 / code 12** is signed and machine-verified. Exact built source `e22beecbaa9d95aeab036ae403684a32a7a33979`.

v0.5.5 keeps v0.5.4's Android 11+ display-bound overlay window context and **500 ms max fail-open**. It does not add another delay or rendering trick. Instead it persists the boot notice lifecycle result locally and displays it in CLEAN START as:

`STARTUP NOTICE DIAGNOSTIC: permission=... • window=... • add=... • present=... • ...ms [detail]`

The diagnostic records overlay permission, window-context mode, addView result, draw/frame-commit/timeout state, elapsed time and a short failure detail. It is saved before ADB cleanup begins. CLEAN START targets, force-stop/read-back semantics, trusted ADB, current-app skip and 30/60/120-second max-three scheduler remain unchanged.

The next real-Shield reboot must determine the cause. Machine checks must not be described as visual acceptance.

## Exact v0.5.5 verification

Run `34226811605`, job `102062828791`, conclusion **success**. 68 JVM tests passed with zero failures/errors/skips; source/API/security contracts passed; lint **0 errors / 24 warnings**; permanent signer/package/version/archive checks passed; nonvisual cold/warm launch/no-fatal smoke passed.

Signed artifact `10056167964`, ZIP `764873` bytes, SHA-256 `85cb44b357b5c7979e3e719ac4b55088a6e8c860774c51047c40dcb0d9391340`. Test artifact `10056218423`, ZIP `96072` bytes, SHA-256 `f6fd08f62ccacd0f22baee16cae71357e29319f4b6d7603808e019a834537e25`.

Delivered APK `Shield-Turbo-v0.5.5.apk`, `2328962` bytes, SHA-256 `40323820eda72df3592fc756a2b30ee15816cc8633a3e577ade65b5475d7b77f`. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Downloaded artifact ZIP digest matched GitHub; APK digest and built-source receipt matched; the APK v2 signing block was independently parsed and matched the permanent BOOP certificate. **No GitHub visual confirmation ran.** Ryan owns real-device appearance/timing/motionlessness acceptance.

## Next test

Install v0.5.5, reboot, open CLEAN START and report the exact diagnostic line plus sign visibility, navigation responsiveness and whether selected Kodi forks are stopped.
