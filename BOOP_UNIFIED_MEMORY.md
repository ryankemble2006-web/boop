# BOOP unified memory

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Current canonical candidate: v63 notifications

BOOP notification presentation is now part of canonical Unified. Built code head `2b0484cb9c95b0836c2bb6e93f6d7c890bc0e2a4`, version 63 / `1.2.17-unified-notifications`, workflow `34308822296` SUCCESS, artifact ID `10087693779`, artifact digest `sha256:5673289f3a11cceceec99cfdeac2506c17eae0fbdfd86560b3c407f49c9e96ea`, APK SHA-256 `e92963c4bff18b8b8fb2b88202aac3207186edb4af05113874d92e0e455e130f`, permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Fresh signed-artifact verification: Shield focused tests 58/58 and unified focused tests 136/136 with zero failures/errors/skips. Notification presenter contracts, seamless wake handoff, preserved Launcher checks, signed assembly, package/version/signer/archive integrity and artifact upload passed. Separate Shield HOME routing workflow `34308822310` also passed.

v63 is CI/signer green, not physically accepted. Ryan owns Pixel appearance, lock-screen privacy and acoustic acceptance. Do not create a v63 rollback checkpoint until he explicitly accepts this exact signed build.

## Durable notification contract

Android's original notification is authoritative. BOOP is a puppet mirror around it.

- One reusable `BoopNotificationPuppetView` serves in-place Wall, unlocked overlay and locked presentation.
- Before authentication the locked surface may show app identity/icon/count only. Do not expose message content there.
- Tap preserves the source Android notification `PendingIntent`.
- Successful `FLAG_AUTO_CANCEL` handling may remove BOOP's mirror only after source send succeeds.
- Swipe and timeout dismiss BOOP's mirror only. They must not cancel the Android shade notification.
- Swipe contract is >=72dp on a dominant horizontal or vertical axis with strict >1.25x directional dominance.
- Entrance motion remains card alpha/translation from -16dp over 260ms with `OvershootInterpolator(0.7f)` plus hands scale 0.96 -> 1 over 220ms.
- Local cue is deterministic 320ms audio plus `{0,35,55,28}` vibration.
- BOOP sound/vibration is allowed only when coordinator `playCue=true` and the native Android channel is known silent for both sound and vibration. Unknown/noisy channels stay visual-only to prevent double alerts.
- Preserve overlay/screen-on/vibrate authority while rejecting full-screen-intent, query-all, accessibility-service and device-admin authority.

The exact approved notification hands binary is locked:

- size `1,809,990` bytes;
- SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`;
- Git blob `d47037271bf320f4f110e3f8416f59882062afac`;
- canonical path `unified/assets/boop-notifications/boop-yellow-hands-approved.png`.

Do not regenerate, recompress, recolor, crop or weaken the hash guard. Notification branch repair is `45fc81c95f9fece434d7c6a0ca7ae9eb8cc4c183`; animation root master repair is `animation-freddie-mercury@783d38d0cbc18e3e93ed305ba36446da79f27ce3`.

## Durable wake architecture

BOOP permanently remains an accepted wake name. A custom name is additive, never a replacement. Custom-name training uses five local spoken examples from the existing single controller-owned 16 kHz PCM stream and stores only a compact pronunciation profile; raw enrolment PCM is not persisted. Never add a competing microphone listener.

Continuous phone wake is allowed while the phone is on any external power. Unpowered handheld behavior remains tap-to-talk. After normal TTS, wake re-arms only after BOOP finishes speaking. A post-wake Android command-ASR no-match or timeout is silent and performs a genuine re-arm. A hard wake-engine/microphone startup failure remains fail-safe latched.

`show diagnostics` stays hidden and pull-only. Do not auto-display diagnostics, persist raw audio or cloud-log diagnostic audio.

## Durable wake-to-command boundary

Preserve exactly the final 1,600 detector samples / 100 ms bridge into command ASR, continue the same live PCM stream immediately afterward, retain wake-prefix stripping as defensive parsing and keep wake-to-command handoff acoustically silent. Do not restore the old full one-second wake-history pre-roll.

Default/permanent BOOP uses zero intentional Sherpa trailing blanks. Ryan physically accepted natural `BOOP + command` without a deliberate pause. Learned custom names remain matchable while speech is active.

Protected rollback tags:

- `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`;
- `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

Never repoint them.

## Durable uncensored-speech rule from v59

Both ordinary tap-to-talk and post-wake command recognition request `RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS=false`. BOOP must not add its own profanity blacklist or replace recognized adult/profane speech with asterisks. Ryan physically accepted the spoken rename/training path on v59.

## Durable listening-eye rules from v60-v63

Listening state must reuse the exact approved black-lidded BOOP eye master already in the app. Do not regenerate or substitute listening-pose artwork. Listening feedback is active only while tap-to-talk ASR or post-wake command ASR is actually listening.

Ryan physically liked v61's stronger reading motion but rejected its double/ghost eye caused by stacking a shifted iris patch over a stationary iris. Durable compositing rule: a moving listening gaze must replace the stationary iris/pupil aperture, never stack a second visible eye layer over it.

v62 implemented the single-layer aperture replacement and wider hue coverage. Its CI/signer evidence was green but physical visual acceptance remained pending. v63 carries that same listening-eye behavior forward, so the v63 Pixel acceptance should also confirm one moving pupil/iris per eye, no stationary ghost, reading sweep intact and selected iris hue intact.

## Clean Shield HOME boundary

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Do not merge it into Unified until Ryan explicitly approves later. Current v63 separate Shield HOME routing CI is green but that does not change the standalone product boundary.

## Permanent visual, Home and assistant contracts

Preserve the approved paired black-lidded eye master, approved geometry/alpha, iris-only hue, headphones/puppetry and five-digit yellow hands. Blink is user-confirmed working; preserve timing/curve/delay and motion/power/lifecycle gates.

HA device names and Home controls are physically accepted and must not regress. Home remains room-scoped and fail-closed; room changes tear down previous-room ownership/state before rebuilding. Shield density scaling remains idempotent and never system-wide.

Assistant ownership remains explicit/reversible through supported Android routes. Shield remote assistant activation/audio remains separately unresolved. No overlay mic, competing recorder, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs focused non-visual tests, compilation/lint, package/signature/integrity checks and artifact upload. Ryan owns screenshots, appearance, animation, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.

## Planned future work: BOOP screensaver `What If?`

Keep the already queued optional `What If?` BOOP screensaver plan. Default OFF. Primary personal target is Pixel C / future BOOP Mirror, with a simple `Off` / `What If?` selector and small idle-time choice. On idle, BOOP fades away to a black full-screen field and the recovered Silk-style opposing pulse animation owns the screen. BOOP returns immediately on touch, wake/voice activity or other deliberate interaction. Recreate the effect natively for current displays, preserve the recovered original ZIPs as reference masters, and handle long-running brightness/power responsibly.
