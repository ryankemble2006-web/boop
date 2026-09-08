# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical state

CLEAN START's force-stop/read-back core remains physically accepted from earlier Shield tests. Stale Recents/task-manager cards can remain after force-stop while the apps themselves are unloaded and reload only when focused. Normal deliberate launch still works.

Latest real-device notice evidence came from v0.5.5: the sign remained invisible, while CLEAN START reported `permission=yes`, `window=DISPLAY_WINDOW_CONTEXT`, `add=ADDED`, `present=DRAWN`, about `54ms`. This proved the old draw callback was a false-positive presentation signal, not proof of compositor delivery.

Presentation history: v0.5.1 flashed only at the end; v0.5.2 showed nothing; v0.5.3 showed nothing and stretched Turbo to almost eight seconds; v0.5.4 showed nothing but restored fast navigation within roughly one second; v0.5.5 showed nothing and exposed the `DRAWN` false-positive.

## Current candidate

**v0.5.6 / code 13** is signed and machine-verified. Exact built source `5870742c83b193251b323a48e12b0ef6c5b8b5ad`.

v0.5.6 requests a hardware-accelerated overlay and, on Android 10+, waits until the view is attached before registering `registerFrameCommitCallback`. `DRAWN` no longer counts as presentation on Android 10+; only `FRAME_COMMITTED` does. Pre-Android-10 keeps the OnDraw fallback. The 500 ms fail-open remains unchanged, so a failed notice cannot recreate v0.5.3's long slowdown.

A concurrent evidence improvement is also preserved: failed trusted boot ADB now records the actual exception class/message and CLEAN START can show `LAST CLEAN START DETAIL:`. Cleanup targets, force-stop/read-back semantics, trusted ADB, current-app skip and 30/60/120-second max-three scheduler are unchanged.

## Exact v0.5.6 verification

Run `34230235524`, job `102074235862`, conclusion **success**. **68 JVM tests passed**, source/API/security contracts passed, lint **0 errors / 24 warnings**, permanent signer/package/version/archive checks passed, and nonvisual cold/warm launch/no-fatal smoke passed.

Signed artifact `10057548249`, ZIP `765636` bytes, SHA-256 `1c601a47fd94d002f8a4e5d5722444dc81f256057fe444cd86e16223d560a4c3`. Test artifact `10057598695`, ZIP `106686` bytes, SHA-256 `ed2d1f902e577a538c5fe8d041c79fc4c30f75930eb7780fcc0c2902a03e9c4a`.

Delivered APK `Shield-Turbo-v0.5.6.apk`, `2330594` bytes, SHA-256 `e462db094cf3f09fa4815949b492ed85f0fa12a57a53635951ce875c8c48cd78`. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Downloaded artifact digests, APK digest, built-source receipt and package/version receipt all matched. The APK v2 signing block was independently parsed and matched `CN=BOOP Development,O=BOOP` and the permanent certificate. **No GitHub visual confirmation ran.** Ryan owns real-device appearance/timing/motionlessness acceptance.

## Next test

Install v0.5.6, reboot, then report sign visibility, navigation responsiveness, exact `STARTUP NOTICE DIAGNOSTIC:` line, and whether the selected Kodi forks are stopped. Android 10+ success should say `present=FRAME_COMMITTED`; do not add arbitrary timing delays if it does not.
