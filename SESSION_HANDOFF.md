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

Do not repoint that checkpoint.

## Current signed candidate: v54 wake-command audio boundary

v53 finally produced usable physical evidence. On the charged Pixel Ryan said `Hey BOOP` once and captured:

`WAKE ASR RESULT +583ms ready=18 begin=153 end=544 partial="hey pooop" final="hey pooop"`

That is a successful Android recognizer result, not an error. It proves the post-wake command recognizer was being fed the wake phrase itself and was completing before Ryan could speak a separate command.

Source tracing found the exact production-path cause in `BoopWakeWordController`: the controller kept one second of wake-detection PCM (`PRE_ROLL_SAMPLES = 16000`) and wrote that entire historical buffer into the command recognizer pipe immediately after wake detection. Android therefore began command recognition with the already-complete wake utterance.

v54 establishes a strict boundary: wake-detection history is not command audio. The recognizer prelude is now empty and the existing pipe continues with live post-detection PCM only. The single controller-owned 16 kHz `AudioRecord`, three-second command window, Sherpa/template detector, wake sensitivity, transcript normalizer, rename parser, five-say enrolment, charging policy, HA, eyes/blink, Launcher, Shield and assistant routing are unchanged.

TDD trail:

- RED `d68fca32f25b9de360404170ff46576baabb33d5`, workflow `34233607842`: the selected unified wake stage failed because the new `BoopWakeCommandAudioPolicy` did not yet exist.
- Policy `edb2750e1140e5f8d4e39ec2e65b9097d71cef01`.
- Controller GREEN `f90031830dfcf7f46c5a3644502a5bdf278acc06`, workflow `34233966994` SUCCESS.

Final v54 receipt:

- built code `fe26f29cb330b450e5e9894a4588b19ae9d7152a`
- version 54 / `1.2.8-unified-wake-command-boundary`
- workflow `34234618256` SUCCESS
- artifact `BOOP-Unified`, ID `10059395955`
- APK SHA-256 `ee6a5ade5538cf3b0b4aa1346cdeb5b957c9ed56ff214279c2d7c46e428fd287`
- artifact ZIP SHA-256 `0f7bb5168a961aa1e44bf455c070a3db8e112c8a55f9360db2236fc1dc4d6908`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh final evidence: non-visual integration/materialization passed; Launcher lint passed; Shield focused tests 58/58 and unified focused tests 85/85 passed with zero failures/errors/skips; signed assembly, package/version, manifest, permanent signer and APK archive integrity passed; artifact upload passed. No emulator/device launch, screenshots, visual acceptance or acoustic acceptance ran.

Detailed receipt: `docs/BOOP-V54-WAKE-COMMAND-BOUNDARY-RECEIPT.md`.

## Physical v54 confirmation: command boundary and custom wake passed

Ryan installed v54 on the charged Pixel and repeated the intended two-stage interaction: wake first, then a separate spoken command after the listening cue. The persistent diagnostic captured:

`WAKE ASR RESULT +2748ms ready=18 begin=871 end=2661 partial="change name to Steve" final="change name to Steve"`

This is the physical proof the v54 boundary change was meant to produce. The post-wake recognizer is no longer terminating on the already-spoken wake phrase; it remains alive long enough to capture the separate rename command accurately inside the existing three-second command window. Ryan also reported that the rename flow responded and `Steve` was accepted.

Ryan then completed enough of the custom-name flow to use `Steve` as the wake name and physically confirmed `Steve, lights on` worked. That proves the custom wake name matched on-device and the resulting command continued through the existing local Home Assistant path successfully.

Treat the wake-command audio-boundary defect and the `Steve` custom wake -> local HA command path as physically passed on this Pixel. Do not change Sherpa sensitivity, the three-second window, transcript normalization or the no-pre-roll command policy in response to the older v53 trace. Full custom-name acceptance still requires one final permanent-fallback check for `Hey BOOP` after the rename.

## Previous diagnostic candidate: v53

v52 introduced local-only post-wake ASR callback tracing. v53 made terminal results/errors persistent in a `BOOP wake diagnostic` dialog until Ryan pressed `Close`, which allowed the physical trace above to be captured. v53 built code `3b85e5ae8babe151c3f95c6aa4637d91fc5b2cb9`, workflow `34231784857`, APK SHA-256 `d1d21ff117bf21b761d7f9fb4f78d0499c3ba662c14408c3c17373428b92dbda`. Detailed receipt: `docs/BOOP-V53-PERSISTENT-WAKE-DIAGNOSTIC-RECEIPT.md`.

## Earlier wake-command normalization

v50 physically preserved `Hey BOOP` wake but spoken rename still failed. v51 repaired natural wake-prefix stripping before local command routing. Built v51 commit `4274ed008014d1ed5810af29b64b164bf8477072`, workflow `34225351709`, APK SHA-256 `e768248f27c671d5c4377d68905405d8c5890d7125390e13bb6c5177aad23e5c`. Detailed receipt: `docs/BOOP-V51-WAKE-COMMAND-NORMALIZE-RECEIPT.md`.

## Five-say custom-name contract

- `BOOP` is permanently valid and never requires training;
- a custom name is additive, never a replacement;
- five natural spoken examples are captured from the existing single controller-owned 16 kHz PCM stream;
- active-speech segments become amplitude-normalised pronunciation features and a compact profile;
- raw enrolment PCM is not persisted;
- the learned matcher is additive to Sherpa and must fail safely without disabling BOOP;
- custom names receive all 33 established natural wake forms;
- changing to a new custom name clears the old matching profile;
- unchanged names reuse their matching profile;
- manual Train remains available;
- no second microphone listener and no cloud wake training.

## Required next Pixel test

1. Keep the Pixel on its charger and let BOOP settle back into the established sleeping-wake state with the Android green microphone indicator present.
2. Say `Hey BOOP, lights off` naturally and confirm the permanent fallback still wakes BOOP and routes the local command.
3. If that works, the v54 rename path, custom `Steve` wake path, and permanent `BOOP` fallback are all physically accepted together.
4. If fallback fails, capture the exact behavior before changing sensitivity, grammar, training data or microphone ownership.

## Architecture boundary: clean Shield HOME remains standalone

The clean Nvidia Shield HOME replacement remains a standalone test app on branch `boop-shield-clean-launcher`, package `com.boop.shieldhome`. It is not part of AIO until Ryan explicitly approves a later merge. Unified Shield routing remains on the existing AIO Shield puppet.

## Protected BOOP contracts

Preserve the approved paired black-lidded eye master, iris-only hue, working blink timing/gates, headphones/puppetry and five-digit yellow hands. Preserve physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. Assistant remote invocation/audio remains separately unresolved and must stay on supported Android routes with no competing microphone listener, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs functional/non-visual verification only. Ryan owns visual, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.
