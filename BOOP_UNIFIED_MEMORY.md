# BOOP unified memory

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Durable wake architecture and rollback

BOOP permanently remains an accepted wake name. A custom name is additive, never a replacement. Foreground wireless charging/docking permits continuous phone wake; undocked phone remains tap-to-talk. Preserve coordinator -> `BoopWakeWordController` -> Sherpa/template -> single 16 kHz `AudioRecord` ownership and coordinated reload/re-arm. Never add a competing microphone listener.

The physically proven wake rollback is exact built v48 code `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`, pinned at branch `checkpoint-boop-unified-v48-wake-arm`. Never repoint it. On Ryan's Pixel: charger -> Android green mic ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP.

v48 receipt: version 48 / `1.2.2-unified-wake-arm`, workflow `34218173825`, APK SHA-256 `0264c3e289aab06a7be45067ce44bd72124355b11f9cc0a8ffa73afb7f4c5f02`, permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Custom-name five-say contract

Custom-name training uses five local spoken examples from the same controller-owned PCM stream. It stores only a compact amplitude-normalised pronunciation profile; raw training PCM is not persisted. The learned matcher is additive to Sherpa and must fail safely without disabling BOOP. Custom names receive all 33 established natural wake forms.

Durable product flow:

- a genuinely new custom name queues/starts local five-say enrolment;
- BOOP prompts `Say <name> five times.`;
- the same microphone owner captures five natural examples and completion returns to normal wake;
- unchanged names do not nag;
- BOOP never requires user training;
- a matching profile is reused unless the user deliberately retrains;
- changing to another name invalidates the old profile;
- no cloud wake training and no second microphone listener.

## Spoken rename history: v49-v51

v49 added automatic five-say enrolment after a changed custom name. Ryan physically confirmed `Hey BOOP` still woke BOOP but `change name to steve` fell through to ordinary handling.

v50 added the missing local rename prefix `change name to `. Its parser-level TDD was valid, but Ryan physically tested v50 and the spoken rename still failed in exactly the same general flow. Wake remained healthy. He tried multiple rename wordings, fuller sentences, and a separated `Hey BOOP` -> listening cue -> rename command sequence. Treat v50 spoken rename as a physical FAIL downstream of wake detection, not as evidence to change Sherpa sensitivity.

Systematic tracing found the next real production-path defect. WAKE recognition uses controller PCM plus pre-roll and then `MainActivity` calls `BoopWakeTranscriptNormalizer.stripLeadingWakeWord(best)` before local routing. The one-argument normalizer stripped only bare `BOOP`, while BOOP supports 33 natural wake calls. A recognizer result containing `HEY BOOP`, `OI BOOP`, `GOOD MORNING BOOP`, `BOOP WAKE UP`, or another established call before the command could therefore reach `BoopWakeNameIntent` still prefixed by the wake call and fail local rename routing.

This source defect matches Ryan's physical result but does not prove the exact transcript Android returned. Preserve that distinction.

TDD RED for the normalization layer: commit `5794ed194cf90a744fc41c0b789718ceac97605c`, workflow `34224636115`. A new test runs all established BOOP wake forms before `change name to Steve`. 79 unified focused tests ran and exactly one failed: `BoopWakeTranscriptNormalizerTest.stripsAllEstablishedBoopWakeCallsBeforeCommand`.

Minimal repair `de2e1d825f4074f9b39ee9c409492e3edf46bff6` changes only `BoopWakeTranscriptNormalizer`: BOOP's natural wake grammar is stripped longest-first before command routing, custom-name stripping still falls back to BOOP, and ordinary non-addressed commands remain untouched. Workflow `34224978703` completed successfully. Do not reopen parser synonyms, wake controller, mic source, Sherpa, enrolment math or charging policy for this repair.

Current signed v51 candidate:

- Built code `4274ed008014d1ed5810af29b64b164bf8477072`
- Version 51 / `1.2.5-unified-wake-command-normalize`
- Workflow `34225351709` SUCCESS
- Artifact `BOOP-Unified`, ID `10055532598`
- APK SHA-256 `e768248f27c671d5c4377d68905405d8c5890d7125390e13bb6c5177aad23e5c`
- Artifact ZIP SHA-256 `11127a3b08387ea64c07c66af1d68d3e9dbe6b7896f0f5ab8309cd669fcf2861`
- Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh v51 CI evidence: non-visual integration/materialization passed; Launcher lint passed; 58 Shield focused tests and 79 unified focused tests passed with zero failures/errors/skips; signed assembly, package/version, manifest, signer and archive integrity passed; downloaded ZIP/APK hashes matched workflow receipts.

v51 is not physically accepted yet. Required next boundary: green mic + `Hey BOOP`, then exactly `change name to Steve`. Expected next behavior is `Say Steve five times.` If this still fails, stop grammar/parser changes and inspect the actual Android recognizer result / wake command capture timing and handoff. If it succeeds, proceed into the existing five-say training/profile test and confirm BOOP fallback afterward.

Detailed receipt: `docs/BOOP-V51-WAKE-COMMAND-NORMALIZE-RECEIPT.md`.

## Clean Shield HOME architecture boundary

The clean Nvidia Shield HOME replacement remains **standalone for testing and is not part of AIO yet**. Standalone branch `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Current unified Shield routing stays on existing AIO `com.boop.shieldoverlay.MainActivity`. Do not reintroduce the standalone launcher into unified until Ryan explicitly approves the later merge.

Standalone launcher rule remains locked: **remove the crap, preserve Shield behavior**. Preserve double-tap Home -> Recent Apps/task switcher, Back, volume/CEC, Nvidia/Android Settings, system remote shortcuts, app switching and normal system animations.

## Permanent visual and Home contracts

The approved paired black-lidded eye master is locked in the unified phone/Wall and Shield path. Preserve approved geometry, supplied alpha and proportions. User hue affects only the iris; do not tint sclera, pupil, highlights or eyelids. Preserve headphones/puppetry and five-digit yellow hands.

Blink is user-confirmed working. Preserve its existing 183 ms curve, 3-7 second delay and motion/power/lifecycle gates. No global animation-setting changes.

HA device names and Home control buttons are physically accepted and must not regress. Home remains room-scoped to confirmed physical controllable devices, fail-closed, with no helpers/diagnostics/config plumbing or whole-house fallback on uncertainty. Room changes tear down previous-room navigation/dashboard/socket/controller state before rebuilding. Shield density scaling remains idempotent and never system-wide.

## Assistant boundary and release discipline

Assistant ownership remains explicit/reversible through supported Android routes. Remote-button/default selection and actual audio from THAT Shield remote are still physically unresolved. No overlay microphone, competing recorder, Google-disable/default/permission hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs focused non-visual tests, compilation/lint, package/signature/integrity/security checks and artifact upload. Ryan owns screenshots, appearance, animation, device and acoustic acceptance. No golden screenshots, aesthetic source-string acceptance or emulator install/launch acceptance.

Keep package `com.boop.alpha1` and permanent signer. Keep private photographs, credentials, device addresses and raw diagnostics out of the public repository. No automatic installs/grants or false Windows-sync claims.
