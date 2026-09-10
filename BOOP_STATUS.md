# BOOP unified status

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current physical rollback: v88 Android voice restored

Exact accepted app/test release head:

`f5f086fc4f67712b5746be067aff852331299bb0`

Protected checkpoint:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Ryan physically confirmed v88 is back to a usable BOOP baseline: Android TextToSpeech speaks ordinary replies again. Natural voice still fails, but that failed experiment no longer poisons ordinary Android speech. This is acceptance of v88 as the current usable rollback, not acceptance of Kokoro.

Do not weaken the v88 runtime-proof isolation or repoint this checkpoint.

## Current test candidate: v89 natural runtime diagnostics

Exact source/build head:

`066f4bcc71187b885b28244e94537e3a19ea4016`

Release:

- versionCode `89`;
- versionName `1.2.89-unified-natural-runtime-diagnostics`;
- canonical workflow `34423535719`: **SUCCESS**;
- separate Shield HOME routing workflow `34423453620`: **SUCCESS**;
- artifact `BOOP-Unified`, ID `10131885702`, size `63,994,347` bytes;
- artifact ZIP SHA-256 `c3ef776370f111a8977efb259894287e1c870f2c469fb2de82d711c10c775217`;
- APK SHA-256 `1716310cbafe64cbe7dcdc4aa4b7dc10aeb8a0315271c2ae7f0e97eaba0f1ea4`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- package `com.boop.alpha1`;
- launch activity `com.boop.alpha1.UnifiedEntryActivity`;
- Shield focused functional tests `58/58`, zero failures/errors/skips;
- Unified focused functional tests `155/155`, zero failures/errors/skips;
- notification/dev/natural focused stage: SUCCESS;
- wake-command handoff stage: SUCCESS;
- signed APK/package/signer/archive checks: SUCCESS.

The exact GitHub artifact was downloaded after CI. ZIP integrity passed, ZIP digest matched GitHub's artifact digest, built commit matched `066f4bcc...`, and the extracted APK digest matched the bundled receipt.

v89 is a **diagnostic candidate**, not a claim that natural voices are fixed. Physical testing is required before changing Kokoro again.

## v89 root-cause investigation

Before v89, the following possible causes were checked rather than guessed:

- exact Sherpa-ONNX 1.13.7 Kokoro configuration supports the current GB lexicon without forcing `lang="eng"`;
- Emma `bf_emma`/21, Isabella `bf_isabella`/22, Fable `bm_fable`/25 and George `bm_george`/26 match the upstream v1.0 voice data;
- the build fetches the pinned Sherpa `1.13.7` AAR and verifies SHA-256 before materialization;
- the shipped v88 APK contained arm64, armv7, x86 and x86_64 Sherpa native libraries;
- arm64 `libsherpa-onnx-jni.so` and `libonnxruntime.so` were present and the JNI library's required native dependencies were satisfied in the APK;
- the inspected arm64 native libraries used `0x4000` load alignment, so the obvious 16 KB-page ELF packaging failure was ruled out;
- v88 currently folds OfflineTts construction errors into the generic `synthesis` stage, so device evidence could not distinguish native/model initialization from actual generation.

Therefore v89 does not make another speculative natural-voice repair. It adds evidence boundaries only.

## v89 Natural Voice DEV codes

| Code | Meaning |
| --- | --- |
| `BOOP DEV E890` | Required natural-voice runtime files are missing/unreadable/incomplete. |
| `BOOP DEV E891` | Kokoro / Sherpa `OfflineTts` model initialization failed or returned invalid runtime metadata. |
| `BOOP DEV E892` | Kokoro initialized, but speech generation failed or produced no audio. |
| `BOOP DEV E893` | Speech generation succeeded, but Android PCM16 playback failed. |
| `BOOP DEV E899` | Natural voice failed outside the known stages. |

The full-screen diagnostic is debug-build-only, shows one short explanation plus a sanitized/truncated root cause, and is called from natural preview failure only. It does not replace the existing `Android voice kept` fallback behavior.

## TDD / diagnostic lineage

- RED source-test commit `00004ce6bf52108d2354b4d45d67d79287641e8e`;
- RED workflow `34423130443`: expected **1 failure, 29 passes**, solely because v88 lacked the new runtime diagnostic contract;
- stage-splitting backend `8fde7f3d2c7576a973b2b0e5e6b7dba0ee3dc30c`;
- DEV diagnostic materialization `436dd5c3c991e8b831f60ef67043fafbb3a0d153`;
- diagnostic test target `a1f268d5cc012f6877bbccb84e1234b37e3b4389`;
- materialization wiring `e2ab8205aebee2e52331bb7c9b49e829f50b5698`;
- v89 release metadata `14819ad95fac3f11b53a2dd2807fabf255828f28`;
- first v89 workflow `34423453651` exposed a whitespace-sensitive source assertion, not an app failure;
- assertion made whitespace-safe at `066f4bcc71187b885b28244e94537e3a19ea4016`;
- final canonical workflow `34423535719`: SUCCESS.

Functional diff from the confirmed v88 docs baseline is confined to natural runtime diagnostics, their materialization/test contract and v89 version metadata. No HA command routing, approved eye master, launcher behavior, package identity or signer was intentionally changed.

## Required physical v89 test

1. Install exact v89 over v88.
2. Before touching a natural voice, say `lights on`. HA should act and Android BOOP should still speak.
3. Open Voice Settings and tap **Emma once**.
4. Photograph/report the giant `BOOP DEV E###` code and the short detail beneath it, or report if Emma actually speaks.
5. Close the diagnostic and say `lights on` again. Android BOOP must still speak.

That single code selects the next engineering path. Do not change Kokoro again before this physical evidence unless static investigation proves a concrete root cause independently.

## Canonical rebuild prompt queued behind Natural Voices

Ryan supplied and approved the full `BOOP CANONICAL REBUILD` plan. Its premise says Natural Voices are already finished. Fresh physical evidence overrides that premise, so the rebuild is intentionally blocked on completing Natural Voices first. Once natural speech is physically proven, create `boop-canonical-rebuild` from the exact verified live `boop-unified` HEAD containing that finished voice work and execute the supplied phases there. Do not perform the canonical rebuild directly on `boop-unified`.

The supplied rebuild's absolute visual rule remains binding: Ryan alone performs visual acceptance. If canonical eye/blink authority is missing, mutated or unverifiable during that project, stop and request `canonical-idle-blink-v1.zip`; never regenerate BOOP.

## Preserved contracts

- Keep `tts.generateWithConfig(text, generation)`; do not restore Sherpa 1.13.7 Android `generateWithConfigAndCallback(...)` after the v86 JNI crash evidence.
- Keep the v88 runtime-proof gate: failed natural preview cannot become BOOP's ordinary voice.
- Natural playback remains signed PCM16 with no `PlaybackParams`; natural pitch stays deferred until playback works physically.
- Natural pack install/download keeps v85 streaming-hash, visible extraction, app-private validation and cooperative cancellation.
- Permanent eye master remains byte-locked at SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; iris hue remains procedural only, default 190 degrees.
- Approved notification hands remain byte-locked.
- Preserve one 16 kHz microphone owner and exact 100 ms wake bridge.
- Unified routing remains explicit override -> TV Shield -> Pixel 7 Pro Wall -> non-TV >=600dp Wall -> smaller handheld Launcher.
- Clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a merge.
- GitHub performs non-visual functional/build/signing checks only. Ryan owns visual, device and acoustic acceptance.

## Physically accepted rollback points

Current usable rollback:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Also preserve:

- `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`;
- v58 `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- v48 `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`;
- `checkpoint-boop-unified-v65-procedural-eyes`.
