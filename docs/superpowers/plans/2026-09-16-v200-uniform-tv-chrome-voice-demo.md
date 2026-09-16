# BOOP v200 Uniform TV Chrome + Voice Demo Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give every BOOP Android TV menu one readable Home-style focus selector, hide the full-screen eyes behind Voice Settings, add an immediate TEST VOICE action, and make natural voice pitch audible as well as speed.

**Architecture:** Add one reusable TV chrome utility in `shield-home-lib`, because the Unified app already depends on that module and can use it to decorate any Activity tree at runtime, including launcher and settings views from the other libraries. Hook it from `UnifiedApplication` only for television UI mode. Keep Voice Settings fixes in the Wall source/materialization layer and add natural pitch at the final Android `AudioTrack` playback stage so Kokoro generation rate is not double-applied.

**Tech Stack:** Android Java 17, programmatic Views, `ActivityLifecycleCallbacks`, `ViewTreeObserver`, `AudioTrack`/`PlaybackParams`, Python contract tests, GitHub Actions, permanent BOOP signer.

**Spec:** `docs/superpowers/specs/2026-09-16-v200-uniform-tv-chrome-voice-demo-design.md`

## Global Constraints

- Owner lineage is `boop-hand-colour-v191`; do not touch `boop-home-centred-favourites-lab-v199`.
- GitHub is the source/build/test authority; no local source edits/builds.
- v200 version name: `1.2.200-uniform-tv-chrome-voice-demo`.
- TV focus chrome: charcoal `#222222`, BOOP blue `#4DB8FF`, 4dp outline, 10dp rounded corners, white text, subtle 1.04x focus scale.
- Automatic chrome targets clickable + focusable `TextView`/`Button`; exclude `EditText`, `SeekBar`, artwork/image/custom card and puppet surfaces.
- Phone/tablet touch UI is unchanged by the global decorator.
- Preserve v197 Home media ownership and v189/v191 accepted character/artwork, lyrics, Startup Manager behavior, sharing protocols, favourites order/layout, and local natural voice model data.

---

### Task 1: RED contract for v200

**Files:**
- Create: `tests/test_v200_uniform_tv_chrome_voice_demo.py`
- Modify: `.github/workflows/build-boop-v191-hand-colour.yml`

**Interfaces:**
- Consumes existing v198 source/workflow.
- Produces a focused contract that requires `BoopTvChrome`, global TV lifecycle installation, opaque/face-hidden Voice Settings, TEST VOICE, and natural `PlaybackParams` pitch.

- [ ] **Step 1: Write the failing source contract**

The test asserts:
```python
assert 'class BoopTvChrome' in chrome
assert 'Color.rgb(77, 184, 255)' in chrome
assert 'BORDER_DP = 4' in chrome
assert 'Configuration.UI_MODE_TYPE_TELEVISION' in chrome
assert 'instanceof EditText' in chrome
assert 'instanceof SeekBar' in chrome
assert 'BoopTvChrome.install(activity)' in unified_app
assert 'BoopTvChrome.accentColor' in focus_chrome
assert 'face.setVisibility(View.INVISIBLE)' in main
assert 'voiceSettingsOverlay.setBackgroundColor(Color.BLACK)' in main
assert 'face.setVisibility(View.VISIBLE)' in main
assert 'TEST VOICE' in natural_patch
assert 'This is how BOOP sounds.' in natural_patch
assert 'wakeFaceForInteraction();' not in the natural preview method body
assert 'PlaybackParams' in natural_backend
assert '.setPitch(pitchForPlayback(pitch))' in natural_backend
assert '.setSpeed(1.0f)' in natural_backend
```
It also verifies version 200/workflow naming once those are introduced.

- [ ] **Step 2: Run the focused GitHub test and verify RED**

Add an early workflow step `python -m pytest -q tests/test_v200_uniform_tv_chrome_voice_demo.py`. Expected result before production edits: failures for missing chrome, TEST VOICE, face ownership, natural pitch, and v200 metadata.

- [ ] **Step 3: Commit the RED test/workflow gate**

Commit message: `test: define v200 TV chrome and voice demo contract`.

---

### Task 2: Shared TV selector chrome

**Files:**
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/BoopTvChrome.java`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/FocusChrome.java`
- Modify: `unified/UnifiedApplication.java`

**Interfaces:**
- Produces: `public static void BoopTvChrome.install(Activity activity)`, `public static int accentColor(Context)`, `public static GradientDrawable filled(Context,int,int,boolean)`.
- Runtime installation is television-only and preserves existing click/key/focus listeners.

- [ ] **Step 1: Implement `BoopTvChrome`**

Use a `WeakHashMap<View, Boolean>` for decorated controls and a `WeakHashMap<View, ViewTreeObserver.OnGlobalLayoutListener>` for roots. `install(Activity)` checks `Configuration.UI_MODE_TYPE_MASK == UI_MODE_TYPE_TELEVISION`, gets `android.R.id.content`, decorates the tree, attaches one global-layout listener for newly created submenu controls, and one global-focus listener that posts final focused/unfocused chrome after legacy focus callbacks.

Eligible controls are clickable + focusable `TextView` values excluding `EditText` and `CompoundButton`; `SeekBar` and artwork/custom cards are naturally excluded because they are not eligible text controls. Apply charcoal/blue rounded backgrounds and animate scale to 1.04/1.0 without replacing existing click or key listeners.

- [ ] **Step 2: Delegate Shield Home `FocusChrome` canonical colors/drawables**

`FocusChrome.accentColor()` and `filled()` call `BoopTvChrome`. Keep artwork-specific outline helpers intact but source their accent and border width from the canonical utility.

- [ ] **Step 3: Install from Unified lifecycle**

In `UnifiedApplication.onActivityResumed`, call `com.boop.shieldhome.BoopTvChrome.install(activity)` after the existing density setup lifecycle registration. This automatically covers Launcher Settings, Apps, Startup Manager, Voice Settings, Build a Boop, developer menus, lyrics actions, setup/permission screens and future programmatic TV menus.

- [ ] **Step 4: Run focused contract**

Expected: TV chrome assertions GREEN; voice/pitch assertions still RED.

- [ ] **Step 5: Commit**

Commit message: `feat: unify BOOP TV focus chrome`.

---

### Task 3: Voice Settings owns the screen and TEST VOICE

**Files:**
- Modify: `source/MainActivity.java`
- Modify: `scripts/patch-unified-natural-voices.py`
- Test: `tests/test_v200_uniform_tv_chrome_voice_demo.py`

**Interfaces:**
- Produces `private void testCurrentVoice()` in materialized `MainActivity`.
- Reuses existing `speakWithAndroidTts`, `naturalSpeechBackend`, `BoopVoiceController.selectedNaturalVoice()`, pitch/rate getters and `finishTtsUtterance()`.

- [ ] **Step 1: Make Voice Settings visually exclusive**

In source `showVoiceSettings()`, remove the opening `wakeFaceForInteraction()`, set `face` INVISIBLE, and change the overlay background from alpha black to `Color.BLACK`. In `hideVoiceSettings()`, restore `face` VISIBLE and `showIdleBlackImmediately()` before normal idle scheduling.

- [ ] **Step 2: Add TEST VOICE directly beneath cadence/speed**

In the natural-voice patch insertion after `cadenceSlider`, create:
```java
Button testVoice = new Button(this);
testVoice.setText("TEST VOICE");
testVoice.setContentDescription("Test current BOOP voice");
testVoice.setOnClickListener(v -> testCurrentVoice());
```
Add it before `addNaturalVoiceSettings()`.

- [ ] **Step 3: Add current-backend demo routing**

`testCurrentVoice()` speaks exactly `This is how BOOP sounds.`. It calls `wakeCoordinator.onTtsStarting()` but never `wakeFaceForInteraction()`, never sets assistant-follow-up flags and never starts recognition. If selected natural voice is usable, call `naturalSpeechBackend.speak` with current pitch/rate and selected speaker. On start failure/error, route the same phrase to `speakWithAndroidTts`. Android backend uses `speakWithAndroidTts` directly.

- [ ] **Step 4: Stop natural preview waking the eyes**

Remove `wakeFaceForInteraction()` only from `previewNaturalVoice`; retain TTS coordination and successful voice selection semantics.

- [ ] **Step 5: Run focused contract**

Expected: screen ownership and TEST VOICE assertions GREEN; natural pitch assertions still RED.

- [ ] **Step 6: Commit**

Commit message: `feat: add in-place voice demo and opaque settings`.

---

### Task 4: Audible natural pitch

**Files:**
- Modify: `source/BoopNaturalSpeechBackend.java`
- Test: `tests/test_v200_uniform_tv_chrome_voice_demo.py`
- Preserve: `tests/test_unified_v70_natural_voices_contract.py`

**Interfaces:**
- `speak(text,speakerId,pitch,rate,callback)` passes `speedForRate(rate)` to generation and separately passes `pitchForPlayback(pitch)` to playback.
- `play(RequestState,float[],int,float)` applies pitch only.

- [ ] **Step 1: Thread pitch through synthesis/playback**

Pass the clamped pitch from `speak()` through `synthesizeAndPlay()` to `play()`.

- [ ] **Step 2: Apply `PlaybackParams` safely**

After successful static PCM write and before `track.play()`:
```java
try {
    android.media.PlaybackParams params = new android.media.PlaybackParams()
            .allowDefaults()
            .setSpeed(1.0f)
            .setPitch(pitchForPlayback(pitch));
    track.setPlaybackParams(params);
} catch (RuntimeException unsupported) {
    android.util.Log.w("BOOP-NaturalVoice", "Natural pitch unavailable; playing original PCM", unsupported);
}
```
Rate remains Sherpa generation speed only, avoiding double speed application.

- [ ] **Step 3: Run focused + existing natural voice tests**

Run v200 contract and `tests/test_unified_v70_natural_voices_contract.py`. Expected GREEN.

- [ ] **Step 4: Commit**

Commit message: `feat: apply natural voice pitch at playback`.

---

### Task 5: v200 build identity and full verification

**Files:**
- Modify: `unified/app-build.gradle`
- Modify: `.github/workflows/build-boop-v191-hand-colour.yml`
- If required by inherited production allowlist: `tests/test_playback_dance_v169.py` only to add intentional v200 production files.

**Interfaces:**
- Version code 200, version name `1.2.200-uniform-tv-chrome-voice-demo`.
- Artifact name `BOOP-Unified-v200-Uniform-TV-Chrome-Voice-Demo`.

- [ ] **Step 1: Bump build/workflow identity**

Rename workflow/concurrency/build/artifact verification strings from v198 to v200 and include the v200 focused test in the preservation gate.

- [ ] **Step 2: Run complete GitHub Actions workflow**

Require inherited character, lyrics, music/bass, startup, v198 voice profile, natural voice, sharing, materialization, Android compile and signer checks to pass.

- [ ] **Step 3: If inherited production allowlist blocks intentional files, update only that allowlist**

The accepted set may add `BoopTvChrome.java`, `FocusChrome.java`, `UnifiedApplication.java`, `MainActivity.java` materialization patch inputs and `BoopNaturalSpeechBackend.java` as appropriate. Do not change music logic to satisfy the guard.

- [ ] **Step 4: Verify artifact**

Require package `com.boop.alpha1`, version 200/name, permanent signer digest, presence of `BoopTvChrome`, natural backend, lyrics/Home bytecode and accepted artwork hashes.

- [ ] **Step 5: Commit any CI-only guard correction and rerun to full GREEN**

---

### Task 6: Joint Shield verification

**Files:**
- Documentation only after physical results: `docs/handoffs/2026-09-16-v200-uniform-tv-chrome-voice-demo.md`, `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md`.

**Interfaces:**
- Signed v200 APK from the successful GitHub artifact.

- [ ] **Step 1: Download signed artifact on Yoga and verify its SHA/package/version**
- [ ] **Step 2: Record Shield accessibility-service setting before install**
- [ ] **Step 3: Install v200 with `adb install -r`, preserving app data and the already-downloaded natural pack**
- [ ] **Step 4: Verify version and unchanged accessibility setting**
- [ ] **Step 5: Check representative TV menus**

Open Home, Launcher Settings, device/room settings, Voice Settings, Build a Boop and Startup Manager. Confirm focused menu/action buttons use charcoal + BOOP-blue outline and the giant full-screen eyes are absent behind Voice Settings.

- [ ] **Step 6: Audition voice sliders**

Use TEST VOICE at deliberately low/high pitch and speed with a natural voice. CI proves routing/build; Ryan supplies the audible/visual acceptance verdict.

- [ ] **Step 7: Write durable receipt and compact current status**

Record exact built commit/run/artifact/hash/install evidence and any physical acceptance limits. Preserve v198/v197 rollback references and keep v199 lab separate.
