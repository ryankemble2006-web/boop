# BOOP Launcher Alpha 2 handoff — 2026-09-07

Owner: Ryan's Launcher work. Authoritative development branch: `boop-launcher-alpha2`.
Project: `launcher/`; package remains `com.boop.launcher`.

## Current state

Ryan rejected the Alpha 1 visual/interaction direction as old-fashioned and approved a clean-sheet replacement inspired by current Pixel Launcher interaction patterns without copying Google proprietary code or assets.

Alpha 1 remains preserved on `boop-launcher-alpha1` only as a historical escape hatch. Do not reuse its monolithic UI architecture merely to save effort.

Approved design:
`docs/superpowers/specs/2026-09-07-boop-launcher-alpha2-design.md`

Implementation plan:
`docs/superpowers/plans/2026-09-07-boop-launcher-alpha2.md`

## First signed Alpha 2 physical-feel baseline

Current tested application source: `844301bfe264a52b202787e8224fb43452dd3ff6`.

GitHub Actions run `34077665229` completed successfully. Unit tests, Android lint, release assemble and the permanent BOOP signing step all passed. Signed artifact `BOOP-Launcher-Alpha2-signed`, artifact ID `10002643933`, contains `BOOP-Launcher-Alpha2.apk` plus package/signature/hash receipts.

APK SHA-256:
`89a5cd50f97dd87e513d86d79bfae71b5f962d86f760190c57527a92fd289b96`

Package receipt:
- package `com.boop.launcher`
- versionCode `3`
- versionName `0.2.0`
- minSdk `29`
- targetSdk / compileSdk `36`

Signature receipt:
- one signer
- APK Signature Scheme v2 verifies
- BOOP signer certificate SHA-256 `c0f4549b7d367f7823a76ef32468f5ef7695e3a3380b2145d5a94ff3b1aa9e61`

This is CI-green only. It has not yet been installed or physically accepted on Ryan's Pixel 10 Pro XL. Do not promote Alpha 2 over Alpha 1 in the shared main app map until physical acceptance.

## What changed from Alpha 1

Alpha 2 replaces the old single-Activity/editor-heavy interaction structure with smaller launcher units for state, edge-to-edge window setup, app enumeration, local search, workspace persistence, app placement/drag and widget-host plumbing.

The baseline deliberately removes Alpha 1 furniture and permissions that are not needed for the clean Home experience: no permanent clock, At a Glance, Google search pill, dock/hotseat, introduction/editor mode, BOOP return-strip overlay, foreground service, Internet permission or microphone permission.

Home is pure black and edge-to-edge. Swipe up on empty Home opens All Apps. Native installed icons/labels are shown. Tap launches; hold from All Apps pins to Home. Search appears only when intentionally opened. Placed apps persist, launch on tap, can be picked up after a hold, moved, or removed by releasing in the top band. Alpha 1 workspace preferences are intentionally discarded once on first Alpha 2 start rather than carrying its old schema forward.

Back behavior is explicit: Search -> All Apps -> Home. Android 13+ uses predictive-back registration; older Android keeps the legacy fallback.

## Known incomplete areas

Do not overstate this baseline:

1. Drawer motion is not yet Launcher3-quality direct-finger spring physics. The current threshold/settle transition exists so Ryan can install and judge the overall direction; Pixel 10 Pro XL feel is the authority for the next motion pass.
2. Widget host selection/configuration plumbing exists and cleans cancelled IDs, but widget views are not yet rendered/movable/resizable on the Alpha 2 workspace. Widget support is therefore incomplete.
3. Dynamic multi-page workspace behavior from the approved full Alpha 2 design is not complete in this baseline.
4. No physical checks yet for icon scale, drawer motion, search discoverability, app move/remove, gesture-navigation visual blending, rotation/process death or real widget flows.

## Next physical step

Install `BOOP-Launcher-Alpha2.apk` over the existing `com.boop.launcher` package. Because the GitHub workflow uses the existing permanent BOOP signing identity, this should be an update rather than a side-load identity change.

On Pixel 10 Pro XL, first judge only the fundamentals: empty pure-black Home, absence of permanent furniture, swipe-up to All Apps, native icon scale/spacing, contextual search, hold-to-pin, app move/remove, Home/Back and the bottom gesture area blending into black. Record anything that feels old-fashioned or awkward as a bug/feel list rather than tuning from emulator screenshots.

After that physical baseline, prioritize true Launcher3-grade drawer/finger physics and fix any blocking interaction issues before finishing widgets/pages or reintroducing BOOP-specific flourishes.

## Cross-app boundaries

Wall remains `com.boop.alpha1`; Launcher remains `com.boop.launcher`; Shield remains `com.boop.shieldoverlay`. Keep the apps independent. Main owns shared cross-project contracts; this branch owns Launcher implementation state.

Before further edits, fetch/recheck live main and `boop-launcher-alpha2`, preserve concurrent work, use the existing release signing workflow, run appropriate checks, commit reviewed changes, push, and verify live GitHub HEAD. CI-green, signed, physically installed and physically accepted are separate states.
