# BOOP unified handoff

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Always re-fetch live `boop-unified`, `boop-unified-notifications` and `main` before notification edits and preserve concurrent work.

## In-progress notification presenter: branch is code-complete through Task 10, release BLOCKED by exact hand binary

Active implementation branch: `boop-unified-notifications`.

Reviewed code head before this handoff update: `b38f62b2316f5eeb0d00f5d6696fecb92b92d661`.

The notification branch now contains the reviewed presenter/runtime work through Tasks 8-10 of `docs/superpowers/plans/2026-09-08-boop-notification-presenter.md`:

- one reusable `BoopNotificationPuppetView` on in-place Wall, unlocked overlay and locked presentation surfaces;
- locked presentation remains privacy-safe and only exposes app identity/icon/count before authentication;
- tap preserves the source notification `PendingIntent`; successful `FLAG_AUTO_CANCEL` taps mirror cancellation only after the source send succeeds;
- swipe/timeout dismiss BOOP's mirror only and leave Android's shade notification authoritative;
- swipe contract is >=72dp on either dominant axis with strict >1.25x dominance;
- approved entrance recipe is restored: card alpha/translation from -16dp over 260ms with `OvershootInterpolator(0.7f)`, hands scale 0.96 -> 1 over 220ms;
- local deterministic 320ms BOOP notification cue + one `{0,35,55,28}` vibration waveform exists;
- cue policy only permits BOOP sound/vibration for coordinator `playCue=true` on a channel whose native sound and vibration are both known-silent; unknown/noisy channels stay visual-only to avoid double alerts;
- manifest contract now includes overlay/screen-on/vibrate authority while explicitly rejecting full-screen-intent, query-all, accessibility-service and device-admin authority;
- canonical CI derives expected version code/name from `unified/app-build.gradle` instead of hardcoded v62 strings.

Pure-Java local verification performed in chat after the above code changes: renderer/policy/swipe harness PASS. At 44.1 kHz the cue is exactly 14,112 samples, deterministic, peak 12,814; silent/noisy/unknown channel policy and horizontal/vertical/diagonal gesture cases all passed. This is not an Android build or physical/acoustic acceptance.

### Exact blocker, do not bypass

GitHub Actions run `34300985129` for code head `b38f62b2316f5eeb0d00f5d6696fecb92b92d661` correctly stops at `Check non-visual integration contracts` before materialization/build.

`unified/assets/boop-notifications/boop-yellow-hands-approved.png` currently points to the wrong Git blob `7cb914516a829a0b824febd16957a1a1dd9c6a62`, recorded at 1,541,931 bytes. The similarly named root source on `animation-freddie-mercury` is the same wrong blob. Do not weaken/remove the hash guard to get a build.

Two independent archived originals in Ryan's ChatGPT Library were materialized and hashed byte-for-byte. Both are 1,809,990 bytes and SHA-256:

`26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`

The exact approved file's Git blob SHA-1 is:

`d47037271bf320f4f110e3f8416f59882062afac`

The connected GitHub tool has no binary/file upload action, so it cannot move that 1.81 MB Library binary into the repository without an external byte-preserving upload. One manual replacement of the notification-branch asset is sufficient: once the exact blob exists in the repository object store, future GitHub-side repair can point the animation source branch at the same blob without another upload.

Next continuation sequence:

1. byte-for-byte replace `unified/assets/boop-notifications/boop-yellow-hands-approved.png` on `boop-unified-notifications` with the 1,809,990-byte approved original;
2. re-fetch live branch and verify the path's Git blob SHA is exactly `d47037271bf320f4f110e3f8416f59882062afac` before trusting CI;
3. repair `animation-freddie-mercury:boop-yellow-hands-approved.png` to that same existing Git blob;
4. require the exact notification-branch GitHub Actions run to get through asset integrity, materialization, notification JUnit, manifest contract and signed APK build;
5. fix any real Android compile/test failure found after the asset gate;
6. only after full green, re-fetch canonical `boop-unified`/`main`, increment the live unified version exactly once, publish the reviewed release candidate, verify signer/artifact hashes, then record evidence in status/memory;
7. Ryan owns the final Pixel visual/lock-screen/acoustic acceptance. Do not create a physical rollback checkpoint before that acceptance.

## Current signed candidate: v62 single-layer reading eyes

Ryan physically tested v61 and liked the reading-style listening motion, but could visibly see the stationary original pupil/iris behind the moving gaze. Source tracing confirmed v61 drew the normal approved eye first and then painted a shifted iris/pupil patch on top. Treat v61 as physically rejected for compositing and do **not** create a v61 checkpoint.

v62 keeps the exact approved black-lidded eye master, v61's stronger listening zoom/read sweep and widened iris hue coverage, but changes the compositing boundary only. While active listening, the face is drawn into a temporary layer, the stationary iris aperture is cleared, and one shifted iris/pupil patch from the same runtime bitmap is drawn into that aperture. The intended result is one visible moving gaze rather than PNG-on-PNG ghosting.

No wake detector, learned-name matcher, five-sample enrolment, microphone ownership, exact 100 ms bridge, command window, powered wake/recovery, diagnostics, HA routing, TTS, blink, Launcher, Shield behavior, package or signer is intentionally changed.

### v62 TDD / build receipt

RED:

- anti-ghost regression commit `7b19f2c81f0fa2a6cb7a3512186298b3bdf4b5e4`
- workflow `34265189069`
- 97 focused unified tests ran; exactly the new single-layer compositing test failed while established tests passed.

Repair/release:

- policy commit `b54e6fad8dc30e4f90ff13a126038053d8d73881`
- renderer commit `d7e4632ab014b026459fd63d8d9d17a8fd9dc16f`
- version bump `55753ff70428a35b7b3f6d9da668b01e358fcb62`
- built/verifier commit `6877bf3d97d069eda950938060e355da039d53cf`
- version 62 / `1.2.16-unified-single-layer-reading-eyes`
- workflow `34265615662` SUCCESS
- artifact `BOOP-Unified`, ID `10071797863`
- artifact digest `sha256:adb58b5eb0373fa1b581dccd625638a6bcaf151477215b57c930197ed52142ef`
- APK SHA-256 `5def47113929e6b0aa59b868e5880056607473fb3ba1ff4e54ac3f771bb7bc3b`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- wake-handoff contracts 3/3 PASS
- Shield focused tests 58/58, zero failures/errors/skips
- unified focused tests 97/97, zero failures/errors/skips
- Launcher lint, signed assembly, package/version, manifest, permanent signer, APK ZIP integrity and artifact upload PASS.

Detailed receipt: `docs/BOOP-V62-SINGLE-LAYER-READING-EYES-RECEIPT.md`.

CI/signer green. Physical visual acceptance is pending and belongs to Ryan.

## Required next Pixel test

Install v62 over v61 without uninstalling. Trigger listening both from a natural wake-name command and tap-to-talk. Confirm there is only one visible moving pupil/iris per eye with no stationary ghost underneath, the reading sweep still looks good, the previously missed blue iris regions follow the selected hue, and natural BOOP/custom wake commands still work. Do not create a v62 checkpoint until Ryan physically accepts this exact signed build.

## v59 uncensored speech is physically accepted

Ryan installed v59 and physically confirmed the adult/profane spoken rename path works: the actual word survives recognition, BOOP asks for the five repetitions using the real word rather than asterisks, and training completes. Treat the Android profanity-masking defect as physically passed on this Pixel.

Exact v59 built code:

- built commit `136b56e6faac8ce450b957ac3057a379c68c7b7b`
- workflow `34257117357` SUCCESS
- APK SHA-256 `7d48cc77407b69428bd2456b80cefbe56cb60f6f8326eb2e3686aa7b22bc7a2e`

Protected v59 rollback:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

Never repoint it.

v58 natural-wake rollback remains:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Never repoint it either. The older v48 wake-arm rollback also remains permanently pinned at `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

## Durable wake/name contracts

- `BOOP` permanently remains an accepted fallback wake name; custom names are additive, never replacements.
- five custom-name samples are local on the existing single controller-owned 16 kHz PCM stream; raw enrolment PCM is not persisted.
- learned custom names are matchable while speech is active without trailing silence.
- default BOOP uses zero intentional Sherpa trailing blanks so natural `BOOP + command` remains seamless.
- never add a competing microphone listener or restore the old full one-second wake-history pre-roll.
- keep the exact final 1,600 detector samples / 100 ms command bridge unless new physical evidence justifies redesign.
- wake-to-command handoff remains acoustically silent.
- any external power permits continuous phone wake; unpowered phone remains tap-to-talk.
- after TTS, re-arm only after BOOP finishes speaking.
- no-match/timeout failures are silent and genuinely re-arm; hard wake-engine startup failures remain fail-safe latched.
- `show diagnostics` remains pull-only.
- BOOP does not intentionally censor recognized adult/profane speech; both recognition intents request unmasked offensive words.
- listening feedback must reuse the exact approved eye master and be driven by active recognizer state, not by a replacement pose image.
- moving listening gaze must replace the stationary iris/pupil aperture rather than stack a second eye layer over it.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eyes, iris-only hue, blink timing/gates, headphones/puppetry and five-digit yellow hands. Preserve physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. Assistant remote invocation/audio remains separately unresolved. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
