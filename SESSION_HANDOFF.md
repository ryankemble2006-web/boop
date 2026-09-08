# BOOP unified handoff

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Always re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current physically accepted wake candidate: v58 natural BOOP wake

Ryan physically tested signed v58 on the powered Pixel and confirmed the default permanent `BOOP` wake now works naturally without a deliberate pause. He specifically reported that spoken slowly or quickly, BOOP is happy to execute the command.

This closes the BOOP-only pause defect that remained after v57.

Previously physically accepted learned-name evidence remains valid:

- `Steve lights on` worked naturally with no deliberate pause.
- `Steve show diagnostics` worked naturally.
- spoken rename to `Fred` triggered the five-sample local enrolment flow; BOOP listened to five repeats and then `Fred lights on` worked with `Done`.
- spoken rename to `Jeff` repeated the same five-sample flow and `Jeff lights on` worked with `Done`.

Together, v57 + v58 physically prove both wake paths can support natural one-breath `wake name + command` speech: learned custom names use the streaming local matcher, while permanent fallback `BOOP` uses Sherpa without an intentional trailing-blank wait.

## v58 root cause and change

The remaining BOOP-only pause came from `BoopSherpaWakeSpotter` configuring Sherpa with `config.setNumTrailingBlanks(1);`. A deliberate pause supplied that blank; continuous `BOOP lights on` could delay the wake decision.

v58 changes only this setting to `config.setNumTrailingBlanks(0);`.

Preserved unchanged:

- BOOP keyword score, threshold and phrase assets;
- v57 streaming learned-name matcher and its quiet-room RMS gate;
- five-sample local training and no raw PCM persistence;
- one controller-owned 16 kHz microphone stream;
- v56 exact final 1,600-sample / 100 ms command bridge and no old one-second pre-roll;
- silent wake handoff and no artificial speaker bing;
- three-second command window;
- any-external-power continuous wake and unplugged tap-to-talk;
- post-wake no-match/timeout silent genuine re-arm and hard-start fail-safe latch;
- pull-only `show diagnostics`;
- HA, TTS, visuals, Launcher, Shield, package and permanent signer.

## v58 build and verification receipt

- built commit `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`
- version 58 / `1.2.12-unified-natural-boop-wake`
- workflow `34252950640` SUCCESS
- artifact `BOOP-Unified`, ID `10066828560`
- artifact digest `sha256:feb800dd99d6da15876892fbae4027def903bd43f3887b4b771c0e384a2ab372`
- APK SHA-256 `5a5b4846a55bc58d3444a8c8f178441af576e86695025c17c8d4483ad9fd01aa`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused tests 58/58, zero failures/errors/skips
- unified focused tests 92/92, zero failures/errors/skips
- materialized wake-handoff contracts 2/2 PASS
- Launcher lint, signed assembly, package/version, manifest, signer, APK ZIP integrity and artifact upload PASS

Detailed build receipt: `docs/BOOP-V58-NATURAL-BOOP-WAKE-RECEIPT.md`.

## Protected physical checkpoint

The exact v58 built code is pinned at:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Do not repoint this branch. It is the physically accepted rollback for the natural BOOP wake boundary and is tied to workflow `34252950640` and the signed APK hash above.

The older exact v48 wake-arm rollback remains permanently pinned at `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`; do not repoint it.

## Durable wake/name contracts

- `BOOP` permanently remains an accepted fallback wake name; custom names are additive, never replacements.
- five custom-name samples are local and use the existing single controller-owned 16 kHz PCM stream; raw enrolment PCM is not persisted.
- learned custom names must be matchable while speech is active, without requiring trailing silence.
- default BOOP must not intentionally wait for a trailing blank before trigger finalization.
- never add a competing microphone listener.
- never restore the old full one-second wake-history pre-roll.
- keep the exact final 1,600 detector samples / 100 ms command bridge unless new physical evidence justifies redesign.
- wake-to-command handoff remains acoustically silent.
- any external power permits continuous phone wake; unpowered phone remains tap-to-talk.
- after TTS, re-arm only after BOOP finishes speaking.
- no-match/timeout failures are silent and genuinely re-arm; hard wake-engine startup failures remain fail-safe latched.
- `show diagnostics` remains pull-only; normal failures do not pop a modal or speak an error.

## Remaining acceptance boundary

No new blanket claim is made about false-positive rate in arbitrary room chatter because Ryan did not explicitly report that observation in this acceptance turn. If future real-world listening reveals false positives, investigate Sherpa confirmation semantics before changing sensitivity, custom-name matching or the command bridge.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eyes, iris-only hue, blink timing/gates, headphones/puppetry and five-digit yellow hands. Preserve physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. Assistant remote invocation/audio remains separately unresolved. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
