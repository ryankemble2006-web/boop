# BOOP unified handoff

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Protected usable rollback: v88 Android voice restored

Exact physically accepted app/test release head:

`f5f086fc4f67712b5746be067aff852331299bb0`

Protected checkpoint:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Ryan physically confirmed the exact v88 build is usable again: ordinary Android TextToSpeech replies work. Natural voice still fails, but the failed natural preview no longer silences ordinary BOOP speech. This checkpoint means **working Android BOOP with natural speech isolated**, not natural speech accepted.

Never repoint this checkpoint. Do not weaken its runtime-proof selection gate while debugging natural speech.

v88 release receipts remain:

- versionCode `88`;
- versionName `1.2.88-unified-natural-playback-recovery`;
- workflow `34418073143`: SUCCESS;
- Shield routing `34418073200`: SUCCESS;
- artifact `10129945925`;
- APK SHA-256 `8fca19f2005b8a252488978e0efc7f7391711207d06ac1557088648a0402e108`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Current signed test candidate: v89 natural runtime diagnostics

Exact source/build head:

`066f4bcc71187b885b28244e94537e3a19ea4016`

Release identity:

- versionCode `89`;
- versionName `1.2.89-unified-natural-runtime-diagnostics`;
- package `com.boop.alpha1`;
- launch activity `com.boop.alpha1.UnifiedEntryActivity`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Verification:

- canonical full workflow `34423535719`: **SUCCESS**;
- separate Shield HOME routing workflow `34423453620`: **SUCCESS**;
- artifact `BOOP-Unified`, ID `10131885702`, size `63,994,347` bytes;
- artifact ZIP SHA-256 `c3ef776370f111a8977efb259894287e1c870f2c469fb2de82d711c10c775217`;
- APK SHA-256 `1716310cbafe64cbe7dcdc4aa4b7dc10aeb8a0315271c2ae7f0e97eaba0f1ea4`;
- notification/developer/natural focused stage: SUCCESS;
- wake-command handoff stage: SUCCESS;
- Shield focused tests `58/58`, zero failures/errors/skips;
- Unified focused tests `155/155`, zero failures/errors/skips;
- signed APK/package/version/entry activity/permanent signer/archive checks: SUCCESS.

The GitHub artifact was downloaded after CI and independently inspected. ZIP integrity passed. Its SHA matched GitHub's artifact digest, `built-commit.txt` matched `066f4bcc...`, and the extracted APK SHA matched `apk-sha256.txt`.

v89 is diagnostic only. Do not checkpoint it as working natural speech without Ryan's physical result.

## Why v89 exists

v88 safely restored Android speech, but natural preview still failed. Its error boundary was too broad: `ensureTts()` construction failures were being reported under the same generic `synthesis` stage as `generateWithConfig(...)`, so hardware testing could not identify the failing component.

A systematic read-only investigation before changing code ruled out several earlier guesses:

1. Exact Sherpa-ONNX `v1.13.7` source permits blank Kokoro `lang` when a lexicon is configured, so do not restore the rejected `kokoro.setLang("eng")` guess.
2. Exact upstream v1.0 voice data agrees with BOOP's speaker identities: Emma `bf_emma`/21, Isabella `bf_isabella`/22, Fable `bm_fable`/25, George `bm_george`/26.
3. BOOP's build downloads the pinned Sherpa `1.13.7` AAR and verifies SHA-256 before copying it into the materialized Android project.
4. The exact v88 APK contained Sherpa/ONNX native libraries for arm64-v8a, armeabi-v7a, x86 and x86_64.
5. On arm64, `libsherpa-onnx-jni.so`'s app-native dependency `libonnxruntime.so` was present; ordinary Android system dependencies were the remaining dynamic requirements.
6. The inspected arm64 Sherpa/ONNX ELF LOAD alignment was `0x4000`, ruling out the obvious 16 KB page-alignment packaging failure.

Therefore v89 deliberately adds evidence rather than another speculative Kokoro fix.

## v89 implementation

`source/BoopNaturalSpeechBackend.java` now keeps the v88 path but splits dedicated failure stages:

- `files`: verifies model, voices, tokens, GB lexicon, Inno files and non-empty eSpeak data are present/readable before Sherpa construction;
- `initialization`: catches `OfflineTts` construction/native/model initialization failure and verifies `sampleRate() > 0` plus enough speakers for SID 26;
- `synthesis`: applies only after initialization when `generateWithConfig(text, generation)` throws or returns no audio;
- `playback`: applies to the existing signed-PCM16 `AudioTrack` stage.

Existing `NaturalSpeechException` stages are preserved through the outer worker instead of being collapsed back into `synthesis`.

`scripts/patch-v89-natural-diagnostics.py` materializes a debug-only full-screen diagnostic when a natural **preview** fails. It does not intercept normal BOOP fallback speech and it does not change selection state. It shows a huge stable code, one short explanation and a sanitized/truncated root exception summary.

### DEV code table

| Code | Meaning |
| --- | --- |
| `BOOP DEV E890` | Natural runtime files incomplete/unreadable. |
| `BOOP DEV E891` | Sherpa/Kokoro `OfflineTts` initialization or runtime metadata failed. |
| `BOOP DEV E892` | Model initialized, but synthesis failed or returned no samples. |
| `BOOP DEV E893` | Synthesis returned audio, but Android PCM16 playback failed. |
| `BOOP DEV E899` | Unknown natural-voice stage. |

Never expose tokens, passwords, private network addresses, signing details or personal data in these diagnostics. DEV diagnostics remain debug-build-only unless Ryan later approves otherwise.

## v89 TDD lineage

- RED contract commit `00004ce6bf52108d2354b4d45d67d79287641e8e`;
- RED workflow `34423130443`: exactly **1 expected failure and 29 passes**, proving the new runtime-stage contract was absent before production work;
- backend stage split `8fde7f3d2c7576a973b2b0e5e6b7dba0ee3dc30c`;
- photographable diagnostic materialization `436dd5c3c991e8b831f60ef67043fafbb3a0d153`;
- test target updated for dedicated patch `a1f268d5cc012f6877bbccb84e1234b37e3b4389`;
- materialization wiring `e2ab8205aebee2e52331bb7c9b49e829f50b5698`;
- release bump `14819ad95fac3f11b53a2dd2807fabf255828f28`;
- first full v89 workflow `34423453651` failed only because the source contract matched one call with formatting-sensitive whitespace;
- test assertion corrected to semantic whitespace-safe matching at `066f4bcc71187b885b28244e94537e3a19ea4016` without changing app implementation;
- final full workflow `34423535719`: SUCCESS.

Diff from the last v88 documentation baseline `f1a6e6bce3b700bde206db8b5223fcbac2b2b32a` to v89 build head is limited to:

- `source/BoopNaturalSpeechBackend.java`;
- `scripts/patch-v89-natural-diagnostics.py`;
- `scripts/materialize-unified.sh`;
- `tests/test_unified_v85_natural_voice_install_flow.py`;
- `unified/app-build.gradle`.

No HA routing, approved eye asset, launcher behavior, package ID or signing identity was intentionally changed.

## Physical test required next

This is now a genuine hardware-evidence gate. Do not guess around it.

1. Install exact v89 over v88.
2. Before pressing any natural row, say `lights on`. HA should act and Android BOOP should speak normally.
3. Open Voice Settings and tap **Emma once**.
4. Report or photograph the giant `BOOP DEV E###` code and the short detail beneath it, or report if Emma actually speaks.
5. Close the diagnostic and say `lights on` again. Android BOOP must still speak.

Interpretation:

- `E890`: fix pack/runtime-file activation or validation only.
- `E891`: focus on Sherpa native/model initialization/config/path/runtime environment; do not touch AudioTrack.
- `E892`: model construction succeeded; focus on generation/frontend/config/text/speaker inputs; do not touch playback.
- `E893`: Kokoro synthesis works; focus only on Android PCM16 output.
- Emma speaks: verify another speaker and normal natural routing before declaring Natural Voices finished.
- Android speech breaks at either `lights on` check: return immediately to the v88 checkpoint and debug regression before natural work.

## Canonical rebuild prompt is loaded, approved, and queued

Ryan supplied the full `BOOP CANONICAL REBUILD / FULL AUTONOMOUS GITHUB DEVELOPMENT RUN` prompt. It grants scoped approval for the ordered rebuild and says not to stop for routine phase approvals. Its architecture is the accepted target: **BOOP is one logical puppet; state is data; surfaces render that same puppet.**

However its Phase 1 explicitly assumes Natural Voices are already finished. Fresh physical evidence says they are not. Therefore the safe order is:

1. finish Natural Voices on `boop-unified` while protecting v88;
2. physically prove them;
3. fetch live `boop-unified` and `main`;
4. create dedicated `boop-canonical-rebuild` from the exact verified head containing finished Natural Voice work;
5. execute the supplied phases there, not directly on `boop-unified`.

During the canonical rebuild Ryan remains the only visual QA. If canonical eye/blink assets are missing, uncertain, mutated, regenerated or unverifiable, stop and request `canonical-idle-blink-v1.zip`. Never recreate BOOP from memory or image generation.

## Protected v88 natural speech rules

- Candidate preview is not selection.
- Successful completed natural playback is required before persisting the natural speaker and `natural_runtime_proven_version`.
- Ordinary speech may use natural only when current pack version has runtime proof.
- Failed preview never substitutes Android TTS and pretends it was the requested natural voice.
- Failed preview must leave ordinary Android TTS available.
- Keep Sherpa synthesis on `tts.generateWithConfig(text, generation)`.
- Never restore `generateWithConfigAndCallback(...)` while shipping Sherpa 1.13.7; v86 exposed its Android JNI abort path.
- Natural playback stays signed PCM16. Do not restore float PCM or `PlaybackParams` while this fault is unresolved.
- Natural pitch remains deferred; Android TTS pitch remains unaffected.
- Keep explicit `lexicon-gb-en.txt`; do not force `lang="eng"` without new evidence.

## Other protected contracts

- Natural pack download/install remains v85 app-private streaming-SHA + explicit extraction + cooperative cancellation + traversal/link rejection + required-file validation + safe activation.
- Permanent approved eye master remains `unified/assets/boop-eyes/boopApprovedEyes.png`, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; never regenerate/destructively edit it.
- User eye hue remains procedural iris-only; default 190 degrees.
- Exact approved notification hands remain byte-locked.
- Preserve one 16 kHz microphone owner, accepted wake/name architecture and exact 100 ms wake bridge.
- Unified routing remains recovery override -> TV/Leanback Shield -> Pixel 7 Pro Wall -> other non-TV >=600dp Wall -> smaller handheld Launcher.
- Clean Nvidia Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a merge.
- GitHub performs non-visual functional/build/signing verification only. Ryan owns physical, visual and acoustic acceptance.

## Physically accepted rollback state

Current usable rollback:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Also preserve:

- `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`;
- v58 `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- v48 `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`;
- `checkpoint-boop-unified-v65-procedural-eyes`.
