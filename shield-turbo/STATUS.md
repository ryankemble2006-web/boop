# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical state

Ryan physically reported **v0.5.0 CLEAN START appears to have beaten the unwanted Kodi-fork startup problem**. The remaining observed issue is UX: CLEAN START causes a noticeable roughly three-second Home-screen freeze while it works.

Earlier physical evidence retained: bedroom brightness works; corrected STANDARD maintenance items are selectable; Developer Options opens; v0.4.1 Startup Manager menu and normal manual Kodi launch work. The old v0.4 app-op startup restriction failed. Display & Sound and Accessibility remain parked.

## Current candidate

**v0.5.1 / code 8** is signed and machine-verified. Exact built source `99c90a63a17f4a3a72b561e2c7ed3792deb41649`.

It adds a static top-centre CLEAN START notice only while the automatic cleanup job runs: `SHIELD TURBO · CLEAN START` / `Tidying startup apps`. The overlay is non-focusable and non-touchable, has no animation/update loop, and is hidden on completion/stop/destroy. If overlay permission is unavailable, CLEAN START continues without the notice.

No movement is allowed. v0.5.1 visual/motion acceptance is pending Ryan's real-Shield reboot. Scheduler timing, force-stop verification, targets, ADB behavior and brightness are unchanged from v0.5.0.

## Exact verification

Run `34215598324`, job `102026489736`, **success**. 68 JVM tests passed; 21 source/API/security contracts passed; lint **0 errors / 24 warnings**; permanent signer/package/version/archive checks passed; nonvisual cold/warm launch/no-fatal smoke passed.

Signed artifact `10051719431`, ZIP `759660` bytes, SHA-256 `2d3b9a3bd5bd04d338c5c553b3ddea9f7734af651e53477dedac14952fa688a1`. Test artifact `10051766008`, ZIP `92395` bytes, SHA-256 `8ea98b5550ab000878fb0cfbe7fce005461925b46ab33f076fda3dd643ca39b5`.

Delivered APK `Shield-Turbo-v0.5.1.apk`, `2316998` bytes, SHA-256 `0b92436b50ac8cb94d3d17855a13d203c4c48bda3a7727c55f5c1167dc74de3c`. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Downloaded ZIP digest/CRC, exact built source, package/version, APK digest and signer receipts were independently matched before delivery.

**No GitHub visual confirmation ran.** Ryan owns real-device appearance and motionlessness acceptance. See SESSION_HANDOFF.md for exact behavior, TDD receipts and physical-test boundary.
