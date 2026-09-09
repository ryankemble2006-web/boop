# BOOP unified memory

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Current canonical candidate: v69 internal dev menu

v69 adds one internal developer/demo surface reachable from the existing Voice settings screen.

The **Animations** shelf calls the existing BOOP face behaviors directly: Wake, Think, Berry, Shake and Sleep. It does not introduce a replacement animation renderer.

The **Notification demos** shelf uses the production `BoopNotificationPuppetView` with local-only Unlocked, Locked and Bundle fixtures. `BoopDevNotificationPreview` is a pure presentation factory with no Android notification API or runtime dependency. Locked demo content flows through the production privacy-redaction model. The dev activity is declared `exported=false`.

v69 carries the v68 procedural hue-wire fix and the finished v65 procedural-eye/sclera stack forward unchanged. v68 had not yet been physically accepted when v69 was built.

Built code head `709c74eb39d28c0d894661e5bde66da18f9ea6cf`, version 69 / `1.2.23-unified-dev-menu`, main workflow `34317400589` SUCCESS, separate Shield HOME routing workflow `34317400631` SUCCESS, artifact ID `10090644503`, artifact digest `sha256:af14acccfa0ae730a1254f518f2210a46645d45fdcd6d2056f9aa4fb4b9449a9`, APK SHA-256 `31da93c3fdfd7116b8bc9b083fd947dadc5952a77c5c67c5d3808b99f0c57f88`, permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Fresh signed-artifact verification: Shield focused tests 58/58 and Unified focused tests 140/140 with zero failures/errors/skips. Non-visual integration contracts, canonical materialization, internal-only/local-only dev plumbing, notification presenter/manifest contracts, seamless wake handoff, Launcher preservation, signed assembly, package/version/signer/archive integrity and artifact upload passed. The downloaded artifact was independently extracted and re-hashed and matched the CI APK receipt exactly.

Test-first evidence:

- `3bb0aadb8bc15df283be93202853f273a432d467` / workflow `34316174381` failed because the new shelf test referenced missing `BoopDevMenuModel`;
- `b32a328f09a7f5d4c347e48986b685da18afd072` / workflow `34316614087` failed because the preview tests referenced missing `BoopDevNotificationPreview`;
- `f571345b1ac68980d7877dc028e597d884d74812` / workflow `34317008388` failed because `.BoopDevMenuActivity` had not yet been added to the materialized manifest;
- final implementation `709c74eb39d28c0d894661e5bde66da18f9ea6cf` passed the complete canonical workflow.

**No visual acceptance was performed by GitHub.** Ryan owns screenshot/appearance/layout/animation/device judgment. v69 is CI/signer green, not physically accepted. Do not create or repoint a v69 rollback checkpoint until he explicitly accepts this exact signed APK.

Detailed receipt: `docs/BOOP-V69-DEV-MENU-RECEIPT.md`.

## Durable dev-menu rule from v69

BOOP Dev is an internal testing surface, not a user-facing authority expansion.

- Keep `BoopDevMenuActivity` non-exported.
- Animation demos should call the real existing BOOP face behaviors rather than duplicate/rebuild them.
- Notification demos must remain local presentation fixtures. They must not create Android shade notifications or call the notification listener/runtime path.
- Locked demo content must remain privacy-redacted through the same production presentation model.
- The dev screen does not grant permissions, install anything, change package identity or alter the permanent signer.

## Durable finished-eye and hue rules

Ryan confirmed the v65 procedural-eye pass as the finished default-eye state where the sclera/whites looked right. Preserve this canonical order:

1. `scripts/patch-unified-reading-eyes.py` installs procedural Canvas iris/pupil/catchlight rendering and the procedural hue setter;
2. `scripts/patch-v64-procedural-sclera.py` covers the complete reading-motion envelope and removes the old socket/remnant crescent;
3. `scripts/patch-v65-feathered-sclera.py` hides the baked-in iris in the centre and feathers cleanup into the approved original grey sclera shading.

The approved black-lidded PNG remains the source for eye bodies/lids and must not be regenerated. The active renderer draws exactly one procedural iris/pupil/catchlight set per eye. Never restore a shifted PNG iris patch or stack a moving iris over a stationary iris.

Durable materialization guard from v68: after the procedural-eye stages run, **no later patch may rerun the legacy bitmap hue-cache setter**. In particular, `scripts/patch-unified-shield-dashboard.py` must never invoke `scripts/patch-unified-iris-cache.py` after procedural eyes are installed. Shield dashboard wiring and Wall hue rendering are separate concerns.

The user eye-colour control drives `proceduralIrisHueDegrees`. `irisColour()` consumes that hue. Default remains BOOP cyan/blue at 190 degrees. User-selected hue affects the procedural iris only. Sclera/whites, pupils, catchlights, black eyelids/accents and the rest of the approved artwork must not be tinted. Do not reintroduce whole-bitmap `ColorFilter` tinting or bitmap-wide recolouring for the active procedural renderer. Preserve the existing voice/slider UX and stored hue behavior unless Ryan asks to redesign it.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference eye provenance. Never repoint it.

## Durable visual-verification boundary

Ryan explicitly instructed that GitHub must not judge BOOP visuals. Keep CI to non-visual contracts, compilation/lint, functional tests, package/signature/integrity and security checks. Do not add screenshot comparisons, golden-image checks, visual diffing, pixel/geometry appearance assertions or animation judging. Exact locked-binary/hash identity checks are allowed because they verify source identity rather than appearance.

Physical appearance of v69 remains Ryan's acceptance gate. This includes the dev-menu animation demos, the carried v68 hue behavior and the finished v65 sclera/white blend.

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

The exact approved notification hands binary is locked: size `1,809,990` bytes, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`, Git blob `d47037271bf320f4f110e3f8416f59882062afac`, canonical path `unified/assets/boop-notifications/boop-yellow-hands-approved.png`. Do not regenerate, recompress, recolor, crop or weaken the hash guard.

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

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Do not merge it into Unified until Ryan explicitly approves later.

## Permanent Home and assistant contracts

HA device names and Home controls are physically accepted and must not regress. Home remains room-scoped and fail-closed; room changes tear down previous-room ownership/state before rebuilding. Shield density scaling remains idempotent and never system-wide.

Assistant ownership remains explicit/reversible through supported Android routes. Shield remote assistant activation/audio remains separately unresolved. No overlay mic, competing recorder, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs focused non-visual tests, compilation/lint, package/signature/integrity checks and artifact upload. Ryan owns appearance, animation, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.

## Planned future work: BOOP screensaver `What If?`

Keep the already queued optional `What If?` BOOP screensaver plan. Default OFF. Primary personal target is Pixel C / future BOOP Mirror, with a simple `Off` / `What If?` selector and small idle-time choice. On idle, BOOP fades away to a black full-screen field and the recovered Silk-style opposing pulse animation owns the screen. BOOP returns immediately on touch, wake/voice activity or other deliberate interaction. Recreate the effect natively for current displays, preserve the recovered original ZIPs as reference masters, and handle long-running brightness/power responsibly.
