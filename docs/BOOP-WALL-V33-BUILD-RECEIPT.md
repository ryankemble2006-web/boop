# BOOP Wall v33 signed build receipt

Recorded 2026-09-07. Branch `boop-wall-free-chat-wip`.

- Application/build commit: `e3507bde3f296dcb419a1dcef0faf735c7243525`.
- VersionCode: 33. VersionName: `0.4.13-wall-blink-speed`.
- Package: `com.boop.alpha1`.
- Workflow run: `34078487851`; job: `101609130595`; build/sign result: SUCCESS.
- Artifact: `10002906331`, `BOOP-Wall-Free-Chat-candidate`.
- Artifact ZIP bytes: 58502662.
- ZIP SHA-256: `4da349afe368758a5217e9eff250371156695c87a9e8e29fd765fc1d9327953f`.
- Delivered filename: `BOOP-Wall-v33-Faster-Blink.apk`.
- APK bytes: 139485306.
- APK SHA-256: `bb94214405291eafce0c99902703ea943facfe9c335e4120d80d3c4155b51b1b`.
- Existing signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

GitHub built the existing stable-signed debug variant and verified package/version,
APK signature and signer continuity. Temporary runner signing material was removed.
Downloaded ZIP/APK digests match the artifact/build receipts; the built-commit and
signer receipts were read. No replacement key or physical install was used.

## Explicit verification limit

Ryan requested no tests. Every regression/unit/runtime/emulator step was SKIPPED;
no instrumentation APK was built. Separate polish workflow run `34078487848` was
also SKIPPED. This is successful assembly/signing, NOT an all-tests-passing run.
No runtime or visual tests were performed locally. Only delivery integrity checks
were performed after download. Do not automatically run tests from this receipt.

Only app behavior delta from v32: blink duration 220 -> 183 ms (220/1.2 rounded).
The 3-7 second blink delay, eyelid geometry, text, sleep timer and other features
are unchanged. Ryan confirmed the previous v32 blink physically; v33 speed has
not yet been accepted. No accepted checkpoint or another app branch was changed.

Final reconciliation changes documentation only and uses [skip ci] to prevent an
unrequested build/test run. Exact earlier evidence remains in the v32 handoff.
