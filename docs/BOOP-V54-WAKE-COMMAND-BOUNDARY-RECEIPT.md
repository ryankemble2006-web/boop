# BOOP v54 wake command boundary receipt

Date: 2026-09-08

Canonical branch: `boop-unified`
Package: `com.boop.alpha1`
Version: 54 / `1.2.8-unified-wake-command-boundary`
Permanent signer unchanged.

## Physical evidence from v53

Ryan physically tested v53 on the charged Pixel and captured the persistent diagnostic after saying `Hey BOOP` once:

`WAKE ASR RESULT +583ms ready=18 begin=153 end=544 partial="hey pooop" final="hey pooop"`

This is a successful recognizer result, not an Android recognizer error. It proves the post-wake recognizer was receiving the wake utterance itself and completing roughly 0.58 seconds after start, before a separate command could be spoken.

## Root cause

`BoopWakeWordController` retained one second of wake-detection PCM (`PRE_ROLL_SAMPLES = 16000`) and, after Sherpa/template wake detection, wrote that entire historical buffer into the Android command-recognizer pipe before continuing with live post-detection PCM. Android therefore began command recognition with the already-complete `Hey BOOP` utterance and could close speech before Ryan's command arrived.

The fix establishes a strict boundary: wake-detection history is not command audio. The command recognizer receives no pre-wake PCM and continues from the existing live post-detection PCM stream. The single controller-owned 16 kHz `AudioRecord`, the three-second command-capture window, wake detector, sensitivity, transcript normalization, rename parser, five-say enrolment and charging policy are unchanged.

## TDD trail

RED commit: `d68fca32f25b9de360404170ff46576baabb33d5`
RED workflow: `34233607842`

The new `BoopWakeCommandAudioPolicyTest` required command-recognizer prelude audio to exclude wake-detection history. The selected unified wake test stage failed because `BoopWakeCommandAudioPolicy` did not yet exist, while earlier integration/Launcher/Shield stages passed.

GREEN implementation:

- policy commit `edb2750e1140e5f8d4e39ec2e65b9097d71cef01`
- controller commit `f90031830dfcf7f46c5a3644502a5bdf278acc06`
- GREEN workflow `34233966994` SUCCESS

## Final signed v54 receipt

Built code: `fe26f29cb330b450e5e9894a4588b19ae9d7152a`
Workflow: `34234618256` SUCCESS
Artifact: `BOOP-Unified`, ID `10059395955`
APK SHA-256: `ee6a5ade5538cf3b0b4aa1346cdeb5b957c9ed56ff214279c2d7c46e428fd287`
Artifact ZIP SHA-256: `0f7bb5168a961aa1e44bf455c070a3db8e112c8a55f9360db2236fc1dc4d6908`
Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Final workflow evidence:

- non-visual integration/materialization: passed;
- Launcher lint: passed;
- Shield focused functional tests: 58 tests, 0 failures, 0 errors, 0 skipped;
- Unified focused functional tests: 85 tests, 0 failures, 0 errors, 0 skipped;
- signed assembly: passed;
- package/version/manifest checks: passed;
- permanent signer verification: passed;
- APK archive integrity: passed;
- artifact upload: passed.

No emulator/device launch, screenshot judging, visual acceptance or acoustic acceptance was performed by GitHub. v54 is CI/signer green, not physically accepted.

## Required next Pixel test

1. Install v54 over the existing BOOP install. Do not uninstall first.
2. Keep the Pixel on its charger and let BOOP settle into the established sleeping-wake state.
3. Confirm the Android green microphone indicator is present.
4. Say `Hey BOOP` once.
5. Wait for BOOP's listening cue, then say exactly `change name to Steve`.
6. If `Say Steve five times.` appears, continue with five natural `Steve` examples, then confirm `Steve` can wake BOOP and `Hey BOOP` still works as the permanent fallback.
7. If the persistent `BOOP wake diagnostic` reports an error, no speech or an unexpected transcript instead, capture it and stop there before further grammar changes.

Protected physical rollback remains v48 at `checkpoint-boop-unified-v48-wake-arm` / `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.
