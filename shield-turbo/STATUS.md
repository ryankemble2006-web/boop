# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical state

CLEAN START's force-stop/read-back core remains physically accepted from earlier Shield tests. Stale Recents/task-manager cards can remain after force-stop while the apps themselves are unloaded and reload only when focused. Normal deliberate launch still works.

Latest v0.5.6 real-device notice result:
- navigation remained quick;
- Android Home appeared to refresh for a microsecond;
- **no static startup sign was visible**;
- diagnostic reported **`present=FRAME_COMMITTED` in about `103ms`**.

Ryan did not restate the other diagnostic fields or target-package stopped state in this specific v0.5.6 report, so do not infer them.

`FRAME_COMMITTED` is app-side frame-submission evidence, not physical display proof. Android documents that the frame may not currently be visible when the callback fires, and the system may change an application-overlay window's position, size or visibility.

Presentation history: v0.5.1 flashed only at the end; v0.5.2 showed nothing; v0.5.3 showed nothing and stretched Turbo to almost eight seconds; v0.5.4 showed nothing but restored fast navigation; v0.5.5 showed nothing and exposed the `DRAWN` false-positive; v0.5.6 showed nothing despite `FRAME_COMMITTED` ~103ms.

## Current candidate

**v0.5.6 / code 13** remains the latest signed machine-verified build. Exact source `5870742c83b193251b323a48e12b0ef6c5b8b5ad`.

The current small `WRAP_CONTENT` boot card is now physically rejected. Do not add another timing delay or longer wait.

The next presentation experiment should use the physically proven brightness-style surface: transparent `MATCH_PARENT x MATCH_PARENT` overlay host with `FLAG_LAYOUT_NO_LIMITS`, containing the same static top-centre CLEAN START card. Change presentation geometry only; preserve non-touch/non-focus, no movement, 500ms fail-open and all cleanup/ADB behavior.

## Exact v0.5.6 verification

Run `34230235524`, job `102074235862`, conclusion **success**. **68 JVM tests passed**, source/API/security contracts passed, lint **0 errors / 24 warnings**, permanent signer/package/version/archive checks passed, and nonvisual cold/warm launch/no-fatal smoke passed.

Signed artifact `10057548249`, ZIP `765636` bytes, SHA-256 `1c601a47fd94d002f8a4e5d5722444dc81f256057fe444cd86e16223d560a4c3`. Test artifact `10057598695`, ZIP `106686` bytes, SHA-256 `ed2d1f902e577a538c5fe8d041c79fc4c30f75930eb7780fcc0c2902a03e9c4a`.

Delivered APK `Shield-Turbo-v0.5.6.apk`, `2330594` bytes, SHA-256 `e462db094cf3f09fa4815949b492ed85f0fa12a57a53635951ce875c8c48cd78`. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Downloaded artifact digests, APK digest, built-source receipt and package/version receipt all matched. APK v2 signer independently matched `CN=BOOP Development,O=BOOP`. **No GitHub visual confirmation ran.** Ryan owns real-device appearance/timing/motionlessness acceptance.

## Next safe step

No more small-window/timing variants. If notice work continues, write the RED contract first and test a brightness-style full-screen transparent host with the static card as its child. CLEAN START core stays untouched.
