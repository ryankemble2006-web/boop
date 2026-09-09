# BOOP unified handoff

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v63 BOOP notifications

Notification presenter work is now merged into canonical unified BOOP. The previous exact-hand binary blocker is resolved and must not be reintroduced.

Built code head:

`2b0484cb9c95b0836c2bb6e93f6d7c890bc0e2a4`

This is a two-parent merge preserving both lineages:

- canonical parent `df8f8973431cc802d6fd16fb61c5b0c072eeda58`, including the queued `What If?` screensaver notes;
- reviewed notification parent `45fc81c95f9fece434d7c6a0ca7ae9eb8cc4c183`.

Release identity:

- versionCode `63`;
- versionName `1.2.17-unified-notifications`;
- workflow `34308822296` SUCCESS;
- Shield HOME routing workflow `34308822310` SUCCESS;
- artifact `BOOP-Unified`, ID `10087693779`;
- artifact ZIP digest `sha256:5673289f3a11cceceec99cfdeac2506c17eae0fbdfd86560b3c407f49c9e96ea`;
- APK SHA-256 `e92963c4bff18b8b8fb2b88202aac3207186edb4af05113874d92e0e455e130f`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Fresh canonical CI evidence from the signed artifact:

- notification presenter contracts PASS;
- seamless wake-command handoff PASS;
- preserved Launcher source PASS;
- Shield focused functional tests 58/58, zero failures/errors/skips;
- unified focused functional tests 136/136, zero failures/errors/skips;
- signed APK assembly PASS;
- package/version/permanent-signer/APK ZIP integrity PASS;
- artifact upload PASS;
- separate Shield HOME routing contract PASS.

Detailed receipt: `docs/BOOP-V63-NOTIFICATIONS-RECEIPT.md`.

## Notification behavior now in canonical BOOP

- One reusable `BoopNotificationPuppetView` serves in-place Wall, unlocked overlay and locked presentation surfaces.
- Before authentication the locked surface exposes only app identity/icon/count, not notification message content.
- Tapping preserves the source Android notification `PendingIntent`. `FLAG_AUTO_CANCEL` mirror cancellation happens only after source send succeeds.
- Swipe or timeout dismisses only BOOP's mirror. Android's shade notification remains authoritative.
- Swipe threshold is >=72dp on a dominant horizontal or vertical axis with strict >1.25x directional dominance.
- Entrance recipe remains card alpha/translation from -16dp over 260ms with `OvershootInterpolator(0.7f)` and hands scale 0.96 -> 1 over 220ms.
- BOOP has a deterministic 320ms notification cue and `{0,35,55,28}` vibration waveform.
- BOOP sound/vibration is permitted only when the coordinator requests the cue and the native Android channel is known silent for both sound and vibration. Noisy or unknown channels remain visual-only to avoid double alerts.
- Manifest authority includes overlay/screen-on/vibrate while rejecting full-screen-intent, query-all, accessibility-service and device-admin authority.

Android's source notification remains authoritative. Do not make BOOP destructive to the shade notification merely because its puppet mirror is dismissed.

## Exact approved notification hands

The exact approved notification binary is now in GitHub and protected by the existing hash guard:

- size `1,809,990` bytes;
- SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`;
- Git blob SHA-1 `d47037271bf320f4f110e3f8416f59882062afac`.

Canonical runtime source:

`unified/assets/boop-notifications/boop-yellow-hands-approved.png`

Notification branch repair commit:

`45fc81c95f9fece434d7c6a0ca7ae9eb8cc4c183`

Animation root master repair:

`animation-freddie-mercury@783d38d0cbc18e3e93ed305ba36446da79f27ce3`

Do not regenerate, recompress, recolor, crop or weaken the hash contract for these hands.

## Physical acceptance still required

v63 is CI/signer green, not physically accepted. Ryan owns the Pixel visual, lock-screen and acoustic judgment.

Install v63 over the current BOOP without uninstalling. Required real-device checks:

1. Trigger a normal notification while unlocked and confirm BOOP presents it correctly.
2. Trigger while locked and confirm only privacy-safe app identity/icon/count is visible before authentication.
3. Tap the BOOP presentation and confirm the source notification action/app opens correctly.
4. Swipe BOOP away and confirm the Android shade notification remains.
5. Let a BOOP presentation time out and confirm the Android shade notification remains.
6. Exercise a natively noisy notification channel and confirm BOOP does not produce a duplicate alert.
7. Exercise a known-silent channel and judge BOOP's cue/vibration physically.
8. Recheck the v62 listening-eye fix carried into v63: one moving pupil/iris per eye, no stationary ghost, reading sweep intact, selected iris hue intact, natural BOOP/custom wake commands intact.

Do not create a v63 physical rollback checkpoint until Ryan explicitly accepts this exact signed build.

## Physically accepted rollback state

Latest physically accepted exact rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Older wake-arm rollback remains:

`checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`

Never repoint accepted checkpoints.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eyes, iris-only hue, blink timing/gates, headphones/puppetry, exact five-digit yellow hands, one 16 kHz microphone owner, local wake-name training, exact 100 ms wake bridge, uncensored speech request, physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
