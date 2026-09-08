# BOOP unified handoff

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Always re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Physically proven rollback checkpoint

Ryan physically confirmed signed v48 on the Pixel in the established continuous-wake condition: charger -> Android green microphone indicator ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP.

The exact built v48 code is permanently pinned at:

- branch `checkpoint-boop-unified-v48-wake-arm`
- commit `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`
- version 48 / `1.2.2-unified-wake-arm`
- workflow `34218173825`
- APK SHA-256 `0264c3e289aab06a7be45067ce44bd72124355b11f9cc0a8ffa73afb7f4c5f02`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Do not repoint that checkpoint. It proves upstream sleeping-mic/listening plus one established BOOP wake phrase.

## Current signed candidate: v50 spoken rename repair

Ryan physically tested v49 and confirmed `Hey BOOP` still woke BOOP, so the v48 wake arm remained healthy. He then said `change name to steve`; BOOP only blinked. Tapping BOOP afterwards produced `sorry i cant find that`.

Systematic debugging found a narrow parser miss. `BoopWakeNameIntent` already runs before ordinary command routing, but its accepted rename prefixes did not include `change name to `. The exact spoken phrase therefore returned `NONE` and fell through to ordinary command handling.

TDD RED: commit `64b5fe89afe884638497e5c49842909c840339fc`, workflow `34222645705`. Ryan's exact phrase `change name to Steve` was added to the existing wake-name intent test. The unified wake suite ran 78 tests and failed exactly one: `BoopWakeNameIntentTest.parsesRequiredRenamePhrases`.

Minimal GREEN: commit `5f2d2941f3e5d2a0b9820b174563007570854379` adds only `change name to ` to `BoopWakeNameIntent.SET_PREFIXES`. Workflow `34222849165` completed successfully. No wake-arm, Sherpa, training-profile math, microphone source, charging policy, HA, eyes, blink, Shield routing, assistant, headphones or puppetry code changed.

Final signed v50 receipt:

- Version: 50 / `1.2.4-unified-wake-rename`
- Built code: `59bea8d630f90be4940d399784325a8002fd6b8b`
- Workflow: `34223081543` SUCCESS
- Artifact: `BOOP-Unified`, ID `10054611864`
- APK SHA-256: `a9d9b10b626bb5e0699384606baeb0399323a12ad2c144c0a4e9daba8ed87e05`
- Artifact ZIP SHA-256: `229ba23e904ec70945e8892150f50a6494d4d73299213e8610864ffbfec38745`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh v50 evidence: non-visual integration/materialisation passed, Launcher lint passed, 58 Shield focused tests and 78 unified focused tests passed with zero failures/errors/skips, signed assembly passed, package/version/manifest/permanent-signer/archive verification passed, and downloaded ZIP/APK hashes matched workflow receipts. No emulator/device launch, screenshots, visual acceptance or v50 physical rename/enrolment acceptance ran.

Detailed receipt: `docs/BOOP-V50-SPOKEN-RENAME-RECEIPT.md`.

## Five-say custom-name contract

The existing local enrolment implementation remains intact:

- `BOOP` is permanently valid and never requires training;
- a custom name is additive, never a replacement;
- five natural spoken examples are captured from the existing single controller-owned 16 kHz PCM stream;
- active-speech segments become amplitude-normalised pronunciation features and a compact profile;
- raw enrolment PCM is not persisted;
- the learned matcher is additive to Sherpa and must fail safely without disabling BOOP;
- custom names receive all 33 established natural wake forms;
- changing to a new custom name clears the old matching profile;
- unchanged names do not nag; a matching profile is reused;
- manual Train remains available for deliberate retraining;
- no second mic listener and no cloud wake training.

## Required next Pixel test

1. Confirm green sleeping mic + `Hey BOOP` still works.
2. Say exactly `change name to Steve` as the post-wake command.
3. BOOP should route locally into the rename flow and prompt `Say Steve five times.` rather than falling through to normal commands.
4. Say `Steve` five times naturally with short pauses; completion should return to normal wake listening.
5. Let BOOP sleep and try `Steve`, `Hey Steve`, `Oi Steve`, then confirm `Hey BOOP` still works.

If step 2 still fails while wake remains healthy, inspect the actual post-wake transcript/handoff before adding more parser phrases. If step 2 works but five-say capture fails, investigate enrolment/session/profile logic downstream. Do not disturb the v48 wake-arm checkpoint.

## Architecture boundary: clean Shield HOME remains standalone

The clean Nvidia Shield HOME replacement remains a standalone test app on branch `boop-shield-clean-launcher`, package `com.boop.shieldhome`. It is not part of AIO until Ryan explicitly approves a later merge. Unified Shield routing remains on the existing AIO Shield puppet.

## Protected BOOP contracts

Preserve the approved paired black-lidded eye master, iris-only hue, working blink timing/gates, headphones/puppetry and five-digit yellow hands. Preserve physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. Assistant remote invocation/audio remains separately unresolved and must stay on supported Android routes with no competing microphone listener, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs functional/non-visual verification only. Ryan owns visual, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.
