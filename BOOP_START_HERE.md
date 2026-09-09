# BOOP — start here

Updated 2026-09-09. Repository: [ryankemble2006-web/boop](https://github.com/ryankemble2006-web/boop).

## Canonical app branch

**Normal BOOP app development starts on `boop-unified`.**

Ryan chose one APK/one canonical app lineage after separate Wall, Launcher and Shield app builds became operationally confusing. Unified keeps package `com.boop.alpha1` and the permanent BOOP signer.

Current canonical app/test head before documentation-only follow-up commits:

`b7a4b4d035419e4ef7e62b474da3a0fc039a08eb`

Current release:

- versionCode `85`;
- versionName `1.2.85-unified-natural-voices-tablet`;
- canonical workflow `34409371052`: SUCCESS;
- artifact `BOOP-Unified`, ID `10126738984`;
- APK SHA-256 `a6257ab2a8540633649276e676da2a3bcbd46be375382ed9d3cd437b00de3c40`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

CI/signer green is not physical acceptance. Ryan still owns the exact-device, visual and acoustic acceptance gate.

For Unified work always read the live branch copies of:

- `SESSION_HANDOFF.md`;
- `BOOP_STATUS.md`;
- `BOOP_UNIFIED_MEMORY.md`;
- `unified/SOURCE_HEADS.md`;
- `BOOP_EYES_MASTER.md` and `BOOP_RULES.md` when artwork/animation is involved.

## Unified routing contract

One AIO package, `com.boop.alpha1`, contains the established bodies:

1. explicit persistent recovery/debug profile override wins first;
2. Android TV / Leanback / television mode -> Shield body;
3. Pixel 7 Pro -> Wall body;
4. other non-TV Android devices with `smallestScreenWidthDp >= 600` -> Wall body, including Xiaomi Pad 7 Pro;
5. sub-600dp handheld Android -> Launcher body.

Tablet support is generic width-based routing, not a Xiaomi model hardcode. Wall-to-Launcher and Launcher-to-Wall remain internal activity transitions in the one package.

Ryan previously gave positive physical evidence for the 600dp tablet Wall route, touch and local Home Assistant control on the v70 tablet candidate. The exact combined v85 APK still requires a physical Pad recheck.

## v85 natural voices

v85 includes the optional local Kokoro natural-voice layer:

- Emma `bf_emma` / speaker 21;
- Isabella `bf_isabella` / 22;
- George `bm_george` / 26;
- Fable `bm_fable` / 25.

The pack is downloaded from the pinned HTTPS Sherpa release asset inside BOOP, verified by pinned size/SHA, extracted to app-private storage, and used locally/offline afterward. Existing Android TextToSpeech remains fallback; installing the pack does not silently switch voices.

Ryan physically found the v70 download reaching Verify and appearing stuck. v85 fixes the post-download flow by hashing bytes during download, making Verify an immediate receipt check, exposing archive extraction separately as `Installing natural voices… N%`, and making Cancel cooperative/non-blocking. Focused RED -> GREEN and the full canonical signed build are recorded in `BOOP_UNIFIED_MEMORY.md`.

## Explicit standalone exception: clean Nvidia Shield HOME

The clean Nvidia Shield HOME replacement is deliberately still standalone:

- branch `boop-shield-clean-launcher`;
- package `com.boop.shieldhome`.

Do **not** merge this standalone clean HOME into `com.boop.alpha1` merely because v85 is canonical. Ryan must explicitly approve that later after Shield hardware testing. Stock launcher recovery remains part of that experiment's safety model.

The v85 canonical merge discussed here is the natural-voice + tablet integration only, not the clean Shield HOME experiment.

## Shared protected contracts

- Permanent approved eye master is `unified/assets/boop-eyes/boopApprovedEyes.png`, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`. Do not regenerate or destructively edit it.
- User eye hue remains procedural-iris-only; default cyan/blue remains 190 degrees.
- Canonical eye patch order remains reading eyes -> v64 sclera -> v65 feathering; no later legacy hue-cache pass.
- Official notification hands remain byte-locked; notification privacy/tap/dismiss semantics remain authoritative to Android's original notification.
- One controller-owned 16 kHz microphone stream, accepted wake-name/training architecture, exact 100 ms wake-to-command bridge and uncensored speech request remain protected.
- GitHub does compilation/non-visual/functional/package/signature/integrity checks only. No screenshot/golden/pixel visual acceptance. Ryan owns visual/device/acoustic acceptance.
- No automatic app install, permission grant or deployment is implied by a green build.

## Rollback discipline

Latest fully physically accepted Unified rollback remains:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

Also preserve v58, v48 and `checkpoint-boop-unified-v65-procedural-eyes`. Do not create or repoint a v85 checkpoint until Ryan explicitly accepts the exact canonical APK physically.

Historical Wall/Launcher/Shield branches and older v70 iteration receipts remain in Git for provenance and rollback. They are not the normal starting point for new app work.

## Starting any BOOP task

1. Read live `main/AGENTS.md`, `main/BOOP_CONTEXT.md`, `main/BOOP_START_HERE.md` and `main/BOOP_RULES.md`.
2. Fetch/check the live intended app branch and `main`; do not trust cached refs or the primary checkout.
3. For normal AIO work use `boop-unified` and read its handoff/status/memory/source-head files.
4. For the clean Shield HOME exception use `boop-shield-clean-launcher` and keep `com.boop.shieldhome` separate until Ryan explicitly approves a merge.
5. Preserve dirty/concurrent work. No reset/force push or silent lineage overwrite.
6. Current user instructions and fresh physical-device evidence beat stale dated notes.
7. CI-green, signer-green and physically accepted are separate states.

When Ryan says `update memory`, treat it as documentation synchronization unless he separately asks for app-code changes.
