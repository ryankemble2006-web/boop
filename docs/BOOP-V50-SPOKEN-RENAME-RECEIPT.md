# BOOP v50 spoken rename receipt

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent BOOP signer unchanged.

## Physical input that triggered this repair

Ryan tested signed v49 on the Pixel. `Hey BOOP` still woke BOOP, proving the v48 wake-arm path remained alive. He then said `change name to steve`; BOOP only blinked. Tapping BOOP afterwards produced `sorry i cant find that`.

This is a physical v49 failure of the local spoken rename route, not a wake-arm or acoustic-listening failure.

## Root cause

`BoopWakeNameIntent` already ran before ordinary command routing, but its accepted rename prefixes did not include the natural phrase `change name to `. It accepted forms such as `your name is Steve`, `I'm calling you Steve`, and `from now on you're Steve`. Therefore `change name to Steve` returned `NONE` and fell through to ordinary command handling.

## TDD trail

RED commit `64b5fe89afe884638497e5c49842909c840339fc` added Ryan's exact phrase to `BoopWakeNameIntentTest` without changing production code. Workflow `34222645705` ran 78 unified focused tests and failed exactly one: `BoopWakeNameIntentTest.parsesRequiredRenamePhrases`.

Minimal GREEN commit `5f2d2941f3e5d2a0b9820b174563007570854379` added only the prefix `change name to ` to `BoopWakeNameIntent.SET_PREFIXES`. Workflow `34222849165` completed successfully, including the wake/routing suite.

No wake-arm logic, Sherpa model, five-say DSP/profile code, microphone source, charging policy, Home Assistant code, eyes, blink, Shield routing, assistant code, headphones or puppetry changed in this repair.

## Final signed v50 candidate

- Version: 50 / `1.2.4-unified-wake-rename`
- Built code: `59bea8d630f90be4940d399784325a8002fd6b8b`
- Workflow: `34223081543` SUCCESS
- Artifact: `BOOP-Unified`, ID `10054611864`
- APK SHA-256: `a9d9b10b626bb5e0699384606baeb0399323a12ad2c144c0a4e9daba8ed87e05`
- Artifact ZIP SHA-256: `229ba23e904ec70945e8892150f50a6494d4d73299213e8610864ffbfec38745`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh final-run evidence: non-visual integration/materialisation passed; Launcher lint passed; 58 Shield focused tests and 78 unified focused tests passed with zero failures/errors/skips; signed assembly passed; package `com.boop.alpha1`, version 50 / `1.2.4-unified-wake-rename`, manifest requirements, permanent signer and APK archive integrity passed. The downloaded artifact ZIP matched GitHub's digest and the extracted APK matched the workflow APK hash.

No emulator/device launch, screenshots, visual acceptance or v50 physical rename/five-say acceptance ran in GitHub.

## Required Pixel discriminator

1. Confirm `Hey BOOP` still wakes from sleep with the green mic present.
2. Say exactly `change name to Steve` as the first post-wake command.
3. BOOP should route locally into custom-name handling and begin the five-say flow rather than falling through to ordinary commands.
4. Say `Steve` five times naturally with short pauses and wait for completion.
5. Let BOOP sleep, test `Steve`, `Hey Steve`, `Oi Steve`, then confirm `Hey BOOP` still works.

If step 2 still fails while `Hey BOOP` works, inspect the actual post-wake transcript/command handoff before changing the parser again. If step 2 works but training fails, move downstream into enrolment capture/profile logic. Do not disturb the physically proven v48 wake-arm checkpoint.

## Rollback

The physically proven wake-arm checkpoint remains branch `checkpoint-boop-unified-v48-wake-arm` at exact built code `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. Never repoint it.
