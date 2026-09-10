# BOOP — start here

## Conversational continuity

Read live [`BOOP_PERSONALITY.md`](BOOP_PERSONALITY.md) on `main` for Ryan's shared Boop voice, contextual jokes and known memory gaps. Updated 2026-09-10: this is the fresh-ish personality baseline Ryan requested. Read it when starting a BOOP conversation as well as engineering work; do not ask Ryan to reconstruct context already recorded there. It supplements engineering handoffs and does not change app contracts or physical acceptance. Existing tasks must explicitly reread it; GitHub publication alone does not automatically synchronize conversations.

Updated 2026-09-09. Repository: [ryankemble2006-web/boop](https://github.com/ryankemble2006-web/boop).

## Canonical app branch

**Normal BOOP app development starts on `boop-unified`.**

Ryan chose one APK/one canonical app lineage after separate Wall, Launcher and Shield app builds became operationally confusing. Unified keeps package `com.boop.alpha1` and the permanent BOOP signer.

## Current voice baseline and scoped rebuild

Canonical `boop-unified` retains v91 app source `11650313221ae5bf997dbb93b6a905bfdc7da1ed` and the permanent signer/package. Ryan physically accepted natural voice selection, demos and a normal selected-voice reply on 2026-09-10. Checkpoint `checkpoint-boop-unified-v91-natural-voices-accepted` preserves that source. Unified run `34433115316` succeeded; artifact `10135283428`; APK SHA256 `42dc50d12031a674aa751918f6bfd6b4deab8b6ced95332a437f4068124fe53d`. Voice acceptance does not imply blanket visual/HA/remote-microphone acceptance. Detailed receipts are on canonical handoff/status/memory.

Ryan selected overhaul items **1, 3, 6, 7, 8 and 10** on dedicated **`boop-canonical-rebuild`**: shared state, configured room and generic exposed HA discovery, one media corner/Home owner, local/Deezer transport, HA Back escape, device profiles. Read that branch's live `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md` and scoped plan. It is a candidate, not a replacement for accepted canonical. Eyes are owned by another task for later transplant; eyes/blink and Turbo redesign are outside this selected scope.

GitHub performs nonvisual builds/functional checks/signing. No GitHub appearance/visual tests. Manual local emulator inspection and Ryan's device/provider acceptance remain separate.

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

## Shield Home integration scope

`boop-shield-clean-launcher` and standalone package `com.boop.shieldhome` remain preserved. Ryan's 2026-09-10 scoped rebuild approval now permits reusing its existing Home/Now Playing source as an internal library in `boop-canonical-rebuild` (`com.boop.alpha1`). This supersedes the older blanket separation rule only for this candidate. Do not merge into canonical, change default Android HOME, grant accessibility/notification access, or alter standalone rollback state by implication. Read the rebuild handoff for verified integration state.

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

Also preserve v58, v48 and `checkpoint-boop-unified-v65-procedural-eyes`. The v91 voice checkpoint and v88 Android-voice checkpoint are additionally protected; v91 acceptance is scoped to the natural-voice result.

Historical Wall/Launcher/Shield branches and older v70 iteration receipts remain in Git for provenance and rollback. They are not the normal starting point for new app work.

## Starting any BOOP task

1. Read live `main/AGENTS.md`, `main/BOOP_CONTEXT.md`, `main/BOOP_START_HERE.md` and `main/BOOP_RULES.md`.
2. Fetch/check the live intended app branch and `main`; do not trust cached refs or the primary checkout.
3. For normal AIO work use `boop-unified` and read its handoff/status/memory/source-head files.
4. For the selected rebuild use `boop-canonical-rebuild`; preserve the standalone clean Shield HOME branch/package as provenance.
5. Preserve dirty/concurrent work. No reset/force push or silent lineage overwrite.
6. Current user instructions and fresh physical-device evidence beat stale dated notes.
7. CI-green, signer-green and physically accepted are separate states.

When Ryan says `update memory`, treat it as documentation synchronization unless he separately asks for app-code changes.
