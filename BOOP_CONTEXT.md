# BOOP shared context

## Conversational continuity

Read live [`BOOP_PERSONALITY.md`](BOOP_PERSONALITY.md) on `main` for Ryan's shared Boop voice, contextual jokes and known memory gaps. Updated 2026-09-10: this is the fresh-ish personality baseline Ryan requested. Read it when starting a BOOP conversation as well as engineering work; do not ask Ryan to reconstruct context already recorded there. It supplements engineering handoffs and does not change app contracts or physical acceptance. Existing tasks must explicitly reread it; GitHub publication alone does not automatically synchronize conversations.

Updated 2026-09-09. Current user instructions and fresh physical-device evidence win over stale dated notes. Normal BOOP app development uses `boop-unified`; main is the shared context hub, not the built app.

## Product identity

BOOP is a useful smart-home/media puppet that lives across several device bodies, not a claim of sentience. Wall, phone Launcher and Shield are different bodies of the same BOOP. Home Assistant remains the local authority for house/media control. Ordinary conversation may use the configured assistant path, but basic local control must not depend on cloud chat availability.

The canonical AIO package is `com.boop.alpha1` with the permanent BOOP signer. One APK contains Wall, Launcher and the established Shield body.

## Current canonical v85 integration

Exact current app/test head before documentation-only follow-up commits:

`b7a4b4d035419e4ef7e62b474da3a0fc039a08eb`

Release:

- versionCode `85`;
- versionName `1.2.85-unified-natural-voices-tablet`;
- canonical workflow `34409371052`: SUCCESS;
- artifact `BOOP-Unified`, ID `10126738984`;
- APK SHA-256 `a6257ab2a8540633649276e676da2a3bcbd46be375382ed9d3cd437b00de3c40`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused tests `58/58`;
- Unified focused tests `155/155`;
- zero failures/errors/skips.

The exact canonical artifact was independently downloaded and its ZIP digest, app head, package/version, APK hash and signer receipts matched GitHub.

This v85 integration combines the current tablet-capable Unified lineage with the optional natural-voice work. It does **not** merge the separate clean Nvidia Shield HOME experiment.

CI/signer green is not physical acceptance. Ryan still owns visual/device/acoustic acceptance and must physically test this exact v85 APK.

## Unified routing contract

Preserve the routing order:

1. explicit persistent recovery/debug override first;
2. Android TV / Leanback / television mode -> `SHIELD`;
3. Pixel 7 Pro -> `WALL`;
4. other non-TV Android devices with `smallestScreenWidthDp >= 600` -> `WALL`;
5. sub-600dp handheld Android -> `LAUNCHER`.

The Xiaomi Pad 7 Pro therefore routes to Wall through the generic 600dp tablet rule, not a model hardcode. Wall presentation continues to derive geometry from live dimensions/orientation; tablet support must not fork or regenerate BOOP's approved eyes.

Ryan previously gave positive physical evidence for the tablet route, touch operation and local Home Assistant control on the v70 tablet candidate. The exact combined v85 build still needs a Pad recheck.

## Natural voices, locked direction

v85 includes four optional British natural voices using local Sherpa-ONNX Kokoro after one in-app model download:

- Emma `bf_emma`, speaker 21;
- Isabella `bf_isabella`, speaker 22;
- George `bm_george`, speaker 26;
- Fable `bm_fable`, speaker 25.

The model pack is downloaded inside BOOP from the pinned HTTPS Sherpa release asset, checked against pinned archive size and SHA-256, stored/extracted in app-private storage, and used offline afterward. There is no OpenAI API requirement, no natural-voice subscription/account path, no browser/file-picker detour, and Android TextToSpeech remains fallback. Installing the pack does not silently switch the selected voice.

Ryan physically found the v70 model download reaching Verify and appearing stuck. The v85 repair is durable:

- SHA-256 is computed while exact download bytes are written;
- post-download Verify is an immediate size/hash receipt check instead of rereading the full archive;
- archive extraction is separately visible as `Installing natural voices… N%`;
- Cancel is cooperative/non-blocking and does not synchronously wait on pack cleanup;
- worker-side terminal cleanup, archive traversal/link checks, required-file validation, safe staging/activation and app-private storage remain intact.

Focused test-first and canonical build evidence is recorded in `boop-unified/BOOP_UNIFIED_MEMORY.md`.

## Permanent BOOP eye master

Ryan approved the exact glossy black-lidded eye master as BOOP's permanent default across phone/Wall, tablet, Shield and animation work.

Canonical master:

`unified/assets/boop-eyes/boopApprovedEyes.png`

Identity: 1774 x 887 RGBA, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`.

Do not edit, flatten, recompress, regenerate, recreate, crop, resize on disk, recolor/recolour, reconstruct transparency or substitute this master without Ryan explicitly approving a new exact master. Runtime scaling, posing, blinking, masking and animation must be non-destructive.

User eye colour changes only the procedural iris. Default remains cyan/blue at 190 degrees. Sclera/whites, pupil, catchlights, black lids/accents and the rest of the approved artwork do not change hue.

Preserve canonical eye materialization order:

1. `patch-unified-reading-eyes.py`;
2. `patch-v64-procedural-sclera.py`;
3. `patch-v65-feathered-sclera.py`.

No later legacy bitmap hue-cache pass may run. `checkpoint-boop-unified-v65-procedural-eyes` remains protected provenance.

## Official yellow hands and notifications

BOOP's approved yellow hands remain five digits per hand, floating without arms, and must not be regenerated/reinterpreted when merely posing them.

Notification hands binary remains locked at `unified/assets/boop-notifications/boop-yellow-hands-approved.png`, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`.

Android's original notification remains authoritative. BOOP mirrors it:

- locked presentation may show app identity/icon/count only before authentication;
- tap preserves source `PendingIntent` behavior;
- swipe/timeout dismisses BOOP's mirror only, not the original shade notification;
- sound/vibration remains guarded to avoid duplicate alerts;
- no full-screen-intent, query-all, accessibility-service or device-admin authority expansion.

Current shared notification pose trial keeps the exact hands asset unchanged, rests hands at `1.12x`, raises the banner/card `36dp`, and preserves the established relative entrance motion. Physical appearance remains Ryan's judgement, never CI's.

## Developer-lab contract

The spoken/settings developer menu remains in-place inside the existing `MainActivity`:

- exact spoken trigger `developer menu`;
- former `dev menu` phrase rejected;
- local route before Home Assistant/command/chat fallback;
- active path uses `showDeveloperMenu()` rather than restoring the failed activity hop.

Voice Settings stays vertically scrollable. In BOOP Dev, the real current BOOP face remains pinned while the animation selector moves horizontally, and animation buttons drive that same visible BOOP directly. The rejected full-screen animation-preview + Dismiss design must not return accidentally.

Notification demos remain local presentation fixtures only and must never create shade notifications or invoke runtime notification-listener paths.

## Wake, voice and privacy contracts

- one controller-owned 16 kHz microphone stream; never add a competing microphone listener;
- BOOP is permanently a wake name; custom names are additive;
- custom-name training uses five local spoken examples and stores compact pronunciation state, not raw enrollment PCM;
- preserve the exact 1,600-sample / 100 ms wake-to-command bridge;
- default BOOP uses zero intentional Sherpa trailing blanks;
- ordinary and post-wake Android recognition request offensive-word masking disabled; BOOP adds no profanity blacklist;
- after normal TTS, wake re-arms only when speech is finished;
- hard wake-engine/microphone startup failure remains fail-safe latched;
- diagnostics remain pull-only and raw audio is not persistently logged.

## Home Assistant and local control

BOOP controls exposed Home Assistant devices dynamically rather than requiring one sentence template per device. Local Home/HA control remains ahead of chat fallback. Room-scoped behavior must fail closed rather than controlling similarly named devices in another room. Existing accepted HA names/Home controls, room isolation and media-control behavior must not regress as unrelated features are added.

## Clean Nvidia Shield HOME exception

The clean Shield HOME replacement remains deliberately standalone on:

- branch `boop-shield-clean-launcher`;
- package `com.boop.shieldhome`.

It is not part of the v85 natural/tablet merge. Do not merge it into `com.boop.alpha1` until Ryan explicitly approves that later after Shield hardware testing. Stock launcher recovery remains part of that experiment's safety boundary.

## Verification and release discipline

Ryan owns BOOP visual acceptance. GitHub must not add screenshot/golden-image comparisons, visual diffing, pixel/geometry appearance assertions or automated animation judging. CI stays focused on compilation, non-visual contracts, functional tests, package/signature/integrity and security.

A green build does not imply automatic installation, permissions, deployment or physical acceptance.

Physical acceptance creates rollback authority. Latest fully physically accepted Unified rollback remains:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

Also preserve v58, v48 and the v65 eye checkpoint. Do not create or repoint a v85 checkpoint until Ryan explicitly reports the exact canonical v85 APK physically accepted.

Historical standalone branches and v70 iteration receipts remain in Git for provenance/rollback, but normal app development starts from the live `boop-unified` branch.
