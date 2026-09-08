# BOOP v59 uncensored speech receipt

Updated 2026-09-08. Canonical AIO branch `boop-unified`, package `com.boop.alpha1`, permanent signer unchanged.

## Physical symptom that motivated v59

On the physically accepted v58 wake baseline, spoken rename itself worked, but Android speech recognition masked offensive words into asterisks. BOOP then faithfully stored the masked transcript and TTS spoke the asterisks during the five-sample training prompt.

Source tracing showed BOOP's own `BoopWakeNameIntent` / `BoopWakeName` path does not contain a profanity filter. Both Android recognition intents simply omitted `RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS`, so the recognizer service's default masking behavior remained in force.

## v59 functional change

v59 requests uncensored Android speech transcripts in both BOOP speech-recognition entry paths by adding:

`intent.putExtra(RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS, false);`

This is materialized into:

- ordinary tap-to-talk recognition in `MainActivity`;
- post-wake command recognition in `BoopWakeRecognitionIntent`.

No wake detector, five-sample enrolment logic, rename parser, Home Assistant routing, TTS, command window, power/recovery behavior, diagnostics, visuals, Launcher, Shield behavior, package or signer is intentionally changed.

The Android recognizer service ultimately decides whether it honors this request, so physical profanity recognition remains a real-device acceptance item.

## TDD trail

RED:

- commit `b021b28d3f635f884dbc144252834b315712c94b`
- workflow `34256341474`
- materialized wake-handoff suite: 3 tests ran, 2 existing tests passed and exactly the new uncensored-speech contract failed because the masking override was absent.

GREEN materialization:

- patch script commit `09971b5a734c23134ed1b326aa9cab5036674855`
- materializer wiring commit `3b2ebeb66c357b947bec4c793165d3e97a8b994f`
- intermediate workflow `34256484198` reached the wake-handoff gate with all 3 tests passing before being superseded/cancelled by the v59 release bump.

Release bump:

- `0f2fe9d12e473b122938d06d767facc646c65476`
- version 59 / `1.2.13-unified-uncensored-speech`.

The first v59-labelled run `34256571189` compiled, tested and signed successfully but package verification then failed because the workflow still hard-coded v58 version metadata. This was a CI expectation defect, not an app-code failure. The verifier was corrected only for the expected v59 version fields at commit `136b56e6faac8ce450b957ac3057a379c68c7b7b`.

## Final signed candidate

- built commit `136b56e6faac8ce450b957ac3057a379c68c7b7b`
- version 59 / `1.2.13-unified-uncensored-speech`
- workflow `34257117357` SUCCESS
- artifact `BOOP-Unified`, ID `10068400476`
- artifact digest `sha256:f397a85aa4747a51394d0bbc42266658cc6609a355a25b210bd761d8813e023c`
- APK SHA-256 `7d48cc77407b69428bd2456b80cefbe56cb60f6f8326eb2e3686aa7b22bc7a2e`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- materialized wake-handoff contracts 3/3 PASS
- Shield focused tests 58/58, zero failures/errors/skips
- unified focused tests 92/92, zero failures/errors/skips
- Launcher lint, signed assembly, package/version, manifest, permanent signer, APK ZIP integrity and artifact upload PASS.

GitHub device/emulator/visual acceptance remains deliberately absent. Physical v59 profanity recognition is pending.

## Required Pixel test

Install v59 over v58 without uninstalling. Keep the established wake behavior otherwise unchanged. Use the current wake name to request a rename to an adult/profane word, confirm BOOP's five-sample prompt contains the actual word rather than asterisks, complete the five repetitions, then confirm the new name wakes BOOP and routes a normal Home Assistant command. Also confirm permanent `BOOP` still works naturally without a pause.

If the recognizer still returns asterisks, do not alter the wake architecture or rename parser. Capture the transcript/diagnostic path and treat the active Android recognition service as the next boundary to investigate.

## Rollback

v59 is CI/signer green only until Ryan physically accepts it. The current physically accepted rollback remains:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Never repoint that checkpoint.
