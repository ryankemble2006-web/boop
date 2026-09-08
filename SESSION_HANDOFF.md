# BOOP unified handoff

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Always re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Latest physical result and repair

Ryan physically tested the signed v47 wake-training APK and reported a total listening failure: the Android green microphone privacy indicator that had appeared on earlier listening builds was absent, and `BOOP`, `Steve` and all tested variants did nothing. This is a physical v47 wake FAIL and is upstream of wake-word acoustics.

Systematic source tracing found an integration hole rather than a model-tuning problem. The repository already had `scripts/patch-unified-wake-arm.py`, introduced by commit `82c637c03f2d26632e34a60d5ad8710cb9bf7aac` to bypass false-negative Android `SpeechRecognizer.checkRecognitionSupport()` results and let the real recognizer start/error path decide capability. But `scripts/materialize-unified.sh` never invoked that patch. The materialized v47 app therefore retained the advisory support callback. `BoopWakeSessionState` requires `recognitionSupported` before ARMED; a false result can keep the coordinator DISARMED so `BoopWakeWordController.arm()` never starts its 16 kHz `AudioRecord`, which exactly matches the missing green mic indicator. No device logs were collected, so v48 physical confirmation is still required.

TDD red proof: commit `2639188a7603be5fb61cdb2db8649aad571400a4`, workflow `34217732094`, failed exactly because the unified materializer lacked the wake-arm patch: 1 failed / 5 passed.

Minimal code repair: commit `7c0fc86f8cbb3d585e8d35dbf8156f88c67a97ac` adds one materialization call, `python3 scripts/patch-unified-wake-arm.py`, immediately after `patch-unified-wake-name.py`. No Sherpa model, learned-profile math, mic source, dock/charging policy, HA controls, eye code, blink or assistant code changed in that repair.

## Current signed AIO candidate

Concurrent Shield routing cleanup was preserved. Final signed v48 was built from descendant commit `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

- Version: 48 / `1.2.2-unified-wake-arm`
- Workflow: `34218173825` SUCCESS
- Artifact: `BOOP-Unified`, ID `10052711766`
- APK SHA-256: `0264c3e289aab06a7be45067ce44bd72124355b11f9cc0a8ffa73afb7f4c5f02`
- Artifact ZIP SHA-256: `6ebcb029673fc6e9785be04a6db8b1f93e64903e6eb6466e172cfc8edddba0d8`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh final-run evidence: non-visual integration contracts passed, real-attempt materialization passed, Launcher lint passed, 58 Shield focused tests and 74 unified focused tests passed with zero failures/errors/skips, signed assembly passed, and package/version/manifest/permanent-signer/archive checks passed. The downloaded artifact ZIP digest matched GitHub metadata and the extracted APK matched the workflow APK hash. No emulator/device launch, screenshots, visual acceptance or acoustic acceptance ran.

Detailed receipt: `docs/BOOP-V48-WAKE-ARM-RECEIPT.md`.

## Required next physical test on Pixel

Test in the established continuous-wake condition: BOOP foreground, Voice Settings closed, microphone permission granted and Pixel wirelessly charging/docked.

1. First check whether the Android green microphone indicator returns.
2. If green is present, try plain `BOOP` several times before touching custom training.
3. If BOOP works, train `Steve` with five natural utterances and test the bare name plus established wrappers such as `Hey Steve`, `Oi Steve`, `Morning Steve`, `Steve wake up`, `Listen Steve` and `Excuse me Steve`.
4. Confirm plain `BOOP` still works after custom training.

If green is still absent, stop there. Do not tune acoustic thresholds. The next gate to inspect is wireless-dock detection / `wakeAllowed` / microphone permission. If green returns but BOOP fails, the microphone arm is restored and the next investigation belongs downstream in Sherpa/template matching.

## Custom wake-name contract retained

- `BOOP` permanently remains an accepted wake name and does not require user training.
- Custom name is additive, never a replacement.
- Five local spoken examples are segmented from the existing single controller-owned 16 kHz PCM stream into amplitude-normalised pronunciation features.
- Only the compact profile is persisted; raw training PCM is not stored.
- Changing the name clears the old profile.
- The learned matcher is additive to Sherpa and must fail safely without disabling BOOP.
- Custom names receive the full 33 established natural wake forms.
- Existing post-wake command capture, coordinator reload/re-arm ownership, foreground wireless-charging policy and undocked tap-to-talk policy remain intact.
- No second microphone listener and no cloud wake training.

## IMPORTANT architecture correction: clean Shield HOME is standalone

Ryan clarified that the clean Nvidia Shield HOME replacement is **not part of AIO yet**. It is a standalone test app for physical acceptance first.

Standalone launcher branch: `boop-shield-clean-launcher`.
Standalone package: `com.boop.shieldhome`.
Standalone green build: `d6e775de68f0f19661736abff6c0432e25320196`, workflow `34217924617`, artifact `BOOP-Shield-Clean-Launcher` ID `10052578743`, APK SHA-256 `374d86419abbc3aca367f79d2a13e36667ab41e8f975dc3f9b1b12f89ca26c68`.

The accidental clean-launcher AIO integration was removed from unified materialisation, manifest and AIO CI. Current `ShieldEntryRoute` sends both Shield HOME and ordinary Shield launches to the existing AIO Shield puppet (`com.boop.shieldoverlay.MainActivity`). Do not reintroduce the standalone clean launcher into AIO until Ryan explicitly approves the later merge.

Standalone launcher rule remains **remove the crap, preserve Shield behavior**. Physical acceptance must preserve double-tap Home -> Recent Apps/task switcher, Back, volume/CEC, Nvidia/Android Settings, system remote shortcuts, app switching and system animations. Fix narrow hardware breaks rather than globally intercepting keys or reimplementing Shield OS.

## Other protected BOOP state

The approved paired black-lidded eye master remains locked in the unified phone/Wall and Shield path. Preserve supplied geometry/alpha, iris-only hue, headphones/puppetry and five-digit yellow hands. Blink is user-confirmed working; do not reopen it. HA names/Home controls were physically accepted earlier and must stay intact. Room state teardown/isolation and idempotent Shield density scaling remain protected.

Assistant selection/remote microphone remains a separate unresolved physical boundary. Use supported Android assistant routes only. No overlay microphone, competing recorder, Google-disable/default hacks, Button Mapper, privileged/ADB ownership or OpenAI API dependency.

## Publication and acceptance boundary

Keep package `com.boop.alpha1` and the permanent signer. Keep credentials, private photos, device addresses and raw diagnostics out of the public repository. No automatic installs/grants. GitHub verifies functional/non-visual behavior, build, package/signature/integrity and artifacts; Ryan owns visual, device and acoustic acceptance. Protected historical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881` until Ryan explicitly promotes a newer physically accepted checkpoint.
