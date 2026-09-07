# BOOP Launcher Alpha 2 handoff — 2026-09-07

Owner: Ryan's Launcher work. Authoritative development branch: `boop-launcher-alpha2`.
Project: `launcher/`; package remains `com.boop.launcher`.

## Current state

Ryan rejected the Alpha 1 visual/interaction direction as old-fashioned and approved a clean-sheet replacement inspired by the current Pixel Launcher experience without copying Google proprietary code or assets.

The old Alpha 1 branch is preserved only as a historical escape hatch. Do not use its UI architecture as Alpha 2's foundation merely to save effort.

A written Alpha 2 design is committed at:

`docs/superpowers/specs/2026-09-07-boop-launcher-alpha2-design.md`

Design commit: `fc8b3b010f194640f0935b3cf57e125ba27a360e`.

No Alpha 2 application code has been changed yet. The written-spec review gate is the next required step before implementation planning/code.

## Locked Alpha 2 direction

- Pure black edge-to-edge home canvas.
- No permanent clock, At a Glance, Google search pill, dock/hotseat, plus button or decorative page furniture.
- Native installed-app icons; restrained Pixel-like spacing and motion.
- Swipe-up app drawer with Pixel-like finger tracking and transitions.
- Drawer stays minimal and black; search is on demand only, not a permanent field.
- HOME role, local app enumeration/search, app placement/move/remove, widgets and persistence are first-build fundamentals.
- BOOP-specific return-strip/voice flourishes are deferred until the basic launcher physically feels right.
- Existing permanent BOOP GitHub signing identity must remain unchanged.
- Package stays `com.boop.launcher`; target API 36.
- No Internet or microphone permission for launcher fundamentals.
- AOSP Launcher3 Android 16 is the preferred public technical reference/base under Apache 2.0. Lawnchair may be inspected but its GPL code is not to be imported without a later explicit decision.
- Pixel Launcher proprietary APK/source/resources are reference-only and must not be published into BOOP.

## Verification philosophy

Automated checks should protect correctness, not pretend to prove feel. CI should compile/sign, test placement/search/widget cancellation, exercise HOME/drawer/app launch/persistence, scan for AndroidRuntime crashes and guard drag-stream regressions.

Ryan's Pixel 10 Pro XL is the authority for physical acceptance of motion, spacing and whether the launcher actually feels modern. Screenshots/emulators do not count as proof of animation quality.

## Historical Alpha 1 evidence

Alpha 1.1 remains preserved on `boop-launcher-alpha1` at handoff commit `ca057eb8eb7f218cc3e67b19f3b98a5cc0b7e138`. Its last tested source was `41f250d4ca0c791df7a83dd1c083aba7840670b7`, with signed build/lint/unit/emulator verification in run 34062128943. This evidence is historical and must not be misrepresented as Alpha 2 verification.

## Cross-app boundaries

Wall remains `com.boop.alpha1`; Launcher remains `com.boop.launcher`; Shield remains `com.boop.shieldoverlay`. Keep the apps independent. Main owns shared cross-project contracts; this branch owns Launcher implementation state.

Before material Alpha 2 implementation/publishing, fetch/recheck live main and this branch, preserve concurrent work, use the existing release signing workflow, run appropriate checks, commit reviewed changes, push, and verify live GitHub HEAD.
