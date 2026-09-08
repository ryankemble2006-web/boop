# BOOP v48 wake-arm repair receipt

Verified 2026-09-08. This records source/build evidence plus Ryan's v47 physical failure. It does not claim v48 acoustic acceptance before a real Pixel retest.

## Physical failure that triggered this pass

Ryan installed v47 (`1.2.1-unified-wake-training`) and reported that BOOP was not listening at all: the Android green microphone privacy indicator that had been present on earlier listening builds did not appear, and neither `BOOP`, `Steve`, nor any natural wake variant triggered.

That symptom is upstream of wake-word matching. If `AudioRecord` never arms, both the permanent Sherpa BOOP route and the learned custom-name route are silent.

## Root cause found in the materialized build path

The repository already contained `scripts/patch-unified-wake-arm.py`, added by commit `82c637c03f2d26632e34a60d5ad8710cb9bf7aac` after Android `SpeechRecognizer.checkRecognitionSupport()` produced false-negative capability results. The patch deliberately treats that support API as advisory and lets the existing real recognizer start/error path decide capability, allowing Sherpa to arm whenever Android >= 33 and a recognizer is actually present.

However, `scripts/materialize-unified.sh` never invoked that patch. v47 therefore still materialized the old advisory support callback. `BoopWakeSessionState` requires `recognitionSupported == true` before entering `ARMED`; a false advisory result can keep the coordinator DISARMED, so `BoopWakeWordController.arm()` never starts its 16 kHz `AudioRecord`. This source-level omission is fully consistent with Ryan's missing green mic indicator. Device logs were not collected, so physical confirmation remains the v48 test.

TDD reproduction: commit `2639188a7603be5fb61cdb2db8649aad571400a4` added a non-visual materialization contract requiring the real-attempt patch after the wake-name patch. Workflow `34217732094` failed exactly there: 1 failed, 5 passed, because `patch-unified-wake-arm.py` was absent from the unified materializer.

Minimal repair: commit `7c0fc86f8cbb3d585e8d35dbf8156f88c67a97ac` adds only:

`python3 scripts/patch-unified-wake-arm.py`

after `patch-unified-wake-name.py`. No Sherpa model, acoustic training/profile code, microphone source, dock policy, HA controls, eyes, blink or Shield presentation was changed by the repair.

## Final v48 signed build

Concurrent Shield routing cleanup was preserved. The final signed candidate was built from descendant commit `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

- Version: 48 / `1.2.2-unified-wake-arm`
- Workflow: `34218173825` SUCCESS
- Artifact: `BOOP-Unified`, ID `10052711766`
- Artifact ZIP SHA-256: `6ebcb029673fc6e9785be04a6db8b1f93e64903e6eb6466e172cfc8edddba0d8`
- APK SHA-256: `0264c3e289aab06a7be45067ce44bd72124355b11f9cc0a8ffa73afb7f4c5f02`
- Package: `com.boop.alpha1`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh final-run evidence: non-visual integration contracts passed, materialization passed with the real-attempt wake gate, Launcher lint passed, 58 Shield focused functional tests passed and 74 unified focused functional tests passed with zero failures/errors/skips. Signed APK assembly, package/version, manifest requirements, permanent signer and APK ZIP integrity passed. The downloaded artifact ZIP digest matched GitHub metadata; the extracted APK hash matched the workflow receipt.

The standalone clean Shield HOME is now excluded from the all-in-one APK path by concurrent Shield routing work; its separate tests/workflow own that lineage. This v48 receipt does not claim standalone Shield HOME physical acceptance.

## Required physical v48 check

On the Pixel, test in the existing continuous-wake condition: BOOP foreground, Voice Settings closed, microphone permission granted, and the phone wirelessly charging/docked. The first check is not `Steve`: confirm the Android green microphone indicator returns. Then try plain `BOOP` several times. Only after that should the custom-name five-sample training and `Steve` variants be judged.

If the green indicator is still absent under those conditions, do not tune acoustic thresholds. The next investigation is the earlier gate, especially wireless-dock detection / wakeAllowed / microphone permission. If the green indicator returns but BOOP does not trigger, the fault has moved downstream and Sherpa/template recognition can be debugged separately.
