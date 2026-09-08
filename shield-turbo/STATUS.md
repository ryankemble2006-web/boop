# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical state

CLEAN START's core result is physically confirmed on Ryan's Shield: selected Kodi forks are force-closed after boot. On v0.5.2 Ryan saw **no startup notice at all**, but confirmed the apps were stopped. Task-manager cards from the previous session remained visible, yet the apps were not loaded and reloaded only when focused. Treat that as stopped-state/normal-relaunch behavior, not as a cleanup failure.

v0.5.1 showed the static notice only for a microsecond at the end; v0.5.2's fixed 500 ms preroll still produced no visible notice. The cleanup itself remains accepted. The remaining problem is presentation before the roughly three-second Home freeze.

Earlier physical evidence retained: bedroom brightness works; corrected STANDARD maintenance items are selectable; Developer Options opens; v0.4.1 Startup Manager menu and normal manual Kodi launch work. The old v0.4 app-op startup restriction failed. Display & Sound and Accessibility remain parked.

## Current candidate

**v0.5.3 / code 10** is signed and machine-verified. Exact built source `a65c800c459d292a37a092f430d08ddfd2be3742`.

v0.5.3 replaces the fixed preroll sleep with a bounded first-frame presentation handshake. The worker waits for `registerFrameCommitCallback` on API 29+ hardware-accelerated rendering, with an OnDraw fallback, before creating `LocalBridge`; if presentation cannot be established it falls through after at most 3 seconds. No animation or movement was added. CLEAN START targets, scheduler, force-stop/read-back behavior and trusted ADB remain unchanged.

Expected physical sequence: static `SHIELD TURBO · CLEAN START` / `Tidying startup apps` is presented first, remains completely motionless while cleanup runs, then disappears. Physical visibility/order/motionlessness are pending Ryan's real-Shield v0.5.3 reboot.

## Exact v0.5.3 verification

Run `34221284613`, job `102044740488`, conclusion **success**. 68 JVM tests passed; 22 source/API/security contracts passed; lint **0 errors / 24 warnings**; permanent signer/package/version/archive checks passed; nonvisual cold/warm launch/no-fatal smoke passed.

Signed artifact `10053936105`, ZIP `760538` bytes, SHA-256 `083fe26f122d10f0bc27ef4569ba9731799b3b0c4e24bcc6c15be874f0f81b1f`. Test artifact `10053981252`, ZIP `91356` bytes, SHA-256 `e6277ca06695eae76ddc16c23bf6eb897b89e050b00742ede23fddf2426fc869`.

Delivered APK `Shield-Turbo-v0.5.3.apk`, `2319002` bytes, SHA-256 `fa50dafa7f01f6c6d2e8c9e840449f0a4fd2d57674ec172fe16e02040da133b1`. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Downloaded artifact digests/CRC, APK digest, built source and certificate were independently checked after CI. **No GitHub visual confirmation ran.** Ryan owns real-device appearance, timing and motionlessness acceptance. See SESSION_HANDOFF.md for the TDD and evidence boundary.
