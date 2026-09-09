# BOOP unified status

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current canonical signed candidate: v85 natural voices + tablet routing

Release identity:

- versionCode `85`;
- versionName `1.2.85-unified-natural-voices-tablet`;
- exact app/test head `b7a4b4d035419e4ef7e62b474da3a0fc039a08eb`.

Canonical post-merge workflow `34409371052`: **SUCCESS**. Separate Shield HOME routing workflow `34409371028`: **SUCCESS**.

Verification receipt:

- artifact `BOOP-Unified`;
- artifact ID `10126738984`;
- artifact size `63,993,838` bytes;
- artifact ZIP SHA-256 `579e6aae724d2ac2da67ff851f488fdf7759bb1ec971a0ee5ab97bc85cef69c6`;
- APK SHA-256 `a6257ab2a8540633649276e676da2a3bcbd46be375382ed9d3cd437b00de3c40`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused tests `58/58`, zero failures/errors/skips;
- Unified focused tests `155/155`, zero failures/errors/skips.

The downloaded canonical artifact independently matched GitHub's ZIP digest, app head, package, version, APK hash and permanent signer receipt.

## Natural voice install repair

Ryan's v70 physical test downloaded the Kokoro pack and then appeared stuck on Verify.

Root cause: BOOP reread the complete downloaded archive for SHA-256 and then performed the whole bzip2/tar extraction behind one static Verify label. Cancel could also block on pack cleanup while the extraction worker held the pack monitor.

v85 changes the post-download flow to:

- calculate SHA-256 while download bytes are written;
- make Verify an immediate size/hash receipt check;
- show extraction separately as `Installing natural voices… N%`;
- make Cancel cooperative/non-blocking, with terminal cleanup owned by the worker;
- retain archive traversal/link rejection, required-file validation, app-private storage and Android TTS fallback.

Focused test-first receipt:

- RED head `35c2c1657bee210802873157fe72630666355c85`, workflow `34408308677`, failed specifically on missing streaming SHA behavior;
- GREEN head `65e524aa05fde80966ded2681d92dfa4f80a630b`, workflow `34408705699`, SUCCESS;
- full canonical v85 workflow then passed.

Natural voice choices remain Emma (`bf_emma`, 21), Isabella (`bf_isabella`, 22), George (`bm_george`, 26), Fable (`bm_fable`, 25). Pack install is optional and local/offline after download. It does not silently switch away from the existing Android voice.

## Android tablet routing

Preserve routing order:

- explicit profile override first;
- TV / Leanback -> `SHIELD`;
- Pixel 7 Pro -> `WALL`;
- other non-TV devices with `smallestScreenWidthDp >= 600` -> `WALL`;
- sub-600dp handhelds -> `LAUNCHER`.

The Xiaomi Pad 7 Pro therefore takes the Wall route without a model hardcode. Ryan previously gave positive physical evidence for this routing/touch/local-HA behavior on the v70 tablet candidate; the exact combined v85 APK still needs its own physical recheck.

## Acceptance boundary

v85 is **CI/signer green, physically pending**.

Main physical checks:

- natural pack reaches 100%, completes Verify, visibly advances through Install, then reaches Ready;
- Emma/Isabella/George/Fable buttons appear and at least two voices speak locally;
- reopening BOOP keeps the installed pack usable;
- Xiaomi Pad still opens Wall and handles portrait/landscape, touch and one local HA command;
- existing developer-menu/pinned-face behavior remains healthy;
- notification hands/banner appearance remains manual visual acceptance only.

No v85 rollback checkpoint has been created or repointed. Latest fully physically accepted rollback remains v59.

## Preserved contracts

- Permanent approved eye master is locked at SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; do not regenerate or destructively edit it.
- User hue is procedural-iris-only; default remains 190 degrees.
- Canonical eye materialization order remains reading eyes -> v64 sclera -> v65 feathering; no later legacy hue pass.
- Exact approved notification hands stay byte-locked; current shared pose is `1.12x` hands and `36dp` raised banner pending physical acceptance.
- Android notifications remain authoritative; BOOP mirrors privacy/tap/dismiss behavior.
- Preserve the single 16 kHz microphone owner, wake/name architecture, exact 100 ms wake bridge, and uncensored-speech request.
- Clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a future merge.
- GitHub does functional/non-visual verification only. Ryan owns visual/device/acoustic acceptance. No automatic installs or grants.

## Physically accepted rollback

Latest fully physically accepted exact rollback:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

Also preserve v58 `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`, v48 `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`, and `checkpoint-boop-unified-v65-procedural-eyes`.

Historical v70 receipts remain available in Git history immediately before the v85 integration.
