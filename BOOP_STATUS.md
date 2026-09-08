# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Physically proven wake checkpoint

The exact built v48 wake-arm code is pinned at branch `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. On Ryan's Pixel: charger -> green Android mic indicator ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP. Do not repoint this checkpoint.

## Current AIO signed candidate: v50 spoken rename repair

Physical v49 result: `Hey BOOP` still woke BOOP, but the immediate command `change name to steve` only caused a blink and then fell through to ordinary handling; tapping BOOP afterwards produced `sorry i cant find that`.

Root cause: the local `BoopWakeNameIntent` parser runs before ordinary command routing but did not accept the natural prefix `change name to `. It therefore returned `NONE` for Ryan's exact phrase and never reached the five-say enrolment path.

TDD evidence:

- RED commit `64b5fe89afe884638497e5c49842909c840339fc`, workflow `34222645705`: 78 unified focused tests ran, exactly one failed, `BoopWakeNameIntentTest.parsesRequiredRenamePhrases`, on the new exact phrase `change name to Steve`.
- Minimal fix commit `5f2d2941f3e5d2a0b9820b174563007570854379`: adds only `change name to ` to the existing SET prefix list. Workflow `34222849165` completed successfully.
- Final release build commit `59bea8d630f90be4940d399784325a8002fd6b8b`.

Final v50 receipt:

- Version: 50 / `1.2.4-unified-wake-rename`
- Workflow: `34223081543` SUCCESS
- Artifact: `BOOP-Unified`, ID `10054611864`
- APK SHA-256: `a9d9b10b626bb5e0699384606baeb0399323a12ad2c144c0a4e9daba8ed87e05`
- Artifact ZIP SHA-256: `229ba23e904ec70945e8892150f50a6494d4d73299213e8610864ffbfec38745`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh final-run evidence: non-visual integration/materialisation passed; Launcher lint passed; Shield 58/58 and unified 78/78 focused functional tests passed with zero failures/errors/skips; signed assembly, package/version, manifest requirements, permanent signer and APK archive integrity passed. Downloaded artifact ZIP and APK hashes matched the workflow receipts. No emulator/device launch, screenshots, visual acceptance or v50 physical rename/five-say acceptance ran.

Read `docs/BOOP-V50-SPOKEN-RENAME-RECEIPT.md` for the exact trail.

## Five-say wake-name contract

BOOP remains the permanent fallback. A custom name is additive. Five local examples are captured through the existing single controller-owned 16 kHz microphone stream and converted to a compact amplitude-normalised pronunciation profile; raw training PCM is not persisted. Custom names keep all 33 established wake forms. No second microphone listener or cloud training.

The intended spoken flow is now: `Hey BOOP` -> `change name to Steve` -> local rename handling -> `Say Steve five times.` -> five examples -> completion -> normal sleeping wake resumes. This is CI/signer green only until Ryan physically tests v50.

## IMPORTANT architecture boundary: clean Shield HOME is standalone

The clean Nvidia Shield HOME replacement remains standalone on branch `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Do not route or merge it into AIO until Ryan explicitly approves that later step.

## Protected AIO state

Approved paired black-lidded eyes remain locked; preserve approved geometry/alpha, iris-only hue, headphones/puppetry and five-digit yellow hands. Blink is user-confirmed working and is not a current defect. HA names/Home controls are physically accepted and must stay intact. Room isolation and idempotent Shield density scaling remain protected. Assistant remote invocation/audio remains a separate unresolved physical boundary.

Ryan owns visual/device/acoustic acceptance. No screenshots/golden/aesthetic acceptance, emulator device acceptance, automatic installs/grants or signer/package changes.
