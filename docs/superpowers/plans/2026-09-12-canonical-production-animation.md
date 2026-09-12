# Canonical Production Animation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the finished Animation Lab engine the production animation engine used by Unified, including all 26 canonical clips and real media routing.

**Architecture:** Package `unified/animation/` as one reusable Android library consumed by the Unified app and Shield Home library. Add a pure-Java production controller over `EyeCatalogue`/`EyeMotion.Controller`, use it from the lab and production surfaces, and migrate Now Playing clip selection from its bespoke eye timeline to canonical media clips while keeping approved artwork bytes untouched.

**Tech Stack:** Java 17, Android library modules, Python materialization scripts, pytest, javac harness tests, Gradle 9.6, GitHub Actions signing.

**Spec:** `docs/superpowers/specs/2026-09-12-canonical-production-animation-design.md`

## Global Constraints

- Package remains `com.boop.alpha1`.
- Preserve exact approved eye master SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`.
- Preserve approved notification hands bytes and existing music artwork bytes.
- Do not add permissions, network calls, microphone behavior, HA behavior, or visual/golden tests.
- Startup Manager scope already on the base branch must remain intact.
- Physical appearance and animation acceptance belong to Ryan on the Shield.

---

### Task 1: Canonical production controller

**Files:**
- Create: `unified/animation/java/com/boop/eyes/ProductionAnimationController.java`
- Create: `source-test/ProductionAnimationControllerTest.java`
- Modify: `source/BoopCanonicalAnimationRouter.java`
- Modify: `source-test/CanonicalAnimationRouterTest.java`

**Interfaces:**
- Produces `ProductionAnimationController(String initialClipId, long nowMs, long seed)`, `select(String,long,float)`, `sample(long)`, `pause(long)`, `resume(long)`, `activeClipId()`, `steadyClipId()`, `supports(String)`, `clipIds()`.
- One-shot clips return to the previous steady clip after their exact catalogue duration; looping clips become the steady clip.

- [ ] Write failing Java harness assertions for all 26 IDs, one-shot return, pause/resume, and router mappings including media/reaction/gesture IDs.
- [ ] Run javac/java harness and confirm RED because the production controller and mappings do not yet exist.
- [ ] Implement the minimal controller and router additions using `EyeCatalogue` without changing catalogue keyframes.
- [ ] Re-run harnesses and confirm GREEN.
- [ ] Commit controller/router work.

### Task 2: One reusable animation library in the materialized APK

**Files:**
- Create: `unified/animation-lib.gradle`
- Modify: `unified/app-build.gradle`
- Modify: `unified/shield-home-lib.gradle`
- Modify: `scripts/patch-unified-canonical-animations.py`
- Modify: `tests/test_unified_canonical_animation_contract.py`

**Interfaces:**
- Produces Gradle module `:animation-lib` containing `com.boop.eyes.*` plus canonical assets.
- `:app` and `:shield-home-lib` depend on `:animation-lib`; the app no longer owns a duplicate private copy of the engine.

- [ ] Extend the materialization contract test to require `animation-lib`, both dependencies, locked asset copies, and production controller materialization.
- [ ] Materialize and confirm RED against the old app-private embedding.
- [ ] Implement the animation library materialization and dependency wiring.
- [ ] Re-materialize and run the contract tests GREEN, including exact asset hashes.
- [ ] Commit library/materialization work.

### Task 3: Make Animation Lab use the production controller

**Files:**
- Modify: `source/BoopCanonicalAnimationActivity.java`
- Modify: `tests/test_unified_canonical_animation_contract.py`

**Interfaces:**
- Developer preview selects and samples through `ProductionAnimationController`; it remains a manual preview surface only.

- [ ] Add a non-visual materialization assertion that the activity compiles against the production controller.
- [ ] Confirm RED before the activity is migrated.
- [ ] Replace its private `EyeMotion.Controller` ownership with `ProductionAnimationController` while preserving labels, slow review, freeze, sign/Freddie behavior, and lifecycle clock semantics.
- [ ] Re-run focused compile/materialization tests GREEN.
- [ ] Commit lab unification.

### Task 4: Route Shield Now Playing through canonical media clips

**Files:**
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/CanonicalMediaAnimationPolicy.java`
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/CanonicalMediaAnimationPolicyTest.java`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingPuppetView.java`

**Interfaces:**
- `CanonicalMediaAnimationPolicy.steadyClip(Mode)` maps GROOVE -> `music`, UPSET -> `media_pause`, REST -> `idle`.
- Track/session changes trigger `track_change` as a one-shot, returning to the current steady media clip.
- `ShieldNowPlayingPuppetView` samples `ProductionAnimationController`; old bespoke eye/blink timelines cannot override canonical clip selection.

- [ ] Write failing pure-Java media policy/controller tests for play, pause, track-change return, and hidden/rest behavior.
- [ ] Run them and confirm RED.
- [ ] Implement policy and migrate the puppet view to canonical `EyeMotion.Pose` sampling while retaining locked artwork/resource composition and focus behavior.
- [ ] Compile Shield Home and run focused tests GREEN.
- [ ] Commit media production wiring.

### Task 5: Full verification, signed build, and handoff

**Files:**
- Modify: `.github/workflows/build-boop-unified.yml` only if the new focused test files must be added to the existing canonical test step.
- Modify: `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md`.
- Modify: `unified/app-build.gradle` for the final collision-free version only after checking live branch/device versions.

- [ ] Run focused canonical/media tests, materialization, Unified unit/build/integrity gates, and verify no locked asset bytes changed.
- [ ] Fetch/reconcile live remote heads; choose a version higher than both current branch and installed Shield package.
- [ ] Push reviewed commits and dispatch the existing permanent-signing workflow for this branch.
- [ ] Verify workflow success, APK package/version/signer/hash, and preserve the currently installed Shield APK as rollback.
- [ ] Install only the newer signed APK, launch Unified, and leave visual/animation acceptance to Ryan.
- [ ] Record exact source/build/install receipts in handoff/status/memory, commit/push docs, and verify live remote HEAD equals local HEAD.
