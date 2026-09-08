# BOOP unified status

Updated 2026-09-08. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v59 uncensored speech

After physical acceptance of v58 natural wake, Ryan reported that spoken rename to adult/profane words became asterisks and BOOP then spoke those stars during five-sample training.

Root cause: BOOP's rename parser does not censor names. Android speech recognition was being allowed to use its default offensive-word masking because neither the tap-to-talk intent nor the post-wake command intent set `RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS`.

v59 requests `EXTRA_MASK_OFFENSIVE_WORDS=false` in both recognition paths. No wake detector, rename parser, local five-sample enrolment, HA routing, TTS, command bridge/window, powered wake/recovery, diagnostics, visuals, Launcher, Shield, package or signer is intentionally changed.

TDD / release evidence:

- RED `b021b28d3f635f884dbc144252834b315712c94b`, workflow `34256341474`: 3 wake-handoff tests ran, exactly the new uncensored-speech contract failed.
- patch `09971b5a734c23134ed1b326aa9cab5036674855` + materializer `3b2ebeb66c357b947bec4c793165d3e97a8b994f`; intermediate workflow reached 3/3 wake-handoff PASS.
- v59 bump `0f2fe9d12e473b122938d06d767facc646c65476`.
- first v59 run `34256571189` compiled/tested/signed but the verifier still expected v58 metadata; verifier-only fix `136b56e6faac8ce450b957ac3057a379c68c7b7b` changed no app behavior.

Final v59 receipt:

- built code `136b56e6faac8ce450b957ac3057a379c68c7b7b`
- version 59 / `1.2.13-unified-uncensored-speech`
- workflow `34257117357` SUCCESS
- artifact `BOOP-Unified`, ID `10068400476`
- artifact digest `sha256:f397a85aa4747a51394d0bbc42266658cc6609a355a25b210bd761d8813e023c`
- APK SHA-256 `7d48cc77407b69428bd2456b80cefbe56cb60f6f8326eb2e3686aa7b22bc7a2e`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- wake-handoff contracts 3/3 PASS
- Shield focused tests 58/58, zero failures/errors/skips
- unified focused tests 92/92, zero failures/errors/skips
- Launcher lint, signed assembly, package/version, manifest, signer, APK integrity and artifact upload PASS.

Detailed receipt: `docs/BOOP-V59-UNCENSORED-SPEECH-RECEIPT.md`.

CI/signer green. Physical profanity recognition pending because the active Android recognition service ultimately decides whether it honors the masking extra.

## Required Pixel acceptance

Install v59 over v58 without uninstalling. Rename BOOP by voice to an adult/profane word. The training prompt should contain the actual word rather than asterisks. Complete five repetitions, then confirm the new name wakes BOOP and a normal HA command works. Confirm permanent `BOOP` still works naturally without a pause.

If stars remain, capture the transcript/diagnostic evidence and investigate the Android recognizer service. Do not disturb wake sensitivity, custom-name streaming, the 100 ms bridge or rename parsing.

## Physically accepted rollback

v58 remains the exact physical rollback:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Ryan physically confirmed default `BOOP + command` works spoken slowly or quickly, and v57 evidence already proved natural Steve/Fred/Jeff names plus spoken five-sample rename.

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`. Never repoint either checkpoint.

## Protected AIO state

BOOP remains the permanent fallback wake name; custom names are additive. Any external power allows continuous wake; unplugged phone remains tap-to-talk. Preserve one 16 kHz microphone owner, local five-say profiles, streaming learned-name matching, default BOOP zero-trailing-blank behavior, the exact v56 100 ms bridge, silent wake handoff, silent no-match/timeout re-arm, pull-only `show diagnostics`, HA names/Home controls, locked eyes/hue/blink, headphones/puppetry, five-digit yellow hands, room isolation and Shield scaling.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a later merge. Ryan owns visual/device/acoustic acceptance. No automatic installs/grants or signer/package changes.
