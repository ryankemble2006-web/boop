# BOOP unified status

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v69 internal dev menu

v69 adds one internal developer/demo surface reachable from Voice settings.

Animations shelf:
- Wake
- Think
- Berry
- Shake
- Sleep

Notification demos shelf:
- Unlocked
- Locked
- Bundle

The animation controls invoke the existing BOOP face behaviors. Notification demos use the production `BoopNotificationPuppetView` with local-only fixtures. Locked demo content is privacy-redacted by the production presentation model. The dev activity is `exported=false` and does not call Android `NotificationManager`, the notification listener service, or `BoopNotificationRuntime`.

v69 carries the v68 procedural hue-wire fix and the finished v65 procedural-eye/sclera stack forward unchanged. v68 was not physically accepted before v69 was built.

Built source:

`709c74eb39d28c0d894661e5bde66da18f9ea6cf`

Release evidence:

- version 69 / `1.2.23-unified-dev-menu`;
- main workflow `34317400589` SUCCESS;
- separate Shield HOME routing workflow `34317400631` SUCCESS;
- artifact `BOOP-Unified`, ID `10090644503`;
- artifact digest `sha256:af14acccfa0ae730a1254f518f2210a46645d45fdcd6d2056f9aa4fb4b9449a9`;
- APK SHA-256 `31da93c3fdfd7116b8bc9b083fd947dadc5952a77c5c67c5d3808b99f0c57f88`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- Shield focused tests 58/58, zero failures/errors/skips;
- Unified focused tests 140/140, zero failures/errors/skips;
- dev-menu/local-only preview plumbing, notification presenter/manifest, wake handoff, Launcher preservation, signed assembly, package/version/signer/integrity and artifact upload all PASS.

The downloaded artifact was independently extracted and the APK re-hashed; it matched the CI receipt exactly.

Detailed receipt: `docs/BOOP-V69-DEV-MENU-RECEIPT.md`.

## Acceptance boundary

**No visual acceptance was performed by GitHub.** v69 is CI/signer green only.

Physical checks:

1. Voice settings -> `Dev menu` opens BOOP Dev.
2. Wake/Think/Berry/Shake/Sleep run correctly and look right on-device.
3. Unlocked/Locked/Bundle demos use the intended BOOP notification puppet presentation.
4. Locked demo exposes no title/body text.
5. Demo previews do not create Android shade notifications.
6. Recheck carried v68 hue: slider changes the procedural iris live while sclera, pupils, catchlights and black lids stay unchanged.
7. Finished v65 sclera/white blend remains correct.

Do not create or repoint a v69 rollback checkpoint until Ryan explicitly accepts this exact signed APK.

## Durable eye ordering

The canonical procedural eye stages remain:

1. `patch-unified-reading-eyes.py`;
2. `patch-v64-procedural-sclera.py`;
3. `patch-v65-feathered-sclera.py`.

No later materialization stage may rerun the legacy bitmap hue setter after those stages. The Shield dashboard pass must remain unrelated to Wall iris tinting.

## Preserved contracts

Android's original notification remains authoritative; BOOP mirrors it. Locked notification presentation stays privacy-safe. Exact notification hands remain locked. Preserve procedural iris-only hue, finished sclera feathering, approved black-lidded eye master, blink, notifications, headphones/puppetry, one microphone owner, wake/name architecture, exact 100 ms bridge, uncensored speech request, HA names/Home controls, room isolation and Shield scaling.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / `com.boop.shieldhome` until Ryan explicitly approves a later merge. No automatic installs/grants or signer/package changes.

## Physically accepted rollback

Latest exact physically accepted rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`.

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected eye provenance. Never repoint protected/accepted checkpoints.
