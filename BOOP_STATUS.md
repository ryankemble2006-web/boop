# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current AIO signed candidate: v49 automatic five-say enrolment

Ryan physically confirmed the v48 upstream wake path on the Pixel: on the wireless charger the Android green microphone indicator turned on, stayed on after BOOP went to sleep, and saying `Hey BOOP` woke BOOP. The exact built v48 code is now pinned at branch `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. Do not repoint that checkpoint.

v49 continues from that physically-proven wake-arm path and completes the intended custom-name product flow. Changing the Voice Settings name from BOOP to a custom name such as `Steve` now queues local enrolment automatically. When Voice Settings closes, BOOP prompts `Say Steve five times.` and reuses the existing controller-owned microphone for five spoken examples. An unchanged custom name does not nag on every settings close, BOOP itself never requires training, and a matching existing profile is not retrained automatically. The manual Train action remains available for deliberate retry/retraining.

The underlying training engine remains the existing local implementation: five accepted utterances from the single 16 kHz PCM owner -> active-speech segmentation -> amplitude-normalised pronunciation features -> compact centroid/threshold/duration profile. Raw training PCM is not persisted. The learned matcher remains additive to Sherpa, BOOP remains permanent fallback, and custom names retain all 33 established natural wake forms. No second microphone listener or cloud training was added.

TDD evidence:

- RED commit `72591554376d2cada7df24d99d5934443a986278`, workflow `34220316310`: the new automatic-training policy test failed to compile because `BoopWakeTrainingPolicy` did not yet exist.
- Implementation commit `7ed577d2cb683da0cf7ae5339efc509566123140`: adds the policy and generated settings-to-enrolment flow while preserving the v48 real-attempt arm gate.
- Final signed v49 build commit `87a7abee880b9283d51fb71ed2bf9bd9ed187b28`.

Final v49 receipt:

- Version: 49 / `1.2.3-unified-wake-enrolment`
- Workflow: `34221275050` SUCCESS
- Artifact: `BOOP-Unified`, ID `10053919758`
- APK SHA-256: `2fd65505bea205cce8e7cdc2124cba4a4fa818c5b1fd4ba0efdc5c24f064ffd9`
- Artifact ZIP SHA-256: `fc27c729a848fbda98dee882b626633da46f1fd338a98fa08911cc76106eecb9`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh final-run evidence: non-visual integration contracts and materialization passed; Launcher lint passed; Shield 58/58 and unified 78/78 focused functional tests passed with zero failures/errors/skips; signed assembly, package/version, manifest requirements, permanent signer and APK archive integrity passed. The downloaded artifact ZIP digest and extracted APK hash matched the workflow receipts. No emulator/device launch, screenshots, visual acceptance or v49 acoustic acceptance ran.

## Required Pixel test for v49

Install v49 over the working v48 lineage. Keep BOOP foreground and wirelessly charging/docked.

1. First confirm the green microphone indicator still remains on while BOOP sleeps and `Hey BOOP` still wakes him. This guards the v48 checkpoint behavior.
2. Open Voice Settings and change the name from BOOP to `Steve`, then close/Done.
3. BOOP should automatically prompt `Say Steve five times.` without requiring the separate Train button.
4. Say `Steve` naturally five times with short pauses. Completion should end with `Steve. Got it.` and the normal wake mic should re-arm.
5. Let BOOP sleep and test bare `Steve`, `Hey Steve`, `Oi Steve`, `Morning Steve`, `Steve wake up`, then confirm `Hey BOOP` still works.
6. Record misses and false wakes separately. Do not tune thresholds from a single utterance.

v49 is CI/signer green only until Ryan physically tests this flow. The v48 checkpoint remains the rollback anchor.

## IMPORTANT architecture boundary: clean Shield HOME is standalone

The clean Nvidia Shield HOME replacement is **not part of AIO yet**. It remains a standalone test app to be physically accepted first and merged later.

Standalone launcher branch: `boop-shield-clean-launcher`. Standalone package: `com.boop.shieldhome`. Do not re-route unified Shield HOME to the standalone launcher until Ryan explicitly approves that later merge. The standalone launcher's locked rule remains: **remove the crap, preserve Shield behavior**.

## Protected AIO state

Approved paired black-lidded eyes remain locked in the unified phone/Wall and Shield path. Preserve approved geometry/alpha, iris-only hue, headphones/puppetry and five-digit yellow hands. Blink is user-confirmed working; preserve its existing timing/gates. HA names/Home controls are physically accepted and must stay intact. Room isolation and idempotent Shield density scaling remain protected. Assistant remote invocation/audio remains a separate unresolved physical boundary.

Ryan owns visual/device/acoustic acceptance. No screenshots/golden/aesthetic acceptance, emulator device acceptance, automatic installs/grants or signer/package changes.
