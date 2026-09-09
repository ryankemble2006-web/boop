# BOOP unified status

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current canonical signed candidate: v87 Kokoro JNI crash fix

Release identity:

- versionCode `87`;
- versionName `1.2.87-unified-kokoro-jni-crash-fix`;
- exact app/test head `c046309cece7a4f4abc7e742190c0adb262c1c44`.

Canonical full workflow `34416346339`: **SUCCESS**. Separate Shield HOME routing workflow `34416346368`: **SUCCESS**.

Verification receipt:

- artifact `BOOP-Unified`;
- artifact ID `10129329924`;
- artifact size `63,993,437` bytes;
- artifact ZIP SHA-256 `86077df44d1ee78582b5cac762458673545a2dec5cee71388fc71fea0308e420`;
- APK SHA-256 `ae1aeb5f73341c0b4b68ea3ab019d107604b2dc82182b0857c0e74fb0f8e77d8`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused tests `58/58`, zero failures/errors/skips;
- Unified focused tests `155/155`, zero failures/errors/skips.

The downloaded artifact independently matched GitHub's ZIP digest, app head, package/version, APK hash and permanent signer receipt.

## v86 physical failure and v87 repair

Ryan physically rejected v86. Pressing a downloaded natural-voice button could produce no sound and cause BOOP to crash/minimise. After a natural voice had been selected, ordinary speech could hit the same failure: for example `lights on` still completed the Home Assistant action, but BOOP gave no spoken acknowledgement and minimised immediately afterward.

Root cause was isolated to Sherpa-ONNX 1.13.7's Android JNI callback generation path. BOOP v86 used `generateWithConfigAndCallback(...)`; the native callback bridge can abort the Android process before Java fallback can run. This is why both natural preview and later normal acknowledgements could disappear once Kokoro became the selected backend.

v87 makes one functional production change in `BoopNaturalSpeechBackend`: use non-callback `tts.generateWithConfig(text, generation)` and check cancellation immediately after synthesis. Do not restore `generateWithConfigAndCallback(...)` on Android Sherpa 1.13.7.

TDD evidence:

- red regression commit `0c83d375...`, workflow `34416099991`: the new crash-path contract failed exactly because `generateWithConfigAndCallback` was still present (`1 failed, 25 passed`);
- fix commit `87041a002...`: swapped only the unsafe generation call; relevant natural contracts, real Gradle natural tests and wake handoff went green in workflow `34416181606` before that run was superseded/cancelled by the release bump;
- release commit `c046309c...`: v87 full workflow `34416346339` passed end-to-end;
- separate Shield HOME routing workflow `34416346368` passed.

The diff from the prior v86 docs head to the v87 release is deliberately narrow: `source/BoopNaturalSpeechBackend.java`, the natural-voice regression test, and the version bump only. Home Assistant routing, Android TTS backend, natural speaker IDs, eyes/artwork, launcher behavior and package ID were not changed.

## Natural voice selector/demo contract

Preserve the v86 selector/demo behavior while using the v87 safe synthesis path:

- a current installed natural pack restores the controller's verified state during startup;
- tapping Emma / Isabella / George / Fable selects that exact natural speaker and updates status to `Selected: <name>`;
- the voice demo uses a dedicated Kokoro natural-only preview path;
- demo failure never silently substitutes Android TTS;
- ordinary BOOP speech still keeps Android TTS fallback for catchable natural-speech failures;
- the explicit invalid Kokoro `eng` override remains removed while the GB lexicon path remains;
- Android Sherpa 1.13.7 synthesis must use `generateWithConfig(...)`, not the crashing callback generation route.

Natural voice choices remain Emma (`bf_emma`, 21), Isabella (`bf_isabella`, 22), George (`bm_george`, 26), Fable (`bm_fable`, 25).

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

v86 is **physically rejected** for the voice crash/minimise described above.

v87 is **CI/signer green, physically pending**. Primary physical checks:

- natural pack remains recognized after app restart without another download;
- tapping Emma visibly selects Emma, speaks her Kokoro demo, and BOOP remains foreground;
- tapping at least one male voice changes timbre and does not crash/minimise;
- with a natural voice selected, `lights on` still changes the light and BOOP remains foreground long enough to speak the acknowledgement;
- ordinary BOOP replies continue after natural voice selection;
- a catchable natural failure may use Android TTS for ordinary BOOP speech, but a voice demo must never impersonate a natural voice with Android TTS;
- Xiaomi Pad still opens Wall and preserves touch/orientation/local-HA behavior.

No v87 rollback checkpoint has been created or repointed. Latest fully physically accepted rollback remains v59.

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
