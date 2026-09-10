# BOOP unified handoff

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Protected usable rollback: v88 Android voice restored

Exact physically accepted app/test release head:

`f5f086fc4f67712b5746be067aff852331299bb0`

Protected checkpoint:

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Ryan physically confirmed exact v88 is usable: ordinary Android TextToSpeech replies work. Natural voice still fails, but a failed natural preview no longer silences ordinary BOOP speech. This checkpoint means **working Android BOOP with natural speech isolated**, not natural speech accepted. Never repoint it and do not weaken its runtime-proof selection gate.

v88 receipts:

- versionCode `88`;
- versionName `1.2.88-unified-natural-playback-recovery`;
- workflow `34418073143`: SUCCESS;
- Shield routing `34418073200`: SUCCESS;
- artifact `10129945925`;
- APK SHA-256 `8fca19f2005b8a252488978e0efc7f7391711207d06ac1557088648a0402e108`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Current signed test candidate: v90 Kokoro preflight repair

Exact source/build head:

`97688d1606e7ac1665a8626a00fed9111f062135`

Release identity:

- versionCode `90`;
- versionName `1.2.90-unified-kokoro-preflight-fix`;
- package `com.boop.alpha1`;
- launch activity `com.boop.alpha1.UnifiedEntryActivity`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Verification:

- canonical full workflow `34426014943`: **SUCCESS**;
- separate Shield HOME routing workflow `34426014914`: **SUCCESS**;
- artifact `BOOP-Unified`, ID `10132764611`, size `63,994,301` bytes;
- artifact ZIP SHA-256 `e9cda859f89198fdc9cbc87b56c9c4322ec4d6d1fa51f2e04e6f8f6fb99544ae`;
- APK SHA-256 `94b63c350d50029d875e40bd4e18b839addad020155de231c918de8bbc3e8275`;
- Shield focused tests `58/58`, zero failures/errors/skips;
- Unified focused tests `157/157`, zero failures/errors/skips;
- signed APK/package/version/entry activity/permanent signer/archive checks: SUCCESS.

The GitHub artifact was downloaded after CI and independently checked. ZIP integrity passed, `built-commit.txt` matched `97688d1606...`, the APK SHA matched `apk-sha256.txt`, package/version matched v90 and the permanent signer receipt matched the protected signer.

**v90 is not yet physically accepted and does not mean Natural Voices are finished.** It removes the false E890 preflight blocker so the next device run can reach actual Sherpa initialization/synthesis/playback evidence or speak naturally.

## v89 physical evidence and root cause

v89 diagnostic release head was:

`066f4bcc71187b885b28244e94537e3a19ea4016`

Ryan's exact physical result on v89:

- command `lights off` completed successfully;
- Android BOOP voice replied, proving the v88 fallback/isolation architecture remained healthy;
- tapping natural voice produced `BOOP DEV E890`;
- short detail: `IllegalStateException: Natural voice runtime files incomplete`.

E890 fires before `OfflineTts` construction. Investigation compared BOOP's preflight list with both BOOP's pinned `natural-voices/manifest.json` and Sherpa's official `kokoro-multi-lang-v1_0` contents. v89 had accidentally invented two additional runtime requirements:

- `inno/nif_model.onnx`;
- `inno/possible_tokens.txt`.

Those files are not required by BOOP's pinned pack manifest and are not part of the official Kokoro v1 runtime path BOOP uses. Therefore v89 guaranteed a false E890 even when the installed pack was valid.

The real BOOP/Sherpa preflight remains:

- `model.onnx`;
- `voices.bin`;
- `tokens.txt`;
- `lexicon-gb-en.txt`;
- readable, non-empty `espeak-ng-data/`.

No pack redownload should be required merely because v89 reported E890.

## v90 TDD lineage

Work was isolated first on `boop-natural-v90-runtime-files`, based exactly on then-live `boop-unified@beef30882c617cf1cffddd893e3ef4022813c216`.

Regression coverage:

- `source-test/BoopNaturalVoiceRuntimeFilesTest.java` creates the exact real runtime-file shape and requires `runtimeFilesReadyForSherpa(...)` to accept it; it also verifies a genuinely missing required input is rejected.
- `tests/test_unified_v85_natural_voice_install_flow.py` now requires `SHERPA_RUNTIME_FILES` to equal the file entries from `natural-voices/manifest.json` excluding the separately validated `espeak-ng-data` directory, and explicitly forbids invented `inno/` paths.

RED evidence:

- workflow `34425786847`, job `102710598737`;
- exactly **1 failed, 9 passed**;
- failure listed only the two bogus extras `inno/nif_model.onnx` and `inno/possible_tokens.txt`.

Minimal production fix:

- commit `86970a634565b7ed9a3efcaa7fd8433dc61af797`;
- removed only those two invented file requirements from `source/BoopNaturalSpeechBackend.java`;
- no Kokoro model config, SIDs, Android fallback, AudioTrack, wake, HA, launcher, eyes or diagnostics behavior changed.

Focused GREEN:

- workflow `34425864351`: **SUCCESS**.

Release lineage was then version-bumped to v90 and the temporary branch-only workflow removed before fast-forward promotion. Net production/test diff from the v89 docs head is limited to:

- `source/BoopNaturalSpeechBackend.java`;
- `source-test/BoopNaturalVoiceRuntimeFilesTest.java`;
- `tests/test_unified_v85_natural_voice_install_flow.py`;
- `unified/app-build.gradle`.

## Natural voice diagnostic ladder retained

v90 keeps the debug-only v89 full-screen diagnostic unchanged:

| Code | Meaning |
| --- | --- |
| `BOOP DEV E890` | Actual required natural runtime files incomplete/unreadable. |
| `BOOP DEV E891` | Sherpa/Kokoro `OfflineTts` initialization or runtime metadata failed. |
| `BOOP DEV E892` | Model initialized, but synthesis failed or returned no samples. |
| `BOOP DEV E893` | Synthesis returned audio, but Android PCM16 playback failed. |
| `BOOP DEV E899` | Unknown natural-voice stage. |

Never expose tokens, passwords, private network addresses, signing details or personal data in these diagnostics.

## Physical test required next

1. Install exact v90 over v89. Existing verified voice pack should remain installed; do not redownload unless BOOP itself says the pack is absent.
2. Before pressing a natural row, say `lights off` or `lights on`. HA should act and Android BOOP should speak normally.
3. Open Voice Settings and tap **Emma once**.
4. Report the new `BOOP DEV E###` code plus the short detail, or report if Emma actually speaks.
5. Close the diagnostic and issue another light command. Android BOOP must still speak if natural preview failed.

Interpretation:

- `E890` again: inspect which **real** required file/directory is unreadable on-device; do not reintroduce `inno` requirements.
- `E891`: preflight passed; focus only on Sherpa native/model initialization/config/path/runtime metadata.
- `E892`: `OfflineTts` initialized; focus on Kokoro generation/frontend/text/speaker input, not AudioTrack.
- `E893`: synthesis returned audio; focus only on Android PCM16 playback.
- Emma speaks: test at least one other speaker, then a normal BOOP command with natural speech selected, before declaring Natural Voices finished.
- Android speech breaks at either light-command check: return to protected v88 and debug the regression before proceeding.

## Natural speech contracts that remain protected

- Natural pack: `kokoro-multi-lang-v1_0`.
- Emma `bf_emma` SID 21; Isabella `bf_isabella` SID 22; Fable `bm_fable` SID 25; George `bm_george` SID 26.
- Candidate preview is not selection.
- Persist a natural voice and `natural_runtime_proven_version` only after the requested natural preview actually completes successfully.
- Ordinary speech may route natural only when the current pack version has runtime proof.
- Failed natural preview never substitutes Android TTS and pretends it was the requested natural voice.
- Failed natural preview must leave ordinary Android TTS available.
- Keep synthesis on `tts.generateWithConfig(text, generation)`.
- Never restore `generateWithConfigAndCallback(...)` while shipping Sherpa 1.13.7; v86 exposed its Android JNI abort path.
- Natural playback remains signed PCM16; do not restore float PCM or `PlaybackParams` while this fault is unresolved.
- Natural pitch remains deferred; Android TTS pitch remains unaffected.
- Keep explicit `lexicon-gb-en.txt`; do not force `lang="eng"` without new evidence.
- Pack download/install remains app-private, pinned HTTPS + exact SHA/size verification, streaming SHA, explicit extraction, cooperative cancellation, traversal/link rejection and safe activation.

## Canonical rebuild prompt is loaded, approved, and queued

Ryan supplied the full `BOOP CANONICAL REBUILD / FULL AUTONOMOUS GITHUB DEVELOPMENT RUN` prompt. Its accepted architecture is: **BOOP is one logical puppet; state is data; surfaces render that same puppet.** It grants scoped approval for the ordered rebuild without routine phase-by-phase approval.

Its Phase 1 assumes Natural Voices are already finished. They are not yet physically proven. Therefore:

1. finish Natural Voices on `boop-unified` while protecting v88;
2. physically prove them;
3. fetch live `boop-unified` and `main`;
4. create dedicated `boop-canonical-rebuild` from the exact verified voice-working head;
5. execute the supplied rebuild phases there, not directly on `boop-unified`.

Do not create the canonical rebuild branch early. If canonical eye/blink assets are missing, uncertain, mutated, regenerated or unverifiable during that later rebuild, stop and request `canonical-idle-blink-v1.zip`. Ryan remains the only visual QA.

## Other protected contracts

- Permanent approved eye master remains `unified/assets/boop-eyes/boopApprovedEyes.png`, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; never regenerate/destructively edit it.
- User eye hue remains procedural iris-only; default 190 degrees.
- Exact approved notification hands remain byte-locked.
- Preserve one 16 kHz microphone owner, accepted wake/name architecture and exact 100 ms wake bridge.
- Unified routing remains recovery override -> TV/Leanback Shield -> Pixel 7 Pro Wall -> other non-TV >=600dp Wall -> smaller handheld Launcher.
- Clean Nvidia Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a merge.
- GitHub performs non-visual functional/build/signing verification only. Ryan owns physical, visual and acoustic acceptance.

## Other historical rollbacks

Also preserve:

- `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`;
- v58 `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`;
- v48 `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`;
- `checkpoint-boop-unified-v65-procedural-eyes`.
