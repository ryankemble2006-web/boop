# BOOP v44 signed test-candidate receipt

Verified 2026-09-08. Physical and visual acceptance remain pending Ryan.

- Branch: `boop-unified`.
- Built code: `4044ee55b5a39e2a220ee897de0393b796c29a5f`.
- Version: 44 / `1.1.1-unified-eyes-wake-home`.
- Package: `com.boop.alpha1`.
- Workflow: `34192698906`, build job `101953848892`, completed successfully.
- Artifact: `BOOP-Unified`, ID `10042812867`, ZIP size 60396442 bytes.
- ZIP SHA-256: `ea5b6225c45a1cf1e4b22d4497ba181f8757f3de2a36cbe986c2263e373490f4`.
- APK size: 142048925 bytes.
- APK SHA-256: `5c60b904d06d8a94ad3ab117e2da86c5726c91ff8f2ca40845a0f2266e9966f6`.
- Permanent signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## What was actually checked

CI passed non-visual integration contracts, materialization, Launcher lint, 52 focused Shield functional tests and 59 focused unified/wake/routing/lifecycle tests. Both suites reported zero failures, errors or skipped tests. Java compilation, assembly, permanent signing, package/version/entry and required-component checks, signer comparison and APK ZIP integrity passed. The signed artifact was uploaded and temporary signer cleanup passed.

The downloaded artifact ZIP digest matched GitHub's artifact metadata. Its single APK was extracted; the built-commit receipt and APK SHA-256 matched. Both ZIP archives passed integrity testing. Downloaded badging recorded package/version/entry correctly. This proves delivered bytes match the signed CI output, not that physical behaviour is accepted.

No screenshot, image comparison, aesthetic source-string guard, visual/animation judgement, emulator install/launch or automated real-device acceptance ran in this workflow. Ryan explicitly owns those checks. The retained tests are focused code/logic checks, not the entire historical suite.

## Included work

Preserves the concurrent `dcc7acf` room controls, compact HA category handling, cached iris-only hue and accumulated UNIGRAM/custom-stream fallback repairs. Adds canonical phone eye-layout/blink helpers to Shield's generated internal library. TV framing stays uniform in the existing corner slot; locked headphones are unchanged. No image regeneration or microphone/permission/identity/signing-policy changes.

Required manual checks: Home room picker/device actions; every Settings row and D-pad focus; exact eyes/blink/iris appearance; Enable/access-screen behaviour; typed/verbal rename/reset and BOOP fallback while foreground on wireless dock; undocked tap-to-talk and heat/mic release. Acoustic cause of the earlier failed wake test remains unproven.

The protected physical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. A signed candidate does not replace it until Ryan accepts the relevant real-device behaviour. Documentation commits after the code commit do not change this APK and do not trigger another build.
