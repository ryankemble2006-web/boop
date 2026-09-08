# BOOP v53 persistent wake diagnostic receipt

Date: 2026-09-08

Canonical branch: `boop-unified`
Package: `com.boop.alpha1`
Version: 53 / `1.2.7-unified-wake-diagnostic-hold`
Permanent signer unchanged.

## Physical evidence that triggered v53

Ryan installed v52 on the Pixel and reported that saying `Hey BOOP` brought up an error, but the message disappeared too quickly to read or capture while the phone remained on its charger. Do not infer the Android error code from that observation. It proves only that the v52 diagnostic path surfaced a fast failure after the wake attempt.

The diagnostic itself was therefore not yet usable evidence. v53 changes diagnostic presentation only so the next physical run can capture the exact recognizer result/error.

## Scope

v53 preserves the existing wake controller, single-microphone ownership, Sherpa/template matching, post-wake Android `SpeechRecognizer`, transcript normalization, rename parser, five-say enrolment, charging policy, Home Assistant, eyes/blink, Launcher, Shield and assistant routing.

The change is intentionally narrow:

- a non-terminal trace that is still pending after 4.5 seconds remains a temporary toast;
- a terminal Android recognizer result or error is shown in a modal `BOOP wake diagnostic` dialog;
- the dialog is non-auto-expiring and stays visible until Ryan presses `Close`;
- a synchronous recognizer `startListening()` exception is captured in the same persistent diagnostic path instead of a transient toast.

No second microphone listener, cloud diagnostic upload, raw audio persistence, permission change or signer/package change was added.

## Test trail

Primary RED: commit `7a27ffd60d01b385b096f8396bf0e18f7c251329`, workflow `34230769806`. The focused unified wake build failed because the new terminal-diagnostic acknowledgement contract did not yet exist (`BoopWakeDiagnosticTrace.requiresAcknowledgement()`).

The implementation then added the minimal acknowledgement state and persistent terminal presentation. A concurrent v53 version bump briefly dropped existing app dependencies; commit `3b85e5ae8babe151c3f95c6aa4637d91fc5b2cb9` restored those dependencies on top of the v53 diagnostic work before the accepted build.

## Final signed v53 receipt

Built code: `3b85e5ae8babe151c3f95c6aa4637d91fc5b2cb9`
Workflow: `34231784857` SUCCESS
Artifact: `BOOP-Unified`, ID `10058183437`
Artifact ZIP SHA-256: `16a574baa521ca54824527090f6b3e9ae216a0805814b4edec4051a30a1d8624`
APK SHA-256: `d1d21ff117bf21b761d7f9fb4f78d0499c3ba662c14408c3c17373428b92dbda`
Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Verification from the final workflow:

- non-visual integration/materialization passed;
- Launcher lint passed;
- Shield focused functional tests: 58 tests, 0 failures, 0 errors, 0 skipped;
- unified focused functional tests: 84 tests, 0 failures, 0 errors, 0 skipped;
- signed assembly passed;
- package/version/manifest checks passed;
- permanent signer verification passed;
- APK archive integrity passed;
- artifact upload passed.

No emulator/device launch, screenshot, appearance or acoustic acceptance was performed by GitHub. v53 is CI/signer green, not physically accepted.

## Required next Pixel test

1. Install v53 over the existing BOOP install. Do not uninstall first.
2. Keep the Pixel on its charger and allow BOOP to settle into the established sleeping-wake state.
3. Confirm the Android green microphone indicator is present.
4. Say `Hey BOOP` once.
5. If the post-wake recognizer terminates, the `BOOP wake diagnostic` dialog should remain on screen until `Close` is pressed.
6. Photograph/screenshot the complete message or copy it exactly and report it back.
7. Stop there. Do not continue rename grammar experiments until the exact diagnostic is known.

The protected physical wake rollback remains v48 at `checkpoint-boop-unified-v48-wake-arm` / `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.
