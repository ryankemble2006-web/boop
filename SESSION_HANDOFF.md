# BOOP unified handoff

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Always re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Physically proven rollback checkpoint

Ryan physically confirmed the signed v48 wake-arm build on the Pixel in the established continuous-wake condition: placing BOOP on the wireless charger turned on Android's green microphone privacy indicator; the green indicator stayed on after BOOP went to sleep; saying `Hey BOOP` woke BOOP from sleep.

The exact built v48 code is permanently pinned at:

- branch `checkpoint-boop-unified-v48-wake-arm`
- commit `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`
- version 48 / `1.2.2-unified-wake-arm`
- workflow `34218173825`
- APK SHA-256 `0264c3e289aab06a7be45067ce44bd72124355b11f9cc0a8ffa73afb7f4c5f02`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Do not repoint that checkpoint. It proves the upstream sleeping-mic/listening repair and the `Hey BOOP` sample, not every wake phrase.

## Current signed candidate: v49 automatic five-say enrolment

Ryan asked to continue from the v48 checkpoint and finish the previously agreed custom-name flow: choose a name such as Steve, then BOOP should automatically ask the user to say it five times.

Most of the training engine was already present in v48. The missing product-flow edge was typed Voice Settings: a changed custom name could be saved without automatically entering training, leaving the separate Train button as a second step.

The v49 implementation adds a narrow policy and flow on top of v48:

- changing BOOP -> Steve (or one custom name -> another) queues automatic enrolment;
- when Voice Settings closes, BOOP prompts `Say Steve five times.`;
- five utterances are captured through the existing `BoopWakeWordController` microphone owner;
- unchanged custom names do not nag on every settings close;
- BOOP itself never needs training;
- a matching already-stored profile is reused rather than automatically retrained;
- the manual Train action remains for deliberate retry/retraining;
- BOOP remains permanent fallback and custom names keep all 33 established natural wake forms;
- no second mic, no cloud wake training, no change to v48's physically-proven real-attempt arm gate.

Training data remains local: active-speech segments are converted to amplitude-normalised pronunciation features and a compact profile is stored. Raw enrolment PCM is not persisted.

### TDD trail

RED: `72591554376d2cada7df24d99d5934443a986278`, workflow `34220316310`. The automatic-training policy test failed exactly because `BoopWakeTrainingPolicy` did not exist.

Implementation: `7ed577d2cb683da0cf7ae5339efc509566123140`, adding `BoopWakeTrainingPolicy` plus the settings-close enrolment flow. The existing v48 arm patch remains applied after the wake-name materialization.

Final release identity commits: `097a3a643022b1f3897ba82e0b73e3eb8601af85` then `87a7abee880b9283d51fb71ed2bf9bd9ed187b28`.

### v49 signed receipt

- Version: 49 / `1.2.3-unified-wake-enrolment`
- Built code: `87a7abee880b9283d51fb71ed2bf9bd9ed187b28`
- Workflow: `34221275050` SUCCESS
- Artifact: `BOOP-Unified`, ID `10053919758`
- APK SHA-256: `2fd65505bea205cce8e7cdc2124cba4a4fa818c5b1fd4ba0efdc5c24f064ffd9`
- Artifact ZIP SHA-256: `fc27c729a848fbda98dee882b626633da46f1fd338a98fa08911cc76106eecb9`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh final-run evidence: non-visual integration/materialization passed, Launcher lint passed, 58 Shield focused tests and 78 unified focused tests passed with zero failures/errors/skips, signed assembly passed, and package/version/manifest/permanent-signer/archive verification passed. Downloaded ZIP and APK hashes matched the workflow receipts. No emulator/device launch, screenshots, visual acceptance or v49 acoustic acceptance ran.

## Next physical test

Install v49 over v48 on the Pixel while BOOP is foreground and wirelessly charging/docked.

1. Confirm the v48 behavior first: green mic remains on while asleep and `Hey BOOP` wakes BOOP.
2. In Voice Settings change BOOP's name to `Steve`, then close/Done.
3. Confirm BOOP automatically says `Say Steve five times.` without requiring the Train button.
4. Say `Steve` five times naturally with short pauses; completion should end with `Steve. Got it.` and the normal sleeping wake mic should re-arm.
5. Test bare `Steve`, `Hey Steve`, `Oi Steve`, `Morning Steve`, `Steve wake up` and finally `Hey BOOP` again.
6. Record misses and false wakes separately before changing thresholds.

If v49 regresses the green mic or sleeping `Hey BOOP`, immediately return to the exact v48 checkpoint and investigate the lifecycle handoff. If the green mic stays healthy but Steve misses, investigate downstream template/Sherpa matching.

## Architecture boundary: clean Shield HOME remains standalone

The clean Nvidia Shield HOME replacement is still a standalone test app on branch `boop-shield-clean-launcher`, package `com.boop.shieldhome`. It is not part of AIO until Ryan explicitly approves a later merge. Unified Shield routing remains on the existing AIO Shield puppet.

## Protected BOOP contracts

Preserve the approved paired black-lidded eye master, iris-only hue, working blink timing/gates, headphones/puppetry and five-digit yellow hands. Preserve physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. Assistant remote invocation/audio remains separately unresolved and must stay on supported Android routes with no competing microphone listener, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs functional/non-visual verification only. Ryan owns visual, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.
