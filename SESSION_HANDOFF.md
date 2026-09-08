# BOOP unified handoff

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Always re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v56 seamless wake-command seam

Ryan physically tested signed v55 on the charged Pixel and isolated the remaining wake-command defect:

- `Steve` wakes BOOP and `BOOP` wakes BOOP.
- tap-to-talk works.
- `Steve`, pause, `lights off` works and says `Done`.
- `BOOP`, pause, `lights off` works and says `Done`.
- one-breath `Steve lights on`, `Steve show diagnostics`, and one-breath BOOP/Steve `lights off` produced no command action.
- every wake played the artificial wake-accepted speaker bing.

This physically proves v55 wake detection, both accepted wake names, tap speech recognition, local HA routing and reply TTS are alive. The failure is the wake-to-command acoustic seam, not Home Assistant or wake sensitivity.

Source tracing found two seam defects:

1. `BoopWakeWordController` reads 16 kHz audio in 1,600-sample / 100 ms blocks. It puts the current detector block into the ring before wake detection fires. v54's zero-prelude policy correctly removed the old one-second wake recording but also discarded this final 100 ms block, so the start of an immediate command can be chopped off.
2. `MainActivity.onWakeDetected` played a 90 ms speaker tone before starting Android command ASR while the live mic path was already carrying the user's command.

v56 changes only that seam:

- `BoopWakeCommandAudioPolicy` now keeps exactly the final 1,600 detector samples as a 100 ms command bridge; the old one-second pre-roll remains forbidden.
- the materialized wake callback no longer calls `playWakeAcceptedCue()`; command handoff is silent.
- wake names, Sherpa/custom template matching, five-say training, three-second command window, powered-wake resilience, silent failure re-arm, pull-only diagnostics, HA, visuals, Launcher and Shield behavior are otherwise unchanged.

TDD trail:

- RED workflow `34245905089` at `4a92e4cf1bf84e50f2b37cc8fae6bf590d1375bb`: 6 Python contracts passed and the new silent-seam contract failed specifically because `playWakeAcceptedCue();` was still in the wake callback.
- 100 ms bridge policy `cf70174e86576276b8e2f2d172c7864368ed4d79`.
- silent materialized seam `87160ac64e61cb6a7d3837ff0e0cc8c627eeaa00`.
- final built code `68bdbb4aabfbefd383a48aa4764568c5cb2222dc`.

Final v56 receipt:

- version 56 / `1.2.10-unified-seamless-wake-command`
- workflow `34246347404` SUCCESS
- artifact `BOOP-Unified`, ID `10064218458`
- artifact digest `sha256:079864cd1ecfa82f8f9618b60738523d84e0f5b1e18ecda43bf7638cc3c230f6`
- APK SHA-256 `5212b2faf4286db17b3afa44d8174d5773d555d9a8779c5d7fb1e7e6aba6a13c`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused tests 58/58 and unified focused tests 90/90, zero failures/errors/skips
- materialized seamless-handoff contract PASS
- Launcher lint, signed assembly, package/version, manifest, signer and APK archive integrity PASS

CI/signer green only. v56 physical Pixel acceptance is pending. Detailed receipt: `docs/BOOP-V56-SEAMLESS-WAKE-COMMAND-RECEIPT.md`.

## Required next Pixel test

Install v56 over v55 without uninstalling. Keep the Pixel on external power and let BOOP settle into wake listening.

1. Say `Steve lights on` naturally in one breath with no deliberate pause. There should be no artificial wake bing.
2. Say `BOOP lights off` naturally in one breath.
3. Say `Steve show diagnostics` naturally in one breath.
4. Wake once and deliberately give no command; confirm it fails silently/re-arms, then immediately issue another wake + command.

If one-breath speech still fails, do not widen the bridge or restore the old one-second pre-roll blindly. Use `Steve`, pause, `show diagnostics` if needed to capture the retained trace and inspect that evidence first.

## Physically proven rollback checkpoint

The exact built v48 wake-arm code remains permanently pinned at branch `checkpoint-boop-unified-v48-wake-arm`, commit `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. On Ryan's Pixel: charger -> Android green mic indicator ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP. Do not repoint it.

v48 receipt: version 48 / `1.2.2-unified-wake-arm`, workflow `34218173825`, APK SHA-256 `0264c3e289aab06a7be45067ce44bd72124355b11f9cc0a8ffa73afb7f4c5f02`.

## Durable wake/name contracts

- `BOOP` permanently remains an accepted wake name.
- a custom name such as `Steve` is additive, never a replacement.
- five custom-name samples are local and use the existing single controller-owned 16 kHz PCM stream; raw enrolment PCM is not persisted.
- one microphone owner only; no competing recorder.
- v54 rule remains: never feed the full wake-history ring into Android command ASR. v56's only permitted bridge is the final 100 ms detector block.
- external power permits continuous wake; unplugged phone remains tap-to-talk.
- after normal TTS, wake re-arms after speech ends. Post-wake no-match/timeout failures are silent and genuinely re-arm. Hard wake-engine start failures remain fail-safe latched.
- `show diagnostics` is pull-only; normal failures do not throw a modal or speak an error.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on branch `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eyes, iris-only hue, blink timing/gates, headphones/puppetry and five-digit yellow hands. Preserve physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. Assistant remote invocation/audio remains separately unresolved. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
