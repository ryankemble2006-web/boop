# BOOP Wall v32: larger instructions and idle blinks

Updated 2026-09-07. Branch `boop-wall-free-chat-wip`.

## Signed test candidate; NOT full CI-green

- Version 32 / `0.4.12-wall-blink-text`, package `com.boop.alpha1`.
- Built `6e48e3bc05f7269376d54179ab32025e1a0b72b9`; run34077206213/job101605575582.
- Artifact10002578514; `BOOP-Wall-v32-Blink-Text.apk`, 139485310 bytes.
- APK SHA-256 `3316a193188cb0b9a0cc31846771b3f57c7987da13747d6b1fe11da22647c96b`.
- ZIP SHA-256 `e99a5258e1e5e6d3191818d5f63ec7950d8de889640f475f59808d058784dfd1`.
- Existing permanent signer verified in CI. Downloaded receipts, ZIP/APK digests,
  archive integrity and presence of new production classes independently checked.

Implemented: 50%-larger bold two-line paste instructions with the same browser
handoff; irregular 3-7 second awake-idle blinks; original sleep deadline unchanged.
No new permissions, settings screens, signer or other app changes.

PASS: 159 local source/JVM regressions; CI bridge/Java/Android unit/build/signing,
real wake-microphone startup, existing mode menu, persistence/revert/cancellation.
FAIL: new instrumented natural-blink frame capture on the portrait emulator.
Exact cause remains unresolved; awake/focused/attached/foreground were true.
Earlier same-app run passed portrait blink/reopen/busy/sleep but failed landscape
sampling. Final landscape/pairing checks were not completed. Do not call either
feature's physical rendering verified by these tests.

Toast callback and span checks passed, but inspected PNGs did not show the toast;
font rendering/clipping remain unverified. Evidence artifact10002576627.
This signed debug APK is delivered for user testing, not an accepted checkpoint.

Ryan's v31 feedback confirmed copied query -> new chat -> paste instruction on
his phone. That does not verify v32 or every v31 flow on both Pixels.

## Accepted Wall baseline (unchanged)

- Physical code: `595e1daa43393882a0e5de43967545ac526b8b66`.
- Annotated tag `checkpoint-boop-wall-595e1da` reread live and still points there.
- Version: 29 / `0.4.9-alpha6.5.6-wall`.
- CI: 33992704568; Pixel 7 Pro acceptance 2026-09-05.
- Natural wake, tap speech, conversation, immediate house/media, QR pairing and
  firm shake-to-puppet response were physically verified on that baseline.
- With OpenCode stopped, local house/media continued working.
- Signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Accepted APK SHA-256: `79ac40677687c4225989fa095644d5d97d36876150afe704e80eb6091d55530b`.
- Preserved Wall branch is still `3a702f8`. Its v30 swipe has emulator evidence
  only. Do not promote a physical checkpoint without Ryan's device acceptance.

## Protected / next

Wall/Home/Routines checkpoints and accepted APKs remain untouched. No changes to
Launcher, Shield, local house/media clients, signing credentials or permissions.
No automatic physical installation. Timed voice routines remain excluded.

Next: actual Pixel check of the new text and awake blink/sleep; diagnose the
remaining visual gate. Record observations before any promotion. Source/build
and physical evidence are different states. See SESSION_HANDOFF.md.
