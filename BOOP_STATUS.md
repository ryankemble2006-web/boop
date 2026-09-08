# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Physically proven wake checkpoint

The exact built v48 wake-arm code is pinned at branch `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. On Ryan's Pixel: charger -> green Android mic indicator ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP. Do not repoint this checkpoint.

## Current AIO signed candidate: v53 persistent wake diagnostic

Physical v52 result: saying `Hey BOOP` on the charged Pixel produced an error diagnostic, but it vanished too quickly to read or capture. The exact Android recognizer error remains unknown. Do not infer an error code from the flash.

v53 makes terminal post-wake diagnostics persistent without changing recognition behavior. Pending traces may still toast; a terminal result/error or synchronous recognizer-start exception now appears in a `BOOP wake diagnostic` dialog that remains until `Close` is pressed.

Final v53 receipt:

- Built code: `3b85e5ae8babe151c3f95c6aa4637d91fc5b2cb9`
- Version: 53 / `1.2.7-unified-wake-diagnostic-hold`
- Workflow: `34231784857` SUCCESS
- Artifact: `BOOP-Unified`, ID `10058183437`
- APK SHA-256: `d1d21ff117bf21b761d7f9fb4f78d0499c3ba662c14408c3c17373428b92dbda`
- Artifact ZIP SHA-256: `16a574baa521ca54824527090f6b3e9ae216a0805814b4edec4051a30a1d8624`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Verification: non-visual integration/materialization passed; Launcher lint passed; Shield 58/58 and unified 84/84 focused functional tests passed with zero failures/errors/skips; signed assembly, package/version, manifest, permanent signer and APK archive integrity passed; artifact upload passed. No emulator/device launch, screenshots, visual acceptance or acoustic acceptance ran.

Primary persistence RED: commit `7a27ffd60d01b385b096f8396bf0e18f7c251329`, workflow `34230769806`, failed because the terminal acknowledgement contract did not yet exist. A concurrent v53 version edit briefly dropped existing dependencies; `3b85e5ae8babe151c3f95c6aa4637d91fc5b2cb9` restored them before the final green build.

Read `docs/BOOP-V53-PERSISTENT-WAKE-DIAGNOSTIC-RECEIPT.md` for the exact trail.

## Previous wake-command normalization state

v50 physically preserved `Hey BOOP` wake but spoken rename still failed. v51 repaired natural wake-prefix stripping before local command routing. Built v51 commit `4274ed008014d1ed5810af29b64b164bf8477072`, workflow `34225351709`, APK SHA-256 `e768248f27c671d5c4377d68905405d8c5890d7125390e13bb6c5177aad23e5c`. v52 then added post-wake ASR diagnostics so physical behavior could be observed rather than guessed.

## Five-say wake-name contract

BOOP remains the permanent fallback. A custom name is additive. Five local examples are captured through the existing single controller-owned 16 kHz microphone stream and converted to a compact amplitude-normalised pronunciation profile; raw training PCM is not persisted. Custom names keep all 33 established wake forms. No second microphone listener or cloud training.

Required Pixel boundary now: install v53, keep the Pixel charged, confirm green mic, say `Hey BOOP` once, capture the persistent `BOOP wake diagnostic` message, and stop. Do not add rename synonyms or proceed with five-say testing until the exact recognizer result/error is known.

## IMPORTANT architecture boundary: clean Shield HOME is standalone

The clean Nvidia Shield HOME replacement remains standalone on branch `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Do not route or merge it into AIO until Ryan explicitly approves that later step.

## Protected AIO state

Approved paired black-lidded eyes remain locked; preserve approved geometry/alpha, iris-only hue, headphones/puppetry and five-digit yellow hands. Blink is user-confirmed working and is not a current defect. HA names/Home controls are physically accepted and must stay intact. Room isolation and idempotent Shield density scaling remain protected. Assistant remote invocation/audio remains a separate unresolved physical boundary.

Ryan owns visual/device/acoustic acceptance. No screenshots/golden/aesthetic acceptance, emulator device acceptance, automatic installs/grants or signer/package changes.
