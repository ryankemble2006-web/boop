# BOOP unified memory

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Current canonical candidate: v70 pinned-face / raised-banner + Android tablet routing

v70 remains versionCode `70`, versionName `1.2.24-unified-dev-menu-doods`.

## Durable Android tablet routing

As of canonical app/test head `bd878606809302de1b871e6c62d8ce905346e766`, Unified BOOP treats non-TV Android devices with `smallestScreenWidthDp >= 600` as Wall devices. Preserve the routing order:

- explicit profile override first;
- Android TV / Leanback / television mode -> `SHIELD`;
- Pixel 7 Pro -> `WALL`;
- other non-TV Android devices at 600dp or wider -> `WALL`;
- sub-600dp handhelds -> `LAUNCHER`.

The Xiaomi Pad 7 Pro therefore uses the Wall body without a Xiaomi-specific model hardcode. Existing `BoopFaceView` and `BoopEyeLayout` already calculate presentation from live view dimensions and distinguish portrait from landscape, so tablet support must not fork, regenerate or reinterpret BOOP's approved eyes or add tablet-specific visual assets.

Test-first and signed-build receipt:

- RED test-only commit `db747c3c3e95acbc3ecae773daa451e6bd3eedc3` failed because the previous resolver had no width-aware overload;
- GREEN implementation before rebase `cf4c6918956f9cdb79f9b97f479c8a0c0d1de45f`;
- canonical app/test head `bd878606809302de1b871e6c62d8ce905346e766`;
- workflow `34395085823`: SUCCESS;
- artifact `BOOP-Unified`, ID `10121327367`, size `62,739,601` bytes;
- artifact ZIP SHA-256 `6dda05bea0ebe78b2239026e813f76220c48405969a95a342820ef2b79ad2395`;
- APK SHA-256 `0955dbb51ffab6707f11df02bf3966fc90ce3d73f204b74a349519d4937b4612`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused tests 58/58 and Unified focused tests 148/148, zero failures/errors/skips.

The exact artifact ZIP was independently downloaded and matched GitHub's digest; the extracted APK matched the workflow receipt. Tablet routing is CI/signer green only until Ryan launches this exact build on the Xiaomi Pad 7 Pro. No v70 rollback checkpoint exists yet; latest fully physically accepted rollback remains v59.

Ryan physically confirmed the current in-place `developer menu` entry works on the Pixel without closing BOOP, and then confirmed the pinned-face concept with `Awesome now I can see him`. Preserve the Voice Settings vertical-scroll repair, in-place route, and pinned visible BOOP model while animation controls are browsed. The older activity-hop candidate `95224d95a04b6a79a8d8c4e9e5ac4c35cf64ca9a` remains physically failed for that bug.

The durable developer-menu entry contract remains:

- exact spoken trigger: `developer menu`;
- former `dev menu` phrase intentionally does not match;
- spoken trigger remains local and ahead of Home Assistant / command-router / chat fallback;
- active spoken/settings route stays inside the existing `MainActivity` and calls `showDeveloperMenu()`;
- the route must not call `startActivity()` or reference `BoopDevMenuActivity.class`;
- BOOP Dev is a fullscreen overlay on the existing `interactionSurface`;
- Voice Settings remains vertically scrollable and its row is labelled `Developer menu`;
- exiting the overlay returns to BOOP without changing package, permissions, signer or authority.

The original `BoopDevMenuActivity` remains non-exported and may remain packaged for provenance/compatibility, but it is not the active spoken/settings route. Do not restore that activity hop without new physical evidence.

## Durable developer-lab selector contract

Ryan corrected the developer-lab design on 2026-09-09. Preserve this interaction unless he explicitly changes it:

- BOOP's real current `BoopFaceView` stays pinned and visible while animation controls are browsed;
- the developer-lab page itself must not scroll vertically;
- `Animations` is a horizontal right-to-left selector below the pinned face;
- swiping the animation selector changes visible controls without moving BOOP off-screen;
- tapping `Wake`, `Think`, `Stop`, `Berry 1`, `Berry 2`, `Berry 3`, `Shake`, or `Sleep` calls the real current behavior directly on that same pinned face;
- animation selection must not clear the developer overlay, create another face, open a full-screen animation page, or require a Dismiss return step;
- selector horizontal position may be retained as controls are browsed;
- `Notification doods` remains a separate horizontal selector. A dood preview may replace the selector temporarily, but must use a dedicated current `BoopFaceView` above the real `BoopNotificationPuppetView` banner/card and exact approved hands;
- `BoopNotificationPuppetView.setFaceVisible(false)` is a dev-composition hook to suppress only the duplicate internal face;
- notification privacy/tap/dismiss/cue semantics remain unchanged;
- visual spacing, motion and whether the approved/current eyes read clearly remain physical acceptance questions for Ryan, never CI appearance assertions.

The prior full-screen animation preview + bottom Dismiss design is rejected and must not be restored accidentally.

All notification doods remain local presentation fixtures. They must never create Android shade notifications, call `NotificationManager`, invoke notification listener/runtime paths or require listener access merely to preview. Locked preview remains privacy-redacted.

## Durable emphasized notification-puppet pose

Ryan approved a shared notification-puppet layout trial on 2026-09-09 after asking for more obvious hands and a higher held banner on all notification doods.

Preserve these exact implementation semantics until Ryan physically tunes or changes them:

- the approved notification-hands PNG remains byte-for-byte unchanged;
- shared `BoopNotificationPuppetView` uses a `1.12f` hands resting scale;
- the banner/card rests `36dp` above its previous center position;
- banner entrance retains the same relative `16dp` approach, so it starts `52dp` above the old center and settles at `36dp` above the old center over the existing `260ms` `OvershootInterpolator(0.7f)` animation;
- hands entrance retains the same relative `0.96 -> 1` motion, now from `1.12 * 0.96` to `1.12` over the existing `220ms`;
- this pose is shared by Dev Lab dood previews and real notification puppet surfaces because it lives in `source/BoopNotificationPuppetView.java`;
- do not alter eyes, notification card content, privacy, tap/open, swipe/timeout dismiss, alert-cue decisions, package, permissions or signer as part of this pose;
- physical visual acceptance remains Ryan's responsibility. CI may verify the semantic pose mode and locked asset identity, but must not judge pixels, visual geometry or aesthetics.

The exact source marker for this mode is `BOOP_NOTIFICATION_PUPPET_EMPHASIZED_POSE_V1`.

### Current verification receipt

Test-first evidence for the emphasized notification pose:

- RED head `9ecf556de5545eef19a73e490ccf5a6989ca1e85`, workflow `34392830481`: failed at the non-visual integration-contract gate before materialization/signing because the new semantic pose marker did not yet exist;
- exact GREEN app/test head `c68a7aba8f0c9bcffa81ad0b517453cc8e50b12d`;
- workflow `34392969200`: SUCCESS.

Artifact receipt:

- artifact `BOOP-Unified`, ID `10120505065`, size `62,740,052` bytes;
- artifact ZIP SHA-256 `de64be9f30d3aa54eb69b6d662e326f70ec56f55776efcf70c05502c4cef8608`;
- APK SHA-256 `ab91846489799be9f6d7c8e38fb8c51925f6a983de2c6f2bb0cbdec676d14235`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused tests 58/58;
- Unified focused tests 144/144;
- zero failures/errors/skips.

The exact artifact ZIP was independently downloaded and matched GitHub's digest. The extracted APK matched `apk-sha256.txt`; `built-commit.txt` matched `c68a7aba8f0c9bcffa81ad0b517453cc8e50b12d`; `badging.txt` confirmed `com.boop.alpha1`, versionCode 70, versionName `1.2.24-unified-dev-menu-doods`; signer receipt matched the permanent BOOP signer.

The in-place developer-menu and pinned-face visibility behaviors have positive Pixel evidence. The emphasized-hands / raised-banner notification pose remains physically pending. No v70 rollback checkpoint exists. Latest fully physically accepted rollback remains v59.

### Prior pinned-face receipt

- RED head `6ada374665a9cc6d504a7188f4df1b406fffdcc6`, workflow `34391024568`;
- GREEN app/test head `a9e4e6a8f6abf023bc9ba1779d0a51f698ee0c3f`, workflow `34391151333` SUCCESS;
- artifact ID `10119809751`, APK SHA-256 `b06d4c2dd5c6b6fa2dac969b7406c401195b01b961263e418eff5101b75b552e`.

## Original v70 developer-lab lineage

Original v70 final production/build commit: `825593a16c004d9c0825720eb014c4f5cc8e58af`. Original app implementation: `2f4a62150121b299a433674e971de1e058f6330f`. Materialized-router assertion correction: `9c907d3497067eb88ea1308875084b8965413951`.

The concept sheet `Glossy Boop App Icon Collection.png` was service identity/style direction only. Runtime doods remain authoritative to current procedural BOOP eyes and the exact approved five-finger yellow hands.

## Durable developer-lab rule

BOOP Dev is an internal testing surface, not a user-facing authority expansion.

- Keep the current `developer menu` spoken route local and ahead of HA/chat fallback.
- Keep current spoken/settings entry in-place inside `MainActivity` unless Ryan physically approves a different architecture.
- Animation demos call real current BOOP behaviors rather than duplicate/rebuild them.
- Infinite/continuous animation needs an explicit `Stop` control; finite animations should be replayable.
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
- Current approved trial entrance semantics are: banner approaches from `16dp` above its raised resting position over `260ms` with `OvershootInterpolator(0.7f)`; hands animate from `0.96x` of their current resting scale to their current resting scale over `220ms`. The resting pose is currently hands `1.12x` and banner `36dp` upward, pending Ryan's physical acceptance.
- Local cue is deterministic 320ms audio plus `{0,35,55,28}` vibration.
- BOOP sound/vibration is allowed only when coordinator `playCue=true` and the native Android channel is known silent for both sound and vibration. Unknown/noisy channels stay visual-only to prevent double alerts.
- Preserve overlay/screen-on/vibrate authority while rejecting full-screen-intent, query-all, accessibility-service and device-admin authority.

Exact approved notification hands binary: `unified/assets/boop-notifications/boop-yellow-hands-approved.png`, size `1,809,990` bytes, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`, Git blob `d47037271bf320f4f110e3f8416f59882062afac`. Do not regenerate, recompress, recolor/recolour, crop or weaken the hash guard. Explicitly approved transforms/layout changes may pose this exact asset without modifying the binary.

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

Never repoint them. Latest fully physically accepted rollback remains v59.

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