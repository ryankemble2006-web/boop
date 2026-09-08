# BOOP unified memory

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Durable wake architecture and checkpoint

BOOP permanently remains an accepted wake name. A custom name is additive, never a replacement. Foreground wireless charging/docking permits continuous phone wake; undocked phone remains tap-to-talk. Preserve coordinator -> `BoopWakeWordController` -> Sherpa/template -> single 16 kHz `AudioRecord` ownership and coordinated reload/re-arm. Never add a competing microphone listener.

The physically proven wake rollback is exact built v48 code `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`, pinned at branch `checkpoint-boop-unified-v48-wake-arm`. Do not repoint it. On Ryan's Pixel: charger -> Android green mic ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP. This physically closes the v47 upstream “not listening at all” defect and proves the sleeping arm path plus one established BOOP phrase.

v48 build receipt: version 48 / `1.2.2-unified-wake-arm`, workflow `34218173825`, APK SHA-256 `0264c3e289aab06a7be45067ce44bd72124355b11f9cc0a8ffa73afb7f4c5f02`, permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Custom-name five-say contract

Custom-name training uses five local spoken examples from the same controller-owned PCM stream. It stores only a compact amplitude-normalised pronunciation profile; raw training PCM is not persisted. The learned matcher is additive to Sherpa and must fail safely without disabling BOOP. Custom names receive all 33 established natural wake forms.

The intended product flow is now explicit and durable:

- changing BOOP's name in Voice Settings to a new custom name such as `Steve` automatically queues training;
- after Voice Settings closes BOOP prompts `Say Steve five times.`;
- the same microphone owner captures five natural examples and completion returns to the normal wake path;
- an unchanged custom name does not nag on every settings close;
- BOOP itself never needs user training;
- a matching existing profile is reused rather than automatically retrained;
- the manual Train action remains available for deliberate retry/retraining;
- changing to a genuinely different untrained name invalidates the old profile;
- no cloud wake training and no second microphone listener.

TDD RED for this flow: commit `72591554376d2cada7df24d99d5934443a986278`, workflow `34220316310`, failed exactly because `BoopWakeTrainingPolicy` did not yet exist. Implementation commit `7ed577d2cb683da0cf7ae5339efc509566123140` added the pure policy and generated settings-close training flow without changing the v48 real-attempt arm behavior.

Current signed candidate:

- Built code `87a7abee880b9283d51fb71ed2bf9bd9ed187b28`
- Version 49 / `1.2.3-unified-wake-enrolment`
- Workflow `34221275050` SUCCESS
- Artifact `BOOP-Unified`, ID `10053919758`
- APK SHA-256 `2fd65505bea205cce8e7cdc2124cba4a4fa818c5b1fd4ba0efdc5c24f064ffd9`
- Artifact ZIP SHA-256 `fc27c729a848fbda98dee882b626633da46f1fd338a98fa08911cc76106eecb9`
- Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Fresh v49 evidence: non-visual integration/materialization passed; Launcher lint passed; 58 Shield focused tests and 78 unified focused tests passed with zero failures/errors/skips; signed assembly, package/version, manifest, signer and archive integrity passed; downloaded ZIP/APK hashes matched workflow receipts. v49 acoustic/product behavior is not physically accepted until Ryan tests it.

Physical v49 sequence: first confirm v48 behavior still holds (green sleeping mic + `Hey BOOP`); change name to Steve and close settings; confirm automatic `Say Steve five times.`; speak Steve five times; confirm completion and re-arm; test Steve/Hey Steve/Oi Steve/Morning Steve/Steve wake up; finish by confirming Hey BOOP still works. Record misses and false wakes separately before tuning thresholds.

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
