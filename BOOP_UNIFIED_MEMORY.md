# BOOP unified memory

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Durable wake architecture

BOOP permanently remains an accepted wake name. A custom name is additive, never a replacement. Custom-name training uses five local spoken examples from the existing single controller-owned 16 kHz PCM stream and stores only a compact pronunciation profile; raw enrolment PCM is not persisted. Never add a competing microphone listener.

Continuous phone wake is allowed while the phone is on any external power: wireless/magnetic dock, USB or AC. Unpowered handheld behavior remains tap-to-talk. After normal TTS, wake re-arms only after BOOP finishes speaking. A post-wake Android command-ASR no-match or speech timeout is silent and performs a genuine controller/coordinator re-arm. A hard wake-engine/microphone startup failure remains fail-safe latched rather than tight-loop retrying.

`show diagnostics` is the hidden pull-only diagnostic command. Normal failures retain useful trace state but do not automatically display a modal, speak an error, persist raw audio or cloud-log diagnostics.

## Durable wake-to-command boundary

v53 physically proved that feeding the full wake-detection history into Android command ASR is wrong because Android transcribed the wake phrase itself. v54 removed that old full one-second pre-roll. Preserve exactly the final 1,600 samples / 100 ms detector block as the command-ASR bridge, continue the same live PCM stream immediately afterward, keep wake-prefix stripping as a defensive parser layer, and keep wake-to-command handoff acoustically silent.

v57 made learned custom names matchable while speech is still active rather than waiting for trailing silence. Physical evidence includes natural `Steve lights on`, `Steve show diagnostics`, five-sample spoken rename to `Fred` followed by `Fred lights on`, and the same flow with `Jeff`.

v58 set default/permanent BOOP Sherpa trailing blanks to zero. Ryan physically accepted natural `BOOP + command` spoken slowly or quickly without a deliberate pause. Do not change wake score, threshold, phrase assets, sensitivity or the 100 ms bridge in response to this accepted behavior.

Exact v58 rollback:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Never repoint it.

## Durable uncensored-speech rule from v59

Both ordinary tap-to-talk and post-wake command recognition request `RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS=false`. BOOP must not add its own profanity blacklist or replace recognized adult/profane speech with asterisks.

Ryan physically accepted this on the Pixel: spoken adult/profane rename survives as the actual word, BOOP's five-sample prompt says the actual word instead of stars, and local training completes.

Protected v59 rollback:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

Never repoint it. v59 workflow `34257117357`, APK SHA-256 `7d48cc77407b69428bd2456b80cefbe56cb60f6f8326eb2e3686aa7b22bc7a2e`.

## Durable listening-eye rules from v60-v62

The listening state must reuse the exact approved black-lidded BOOP eye master already in the app. Do not regenerate or substitute listening-pose artwork. Listening feedback is active only while tap-to-talk ASR or post-wake command ASR is actually listening; ordinary powered/wake-armed waiting is not active listening.

v60 introduced a runtime listening cue using the approved bitmap. Ryan physically reported it was too subtle and also identified blue iris regions that the hue control did not affect.

v61 increased listening visibility with the same approved art: about 1.16x zoom, slightly downward reading gaze, smooth left/right scan with 720 ms half-sweep, and wider iris hue coverage. Ryan physically liked the motion but rejected the compositing because the stationary base pupil/iris remained visible under the shifted reading patch. v61 is not a physical checkpoint.

Durable compositing rule from that physical evidence: **a moving listening gaze must replace the stationary iris/pupil aperture, never stack a second visible eye layer over it.** The approved PNG bytes remain unchanged.

v62 implements that rule. During active listening the face is composited on a temporary layer, the stationary iris aperture is cleared, and one shifted iris/pupil patch from the same approved runtime bitmap is drawn into the aperture. This preserves the v61 reading motion and widened iris hue coverage while eliminating the known PNG-on-PNG ghost mechanism.

v62 TDD/release trail:

- RED commit `7b19f2c81f0fa2a6cb7a3512186298b3bdf4b5e4`, workflow `34265189069`: 97 focused unified tests ran; exactly the new anti-ghost compositing regression failed.
- compositing policy `b54e6fad8dc30e4f90ff13a126038053d8d73881`.
- renderer `d7e4632ab014b026459fd63d8d9d17a8fd9dc16f`.
- version bump `55753ff70428a35b7b3f6d9da668b01e358fcb62`.
- final built code `6877bf3d97d069eda950938060e355da039d53cf`.
- version 62 / `1.2.16-unified-single-layer-reading-eyes`.
- workflow `34265615662` SUCCESS.
- artifact ID `10071797863`, ZIP digest `sha256:adb58b5eb0373fa1b581dccd625638a6bcaf151477215b57c930197ed52142ef`.
- APK SHA-256 `5def47113929e6b0aa59b868e5880056607473fb3ba1ff4e54ac3f771bb7bc3b`.
- signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- wake-handoff contracts 3/3 PASS; Shield 58/58; unified 97/97; zero failures/errors/skips; package/version/manifest/signer/APK integrity/upload PASS.

v62 is CI/signer green, not yet physically accepted. Do not create a v62 rollback checkpoint until Ryan confirms on the real Pixel that only one moving pupil/iris is visible and the hue fix looks correct.

Detailed receipt: `docs/BOOP-V62-SINGLE-LAYER-READING-EYES-RECEIPT.md`.

## Historical wake landmarks retained

Older exact wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. Never repoint it.

Wake progression: v51 natural prefix stripping; v53 persistent ASR evidence; v54 full-pre-roll repair; v55 any-external-power wake, silent real re-arm and pull-only diagnostics; v56 silent wake seam + exact 100 ms bridge; v57 streaming learned names; v58 default BOOP zero trailing blank; v59 uncensored recognizer request; v60 listening state; v61 reading motion + hue expansion but ghosted; v62 single-layer reading compositing.

## Clean Shield HOME boundary

The clean Nvidia Shield HOME replacement remains standalone for testing on `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Current unified Shield routing stays on the existing AIO Shield body. Do not merge the standalone launcher into unified until Ryan explicitly approves later.

## Permanent visual, Home and assistant contracts

Preserve the approved paired black-lidded eye master, approved geometry/alpha, iris-only hue, headphones/puppetry and five-digit yellow hands. Blink is user-confirmed working; preserve timing/curve/delay and motion/power/lifecycle gates.

HA device names and Home controls are physically accepted and must not regress. Home remains room-scoped and fail-closed; room changes tear down previous-room ownership/state before rebuilding. Shield density scaling remains idempotent and never system-wide.

Assistant ownership remains explicit/reversible through supported Android routes. Shield remote assistant activation/audio remains separately unresolved. No overlay mic, competing recorder, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs focused non-visual tests, compilation/lint, package/signature/integrity checks and artifact upload. Ryan owns screenshots, appearance, animation, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.
