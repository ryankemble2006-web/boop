# BOOP unified handoff

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v69 internal dev menu

v69 adds an internal BOOP developer/demo screen reachable from the existing Voice settings surface. It does not export a new external entry point.

The `Animations` shelf drives the existing BOOP face behaviors: Wake, Think, Berry, Shake and Sleep. The `Notification demos` shelf uses the real `BoopNotificationPuppetView` with local-only Unlocked, Locked and Bundle fixtures. Locked demo content goes through the production privacy-redaction model. Demo previews do not create Android shade notifications and do not call the notification listener/runtime path.

v69 carries the v68 procedural hue-wire fix and the finished v65 procedural-eye/sclera stack forward unchanged. v68 had not yet been physically accepted when v69 was built.

Built code head:

`709c74eb39d28c0d894661e5bde66da18f9ea6cf`

Release identity:

- versionCode `69`;
- versionName `1.2.23-unified-dev-menu`;
- main workflow `34317400589` SUCCESS;
- separate Shield HOME routing workflow `34317400631` SUCCESS;
- artifact `BOOP-Unified`, ID `10090644503`;
- artifact size `62,728,418` bytes;
- artifact ZIP digest `sha256:af14acccfa0ae730a1254f518f2210a46645d45fdcd6d2056f9aa4fb4b9449a9`;
- APK SHA-256 `31da93c3fdfd7116b8bc9b083fd947dadc5952a77c5c67c5d3808b99f0c57f88`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Detailed receipt: `docs/BOOP-V69-DEV-MENU-RECEIPT.md`.

## Test-first evidence

v69 was built through three explicit RED stages:

- `3bb0aadb8bc15df283be93202853f273a432d467` / workflow `34316174381`: failed because `BoopDevMenuModel` did not exist;
- `b32a328f09a7f5d4c347e48986b685da18afd072` / workflow `34316614087`: failed because `BoopDevNotificationPreview` did not exist;
- `f571345b1ac68980d7877dc028e597d884d74812` / workflow `34317008388`: failed because `.BoopDevMenuActivity` was not yet wired into the materialized manifest.

Final workflow `34317400589` passed:

- non-visual integration contracts;
- canonical materialization;
- internal-only dev-menu/local-only notification-demo plumbing contract;
- notification presenter/manifest contracts;
- seamless wake-command handoff;
- preserved Launcher checks;
- Shield focused functional tests 58/58, zero failures/errors/skips;
- Unified focused functional tests 140/140, zero failures/errors/skips;
- signed APK assembly;
- package/version/permanent-signer/APK ZIP integrity;
- artifact upload.

The downloaded artifact was independently extracted and re-hashed; the APK matched the CI receipt exactly.

## Immediate physical acceptance

**GitHub did not perform visual acceptance. Ryan owns the physical result.** v69 is CI/signer green only.

On the phone:

1. Open Voice settings and confirm `Dev menu` opens BOOP Dev.
2. Exercise Wake, Think, Berry, Shake and Sleep and judge the real motion.
3. Exercise Unlocked, Locked and Bundle notification demos.
4. Confirm Locked demo exposes no title/body text.
5. Confirm demos do not add a new Android shade notification.
6. Recheck the carried v68 hue fix across obvious colours: only the procedural iris should change; sclera, pupils, catchlights and black lids must remain unchanged.
7. Confirm the finished v65 sclera/white blend remains correct.

Do not create or repoint a v69 rollback checkpoint until Ryan explicitly accepts this exact signed APK.

## Durable eye ordering

The finished v65 procedural eye stack remains canonical:

1. `scripts/patch-unified-reading-eyes.py` installs procedural Canvas iris/pupil/catchlight rendering and the procedural hue setter;
2. `scripts/patch-v64-procedural-sclera.py` covers the complete moving-iris envelope;
3. `scripts/patch-v65-feathered-sclera.py` feathers cleanup into the approved grey sclera shading.

After those stages, no later patch may rerun the legacy bitmap hue-cache setter. In particular, `scripts/patch-unified-shield-dashboard.py` must never invoke `scripts/patch-unified-iris-cache.py` after procedural eyes are installed.

User hue remains iris-only. Default cyan/blue remains 190 degrees. Sclera, pupils, catchlights, black lids and the rest of the approved eye artwork remain outside hue control.

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
