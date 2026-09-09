# BOOP unified handoff

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v68 procedural hue wire fix

Ryan physically tested signed v67 and reported that the eye hue control did not work. v67 is not physically accepted.

Root cause was confirmed in canonical materialization: after the v65 procedural-eye stack installed the new `setEyeHueDegrees()` implementation, `scripts/patch-unified-shield-dashboard.py` reran the old bitmap-era iris-cache patch. Because the rendered iris is procedural, that late overwrite changed a bitmap no longer used for the visible iris.

v68 removes only that stale late invocation. The legacy iris-cache baseline still runs early in `scripts/materialize-android.sh`, before the procedural renderer replaces it. No eye geometry, sclera/white feathering, artwork, blink, notification, wake, Home, room, package or signer behavior was intentionally changed.

Built code head:

`91e562754be31745a5ee76538ebef50e6c6a9b2d`

Release identity:

- versionCode `68`;
- versionName `1.2.22-unified-hue-wire-fix`;
- workflow `34314023763` SUCCESS;
- artifact `BOOP-Unified`, ID `10089480166`;
- artifact ZIP digest `sha256:f1b546f52800260a878940db0b8f074b1a8a07fec9df4370b2843acfc59c1699`;
- APK SHA-256 `571f0a501e5a3df921fc1e521ae235810860df3f493c223af491f974cd30e352`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Detailed receipt: `docs/BOOP-V68-HUE-WIRE-FIX-RECEIPT.md`.

## Test-first evidence

Regression commit `8e4527e99f835acc4bf4000d2fa9a9c53a32e9d7` produced workflow `34313965830`, which failed at the non-visual integration-contract step exactly because the late Shield dashboard patch still referenced the legacy iris-cache patch.

Fresh v68 workflow `34314023763` then passed:

- non-visual integration contracts;
- canonical materialization;
- notification presenter/manifest contracts;
- seamless wake-command handoff;
- preserved Launcher checks;
- Shield focused functional tests 58/58, zero failures/errors/skips;
- Unified focused functional tests 136/136, zero failures/errors/skips;
- signed APK assembly;
- package/version/permanent-signer/APK ZIP integrity;
- artifact upload.

The downloaded artifact was independently re-hashed after extraction and matched the CI APK receipt exactly.

## Eye state and durable ordering rule

The finished v65 procedural eye stack remains canonical:

1. `scripts/patch-unified-reading-eyes.py` installs procedural Canvas iris/pupil/catchlight rendering and the procedural hue setter;
2. `scripts/patch-v64-procedural-sclera.py` covers the complete moving-iris envelope;
3. `scripts/patch-v65-feathered-sclera.py` feathers cleanup back into the approved grey sclera shading.

After those stages, no later patch may rerun the legacy bitmap hue-cache setter. In particular, the Shield dashboard materialization pass must never invoke `scripts/patch-unified-iris-cache.py` after procedural eyes are installed.

User hue remains iris-only. Default cyan/blue remains 190 degrees. Sclera, pupil, catchlights, black lids and the rest of the approved eye artwork remain outside hue control. Do not restore shifted PNG iris patches or whole-bitmap tinting for the active procedural renderer.

## Visual/device acceptance

**GitHub did not perform visual acceptance.** Ryan owns the physical result.

v68 is CI/signer green, not physically accepted. The immediate real-device check is simple: open the existing hue control and move the slider across obvious colours. The procedural iris should update live while whites, pupils, highlights and black lids remain unchanged. Also confirm the finished v65 sclera/whites still look correct.

Do not create or repoint a v68 rollback checkpoint until Ryan explicitly accepts this exact signed APK.

## Preserved notification contract

Android's original notification remains authoritative. BOOP is a puppet mirror around it. Locked state may expose app identity/icon/count only before authentication. Tap preserves the source `PendingIntent`; swipe/timeout dismiss BOOP's mirror only; duplicate-alert prevention remains in force.

Exact approved notification hands remain locked:

- size `1,809,990` bytes;
- SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`;
- Git blob `d47037271bf320f4f110e3f8416f59882062afac`;
- canonical path `unified/assets/boop-notifications/boop-yellow-hands-approved.png`.

Do not regenerate, recompress, recolor, crop or weaken the hash contract.

## Physically accepted rollback state

Latest physically accepted exact rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains:

`checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`

Older wake-arm rollback remains:

`checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`

`checkpoint-boop-unified-v65-procedural-eyes` remains protected/reference eye provenance. Never repoint protected/accepted checkpoints.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eye master, procedural iris-only hue, feathered sclera cleanup, blink timing/gates, headphones/puppetry, exact five-digit yellow hands, one 16 kHz microphone owner, local wake-name training, exact 100 ms wake bridge, uncensored speech request, physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
