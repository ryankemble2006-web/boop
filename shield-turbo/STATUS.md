# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical state

CLEAN START's core result is physically confirmed on Ryan's Shield: the selected Kodi forks are force-closed after boot. Ryan checked Shield Apps after the cleanup and confirmed they had been force-closed. The cleanup still causes a noticeable roughly three-second Home-screen freeze while it works.

v0.5.1 added a static CLEAN START overlay, but Ryan physically saw it only for a microsecond at the end of the freeze. Treat that as a timing failure of the notice, not a CLEAN START failure. The fixed card itself must have no movement whatsoever.

Earlier physical evidence retained: bedroom brightness works; corrected STANDARD maintenance items are selectable; Developer Options opens; v0.4.1 Startup Manager menu and normal manual Kodi launch work. The old v0.4 app-op startup restriction failed. Display & Sound and Accessibility remain parked.

## Current candidate

**v0.5.2 / code 9** is signed and machine-verified. Exact built source `6df33fa4e62688e09d1a5ee33c0ca7128afd6ed4`.

The only functional change from v0.5.1 is a 500 ms worker-thread preroll after the existing static overlay is shown and before LocalBridge/ADB force-stop work begins. It gives Android a render opportunity before the Shield becomes busy. It does not animate the notice, block the UI thread, change targets, change the 30/60/120-second bounded scheduler, or change force-stop/read-back behavior.

Expected physical sequence: static `SHIELD TURBO · CLEAN START` / `Tidying startup apps` appears first, remains completely motionless while cleanup runs, then disappears. Physical visibility/order/motionlessness are pending Ryan's real-Shield reboot.

## Exact v0.5.2 verification

Run `34218659116`, job `102036355692`, conclusion **success**. 68 JVM tests passed; 22 source/API/security contracts passed; lint **0 errors / 24 warnings**; permanent signer/package/version/archive checks passed; nonvisual cold/warm launch/no-fatal smoke passed.

Signed artifact `10052914776`, ZIP `759836` bytes, SHA-256 `0f59a1d4412b6b9671977d4fe18a874d02a1ed75c334b54d611ddf703f82c875`. Test artifact `10052958031`, ZIP `89730` bytes, SHA-256 `601f53247802e45c8099827c855c31587598832fbf6677065c2adff3e75a7fd8`.

Delivered APK `Shield-Turbo-v0.5.2.apk`, `2317430` bytes, SHA-256 `e2920b1d6c0a5ea36a5f93829729a7391ca7a038f0c9241d891d3bd75b71fd51`. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Downloaded artifact ZIP digest/CRC, exact built source, package/version, APK digest and signer receipts were independently matched before delivery.

**No GitHub visual confirmation ran.** Ryan owns real-device appearance, timing and motionlessness acceptance. See SESSION_HANDOFF.md for TDD receipts and the exact physical-test boundary.
