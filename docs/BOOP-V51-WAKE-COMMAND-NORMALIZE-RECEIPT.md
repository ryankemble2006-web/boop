# BOOP v51 wake-command normalization receipt

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged.

## Physical evidence that triggered this repair

Ryan physically tested signed v50 on the Pixel. `Hey BOOP` still woke BOOP, so the physically-proven v48 listening/arm path remained healthy. After wake, no tested rename wording entered the custom-name flow. Ryan tried the natural command `change name to steve`, fuller rename sentences, and also `Hey BOOP` followed by a pause for the listening cue and then the rename command. The result remained the same rather than reaching `Say Steve five times.`

Treat v50 spoken rename as a physical FAIL downstream of wake detection. Do not tune Sherpa wake sensitivity from this result.

## Root cause

The wake-recognition path feeds Android speech recognition from the controller-owned PCM pipe, including wake pre-roll. In `MainActivity` WAKE results are normalized before `handleRecognizedSpeech()` runs. The production call used the one-argument `BoopWakeTranscriptNormalizer.stripLeadingWakeWord(best)`.

That one-argument normalizer stripped only a leading bare `BOOP`. It did not strip the established natural BOOP calls such as `HEY BOOP`, `OI BOOP`, `GOOD MORNING BOOP`, `BOOP WAKE UP`, and the rest of the 33-form wake grammar. Therefore a wake recognition result containing a natural wake call before the command could reach the local rename parser with the wake call still attached and fall through to ordinary command handling.

This explains the physical symptom and is a distinct layer from the v50 rename-intent grammar repair. It does not prove the exact transcript Android returned on Ryan's device; physical v51 testing remains required.

## TDD trail

RED commit `5794ed194cf90a744fc41c0b789718ceac97605c` added a focused regression test requiring every established BOOP wake form from `BoopWakeKeywordBuilder.naturalPhrases("BOOP")` to be removed before `change name to Steve` is routed. Workflow `34224636115` ran 79 unified focused tests and failed exactly one: `BoopWakeTranscriptNormalizerTest.stripsAllEstablishedBoopWakeCallsBeforeCommand`.

Minimal production repair commit `de2e1d825f4074f9b39ee9c409492e3edf46bff6` changes only `BoopWakeTranscriptNormalizer`: the default BOOP path now strips the complete natural wake-call grammar longest-first, and the custom-name overload still falls back to BOOP. Ordinary non-addressed commands remain untouched. No wake controller, microphone source, Sherpa model, enrolment/profile math, charging policy, HA, eyes, blink, Shield routing, assistant, headphones or puppetry code changed.

Workflow `34224978703` completed successfully after that repair, including the new 79-test wake suite.

## Final signed v51 receipt

- Version: 51 / `1.2.5-unified-wake-command-normalize`
- Built code: `4274ed008014d1ed5810af29b64b164bf8477072`
- Workflow: `34225351709` SUCCESS
- Artifact: `BOOP-Unified`, ID `10055532598`
- APK SHA-256: `e768248f27c671d5c4377d68905405d8c5890d7125390e13bb6c5177aad23e5c`
- Artifact ZIP SHA-256: `11127a3b08387ea64c07c66af1d68d3e9dbe6b7896f0f5ab8309cd669fcf2861`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh final-run evidence: non-visual integration/materialization passed; Launcher lint passed; Shield focused tests 58/58 and unified focused tests 79/79 passed with zero failures/errors/skips; signed assembly, package/version, manifest requirements, permanent signer and APK archive integrity passed. The downloaded artifact ZIP digest matched GitHub metadata and the extracted APK hash matched the workflow receipt.

No emulator/device launch, screenshots, visual acceptance or v51 physical rename/enrolment acceptance ran.

## Required Pixel check

1. Keep BOOP foreground and in the established wireless-charging continuous-wake condition.
2. Confirm the green mic remains active and `Hey BOOP` wakes BOOP.
3. After wake, say exactly `change name to Steve`.
4. Expected next behavior is the local five-say prompt: `Say Steve five times.`
5. Stop at this boundary if step 3 still fails. Do not add more rename synonyms. The next investigation would inspect the real recognizer result / command-capture timing and handoff.
6. If the prompt appears, continue with five natural `Steve` examples, then test the trained name and finally confirm BOOP fallback still wakes.

The exact physically-proven rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`; never repoint it.
