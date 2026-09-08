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

## Spoken rename phrase repair, v50

Ryan physically tested v49 and confirmed `Hey BOOP` still woke BOOP. The next command `change name to steve` only produced a blink, then tapping BOOP yielded `sorry i cant find that`.

This was not an acoustic wake failure. `BoopWakeNameIntent` already owns local rename routing before general commands, but its SET prefixes omitted the natural phrase `change name to `. Ryan's exact phrase therefore returned `NONE` and fell through to ordinary handling.

TDD RED: commit `64b5fe89afe884638497e5c49842909c840339fc`, workflow `34222645705`. The exact phrase `change name to Steve` was added to the existing parser test. 78 unified focused tests ran and exactly one failed: `BoopWakeNameIntentTest.parsesRequiredRenamePhrases`.

Minimal GREEN: commit `5f2d2941f3e5d2a0b9820b174563007570854379` adds only `change name to ` to `BoopWakeNameIntent.SET_PREFIXES`. Workflow `34222849165` completed successfully. Do not generalise this repair into wake sensitivity/model changes; none were needed.

Current signed v50 candidate:

- Built code `59bea8d630f90be4940d399784325a8002fd6b8b`
- Version 50 / `1.2.4-unified-wake-rename`
- Workflow `34223081543` SUCCESS
- Artifact `BOOP-Unified`, ID `10054611864`
- APK SHA-256 `a9d9b10b626bb5e0699384606baeb0399323a12ad2c144c0a4e9daba8ed87e05`
- Artifact ZIP SHA-256 `229ba23e904ec70945e8892150f50a6494d4d73299213e8610864ffbfec38745`
- Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh v50 CI evidence: non-visual integration/materialisation passed; Launcher lint passed; 58 Shield focused tests and 78 unified focused tests passed with zero failures/errors/skips; signed assembly, package/version, manifest, signer and archive integrity passed; downloaded ZIP/APK hashes matched workflow receipts.

v50 physical sequence remains unaccepted: first confirm green sleeping mic + `Hey BOOP`; then say exactly `change name to Steve`; BOOP should enter local rename/five-say flow; say Steve five times; confirm normal wake returns; test Steve/Hey Steve/Oi Steve and finish with Hey BOOP. If the exact rename still fails while wake works, inspect the actual post-wake transcript/handoff before extending grammar further. If rename works but training fails, move downstream to enrolment capture/profile logic.

Detailed receipt: `docs/BOOP-V50-SPOKEN-RENAME-RECEIPT.md`.

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
