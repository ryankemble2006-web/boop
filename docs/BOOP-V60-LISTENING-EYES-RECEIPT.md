# BOOP Unified v60 listening-eyes receipt

Date: 2026-09-08

Canonical branch: `boop-unified`
Package: `com.boop.alpha1`
Signer: permanent BOOP signer, unchanged

## User request and visual boundary

Ryan asked for a tiny eye behavior that clearly shows when BOOP is actively listening. A generated replacement-eye concept was explicitly rejected. The accepted implementation therefore uses **only the existing locked BOOP eye master** and changes runtime pose/state, not artwork.

Locked eye asset remains the approved paired black-lidded eye master. No PNG bytes are regenerated or substituted. Existing iris-only hue handling, blink timing/gates, geometry and approved face identity remain intact.

## v59 physical acceptance carried forward

Immediately before this work, Ryan physically confirmed v59's uncensored-speech change on the Pixel: spoken adult/profane wake-name rename and five-sample training completed with the actual word rather than Android's asterisks. Treat v59 uncensored recognition as physically passed on this device.

Protected exact v59 rollback:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

Never repoint it.

## v60 behavior

The listening cue is deliberately tied to **active recognizer ownership**, not merely to powered/armed wake state.

When tap-to-talk or post-wake command ASR is actively listening:

- the existing approved eye renderer enters a gentle vertical attentive/breathing pulse;
- half-cycle is `520 ms`;
- vertical scale runs from `1.025` to `1.060`;
- if Android animations are disabled, the renderer uses the static maximum attentive pose;
- the old static listening alpha-dim cue is removed from these recognizer start/stop paths.

The cue stops when recognition ends/errors/cancels, BOOP goes idle/sleeps, thinking begins, the Activity pauses, or the Activity is destroyed. Existing `listening` state continues to block idle blink while command ASR owns the interaction.

No wake detector, custom-name matcher, microphone ownership, command bridge/window, powered wake/recovery policy, recognizer intent, Home Assistant routing, TTS, package, signer, Launcher or Shield behavior is intentionally changed.

## TDD trail

RED:

- test file `source-test/BoopWakeListeningCueStateTest.java`
- commit `5e2e0160994d44804f33a06faef3bd668d8a57d4`
- workflow `34258998143`
- failure was exactly the missing `BoopListeningCueState` symbol required by the new lifecycle contract.

GREEN implementation:

- pure lifecycle helper `source/BoopListeningCueState.java`
- materialization patch `scripts/patch-unified-listening-eyes.py`
- materializer wiring in `scripts/materialize-unified.sh`
- initial fail-closed materialization exposed composition with the existing sleep-charm patch; the listening patch was adapted rather than bypassing the existing eye lifecycle
- code-green commit `921e221e608e900537bb6d6c5797fc1cab7ee5ad`
- workflow `34259741946` passed materialization, lint, focused functional tests, signing, assembly, package/signature/integrity and artifact upload
- maintenance review then made the listening-eye materializer idempotent at commit `490eb26b286c33e29e4d4a2e1ca497379bec4c61` without changing runtime behavior.

## Final v60 build

- built commit `47e2edb1cd4415d8716a108cfc0f0cf7fb82de8a`
- version 60 / `1.2.14-unified-listening-eyes`
- workflow `34260135850` SUCCESS
- artifact `BOOP-Unified`, ID `10069626604`
- artifact digest `sha256:533672bee255644d6db50a60d0bc1dfe8d46b1184f46d9a2e6504b25197ac3a0`
- APK SHA-256 `4478be2d4b684ff2688fea5ae662a205bcce0ce5da1e25f162b4ef7ed411d1c6`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- materialized wake-handoff contracts 3/3 PASS
- Shield focused tests 58/58, zero failures/errors/skips
- unified focused tests 93/93, zero failures/errors/skips
- Launcher lint PASS
- signed assembly PASS
- package/version/manifest/permanent-signer/APK ZIP integrity PASS
- artifact upload PASS

GitHub performs non-visual verification only. No emulator/device launch, screenshots or automated visual acceptance were performed.

## Required physical Pixel acceptance

Install v60 over v59 without uninstalling.

1. While powered, use a natural wake-name + command. The exact approved eyes should visibly enter the listening pulse only during command recognition, then stop before/when BOOP begins the reply such as `Done`.
2. Tap-to-talk should show the same listening cue while ASR is active and return to the ordinary approved eye state afterward.
3. Confirm both the current custom wake name and permanent fallback `BOOP` still execute natural no-pause Home Assistant commands.
4. Confirm ordinary powered wake-armed waiting does **not** continuously pulse the eyes.

Ryan owns the visual judgment. If the pulse is too strong/weak/fast/slow, tune only the listening pose parameters after capturing the real-device observation. Do not regenerate the eye artwork or disturb the accepted wake/audio boundary.

## Checkpoint policy

Do **not** create a v60 physical checkpoint until Ryan visually accepts this exact signed APK. Until then, v59 is the most recent physically accepted rollback and v58 remains the protected natural-wake rollback.
