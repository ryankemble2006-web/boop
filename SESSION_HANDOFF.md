# BOOP unified handoff

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current canonical signed candidate: v87 Kokoro JNI crash fix

Release identity:

- versionCode `87`;
- versionName `1.2.87-unified-kokoro-jni-crash-fix`;
- package `com.boop.alpha1`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Exact **app/test release head** before documentation-only follow-up commits:

`c046309cece7a4f4abc7e742190c0adb262c1c44`

Canonical workflow `34416346339`: **SUCCESS**. Separate Shield HOME routing workflow `34416346368`: **SUCCESS**.

Verification receipt:

- artifact `BOOP-Unified`;
- artifact ID `10129329924`;
- artifact size `63,993,437` bytes;
- artifact ZIP SHA-256 `86077df44d1ee78582b5cac762458673545a2dec5cee71388fc71fea0308e420`;
- APK SHA-256 `ae1aeb5f73341c0b4b68ea3ab019d107604b2dc82182b0857c0e74fb0f8e77d8`;
- Shield focused functional tests `58/58`, zero failures/errors/skips;
- Unified focused functional tests `155/155`, zero failures/errors/skips.

The exact artifact ZIP was downloaded after CI. Its SHA-256 matched GitHub's artifact digest. `built-commit.txt` matched `c046309c...`; `badging.txt` confirmed package `com.boop.alpha1`, versionCode `87`, versionName `1.2.87-unified-kokoro-jni-crash-fix`; `apk-sha256.txt` matched the extracted APK; `signer-sha256.txt` matched the permanent BOOP signer.

## v86 physical failure: native Kokoro callback crash

Ryan physically rejected v86 after testing the exact canonical artifact. Symptoms:

- tapping Emma / Isabella / George / Fable could produce no sound and BOOP could crash/minimise;
- after a natural voice had been selected, normal speech could die too;
- specifically, spoken `lights on` still changed the lights through Home Assistant, but BOOP gave no reply and minimised when the acknowledgement should have played.

This isolated the fault after successful command execution, inside speech output rather than HA routing.

Root cause was Sherpa-ONNX 1.13.7's Android JNI callback generation path. BOOP v86 called `generateWithConfigAndCallback(...)`. That native callback bridge can abort the Android process, so Java's ordinary natural-to-Android-TTS fallback never gets a chance to run.

Do **not** restore `generateWithConfigAndCallback(...)` for Android natural speech while BOOP is on Sherpa-ONNX 1.13.7.

## v87 repair

`BoopNaturalSpeechBackend` now uses:

`tts.generateWithConfig(text, generation)`

Cancellation is checked again immediately after synthesis before playback. This removes the crashing callback route without changing HA control, normal Android TTS, voice IDs, selection behavior, artwork, launcher behavior, package ID or signer.

TDD / verification lineage:

- `0c83d375...` added the regression contract; workflow `34416099991` failed RED exactly because the unsafe callback symbol was still present (`1 failed, 25 passed`);
- `87041a002...` made the one functional production repair; relevant natural contracts, real Gradle natural tests and wake handoff were green in workflow `34416181606` before that run was superseded/cancelled by the release commit;
- `c046309c...` bumped to v87; full workflow `34416346339` passed all stages;
- Shield HOME routing workflow `34416346368` passed;
- compare from the prior v86 documentation head `048af1c...` to v87 release contains only `source/BoopNaturalSpeechBackend.java`, `tests/test_unified_v85_natural_voice_install_flow.py`, and `unified/app-build.gradle`.

## Natural voice selector/demo behavior to preserve

The v86 selector/demo repair remains correct and must be retained with the v87 safe backend:

- tapping Emma / Isabella / George / Fable selects that exact natural speaker;
- Voice Settings shows `Selected: <name>` immediately;
- the demo goes through a dedicated natural-only preview path;
- the preview never substitutes Android TTS if Kokoro synthesis fails;
- a failed or unavailable natural demo gives a short local failure message instead;
- normal BOOP speech still retains Android TTS fallback for catchable natural failures;
- startup reconciles a current installed pack with the controller's verified state before previews can run;
- the invalid Kokoro `eng` language override remains removed while the explicit GB English lexicon path remains;
- Android natural synthesis uses `generateWithConfig(...)`, not `generateWithConfigAndCallback(...)`.

Natural voice order remains:

1. Emma: `bf_emma`, speaker `21`;
2. Isabella: `bf_isabella`, speaker `22`;
3. George: `bm_george`, speaker `26`;
4. Fable: `bm_fable`, speaker `25`.

## Natural voice download/install flow from v85 remains protected

Preserve the v85 repair:

- SHA-256 is calculated while download bytes are written;
- post-download Verify is an immediate size/hash receipt check;
- extraction is separately visible as `Installing natural voices… N%`;
- Cancel is cooperative/non-blocking and does not synchronously enter pack cleanup from the UI thread;
- worker owns terminal cleanup;
- archive traversal/link rejection, required-file validation, app-private storage, safe activation and archive deletion remain intact.

Natural voices remain optional and local/offline after the one-time in-app model download. Installing the pack does not silently select a natural voice.

## Physical acceptance boundary for v87

v86 is physically rejected for the voice crash described above.

v87 is **CI/signer green, physically pending**.

Primary check:

1. Install v87 over the current BOOP build.
2. Open Voice Settings with the natural pack already installed. It should not redownload.
3. Tap Emma. Status should change to `Selected: Emma`, Emma should speak, and BOOP must remain foreground.
4. Repeat with at least one male voice, ideally George or Fable, and confirm the timbre changes without a crash/minimise.
5. Close/reopen BOOP and repeat one natural demo to prove installed-pack verification survives restart.
6. With a natural voice selected, say `lights on`; HA should complete the action and BOOP should stay alive/foreground and speak its acknowledgement.
7. Speak another ordinary BOOP response with the natural voice selected and confirm normal speech continues.
8. If a natural demo fails, Android TTS must not impersonate it; ordinary BOOP speech may still use Android fallback for a catchable natural failure.

Tablet check remains: Xiaomi Pad 7 Pro should route to Wall through the generic `smallestScreenWidthDp >= 600` rule, preserve touch/tap-to-speak, portrait/landscape handling and local HA control.

Do **not** create or repoint a v87 rollback checkpoint until Ryan explicitly accepts the physical APK. Latest fully physically accepted rollback remains v59.

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
