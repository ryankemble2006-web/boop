# BOOP unified handoff

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Always re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Physically proven rollback checkpoint

Ryan physically confirmed signed v48 on the Pixel in the established continuous-wake condition: charger -> Android green microphone indicator ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP.

The exact built v48 code is permanently pinned at:

- branch `checkpoint-boop-unified-v48-wake-arm`
- commit `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`
- version 48 / `1.2.2-unified-wake-arm`
- workflow `34218173825`
- APK SHA-256 `0264c3e289aab06a7be45067ce44bd72124355b11f9cc0a8ffa73afb7f4c5f02`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Do not repoint that checkpoint. It proves upstream sleeping-mic/listening plus one established BOOP wake phrase.

## Current signed candidate: v51 wake-command normalization

Ryan physically tested v50 and confirmed the wake itself still worked: `Hey BOOP` woke BOOP. Spoken rename still failed. No tested rename wording entered the five-say flow, including the natural `change name to steve`, fuller sentences, and `Hey BOOP` followed by a pause for the listening cue and then the rename command. v50 spoken rename is therefore a physical FAIL downstream of wake detection.

The v50 parser repair was real but incomplete. Source tracing found the next upstream defect in the actual WAKE result path: `MainActivity` calls the one-argument `BoopWakeTranscriptNormalizer.stripLeadingWakeWord(best)` before `handleRecognizedSpeech()`. The wake-recognition audio pipe includes pre-roll, but that normalizer removed only a leading bare `BOOP`. It did not remove the established natural calls such as `HEY BOOP`, `OI BOOP`, `GOOD MORNING BOOP`, `BOOP WAKE UP`, and the rest of the 33-form BOOP grammar. A recognition result containing the natural wake call before the command could therefore reach the local rename parser still prefixed by that wake call and fall through to ordinary command handling.

Do not overstate this as proof of the exact string Android returned on Ryan's Pixel. It is a concrete production-path defect that matches the physical symptom and is now covered by a regression test. v51 still needs physical acceptance.

### TDD trail

RED: commit `5794ed194cf90a744fc41c0b789718ceac97605c`, workflow `34224636115`. The new test feeds every established BOOP wake form before `change name to Steve`. The unified suite ran 79 focused tests and failed exactly one: `BoopWakeTranscriptNormalizerTest.stripsAllEstablishedBoopWakeCallsBeforeCommand`.

Minimal GREEN: commit `de2e1d825f4074f9b39ee9c409492e3edf46bff6` changes only `BoopWakeTranscriptNormalizer`. BOOP's complete natural wake grammar is stripped longest-first before command routing; custom-name normalization retains BOOP as fallback. Workflow `34224978703` completed successfully. No wake controller, microphone source, Sherpa model, five-say profile math, charging policy, HA, eyes, blink, Shield routing, assistant, headphones or puppetry code changed.

Release identity: versionCode 51 / `1.2.5-unified-wake-command-normalize`. Final built commit `4274ed008014d1ed5810af29b64b164bf8477072`.

### v51 signed receipt

- Workflow: `34225351709` SUCCESS
- Artifact: `BOOP-Unified`, ID `10055532598`
- APK SHA-256: `e768248f27c671d5c4377d68905405d8c5890d7125390e13bb6c5177aad23e5c`
- Artifact ZIP SHA-256: `11127a3b08387ea64c07c66af1d68d3e9dbe6b7896f0f5ab8309cd669fcf2861`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh final evidence: non-visual integration/materialization passed; Launcher lint passed; Shield focused tests 58/58 and unified focused tests 79/79 passed with zero failures/errors/skips; signed assembly, package/version, manifest, permanent signer and archive integrity passed. Downloaded ZIP/APK hashes matched workflow/GitHub receipts. No emulator/device launch, screenshots, visual acceptance or v51 physical rename/enrolment acceptance ran.

Detailed receipt: `docs/BOOP-V51-WAKE-COMMAND-NORMALIZE-RECEIPT.md`.

## Five-say custom-name contract

The existing local enrolment implementation remains intact:

- `BOOP` is permanently valid and never requires training;
- a custom name is additive, never a replacement;
- five natural spoken examples are captured from the existing single controller-owned 16 kHz PCM stream;
- active-speech segments become amplitude-normalised pronunciation features and a compact profile;
- raw enrolment PCM is not persisted;
- the learned matcher is additive to Sherpa and must fail safely without disabling BOOP;
- custom names receive all 33 established natural wake forms;
- changing to a new custom name clears the old matching profile;
- unchanged names do not nag; a matching profile is reused;
- manual Train remains available for deliberate retraining;
- no second mic listener and no cloud wake training.

## Required next Pixel test

1. Confirm green sleeping mic + `Hey BOOP` still works.
2. After wake, say exactly `change name to Steve`.
3. Expected next behavior is `Say Steve five times.`
4. If step 2 still fails, stop there. Do not add more rename synonyms. Inspect the actual recognizer result / wake command-capture timing and handoff next.
5. If the prompt appears, say `Steve` five times naturally with short pauses, confirm completion/re-arm, test `Steve` plus a few natural variants, then confirm `Hey BOOP` still works.

## Architecture boundary: clean Shield HOME remains standalone

The clean Nvidia Shield HOME replacement remains a standalone test app on branch `boop-shield-clean-launcher`, package `com.boop.shieldhome`. It is not part of AIO until Ryan explicitly approves a later merge. Unified Shield routing remains on the existing AIO Shield puppet.

## Protected BOOP contracts

Preserve the approved paired black-lidded eye master, iris-only hue, working blink timing/gates, headphones/puppetry and five-digit yellow hands. Preserve physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. Assistant remote invocation/audio remains separately unresolved and must stay on supported Android routes with no competing microphone listener, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs functional/non-visual verification only. Ryan owns visual, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.
