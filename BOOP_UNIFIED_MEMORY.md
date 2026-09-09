# BOOP unified memory

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Current canonical candidate: v70 developer lab + notification doods

v70 is versionCode `70`, versionName `1.2.24-unified-dev-menu-doods`. Final production/build commit is `825593a16c004d9c0825720eb014c4f5cc8e58af`. The app implementation landed at `2f4a62150121b299a433674e971de1e058f6330f`; `9c907d3497067eb88ea1308875084b8965413951` corrected only a brittle materialized-router test assertion. The final workflow-only commits make Unified release version bumps trigger the separate Shield HOME routing workflow.

v70 extends the internal dev surface from v69 into a real local BOOP test lab:

- spoken `dev menu` is recognized by `BoopDevMenuIntent` and intercepted in the local speech path before Home Assistant / command-router / chat fallback;
- no Chat Mode setup, OpenCode/ChatGPT Web or internet dependency is required to open it;
- `BoopDevMenuActivity` stays `exported=false`;
- BOOP Dev uses immersive/fullscreen black presentation with large scrollable/remote-friendly controls;
- animation actions are Wake, Think, Stop, Berry 1, Berry 2, Berry 3, Shake and Sleep, wired to the real current BOOP animation methods;
- notification demos are Facebook, WhatsApp, Gmail, X/Twitter, YouTube, Messenger, Instagram, Discord, Spotify, Reddit, Locked and Bundle;
- notification demos are local presentation models only, never real shade posts and never notification-runtime/listener calls;
- the real `BoopNotificationPuppetView` remains the renderer, using current procedural BOOP eyes and the exact locked five-finger yellow hands;
- dev-only service identity/icon treatment supplies recognizable app identity without baking the old concept-sheet face/hands into runtime art;
- Locked preview uses the production privacy-redaction model and exposes no message title/body before authentication;
- finite animations can be replayed; Think has an explicit Stop/reset path; returning from a dood returns cleanly to BOOP Dev.

The ChatGPT Library concept sheet `Glossy Boop App Icon Collection.png` was consulted as service identity/style direction only. The runtime renderer remains authoritative for BOOP's current eyes and hands.

Final release evidence: main workflow `34322564398` SUCCESS; Shield HOME routing workflow `34322564357` SUCCESS; artifact ID `10092558111`; artifact size `62,739,368` bytes; artifact ZIP SHA-256 `3c68ba78f2fb36bf50d6bbaf0d85a50a8a51dc5d345349b2dc70c32ae45c00e1`; APK SHA-256 `53c2956873e7a7268b829da5d9bd4f23d0f6ee20a0919051cb95bbd275f867a4`; permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Shield focused tests are 58/58 and Unified focused tests are 144/144, zero failures/errors/skips. Shield HOME routing passed.

The exact final artifact ZIP was independently downloaded and SHA-256 hashed and matched GitHub's artifact digest. The APK was independently extracted and SHA-256 hashed and matched the CI receipt. The APK v2 signing block was independently parsed; its embedded signer certificate SHA-256 matched both the CI signer receipt and the canonical permanent BOOP signer.

Test-first evidence: RED head `4c81770aa869a46352115572c0757ca0f9876847` / workflow `34321173070` failed on the deliberately missing v70 intent/action/identity classes. Implementation `2f4a62150121b299a433674e971de1e058f6330f` / workflow `34321836150` then exposed a test-only mismatch because the approved Chat Mode materializer rewrites the later router boundary to the guarded two-argument form. `9c907d3497067eb88ea1308875084b8965413951` corrected that assertion and workflow `34321947467` went green. Final exact-head workflows `34322564398` and `34322564357` are green.

**GitHub performed NO visual acceptance. Physical v70 acceptance is pending and belongs to Ryan.** No screenshot tests, golden-image tests, pixel comparisons or automated visual judgments were used. No v70 rollback checkpoint was created or repointed.

Detailed receipt: `docs/BOOP-V70-DEV-MENU-DOODS-RECEIPT.md`.

## Durable developer-lab rule

BOOP Dev is an internal testing surface, not a user-facing authority expansion.

- Keep `BoopDevMenuActivity` non-exported.
- Keep spoken `dev menu` local and ahead of HA/chat fallback.
- Animation demos should call real current BOOP behaviors rather than duplicate/rebuild them.
- Infinite/continuous previews need an explicit stop/reset path; finite ones should be replayable.
- Notification demos must remain local presentation fixtures. They must never create Android shade notifications, call `NotificationManager`, invoke the notification listener/runtime path, or require listener access merely to preview.
- Locked demo content must remain privacy-redacted through the production presentation model.
- Runtime doods must use the current procedural eyes and exact approved five-finger yellow hands. Concept sheets are reference/style direction only.
- The dev screen does not grant permissions, install anything, change package identity or alter the permanent signer.

## Durable finished-eye and hue rules

Ryan confirmed the v65 procedural-eye pass as the finished default-eye state where the sclera/whites looked right. Preserve this canonical order:

1. `scripts/patch-unified-reading-eyes.py` installs procedural Canvas iris/pupil/catchlight rendering and the procedural hue setter;
2. `scripts/patch-v64-procedural-sclera.py` covers the complete reading-motion envelope and removes the old socket/remnant crescent;
3. `scripts/patch-v65-feathered-sclera.py` hides the baked-in iris in the centre and feathers cleanup into the approved original grey sclera shading.

The approved black-lidded PNG remains the source for eye bodies/lids and must not be regenerated. The active renderer draws exactly one procedural iris/pupil/catchlight set per eye. Never restore a shifted PNG iris patch or stack a moving iris over a stationary iris.

Durable materialization guard from v68: after the procedural-eye stages run, **no later patch may rerun the legacy bitmap hue-cache setter**. In particular, `scripts/patch-unified-shield-dashboard.py` must never invoke `scripts/patch-unified-iris-cache.py` after procedural eyes are installed. Shield dashboard wiring and Wall hue rendering are separate concerns.

The user eye-colour control drives `proceduralIrisHueDegrees`; `irisColour()` consumes that hue. Default remains BOOP cyan/blue at 190 degrees. User-selected hue affects the procedural iris only. Sclera/whites, pupils, catchlights, black eyelids/accents and the rest of the approved artwork must not be tinted. Do not reintroduce whole-bitmap `ColorFilter` tinting or bitmap-wide recolouring for the active procedural renderer. Preserve the existing voice/slider UX and stored hue behavior unless Ryan asks to redesign it.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference eye provenance. Never repoint it.

## Durable visual-verification boundary

Ryan explicitly instructed that GitHub must not judge BOOP visuals. Keep CI to non-visual contracts, compilation/lint, functional tests, package/signature/integrity and security checks. Do not add screenshot comparisons, golden-image checks, visual diffing, pixel/geometry appearance assertions or animation judging. Exact locked-binary/hash identity checks are allowed because they verify source identity rather than appearance.

Physical appearance of v70 remains Ryan's acceptance gate, including fullscreen presentation, animation motion, app-specific dood identity, current eyes/hands, the carried v68 hue behavior and finished v65 sclera/white blend.

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

The exact approved notification hands binary is locked: path `unified/assets/boop-notifications/boop-yellow-hands-approved.png`, size `1,809,990` bytes, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`, Git blob `d47037271bf320f4f110e3f8416f59882062afac`. Do not regenerate, recompress, recolor/recolour, crop or weaken the hash guard.

## Durable wake architecture

BOOP permanently remains an accepted wake name. A custom name is additive, never a replacement. Custom-name training uses five local spoken examples from the existing single controller-owned 16 kHz PCM stream and stores only a compact pronunciation profile; raw enrolment PCM is not persisted. Never add a competing microphone listener.

Continuous phone wake is allowed while the phone is on any external power. Unpowered handheld behavior remains tap-to-talk. After normal TTS, wake re-arms only after BOOP finishes speaking. A post-wake Android command-ASR no-match or timeout is silent and performs a genuine re-arm. A hard wake-engine/microphone startup failure remains fail-safe latched.

`show diagnostics` stays hidden and pull-only. Do not auto-display diagnostics, persist raw audio or cloud-log diagnostic audio.

## Durable wake-to-command boundary

Preserve exactly the final 1,600 detector samples / 100 ms bridge into command ASR, continue the same live PCM stream immediately afterward, retain wake-prefix stripping as defensive parsing and keep wake-to-command handoff acoustically silent. Do not restore the old full one-second wake-history pre-roll.

Default/permanent BOOP uses zero intentional Sherpa trailing blanks. Ryan physically accepted natural `BOOP + command` without a deliberate pause. Learned custom names remain matchable while speech is active.

Protected rollback checkpoints:

- `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`;
- `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

Never repoint them. Latest physically accepted rollback remains v59. No v70 rollback checkpoint exists.

## Durable uncensored-speech rule from v59

Both ordinary tap-to-talk and post-wake command recognition request `RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS=false`. BOOP must not add its own profanity blacklist or replace recognized adult/profane speech with asterisks. Ryan physically accepted the spoken rename/training path on v59.

## Clean Shield HOME boundary

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Do not merge it into Unified until Ryan explicitly approves later.

The Unified CI contract now also runs the separate Shield HOME routing workflow whenever `unified/app-build.gradle` changes, so release version bumps cannot silently skip that gate.

## Permanent Home and assistant contracts

HA device names and Home controls are physically accepted and must not regress. Home remains room-scoped and fail-closed; room changes tear down previous-room ownership/state before rebuilding. Shield density scaling remains idempotent and never system-wide.

Assistant ownership remains explicit/reversible through supported Android routes. Shield remote assistant activation/audio remains separately unresolved. No overlay mic, competing recorder, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs focused non-visual tests, compilation/lint, package/signature/integrity checks and artifact upload. Ryan owns appearance, animation, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.

## Planned future work: BOOP screensaver `What If?`

Keep the already queued optional `What If?` BOOP screensaver plan. Default OFF. Primary personal target is Pixel C / future BOOP Mirror, with a simple `Off` / `What If?` selector and small idle-time choice. On idle, BOOP fades away to a black full-screen field and the recovered Silk-style opposing pulse animation owns the screen. BOOP returns immediately on touch, wake/voice activity or other deliberate interaction. Recreate the effect natively for current displays, preserve the recovered original ZIPs as reference masters, and handle long-running brightness/power responsibly.
