# BOOP unified status

Updated 2026-09-08. Branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged.

## Signed repair candidate

Code commit `949f1085328a3e815d9bc57747425f1f930c48db`, version 45 / `1.1.2-unified-assist-repair`. Run `34201200463` completed successfully; artifact `BOOP-Unified` ID `10045928699`. Extracted APK SHA-256 `217e004f26bca33066e3d2089d2e2bc448c102c332abb25f97cf00122d5ed239`; permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Non-visual verification passed: 58 Shield focused tests + 66 unified wake/routing/assistant tests with zero failures/errors/skips, Launcher lint, compilation, package identity, manifest integration, permanent signature and ZIP integrity. No visual, screenshot/golden, aesthetic source-string, emulator launch/install or physical-device acceptance ran.

## Previous physical failures now addressed in code

Ryan physically found the prior candidate still had four failures: assistant choice did not actually change Android's assistant, Home device labels appeared as literal `null`, neither BOOP nor the custom wake name triggered acoustically on phone, and the copied eye PNG had an opaque black border with no visible blink.

Current repairs: HA JSON null/device-name fallback is fixed; Sherpa wake arming no longer gets vetoed by an advisory speech-support probe; the same locked eye RGB pixels are packaged with real alpha transparency; assistant role eligibility now includes both `VoiceInteractionService` and explicit `ACTION_ASSIST`. Existing room switching, physical-device-only filtering, idempotent Shield scaling, iris-only hue, headphones and puppetry remain preserved.

Physical acceptance is still required for actual assistant role takeover where firmware permits, remote-button activation, actual audio from the Shield remote microphone, clean one-shot recording/cancel/repeat/return behaviour, BOOP/custom acoustic wake, exact eyes/blink, repeated-open scale stability, room switching and real device names. Opening BOOP alone is not remote-mic success.

Protected physical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. No automatic install/grants, signing/package identity change, Windows synchronization or unattended monitoring.
