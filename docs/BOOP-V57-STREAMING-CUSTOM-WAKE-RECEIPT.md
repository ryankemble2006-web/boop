# BOOP Unified v57 streaming custom wake receipt

Updated 2026-09-08. Canonical branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged.

## Physical evidence that triggered v57

Ryan physically tested signed v56 on the powered Pixel. `Steve` and `BOOP` both wake BOOP, tap-to-talk works, and a deliberate pause between wake name and command allows local Home Assistant commands to run and reply `Done`. Natural one-breath wake + command still failed.

Using the pull-only diagnostic path after a failed one-breath `Steve lights on`, the retained trace was:

`WAKE ASR ERROR 7 +1502ms ready=28 begin=290 end=1405 partial=- final=-`

Sanitized accompanying state: wake name `Steve`, external power true, microphone permission true, recovery reached `ARMED`.

This proves Android command ASR observed speech boundaries but decoded no transcript. It also proves the powered silent-recovery path returned to an armed wake state.

## Root cause

The learned custom-name path in `BoopWakeTemplateMatcher` used `BoopWakeUtteranceSegmenter`, which only emitted a candidate after two quiet 100 ms chunks. A custom name therefore required roughly 200 ms of trailing silence before it could be evaluated.

For natural continuous speech such as `Steve lights on`, the detector kept consuming the command as part of the same utterance. Wake detection arrived too late for the downstream command recognizer, even though v56 had already removed the speaker cue and retained a bounded 100 ms command bridge.

This is distinct from the old v53/v54 defect. The full one-second wake-history pre-roll remains forbidden.

## v57 implementation

v57 changes only the learned custom-name detector:

- trained custom names can be evaluated while speech is still active rather than waiting for trailing silence;
- the existing silence-terminated matcher remains as a fallback;
- the same controller-owned 16 kHz PCM stream remains the only microphone source;
- expensive pronunciation matching is gated behind a cheap RMS/speech activity check so quiet-room continuous wake does not continuously run spectral feature extraction;
- unrelated continuous speech has an explicit negative regression test;
- the v56 100 ms command bridge remains unchanged;
- the artificial wake speaker cue remains removed;
- Sherpa/default `BOOP`, Home Assistant routing, three-second command window, five-say training, powered wake, silent failure recovery, pull-only diagnostics, visuals, Launcher and Shield behavior are otherwise unchanged.

## TDD trail

RED:

- commit `032ba991f4f70880bd2c108473c34b7701db7917`
- workflow `34248072092`
- focused unified suite ran 92 tests and failed exactly one new regression: `BoopWakeTemplateMatcherTest.trainedNameMatchesBeforeImmediateCommandFinishes`.

Initial GREEN implementation:

- commit `964142749a2560b90a8be608b5c5deaa0f893066`
- workflow `34248439829` SUCCESS
- not shipped because review found the first streaming search performed too many expensive feature extractions for an always-listening device.

Reviewed/performance-gated implementation:

- built code `6c8131200ee0ff8a91464569a982312cb5512cb2`
- version 57 / `1.2.11-unified-streaming-custom-wake`
- workflow `34249050741`, attempt 2 SUCCESS
- artifact `BOOP-Unified`, ID `10065473551`
- artifact digest `sha256:496786d407a39d8034ec4dfd335e903c4ae8e3531a1128bf1f3e4a82440d1912`
- APK SHA-256 `3ce0593f61fbf6b35e6fbb664bc5bc7984b053844c7f6be83afe2955ab713459`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Verification on the final built code:

- non-visual integration/materialization PASS;
- seamless wake-command handoff contract PASS;
- Launcher lint PASS;
- Shield focused functional tests 58/58, zero failures/errors/skips;
- unified focused functional tests 92/92, zero failures/errors/skips, including the no-trailing-silence custom-wake regression and unrelated-speech rejection;
- signed assembly PASS;
- package/version/manifest checks PASS;
- permanent signer PASS;
- APK ZIP integrity PASS;
- artifact upload PASS on workflow attempt 2.

Attempt 1 built and verified the same APK but GitHub artifact finalization returned a transient 403 after the bytes were uploaded. No code change was made between attempts.

## Required physical Pixel test

Install v57 over v56 without uninstalling. Keep the Pixel on external power and use natural speech with no deliberate pause:

1. `Steve lights on`
2. `Steve show diagnostics`
3. `BOOP lights off`

The first two specifically test the v57 learned custom-name change. The third keeps the default BOOP/Sherpa path separate. If Steve works but BOOP still requires a pause, do not treat them as the same defect and do not widen the command bridge blindly.

Physical v57 acceptance is pending Ryan's device test. CI/signer green is not physical green.
