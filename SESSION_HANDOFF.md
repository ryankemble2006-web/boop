# BOOP unified handoff

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v67 finished eyes + repaired colour control

Ryan identified that canonical v63 notifications had landed before the later finished default-eye work. He confirmed the later v65 eye pass was the one where the sclera/whites were finally correct and asked to bring that finished eye implementation into canonical Unified, repair the eye-colour changer for it, and jump directly to v67.

Built code head:

`63bb80283af7424bc1012fe71444552d8b942a74`

Production eye transplant commit:

`c35b57a44ec0e7fb8f06cb49a4f0ab10915dbf5d`

Release identity:

- versionCode `67`;
- versionName `1.2.21-unified-finished-eyes-hue`;
- workflow `34312779359` SUCCESS;
- separate Shield HOME routing workflow `34312684627` SUCCESS on the production code commit;
- artifact `BOOP-Unified`, ID `10089043590`;
- artifact ZIP digest `sha256:c0a291a324b0e96a81c4726ad187efa5f65f0ba63dc41d428d7aaae1bc001000`;
- APK SHA-256 `13c51f8a56e109a9dc57bc37cba3175ce5290b210f65b2692ce575d76194b55b`;
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Detailed receipt: `docs/BOOP-V67-FINISHED-EYES-HUE-RECEIPT.md`.

## What changed in v67

The protected/reference v65 eye lineage was not wholesale-merged. Only the finished eye renderer stack was transplanted into current canonical Unified:

- `scripts/patch-unified-reading-eyes.py` now uses the v65 procedural Canvas iris renderer;
- `scripts/patch-v64-procedural-sclera.py` is now part of normal canonical materialization and covers the full moving-iris envelope without the old dark socket/remnant crescent;
- `scripts/patch-v65-feathered-sclera.py` is now part of normal canonical materialization and fades the neutral cleanup into the approved grey sclera shading rather than leaving an opaque pale oval;
- the existing colour-control UI/voice trigger remains, but `setEyeHueDegrees()` now drives `proceduralIrisHueDegrees` directly;
- procedural iris colours consume the selected hue, so colour changes target the iris rather than recolouring the eye bitmap;
- sclera, pupil, catchlights, black lids and the rest of the approved eye artwork remain outside the user hue control;
- the approved PNG remains the base source for eye bodies/lids and no moving PNG iris patch is stacked over a stationary iris.

v67 preserves the v63 notification presenter, wake/name architecture, blink, headphones/puppetry, Home controls, room isolation, exact notification hands and standalone Shield HOME boundary.

## Fresh non-visual CI evidence

The new eye/hue contract was developed test-first. RED workflow `34312326378` failed 2 tests / passed 10 before production changes because canonical materialization lacked the v64/v65 sclera stages and procedural hue wiring.

A first production run `34312684631` later failed only because the new semantic test used a regex that treated Python patch-source `\\n` escapes as already-materialized Java. Production code was unchanged; the test representation was corrected in the final built head.

Fresh canonical workflow `34312779359` then passed:

- non-visual integration contracts 12/12;
- canonical materialization including procedural irises, v64 sclera cleanup and v65 feathering;
- notification presenter/manifest contracts;
- seamless wake-command handoff;
- preserved Launcher lint/source checks;
- Shield focused functional tests 58/58, zero failures/errors/skips;
- Unified focused functional tests 136/136, zero failures/errors/skips;
- signed APK assembly;
- package/version/permanent-signer/APK ZIP integrity;
- artifact upload.

Separate Shield HOME routing workflow `34312684627` also passed on `c35b57a44ec0e7fb8f06cb49a4f0ab10915dbf5d`.

## Visual acceptance remains manual

**GitHub did not perform visual acceptance.** No screenshot, golden-image, appearance/layout/animation comparison or pixel-judging CI was added or run. Ryan explicitly owns the real-device visual decision.

v67 is CI/signer green, not physically accepted as an integrated build. Do not create or repoint a v67 rollback checkpoint until Ryan explicitly accepts this exact signed APK on real hardware.

Real-device acceptance should confirm:

1. default eyes match the finished v65 look, especially the sclera/white blend;
2. the eye colour control changes only the iris and follows the selected hue live;
3. default cyan/blue remains correct;
4. reading/listening motion has one procedural iris/pupil per eye with no stationary ghost or socket crescent;
5. blink remains unchanged;
6. unlocked and privacy-safe locked notification presentation still work;
7. notification tap/swipe/timeout and cue policy still behave as intended;
8. natural BOOP/custom wake and existing Home/room controls remain intact.

## Preserved notification contract

Android's original notification remains authoritative. BOOP is a puppet mirror around it. Locked state may expose app identity/icon/count only before authentication. Tap preserves the source `PendingIntent`; swipe/timeout dismiss BOOP's mirror only; duplicate alert prevention remains in force.

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

The existing `checkpoint-boop-unified-v65-procedural-eyes` remains a protected/reference lineage for the finished eye work. Never repoint accepted/protected checkpoints.

## Architecture boundary and protected state

The clean Nvidia Shield HOME replacement remains standalone on `boop-shield-clean-launcher`, package `com.boop.shieldhome`; do not merge it into AIO until Ryan explicitly approves later.

Preserve approved black-lidded eye master, procedural iris-only hue, feathered sclera cleanup, blink timing/gates, headphones/puppetry, exact five-digit yellow hands, one 16 kHz microphone owner, local wake-name training, exact 100 ms wake bridge, uncensored speech request, physically accepted HA names/Home controls, room isolation and idempotent Shield scaling. GitHub performs functional/non-visual verification only; Ryan owns visual/device/acoustic acceptance. No automatic installs/grants.
