# BOOP unified memory

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Wake architecture and v48 physical repair

BOOP permanently remains an accepted wake name. A custom name is additive, never a replacement. Foreground wireless charging/docking permits continuous phone wake; undocked phone remains tap-to-talk. Preserve coordinator -> `BoopWakeWordController` -> Sherpa/template -> single 16 kHz `AudioRecord` ownership and coordinated reload/re-arm. Never add a competing microphone listener.

Custom-name training uses five local spoken examples from that same controller-owned PCM stream. It stores only a compact amplitude-normalised pronunciation profile; raw training PCM is not persisted. Changing the selected custom name clears its profile. The learned matcher is additive to Sherpa and must fail safely without disabling BOOP. Custom names receive all 33 established natural wake forms.

Ryan physically tested v47 (`1.2.1-unified-wake-training`) and reported a total listening failure: no Android green microphone privacy indicator, and no response to `BOOP`, `Steve` or variants. This is physical evidence that v47 was not even armed, not evidence that wake acoustics needed threshold tuning.

Source root cause: `scripts/patch-unified-wake-arm.py` already contained the intended bypass for false-negative Android `SpeechRecognizer.checkRecognitionSupport()` results, but unified materialization never called it. Since `BoopWakeSessionState` requires recognition support before ARMED, the stale advisory callback could prevent `BoopWakeWordController.arm()` and therefore prevent `AudioRecord` from starting. The missing call was proven by a TDD red integration test: commit `2639188a7603be5fb61cdb2db8649aad571400a4`, workflow `34217732094`, 1 failed / 5 passed exactly on the absent wake-arm materializer entry.

Minimal repair `7c0fc86f8cbb3d585e8d35dbf8156f88c67a97ac` adds `python3 scripts/patch-unified-wake-arm.py` immediately after the wake-name patch. No Sherpa model, training/profile math, microphone source, charging policy, HA, eyes or blink changed.

Signed v48 candidate after preserving concurrent Shield routing cleanup:

- Built code `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`
- Version 48 / `1.2.2-unified-wake-arm`
- Workflow `34218173825` SUCCESS
- Artifact `BOOP-Unified`, ID `10052711766`
- APK SHA-256 `0264c3e289aab06a7be45067ce44bd72124355b11f9cc0a8ffa73afb7f4c5f02`
- Artifact ZIP SHA-256 `6ebcb029673fc6e9785be04a6db8b1f93e64903e6eb6466e172cfc8edddba0d8`
- Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh v48 CI evidence: non-visual integration and materialization contracts passed; Launcher lint passed; 58 Shield focused tests and 74 unified focused tests passed with zero failures/errors/skips; signed assembly, package/version, manifest, signer and archive integrity passed. Downloaded ZIP/APK hashes matched workflow receipts.

### Physical v48 wake proof

Ryan physically tested v48 in the established continuous-wake condition on the Pixel. After placing BOOP on the wireless charger, the Android green microphone privacy indicator turned on. BOOP then went to sleep while the green mic indicator stayed on. Saying `Hey BOOP` woke BOOP from sleep.

Treat this as **physical confirmation that the upstream wake-arm/listening repair works on real hardware** and as a successful physical sample for the established `Hey BOOP` wake phrase while sleeping on the charger. The earlier v47 “not listening at all” defect is closed at this upstream layer.

Do not generalize this into full acoustic acceptance. Bare `BOOP`, custom `Steve`, five-sample enrolment, the rest of the 33 natural variants, miss rate and false-wake rate remain physically untested in v48. When future misses occur with the green mic still active, investigate/tune Sherpa/template matching downstream. If the green mic disappears again, return upstream to dock/wakeAllowed/permission.

Detailed receipt: `docs/BOOP-V48-WAKE-ARM-RECEIPT.md`.

## Clean Shield HOME architecture boundary

Ryan clarified that the clean Nvidia Shield HOME replacement is **standalone for testing and is not part of AIO yet**.

Standalone branch `boop-shield-clean-launcher`; package `com.boop.shieldhome`. Standalone green build `d6e775de68f0f19661736abff6c0432e25320196`, workflow `34217924617`, artifact `BOOP-Shield-Clean-Launcher` ID `10052578743`, APK SHA-256 `374d86419abbc3aca367f79d2a13e36667ab41e8f975dc3f9b1b12f89ca26c68`.

The accidental standalone-launcher integration was removed from unified materialization, manifest and AIO CI. Current `ShieldEntryRoute` keeps both Shield HOME and ordinary Shield launches on existing AIO `com.boop.shieldoverlay.MainActivity`. Do not reintroduce `com.boop.shieldhome.ShieldLauncherActivity` into unified until Ryan explicitly approves the later merge.

Standalone launcher rule is locked: **remove the crap, preserve Shield behavior**. Physical acceptance must preserve double-tap Home -> Recent Apps/task switcher, Back, volume/CEC, Nvidia/Android Settings, system remote shortcuts, app switching and normal system animations. Fix narrow hardware breaks; do not globally intercept remote keys, disable the stock launcher or reimplement Shield OS behavior.

## Permanent visual contracts

The approved paired black-lidded eye master is locked in the unified phone/Wall and Shield path. Preserve approved geometry, supplied alpha and proportions. User hue affects only the iris; do not tint sclera, pupil, highlights or eyelids. Preserve headphones/puppetry and five-digit yellow hands.

Blink is user-confirmed working. Preserve its existing 183 ms curve, 3-7 second delay and motion/power/lifecycle gates. No global animation-setting changes.

## Home Assistant and lifecycle contracts

HA device names and Home control buttons are physically accepted and must not regress. Home remains room-scoped to confirmed physical controllable devices, read-only/fail-closed, with no helpers/diagnostics/config plumbing or whole-house fallback on uncertainty.

Room changes tear down previous-room navigation/dashboard/socket/controller state before storing/rebuilding. Shield density scaling remains idempotent from a stable baseline and must never become cumulative or system-wide.

## Assistant boundary

Assistant ownership remains explicit/reversible through supported Android routes. Remote-button/default selection and actual audio from THAT Shield remote are still physically unresolved. No overlay microphone, competing recorder, Google-disable/default/permission hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency. Success requires real invocation plus remote audio, routing/response, clean cancel/repeat and return behavior.

## Testing and release discipline

GitHub performs focused non-visual tests, compilation/lint, package/signature/integrity/security checks and artifact upload. Ryan owns screenshots, appearance, animation, device and acoustic acceptance. No golden screenshots, aesthetic source-string acceptance or emulator install/launch acceptance.

Keep package `com.boop.alpha1` and permanent signer. Keep private photographs, credentials, device addresses and raw diagnostics out of the public repository. No automatic installs/grants or false Windows-sync claims. Protected historical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881` until Ryan explicitly promotes a newer physically accepted checkpoint.
