# BOOP unified status

Updated 2026-09-09. Canonical AIO branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current signed candidate: v70 in-place developer-menu hotfix

Release identity remains unchanged:

- versionCode `70`;
- versionName `1.2.24-unified-dev-menu-doods`.

### Current repair

Ryan physically confirmed that the earlier activity-hop hotfix at `95224d95a04b6a79a8d8c4e9e5ac4c35cf64ca9a` still forced BOOP to close when he said `dev menu`. That candidate is therefore physically failed for this bug despite green CI. Its Voice Settings vertical-scroll repair remains retained.

The current route removes the developer-menu activity hop from recognized speech and from the Voice Settings button. The exact local spoken command is now `developer menu`; the former `dev menu` phrase intentionally does not match. The command remains ahead of Home Assistant / command-router / chat fallback and now calls an in-place `showDeveloperMenu()` overlay inside the existing `MainActivity` rather than `startActivity()` / `BoopDevMenuActivity.class`.

Voice Settings remains vertically scrollable, with the row labelled `Developer menu` and `Done` still reachable.

Exact app/test head: `c17e98d9a09471cf8f53f2bee171a77e3b3b1203`.

Exact build workflow `34385817960` completed SUCCESS. Materialization, developer-menu contracts, exact phrase/rejection JUnit tests, seamless wake handoff, Launcher lint, Shield functional tests, wake/routing/lifecycle tests, signed APK assembly, package/version/permanent-signer/archive verification and artifact upload all passed.

- Artifact: `BOOP-Unified`
- Artifact ID: `10117795236`
- Artifact size: `62,741,119` bytes
- Artifact ZIP SHA-256: `5f11f925c097b67f1650fcb445918148e633071739b23245c54742d984c20faf`
- APK SHA-256: `c45962533574b9a0ef5bd08ad3f785c94668e9ec793ed6377967d7cae5195a20`
- Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- Shield focused functional tests: 58/58, zero failures/errors/skips
- Unified focused functional tests: 144/144, zero failures/errors/skips

The exact artifact ZIP was independently downloaded and matched GitHub's digest; the extracted APK and signer receipts also matched CI.

Detailed receipt: `docs/BOOP-V70-DEVELOPER-MENU-IN-PLACE-HOTFIX-RECEIPT.md`.

## Acceptance boundary

**This build is CI/signer green only. Physical acceptance is pending and belongs to Ryan.** GitHub performed no visual acceptance.

Current physical checks:

1. Install the APK from workflow `34385817960` and say `developer menu`; confirm BOOP stays open and BOOP Dev appears.
2. Confirm saying `dev menu` no longer invokes BOOP Dev.
3. Open Voice Settings, scroll to `Developer menu`, open it and use `Done`.
4. Exit/re-enter the developer menu and confirm wake/microphone behavior remains healthy.
5. Exercise the existing animation controls and notification dood previews as needed for v70 visual acceptance.

No v70 rollback checkpoint was created or repointed. Latest physically accepted rollback remains v59.

## Original v70 lineage

Original v70 production/build commit: `825593a16c004d9c0825720eb014c4f5cc8e58af`; original app implementation `2f4a62150121b299a433674e971de1e058f6330f`; materialized-router assertion correction `9c907d3497067eb88ea1308875084b8965413951`.

The original non-exported `BoopDevMenuActivity` may remain in the package for provenance/compatibility, but the current spoken/settings path must stay in `MainActivity` unless later physical evidence supports changing it.

## Durable eye ordering

Canonical procedural-eye stages remain:

1. `patch-unified-reading-eyes.py`;
2. `patch-v64-procedural-sclera.py`;
3. `patch-v65-feathered-sclera.py`.

No later materialization stage may rerun the legacy bitmap hue setter. User hue remains procedural-iris-only. Sclera, pupils, catchlights, black lids and approved eye artwork remain outside hue control.

## Locked notification hands

Canonical asset `unified/assets/boop-notifications/boop-yellow-hands-approved.png` remains exactly size `1,809,990` bytes, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`, Git blob `d47037271bf320f4f110e3f8416f59882062afac`. Do not regenerate, recompress, recolour/recolor, crop or weaken this guard.

## Preserved contracts

Android's original notification remains authoritative; BOOP mirrors it. Production locked presentation remains identity/icon/count-only before authentication. Preserve approved black-lidded eye master, procedural iris-only hue, feathered sclera, blink, notification privacy/tap/dismiss semantics, headphones/puppetry, one microphone owner, wake/name architecture, exact 100 ms wake bridge, uncensored speech request, HA names/Home controls, room isolation and idempotent Shield scaling.

The clean Shield HOME remains standalone on `boop-shield-clean-launcher` / package `com.boop.shieldhome` until Ryan explicitly approves a later merge. No automatic installs/grants or signer/package changes.

## Physically accepted rollback

Latest exact physically accepted rollback remains v59:

`checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`

v58 natural-wake rollback remains `checkpoint-boop-unified-v58-natural-boop-wake` -> `2d8fa4762298e6f0704dd502a6b04d1cb8e7e082`.

Older wake-arm rollback remains `checkpoint-boop-unified-v48-wake-arm` -> `64745e5ea6b5d89d08cb3b90a17ff28130685ad9`.

`checkpoint-boop-unified-v65-procedural-eyes` remains protected eye provenance. Never repoint protected/accepted checkpoints.
