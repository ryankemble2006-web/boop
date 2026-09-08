# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v57 streaming custom wake

Physical v56 result on Ryan's powered Pixel:

- `Steve` and `BOOP` both wake BOOP;
- tap-to-talk works;
- a deliberate pause between wake name and command allows `lights off` to work and BOOP says `Done`;
- natural one-breath wake + command still fails.

Pull-only diagnostic captured after failed one-breath `Steve lights on`:

`WAKE ASR ERROR 7 +1502ms ready=28 begin=290 end=1405 partial=- final=-`

Sanitized state showed external power true, microphone permission true and recovery `ARMED`. Android ASR therefore saw speech boundaries but produced no transcript, while wake recovery itself worked.

Root cause: the learned custom-name matcher waited for two quiet 100 ms chunks before evaluating a trained name. Continuous `Steve lights on` was therefore treated as one longer utterance and custom wake detection arrived too late for the command.

v57 makes learned custom-name matching streaming during active speech, retains the old silence-ended path as fallback, and gates expensive feature extraction behind cheap speech activity. It does not change Sherpa/default `BOOP`, the v56 100 ms command bridge, the three-second command window, the silent wake seam, HA routing, five-say training, powered wake or microphone ownership.

## v57 receipt

- built code `6c8131200ee0ff8a91464569a982312cb5512cb2`
- version 57 / `1.2.11-unified-streaming-custom-wake`
- workflow `34249050741`, attempt 2 SUCCESS
- artifact `BOOP-Unified`, ID `10065473551`
- artifact digest `sha256:496786d407a39d8034ec4dfd335e903c4ae8e3531a1128bf1f3e4a82440d1912`
- APK SHA-256 `3ce0593f61fbf6b35e6fbb664bc5bc7984b053844c7f6be83afe2955ab713459`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused tests 58/58, zero failures/errors/skips
- unified focused tests 92/92, zero failures/errors/skips
- no-trailing-silence custom-wake regression PASS
- unrelated-continuous-speech rejection PASS
- seamless wake-command handoff contract PASS
- Launcher lint, signed assembly, package/version, manifest, signer and APK integrity PASS
- artifact upload PASS.

TDD RED was `032ba991f4f70880bd2c108473c34b7701db7917`, workflow `34248072092`: 92 focused unified tests ran and exactly the new no-pause custom-wake regression failed. Initial GREEN `964142749a2560b90a8be608b5c5deaa0f893066` passed but was not shipped after review found its sliding-window matching too computationally eager. Final `6c813...` gates heavy work to active speech.

Detailed receipt: `docs/BOOP-V57-STREAMING-CUSTOM-WAKE-RECEIPT.md`.

## Required Pixel acceptance

Install v57 over v56 and keep the Pixel powered. Without deliberate pauses, test:

1. `Steve lights on`
2. `Steve show diagnostics`
3. `BOOP lights off`

The first two are the v57 target. The third keeps default Sherpa BOOP separate. If Steve works but BOOP still needs a pause, debug the default wake seam separately and do not widen the command bridge blindly.

Physical v57 acceptance is pending.

## Protected AIO state

BOOP remains the permanent fallback wake name; custom names are additive. Any external power allows continuous wake; unplugged phone remains tap-to-talk. Preserve one 16 kHz microphone owner, local five-say profiles, the v56 exact 100 ms command bridge, silent wake handoff, silent no-match/timeout re-arm, pull-only `show diagnostics`, HA names/Home controls, locked eyes/hue/blink, headphones/puppetry, five-digit yellow hands, room isolation and Shield scaling.

The exact physically proven v48 wake rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`; do not repoint it.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a later merge. Ryan owns visual/device/acoustic acceptance. No automatic installs/grants or signer/package changes.
