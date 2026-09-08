# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical state

CLEAN START's force-stop/read-back core remains physically accepted from earlier Shield tests. Stale Recents/task-manager cards can remain while target apps themselves are stopped and reload only when focused. Normal deliberate launch still works.

Latest v0.5.6 notice evidence: navigation remained quick; Home refreshed for a microsecond; no sign was visible; diagnostic reported `FRAME_COMMITTED` in about 103ms. This rejected the small `WRAP_CONTENT` boot-card architecture, not the cleanup core.

## Current candidate

**v0.5.7 / code 14** is signed and machine-verified. Exact built source `5f3b18fca5921e2a47f132c1a149d03d59d7f091`. Physical notice acceptance is pending.

v0.5.7 changes presentation geometry only: a transparent `MATCH_PARENT x MATCH_PARENT` `FrameLayout` host using brightness-style `FLAG_LAYOUT_NO_LIMITS`, with the existing static CLEAN START card top-centre inside it. Non-touch/non-focus, attach-gated frame commit, 500ms fail-open, cleanup engine, targets, scheduler and trusted ADB are unchanged. No animation or artificial delay was added.

## Exact v0.5.7 verification

Release run `34236299335`, job `102094767133`, conclusion **success**. **68 JVM tests passed**, source/API/security contracts passed, lint **0 errors / 24 warnings**, permanent signer/package/version/archive checks passed, and nonvisual cold/warm launch/no-fatal smoke passed.

Signed artifact `10060096819`, ZIP `765768` bytes, SHA-256 `6c2098ff9ac4135ad105020567c098dd66deb4223c73da2ee6db51135fa6779e`. Test artifact `10060152208`, ZIP `100919` bytes, SHA-256 `902228e7d291dd16efdba06a6936efe8db674e67bc92567a98690361ee7437f2`.

Delivered APK `Shield-Turbo-v0.5.7.apk`, `2330782` bytes, SHA-256 `289db308bd387d5cf2249e44dd99a92a00cfae5e73b0e3ee54dca07e154321c0`. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Downloaded artifact digests, APK digest, source receipt and package/version receipt matched. APK v2 signer independently matched `CN=BOOP Development,O=BOOP`. **No visual tests ran.** Ryan owns real-device appearance/timing/motionlessness acceptance.

TDD: RED `87a7fd89...` / run `34234888441` / job `102089929246`; production `a3b35897...`; formatting-agnostic guard + full GREEN `607ac8f1...` / run `34235514995` / job `102092069734`; final atomic release source `5f3b18fc...`.

## Next physical test

Install v0.5.7, reboot, report sign visibility, navigation speed, exact startup diagnostic, and target stopped state if convenient. If the sign is visibly correct and static, freeze notice presentation.
