# BOOP Unified v56 seamless wake-command receipt

Date: 2026-09-08

Canonical branch: `boop-unified`
Package: `com.boop.alpha1`
Permanent signer unchanged.

## Physical evidence that triggered v56

Ryan physically tested signed v55 on the charged Pixel and reported:

- `Steve` wakes BOOP.
- `BOOP` wakes BOOP.
- tap-to-talk works.
- `Steve`, pause, `lights off` works and BOOP says `Done`.
- `BOOP`, pause, `lights off` works and BOOP says `Done`.
- one-breath `Steve lights on` produced no command action.
- one-breath `Steve show diagnostics` produced no command action.
- one-breath `BOOP lights off` / `Steve lights off` produced no command action.
- every wake played the artificial wake-accepted speaker bing.

This isolates the defect to the wake-to-command acoustic seam. Wake detection, both accepted wake names, tap speech recognition, local Home Assistant routing and normal spoken replies are physically working in v55.

## Root cause

`BoopWakeWordController` reads 16 kHz PCM in `READ_SAMPLES = 1600` blocks, i.e. 100 ms at a time. The current block is written into the wake-history ring before Sherpa/custom wake detection decides that a wake fired.

v54 correctly stopped feeding the previous full one-second wake-history ring into Android command ASR, because that made Android recognize the wake phrase as the command. However, the zero-prelude policy also discarded the detector's final 100 ms block. With natural continuous speech such as `Steve lights on`, the beginning of `lights` can already exist in that terminal detector block and is therefore lost.

`MainActivity.onWakeDetected` also called `playWakeAcceptedCue()` before `startWakeRecognition(session)`. That cue plays a 90 ms tone through the device speaker while the same live microphone pipeline is capturing the immediate command.

The deliberate pause in Ryan's working test avoids both boundaries, matching the source-level diagnosis.

## v56 change

v56 makes only the wake-to-command seam change:

1. `BoopWakeCommandAudioPolicy` keeps exactly the final 1,600 samples (100 ms) of wake-detection history as a command bridge. It never restores the old one-second pre-roll.
2. The materialized wake callback no longer plays the artificial wake-accepted speaker cue. Command recognition starts acoustically silent.
3. Existing wake names, Sherpa/custom matcher, five-say profile, three-second command window, HA routing, powered-wake resilience, pull-only diagnostics, visuals, Launcher and Shield bodies are unchanged.

## TDD / regression trail

RED:

- `source-test/BoopWakeCommandAudioPolicyTest.java` was changed first to require a final 1,600-sample bridge, preservation of short history without padding, and empty/null safety.
- `tests/test_unified_wake_handoff_contract.py` was added to require the materialized wake callback to start command recognition without `playWakeAcceptedCue()`.
- Workflow `34245905089` on commit `4a92e4cf1bf84e50f2b37cc8fae6bf590d1375bb` failed specifically because the old wake callback still contained `playWakeAcceptedCue();` (6 Python contracts passed, the new cue contract failed).

GREEN implementation:

- final 100 ms bridge policy commit `cf70174e86576276b8e2f2d172c7864368ed4d79`;
- silent materialized seam patch commit `87160ac64e61cb6a7d3837ff0e0cc8c627eeaa00`;
- materialization wiring `849934e1fba9e963a90011e1746169c71bc3e303`;
- materialized seam test correction `650aa7d0905d536e9049726aaf9877557417e44e`;
- v56 version bump `ce959438de559ea5a1dd74c9b498af5eb7c6d1a2`;
- final CI/code head `68bdbb4aabfbefd383a48aa4764568c5cb2222dc`.

## Final signed candidate receipt

- Version: 56 / `1.2.10-unified-seamless-wake-command`
- Built code: `68bdbb4aabfbefd383a48aa4764568c5cb2222dc`
- Workflow: `34246347404` SUCCESS
- Artifact: `BOOP-Unified`, ID `10064218458`
- Artifact digest: `sha256:079864cd1ecfa82f8f9618b60738523d84e0f5b1e18ecda43bf7638cc3c230f6`
- APK SHA-256: `5212b2faf4286db17b3afa44d8174d5773d555d9a8779c5d7fb1e7e6aba6a13c`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused tests: 58, zero failures/errors/skips
- Unified focused tests: 90, zero failures/errors/skips
- seamless wake-command materialized contract: PASS
- Launcher lint: PASS
- signed assembly, package/version, manifest, permanent signer and APK ZIP integrity: PASS
- artifact upload: PASS

No emulator/device launch, visual acceptance or acoustic acceptance ran in GitHub. v56 is CI/signer green and still requires Ryan's physical Pixel test.

## Required physical v56 test

Install v56 over the existing v55 app while preserving app data.

On external power, after BOOP settles into wake listening:

1. Say `Steve lights on` naturally in one breath with no deliberate pause. There should be no artificial wake bing.
2. Say `BOOP lights off` naturally in one breath.
3. Say `Steve show diagnostics` naturally in one breath.
4. Deliberately wake and give no command once; confirm the v55 silent-failure/re-arm behavior remains intact, then immediately issue another wake + command.

If one-breath speech still fails, do not widen the bridge or restore the old one-second pre-roll blindly. Use `Steve`, pause, `show diagnostics` if necessary to capture the retained trace, then inspect the physical evidence before another change.
