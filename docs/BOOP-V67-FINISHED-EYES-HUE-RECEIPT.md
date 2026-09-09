# BOOP Unified v67 finished eyes + procedural hue receipt

Date: 2026-09-09

Canonical branch: `boop-unified`
Package: `com.boop.alpha1`
Permanent signer unchanged.

## Built candidate

- Built source head: `63bb80283af7424bc1012fe71444552d8b942a74`
- Production eye commit: `c35b57a44ec0e7fb8f06cb49a4f0ab10915dbf5d`
- versionCode: `67`
- versionName: `1.2.21-unified-finished-eyes-hue`
- canonical build workflow: `34312779359` SUCCESS
- separate Shield HOME routing workflow for the production code: `34312684627` SUCCESS
- artifact: `BOOP-Unified`, ID `10089043590`
- artifact ZIP digest: `sha256:c0a291a324b0e96a81c4726ad187efa5f65f0ba63dc41d428d7aaae1bc001000`
- APK SHA-256: `13c51f8a56e109a9dc57bc37cba3175ce5290b210f65b2692ce575d76194b55b`
- permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

## Eye repair included

v67 surgically brings the finished procedural-eye work from protected reference branch `checkpoint-boop-unified-v65-procedural-eyes` into the current canonical notification lineage. It does not merge the experimental branch wholesale.

Canonical materialization now applies, in order:

1. `scripts/patch-unified-reading-eyes.py` using the v65 procedural Canvas iris renderer;
2. `scripts/patch-v64-procedural-sclera.py` to widen/clean the neutral sclera socket across the reading-motion envelope;
3. `scripts/patch-v65-feathered-sclera.py` to feather that cleanup into the approved original sclera shading.

The colour control is repaired for the procedural renderer. `setEyeHueDegrees()` now updates `proceduralIrisHueDegrees`; procedural iris colours consume that hue directly. The colour control must affect the iris only. It must not tint the sclera, pupil, catchlights, black lids, or other approved eye artwork.

The approved eye PNG remains the source for the eye bodies/lids and remains byte-locked. The procedural renderer neutralises the baked-in iris in memory and draws one procedural iris/pupil/catchlight set per eye. No moving PNG eye patch is layered over a stationary iris.

## TDD / non-visual verification

RED was demonstrated before the production change on workflow `34312326378`: 2 failed / 10 passed, specifically because canonical materialization lacked the v64/v65 sclera passes and the canonical reading renderer lacked procedural hue plumbing.

A later CI failure on `34312684631` was traced to a representation bug in the new test regex: it inspected Python patch-source escape sequences as if they were materialized Java. Production code was not changed for that failure. The test was corrected in commit `63bb80283af7424bc1012fe71444552d8b942a74` to check the same semantic contract without depending on source-string newline representation.

Fresh green canonical evidence from workflow `34312779359`:

- non-visual integration contracts: 12/12 passed;
- canonical materialization completed and logged procedural Canvas irises, v64 sclera coverage, and v65 feathering;
- notification presenter/manifest contracts passed;
- seamless wake-command handoff passed;
- preserved Launcher lint/source checks passed;
- Shield focused functional tests: 58/58, zero failures/errors/skips;
- Unified focused functional tests: 136/136, zero failures/errors/skips;
- signed APK assembly passed;
- package/version/permanent-signer/APK ZIP integrity passed;
- artifact upload passed;
- separate Shield HOME routing workflow `34312684627` passed.

## Visual acceptance boundary

**No visual acceptance was performed by GitHub.** No screenshot, golden-image, appearance, layout, animation, or pixel-judging CI was added or used. Ryan explicitly owns the real-device visual judgment.

v67 is CI/signer green only. Physical Pixel/Shield appearance, notification presentation and acoustic behavior remain pending. Do not create or repoint a v67 physical rollback checkpoint until Ryan explicitly accepts this exact signed build.

## Preserved v63 notification contract

v67 preserves the canonical notification presenter introduced in v63, including privacy-safe locked presentation, source `PendingIntent` behavior, non-destructive swipe/timeout, duplicate-alert avoidance, and the exact notification hands binary:

- size `1,809,990` bytes;
- SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`;
- Git blob `d47037271bf320f4f110e3f8416f59882062afac`.

Android's source notification remains authoritative.
