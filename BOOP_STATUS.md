# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Physically proven wake checkpoint

The exact built v48 wake-arm code is pinned at branch `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. On Ryan's Pixel: charger -> green Android mic indicator ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP. Do not repoint this checkpoint.

## Current AIO signed candidate: v51 wake-command normalization

Physical v50 result: wake remained healthy (`Hey BOOP` woke BOOP), but no tested spoken rename wording reached `Say Steve five times.` Ryan tried `change name to steve`, fuller sentences, and a separated `Hey BOOP` -> listening cue -> rename command flow. Treat v50 spoken rename as a physical FAIL downstream of wake detection.

Root cause tracing moved upstream of the already-fixed rename parser. In the real WAKE result path, `MainActivity` normalizes the recognizer result before local routing. The production one-argument `BoopWakeTranscriptNormalizer` stripped only a leading bare `BOOP`, despite wake audio containing pre-roll and BOOP supporting 33 natural wake calls. Natural prefixes such as `HEY BOOP`, `OI BOOP`, `GOOD MORNING BOOP` or `BOOP WAKE UP` could therefore remain attached to the post-wake command and prevent the local rename parser from seeing the command at its start.

This is a concrete code-path defect matching the physical symptom, not proof of the exact transcript Android returned. v51 remains physically unaccepted until Ryan tests it.

TDD evidence:

- RED commit `5794ed194cf90a744fc41c0b789718ceac97605c`, workflow `34224636115`: 79 unified focused tests ran, exactly one failed, `BoopWakeTranscriptNormalizerTest.stripsAllEstablishedBoopWakeCallsBeforeCommand`, which exercises all established BOOP wake calls before `change name to Steve`.
- Minimal repair commit `de2e1d825f4074f9b39ee9c409492e3edf46bff6`: default BOOP normalization now strips the complete natural wake grammar longest-first; custom-name normalization retains BOOP fallback. Workflow `34224978703` completed successfully.
- Final release build commit `4274ed008014d1ed5810af29b64b164bf8477072`.

Final v51 receipt:

- Version: 51 / `1.2.5-unified-wake-command-normalize`
- Workflow: `34225351709` SUCCESS
- Artifact: `BOOP-Unified`, ID `10055532598`
- APK SHA-256: `e768248f27c671d5c4377d68905405d8c5890d7125390e13bb6c5177aad23e5c`
- Artifact ZIP SHA-256: `11127a3b08387ea64c07c66af1d68d3e9dbe6b7896f0f5ab8309cd669fcf2861`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh final-run evidence: non-visual integration/materialization passed; Launcher lint passed; Shield 58/58 and unified 79/79 focused functional tests passed with zero failures/errors/skips; signed assembly, package/version, manifest requirements, permanent signer and APK archive integrity passed. Downloaded artifact ZIP/APK hashes matched the receipts. No emulator/device launch, screenshots, visual acceptance or v51 physical rename/five-say acceptance ran.

Read `docs/BOOP-V51-WAKE-COMMAND-NORMALIZE-RECEIPT.md` for the exact trail.

## Five-say wake-name contract

BOOP remains the permanent fallback. A custom name is additive. Five local examples are captured through the existing single controller-owned 16 kHz microphone stream and converted to a compact amplitude-normalised pronunciation profile; raw training PCM is not persisted. Custom names keep all 33 established wake forms. No second microphone listener or cloud training.

Required Pixel boundary: confirm green mic + `Hey BOOP`; then say exactly `change name to Steve`. Expected next behavior is `Say Steve five times.` If that still fails, stop grammar changes and inspect the real recognizer result / wake command-capture timing and handoff. If it succeeds, continue into five-say acoustic testing.

## IMPORTANT architecture boundary: clean Shield HOME is standalone

The clean Nvidia Shield HOME replacement remains standalone on branch `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Do not route or merge it into AIO until Ryan explicitly approves that later step.

## Protected AIO state

Approved paired black-lidded eyes remain locked; preserve approved geometry/alpha, iris-only hue, headphones/puppetry and five-digit yellow hands. Blink is user-confirmed working and is not a current defect. HA names/Home controls are physically accepted and must stay intact. Room isolation and idempotent Shield density scaling remain protected. Assistant remote invocation/audio remains a separate unresolved physical boundary.

Ryan owns visual/device/acoustic acceptance. No screenshots/golden/aesthetic acceptance, emulator device acceptance, automatic installs/grants or signer/package changes.
