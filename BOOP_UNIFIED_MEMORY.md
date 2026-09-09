# BOOP unified memory

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Current canonical candidate: v70 in-place developer-menu hotfix

v70 remains versionCode `70`, versionName `1.2.24-unified-dev-menu-doods`.

Ryan physically confirmed that the earlier activity-hop hotfix at `95224d95a04b6a79a8d8c4e9e5ac4c35cf64ca9a` still forced BOOP to close when he said `dev menu`. Treat that candidate as physically failed for this bug despite its green CI. Preserve only its successful Voice Settings vertical-scroll repair.

The durable developer-menu entry contract is now:

- exact spoken trigger: `developer menu`;
- former `dev menu` phrase intentionally does not match;
- spoken trigger remains local and ahead of Home Assistant / command-router / chat fallback;
- active spoken/settings route stays inside the existing `MainActivity` and calls `showDeveloperMenu()`;
- the route must not call `startActivity()` or reference `BoopDevMenuActivity.class`;
- BOOP Dev is a fullscreen overlay on the existing `interactionSurface`;
- Voice Settings remains vertically scrollable and its row is labelled `Developer menu`;
- exiting the overlay returns to BOOP without changing package, permissions, signer or authority.

The original `BoopDevMenuActivity` remains non-exported and may remain packaged for provenance/compatibility, but it is no longer the current spoken/settings route. Do not restore that activity hop without new physical evidence.

The in-place developer lab preserves the v70 behavior: Wake, Think, Stop, Berry 1/2/3, Shake, Sleep and local notification previews for Facebook, WhatsApp, Gmail, X/Twitter, YouTube, Messenger, Instagram, Discord, Spotify, Reddit, Locked and Bundle. Previews use the production-style puppet presentation locally, never post Android shade notifications, never call the notification runtime/listener path and never require listener access merely to preview. Locked preview remains privacy-redacted.

Exact CI/signer-green app/test head: `c17e98d9a09471cf8f53f2bee171a77e3b3b1203`.

Workflow `34385817960` completed SUCCESS. Developer-menu materialization and JUnit phrase contracts passed, as did seamless wake handoff, Launcher preservation/lint, Shield controls, wake/routing/lifecycle/assistant policy, signed APK assembly and package/permanent-signer/archive verification. Shield focused tests: 58/58; Unified focused tests: 144/144; zero failures/errors/skips.

Artifact `BOOP-Unified` ID `10117795236`, size `62,741,119` bytes, artifact ZIP SHA-256 `5f11f925c097b67f1650fcb445918148e633071739b23245c54742d984c20faf`, APK SHA-256 `c45962533574b9a0ef5bd08ad3f785c94668e9ec793ed6377967d7cae5195a20`, permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. The artifact ZIP was independently downloaded and matched GitHub's digest; the extracted APK and signer receipts matched CI.

Detailed receipt: `docs/BOOP-V70-DEVELOPER-MENU-IN-PLACE-HOTFIX-RECEIPT.md`.

**Physical acceptance remains pending and belongs to Ryan.** Next real-device check is exact phrase `developer menu`, Voice Settings -> `Developer menu`, exit/re-enter, and wake/microphone health. No v70 rollback checkpoint exists. Latest physically accepted rollback remains v59.

## Original v70 developer-lab lineage

Original v70 final production/build commit: `825593a16c004d9c0825720eb014c4f5cc8e58af`. Original app implementation: `2f4a62150121b299a433674e971de1e058f6330f`. Materialized-router assertion correction: `9c907d3497067eb88ea1308875084b8965413951`.

The concept sheet `Glossy Boop App Icon Collection.png` was service identity/style direction only. Runtime doods remain authoritative to current procedural BOOP eyes and the exact approved five-finger yellow hands.

## Durable developer-lab rule

BOOP Dev is an internal testing surface, not a user-facing authority expansion.

- Keep the current `developer menu` spoken route local and ahead of HA/chat fallback.
- Keep current spoken/settings entry in-place inside `MainActivity` unless Ryan physically approves a different architecture.
- Animation demos call real current BOOP behaviors rather than duplicate/rebuild them.
- Infinite/continuous previews need an explicit stop/reset path; finite ones should be replayable.
- Notification demos remain local presentation fixtures and must never create Android shade notifications, call `NotificationManager`, invoke notification listener/runtime paths or require listener access merely to preview.
- Locked demo content remains privacy-redacted through the production presentation model.
- Runtime doods use current procedural eyes and exact approved five-finger yellow hands. Concept sheets are reference/style direction only.
- The dev screen does not grant permissions, install anything, change package identity or alter the permanent signer.

## Durable finished-eye and hue rules

Ryan confirmed the v65 procedural-eye pass as the finished default-eye state where the sclera/whites looked right. Preserve this canonical order:

1. `scripts/patch-unified-reading-eyes.py` installs procedural Canvas iris/pupil/catchlight rendering and the procedural hue setter;
2. `scripts/patch-v64-procedural-sclera.py` covers the complete reading-motion envelope and removes the old socket/remnant crescent;
3. `scripts/patch-v65-feathered-sclera.py` hides the baked-in iris in the centre and feathers cleanup into the approved original grey sclera shading.

The approved black-lidded PNG remains the source for eye bodies/lids and must not be regenerated. The active renderer draws exactly one procedural iris/pupil/catchlight set per eye. Never restore a shifted PNG iris patch or stack a moving iris over a stationary iris.

After the procedural-eye stages run, no later patch may rerun the legacy bitmap hue-cache setter. In particular, `scripts/patch-unified-shield-dashboard.py` must never invoke `scripts/patch-unified-iris-cache.py` after procedural eyes are installed.

The user eye-colour control drives `proceduralIrisHueDegrees`; `irisColour()` consumes that hue. Default remains BOOP cyan/blue at 190 degrees. User-selected hue affects the procedural iris only. Sclera/whites, pupils, catchlights, black eyelids/accents and the rest of the approved artwork must not be tinted. Preserve the existing voice/slider UX and stored hue behavior unless Ryan asks to redesign it.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference eye provenance. Never repoint it.

## Durable visual-verification boundary

Ryan explicitly instructed that GitHub must not judge BOOP visuals. Keep CI to non-visual contracts, compilation/lint, functional tests, package/signature/integrity and security checks. Do not add screenshot comparisons, golden-image checks, visual diffing, pixel/geometry appearance assertions or animation judging. Exact locked-binary/hash identity checks are allowed because they verify source identity rather than appearance.

Physical appearance, animation, device and acoustic acceptance remain Ryan's gate.

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

Exact approved notification hands binary: `unified/assets/boop-notifications/boop-yellow-hands-approved.png`, size `1,809,990` bytes, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`, Git blob `d47037271bf320f4f110e3f8416f59882062afac`. Do not regenerate, recompress, recolor/recolour, crop or weaken the hash guard.

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

Never repoint them. Latest physically accepted rollback remains v59.

## Durable uncensored-speech rule from v59

Both ordinary tap-to-talk and post-wake command recognition request `RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS=false`. BOOP must not add its own profanity blacklist or replace recognized adult/profane speech with asterisks. Ryan physically accepted the spoken rename/training path on v59.

## Clean Shield HOME boundary

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Do not merge it into Unified until Ryan explicitly approves later.

The Unified CI contract also runs the separate Shield HOME routing workflow whenever `unified/app-build.gradle` changes, so release version bumps cannot silently skip that gate.

## Permanent Home and assistant contracts

HA device names and Home controls are physically accepted and must not regress. Home remains room-scoped and fail-closed; room changes tear down previous-room ownership/state before rebuilding. Shield density scaling remains idempotent and never system-wide.

Assistant ownership remains explicit/reversible through supported Android routes. Shield remote assistant activation/audio remains separately unresolved. No overlay mic, competing recorder, Google-disable/default hacks, Button Mapper, privileged ADB ownership or direct OpenAI API dependency.

GitHub performs focused non-visual tests, compilation/lint, package/signature/integrity checks and artifact upload. Ryan owns appearance, animation, device and acoustic acceptance. Keep package `com.boop.alpha1`, permanent signer, credentials and private device data protected. No automatic installs/grants.

## Planned future work: BOOP screensaver `What If?`

Keep the already queued optional `What If?` BOOP screensaver plan. Default OFF. Primary personal target is Pixel C / future BOOP Mirror, with a simple `Off` / `What If?` selector and small idle-time choice. On idle, BOOP fades away to a black full-screen field and the recovered Silk-style opposing pulse animation owns the screen. BOOP returns immediately on touch, wake/voice activity or other deliberate interaction. Recreate the effect natively for current displays, preserve the recovered original ZIPs as reference masters, and handle long-running brightness/power responsibly.
