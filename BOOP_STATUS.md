# BOOP unified status

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current canonical signed candidate: v86 natural voice preview fix

Release identity:

- versionCode `86`;
- versionName `1.2.86-unified-natural-voice-preview-fix`;
- exact app/test head `071159fa8991a92584f301e3072033abe8c405e1`.

Canonical full workflow `34414497922`: **SUCCESS**. Separate Shield HOME routing workflow `34414497875`: **SUCCESS**.

Verification receipt:

- artifact `BOOP-Unified`;
- artifact ID `10128637096`;
- artifact size `63,993,556` bytes;
- artifact ZIP SHA-256 `2d791d7247ce0827ba66bc4cdfd13aec74ce09e0ad4112da0a0752e9bbcd73b0`;
- APK SHA-256 `ed567c3e04c7d5bf91a57fe01cdf607f7df76539f30522dcabab45cf3e3d6e80`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused tests `58/58`, zero failures/errors/skips;
- Unified focused tests `155/155`, zero failures/errors/skips.

The downloaded artifact independently matched GitHub's ZIP digest, app head, package/version, APK hash and permanent signer receipt.

## Natural voice selector/demo repair

Ryan reported that the four downloaded natural-voice buttons still spoke whichever Android TTS voice was selected.

v86 repairs both causes:

- a current installed natural pack now restores the controller's verified state during startup;
- tapping Emma / Isabella / George / Fable selects that exact natural speaker and updates status to `Selected: <name>`;
- the voice demo uses a dedicated Kokoro natural-only preview path;
- demo failure never silently substitutes Android TTS;
- ordinary BOOP speech still keeps Android TTS fallback for resilience;
- the explicit invalid Kokoro `eng` override is removed while the GB lexicon path remains.

Natural voice choices remain Emma (`bf_emma`, 21), Isabella (`bf_isabella`, 22), George (`bm_george`, 26), Fable (`bm_fable`, 25).

A temporary diagnostic workflow was used only to isolate the earlier red result. It proved materialization, Python natural contracts and Gradle natural tests green, then was deleted. A clean full v85-equivalent validation passed at workflow `34414131237`; the final v86 release workflow then passed at `34414497922`.

## Natural voice install flow remains protected

Keep the v85 download/install repair:

- SHA-256 calculated while bytes are downloaded;
- Verify is the immediate size/hash receipt step;
- extraction is visible as `Installing natural voices… N%`;
- Cancel is cooperative/non-blocking;
- archive/link/path validation, required-file validation, app-private storage and safe activation remain intact.

## Android tablet routing

Preserve routing order:

- explicit profile override first;
- TV / Leanback -> `SHIELD`;
- Pixel 7 Pro -> `WALL`;
- other non-TV devices with `smallestScreenWidthDp >= 600` -> `WALL`;
- sub-600dp handhelds -> `LAUNCHER`.

The Xiaomi Pad 7 Pro therefore takes Wall without a model hardcode.

## Acceptance boundary

v86 is **CI/signer green, physically pending**.

Primary physical checks:

- installed natural pack is recognized after app restart without another download;
- tapping Emma visibly selects Emma and produces Emma's natural demo, not the Android voice;
- tapping one male voice produces a clearly different natural demo;
- normal BOOP speech uses the selected natural voice;
- if a natural demo fails, Android TTS does not impersonate the demo;
- Xiaomi Pad still opens Wall and preserves touch/orientation/local-HA behavior.

No v86 rollback checkpoint has been created or repointed. Latest fully physically accepted rollback remains v59.

## Preserved contracts

- Permanent approved eye master is locked at SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; do not regenerate or destructively edit it.
- User hue remains procedural-iris-only; default remains 190 degrees.
- Canonical eye materialization order remains reading eyes -> v64 sclera -> v65 feathering; no later legacy hue pass.
- Exact approved notification hands stay byte-locked; current shared pose remains `1.12x` hands and `36dp` raised banner pending physical acceptance.
- Android notifications remain authoritative; BOOP mirrors privacy/tap/dismiss behavior.
- Preserve the single 16 kHz microphone owner, wake/name architecture, exact 100 ms wake bridge and uncensored-speech request.
- Clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a future merge.
- GitHub does functional/non-visual verification only. Ryan owns visual/device/acoustic acceptance. No automatic installs or grants.

## Physically accepted rollback

Latest fully physically accepted exact rollback:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

Also preserve v58 `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`, v48 `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`, and `checkpoint-boop-unified-v65-procedural-eyes`.
