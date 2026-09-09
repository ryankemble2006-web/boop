# BOOP unified memory

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Current canonical candidate: v67 finished eyes + repaired procedural hue

Built code head `63bb80283af7424bc1012fe71444552d8b942a74`, production eye transplant commit `c35b57a44ec0e7fb8f06cb49a4f0ab10915dbf5d`, version 67 / `1.2.21-unified-finished-eyes-hue`, workflow `34312779359` SUCCESS, artifact ID `10089043590`, artifact digest `sha256:c0a291a324b0e96a81c4726ad187efa5f65f0ba63dc41d428d7aaae1bc001000`, APK SHA-256 `13c51f8a56e109a9dc57bc37cba3175ce5290b210f65b2692ce575d76194b55b`, permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Fresh signed-artifact verification: non-visual integration contracts 12/12, Shield focused tests 58/58 and Unified focused tests 136/136 with zero failures/errors/skips. Notification presenter/manifest contracts, seamless wake handoff, preserved Launcher checks, signed assembly, package/version/signer/archive integrity and artifact upload passed. Separate Shield HOME routing workflow `34312684627` also passed on the production eye commit.

**No visual acceptance was performed by GitHub.** Ryan explicitly owns screenshot/appearance/layout/animation/device judgment. v67 is CI/signer green, not physically accepted as an integrated build. Do not create or repoint a v67 rollback checkpoint until he explicitly accepts this exact signed APK.

Detailed receipt: `docs/BOOP-V67-FINISHED-EYES-HUE-RECEIPT.md`.

## Durable finished-eye rules from v67

Ryan confirmed the later v65 procedural-eye pass was the finished default-eye state where the sclera/whites finally looked right. v67 surgically ports that eye stack into the canonical notification lineage rather than merging the experimental branch wholesale.

Canonical materialization order is now durable:

1. `scripts/patch-unified-reading-eyes.py` supplies the procedural Canvas iris/pupil/catchlight renderer;
2. `scripts/patch-v64-procedural-sclera.py` widens the in-memory neutral sclera cleanup beyond the complete reading-motion envelope and removes the dark socket/remnant crescent problem;
3. `scripts/patch-v65-feathered-sclera.py` keeps the old baked-in iris hidden in the centre but feathers the cleanup back into the approved original grey sclera shading instead of leaving a pale contact-lens edge.

The approved black-lidded PNG remains the source for the eye bodies/lids and must not be regenerated. The procedural renderer neutralises the baked-in iris in memory and draws exactly one procedural iris/pupil/catchlight set per eye. Do not restore the old shifted PNG iris patch or stack a moving iris over a stationary iris.

The user eye-colour control now drives `proceduralIrisHueDegrees` directly. `irisColour()` consumes that hue for the procedural iris. This is the durable hue rule:

- default remains BOOP cyan/blue;
- user-selected hue affects the procedural iris only;
- sclera/whites, pupils, catchlights, black eyelids/accents and the rest of the approved eye artwork must not be tinted;
- do not reintroduce whole-bitmap `ColorFilter` tinting or bitmap-wide pixel recolouring for the active procedural renderer;
- preserve the existing voice/slider UX and stored hue behavior unless Ryan explicitly asks to redesign it.

The protected branch `checkpoint-boop-unified-v65-procedural-eyes` remains reference/provenance for the finished eye work. Never repoint it.

## Durable visual-verification boundary

Ryan explicitly instructed that GitHub must not judge BOOP visuals. Keep CI to non-visual contracts, compilation/lint, functional tests, package/signature/integrity and security checks. Do not add screenshot comparisons, golden-image checks, visual diffing, pixel/geometry appearance assertions, animation judging or other automated claims that BOOP looks right. Exact approved binary/hash integrity checks are allowed because they verify locked source identity rather than appearance.

Physical appearance of the v67 finished-eye integration remains Ryan's acceptance gate. Required physical checks include the sclera/white blend, default cyan, live iris-only colour changes, reading motion without ghost/socket crescent, blink, and preservation of notification presentation.

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

Do not regenerate, recompress, recolor, crop or weaken the hash guard.

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

## Clean Shield HOME boundary

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Do not merge it into Unified until Ryan explicitly approves later. The v67 separate Shield HOME routing contract is green but that does not change the standalone product boundary.

## Permanent Home and assistant contracts

HA device names and Home controls are physically accepted and must not regress. Home remains room-scoped and fail-closed; room changes tear down previous-room ownership/state before rebuilding. Shield density scaling remains idempotent and never system-wide.

Assistant ownership remains explicit/reversible through supported Android routes. Shield remote assistant activation/audio remains separately unresolved. No overlay mic, competing recorder, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs focused non-visual tests, compilation/lint, package/signature/integrity checks and artifact upload. Ryan owns appearance, animation, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.

## Planned future work: BOOP screensaver `What If?`

Keep the already queued optional `What If?` BOOP screensaver plan. Default OFF. Primary personal target is Pixel C / future BOOP Mirror, with a simple `Off` / `What If?` selector and small idle-time choice. On idle, BOOP fades away to a black full-screen field and the recovered Silk-style opposing pulse animation owns the screen. BOOP returns immediately on touch, wake/voice activity or other deliberate interaction. Recreate the effect natively for current displays, preserve the recovered original ZIPs as reference masters, and handle long-running brightness/power responsibly.
