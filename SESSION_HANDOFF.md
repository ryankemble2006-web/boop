# BOOP unified handoff

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current canonical signed candidate: v86 natural voice preview fix

Release identity:

- versionCode `86`;
- versionName `1.2.86-unified-natural-voice-preview-fix`;
- package `com.boop.alpha1`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Exact **app/test head** before documentation-only follow-up commits:

`071159fa8991a92584f301e3072033abe8c405e1`

Canonical workflow `34414497922`: **SUCCESS**. Separate Shield HOME routing workflow `34414497875`: **SUCCESS**.

Verification receipt:

- artifact `BOOP-Unified`;
- artifact ID `10128637096`;
- artifact size `63,993,556` bytes;
- artifact ZIP SHA-256 `2d791d7247ce0827ba66bc4cdfd13aec74ce09e0ad4112da0a0752e9bbcd73b0`;
- APK SHA-256 `ed567c3e04c7d5bf91a57fe01cdf607f7df76539f30522dcabab45cf3e3d6e80`;
- Shield focused functional tests `58/58`, zero failures/errors/skips;
- Unified focused functional tests `155/155`, zero failures/errors/skips.

The exact artifact ZIP was downloaded after CI. Its SHA-256 matched GitHub's artifact digest. `built-commit.txt` matched `071159fa...`; `badging.txt` confirmed package `com.boop.alpha1`, versionCode `86`, versionName `1.2.86-unified-natural-voice-preview-fix`; `apk-sha256.txt` matched the extracted APK; `signer-sha256.txt` matched the permanent BOOP signer.

## v86 natural voice selector/demo repair

Ryan physically reported on v85 that tapping Emma, Isabella, George or Fable spoke the currently selected Android TTS voice instead of demonstrating/selecting the requested downloaded natural voice.

Two defects were repaired:

1. On a later app launch, an already-installed current natural pack could be marked usable without restoring the controller's persisted `verified` state. The generic `speak()` path therefore considered natural speech unavailable and silently fell back to Android TTS.
2. Voice-name buttons called generic `speak(preview)`. Generic speech is intentionally allowed to fall back to Android TTS for normal BOOP operation, so a failed natural demo could masquerade as a successful natural demo.

v86 behavior is now explicit:

- tapping Emma / Isabella / George / Fable selects that exact natural speaker;
- Voice Settings shows `Selected: <name>` immediately;
- the demo goes through a dedicated natural-only preview path;
- the preview never substitutes Android TTS if Kokoro synthesis fails;
- a failed or unavailable natural demo gives a short local failure message instead;
- normal BOOP speech still retains Android TTS fallback for resilience;
- startup reconciles a current installed pack with the controller's verified state before previews can run;
- the invalid Kokoro `eng` language override was removed while the explicit GB English lexicon path remains.

Natural voice order remains:

1. Emma: `bf_emma`, speaker `21`;
2. Isabella: `bf_isabella`, speaker `22`;
3. George: `bm_george`, speaker `26`;
4. Fable: `bm_fable`, speaker `25`.

### Test lineage for this repair

- `0fe518ac...` added the first regression test;
- `9b6167f7...` corrected the test to the real startup gate;
- `ca2ae929...` restored verified installed-pack state on startup; workflow `34412253710` passed end-to-end;
- `95c48d77...` added natural-only preview and Kokoro frontend regression contracts;
- `f388c8c5...` removed the bad Kokoro `eng` override;
- `659ad2ae...` changed voice-name taps to selector + dedicated natural preview;
- a temporary split diagnostic workflow proved materialization, Python natural contracts and Gradle natural tests all green, then was removed;
- clean full validation at v85-equivalent code head `fe19092d...`, workflow `34414131237`: SUCCESS;
- release bump `071159fa...`, workflow `34414497922`: SUCCESS.

The earlier red runs during TDD are historical evidence only. Do not treat them as the current branch state.

## Natural voice download/install flow from v85 remains protected

Preserve the v85 repair:

- SHA-256 is calculated while download bytes are written;
- post-download Verify is an immediate size/hash receipt check;
- extraction is separately visible as `Installing natural voices… N%`;
- Cancel is cooperative/non-blocking and does not synchronously enter pack cleanup from the UI thread;
- worker owns terminal cleanup;
- archive traversal/link rejection, required-file validation, app-private storage, safe activation and archive deletion remain intact.

Natural voices remain optional and local/offline after the one-time in-app model download. Installing the pack does not silently select a natural voice.

## Physical acceptance boundary for v86

v86 is **CI/signer green, physically pending**.

Primary check:

1. Install v86 over the current BOOP build.
2. Open Voice Settings with the natural pack already installed. It should not redownload.
3. Tap Emma. Status should change to `Selected: Emma` and the demo must sound like Emma, not the Android voice.
4. Repeat with at least one male voice, ideally George or Fable, and confirm the timbre changes.
5. Close/reopen BOOP and repeat one natural demo to prove installed-pack verification survives restart.
6. Speak a normal BOOP response with a natural voice selected and confirm it uses the chosen natural voice.
7. If a natural demo fails, Android TTS must not impersonate it; the app should report the natural preview failure instead.

Tablet check remains: Xiaomi Pad 7 Pro should route to Wall through the generic `smallestScreenWidthDp >= 600` rule, preserve touch/tap-to-speak, portrait/landscape handling and local HA control.

Do **not** create or repoint a v86 rollback checkpoint until Ryan explicitly accepts the physical APK. Latest fully physically accepted rollback remains v59.

## Durable Android tablet routing

Preserve profile order:

1. explicit persistent recovery/debug override;
2. Android TV / Leanback / television mode -> `SHIELD`;
3. Pixel 7 Pro -> `WALL`;
4. other non-TV Android devices with `smallestScreenWidthDp >= 600` -> `WALL`;
5. sub-600dp handheld Android -> `LAUNCHER`.

This is generic tablet support, not a Xiaomi model hardcode.

## Durable developer-menu / developer-lab state

Preserve the accepted in-place `developer menu` route inside `MainActivity`; do not restore the rejected activity-hop route. Voice Settings stays vertically scrollable. Dev Lab pins the real current BOOP face and uses its horizontal animation selector. Notification dood previews remain local fixtures only and do not create shade notifications or new authority.

## Durable protected state

- Permanent approved eye master remains `unified/assets/boop-eyes/boopApprovedEyes.png`, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`. Do not regenerate or destructively edit it.
- Canonical procedural-eye order remains `patch-unified-reading-eyes.py` -> `patch-v64-procedural-sclera.py` -> `patch-v65-feathered-sclera.py`. No later legacy bitmap hue pass.
- User eye hue remains iris-only; default cyan/blue remains 190 degrees.
- Exact approved notification hands remain byte-locked; current shared pose remains `1.12x` with banner `36dp` upward pending physical visual acceptance.
- Android's original notification remains authoritative; BOOP mirrors it without changing locked privacy/tap/dismiss semantics.
- Preserve one 16 kHz microphone owner, accepted wake/name architecture, exact 100 ms wake bridge and uncensored-speech request.
- Clean Nvidia Shield HOME remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`, until Ryan explicitly approves a future merge.
- GitHub performs functional/non-visual verification only. No screenshot/golden/pixel appearance tests. Ryan owns visual, device and acoustic acceptance.
- No automatic installs, permission grants or signer/package changes.

## Physically accepted rollback state

Latest fully physically accepted exact rollback remains:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

Also preserve:

- `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`;
- `checkpoint-boop-unified-v65-procedural-eyes` as protected eye provenance.
