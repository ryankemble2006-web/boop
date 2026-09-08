# BOOP unified memory

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Durable wake architecture and rollback

BOOP permanently remains an accepted wake name. A custom name is additive, never a replacement. Foreground wireless charging/docking permits continuous phone wake; undocked phone remains tap-to-talk. Preserve coordinator -> `BoopWakeWordController` -> Sherpa/template -> single 16 kHz `AudioRecord` ownership and coordinated reload/re-arm. Never add a competing microphone listener.

The physically proven wake rollback is exact built v48 code `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`, pinned at branch `checkpoint-boop-unified-v48-wake-arm`. Never repoint it. On Ryan's Pixel: charger -> Android green mic ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP.

v48 receipt: version 48 / `1.2.2-unified-wake-arm`, workflow `34218173825`, APK SHA-256 `0264c3e289aab06a7be45067ce44bd72124355b11f9cc0a8ffa73afb7f4c5f02`, permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Current diagnostic boundary: v52 -> v53

v52 added local-only post-wake Android `SpeechRecognizer` callback tracing. Ryan physically reported that saying `Hey BOOP` produced an error message, but it vanished too quickly to read or capture while the phone remained on its charger. The exact error code is therefore still unknown. Preserve that distinction: do not infer `ERROR 7` or any other code from the flash.

v53 exists only to make that evidence capturable. Pending traces may still use a temporary toast. A terminal recognizer result/error, or a synchronous recognizer-start exception, appears in a `BOOP wake diagnostic` dialog that remains until `Close` is pressed. No wake detector, mic source, recognizer behavior, transcript normalization, parser, five-say math, charging policy or unrelated app behavior was intentionally changed.

v53 final receipt:

- Built code `3b85e5ae8babe151c3f95c6aa4637d91fc5b2cb9`
- Version 53 / `1.2.7-unified-wake-diagnostic-hold`
- Workflow `34231784857` SUCCESS
- Artifact `BOOP-Unified`, ID `10058183437`
- APK SHA-256 `d1d21ff117bf21b761d7f9fb4f78d0499c3ba662c14408c3c17373428b92dbda`
- Artifact ZIP SHA-256 `16a574baa521ca54824527090f6b3e9ae216a0805814b4edec4051a30a1d8624`
- Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Final v53 evidence: non-visual integration/materialization passed; Launcher lint passed; 58 Shield focused tests and 84 unified focused tests passed with zero failures/errors/skips; signed assembly, package/version, manifest, permanent signer and APK archive integrity passed; artifact upload passed. No device/emulator launch or acoustic/visual acceptance ran.

Primary persistence RED: `7a27ffd60d01b385b096f8396bf0e18f7c251329` / workflow `34230769806`, where the focused unified build failed because the terminal acknowledgement contract did not yet exist. A concurrent v53 Gradle edit briefly dropped existing dependencies; `3b85e5ae8babe151c3f95c6aa4637d91fc5b2cb9` restored them before the final green build.

Required next physical boundary: install v53 over the existing app, keep the Pixel charged, confirm green mic, say `Hey BOOP` once, capture the persistent `BOOP wake diagnostic` message, then stop. Do not add rename synonyms or continue five-say testing until the exact recognizer result/error is known.

Detailed receipt: `docs/BOOP-V53-PERSISTENT-WAKE-DIAGNOSTIC-RECEIPT.md`.

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

v50 added the missing local rename prefix `change name to `. Ryan physically tested v50 and spoken rename still failed while wake remained healthy. Treat that as a failure downstream of wake detection, not evidence to change Sherpa sensitivity.

Systematic tracing then found the real WAKE routing defect: `MainActivity` normalized the recognizer result before local routing, but the one-argument normalizer stripped only bare `BOOP` despite BOOP supporting 33 natural wake calls. v51 repaired the complete natural wake-prefix stripping longest-first while preserving BOOP fallback for custom names.

v51 built code `4274ed008014d1ed5810af29b64b164bf8477072`, workflow `34225351709`, APK SHA-256 `e768248f27c671d5c4377d68905405d8c5890d7125390e13bb6c5177aad23e5c`. Detailed receipt: `docs/BOOP-V51-WAKE-COMMAND-NORMALIZE-RECEIPT.md`.

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
