# Shield Media Motion Preview Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Give approved H1 and P1 deterministic, restrained motion in a reviewable local preview.

**Architecture:** One pure Java time sampler defines the motion independently of Android. A tiny Java exporter emits its sampled values for the browser preview, avoiding a second implementation of the timing. The preview composites cleaned image layers without touching the installed Shield app.

**Tech Stack:** Java 17, JUnit 4, browser canvas, Pillow for user-authorized background cleanup.

**Spec:** `docs/superpowers/specs/2026-09-06-shield-media-motion-preview.md`

## Global Constraints

- This increment is a motion preview, not an APK replacement or automatic media detection rollout.
- No microphone, network, Home Assistant, authentication, manifest, Settings, overlay runtime or installed-app changes.
- H1 and P1 only; original sources remain intact; true transparent cleaned siblings.
- Pixel motion uses a logical 1536 x 1024 canvas.

### Task 1: Portable deterministic motion and export

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/MediaPuppetMotion.java`
- Test: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/MediaPuppetMotionTest.java`
- Create: `tools/media-motion-preview/ExportMotion.java`
- Create: `tools/media-motion-preview/test-motion.ps1`

**Interfaces:**
- `public static Pose music(long elapsedMs)`; `public static Pose cinema(long elapsedMs)`.
- `Pose` immutable public final float fields `x`, `y`, `rotationDegrees`, `kernelAlpha`; all pixel offsets apply to the hand for cinema, whole character for music.
- `public static final long MUSIC_PERIOD_MS = 3600L`, `CINEMA_PERIOD_MS = 12000L`.
- Exporter prints one JSON object to stdout with `stepMs: 40`, `musicPeriodMs`, `cinemaPeriodMs`, `music` and `cinema` arrays of `[x,y,rotationDegrees,kernelAlpha]`, sampled from zero inclusive to respective period exclusive. Compile with the actual motion class.

- [x] Write JUnit tests FIRST and record the failing compile/test command before production code. Assert music amplitudes (x <= 6, y <= 9, tilt <= 1.8 degrees), periodic identity, finite outputs with negative and Long.MAX_VALUE input; cinema chronological sequence (rest -> lift -> kernel fade -> empty return -> pickup) and stable wrap.

```java
assertEquals(MediaPuppetMotion.music(0).y,
    MediaPuppetMotion.music(3600).y, 0.0001f);
assertEquals(0f, MediaPuppetMotion.cinema(7400).kernelAlpha, 0f);
assertEquals(1f, MediaPuppetMotion.cinema(0).kernelAlpha, 0f);
```

- [x] Implement with no Android imports. Normalize time using `Math.floorMod(elapsedMs, period)`; music sine-derived offsets, cinema smoothstep interpolation between these keyframes:

```
ms       x    y    rotationDegrees   kernelAlpha
0        0    0       0                 1
4400     0    0       0                 1
5900    105  -185      9                 1
6500    105  -185      9                 1
6700    105  -185      9                 0
7200    105  -185      9                 0
8900      0     0      0                 0
9300      0     0      0                 0
9450      0     0      0                 1
12000     0     0      0                 1
```

- [x] Run tests and exporter using Java 17 plus cached JUnit 4.13.2 and Hamcrest 1.3. Test script parameters must accept paths to javac, java, junit, hamcrest and output directory, no absolute machine paths in tracked files. Script must stop on every nonzero native exit. Default output must be a task-owned build folder. Include exporter JSON checks (90 music rows, 300 cinema rows, finite numbers, samples identical to sampler).
- [x] Self-review, write report including red/green evidence and limitations. Do not commit until reviewer has inspected; root will own final commit decision. Never edit any other files or start subagents.

### Task 2: Clean art, compose preview, verify and record

**Files:**
- Create assets and standalone preview under `tools/media-motion-preview/`.
- Create task-owned inline preview in the supplied visualization directory.
- Update `BOOP_MEMORY.txt` with approved concepts, motion preview status and next physical verification boundary.

**Interfaces:** consumes exporter JSON arrays from Task 1 at 40ms intervals. Browser interpolates adjacent samples with wrap; no independent timing formula.

- [x] Preserve input renders. Use user-authorized local cleanup to produce RGBA sprites; inspect on light and dark surfaces. Keep `music.png`, `cinema-base.png`, `hand.png`, `kernel.png`, plus their provenance/cleanup method.
- [x] Build preview with canvas compositing of the original layers, applying the exported motion values. Place kernel at the pinching fingers; use same transform as hand. Allow pause; page-hidden means no animation work; respect reduced motion. Display a TV-corner scale comparison explicitly labelled as a scale study, not live Shield playback.
- [x] Verify loaded images, alpha extrema/background pixels, current displayed states at rest/lift/nibble/return, narrow layout, and playback controls. Re-run Task 1 tests.
- [x] Record exact output paths, tests, what remains before Android integration, and protected branch status. No push, APK, runtime rewrite or physical-green claim.
