# BOOP Wall v33: faster blink, build/sign only

Updated 2026-09-07. Branch `boop-wall-free-chat-wip`.

## Current candidate

- VersionCode 33 / `0.4.13-wall-blink-speed`, package `com.boop.alpha1`.
- Application/build commit `e3507bde3f296dcb419a1dcef0faf735c7243525`.
- Exact artifact, signing and build receipt: `docs/BOOP-WALL-V33-BUILD-RECEIPT.md`.
- One behavior change: blink duration 220 -> 183 ms (1.2x speed, rounded).
- Irregular 3-7 second gaps, eye geometry, v32 larger text and original sleep
  deadline remain unchanged. No new permissions or changes to other apps.
- Ryan explicitly requested no tests/emulators. This is a build/sign-only
  candidate, not a fresh regression-tested or physically accepted checkpoint.

## Actual physical evidence

Ryan reported v32: "he blinks just fine". The earlier CI visual-capture failure
must not be described as proof that the physical blink is broken. Its exact
unresolved history remains in `docs/BOOP-WALL-V32-HANDOFF.md`.
That comment does not verify v32 text size/clipping, sleep or every orientation.
The new v33 speed remains untested. v31's browser copy/open/paste path was also
confirmed by Ryan; that is limited evidence for the path he actually used.

## Accepted Wall baseline (unchanged)

- Physical code: `595e1daa43393882a0e5de43967545ac526b8b66`.
- Protected tag: `checkpoint-boop-wall-595e1da` (not moved by this work).
- Version: 29 / `0.4.9-alpha6.5.6-wall`.
- CI: 33992704568; Pixel 7 Pro acceptance 2026-09-05.
- Natural wake, tap speech, conversation, immediate house/media, QR pairing and
  firm shake-to-puppet response were physically verified on that baseline.
- With OpenCode stopped, local house/media continued working.
- Signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Accepted APK SHA-256: `79ac40677687c4225989fa095644d5d97d36876150afe704e80eb6091d55530b`.
- Preserved Wall branch remains separate. Its v30 swipe has emulator evidence
  only. Do not promote a physical checkpoint without Ryan's device acceptance.

No automatic physical installation. Launcher, Shield, local house/media clients,
signing credentials and permissions are untouched. Record Ryan's next feedback;
do not restart the old visual-test loop for this explicitly no-tests update.
