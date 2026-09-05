# BOOP Shield Overlay POC Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a disposable Nvidia Shield APK that shows BOOP's approved eyes as a small persistent transparent non-interactive overlay across launcher, Kodi, video playback, app switching, and HDR/DV transitions.

**Architecture:** Create a standalone Gradle Android project under `shield-overlay/` with package `com.boop.shieldoverlay`. The app has one permission/bootstrap activity, one `specialUse` foreground service, and one transparent custom overlay view; it copies the approved eye bitmap from the existing Alpha 1 project ZIP at build time and does not depend on Alpha 1 runtime code.

**Tech Stack:** Android SDK 36, Java 17, Gradle/Android Gradle Plugin, `WindowManager.TYPE_APPLICATION_OVERLAY`, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-05-boop-shield-overlay-poc-design.md`

## Global Constraints

- Package ID: `com.boop.shieldoverlay`.
- `compileSdk 36`, `targetSdk 36`, `minSdk 26`.
- No HA, microphone, speech, settings UI, touch/click handling, remote/key handling, black background, network access, accounts, storage, telemetry, boot receiver, or accessibility service.
- Overlay flags must include `FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE` and type `TYPE_APPLICATION_OVERLAY`.
- Overlay must be genuinely transparent and window-sized only around the eye puppet.
- First visual placement: upper-right, ~14% of display width, ~3% top/right inset.
- One wake/open animation only; no wandering, periodic reactions, member berries, or autonomous behavior.
- Existing Alpha 1 source/workflow remains unchanged.

---

### Task 1: Standalone Android project and deterministic geometry helper

**Files:**
- Create: `shield-overlay/settings.gradle`
- Create: `shield-overlay/build.gradle`
- Create: `shield-overlay/gradle.properties`
- Create: `shield-overlay/app/build.gradle`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/OverlayGeometryTest.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/OverlayGeometry.java`

**Interfaces:**
- Produces: `OverlayGeometry.calculate(int displayWidth, int displayHeight)` returning immutable geometry with `width`, `height`, `x`, `y`.
- Eye-pair aspect ratio is fixed from the approved source crops: overall pair width 764 units, height 393 units.

- [ ] **Step 1: Write the failing geometry test**

```java
package com.boop.shieldoverlay;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public final class OverlayGeometryTest {
    @Test public void usesFourteenPercentWidthAndThreePercentInsets() {
        OverlayGeometry.Geometry g = OverlayGeometry.calculate(1920, 1080);
        assertEquals(269, g.width());
        assertEquals(138, g.height());
        assertEquals(58, g.x());
        assertEquals(32, g.y());
    }
}
```

- [ ] **Step 2: Run test and verify it fails**

Run: `gradle -p shield-overlay :app:testDebugUnitTest --tests com.boop.shieldoverlay.OverlayGeometryTest`
Expected: compilation failure because `OverlayGeometry` does not exist.

- [ ] **Step 3: Add minimal project/build files and geometry implementation**

`OverlayGeometry.calculate` uses `Math.round(displayWidth * 0.14f)` for width, preserves `764f / 393f` aspect ratio, and returns `Math.round(displayWidth * 0.03f)` / `Math.round(displayHeight * 0.03f)` for right/top inset values.

- [ ] **Step 4: Run test and verify PASS**

Run: `gradle -p shield-overlay :app:testDebugUnitTest --tests com.boop.shieldoverlay.OverlayGeometryTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add shield-overlay
git commit -m "test: define Shield overlay geometry"
```

### Task 2: Transparent eye renderer using approved crop geometry

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopOverlayView.java`
- Create during CI/build: `shield-overlay/app/src/main/res/drawable-nodpi/boop_eyes.png`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/BoopEyeSourceTest.java`

**Interfaces:**
- `BoopOverlayView(Context)` draws only the left crop `Rect(90,600,419,993)` and right crop `Rect(525,600,854,993)` from `R.drawable.boop_eyes`.
- Public/package method `void wakeOnce()` performs the approved-style vertical opening animation once.

- [ ] **Step 1: Write crop regression test** checking constants match checkpoint `776e75c` and there is no black-background constant/path.
- [ ] **Step 2: Run unit test and verify failure** because `BoopOverlayView` does not exist.
- [ ] **Step 3: Implement renderer** with anti-aliased/filter bitmap paint, `setBackgroundColor(Color.TRANSPARENT)`, no `canvas.drawColor(Color.BLACK)`, two eye destinations centered inside the compact window, and `wakeOnce()` starting at `scaleY=0.08f` and animating to `1f` over 380 ms with `OvershootInterpolator(0.45f)`.
- [ ] **Step 4: Run unit/source tests and verify PASS**.
- [ ] **Step 5: Commit** with `feat: render transparent BOOP Shield eyes`.

### Task 3: Foreground overlay service and window flags

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/OverlayWindowSpec.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopOverlayService.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/OverlayWindowSpecTest.java`

**Interfaces:**
- `OverlayWindowSpec.flags()` returns `FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE`.
- `OverlayWindowSpec.type()` returns `TYPE_APPLICATION_OVERLAY`.
- Service owns at most one `BoopOverlayView`, uses `START_STICKY`, and removes the view in `onDestroy()`.

- [ ] **Step 1: Write failing `OverlayWindowSpecTest`** asserting exact type and both flags.
- [ ] **Step 2: Run test and verify failure**.
- [ ] **Step 3: Implement `OverlayWindowSpec` and service**. The service creates a low-importance notification channel, calls `startForeground`, checks `Settings.canDrawOverlays`, computes current display geometry, creates `WindowManager.LayoutParams` with `PixelFormat.TRANSLUCENT`, `Gravity.TOP | Gravity.END`, fixed compact dimensions and insets, adds one view only, then calls `wakeOnce()`.
- [ ] **Step 4: Run tests and verify PASS**.
- [ ] **Step 5: Commit** with `feat: keep BOOP eyes in noninteractive overlay service`.

### Task 4: One-time overlay permission bootstrap and manifest isolation

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/MainActivity.java`
- Create: `shield-overlay/app/src/main/AndroidManifest.xml`
- Create: `shield-overlay/app/src/main/res/values/strings.xml`
- Create: `shield-overlay/app/src/main/res/values/themes.xml`
- Create: `shield-overlay/app/src/main/res/drawable/ic_boop_notification.xml`

**Interfaces:**
- `MainActivity` checks `Settings.canDrawOverlays(this)` on create/resume.
- Missing permission launches `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` for `package:com.boop.shieldoverlay` exactly once per visible permission cycle.
- Granted permission calls `ContextCompat.startForegroundService(...)` and immediately `finish()`.

- [ ] **Step 1: Add source/manifest regression checks** that fail if excluded permissions are present.
- [ ] **Step 2: Implement activity and manifest** declaring only `SYSTEM_ALERT_WINDOW`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, the launcher activity, and the non-exported `specialUse` foreground service with subtype property `persistent_noninteractive_visual_overlay_poc`.
- [ ] **Step 3: Run unit/source checks** and verify PASS.
- [ ] **Step 4: Build debug APK locally/CI-equivalent** and inspect manifest/package with `aapt dump badging` / `aapt dump permissions`.
- [ ] **Step 5: Commit** with `feat: add Shield overlay permission bootstrap`.

### Task 5: Dedicated GitHub Actions build, asset extraction, and APK guardrails

**Files:**
- Create: `.github/workflows/build-shield-overlay-poc.yml`
- Create: `tests/test_shield_overlay_source.py`

**Interfaces:**
- Workflow artifact name: `BOOP-Shield-Overlay-POC-debug`.
- Output APK: `shield-overlay/app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 1: Write failing Python source regression test** asserting the project uses separate package/path, required flags/type/transparent drawing, and lacks strings for `RECORD_AUDIO`, `INTERNET`, `BOOT_COMPLETED`, accessibility service declarations, HA, or speech APIs.
- [ ] **Step 2: Run `python3 -m unittest tests.test_shield_overlay_source -v` and verify failure** before workflow/project is complete.
- [ ] **Step 3: Implement workflow**: checkout; unzip `BOOP-Alpha1-project.zip` to a temporary directory; locate `boop_eyes.png` and copy it to `shield-overlay/app/src/main/res/drawable-nodpi/`; run Python regression tests; set up Java 17, Android SDK 36/build-tools 36.0.0, Gradle; run unit tests; assemble debug; inspect badging/permissions; shell-fail unless package equals `com.boop.shieldoverlay` and excluded permissions are absent; upload APK artifact.
- [ ] **Step 4: Run source tests and workflow syntax review**.
- [ ] **Step 5: Commit** with `ci: build isolated BOOP Shield overlay POC`.

### Task 6: End-to-end verification and handoff

**Files:**
- No feature files unless a verification failure requires a focused fix.

**Interfaces:**
- Requires all prior tests/builds green and a downloadable workflow artifact.

- [ ] **Step 1: Trigger/observe the Shield workflow on `boop-shield-overlay-poc`**.
- [ ] **Step 2: Inspect completed workflow logs** for test, build, badging, and permission guard results.
- [ ] **Step 3: Download `BOOP-Shield-Overlay-POC-debug` artifact and confirm the APK exists**.
- [ ] **Step 4: Report the exact APK and the physical Shield test sequence**: launcher → Kodi → video → app switching → HDR/DV, checking transparency, remote passthrough, persistence, and annoyance level.
