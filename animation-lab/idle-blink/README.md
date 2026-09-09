# BOOP idle blink cleanup lab

This folder is a self-contained Animation Lab package for BOOP's approved idle blink. It is deliberately separate from production app code so it can be reviewed here and later transplanted manually into BOOP Air.

## User-approved target

Physical Shield video supplied by Ryan on 2026-09-09 shows two flat black shapes above BOOP's real textured eyelids while the headphones puppet is visible. Ryan identified those shapes as Photoshop leftovers and asked for them to be deleted. They are not part of BOOP's eyelids or blink design.

The finished idle blink must preserve the real BOOP artwork and use an upper-lid-only motion: the lid travels down from above, fully covers the eye, holds very briefly, then retracts. Do not squash, redraw, regenerate, stylize or reinterpret the eyes. Keep headphones and other artwork independent of the blink. Natural idle spacing and an occasional double blink belong to the reusable animation package.

## Exact source provenance

`assets/boop-headphones-source.png` is copied by Git blob identity from `boop-unified:shield-overlay/app/src/main/res/drawable-nodpi/boop_headphones.png` at the live source state inspected for this task. Source Git blob: `b2112ec156668cc165747d8778a8e564e268b184`.

The source is kept untouched. Cleanup must be deterministic and restricted to the confirmed Photoshop leftovers. If flattened pixels overlap genuine eyelid artwork, do not invent replacement pixels. Use approved source layers/assets or stop for manual art repair.

## Current stage

`tools/inspect_artifacts.py` performs a text-only inspection of the exact source PNG. Its first purpose is to locate the black leftovers precisely before any pixel edit is allowed. The generated `analysis/artifact-map.txt` is evidence, not visual acceptance.

Production Shield, Wall, Air, permissions, signing, packages and deployment are outside this branch. No Windows checkout synchronization is claimed.
