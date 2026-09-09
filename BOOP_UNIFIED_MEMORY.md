# BOOP unified memory

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer. Fresh `main` owns shared contracts. Always re-fetch live `boop-unified` and `main` before edits; preserve concurrent work.

## Current canonical candidate: v85 natural voices + tablet routing

Exact app/test head before documentation-only follow-up commits:

`b7a4b4d035419e4ef7e62b474da3a0fc039a08eb`

Release identity:

- versionCode `85`;
- versionName `1.2.85-unified-natural-voices-tablet`;
- package `com.boop.alpha1`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Canonical workflow `34409371052`: SUCCESS. Artifact `BOOP-Unified`, ID `10126738984`, size `63,993,838` bytes. Artifact ZIP SHA-256 `579e6aae724d2ac2da67ff851f488fdf7759bb1ec971a0ee5ab97bc85cef69c6`. APK SHA-256 `a6257ab2a8540633649276e676da2a3bcbd46be375382ed9d3cd437b00de3c40`. Shield focused tests `58/58`; Unified focused tests `155/155`; zero failures/errors/skips. The exact artifact was independently downloaded and all receipts matched.

v85 is CI/signer green only until Ryan physically accepts this exact APK. No v85 rollback checkpoint exists yet. Latest fully physically accepted rollback remains v59.

## Durable natural-voice contract

Natural voices are optional, local/offline after one in-app download, and do not use an OpenAI API, account, subscription or cloud TTS.

Pinned pack: Sherpa-ONNX Kokoro `kokoro-multi-lang-v1_0` from the pinned HTTPS release asset. The app verifies the pinned archive byte size and SHA-256 before activation and stores the extracted pack in app-private storage.

Exact user voice order and speaker IDs:

1. Emma: key `bf_emma`, speaker `21`;
2. Isabella: key `bf_isabella`, speaker `22`;
3. George: key `bm_george`, speaker `26`;
4. Fable: key `bm_fable`, speaker `25`.

Installing the pack must not silently change the selected voice. Android TextToSpeech remains the fallback and existing Android/local voices remain available. Spoken `change voice` cycles the exact four natural voices only when the natural backend is selected and usable; otherwise it keeps the existing Android voice behavior. Existing speech-rate control maps to Kokoro speed and existing pitch maps to local playback pitch.

Natural-speech failure must fall back to Android TTS for the same utterance without starting a second wake/TTS ownership cycle.

### Durable v85 download/install flow

Ryan physically found the v70 download reaching Verify and appearing stuck. The old flow reread the full roughly 350 MB archive for SHA-256, then extracted the bzip2/tar archive behind the same static Verify label. Cancel could also block on synchronized pack cleanup during extraction.

Preserve the v85 repair:

- calculate SHA-256 as exact download bytes are written;
- post-download Verify is only the immediate size/hash receipt check;
- extraction is separately visible as `Installing natural voices… N%`;
- install progress advances during archive extraction;
- Cancel is cooperative/non-blocking: set cancellation state and cancel the HTTP call, but do not synchronously enter pack cleanup from the UI thread;
- the worker owns terminal cleanup after observing cancellation;
- retain archive-root validation, canonical path traversal rejection, link rejection, required-file validation, staging, safe activation/backup restoration and archive deletion after success;
- incomplete staging/archive state is cleaned on terminal failure/cancel;
- reopening BOOP after a successful install must detect the verified active pack without redownloading it.

Test-first lineage for this repair:

- clean RED head `35c2c1657bee210802873157fe72630666355c85`, workflow `34408308677`, failed specifically because streaming SHA calculation was absent;
- GREEN focused head `65e524aa05fde80966ded2681d92dfa4f80a630b`, workflow `34408705699`, SUCCESS;
- canonical post-merge build `b7a4b4d0...`, workflow `34409371052`, SUCCESS.

## Durable Android tablet routing

Preserve Unified profile routing exactly:

1. explicit persistent recovery/debug override wins first;
2. Android TV / Leanback / television mode -> `SHIELD`;
3. Pixel 7 Pro -> `WALL`;
4. other non-TV Android devices with `smallestScreenWidthDp >= 600` -> `WALL`;
5. sub-600dp handheld Android -> `LAUNCHER`.

The Xiaomi Pad 7 Pro therefore uses Wall through the generic 600dp rule, not a Xiaomi model hardcode. Tablet support must not fork, regenerate or reinterpret BOOP's approved eyes. Existing face/layout code continues to derive presentation from live dimensions and orientation.

Ryan previously gave positive physical evidence for tablet Wall routing, touch and local Home Assistant control on the v70 tablet candidate. The exact v85 combined APK still requires its own physical recheck.

## Durable developer-menu contract

Preserve the current developer-menu path:

- exact spoken trigger `developer menu`;
- old `dev menu` phrase intentionally rejected;
- local route before Home Assistant/command-router/chat fallback;
- active spoken/settings entry stays inside existing `MainActivity` through `showDeveloperMenu()`;
- do not restore the failed activity-hop route to `BoopDevMenuActivity` without new physical evidence;
- Voice Settings stays vertically scrollable and its `Developer menu` row stays reachable;
- BOOP Dev is an internal testing surface and grants no new authority/permissions.

Ryan physically confirmed the in-place route works and the pinned-face concept is visible.

## Durable developer-lab selector contract

- BOOP's real current `BoopFaceView` stays pinned and visible while animation controls are browsed;
- Dev Lab itself does not vertically scroll;
- `Animations` is a horizontal selector below the pinned face;
- Wake, Think, Stop, Berry 1/2/3, Shake and Sleep call the real current behavior directly on that same visible BOOP;
- do not restore the rejected full-screen animation-preview + Dismiss page;
- notification dood previews remain local fixtures and must not create Android shade notifications or invoke runtime listener paths;
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

No later materialization stage may rerun the legacy bitmap hue-cache setter. User-selected hue affects only the procedural iris. Default remains cyan/blue at 190 degrees. Sclera/whites, pupils, catchlights, black eyelids/accents and the rest of the approved artwork are outside hue control.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference eye provenance and must never be repointed.

## Durable notification contract

Android's original notification remains authoritative. BOOP is a puppet mirror around it.

- locked presentation may expose app identity/icon/count only before authentication;
- tap preserves source `PendingIntent` behavior;
- swipe/timeout dismisses BOOP's mirror only, not the Android shade notification;
- local cue remains guarded to avoid duplicate native/BOOP alerts;
- no full-screen-intent, query-all, accessibility-service or device-admin authority expansion;
- notification doods in Dev Lab remain local presentation fixtures only.

Exact approved notification hands remain byte-locked at `unified/assets/boop-notifications/boop-yellow-hands-approved.png`, size `1,809,990` bytes, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`. Do not regenerate, recompress, recolor/recolour, crop or weaken this guard.

Current shared pose trial: hands rest at `1.12x`; banner/card rests `36dp` upward; relative entrance motion remains the established `16dp` approach / `260ms` overshoot and `0.96 -> 1` hands motion over `220ms`. Physical visual acceptance remains Ryan's decision.

## Durable wake and microphone architecture

- one controller-owned 16 kHz microphone stream; never add a competing microphone listener;
- BOOP permanently remains an accepted wake name;
- custom wake name is additive, trained from five local examples, stores only compact pronunciation state, not raw enrolment PCM;
- continuous phone wake is allowed on external power; unpowered handheld remains tap-to-talk;
- after TTS, wake re-arms only after speech finishes;
- post-wake no-match/timeout is silent and genuinely re-arms;
- hard wake-engine/microphone startup failure remains fail-safe latched;
- preserve exact final 1,600 detector samples / 100 ms wake-to-command bridge;
- keep wake-prefix stripping only as defensive parsing;
- default BOOP uses zero intentional Sherpa trailing blanks;
- ordinary and post-wake Android ASR requests keep offensive-word masking disabled; BOOP adds no profanity blacklist.

## Clean Shield HOME boundary

The clean Nvidia Shield HOME replacement remains a separate validation lane on `boop-shield-clean-launcher`, package `com.boop.shieldhome`. Do not merge it into the AIO merely because v85 exists. A later merge requires Ryan's explicit approval after Shield physical testing.

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

Do not create or repoint a v85 checkpoint until Ryan explicitly reports the exact canonical APK physically accepted.

Historical v70 receipts and rejected/accepted iteration details remain preserved in Git history immediately before the v85 integration.
