# BOOP Unified v70 Developer Lab Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build signed BOOP Unified v70 with an offline spoken `dev menu`, immersive dev presentation, and app-specific local notification dood previews driven by the real BOOP face/hands renderer.

**Architecture:** Keep production notification intake untouched. Add a tiny local speech intent and inject it into the existing recognized-speech path before Home Assistant/chat routing; expand the existing dev menu model/activity and local presentation factory; add a dev-only procedural identity-icon resolver used by the existing `BoopNotificationPuppetView`. Lock privacy stays delegated to `BoopNotificationPresentation.from(..., locked=true)` so title/body redaction is the production contract.

**Tech Stack:** Android Java 17, Gradle 9.6, JUnit 4, Python/pytest source contracts, GitHub Actions.

**Spec:** This file's Global Constraints section captures the user-approved 2026-09-09 v70 brief.

## Global Constraints

- Canonical branch: `boop-unified`; do not modify `main` unless a genuinely shared product contract requires it.
- Version: `versionCode 70`, `versionName "1.2.24-unified-dev-menu-doods"`.
- Package stays `com.boop.alpha1`; permanent signer stays unchanged.
- No installs, ADB actions, device grants, notification posting, listener requirement for preview, or live-message dependency.
- `BoopDevMenuActivity` remains `android:exported="false"`.
- No screenshot tests, golden-image tests, pixel comparisons, automated visual judgments, or visual acceptance claims.
- Runtime doods use current procedural eyes and exact locked five-finger hands asset `unified/assets/boop-notifications/boop-yellow-hands-approved.png` with size `1809990`, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`, Git blob `d47037271bf320f4f110e3f8416f59882062afac`.
- Preserve eye patch order: `patch-unified-reading-eyes.py`, `patch-v64-procedural-sclera.py`, `patch-v65-feathered-sclera.py`; do not reintroduce bitmap hue setters after procedural eyes.
- Do not create or repoint a v70 rollback checkpoint. Ryan owns physical/visual/device/acoustic acceptance.

---

### Task 1: Local spoken developer-menu interception

**Files:**
- Create: `source/BoopDevMenuIntent.java`
- Create: `source-test/BoopDevMenuIntentTest.java`
- Modify: `scripts/patch-unified-dev-menu.py`
- Create: `tests/test_unified_v70_dev_lab_contract.py`
- Modify: `.github/workflows/build-boop-unified.yml`

**Interfaces:**
- Consumes: `MainActivity.handleRecognizedSpeech(String)` and existing dev activity.
- Produces: `BoopDevMenuIntent.matches(String)` and materialized early-return launch of `BoopDevMenuActivity` before `commandRouter.process(transcript)`.

- [ ] **Step 1: Write failing matcher and source-order contracts**

```java
assertTrue(BoopDevMenuIntent.matches("dev menu"));
assertTrue(BoopDevMenuIntent.matches("Dev menu."));
assertFalse(BoopDevMenuIntent.matches("open settings"));
```

```python
assert main.index('BoopDevMenuIntent.matches(transcript)') < main.index('commandRouter.process(transcript)')
assert 'BoopDevMenuActivity' in main
```

- [ ] **Step 2: Run the focused tests and confirm RED for the missing intent/interception**

Run the branch's nonvisual Unified CI test lane. Expected: failure specifically because `BoopDevMenuIntent`/the v70 interception is absent.

- [ ] **Step 3: Implement minimal local matcher and early-return launch**

```java
if (BoopDevMenuIntent.matches(transcript)) {
    startActivity(new Intent().setClassName(
            getPackageName(), "com.boop.alpha1.BoopDevMenuActivity"));
    return;
}
```

- [ ] **Step 4: Re-run focused tests and confirm GREEN**

Expected: matcher and source-order contract pass without invoking Home Assistant/chat.

- [ ] **Step 5: Commit**

```text
feat(unified): intercept spoken dev menu locally
```

### Task 2: Expand the dev action model and local demo presentations

**Files:**
- Modify: `source/BoopDevMenuModel.java`
- Modify: `source/BoopDevNotificationPreview.java`
- Modify: `source-test/BoopNotificationDevMenuModelTest.java`
- Modify: `source-test/BoopNotificationDevPreviewTest.java`

**Interfaces:**
- Consumes: `BoopNotificationPresentation`, `BoopNotificationEnvelope`, `BoopNotificationSurface`.
- Produces: actions for Wake, Think, Berry 1/2/3, Shake, Sleep, Facebook, WhatsApp, Gmail, X/Twitter, YouTube, Messenger, Instagram, Discord, Spotify, Reddit, Locked, Bundle.

- [ ] **Step 1: Update tests first for the complete action list and privacy/local-model contracts**

```java
assertEquals("Facebook", presentation.cards().get(0).appLabel());
assertEquals(BoopNotificationSurface.LOCKED, locked.surface());
assertNull(locked.cards().get(0).title());
assertNull(locked.cards().get(0).text());
```

- [ ] **Step 2: Run notification unit tests and confirm RED on the missing service actions**

Run `:app:testDebugUnitTest --tests '*BoopNotification*'`. Expected: missing enum/actions or mismatched v69 generic preview behavior.

- [ ] **Step 3: Implement service-specific fake envelopes and bundle**

Each service action returns one local `BoopNotificationPresentation`; locked uses `BoopNotificationPresentation.from(..., LOCKED, true)`; bundle contains several service demo envelopes. No runtime/listener/posting API is called.

- [ ] **Step 4: Re-run notification tests and confirm GREEN**

Expected: all model/presentation tests pass, including redaction.

- [ ] **Step 5: Commit**

```text
feat(unified): add app-specific notification demo models
```

### Task 3: Procedural app identity treatment inside the real puppet renderer

**Files:**
- Create: `source/BoopDevNotificationIdentity.java`
- Create: `source/BoopDevNotificationIconDrawable.java`
- Modify: `source/BoopNotificationPuppetView.java`
- Create: `source-test/BoopDevNotificationIdentityTest.java`

**Interfaces:**
- Consumes: dev demo package names from `BoopDevNotificationPreview`.
- Produces: deterministic local identity specs and an Android Drawable for dev packages; production packages still resolve through `PackageManager`.

- [ ] **Step 1: Write failing catalog tests**

```java
assertEquals("Facebook", BoopDevNotificationIdentity.forPackage("boop.dev.facebook").label());
assertNull(BoopDevNotificationIdentity.forPackage("com.real.production.app"));
```

- [ ] **Step 2: Run focused unit tests and confirm RED because the identity catalog is absent**

Run `:app:testDebugUnitTest --tests '*BoopDevNotificationIdentityTest'`.

- [ ] **Step 3: Implement identity specs and procedural icon drawing**

Use geometry/text strokes only for recognizable service treatments. `BoopNotificationPuppetView.loadAppIcon()` first checks the dev catalog, otherwise preserves its current `PackageManager` lookup/fallback. Face rendering remains the real `BoopFaceView`; hands remain `R.drawable.boop_notification_hands`.

- [ ] **Step 4: Re-run tests and confirm GREEN**

No visual assertions are added.

- [ ] **Step 5: Commit**

```text
feat(unified): render local notification service identities
```

### Task 4: Immersive, repeatable, remote-friendly dev activity

**Files:**
- Modify: `source/BoopDevMenuActivity.java`
- Modify: `source-test/BoopNotificationDevMenuModelTest.java`
- Modify: `tests/test_unified_v70_dev_lab_contract.py`

**Interfaces:**
- Consumes: real `BoopFaceView` animation methods and local notification presentation factory.
- Produces: fullscreen black menu/previews, separate Berry 1/2/3 actions, Stop/reset for infinite Think, clean Back-to-menu behavior.

- [ ] **Step 1: Add nonvisual contracts for all actions mapping to implementations and immersive calls**

```python
for token in ('WAKE','THINK','BERRY_1','BERRY_2','BERRY_3','SHAKE','SLEEP'):
    assert token in activity
assert 'WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars()' in activity
```

- [ ] **Step 2: Run contracts and confirm RED on v69 activity/model**

Expected: missing Berry variants/fullscreen/Stop mapping.

- [ ] **Step 3: Implement fullscreen and action dispatch**

Use the same API-R `WindowInsetsController` and legacy `SYSTEM_UI_FLAG_IMMERSIVE_STICKY` pattern already used by BOOP. Think runs until Stop/reset; finite animations can be replayed by repeated button activation.

- [ ] **Step 4: Re-run contracts/unit tests and confirm GREEN**

No visual CI checks.

- [ ] **Step 5: Commit**

```text
feat(unified): make developer lab immersive and repeatable
```

### Task 5: Security/materialization guards and v70 version

**Files:**
- Modify: `unified/app-build.gradle`
- Modify: `tests/test_unified_v70_dev_lab_contract.py`
- Modify: `.github/workflows/build-boop-unified.yml`

**Interfaces:**
- Consumes: manifest, materialization script, approved hand asset, procedural-eye patch sequence.
- Produces: v70 package/version plus executable CI contracts.

- [ ] **Step 1: Add/strengthen tests first**

```python
assert 'android:name=".BoopDevMenuActivity"' in manifest
assert 'android:exported="false"' in manifest
assert sha256(hands).hexdigest() == '26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1'
assert materialize.index('patch-unified-reading-eyes.py') < materialize.index('patch-v64-procedural-sclera.py') < materialize.index('patch-v65-feathered-sclera.py')
assert 'NotificationManager' not in dev_activity
assert 'BoopNotificationRuntime' not in dev_activity
assert 'BoopNotificationListenerService' not in dev_activity
```

- [ ] **Step 2: Run the contract lane and confirm expected RED until v70 version/contracts are wired**

- [ ] **Step 3: Set version and wire the contract test into Unified CI**

```gradle
versionCode 70
versionName "1.2.24-unified-dev-menu-doods"
```

- [ ] **Step 4: Run all nonvisual CI lanes until GREEN**

Materialization, notification/dev contracts, wake handoff, Launcher, Shield functional, Unified functional, Shield HOME routing, signed build, package/version/signature/archive and artifact upload must all pass.

- [ ] **Step 5: Commit production code**

```text
feat(unified): build v70 developer dood lab
```

### Task 6: Release verification and documentation sync

**Files:**
- Modify: `SESSION_HANDOFF.md`
- Modify: `BOOP_STATUS.md`
- Modify: `BOOP_UNIFIED_MEMORY.md`
- Create: `docs/BOOP_UNIFIED_V70_RECEIPT.md`

**Interfaces:**
- Consumes: exact green production commit, GitHub workflow/run data and signed artifact.
- Produces: independent artifact/hash/signer receipt and final docs-only head.

- [ ] **Step 1: Download the exact green workflow artifact and independently hash ZIP/APK**

Confirm APK hash equals CI `apk-sha256.txt` and signer SHA-256 equals the canonical permanent signer.

- [ ] **Step 2: Record workflow IDs, artifact ID, ZIP digest, APK digest, signer digest, production SHA, Shield/Unified test totals and Shield HOME result**

- [ ] **Step 3: Update handoff/status/memory/receipt with physical acceptance pending**

Explicitly state GitHub performed NO visual acceptance, no v70 rollback checkpoint was created, and v70 adds local spoken `dev menu`, fullscreen dev lab, and app-specific notification dood previews using current eyes plus exact five-finger hands.

- [ ] **Step 4: Commit docs only**

```text
docs(unified): record v70 signed artifact [skip ci]
```

- [ ] **Step 5: Re-fetch live `boop-unified` and verify final docs HEAD descends from the production commit**

Final delivery includes the signed APK, version, APK SHA-256, production commit SHA, CI summary and a short physical test list. Visual acceptance remains Ryan's decision.
