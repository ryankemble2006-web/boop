# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v56 seamless wake command

Physical v55 result on Ryan's charged Pixel:

- `Steve` and `BOOP` both wake BOOP.
- tap-to-talk works.
- `Steve`, pause, `lights off` works and BOOP says `Done`.
- `BOOP`, pause, `lights off` works and BOOP says `Done`.
- one-breath wake + command (`Steve lights on`, `Steve show diagnostics`, BOOP/Steve `lights off`) does not route the command.
- each wake plays the artificial wake-accepted speaker bing.

This isolates the v55 failure to the wake-to-command seam. Wake detection, custom/default names, tap ASR, local HA and reply TTS are physically working.

Root cause: the wake recorder reads 1,600 samples at 16 kHz (100 ms) per detector block and writes that block to the ring before detection. v54's zero-prelude policy removed the old one-second wake recording but also discarded this final detector block, so immediate command onset can be lost. The wake callback also played a 90 ms speaker cue into the live command-capture period.

v56 keeps only the final 1,600 samples as a 100 ms bridge and removes the cue from the materialized wake callback. It does not restore the old one-second pre-roll.

Final v56 receipt:

- built code `68bdbb4aabfbefd383a48aa4764568c5cb2222dc`
- version 56 / `1.2.10-unified-seamless-wake-command`
- workflow `34246347404` SUCCESS
- artifact `BOOP-Unified`, ID `10064218458`
- APK SHA-256 `5212b2faf4286db17b3afa44d8174d5773d555d9a8779c5d7fb1e7e6aba6a13c`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused tests 58/58, unified focused tests 90/90, zero failures/errors/skips
- seamless materialized handoff contract PASS
- Launcher lint, signed assembly, package/version, manifest, signer and APK integrity PASS

CI/signer green. Physical v56 acceptance pending. Detailed receipt: `docs/BOOP-V56-SEAMLESS-WAKE-COMMAND-RECEIPT.md`.

## TDD evidence

RED workflow `34245905089` at `4a92e4cf1bf84e50f2b37cc8fae6bf590d1375bb` failed specifically because the wake callback still contained `playWakeAcceptedCue();`. The final 100 ms policy and silent seam then passed the selected regression contracts and the full focused wake suite.

## Required Pixel test

Install v56 over v55 and keep the Pixel powered. Test without deliberate pauses:

1. `Steve lights on`
2. `BOOP lights off`
3. `Steve show diagnostics`

There should be no artificial wake bing. Then deliberately wake without a command once and verify silent re-arm by issuing another wake + command immediately afterward.

If one-breath speech still fails, use `Steve`, pause, `show diagnostics` if needed. Do not restore the old one-second pre-roll or change sensitivity before reading the physical trace.

## Physically proven rollback checkpoint

The exact built v48 wake-arm code remains pinned at `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. On Ryan's Pixel: charger -> green Android mic indicator ON -> BOOP sleeps while green remains ON -> `Hey BOOP` wakes BOOP. Do not repoint this checkpoint.

## Protected AIO state

BOOP remains the permanent fallback wake name; custom names are additive. External power allows continuous wake; unplugged phone remains tap-to-talk. Preserve one 16 kHz microphone owner, the three-second command window, local five-say profiles, silent no-match/timeout recovery, pull-only `show diagnostics`, HA names/Home controls, locked eyes/hue/blink, headphones/puppetry, five-digit yellow hands, room isolation and Shield scaling.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a later merge. Ryan owns visual/device/acoustic acceptance. No automatic installs/grants or signer/package changes.
