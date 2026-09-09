# BOOP unified handoff

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current canonical signed candidate: v85 natural voices + tablet routing

Release identity:

- versionCode `85`;
- versionName `1.2.85-unified-natural-voices-tablet`;
- package `com.boop.alpha1`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Exact **app/test head** (before documentation-only follow-up commits):

`b7a4b4d035419e4ef7e62b474da3a0fc039a08eb`

That merge combines the current Unified/tablet lineage with the local natural-voice work. It does not merge the separate clean Shield HOME package.

### Canonical verification receipt

Canonical post-merge workflow `34409371052`: **SUCCESS**.

Also passed the separate Shield HOME routing workflow `34409371028`.

The canonical workflow passed non-visual integration contracts, materialization, natural-voice/developer-lab/notification contracts, wake handoff, Launcher lint, Shield functional tests, broader Unified routing/lifecycle/assistant tests, permanent signing, APK assembly, package/version/signer/archive verification, and artifact upload.

- artifact `BOOP-Unified`;
- artifact ID `10126738984`;
- artifact size `63,993,838` bytes;
- artifact ZIP SHA-256 `579e6aae724d2ac2da67ff851f488fdf7759bb1ec971a0ee5ab97bc85cef69c6`;
- APK SHA-256 `a6257ab2a8540633649276e676da2a3bcbd46be375382ed9d3cd437b00de3c40`;
- Shield focused functional tests `58/58`, zero failures/errors/skips;
- Unified focused functional tests `155/155`, zero failures/errors/skips.

The exact artifact ZIP was independently downloaded after CI. Its SHA-256 matched GitHub's artifact digest. `built-commit.txt` matched `b7a4b4d0...`; `badging.txt` confirmed package `com.boop.alpha1`, versionCode `85`, versionName `1.2.85-unified-natural-voices-tablet`; `apk-sha256.txt` matched the extracted APK; and `signer-sha256.txt` matched the permanent BOOP signer.

## Natural voices: verify/install hang repair

Ryan physically reported that the model download reached the post-download **Verify** state and appeared stuck there.

Root cause in the v70 implementation: after downloading the roughly 350 MB Kokoro archive, BOOP performed a second full archive SHA-256 read and then the complete bzip2/tar extraction while the UI still showed one static `Verifying natural voices…` label. Cancel could also synchronously enter pack cleanup while extraction owned the pack monitor, making the UI look wedged.

v85 fixes the pipeline at its source:

- SHA-256 is calculated while the download bytes are written, removing the second full archive reread;
- the post-download `Verifying natural voices…` stage is now the immediate size/hash receipt check;
- extraction is a separate `Installing natural voices… N%` phase with progress life-signs;
- Cancel sets the cooperative cancellation flag and cancels the HTTP call without synchronously waiting on pack cleanup;
- the worker owns terminal cleanup after observing cancellation;
- archive path/link/traversal validation and required-file validation remain intact;
- the pack remains app-private and the archive is deleted after successful activation.

Focused TDD evidence:

- clean RED head `35c2c1657bee210802873157fe72630666355c85`, workflow `34408308677`, failed specifically because streaming SHA calculation was absent;
- GREEN focused head `65e524aa05fde80966ded2681d92dfa4f80a630b`, workflow `34408705699`: SUCCESS;
- full combined/canonical workflows then passed as recorded above.

Natural voice model remains Sherpa-ONNX Kokoro `kokoro-multi-lang-v1_0`, downloaded in-app from the pinned HTTPS release asset and verified against the pinned size/SHA manifest. Voice order remains Emma (`bf_emma`, speaker 21), Isabella (`bf_isabella`, 22), George (`bm_george`, 26), Fable (`bm_fable`, 25). Existing Android TextToSpeech remains fallback. Installing the pack does not silently select a natural voice.

## Durable Android tablet routing

Preserve the Unified profile order:

1. explicit persistent recovery/debug override;
2. Android TV / Leanback / television mode -> `SHIELD`;
3. Pixel 7 Pro -> `WALL`;
4. other non-TV Android devices with `smallestScreenWidthDp >= 600` -> `WALL`;
5. sub-600dp handheld Android -> `LAUNCHER`.

This is generic tablet support, not a Xiaomi model hardcode. BOOP's approved eye source is not forked or regenerated for tablets.

Ryan previously gave positive Xiaomi Pad 7 Pro evidence for the 600dp Wall route, touch operation and local Home Assistant control on the v70 tablet candidate. The **combined v85 APK itself still needs a physical Pad recheck** because CI/device acceptance are separate gates.

## Physical acceptance boundary for v85

v85 is **CI/signer green only** until Ryan tests this exact APK.

Primary voice check:

1. Open Voice Settings and download natural voices.
2. Confirm download reaches 100%, Verify completes, then `Installing natural voices… N%` advances instead of appearing frozen.
3. Confirm it reaches `Natural voices ready` and exposes Emma, Isabella, George and Fable.
4. Select at least one female and one male voice and confirm BOOP speaks locally.
5. Reopen BOOP and confirm the installed pack remains usable without another download.
6. Optional cancellation check: start again only if a clean retry is needed, cancel, and confirm the UI remains responsive and returns to a retryable state.

Pad check: launch on the Xiaomi Pad 7 Pro, confirm direct Wall routing, rotate portrait/landscape, then check touch/tap-to-speak and one local HA command.

Existing physical evidence remains valid for the in-place `developer menu` route and pinned BOOP face. Notification raised-banner/hands appearance remains Ryan's visual judgement; GitHub does not certify appearance.

Do **not** create or repoint a v85 rollback checkpoint until Ryan explicitly accepts the physical build. Latest fully physically accepted rollback remains v59.

## Durable protected state

- Permanent approved eye master remains `unified/assets/boop-eyes/boopApprovedEyes.png`, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`. Do not regenerate or destructively edit it.
- Canonical procedural-eye order remains `patch-unified-reading-eyes.py` -> `patch-v64-procedural-sclera.py` -> `patch-v65-feathered-sclera.py`. No later legacy bitmap hue pass.
- User eye hue remains iris-only; default cyan/blue remains 190 degrees.
- Exact approved notification hands remain byte-locked; shared pose currently rests at `1.12x` with banner `36dp` upward pending physical visual acceptance.
- Android's original notification remains authoritative; BOOP mirrors it without changing locked privacy/tap/dismiss semantics.
- One 16 kHz microphone owner, accepted wake-name/training architecture, exact 100 ms wake bridge and uncensored speech request remain protected.
- Clean Nvidia Shield HOME remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`, until Ryan explicitly approves a later merge.
- GitHub performs functional/non-visual verification only. No screenshot/golden/pixel appearance tests. Ryan owns visual, device and acoustic acceptance.
- No automatic installs, permission grants or signer/package changes.

## Physically accepted rollback state

Latest fully physically accepted exact rollback remains:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

Also preserve:

- `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`;
- `checkpoint-boop-unified-v65-procedural-eyes` as protected eye provenance.

Historical v70 developer-lab/tablet/notification receipts remain preserved in Git history immediately before the v85 integration; do not repoint historical checkpoints.
