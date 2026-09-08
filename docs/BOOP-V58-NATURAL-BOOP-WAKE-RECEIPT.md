# BOOP Unified v58 natural BOOP wake receipt

Date: 2026-09-08
Branch: `boop-unified`
Package: `com.boop.alpha1`
Signer: permanent BOOP signer unchanged

## Physical evidence that led to v58

Signed v57 fixed the learned custom-name path on Ryan's powered Pixel. Natural one-breath custom wake + command worked without a deliberate pause:

- `Steve lights on` worked.
- `Steve show diagnostics` worked.
- Ryan changed the name by voice to `Fred`; BOOP requested five spoken samples, listened to all five, then `Fred lights on` worked and BOOP said `Done`.
- Ryan changed the name by voice to `Jeff`; the same five-sample local enrolment completed, then `Jeff lights on` worked and BOOP said `Done`.

The permanent fallback `BOOP` still woke the app, but `BOOP lights on` spoken continuously required a pause after `BOOP`. This isolated the remaining failure to the default Sherpa keyword path. Five-sample enrolment, learned-name streaming, command ASR, routing, Home Assistant, reply TTS and wake recovery were no longer plausible causes.

## Root cause

`source/BoopSherpaWakeSpotter.java` configured Sherpa with:

`config.setNumTrailingBlanks(1);`

That setting required a trailing blank after the recognized BOOP keyword before Sherpa finalized the trigger. A deliberate pause supplied that blank. Continuous speech such as `BOOP lights on` did not, so the wake decision arrived late enough to lose useful command audio.

The learned-name matcher fixed in v57 does not have that trailing-silence requirement, which explains why Steve, Fred and Jeff already worked naturally.

## v58 change

One functional production change only:

- change Sherpa `setNumTrailingBlanks(1)` to `setNumTrailingBlanks(0)` for the permanent BOOP/default keyword path.

Not changed:

- keyword score or threshold;
- BOOP keyword phrases/assets;
- v57 learned custom-name matcher;
- five-sample local enrolment;
- v56 exact 1,600-sample / 100 ms command bridge;
- three-second command window;
- microphone ownership;
- external-power wake policy;
- silent no-match/timeout re-arm;
- pull-only `show diagnostics`;
- Home Assistant routing or TTS;
- visuals, Launcher, Shield behavior, package or signer.

## TDD evidence

Valid RED:

- commit `177cb0adcf685101b6cbc77478bacb3f569d539f`
- workflow `34252407406`
- materialized wake-handoff suite: 2 tests run, 1 passed and exactly the new `test_default_boop_does_not_require_trailing_silence_before_trigger` regression failed because `config.setNumTrailingBlanks(0);` was absent.

GREEN production change:

- commit `2f4b225998a40835d3db81595573b8af28009c06`
- workflow `34252652849`
- new materialized natural-BOOP contract passed, focused wake/routing tests passed, signed build/package/signer/archive checks passed and artifact upload passed.

Final v58 release/build code:

- built commit `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`
- version 58 / `1.2.12-unified-natural-boop-wake`
- workflow `34252950640` SUCCESS
- artifact `BOOP-Unified`, ID `10066828560`
- artifact digest `sha256:feb800dd99d6da15876892fbae4027def903bd43f3887b4b771c0e384a2ab372`
- APK SHA-256 `5a5b4846a55bc58d3444a8c8f178441af576e86695025c17c8d4483ad9fd01aa`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused tests: 58/58, zero failures/errors/skips
- unified focused tests: 92/92, zero failures/errors/skips
- materialized wake-handoff tests: 2/2 PASS, including the no-trailing-silence BOOP contract
- Launcher lint, signed assembly, package/version, manifest, signer and APK ZIP integrity PASS
- artifact upload PASS

This is CI/signer green. Physical v58 acceptance is pending.

## Required powered-Pixel acceptance

Install v58 over v57 without uninstalling. Keep the phone on external power.

1. Say `BOOP lights on` naturally in one breath with no deliberate pause.
2. Say `BOOP lights off` the same way.
3. Say the currently trained custom name (currently `Jeff`) + `lights on` naturally to confirm the v57 learned-name path did not regress.
4. Optionally say `BOOP show diagnostics` in one breath.
5. Leave BOOP listening during ordinary nearby speech for a short sanity check that removing the trailing blank did not introduce an obvious false-positive regression. Sensitivity and keyword thresholds are unchanged.

Do not widen the 100 ms command bridge or change wake sensitivity in response to this specific issue without new physical evidence.
