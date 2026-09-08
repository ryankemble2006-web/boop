# BOOP v62 single-layer reading eyes receipt

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged.

## Physical finding that triggered v62

Ryan physically tested signed v61 on the Pixel. The listening reading motion itself was liked, but he could visibly see the stationary original pupil/iris behind the moving reading gaze. Source tracing confirmed v61 rendered the normal approved eye first and then drew a shifted iris/pupil patch over it. That produced a double/ghost eye rather than one moving gaze.

v61 is therefore **not visually accepted** and must not be made a physical rollback checkpoint.

## v62 bounded repair

v62 keeps the exact approved BOOP eye master and the v61 listening zoom / reading sweep, but changes compositing only:

- while listening, render into a temporary layer;
- clear the stationary iris aperture from that layer;
- draw one shifted iris/pupil patch from the same approved runtime bitmap into the aperture;
- restore the layer so only one visible reading gaze remains;
- preserve the widened iris hue coverage from v61;
- do not change wake detection, command recognition, microphone ownership, the 100 ms command bridge, HA routing, TTS, blink, package or signer.

No replacement/generated listening-eye artwork is used.

## TDD evidence

RED:

- commit `7b19f2c81f0fa2a6cb7a3512186298b3bdf4b5e4`
- workflow `34265189069`
- 97 focused unified tests ran; exactly the new anti-ghost compositing regression failed while established tests passed.

Repair commits:

- policy `b54e6fad8dc30e4f90ff13a126038053d8d73881`
- renderer `d7e4632ab014b026459fd63d8d9d17a8fd9dc16f`
- release metadata `55753ff70428a35b7b3f6d9da668b01e358fcb62`
- final verifier/built commit `6877bf3d97d069eda950938060e355da039d53cf`

## Final signed v62

- version 62 / `1.2.16-unified-single-layer-reading-eyes`
- built commit `6877bf3d97d069eda950938060e355da039d53cf`
- workflow `34265615662` SUCCESS
- artifact `BOOP-Unified`, ID `10071797863`
- artifact ZIP digest `sha256:adb58b5eb0373fa1b581dccd625638a6bcaf151477215b57c930197ed52142ef`
- APK SHA-256 `5def47113929e6b0aa59b868e5880056607473fb3ba1ff4e54ac3f771bb7bc3b`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- wake-handoff contracts 3/3 PASS
- Shield focused tests 58/58, zero failures/errors/skips
- unified focused tests 97/97, zero failures/errors/skips
- Launcher lint, signed assembly, package/version, manifest, permanent signer, APK ZIP integrity and artifact upload PASS.

CI/signer green only. Visual/device acceptance remains Ryan's physical Pixel test.

## Required physical acceptance

Install v62 over v61 without uninstalling. Trigger active listening by wake-name command and by tap-to-talk. Confirm there is only one visible moving iris/pupil per eye with no stationary pupil ghost behind it, the reading sweep still looks right, hue changes cover the previously missed iris blue, and natural BOOP/custom wake commands still work. Do not create a v62 checkpoint until Ryan physically accepts this exact signed build.
