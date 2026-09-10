# BOOP unified status

Updated 2026-09-10. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Local continuation and emulator inspection (2026-09-10)

Ryan requested continuing the language/Natural Voice work locally, reusing GitHub for suitable non-visual checks to reduce credit use, and using local emulator inspection or asking Ryan for visual evidence. No GitHub visual tests are authorized.

- Recovered the canonical worktree at `C:/Users/ryank/Documents/Codex/BOOP/.worktrees/boop-unified` from live GitHub `625257e7f0706c42286e2632bb5b8ac7c1be3d40`; main checked at `5179f95961c9c43b4939dd1ea4349a32eb7f99d1`.
- Reused existing successful GitHub run `34433115316` and artifact `10135283428`; independently downloaded and matched the exact ZIP and APK SHA-256 receipts above. No application code or new build was needed for this recovery.
- Installed exact v91 into the local `Pixel_7_Pro_API_36` emulator. BOOP launched; the optional notification setup was skipped. The emulator reports a generic model and initially routes to Launcher, so Wall `com.boop.alpha1/.MainActivity` was opened explicitly for this inspection.
- Direct manual inspection showed the Wall idle-black state and BOOP eyes appearing after a tap. This is limited emulator launch/render/interaction evidence, not acceptance of all appearance or voice behavior. No app crash appeared in the inspected AndroidRuntime log.
- Natural Voice settings/previews, second speaker, normal routed natural reply, and real-device HA/acoustic behavior remain unverified in this continuation. Ryan confirmed he had not received the APK, so no v91 physical result exists yet; supplied the exact candidate locally as requested. Do not infer acceptance from emulator launch.
- v88 remains the protected usable physical rollback. The canonical rebuild remains gated on physical Natural Voice acceptance.

## Current physical rollback: v88 Android voice restored

`checkpoint-boop-unified-v88-android-voice-restored` -> `f5f086fc4f67712b5746be067aff852331299bb0`

Ryan physically confirmed exact v88 is usable: normal Android TextToSpeech replies work and failed Natural Voice experiments no longer silence ordinary BOOP speech. Natural speech itself is not accepted at this checkpoint. Never repoint it or weaken the runtime-proof fallback gate.

## Current signed test candidate: v91

Exact canonical source/build head:

`11650313221ae5bf997dbb93b6a905bfdc7da1ed`

Identity and receipts:

- versionCode `91`;
- versionName `1.2.91-unified-static-track-state-fix`;
- package `com.boop.alpha1`;
- launch activity `com.boop.alpha1.UnifiedEntryActivity`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- canonical full Unified workflow `34433115316`: **SUCCESS**;
- canonical Shield HOME routing workflow `34433115318`: **SUCCESS**;
- artifact ID `10135283428`;
- artifact ZIP SHA-256 `e4f2fb47e2a3b5db2d338512b44ebdbfa840a42b03acc99a5bce02243f8c820c`;
- APK SHA-256 `42dc50d12031a674aa751918f6bfd6b4deab8b6ced95332a437f4068124fe53d`;
- Shield focused tests `58/58`;
- Unified focused tests `157/157`;
- package/version/entry activity/signer/ZIP checks: SUCCESS.

The canonical artifact was downloaded after CI. Its `built-commit.txt`, signer receipt, APK checksum and badging were checked independently against the values above. v91 is **not physically accepted yet** and Natural Voices are **not yet finished**.

## v90 physical result

Ryan installed v90 and reported:

- the prior false `E890` was gone;
- Emma preview reached `BOOP DEV E893`;
- detail: `IllegalStateException: Natural speech audio output unavailable`.

That result proves the installed runtime files passed preflight, Sherpa/Kokoro initialized, and synthesis returned audio. The remaining failure was inside Android playback before the generated PCM was written.

## E893 root cause

v90 creates the generated utterance as a signed PCM16 `AudioTrack` in `MODE_STATIC`. Android's static-track state contract uses `STATE_NO_STATIC_DATA` for a successfully initialized static track before sample data has been written. BOOP incorrectly treated every state other than `STATE_INITIALIZED` as construction failure **before `write(...)`**.

Therefore a healthy newly created static track could be rejected with exactly the observed `Natural speech audio output unavailable` E893.

The v91 repair is deliberately minimal:

- keep `MODE_STATIC`;
- keep signed PCM16;
- keep the existing whole-utterance buffer/write/play path;
- keep the existing AudioAttributes/session/sample-rate strategy;
- reject only `AudioTrack.STATE_UNINITIALIZED` before the write.

No Kokoro model files/config, voice SIDs, Android-TTS fallback, HA routing, wake architecture, launcher behaviour, approved eyes, package ID or signer changed.

## v91 TDD / build proof

Work was isolated on `boop-natural-v91-audiotrack-stream`; the branch name reflects the first playback hypothesis, but the evidence selected the smaller state-contract fix instead of a streaming redesign.

RED:

- workflow `34432449031`, job `102730608219`;
- exactly `1 failed, 31 passed`;
- only the new static-track state-contract regression failed against v90 behaviour.

Minimal production fix:

- commit `cd75e7b6e6344bf8122e30c2e4a025529be60cab`;
- one production condition changed from `state != STATE_INITIALIZED` to `state == STATE_UNINITIALIZED`.

GREEN:

- focused natural/wake stages on `34432516657`: SUCCESS before that run was superseded by the version-stamp push;
- full isolated v91 workflow `34432642013`: **SUCCESS**;
- canonical promoted workflow `34433115316`: **SUCCESS**;
- canonical Shield routing `34433115318`: **SUCCESS**.

## Physical test required next

1. Install exact v91 over v90. Do not redownload the Natural Voice pack unless BOOP explicitly says it is absent.
2. Say `lights off` or `lights on`; HA should act and Android BOOP should still speak.
3. Open Voice Settings and tap **Emma once**.
4. If Emma speaks, tap one second Natural Voice such as **George** or **Isabella**.
5. Then issue a normal BOOP/HA command and confirm the reply uses the selected Natural Voice.
6. If any preview fails, report the giant `BOOP DEV E###` plus the short detail, then issue another light command and confirm Android TTS still speaks.

Interpretation:

- `E890`: a real required file/directory is inaccessible.
- `E891`: Sherpa/Kokoro initialization failed.
- `E892`: initialization passed but synthesis/generation failed.
- `E893`: generated audio reached Android playback; use the new detail to isolate write/play/output failure.
- Emma plus a second speaker plus a normal natural reply all work: Natural Voices can be physically accepted, then the canonical rebuild gate may open.

## Protected Natural Voice rules

- Pack `kokoro-multi-lang-v1_0`.
- Emma `bf_emma`/21; Isabella `bf_isabella`/22; Fable `bm_fable`/25; George `bm_george`/26.
- Preview is not selection; persist natural only after successful requested natural playback.
- Failed preview must leave Android TTS available.
- Keep `tts.generateWithConfig(text, generation)` and never restore Sherpa 1.13.7 `generateWithConfigAndCallback(...)` after the v86 JNI-abort evidence.
- Natural playback remains signed PCM16 without `PlaybackParams`; natural pitch remains deferred until basic natural playback is physically proven.
- Keep explicit `lexicon-gb-en.txt`; do not force `lang="eng"` without new evidence.
- App-private pinned pack download/install safeguards remain intact.

## Canonical rebuild gate

The approved `BOOP CANONICAL REBUILD` prompt is queued but its Phase 1 assumes Natural Voices are finished. Do not create `boop-canonical-rebuild` until v91 (or a later exact voice-working build if needed) is physically verified for multiple Natural Voices and normal BOOP speech. Then fetch live `boop-unified` and `main`, create the rebuild branch from that exact verified head, and execute the approved phases there.

## Other protected contracts

- Approved eye master remains byte-locked at SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`; never regenerate it.
- Approved notification hands remain byte-locked.
- Preserve the single 16 kHz microphone owner and exact 100 ms wake bridge.
- Unified device routing remains unchanged.
- Clean Shield HOME remains standalone until explicit merge approval.
- Ryan owns visual/device/acoustic acceptance; CI performs non-visual verification only.

## Historical Natural Voice evidence

- v89: Android fallback worked; Emma produced false `E890` because BOOP's new diagnostic preflight invented two non-existent `inno/...` requirements.
- v90: removed those bogus requirements; physical test progressed to `E893`, proving files/init/synthesis passed and exposing the static `AudioTrack` state bug fixed in v91.

Also preserve v59, v58, v48 and the v65 procedural-eyes checkpoint. v88 remains the current usable rollback until Natural Voices are physically accepted.
