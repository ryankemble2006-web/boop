# BOOP unified memory

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Fresh `main` owns shared product contracts. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current physically usable checkpoint: v88 Android voice restored

Exact app/test release head:

`f5f086fc4f67712b5746be067aff852331299bb0`

Protected checkpoint:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Ryan physically confirmed this exact v88 build is back to a usable BOOP baseline and explicitly approved it as a checkpoint. Android TextToSpeech is restored and ordinary BOOP replies speak again. Natural voice still fails, but the failure no longer poisons ordinary speech. The checkpoint therefore means **usable Android speech restored**, not **natural speech fixed**.

Never repoint this checkpoint. The v88 runtime-proof isolation is now physically valuable and must not be weakened while debugging Kokoro.

v88 receipts:

- versionCode `88`;
- versionName `1.2.88-unified-natural-playback-recovery`;
- workflow `34418073143`: SUCCESS;
- artifact `10129945925`;
- APK SHA-256 `8fca19f2005b8a252488978e0efc7f7391711207d06ac1557088648a0402e108`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Current unaccepted diagnostic candidate: v89

Exact app/test build head:

`066f4bcc71187b885b28244e94537e3a19ea4016`

Release:

- versionCode `89`;
- versionName `1.2.89-unified-natural-runtime-diagnostics`;
- canonical workflow `34423535719`: SUCCESS;
- Shield HOME routing `34423453620`: SUCCESS;
- artifact ID `10131885702`, size `63,994,347` bytes;
- artifact ZIP SHA-256 `c3ef776370f111a8977efb259894287e1c870f2c469fb2de82d711c10c775217`;
- APK SHA-256 `1716310cbafe64cbe7dcdc4aa4b7dc10aeb8a0315271c2ae7f0e97eaba0f1ea4`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- package `com.boop.alpha1`;
- launch activity `com.boop.alpha1.UnifiedEntryActivity`;
- Shield focused tests `58/58`;
- Unified focused tests `155/155`;
- zero failures/errors/skips in those focused suites;
- natural/dev/notification and wake handoff stages green;
- package/signature/archive verification green.

The exact artifact was downloaded and independently checked: ZIP integrity passed, GitHub's artifact digest matched the downloaded ZIP, `built-commit.txt` matched `066f4bcc...`, and the extracted APK digest matched the artifact receipt.

v89 has **no physical natural-voice acceptance**. Do not checkpoint it as a good voice build.

## Durable natural voice identity

Natural voices remain optional, local/offline after one in-app model download and use no paid/cloud TTS API.

Pinned Sherpa-ONNX Kokoro pack: `kokoro-multi-lang-v1_0`.

Exact voice order:

1. Emma: key `bf_emma`, speaker 21;
2. Isabella: key `bf_isabella`, speaker 22;
3. George: key `bm_george`, speaker 26;
4. Fable: key `bm_fable`, speaker 25.

The pack remains downloaded from the pinned HTTPS Sherpa release, verified by pinned archive size/SHA, extracted into app-private storage and reused offline. Preserve the v85 streaming-hash / visible extraction / cooperative cancellation / traversal and link rejection / required-file validation / safe activation flow.

## Natural speech physical history

### v85

Natural voice rows appeared to work but actually spoke the selected Android TTS voice. Generic speech fallback hid natural synthesis failure.

### v86

The natural-only selector/preview path entered Sherpa's native backend. Hardware exposed a process-crashing Android JNI callback route. Ryan reported no sound and crash/minimise. `lights on` completed Home Assistant control, then BOOP minimised and gave no acknowledgement when speech started.

Never restore Sherpa-ONNX 1.13.7 Android `generateWithConfigAndCallback(...)`. Use `tts.generateWithConfig(text, generation)`.

### v87

Removing the JNI callback stopped the hard-crash route, but all four natural previews still failed and ordinary BOOP speech remained mute because pressing a natural row persisted a dead natural backend before it had proven playback.

### v88

v88 changed natural selection to successful-playback proof. Ryan physically confirmed Android speech is restored and remains usable even though natural preview still fails. This validates the **isolation architecture**, not Kokoro itself.

### v89

v89 does not claim another Kokoro repair. It is a hardware evidence build that separates runtime files, model/native initialization, synthesis and playback and renders the failing stage as a large debug-only code.

## Durable v88 natural voice safety contract

A natural voice does **not** become BOOP's normal mouth merely because its button was pressed.

Selection order is mandatory:

1. Resolve candidate voice/SID without selecting it.
2. Require the installed current pack to be verified and ready for preview.
3. Attempt dedicated natural synthesis/playback.
4. Only after successful completed playback persist that speaker.
5. Only after successful completed playback mark current pack version runtime-proven.
6. Ordinary BOOP speech may route through natural only when natural is selected **and** the current pack version equals the stored runtime-proven version.

Controller concepts remain:

- `naturalPackReadyForPreview()` = current installed/verified pack may be tried;
- `markNaturalPlaybackProven()` = persist `natural_runtime_proven_version` after successful preview playback only;
- `naturalBackendSelectedAndUsable()` = selected natural + preview-ready pack + matching runtime-proven version.

Failed preview must never persist/select that candidate and must never poison later ordinary speech. Preview must not substitute Android TTS and impersonate the requested natural voice. Ordinary speech retains Android TTS resilience.

## Durable v88 Android synthesis/playback path

Sherpa synthesis remains:

`tts.generateWithConfig(text, generation)`

Natural playback remains conservative signed PCM16:

- clamp generated float samples to [-1, 1];
- convert to signed 16-bit PCM;
- `AudioFormat.ENCODING_PCM_16BIT`;
- mono `AudioTrack`;
- static playback buffer;
- no `AudioFormat.ENCODING_PCM_FLOAT`;
- no natural `PlaybackParams` pitch manipulation while basic playback is unresolved.

Speech rate maps to Kokoro generation speed. Natural pitch remains deferred. Keep explicit `lexicon-gb-en.txt`; do not force `kokoro.setLang("eng")` without new evidence.

## v89 systematic investigation and durable ruled-outs

Before another production voice fix, exact source/artifact evidence was gathered.

Do not casually reopen these eliminated guesses unless new hardware evidence contradicts them:

- exact Sherpa-ONNX `v1.13.7` Kokoro validation allows blank `lang` when a lexicon is supplied;
- exact upstream v1.0 voice data agrees with BOOP's Emma/Isabella/Fable/George speaker IDs;
- the BOOP materialization downloads `sherpa-onnx-1.13.7.aar` from the pinned Sherpa release and validates SHA-256 before use;
- the exact v88 APK contains Sherpa native libraries for arm64-v8a, armeabi-v7a, x86 and x86_64;
- the arm64 JNI library's app-native ONNX dependency is present in the APK;
- inspected arm64 Sherpa/ONNX LOAD alignment is `0x4000`, so the simple 16 KB Android page-alignment packaging failure is not the explanation;
- v88's remaining evidence problem was that `ensureTts()` construction errors were collapsed into the same `synthesis` wrapper as real `generateWithConfig(...)` failures.

## v89 diagnostic contract

`BoopNaturalSpeechBackend` now reports these stages without changing the v88 selection/fallback architecture:

- `files`: required model/runtime files must exist, be readable/nonempty and eSpeak data must be nonempty;
- `initialization`: `OfflineTts` construction/native/model opening failure, plus sanity probes `sampleRate() > 0` and speaker count sufficient for SID 26;
- `synthesis`: model initialized but `generateWithConfig(...)` failed or returned no samples;
- `playback`: generated audio reached Android PCM16 output and playback failed.

Existing `NaturalSpeechException` instances are preserved through the worker rather than rewrapped as generic synthesis failures.

Debug-only natural preview diagnostics:

| Code | Durable meaning |
| --- | --- |
| `BOOP DEV E890` | Natural runtime files incomplete/unreadable. |
| `BOOP DEV E891` | Sherpa/Kokoro OfflineTts initialization or runtime metadata failed. |
| `BOOP DEV E892` | OfflineTts initialized, but generation failed/no samples. |
| `BOOP DEV E893` | Generated audio exists, but Android PCM16 playback failed. |
| `BOOP DEV E899` | Unknown natural failure stage. |

The diagnostic is full-screen, debug-build-only, large/photographable, and contains a short explanation plus sanitized/truncated root exception detail. It is invoked from natural **preview** failure only. It must not take over ordinary speech fallback or leak credentials/tokens/private addresses/signing/personal data.

## v89 TDD lineage

- RED test `00004ce6bf52108d2354b4d45d67d79287641e8e`;
- RED workflow `34423130443`: exactly one expected missing-contract failure and 29 passes;
- backend stage split `8fde7f3d2c7576a973b2b0e5e6b7dba0ee3dc30c`;
- diagnostic UI patch `436dd5c3c991e8b831f60ef67043fafbb3a0d153`;
- diagnostic test source target `a1f268d5cc012f6877bbccb84e1234b37e3b4389`;
- materialization wiring `e2ab8205aebee2e52331bb7c9b49e829f50b5698`;
- v89 release metadata `14819ad95fac3f11b53a2dd2807fabf255828f28`;
- workflow `34423453651` then exposed a whitespace-sensitive test assertion only;
- semantic whitespace-safe assertion `066f4bcc71187b885b28244e94537e3a19ea4016`;
- final full workflow `34423535719`: SUCCESS.

## Required next physical evidence

1. Install exact v89 over v88.
2. Before natural preview, say `lights on`; HA should act and Android BOOP should speak.
3. Tap Emma once.
4. Record/photo `BOOP DEV E###` and the short detail, or report if Emma actually speaks.
5. Close diagnostic and say `lights on` again; Android BOOP must still speak.

Use the code to choose the next root-cause path. Do not blindly alter model config/audio after this instrumentation.

## Canonical rebuild prompt now loaded

Ryan supplied and approved `BOOP CANONICAL REBUILD / FULL AUTONOMOUS GITHUB DEVELOPMENT RUN` as the next major project. It explicitly makes Natural Voices a completed prerequisite and then creates dedicated branch `boop-canonical-rebuild` from the exact verified `boop-unified` head containing that completed Natural Voice work.

Fresh hardware evidence overrides the prompt's assumption that Natural Voices are already finished. Therefore:

1. finish Natural Voices safely on `boop-unified`;
2. physically verify the natural speech subsystem;
3. fetch live `boop-unified` and `main`;
4. create `boop-canonical-rebuild` from that exact finished head;
5. execute the approved phase order there without routine approval pauses.

The target permanent architecture from that prompt is: **BOOP is one logical puppet. Eyes, hue, animation, speech/listening state, room, media state, headphones state and renderer ownership are data; Wall, Shield, launcher, Now Playing, media corner, phone, tablet and other surfaces display that same logical BOOP rather than independently implementing him.** Record this cross-app contract in shared context when the canonical rebuild actually begins/lands, not prematurely while Natural Voices remain unfinished.

Absolute visual rule for that rebuild: Ryan is visual QA. No screenshot/golden/perceptual/pixel/AI appearance acceptance. If canonical eye/blink authority is missing, uncertain, replaced, mutated, regenerated or unverifiable, stop and request `canonical-idle-blink-v1.zip`; never reconstruct or regenerate BOOP.

## Other durable Unified contracts

- Home Assistant remains local authority and basic control must not depend on cloud chat.
- Permanent approved eye master remains `unified/assets/boop-eyes/boopApprovedEyes.png`, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; never regenerate/destructively edit it.
- Eye hue remains procedural iris-only; default 190 degrees.
- Approved five-digit notification hands remain byte-locked. Android's original notification remains authoritative.
- Preserve one controller-owned 16 kHz microphone stream, BOOP permanent wake name, additive custom wake name and exact 1,600-sample / 100 ms wake-command bridge.
- Developer menu remains the accepted in-place `MainActivity` route.
- Unified routing remains recovery override -> TV/Leanback Shield -> Pixel 7 Pro Wall -> other non-TV >=600dp Wall -> smaller handheld Launcher.
- Clean Nvidia Shield HOME remains separate on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a merge.
- GitHub checks compilation, non-visual functional behavior, package/signature/integrity and security only. Ryan owns visual/device/acoustic acceptance.

## Protected rollback checkpoints

Current usable rollback:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Also preserve:

- `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`;
- v58 `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- v48 `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`;
- `checkpoint-boop-unified-v65-procedural-eyes`.
