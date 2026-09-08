# BOOP v55 powered-wake resilience receipt

Updated 2026-09-08. Canonical branch `boop-unified`; package `com.boop.alpha1`; permanent BOOP signer unchanged.

## Why v55 exists

Physical v54 testing proved the wake-command audio boundary and the custom `Steve` wake path, including a real local Home Assistant command (`Steve, lights on`). A later physical attempt exposed Android `SpeechRecognizer` error 7 (`ERROR_NO_MATCH`) after wake. The command action could still occur, but the temporary v53/v54 diagnostic modal interrupted the normal reply and, more importantly, source tracing found that a failed post-wake command capture could leave the coordinator believing the wake engine was armed after `BoopWakeWordController` had already stopped its microphone.

Ryan also changed the powered-phone contract: while the phone is on **any external power** it should return to continuous wake listening after normal replies and after recoverable command-recognition failures. Undocked/unpowered handheld behaviour remains tap-to-talk.

## Implemented contract

v55 keeps the v54 no-pre-roll boundary and the single controller-owned 16 kHz `AudioRecord` architecture, then adds these bounded changes:

- AC, USB, wireless charging and Android dock power all allow continuous wake listening while the app is foregrounded and microphone permission is available.
- Wireless charging remains separately identified for the existing proximity/presence behaviour; ordinary USB/AC charging does not pretend to be a magnetic/wireless dock.
- A post-wake recognizer failure or timeout is silent: no spoken error and no automatic diagnostic modal. BOOP closes that command capture, clears the coordinator's stale `engineArmed` bookkeeping and performs a real wake-engine arm again.
- Successful local commands keep their normal reply, including `Done`; TTS still owns the microphone boundary and wake listening resumes after BOOP finishes speaking so BOOP does not hear its own reply as a new command.
- A true wake-engine/microphone startup failure remains fail-safe instead of entering a tight retry loop. It is retained in diagnostics and the wake session stays safely disarmed until a fresh valid session boundary.
- The old automatic diagnostic popup is retired from normal operation. Wake traces are retained in memory only and are shown on demand by the hidden voice command `show diagnostics` (also accepts singular `show diagnostic`).
- The requested diagnostic panel is screenshot-friendly and reports the latest wake/ASR trace plus wake name, external-power state, wireless-dock state, microphone permission, wake coordinator state, recognition mode/listening state and last recovery snapshot. Closing the panel returns to the normal wake policy.
- No raw enrolment/wake audio is persisted and no diagnostic data is sent to a new cloud service.

## TDD and review trail

Tests were written before the implementation for the new contracts:

- `2a2e56a17a1bc232eed5e2a75ba78287c51bdca9` — external-power wake policy RED test.
- `e8f3fa69b7b6a98f3f5d707cd82f977ca31813d2` — hidden diagnostics intent RED test.
- `d672cd4a85588a54c48c3922a7039a48794bfc2a` — real command-failure re-arm RED test.

The missing production APIs were confirmed before implementation. The first materialized build then exposed a Java-string escaping defect in the new diagnostic panel; that was fixed at `a118699d4daff5de6b8b73508d039b03a5641df2`. Review of the subsequent green candidate caught one additional safety issue: immediately retrying a genuine wake-engine startup failure could form a hot retry loop. Final code at `f00bf5ab00c677b3dfeaa5cf7f627b47e1936440` keeps recoverable post-wake ASR failures self-healing while leaving true engine-start failures fail-safe.

## Final signed v55 receipt

- Built code: `f00bf5ab00c677b3dfeaa5cf7f627b47e1936440`
- Version: 55 / `1.2.9-unified-powered-wake-resilience`
- Workflow: `34242379018` — SUCCESS
- Artifact: `BOOP-Unified`, ID `10062603996`
- Artifact ZIP SHA-256: `433c915dc2d864566397b26d6369ac887b1c4df4d131c36b1a51b093c4eeacb9`
- APK SHA-256: `37194adcd6ed49f007df700496b2dad0348f3b838b14956a60a52e31bd23bafe`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Final workflow evidence:

- non-visual integration/materialization: passed;
- preserved Launcher lint: passed;
- Shield focused functional tests: 58 / 58, zero failures/errors/skips;
- unified focused functional tests: 88 / 88, zero failures/errors/skips;
- signed assembly: passed;
- package/version/entry/manifest checks: passed;
- permanent signer check: passed;
- APK ZIP integrity: passed;
- artifact upload: passed.

Per BOOP rules, no emulator/device launch, screenshot comparison, visual judgment or acoustic acceptance was run by CI. v55 is **CI/signer green, not yet physically accepted**.

## Physical evidence retained from v54

Do not lose the already-proven device evidence while testing v55:

- v54 physically captured `change name to Steve` as the separate post-wake command rather than re-hearing the wake phrase.
- Ryan reported the rename flow accepted `Steve`.
- Ryan physically confirmed `Steve, lights on` worked, proving custom wake -> command recognition -> local routing -> Home Assistant action on the Pixel.
- The permanent `Hey BOOP` fallback after that rename still needs an explicit final physical confirmation.

## Required v55 Pixel test

Install v55 **over** the existing BOOP install; do not uninstall or clear app data.

1. Leave the Pixel on its usual wireless/magnetic charger. Confirm BOOP returns to the green Android microphone indicator/wake-ready state.
2. Say `Steve, lights on` or `Steve, lights off`. Confirm the command works, BOOP can say the normal `Done`, and wake listening returns after TTS finishes.
3. Trigger a recoverable no-command case: wake BOOP with `Steve`, then deliberately say nothing during the command window. There should be **no spoken error and no diagnostic popup**. The microphone/wake listener should silently return.
4. Immediately try another real `Steve` command. It should work without touching the screen or restarting BOOP.
5. Say `Steve, show diagnostics`. Confirm the panel appears only on request, is readable enough to screenshot, and closing it returns to wake listening.
6. If convenient, repeat continuous-wake checks while powered by USB/AC as well as wireless charging. External power should allow wake listening in each case; proximity/presence behaviour remains wireless-dock-specific.
7. Unplug the handheld and confirm ordinary tap-to-talk behaviour remains unchanged.
8. Finally say `Hey BOOP, lights off` (or another harmless local command) to explicitly confirm the permanent BOOP fallback still works after the `Steve` rename.

If anything fails, capture the exact physical behaviour and, when useful, invoke `show diagnostics` before changing sensitivity, grammar, enrolment data, microphone ownership or the v54 no-pre-roll boundary.
