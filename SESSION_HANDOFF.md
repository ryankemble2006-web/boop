# BOOP unified handoff

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Immediate next action

Physical-test **BOOP Unified v91** on the device that produced v90 E893. Do not begin the queued Canonical Rebuild until Natural Voices are physically proven.

Exact v91 canonical source/build head:

`11650313221ae5bf997dbb93b6a905bfdc7da1ed`

Release identity and verification:

- versionCode `91`;
- versionName `1.2.91-unified-static-track-state-fix`;
- package `com.boop.alpha1`;
- launch activity `com.boop.alpha1.UnifiedEntryActivity`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- canonical Unified workflow `34433115316`: **SUCCESS**;
- canonical Shield HOME routing workflow `34433115318`: **SUCCESS**;
- canonical artifact `BOOP-Unified`, ID `10135283428`;
- artifact ZIP SHA-256 `e4f2fb47e2a3b5db2d338512b44ebdbfa840a42b03acc99a5bce02243f8c820c`;
- APK SHA-256 `42dc50d12031a674aa751918f6bfd6b4deab8b6ced95332a437f4068124fe53d`;
- Shield focused tests `58/58`, zero failures/errors/skips;
- Unified focused tests `157/157`, zero failures/errors/skips;
- signed APK/package/version/entry activity/permanent signer/archive checks: SUCCESS.

The canonical artifact was downloaded after CI and independently checked. `built-commit.txt` matches `11650313221...`, the APK matches its bundled SHA receipt, badging reports v91 + `UnifiedEntryActivity`, and the signer receipt matches the permanent signer.

**v91 is CI/build verified but not physically accepted.** Ryan remains the physical/visual/acoustic authority.

## Protected usable rollback: v88

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Ryan physically confirmed v88 restores ordinary Android TextToSpeech and keeps a failed Natural Voice attempt from poisoning normal BOOP speech. Never repoint this checkpoint or weaken its runtime-proof isolation. Keep it as the usable rollback until Natural Voices are physically accepted.

## Physical evidence that selected v91

### v89

- `lights off` executed;
- Android BOOP spoke normally;
- Emma preview produced `BOOP DEV E890`;
- detail: `IllegalStateException: Natural voice runtime files incomplete`.

Root cause was BOOP's own new preflight accidentally requiring two files not present in the pinned Kokoro v1 runtime: `inno/nif_model.onnx` and `inno/possible_tokens.txt`.

### v90

v90 removed only those bogus file requirements. Ryan then physically reported:

- E890 was gone;
- Emma preview produced `BOOP DEV E893`;
- detail: `IllegalStateException: Natural speech audio output unavailable`.

This is crucial evidence: v90 passed runtime-file preflight, Sherpa/Kokoro initialization, and synthesis returned audio. The fault was therefore in Android playback before generated PCM was written.

## v91 root cause and exact repair

`BoopNaturalSpeechBackend` uses signed PCM16 and an `AudioTrack` in `MODE_STATIC` for the generated utterance. Android's static-track state contract reports `STATE_NO_STATIC_DATA` for a successfully initialized static track before data is written. v90 checked:

`track.getState() != AudioTrack.STATE_INITIALIZED`

before `track.write(...)`, so a healthy static track could be rejected as unavailable simply because it had not received its samples yet.

v91 changes only that production condition to reject:

`track.getState() == AudioTrack.STATE_UNINITIALIZED`

Production fix commit:

`cd75e7b6e6344bf8122e30c2e4a025529be60cab`

The first playback hypothesis was to switch to streaming; investigation found the smaller Android state-contract explanation before production code was changed. Keep `MODE_STATIC` for now. Do not redesign the playback transport unless new physical evidence requires it.

## v91 TDD lineage

Isolated branch: `boop-natural-v91-audiotrack-stream`.

RED:

- workflow `34432449031`;
- job `102730608219`;
- exactly `1 failed, 31 passed`;
- the single failure was the new regression requiring a static track to accept Android's pre-write `STATE_NO_STATIC_DATA` state and reject only `STATE_UNINITIALIZED`.

GREEN:

- minimal production fix: `cd75e7b6e6344bf8122e30c2e4a025529be60cab`;
- natural/wake stages on `34432516657` went green before that run was superseded by the v91 version-stamp push;
- full isolated v91 workflow `34432642013`: **SUCCESS**;
- canonical promoted workflow `34433115316`: **SUCCESS**;
- canonical Shield routing `34433115318`: **SUCCESS**.

Net effective code/test/version diff from the v90 live base is limited to:

- one production condition in `source/BoopNaturalSpeechBackend.java`;
- the v91 static-track regression in `tests/test_unified_v85_natural_voice_install_flow.py`;
- versionCode/versionName in `unified/app-build.gradle`.

The temporary WIP workflow trigger was removed before promotion. No Kokoro model config, pack contents, natural SIDs, HA routing, wake architecture, approved eyes, launcher behavior, package identity or signer changed.

## v91 physical test procedure

1. Install the exact v91 APK over v90. Do **not** redownload the Natural Voice pack unless BOOP explicitly says it is absent.
2. Say `lights off` or `lights on`. HA should act and ordinary Android BOOP speech should still work.
3. Open Voice Settings and tap **Emma once**.
4. If Emma speaks, tap one second Natural Voice, preferably **George** or **Isabella**.
5. Then issue a normal BOOP/HA command and confirm the reply is spoken with the selected Natural Voice.
6. If any natural preview fails, record the giant `BOOP DEV E###` plus the short detail beneath it. Then issue another light command; ordinary Android TTS must still speak.

Do not mark Natural Voices finished from Emma alone. Require at least one second speaker plus normal routed BOOP speech.

## Diagnostic ladder

| Code | Meaning |
| --- | --- |
| `BOOP DEV E890` | A real required runtime file/directory is incomplete or unreadable. |
| `BOOP DEV E891` | Sherpa/Kokoro `OfflineTts` initialization or runtime metadata failed. |
| `BOOP DEV E892` | Model initialized but synthesis/generation failed or returned no samples. |
| `BOOP DEV E893` | Synthesis returned audio but Android PCM16 playback failed. Use the short detail to distinguish creation/write/play failures. |
| `BOOP DEV E899` | Unknown natural failure stage. |

Diagnostics remain debug-only, photographable and sanitized. Never expose tokens, passwords, private addresses, credentials, signing material or personal data.

## Natural Voice contracts that remain locked

- Pack: `kokoro-multi-lang-v1_0`.
- Emma `bf_emma` SID 21; Isabella `bf_isabella` SID 22; Fable `bm_fable` SID 25; George `bm_george` SID 26.
- Candidate preview is not selection.
- Persist a Natural Voice and `natural_runtime_proven_version` only after the requested natural preview completes successfully.
- Normal BOOP speech may route natural only when selection + current-pack runtime proof match.
- Failed natural preview must leave Android TTS available and must not substitute Android TTS while pretending the natural preview succeeded.
- Keep synthesis on `tts.generateWithConfig(text, generation)`.
- Never restore Sherpa 1.13.7 `generateWithConfigAndCallback(...)`; v86 exposed its Android JNI abort path.
- Natural playback remains signed PCM16 with no `PlaybackParams`; natural pitch remains deferred until basic playback is physically proven.
- Keep explicit `lexicon-gb-en.txt`; do not force `lang="eng"` without new evidence.
- Pack installation remains app-private, pinned HTTPS + exact SHA/size verification, streaming digest, explicit extraction, cooperative cancellation, traversal/link rejection and safe activation.

## Canonical rebuild remains queued behind physical voice acceptance

Ryan supplied and approved the full `BOOP CANONICAL REBUILD / FULL AUTONOMOUS GITHUB DEVELOPMENT RUN`. Its architecture is: **BOOP is one logical puppet; state is data; every surface renders that same puppet rather than independently implementing him.**

Do not create `boop-canonical-rebuild` yet. Once Natural Voices are physically accepted:

1. fetch live `boop-unified` and `main`;
2. create `boop-canonical-rebuild` from the exact physically verified voice-working `boop-unified` head;
3. execute the approved rebuild phases there without routine approval stops;
4. inspect already-landed media improvements before adding anything so working media code is not duplicated or redesigned.

If canonical eye/blink authority is missing, uncertain, mutated, regenerated or unverifiable during that rebuild, stop and request `canonical-idle-blink-v1.zip`. Never recreate BOOP from memory. Ryan is the only visual QA.

## Other protected contracts

- Approved eye master: `unified/assets/boop-eyes/boopApprovedEyes.png`, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; never regenerate/destructively edit it.
- Exact approved notification hands remain byte-locked.
- Preserve one controller-owned 16 kHz microphone stream and exact 1,600-sample / 100 ms wake-command bridge.
- Unified routing remains recovery override -> TV/Leanback Shield -> Pixel 7 Pro Wall -> other non-TV >=600dp Wall -> smaller handheld Launcher.
- Clean Nvidia Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a merge.
- GitHub performs non-visual functional/build/signing verification only. Ryan owns physical, visual and acoustic acceptance.

## Historical rollbacks

Also preserve:

- `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`;
- v58 `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- v48 `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`;
- `checkpoint-boop-unified-v65-procedural-eyes`.
