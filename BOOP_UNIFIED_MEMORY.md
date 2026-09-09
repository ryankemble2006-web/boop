# BOOP unified memory

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Fresh `main` owns shared contracts. Re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Current canonical candidate: v87 Kokoro JNI crash fix

Exact app/test release head before documentation-only follow-up commits:

`c046309cece7a4f4abc7e742190c0adb262c1c44`

Release identity:

- versionCode `87`;
- versionName `1.2.87-unified-kokoro-jni-crash-fix`;
- package `com.boop.alpha1`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Canonical workflow `34416346339`: SUCCESS. Shield HOME routing workflow `34416346368`: SUCCESS. Artifact `BOOP-Unified`, ID `10129329924`, size `63,993,437` bytes. Artifact ZIP SHA-256 `86077df44d1ee78582b5cac762458673545a2dec5cee71388fc71fea0308e420`. APK SHA-256 `ae1aeb5f73341c0b4b68ea3ab019d107604b2dc82182b0857c0e74fb0f8e77d8`. Shield focused tests `58/58`; Unified focused tests `155/155`; zero failures/errors/skips.

v86 is physically rejected. v87 is CI/signer green only until Ryan physically accepts the exact APK. No v87 rollback checkpoint exists. Latest fully physically accepted rollback remains v59.

## Durable natural-voice contract

Natural voices are optional, local/offline after one in-app download, and do not use an OpenAI API, account, subscription or cloud TTS.

Pinned pack: Sherpa-ONNX Kokoro `kokoro-multi-lang-v1_0` from the pinned HTTPS release asset. BOOP verifies the pinned archive byte size and SHA-256 before activation and stores the extracted pack in app-private storage.

Exact user voice order and speaker IDs:

1. Emma: key `bf_emma`, speaker `21`;
2. Isabella: key `bf_isabella`, speaker `22`;
3. George: key `bm_george`, speaker `26`;
4. Fable: key `bm_fable`, speaker `25`.

Installing the pack must not silently change the selected voice. Android TextToSpeech remains available as the fallback for ordinary BOOP speech. Existing Android/local voices remain available. Spoken `change voice` cycles the four natural voices only when the natural backend is selected and usable; otherwise it keeps the Android voice behavior. Existing speech-rate control maps to Kokoro speed and existing pitch maps to local playback pitch.

### Durable v86 selector/demo behavior

Ryan originally reported on v85 that pressing Emma / Isabella / George / Fable spoke whichever Android TTS voice was selected.

Preserve the selector/demo repair:

- on startup, if the current natural pack is already installed and passes `isInstalled()`, restore the controller's verified pack state with the current manifest version;
- if no valid pack is installed, leave natural-pack usability false;
- pressing a natural voice row is both the selector and the demo;
- after selection the settings UI shows `Selected: <name>`;
- preview uses the dedicated `previewNaturalVoice(...)` path, not generic `speak(...)`;
- natural preview calls `naturalSpeechBackend.speak(...)` directly with the selected speaker SID, pitch and speech rate;
- natural preview failure must not call `speakWithAndroidTts`; show a short local failure message instead;
- ordinary BOOP `speak(...)` must still keep same-utterance Android TTS fallback for catchable natural synthesis failures;
- the Kokoro backend must not force `kokoro.setLang("eng")`; retain the explicit `lexicon-gb-en.txt` lexicon path.

Why this separation matters: generic BOOP speech needs resilience, but a voice demo must never silently substitute another engine because that makes every natural voice button sound like the current Android voice and hides the real failure.

### Durable v87 Android Kokoro crash rule

Physical v86 testing exposed a native crash that CI could not exercise on hardware:

- tapping a downloaded natural voice could produce no sound and crash/minimise BOOP;
- after natural voice selection, normal acknowledgements could crash the same way;
- `lights on` still completed the Home Assistant action, then BOOP gave no spoken reply and minimised exactly when speech output began.

Root cause is Sherpa-ONNX 1.13.7's Android JNI callback generation path. BOOP v86 used `generateWithConfigAndCallback(...)`; the native callback bridge can abort the process before Java fallback runs.

**Locked rule while using Sherpa-ONNX 1.13.7 on Android:** do not call `generateWithConfigAndCallback(...)` for BOOP natural speech. Use `tts.generateWithConfig(text, generation)` and check cancellation after synthesis before playback.

v87 TDD lineage:

- regression commit `0c83d3756288668387575bd449ccc9767781cf5d`, workflow `34416099991`: RED exactly on the forbidden callback symbol (`1 failed, 25 passed`);
- production fix `87041a002b16e10401c21282dd29d24f2a56a9b3`: switched to non-callback generation only; relevant natural contracts, Gradle natural tests and wake handoff went green in workflow `34416181606` before the release commit superseded/cancelled that run;
- release head `c046309cece7a4f4abc7e742190c0adb262c1c44`, workflow `34416346339`: SUCCESS end-to-end;
- Shield HOME routing workflow `34416346368`: SUCCESS.

The v86-docs-head to v87-release diff is intentionally narrow: natural backend, one regression test, and version metadata only. Do not attribute HA routing, Android TTS, speaker IDs, eyes/artwork, launcher behavior or package identity changes to v87.

### Historical v86 lineage

- `0fe518ac4c921da5dfed27f2a68e647b531491af` first selector/demo regression test;
- `9b6167f7550fd6519648a361e6a72ef0fa44194e` corrected the startup gate test;
- `ca2ae929dd3bc781197f9d63f99e44cd72b44a46` startup verified-state repair, full workflow `34412253710` SUCCESS;
- `95c48d77da26179b2e4f6fdbece620d8cb118281` natural-only preview/frontend regression contracts;
- `f388c8c56f978c5d4c8c6ddc3abf2ce8e4759b51` removed the invalid Kokoro `eng` override;
- `659ad2aef78a6b94dbdad2ee3e121371e0e969ed` implemented selector + natural-only preview;
- temporary diagnostic workflow proved materialization, Python natural contracts and Gradle natural tests green, then was removed;
- clean full v85-equivalent validation at `fe19092dcfa6248c25b11aaf765958632848004a`, workflow `34414131237`, SUCCESS;
- v86 release head `071159fa8991a92584f301e3072033abe8c405e1`, workflow `34414497922`, SUCCESS in CI but later physically rejected for the JNI callback crash.

### Durable v85 download/install flow

Preserve the v85 repair:

- calculate SHA-256 as exact download bytes are written;
- post-download Verify is only the immediate size/hash receipt check;
- extraction is separately visible as `Installing natural voices… N%`;
- install progress advances during archive extraction;
- Cancel is cooperative/non-blocking and must not synchronously enter pack cleanup from the UI thread;
- the worker owns terminal cleanup after observing cancellation;
- retain archive-root validation, canonical path traversal rejection, link rejection, required-file validation, staging, safe activation/backup restoration and archive deletion after success;
- reopening BOOP after a successful install must detect the current valid pack without redownloading it.

## Durable Android tablet routing

Preserve Unified profile routing exactly:

1. explicit persistent recovery/debug override wins first;
2. Android TV / Leanback / television mode -> `SHIELD`;
3. Pixel 7 Pro -> `WALL`;
4. other non-TV Android devices with `smallestScreenWidthDp >= 600` -> `WALL`;
5. sub-600dp handheld Android -> `LAUNCHER`.

The Xiaomi Pad 7 Pro therefore uses Wall through the generic 600dp rule, not a Xiaomi model hardcode. Tablet support must not fork, regenerate or reinterpret BOOP's approved eyes.

## Durable developer-menu contract

Preserve the accepted developer-menu path:

- exact spoken trigger `developer menu`;
- old `dev menu` phrase intentionally rejected;
- local route before Home Assistant/command-router/chat fallback;
- active spoken/settings entry stays inside existing `MainActivity` through `showDeveloperMenu()`;
- do not restore the failed activity-hop route to `BoopDevMenuActivity` without new physical evidence;
- Voice Settings stays vertically scrollable and its `Developer menu` row stays reachable;
- BOOP Dev grants no new authority/permissions.

## Durable developer-lab selector contract

- BOOP's real current `BoopFaceView` stays pinned and visible while animation controls are browsed;
- Dev Lab itself does not vertically scroll;
- `Animations` is a horizontal selector below the pinned face;
- Wake, Think, Stop, Berry 1/2/3, Shake and Sleep call the real current behavior directly on that same visible BOOP;
- do not restore the rejected full-screen animation-preview + Dismiss page;
- notification dood previews remain local fixtures only and must not create Android shade notifications or invoke runtime listener paths;
- GitHub never judges the visual composition; Ryan does.

## Durable eye and hue rules

Permanent approved eye master:

`unified/assets/boop-eyes/boopApprovedEyes.png`

SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`.

Do not edit, flatten, recompress, regenerate, recreate, crop, resize on disk, recolor/recolour, reconstruct transparency or substitute this source without Ryan explicitly approving a new exact master.

Canonical procedural-eye order:

1. `scripts/patch-unified-reading-eyes.py`;
2. `scripts/patch-v64-procedural-sclera.py`;
3. `scripts/patch-v65-feathered-sclera.py`.

No later materialization stage may rerun the legacy bitmap hue-cache setter. User-selected hue affects only the procedural iris. Default remains cyan/blue at 190 degrees.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference eye provenance and must never be repointed.

## Durable notification contract

Android's original notification remains authoritative. BOOP is a puppet mirror around it.

- locked presentation may expose app identity/icon/count only before authentication;
- tap preserves source `PendingIntent` behavior;
- swipe/timeout dismisses BOOP's mirror only, not the Android shade notification;
- local cue remains guarded to avoid duplicate native/BOOP alerts;
- no full-screen-intent, query-all, accessibility-service or device-admin authority expansion;
- notification doods in Dev Lab remain local presentation fixtures only.

Exact approved notification hands remain byte-locked at `unified/assets/boop-notifications/boop-yellow-hands-approved.png`, size `1,809,990` bytes, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`. Current shared pose remains hands `1.12x`, banner `36dp` upward pending physical visual acceptance.

## Durable wake and microphone architecture

- one controller-owned 16 kHz microphone stream; never add a competing microphone listener;
- BOOP permanently remains an accepted wake name;
- custom wake name is additive, trained from five local examples, stores compact pronunciation state rather than raw enrolment PCM;
- continuous phone wake is allowed on external power; unpowered handheld remains tap-to-talk;
- after TTS, wake re-arms only after speech finishes;
- preserve exact final 1,600 detector samples / 100 ms wake-to-command bridge;
- ordinary and post-wake Android ASR requests keep offensive-word masking disabled; BOOP adds no profanity blacklist.

## Clean Shield HOME boundary

The clean Nvidia Shield HOME replacement remains a separate validation lane on `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Do not merge it into the AIO without Ryan's explicit approval after physical testing.

## Verification boundary

GitHub may verify compilation, non-visual contracts, functional behavior, package/signature/integrity and security. Do not add screenshot comparisons, golden-image checks, pixel/geometry appearance assertions or automated animation judging. Physical appearance, animation, device behavior and acoustic acceptance remain Ryan's gate.

No automatic app installation, permission grant or deployment is implied by a green build.

## Protected rollback checkpoints

Latest fully physically accepted rollback remains:

- `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`.

Also preserve:

- `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`;
- `checkpoint-boop-unified-v65-procedural-eyes`.

Do not create or repoint a v87 checkpoint until Ryan explicitly reports the exact canonical APK physically accepted.
