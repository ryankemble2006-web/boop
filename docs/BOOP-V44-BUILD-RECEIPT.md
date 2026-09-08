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

## Second download and chat delivery confirmation

The room/iris development chat independently downloaded artifact10042812867, verified the ZIP digest and built commit, and delivered `BOOP-Unified-v44-4044ee5.apk`. The actual certificate read from the downloaded APK v2 signing block hashes to the same permanent certificate above. DEX inspection found BoopIrisTint, HaEntityCategory, RoomDeviceControls, TvHomeView DeviceRow, encodeUnigram, drawLockedEyes and runEyeBlink. Eye PNG, original BOOP keywords and bpe.model are byte-identical to parent dcc7acf. These checks establish binary provenance/presence, not visual or acoustic behaviour. Test waking with Voice Settings closed, foreground and wirelessly charging; undocked is intentionally tap-only.

Earlier in that chat, intermediate dcc7acf32acd2cf44d25e1f88fce7d388695e030 was also delivered: run34192089796, artifact10042611490, APK SHA-256 `66ca8893b35989b11946a650fd85a8380b511f9aa25544322825b43a05fe5500`, ZIP SHA-256 `da65eb5406a269409f3a568aeb10822725e4bd25015883ff4d929d9843933ba1`. Its 111 functional tests/build/signing/upload passed, but the whole run later ended cancelled during post-upload non-visual launch smoke. Do not call that whole run green. The newer verified v44 link supersedes the intermediate recommendation.

Concurrent v44 source and its finalized handoff/status/context/memory at793bfff2b654173913162b5f219660adccec9e6d were read and preserved instead of overwritten by a stale pending-build receipt. This confirmation only extends the receipt; it does not change app code, workflows, permissions, installation or signing. No Windows synchronization or unattended monitoring is claimed.
