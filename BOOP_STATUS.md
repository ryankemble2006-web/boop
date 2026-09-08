# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Physically proven wake checkpoint

The exact built v48 wake-arm code is pinned at branch `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. On Ryan's Pixel: charger -> green Android mic indicator ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP. Do not repoint this checkpoint.

## Current AIO signed candidate: v54 wake-command boundary

Physical v53 evidence after one `Hey BOOP`:

`WAKE ASR RESULT +583ms ready=18 begin=153 end=544 partial="hey pooop" final="hey pooop"`

This proves Android command ASR was successfully transcribing the wake phrase itself and terminating before a separate command could be spoken.

Root cause: `BoopWakeWordController` stored one second of wake-detection PCM and wrote that pre-roll into the command recognizer pipe after wake detection. v54 excludes wake-detection history from command ASR and continues with live post-detection PCM only. The single 16 kHz microphone owner and three-second command window are unchanged. No sensitivity, parser, five-say, charging, HA, visual, Shield or assistant behavior was intentionally changed.

TDD evidence:

- RED `d68fca32f25b9de360404170ff46576baabb33d5`, workflow `34233607842`.
- Policy `edb2750e1140e5f8d4e39ec2e65b9097d71cef01`.
- GREEN controller `f90031830dfcf7f46c5a3644502a5bdf278acc06`, workflow `34233966994` SUCCESS.

Final v54 receipt:

- Built code: `fe26f29cb330b450e5e9894a4588b19ae9d7152a`
- Version: 54 / `1.2.8-unified-wake-command-boundary`
- Workflow: `34234618256` SUCCESS
- Artifact: `BOOP-Unified`, ID `10059395955`
- APK SHA-256: `ee6a5ade5538cf3b0b4aa1346cdeb5b957c9ed56ff214279c2d7c46e428fd287`
- Artifact ZIP SHA-256: `0f7bb5168a961aa1e44bf455c070a3db8e112c8a55f9360db2236fc1dc4d6908`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Verification: non-visual integration/materialization passed; Launcher lint passed; Shield 58/58 and unified 85/85 focused functional tests passed with zero failures/errors/skips; signed assembly, package/version, manifest, permanent signer and APK archive integrity passed; artifact upload passed. No emulator/device launch, screenshots, visual acceptance or acoustic acceptance ran.

Read `docs/BOOP-V54-WAKE-COMMAND-BOUNDARY-RECEIPT.md` for the exact trail.

## Previous diagnostic state

v52 added post-wake ASR diagnostics. v53 made terminal results/errors persistent and physically exposed the wake-phrase pre-roll defect. v53 built code `3b85e5ae8babe151c3f95c6aa4637d91fc5b2cb9`, workflow `34231784857`, APK SHA-256 `d1d21ff117bf21b761d7f9fb4f78d0499c3ba662c14408c3c17373428b92dbda`.

## Earlier wake-command normalization

v50 physically preserved `Hey BOOP` wake but spoken rename still failed. v51 repaired natural wake-prefix stripping before local command routing. Built v51 commit `4274ed008014d1ed5810af29b64b164bf8477072`, workflow `34225351709`, APK SHA-256 `e768248f27c671d5c4377d68905405d8c5890d7125390e13bb6c5177aad23e5c`.

## Five-say wake-name contract

BOOP remains the permanent fallback. A custom name is additive. Five local examples are captured through the existing single controller-owned 16 kHz microphone stream and converted to a compact amplitude-normalised pronunciation profile; raw training PCM is not persisted. Custom names keep all 33 established wake forms. No second microphone listener or cloud training.

Required Pixel boundary now: install v54, keep the Pixel charged, confirm green mic, say `Hey BOOP`, wait for the listening cue, then say exactly `change name to Steve`. If BOOP says `Say Steve five times.`, continue training and then confirm `Steve` plus `Hey BOOP` fallback. If a persistent diagnostic appears instead, capture it and stop before further grammar changes.

## IMPORTANT architecture boundary: clean Shield HOME is standalone

The clean Nvidia Shield HOME replacement remains standalone on branch `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Do not route or merge it into AIO until Ryan explicitly approves that later step.

## Protected AIO state

Approved paired black-lidded eyes remain locked; preserve approved geometry/alpha, iris-only hue, headphones/puppetry and five-digit yellow hands. Blink is user-confirmed working and is not a current defect. HA names/Home controls are physically accepted and must stay intact. Room isolation and idempotent Shield density scaling remain protected. Assistant remote invocation/audio remains a separate unresolved physical boundary.

Ryan owns visual/device/acoustic acceptance. No screenshots/golden/aesthetic acceptance, emulator device acceptance, automatic installs/grants or signer/package changes.
