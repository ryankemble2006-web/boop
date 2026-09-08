# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current AIO signed candidate: v48 wake-arm repair

Ryan physically tested v47 and found BOOP was not listening at all: the Android green microphone privacy indicator was absent, and `BOOP`, `Steve` and wake variants did nothing. Treat v47 wake as a physical FAIL upstream of acoustic matching.

Source investigation found that `scripts/patch-unified-wake-arm.py` already contained the intended repair for Android `checkRecognitionSupport()` false negatives, but `scripts/materialize-unified.sh` never invoked it. `BoopWakeSessionState` requires recognition support before entering ARMED, so the old advisory result could keep the coordinator DISARMED and prevent `BoopWakeWordController.arm()` from starting `AudioRecord`. This is sufficient to cause the missing green-mic symptom; v48 still needs the real Pixel confirmation.

TDD red: commit `2639188a7603be5fb61cdb2db8649aad571400a4`, workflow `34217732094`, failed exactly on the missing materializer call: 1 failed / 5 passed.

Minimal repair: `7c0fc86f8cbb3d585e8d35dbf8156f88c67a97ac` invokes `patch-unified-wake-arm.py` immediately after the wake-name patch. No Sherpa model, local training/profile math, mic source, charging policy, HA controls, eyes or blink changed.

Final signed descendant candidate:

- Built code: `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`
- Version: 48 / `1.2.2-unified-wake-arm`
- Workflow: `34218173825` SUCCESS
- Artifact: `BOOP-Unified`, ID `10052711766`
- APK SHA-256: `0264c3e289aab06a7be45067ce44bd72124355b11f9cc0a8ffa73afb7f4c5f02`
- Artifact ZIP SHA-256: `6ebcb029673fc6e9785be04a6db8b1f93e64903e6eb6466e172cfc8edddba0d8`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh final-run evidence: non-visual integration contracts and materialization passed; Launcher lint passed; Shield 58/58 and unified 74/74 focused functional tests passed with zero failures/errors/skips; signed APK assembly, package/version, manifest requirements, permanent signer and archive integrity passed. Downloaded artifact ZIP digest and extracted APK hash matched the workflow receipts. No emulator/device launch, screenshots, visual acceptance or acoustic acceptance ran.

Read `docs/BOOP-V48-WAKE-ARM-RECEIPT.md` for the exact failure/root-cause/build trail.

## Required Pixel check

Use the existing continuous-wake condition: BOOP foreground, Voice Settings closed, microphone permission granted and the phone wirelessly charging/docked.

1. First confirm the Android green microphone indicator returns.
2. Then try plain `BOOP` several times.
3. Only after BOOP wakes, train/test `Steve` and its natural variants.

If the green indicator is still absent, do not tune acoustic thresholds. Investigate wireless-dock detection / `wakeAllowed` / permission. If green returns but BOOP does not trigger, move downstream to Sherpa/template recognition.

## Wake-name contract

BOOP permanently remains the fallback. Custom names are additive and use the same controller-owned 16 kHz microphone path. Five local utterances create a compact pronunciation profile; raw training PCM is not persisted. Changing the custom name clears its old profile. Custom names also receive all 33 established natural wake forms. No competing microphone listener or cloud training.

## IMPORTANT architecture correction: clean Shield HOME is standalone

Ryan clarified that the clean Nvidia Shield HOME replacement is **not part of AIO yet**. It is a standalone test app to be physically accepted first and merged later.

Standalone launcher branch: `boop-shield-clean-launcher`.
Standalone package: `com.boop.shieldhome`.
Standalone green build: `d6e775de68f0f19661736abff6c0432e25320196`, workflow `34217924617`, artifact `BOOP-Shield-Clean-Launcher` ID `10052578743`, APK SHA-256 `374d86419abbc3aca367f79d2a13e36667ab41e8f975dc3f9b1b12f89ca26c68`.

The accidental clean-launcher integration was removed from unified materialisation, manifest and AIO CI. `ShieldEntryRoute` now keeps both Shield HOME and ordinary Shield launches on the existing AIO Shield body (`com.boop.shieldoverlay.MainActivity`). Do not re-route unified Shield HOME to the standalone launcher until Ryan explicitly approves that later merge.

The standalone launcher's locked rule remains: **remove the crap, preserve Shield behavior**. Physical acceptance must preserve double-tap Home -> Recent Apps/task switcher, Back, volume/CEC, Nvidia/Android Settings, system remote shortcuts, app switching and system animations.

## Other protected AIO state

Approved paired black-lidded eyes remain in the unified materialized build path; preserve approved geometry/alpha, iris-only hue, blink timing/gates, headphones/puppetry and five-digit hands. Blink is user-confirmed working. HA names/Home controls were physically accepted earlier and must stay intact. Room isolation and idempotent Shield density scaling remain protected. Assistant remote invocation/audio remains a separate unresolved physical boundary.

Ryan owns visual/device/acoustic acceptance. No screenshots/golden/aesthetic acceptance, emulator device acceptance, automatic installs/grants or signer/package changes. Protected historical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881` until Ryan explicitly promotes a newer physically accepted checkpoint.
