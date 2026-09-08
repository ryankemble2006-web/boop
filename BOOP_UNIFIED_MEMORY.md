# BOOP unified memory

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Durable wake architecture

BOOP permanently remains an accepted wake name. A custom name such as `Steve` is additive, never a replacement. Custom-name training uses five local spoken examples from the existing single controller-owned 16 kHz PCM stream and stores only a compact pronunciation profile; raw enrolment PCM is not persisted. Never add a competing microphone listener.

Continuous phone wake is allowed while the phone is on **any external power** (wireless/magnetic dock, USB or AC). Unpowered/undocked handheld behavior remains tap-to-talk. The wake engine is the resting state while powered, except while tap recognition, settings, command processing or TTS legitimately owns the microphone/state.

After normal TTS, wake re-arms only after BOOP finishes speaking so BOOP does not hear its own reply. A post-wake Android command-ASR no-match or speech timeout is silent and performs a genuine controller/coordinator re-arm; no spoken error and no automatic modal. A hard wake-engine/microphone startup failure remains fail-safe latched instead of tight-loop retrying.

`show diagnostics` is the hidden pull-only diagnostic command. Normal wake/ASR failures retain useful trace state but do not display it automatically and do not save raw audio or cloud-log diagnostics.

## Durable wake-to-command audio rule

v53 physically proved that feeding the full wake-detection history into Android command ASR is wrong: Android heard the already-spoken wake phrase and completed before a separate command. v54 therefore removed the old full one-second pre-roll.

v55 physical testing exposed the opposite edge of that boundary for natural one-breath commands. The wake controller reads `READ_SAMPLES = 1600` samples at 16 kHz, i.e. 100 ms blocks, and writes the current block to the ring before wake detection decides it fired. Discarding **all** wake history therefore also discards the detector's terminal 100 ms block, which can contain the beginning of an immediately-following command such as `Steve lights on`.

The durable v56 rule is therefore:

- never restore the old one-second wake-history pre-roll;
- preserve exactly the **final 1,600 samples / 100 ms detector block** as the command-ASR bridge;
- continue the same live PCM stream immediately afterward;
- keep natural wake-prefix transcript stripping as a defensive parser layer;
- keep the wake-to-command handoff acoustically silent: do not play the artificial wake-accepted speaker cue while command audio is being captured.

This is a bounded seam bridge, not permission to expand command pre-roll without new physical evidence.

## Current physical evidence: v55

On Ryan's charged Pixel with v55:

- `Steve` wakes BOOP.
- `BOOP` wakes BOOP.
- tap-to-talk works.
- `Steve`, pause, `lights off` works and BOOP says `Done`.
- `BOOP`, pause, `lights off` works and BOOP says `Done`.
- one-breath `Steve lights on`, `Steve show diagnostics`, and one-breath BOOP/Steve `lights off` do not route the command.
- each wake plays the artificial wake-accepted bing.

This physically narrows the problem to the wake-to-command seam. Do not respond to this evidence by changing wake sensitivity, Home Assistant routing, the Steve profile, the three-second command window or microphone ownership.

v55 otherwise introduced and retains the any-external-power wake policy, silent no-match/timeout recovery and pull-only diagnostics.

## Current signed candidate: v56

v56 makes only the seam repair described above.

TDD evidence:

- RED workflow `34245905089` at `4a92e4cf1bf84e50f2b37cc8fae6bf590d1375bb` failed specifically because the old materialized wake callback still called `playWakeAcceptedCue();`; six other selected Python contracts passed.
- final 100 ms bridge policy commit `cf70174e86576276b8e2f2d172c7864368ed4d79`.
- silent materialized wake seam commit `87160ac64e61cb6a7d3837ff0e0cc8c627eeaa00`.
- final built code `68bdbb4aabfbefd383a48aa4764568c5cb2222dc`.

v56 receipt:

- version 56 / `1.2.10-unified-seamless-wake-command`
- workflow `34246347404` SUCCESS
- artifact `BOOP-Unified`, ID `10064218458`
- artifact digest `sha256:079864cd1ecfa82f8f9618b60738523d84e0f5b1e18ecda43bf7638cc3c230f6`
- APK SHA-256 `5212b2faf4286db17b3afa44d8174d5773d555d9a8779c5d7fb1e7e6aba6a13c`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused tests 58/58; unified focused tests 90/90; zero failures/errors/skips
- materialized seamless-handoff contract PASS
- Launcher lint, signed assembly, package/version, manifest, signer and APK integrity PASS

CI/signer green; physical v56 acceptance pending. Detailed receipt: `docs/BOOP-V56-SEAMLESS-WAKE-COMMAND-RECEIPT.md`.

Required physical test: install v56 over v55, keep the Pixel powered, and say `Steve lights on`, `BOOP lights off`, and `Steve show diagnostics` naturally with no deliberate pause. There should be no artificial wake bing. Then deliberately wake without a command once and confirm silent re-arm by issuing another wake + command. If one-breath speech still fails, use `Steve`, pause, `show diagnostics` if necessary and inspect the trace before changing the bridge.

## Physically proven rollback

The exact built v48 wake-arm code remains the protected wake rollback at branch `checkpoint-boop-unified-v48-wake-arm`, commit `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. Never repoint it. On Ryan's Pixel: charger -> Android green mic ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP.

v48 receipt: version 48 / `1.2.2-unified-wake-arm`, workflow `34218173825`, APK SHA-256 `0264c3e289aab06a7be45067ce44bd72124355b11f9cc0a8ffa73afb7f4c5f02`, permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Historical wake landmarks retained

v51 repaired natural wake-prefix stripping before local command routing. v53 introduced persistent physical ASR evidence. v54 repaired the full-pre-roll defect and physically allowed a separate post-wake rename command; Ryan then trained/used `Steve`. Those historical receipts remain in `docs/` and Git history. They are context, not permission to reintroduce their superseded diagnostic UI or audio behavior.

## Clean Shield HOME boundary

The clean Nvidia Shield HOME replacement remains standalone for testing on branch `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Current unified Shield routing stays on the existing AIO Shield body. Do not merge the standalone launcher into unified until Ryan explicitly approves later.

## Permanent visual, Home and assistant contracts

Preserve the approved paired black-lidded eye master, approved geometry/alpha, iris-only hue, headphones/puppetry and five-digit yellow hands. Blink is user-confirmed working; preserve its current timing/curve/delay and motion/power/lifecycle gates.

HA device names and Home controls are physically accepted and must not regress. Home remains room-scoped and fail-closed; room changes tear down previous-room ownership/state before rebuilding. Shield density scaling remains idempotent and never system-wide.

Assistant ownership remains explicit/reversible through supported Android routes. Shield remote assistant activation/audio remains separately unresolved. No overlay mic, competing recorder, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs focused non-visual tests, compilation/lint, package/signature/integrity checks and artifact upload. Ryan owns screenshots, appearance, animation, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.
