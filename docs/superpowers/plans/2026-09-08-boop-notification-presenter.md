# BOOP Notification Presenter Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build BOOP's opt-in phone-wide notification presenter so selected app/channel notifications can wake the display, interrupt with BOOP, group bursts, open the original target, and leave Android's real notification state intact except for normal successful-tap auto-cancel semantics.

**Architecture:** Keep notification policy as pure Java domain logic in `source/`, with Android adapters for Notification Listener intake, permission/setup, overlays, lock-screen Activity, inbox, sound/vibration and settings. The listener learns notification channels from `NotificationListenerService.Ranking.getChannel()` rather than trying to enumerate or modify arbitrary third-party channels; message content is read only after the user's package+channel allowlist passes. Presentation uses the current reconciled BOOP face renderer plus the exact approved five-digit hand asset at runtime, never a baked/generated mascot.

**Tech Stack:** Java 17, Android SDK 36, minSdk 29, Android framework Views, `NotificationListenerService`, `WindowManager.TYPE_APPLICATION_OVERLAY`, `Activity.setShowWhenLocked`, `Activity.setTurnScreenOn`, `PendingIntent`, `LauncherApps`, `SharedPreferences`, `AudioTrack`, `VibrationEffect`, JUnit 4, Python 3.12 structural tests, Gradle 9.6, existing permanent BOOP signing workflow.

**Spec:** `docs/superpowers/specs/2026-09-08-boop-notification-presenter-design.md`

## Global Constraints

- Canonical app branch is `boop-unified`; package remains `com.boop.alpha1`; permanent signer remains unchanged.
- Do not start notification production-code commits until the current procedural-eye/sleep/hue work has been reconciled into canonical `boop-unified` and Ryan has physically accepted the resulting eye/sleep/hue state.
- Phone modes only for this feature. `BoopDeviceProfile.Mode.SHIELD` must not initialize, onboard, listen for, configure, or present phone notifications.
- Notification presentation is default-deny. `masterEnabled && appEnabled && channelEnabled` is required before BOOP may read rich notification content or present it.
- Initial presentation timeout is 8,000 ms; user may set 3,000-30,000 ms.
- Initial burst window is 4,000 ms. A notification absorbed by an already-visible presentation updates that presentation without replaying the cue.
- Locked or screen-off presentation exposes app identity/icon and count only. No sender, title, message body, account detail, contact photo or preview appears while locked.
- Swiping or timing out BOOP never cancels the Android notification.
- Successful tap sends the original `PendingIntent`; if the source notification is `FLAG_AUTO_CANCEL`, request listener cancellation only after the send succeeds.
- BOOP stores package/channel choices, timeout and minimal dedupe/channel metadata only. No durable title/body/sender/contact/message archive and no cloud upload.
- BOOP must not call `NotificationListenerService.getNotificationChannels(pkg,user)` or `updateNotificationChannel(pkg,user,channel)` for arbitrary apps. Ordinary notification listeners do not own that authority.
- Channel/category inventory is learned from `Ranking.getChannel()` on active/new notifications. An unseen channel is not BOOP-presented until the user later enables that observed channel.
- User-silencing is explicit: BOOP opens Android's channel settings with `Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS`; it never silently changes third-party channel sound/vibration.
- BOOP's replacement cue/vibration may play only when the selected channel is observed as having both native sound and native vibration disabled. Otherwise visual presentation may occur but BOOP suppresses its own cue to avoid double alerts.
- First-run setup asks for Notification Access and Display over other apps. Declining either remains non-blocking and never disables normal BOOP.
- Do not use root, device-admin, accessibility automation, secure-setting writes, alarm/call full-screen-intent abuse, keyguard bypass, `QUERY_ALL_PACKAGES`, or a new foreground service merely to make notification presentation work.
- Preserve exact approved BOOP eyes, user iris hue, blink behavior, current reconciled procedural-eye/sleep renderer, headphones/puppetry, HA behavior, wake architecture, Launcher and Shield behavior.
- Notification hands use the exact lock-screen reference binary from `animation-freddie-mercury:boop-yellow-hands-approved.png`, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`, in a notification-specific asset path. Do not overwrite the older general hand-master record/hash.
- Automated checks are functional/structural/integrity only. No screenshots, golden images, emulator appearance grading, aesthetic source-string tests or CI claims that BOOP looks/sounds good.
- Ryan owns real-device visual, lock-screen, interruption and acoustic acceptance.

---

## Hard Gate 0: Reconcile the current eye/sleep work before notification code

This is a preflight gate, not a notification implementation task.

- [ ] **Step 1: Fetch live heads without touching another task's checkout**

```bash
git fetch origin main boop-unified boop-unified-v63-fast-eyes animation-freddie-mercury --tags
git rev-parse origin/main
git rev-parse origin/boop-unified
git rev-parse origin/boop-unified-v63-fast-eyes
```

Expected before notification implementation: `origin/boop-unified` is the live canonical AIO branch and contains the user-approved reconciled procedural-eye/sleep/hue work, not merely the older v62 canonical state.

- [ ] **Step 2: Read the canonical handoff/status and verify physical acceptance is explicit**

```bash
sed -n '1,240p' SESSION_HANDOFF.md
sed -n '1,240p' BOOP_STATUS.md
sed -n '1,260p' BOOP_UNIFIED_MEMORY.md
```

If those files still describe sleep/hue as pending, experimental, WIP or physically unaccepted, stop. Finish and physically accept that track first, update canonical documentation, then resume this plan from the new live canonical head.

- [ ] **Step 3: Verify exact visual assets**

```bash
sha256sum unified/assets/boop-eyes/boopApprovedEyes.png
git show origin/animation-freddie-mercury:boop-yellow-hands-approved.png | sha256sum
```

Expected hashes:

```text
ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22  unified/assets/boop-eyes/boopApprovedEyes.png
26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1  -
```

- [ ] **Step 4: Create/use an isolated execution worktree from the newly reconciled canonical head**

Use `superpowers:using-git-worktrees` at execution time. Do not switch/reset a checkout owned by another running BOOP task.

---

## File Map

**Create, notification domain/config:**
- `source/BoopNotificationChannelInfo.java` - observed channel identity/effect metadata and stable encoding.
- `source/BoopNotificationSettingsState.java` - immutable master/app/channel/timeout state.
- `source/BoopNotificationSettingsCodec.java` - separator-safe package/channel keys and metadata encoding.
- `source/BoopNotificationSettingsStore.java` - `SharedPreferences` adapter for config and observed-channel metadata only.
- `source/BoopNotificationPolicy.java` - default-deny allow decision.
- `source/BoopNotificationIntakePolicy.java` - decides whether listener may read rich notification content.
- `source/BoopNotificationEnvelope.java` - transient rich notification domain object; no `PendingIntent`.
- `source/BoopNotificationPresentation.java` - locked/unlocked presentation model and redaction.
- `source/BoopNotificationCoordinator.java` - dedupe, active bundle, burst and rebuild state machine.

**Create, Android runtime/platform:**
- `source/BoopNotificationRuntime.java`
- `source/BoopNotificationListenerService.java`
- `source/BoopNotificationPermissionState.java`
- `source/BoopNotificationOnboardingState.java`
- `source/BoopNotificationStartupGate.java`
- `source/BoopNotificationOnboardingActivity.java`
- `source/BoopNotificationAppEntry.java`
- `source/BoopNotificationAppCatalogModel.java`
- `source/BoopNotificationAppCatalog.java`
- `source/BoopNotificationSettingsActivity.java`
- `source/BoopNotificationSurface.java`
- `source/BoopNotificationSurfaceSelector.java`
- `source/BoopNotificationHost.java`
- `source/BoopNotificationInPlaceController.java`
- `source/BoopNotificationOverlayController.java`
- `source/BoopNotificationLockActivity.java`
- `source/BoopNotificationInboxActivity.java`
- `source/BoopNotificationTapPolicy.java`
- `source/BoopNotificationTapLauncher.java`
- `source/BoopNotificationSwipeGesture.java`
- `source/BoopNotificationPuppetView.java`
- `source/BoopNotificationCuePolicy.java`
- `source/BoopNotificationCueRenderer.java`
- `source/BoopNotificationCue.java`

**Create, assets/materialization/contracts:**
- `unified/assets/boop-notifications/README.md`
- `unified/assets/boop-notifications/boop-yellow-hands-approved.png`
- `scripts/materialize-boop-notification-assets.py`
- `scripts/patch-unified-notifications.py`
- `tests/test_unified_notification_manifest_contract.py`
- `tests/test_unified_notification_asset_integrity.py`

**Create, JUnit tests:**
- `source-test/BoopNotificationSettingsCodecTest.java`
- `source-test/BoopNotificationPolicyTest.java`
- `source-test/BoopNotificationIntakePolicyTest.java`
- `source-test/BoopNotificationPresentationTest.java`
- `source-test/BoopNotificationCoordinatorTest.java`
- `source-test/BoopNotificationStartupGateTest.java`
- `source-test/BoopNotificationAppCatalogModelTest.java`
- `source-test/BoopNotificationSurfaceSelectorTest.java`
- `source-test/BoopNotificationTapPolicyTest.java`
- `source-test/BoopNotificationSwipeGestureTest.java`
- `source-test/BoopNotificationCuePolicyTest.java`
- `source-test/BoopNotificationCueRendererTest.java`

**Modify:**
- `source/AndroidManifest.xml`
- `unified/UnifiedApplication.java`
- `unified/UnifiedEntryActivity.java`
- `scripts/materialize-unified.sh`
- `.github/workflows/build-boop-unified.yml`
- `unified/app-build.gradle` only for the final version bump after implementation passes.
- `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md` after a signed candidate exists.

---

### Task 1: Build default-deny notification settings and observed-channel metadata

**Files:**
- Create: `source/BoopNotificationChannelInfo.java`
- Create: `source/BoopNotificationSettingsState.java`
- Create: `source/BoopNotificationSettingsCodec.java`
- Create: `source/BoopNotificationSettingsStore.java`
- Create: `source/BoopNotificationPolicy.java`
- Create: `source/BoopNotificationIntakePolicy.java`
- Test: `source-test/BoopNotificationSettingsCodecTest.java`
- Test: `source-test/BoopNotificationPolicyTest.java`
- Test: `source-test/BoopNotificationIntakePolicyTest.java`

**Interfaces:**
- `BoopNotificationSettingsState(boolean masterEnabled, long timeoutMs, Set<String> enabledApps, Set<String> enabledChannelKeys)`.
- `boolean BoopNotificationSettingsState.isAppEnabled(String packageName)`.
- `boolean BoopNotificationSettingsState.isChannelEnabled(String packageName, String channelId)`.
- `long BoopNotificationSettingsState.timeoutMs()` clamped to 3,000-30,000; default store value 8,000.
- `String BoopNotificationSettingsCodec.channelKey(String packageName, String channelId)`.
- `String BoopNotificationChannelInfo.encode()` and `static BoopNotificationChannelInfo decode(String encoded)` use URL-safe Base64 without padding for arbitrary Unicode names/IDs.
- `boolean BoopNotificationChannelInfo.nativeEffectsSilent()` is true only when effect state is known and both sound and vibration are off.
- `boolean BoopNotificationPolicy.allows(state, packageName, channelId)` requires master+app+channel.
- `BoopNotificationIntakePolicy.Mode decide(state, packageName, channelId)` returns `OBSERVE_CHANNEL_ONLY` or `READ_RICH_CONTENT`.

- [ ] **Step 1: Write failing codec/policy/privacy tests**

```java
@Test public void defaultStateDeniesEverything() {
    BoopNotificationSettingsState state = BoopNotificationSettingsState.defaults();
    assertFalse(BoopNotificationPolicy.allows(state, "com.chat.app", "messages"));
    assertEquals(8000L, state.timeoutMs());
}

@Test public void requiresMasterAppAndChannel() {
    Set<String> apps = Set.of("com.chat.app");
    Set<String> channels = Set.of(BoopNotificationSettingsCodec.channelKey("com.chat.app", "messages"));
    BoopNotificationSettingsState off = new BoopNotificationSettingsState(false, 8000L, apps, channels);
    BoopNotificationSettingsState on = new BoopNotificationSettingsState(true, 8000L, apps, channels);
    assertFalse(BoopNotificationPolicy.allows(off, "com.chat.app", "messages"));
    assertTrue(BoopNotificationPolicy.allows(on, "com.chat.app", "messages"));
    assertFalse(BoopNotificationPolicy.allows(on, "com.chat.app", "promotions"));
}

@Test public void deniedChannelDoesNotPermitRichContentRead() {
    BoopNotificationSettingsState state = BoopNotificationSettingsState.defaults();
    assertEquals(BoopNotificationIntakePolicy.Mode.OBSERVE_CHANNEL_ONLY,
        BoopNotificationIntakePolicy.decide(state, "com.chat.app", "messages"));
}

@Test public void channelMetadataRoundTripsUnicodeAndSeparators() {
    BoopNotificationChannelInfo original = new BoopNotificationChannelInfo(
        "com.example|odd", "messages/a|b", "Messages • 家族", true, false, false, 1234L);
    assertEquals(original, BoopNotificationChannelInfo.decode(original.encode()));
}
```

- [ ] **Step 2: Run the focused tests and verify RED**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotificationSettingsCodecTest' \
  --tests '*BoopNotificationPolicyTest' \
  --tests '*BoopNotificationIntakePolicyTest' --stacktrace
```

Expected: compilation/test failure because the new notification settings types do not exist yet.

- [ ] **Step 3: Implement the minimal settings model and policy**

Use these constants and decisions exactly:

```java
static final long DEFAULT_TIMEOUT_MS = 8_000L;
static final long MIN_TIMEOUT_MS = 3_000L;
static final long MAX_TIMEOUT_MS = 30_000L;
```

```java
static boolean allows(BoopNotificationSettingsState state, String packageName, String channelId) {
    return state != null
            && state.masterEnabled()
            && state.isAppEnabled(packageName)
            && state.isChannelEnabled(packageName, channelId);
}
```

`BoopNotificationSettingsStore` uses one preferences file named `boop_notifications` with keys `master_enabled`, `timeout_ms`, `enabled_apps`, `enabled_channels`, and `observed_channels`. `observed_channels` contains only encoded `BoopNotificationChannelInfo` metadata. It must never contain title/body/sender/contact fields.

- [ ] **Step 4: Re-run focused tests**

Run the Step 2 command again. Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add source/BoopNotification*.java source-test/BoopNotification*Test.java
git commit -m "feat(unified): add notification opt-in policy"
```

---

### Task 2: Build transient presentation, redaction, dedupe and bundle state

**Files:**
- Create: `source/BoopNotificationEnvelope.java`
- Create: `source/BoopNotificationPresentation.java`
- Create: `source/BoopNotificationCoordinator.java`
- Test: `source-test/BoopNotificationPresentationTest.java`
- Test: `source-test/BoopNotificationCoordinatorTest.java`

**Interfaces:**
- `BoopNotificationEnvelope` fields: `key`, `packageName`, `appLabel`, `channelId`, `channelName`, `title`, `text`, `postTimeMs`, `autoCancel`.
- `BoopNotificationPresentation.from(List<BoopNotificationEnvelope> bundle, BoopNotificationSurface surface, boolean locked)`.
- Locked `Card` objects retain key/package/appLabel but force `title == null` and `text == null`.
- `BoopNotificationCoordinator(long burstWindowMs)` with production value 4,000 ms.
- `Decision onPosted(BoopNotificationEnvelope envelope, long nowMs, BoopNotificationSettingsState settings)`.
- `void rebuild(Collection<BoopNotificationEnvelope> active, BoopNotificationSettingsState settings)` seeds active inbox state but never creates a visible presentation or cue.
- `void onRemoved(String key)`.
- `void onPresentationDismissed()` clears the visible bundle but not Android-active inbox entries.
- `List<BoopNotificationEnvelope> activeNotifications()` and `visibleBundle()` return immutable snapshots.
- `Decision.playCue()` is true only for a brand-new visible presentation, false for duplicate/update/absorbed bundle members.

- [ ] **Step 1: Write failing redaction/grouping tests**

```java
@Test public void lockedPresentationRemovesRichText() {
    BoopNotificationEnvelope n = fixture("k1", "com.chat", "messages", "Alice", "Dinner?");
    BoopNotificationPresentation p = BoopNotificationPresentation.from(
        List.of(n), BoopNotificationSurface.LOCKED, true);
    assertNull(p.cards().get(0).title());
    assertNull(p.cards().get(0).text());
    assertEquals("com.chat", p.cards().get(0).packageName());
}

@Test public void secondNotificationJoinsVisibleBundleWithoutSecondCue() {
    BoopNotificationCoordinator c = new BoopNotificationCoordinator(4000L);
    BoopNotificationSettingsState s = allowed("com.chat", "messages");
    BoopNotificationCoordinator.Decision first = c.onPosted(fixture("k1", "com.chat", "messages", "A", "1"), 1000L, s);
    BoopNotificationCoordinator.Decision second = c.onPosted(fixture("k2", "com.chat", "messages", "B", "2"), 2200L, s);
    assertEquals(BoopNotificationCoordinator.Kind.PRESENT, first.kind());
    assertTrue(first.playCue());
    assertEquals(BoopNotificationCoordinator.Kind.UPDATE, second.kind());
    assertFalse(second.playCue());
    assertEquals(2, second.bundle().size());
}

@Test public void rebuildNeverInterruptsOrPlaysCue() {
    BoopNotificationCoordinator c = new BoopNotificationCoordinator(4000L);
    BoopNotificationSettingsState s = allowed("com.chat", "messages");
    c.rebuild(List.of(fixture("k1", "com.chat", "messages", "A", "1")), s);
    assertEquals(1, c.activeNotifications().size());
    assertTrue(c.visibleBundle().isEmpty());
}
```

Also cover mixed-app bundling, same-key updates, removal, dismissal and denied-channel rejection.

- [ ] **Step 2: Run focused tests and verify RED**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotificationPresentationTest' \
  --tests '*BoopNotificationCoordinatorTest' --stacktrace
```

Expected: FAIL because presentation/coordinator types are absent.

- [ ] **Step 3: Implement the state machine**

Use a `LinkedHashMap<String, BoopNotificationEnvelope>` for Android-active entries and a `LinkedHashSet<String>` for current visible bundle keys. A new allowed key when no presentation is visible returns `PRESENT/playCue=true`; any allowed key while a presentation is visible returns `UPDATE/playCue=false`; reposting an existing visible key updates its envelope in place without cue; `rebuild` populates only active entries.

- [ ] **Step 4: Re-run focused tests**

Run Step 2 again. Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add source/BoopNotificationEnvelope.java source/BoopNotificationPresentation.java \
  source/BoopNotificationCoordinator.java source-test/BoopNotificationPresentationTest.java \
  source-test/BoopNotificationCoordinatorTest.java
git commit -m "feat(unified): add notification bundle coordinator"
```

---

### Task 3: Add Notification Listener intake and app-owned runtime without reading denied content

**Files:**
- Create: `source/BoopNotificationRuntime.java`
- Create: `source/BoopNotificationListenerService.java`
- Modify: `unified/UnifiedApplication.java`

**Interfaces:**
- `static BoopNotificationRuntime initialize(Application application)`; calling twice returns the existing runtime.
- `static BoopNotificationRuntime get(Context context)`.
- `void attachListener(BoopNotificationListenerService listener)` / `detachListener(listener)`.
- `void observeChannel(BoopNotificationChannelInfo info)`.
- `void post(BoopNotificationEnvelope envelope, PendingIntent contentIntent, BoopNotificationChannelInfo channelInfo)`.
- `void rebuild(List<RuntimeRecord> records)` seeds coordinator/inbox and PendingIntent map without presenting/cueing.
- `void remove(String key)`.
- `void cancelAfterSuccessfulAutoCancelTap(String key)` delegates to the currently attached listener only.

- [ ] **Step 1: Add a failing privacy-order assertion to `BoopNotificationIntakePolicyTest`**

```java
@Test public void allowedChannelPermitsRichContentReadOnlyAfterFullOptIn() {
    BoopNotificationSettingsState state = fullyAllowed("com.chat", "messages");
    assertEquals(BoopNotificationIntakePolicy.Mode.READ_RICH_CONTENT,
        BoopNotificationIntakePolicy.decide(state, "com.chat", "messages"));
    assertEquals(BoopNotificationIntakePolicy.Mode.OBSERVE_CHANNEL_ONLY,
        BoopNotificationIntakePolicy.decide(state, "com.chat", "promotions"));
}
```

- [ ] **Step 2: Run the focused test**

Expected: PASS only if Task 1 privacy logic already enforces this boundary. If it fails, repair Task 1 before wiring Android APIs.

- [ ] **Step 3: Implement listener channel observation before rich-content extraction**

`onNotificationPosted(StatusBarNotification sbn, RankingMap rankingMap)` must follow this order:

```java
String key = sbn.getKey();
String packageName = sbn.getPackageName();
Ranking ranking = new Ranking();
NotificationChannel channel = rankingMap != null && rankingMap.getRanking(key, ranking)
        ? ranking.getChannel() : null;
BoopNotificationChannelInfo info = channelInfo(packageName, sbn.getNotification(), channel);
runtime.observeChannel(info);
BoopNotificationSettingsState settings = runtime.settings();
if (BoopNotificationIntakePolicy.decide(settings, packageName, info.channelId())
        != BoopNotificationIntakePolicy.Mode.READ_RICH_CONTENT) {
    return;
}
BoopNotificationEnvelope envelope = richEnvelope(sbn, info);
runtime.post(envelope, sbn.getNotification().contentIntent, info);
```

`richEnvelope(...)` is the only listener method allowed to read `Notification.EXTRA_TITLE`, `Notification.EXTRA_TEXT` or equivalent rich extras.

`onListenerConnected()` iterates `getActiveNotifications()`, learns channel metadata for every active notification, and creates rebuild records only for entries that pass `READ_RICH_CONTENT`. It then calls one `runtime.rebuild(records)` after the scan. No rebuilt item may interrupt or play the BOOP cue.

- [ ] **Step 4: Initialize runtime only on phone modes**

At the top of `UnifiedApplication.onCreate()` resolve `BoopDeviceProfile.Mode` once. For `WALL` and `LAUNCHER`, call `BoopNotificationRuntime.initialize(this)` and return from the existing Shield-only density block. For `SHIELD`, preserve the current density/crash-handler path and do not initialize notification runtime.

- [ ] **Step 5: Materialize and compile**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:compileDebugJavaWithJavac :app:testDebugUnitTest \
  --tests '*BoopNotification*' --stacktrace
```

Expected: PASS. No manifest service declaration is required for Java compilation yet; Task 10 adds and structurally verifies it before release.

- [ ] **Step 6: Commit**

```bash
git add source/BoopNotificationRuntime.java source/BoopNotificationListenerService.java \
  unified/UnifiedApplication.java source-test/BoopNotificationIntakePolicyTest.java
git commit -m "feat(unified): add notification listener runtime"
```

---

### Task 4: Add first-run Notification Access + overlay onboarding without blocking BOOP

**Files:**
- Create: `source/BoopNotificationPermissionState.java`
- Create: `source/BoopNotificationOnboardingState.java`
- Create: `source/BoopNotificationStartupGate.java`
- Create: `source/BoopNotificationOnboardingActivity.java`
- Create: `source-test/BoopNotificationStartupGateTest.java`
- Modify: `unified/UnifiedEntryActivity.java`

**Interfaces:**
- `boolean BoopNotificationPermissionState.hasListenerAccess(Context)` uses `NotificationManager.isNotificationListenerAccessGranted(ComponentName)`.
- `boolean BoopNotificationPermissionState.hasOverlayAccess(Context)` uses `Settings.canDrawOverlays(context)`.
- `Intent notificationListenerSettingsIntent()` returns `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`.
- `Intent overlaySettingsIntent(Context)` returns `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` with `package:<packageName>`.
- `BoopNotificationOnboardingState.isSeen(Context)` / `markSeen(Context)` uses `boop_notifications:onboarding_seen_v1`.
- `BoopNotificationStartupGate.Target resolve(BoopDeviceProfile.Mode mode, boolean onboardingSeen)` returns `NOTIFICATION_ONBOARDING` only for non-SHIELD + unseen.

- [ ] **Step 1: Write failing startup-gate tests**

```java
@Test public void unseenPhoneGetsNotificationOnboarding() {
    assertEquals(BoopNotificationStartupGate.Target.NOTIFICATION_ONBOARDING,
        BoopNotificationStartupGate.resolve(BoopDeviceProfile.Mode.WALL, false));
    assertEquals(BoopNotificationStartupGate.Target.NOTIFICATION_ONBOARDING,
        BoopNotificationStartupGate.resolve(BoopDeviceProfile.Mode.LAUNCHER, false));
}

@Test public void shieldNeverGetsPhoneNotificationOnboarding() {
    assertEquals(BoopNotificationStartupGate.Target.NORMAL,
        BoopNotificationStartupGate.resolve(BoopDeviceProfile.Mode.SHIELD, false));
}
```

- [ ] **Step 2: Run and verify RED**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotificationStartupGateTest' --stacktrace
```

Expected: FAIL because the startup gate is absent.

- [ ] **Step 3: Implement the gate and non-blocking onboarding Activity**

The Activity has exactly these phases: `INTRO`, `WAITING_LISTENER_SETTINGS`, `WAITING_OVERLAY_SETTINGS`, `DONE`.

- `INTRO` shows plain-English setup with `Set up` and `Not now`.
- `Not now` marks onboarding seen and finishes immediately.
- `Set up` opens Notification Access settings and records `WAITING_LISTENER_SETTINGS`.
- On resume from listener settings, show/open the overlay step regardless of whether listener permission was granted; BOOP records actual grant state rather than assuming success.
- On resume from overlay settings, mark onboarding seen and finish. Core BOOP continues even if either permission remains denied.

Do not enable the notification master toggle or any app/channel during onboarding.

- [ ] **Step 4: Gate `UnifiedEntryActivity` before normal phone routing**

Before ordinary WALL/LAUNCHER routing, resolve `BoopNotificationStartupGate`. If onboarding is required, start `BoopNotificationOnboardingActivity` for result once and resume normal routing when it returns. Keep the existing Shield assistant first-run path unchanged.

- [ ] **Step 5: Run notification tests and compile**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotification*' --tests '*ShieldEntryRouteTest' --tests '*BoopDeviceProfileTest' --stacktrace
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add source/BoopNotificationPermissionState.java source/BoopNotificationOnboardingState.java \
  source/BoopNotificationStartupGate.java source/BoopNotificationOnboardingActivity.java \
  source-test/BoopNotificationStartupGateTest.java unified/UnifiedEntryActivity.java
git commit -m "feat(unified): add notification first-run setup"
```

---

### Task 5: Build Voice Settings -> Notifications app/channel controls

**Files:**
- Create: `source/BoopNotificationAppEntry.java`
- Create: `source/BoopNotificationAppCatalogModel.java`
- Create: `source/BoopNotificationAppCatalog.java`
- Create: `source/BoopNotificationSettingsActivity.java`
- Create: `source-test/BoopNotificationAppCatalogModelTest.java`
- Create: `scripts/patch-unified-notifications.py`
- Modify: `scripts/materialize-unified.sh`

**Interfaces:**
- `List<BoopNotificationAppEntry> BoopNotificationAppCatalogModel.merge(launchable, observed)` dedupes by package and sorts by label, then package.
- `BoopNotificationAppCatalog.load(Context, Collection<BoopNotificationChannelInfo>)` unions `LauncherApps.getActivityList(null, Process.myUserHandle())` with packages seen by the listener.
- `BoopNotificationSettingsActivity` reads/writes `BoopNotificationSettingsStore` only.
- Channel rows come only from observed `BoopNotificationChannelInfo`; do not call listener channel-enumeration/update APIs.
- Android channel settings route uses `Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS`, `Settings.EXTRA_APP_PACKAGE`, `Settings.EXTRA_CHANNEL_ID`.

- [ ] **Step 1: Write failing app-catalog merge tests**

```java
@Test public void mergesLaunchableAndObservedPackagesWithoutDuplicates() {
    List<BoopNotificationAppEntry> result = BoopNotificationAppCatalogModel.merge(
        List.of(new BoopNotificationAppEntry("com.chat", "Chat")),
        List.of(new BoopNotificationAppEntry("com.chat", "Chat"),
                new BoopNotificationAppEntry("com.sync", "Sync Service")));
    assertEquals(2, result.size());
    assertEquals("com.chat", result.get(0).packageName());
    assertEquals("com.sync", result.get(1).packageName());
}
```

- [ ] **Step 2: Run and verify RED**

Use the notification-focused Gradle command from Task 4. Expected: FAIL because catalog types are absent.

- [ ] **Step 3: Implement settings UI behavior**

The Activity must show, in this order:

1. readiness text: `Ready`, `Needs Notification Access`, `Needs Display Permission`, or both missing;
2. master BOOP Notifications switch, default OFF;
3. user-set timeout control, 3-30 seconds, default 8;
4. installed/observed app list with per-app toggle;
5. under an enabled app, only channels BOOP has actually observed;
6. for each observed channel, its Android channel name, BOOP allow toggle, native alert state, and `Open Android channel settings` button.

If an enabled app has no observed channel yet, show exactly: `No notification categories seen yet. When this app sends one, BOOP will learn the category here. It will not interrupt until you enable that category.`

If an enabled channel still has native sound or vibration, show: `Android is still alerting for this category. Make it silent there before BOOP uses his own sound.`

- [ ] **Step 4: Patch the final materialized Wall Voice Settings, not the historical base renderer**

`scripts/patch-unified-notifications.py` inserts one `Notifications` button before the existing Voice `Done` button. Its click handler starts `BoopNotificationSettingsActivity`. The patch must be idempotent by checking a marker comment `// BOOP_NOTIFICATION_SETTINGS_ENTRY_V1` and failing if its structural anchor is missing rather than silently editing the wrong code.

Add this late in `scripts/materialize-unified.sh`, after all current eye/sleep/hue patches, so notification UI does not own those patches.

- [ ] **Step 5: Materialize, inspect only the non-visual integration, then compile**

```bash
bash scripts/materialize-unified.sh
grep -n "BOOP_NOTIFICATION_SETTINGS_ENTRY_V1" boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotification*' --stacktrace
```

Expected: exactly one integration marker and all tests PASS. This grep checks patch application only, not appearance.

- [ ] **Step 6: Commit**

```bash
git add source/BoopNotificationAppEntry.java source/BoopNotificationAppCatalogModel.java \
  source/BoopNotificationAppCatalog.java source/BoopNotificationSettingsActivity.java \
  source-test/BoopNotificationAppCatalogModelTest.java scripts/patch-unified-notifications.py \
  scripts/materialize-unified.sh
git commit -m "feat(unified): add notification controls under Voice"
```

---

### Task 6: Select locked/in-place/overlay surfaces and wire interruption lifecycle

**Files:**
- Create: `source/BoopNotificationSurface.java`
- Create: `source/BoopNotificationSurfaceSelector.java`
- Create: `source/BoopNotificationHost.java`
- Create: `source/BoopNotificationInPlaceController.java`
- Create: `source/BoopNotificationOverlayController.java`
- Create: `source/BoopNotificationLockActivity.java`
- Test: `source-test/BoopNotificationSurfaceSelectorTest.java`
- Modify: `source/BoopNotificationRuntime.java`
- Modify: `scripts/patch-unified-notifications.py`

**Interfaces:**
- `BoopNotificationSurface choose(boolean interactive, boolean keyguardLocked, boolean wallHostVisible)`.
- `!interactive || keyguardLocked -> LOCKED`; else visible Wall host -> `IN_PLACE`; otherwise -> `OVERLAY`.
- `BoopNotificationHost.show(BoopNotificationPresentation presentation, long timeoutMs)`, `update(...)`, `hide()`.
- `BoopNotificationRuntime.registerWallHost(BoopNotificationHost host)` / `unregisterWallHost(host)`.

- [ ] **Step 1: Write failing surface-selection tests**

```java
@Test public void screenOffAlwaysUsesPrivacySafeLockedSurface() {
    assertEquals(BoopNotificationSurface.LOCKED,
        BoopNotificationSurfaceSelector.choose(false, false, false));
}

@Test public void unlockedWallUsesInPlaceSurface() {
    assertEquals(BoopNotificationSurface.IN_PLACE,
        BoopNotificationSurfaceSelector.choose(true, false, true));
}

@Test public void unlockedOtherAppUsesOverlay() {
    assertEquals(BoopNotificationSurface.OVERLAY,
        BoopNotificationSurfaceSelector.choose(true, false, false));
}
```

- [ ] **Step 2: Run and verify RED**

Run notification-focused Gradle tests. Expected: FAIL because surface types are absent.

- [ ] **Step 3: Implement platform selection and controllers**

Runtime reads `PowerManager.isInteractive()` and `KeyguardManager.isKeyguardLocked()` only when it needs to present. `BoopNotificationOverlayController` creates a full-screen, focusable `TYPE_APPLICATION_OVERLAY` only when `Settings.canDrawOverlays(context)` is true. If overlay access is missing, it reports failure to runtime and leaves the Android notification untouched.

`BoopNotificationLockActivity` calls `setShowWhenLocked(true)` and `setTurnScreenOn(true)` in `onCreate`, uses `FLAG_KEEP_SCREEN_ON` only while the BOOP presentation is active, and finishes on BOOP timeout/swipe. It never calls keyguard-bypass APIs.

Register an `ACTION_USER_PRESENT` receiver while the lock Activity is alive. On unlock, call `runtime.transitionAfterUnlock()` so the same active bundle is re-rendered as rich unlocked content without replaying the cue, then finish the lock Activity.

- [ ] **Step 4: Extend the late MainActivity patch with the in-place host**

Patch the materialized Wall Activity to create `BoopNotificationInPlaceController(interactionSurface)`, register it with runtime in `onResume`, and unregister it in `onPause`. Add marker `// BOOP_NOTIFICATION_IN_PLACE_HOST_V1`. Do not alter face geometry, blink, wake microphone ownership or sleep code in this patch.

- [ ] **Step 5: Materialize and test**

```bash
bash scripts/materialize-unified.sh
grep -n "BOOP_NOTIFICATION_IN_PLACE_HOST_V1" boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotification*' --tests '*BoopPresenceStateTest' --stacktrace
```

Expected: one host marker and PASS.

- [ ] **Step 6: Commit**

```bash
git add source/BoopNotificationSurface.java source/BoopNotificationSurfaceSelector.java \
  source/BoopNotificationHost.java source/BoopNotificationInPlaceController.java \
  source/BoopNotificationOverlayController.java source/BoopNotificationLockActivity.java \
  source/BoopNotificationRuntime.java source-test/BoopNotificationSurfaceSelectorTest.java \
  scripts/patch-unified-notifications.py
git commit -m "feat(unified): add notification presentation surfaces"
```

---

### Task 7: Preserve original notification tap semantics and add BOOP inbox

**Files:**
- Create: `source/BoopNotificationTapPolicy.java`
- Create: `source/BoopNotificationTapLauncher.java`
- Create: `source/BoopNotificationInboxActivity.java`
- Test: `source-test/BoopNotificationTapPolicyTest.java`
- Modify: `source/BoopNotificationRuntime.java`

**Interfaces:**
- `boolean BoopNotificationTapPolicy.shouldCancelAfterSuccessfulSend(boolean autoCancel, boolean sendSucceeded)`.
- `int BoopNotificationTapPolicy.backgroundStartModeForSdk(int sdk)` returns API-appropriate PendingIntent BAL opt-in: API 36+ `MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE`; API 34-35 `MODE_BACKGROUND_ACTIVITY_START_ALLOWED`; older versions no ActivityOptions override.
- `BoopNotificationTapLauncher.Result send(Context context, PendingIntent intent, int sdk)` returns `OPENED` or `CANCELLED`.
- Inbox reads current runtime active notifications; no titles/bodies are serialized into Intent extras or persisted.

- [ ] **Step 1: Write failing tap-policy tests**

```java
@Test public void autoCancelHappensOnlyAfterSuccessfulOpen() {
    assertTrue(BoopNotificationTapPolicy.shouldCancelAfterSuccessfulSend(true, true));
    assertFalse(BoopNotificationTapPolicy.shouldCancelAfterSuccessfulSend(true, false));
    assertFalse(BoopNotificationTapPolicy.shouldCancelAfterSuccessfulSend(false, true));
}

@Test public void android36UsesVisibleOnlyBackgroundStartMode() {
    assertEquals(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE,
        BoopNotificationTapPolicy.backgroundStartModeForSdk(36));
}
```

- [ ] **Step 2: Run and verify RED**

Expected: FAIL because tap types are absent.

- [ ] **Step 3: Implement PendingIntent sending**

For API 36+, build `ActivityOptions` and call `setPendingIntentBackgroundActivityStartMode(MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE)`. For API 34-35 use `MODE_BACKGROUND_ACTIVITY_START_ALLOWED`. Send with the options `Bundle`. Catch `PendingIntent.CanceledException` and return `CANCELLED`.

On `OPENED`, if envelope `autoCancel` is true, runtime asks the attached listener to `cancelNotification(key)`. On `CANCELLED`, leave the Android notification and BOOP card untouched and show `Can't open that right now.`

Swipe/timeout paths must never call listener cancellation.

- [ ] **Step 4: Implement BOOP inbox**

If the visible presentation contains more than one item, tapping the bundle opens `BoopNotificationInboxActivity`. The Activity reads `runtime.activeNotifications()` at render time, groups cards by app, and sends an individual card through the same `BoopNotificationTapLauncher`. If runtime is empty, finish cleanly.

- [ ] **Step 5: Run tests and compile**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotification*' --stacktrace
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add source/BoopNotificationTapPolicy.java source/BoopNotificationTapLauncher.java \
  source/BoopNotificationInboxActivity.java source/BoopNotificationRuntime.java \
  source-test/BoopNotificationTapPolicyTest.java
git commit -m "feat(unified): preserve notification open semantics"
```

---

### Task 8: Materialize the exact BOOP hands and build one reusable runtime puppet view

**Files:**
- Create: `unified/assets/boop-notifications/README.md`
- Create binary by exact transfer: `unified/assets/boop-notifications/boop-yellow-hands-approved.png`
- Create: `scripts/materialize-boop-notification-assets.py`
- Create: `tests/test_unified_notification_asset_integrity.py`
- Create: `source/BoopNotificationSwipeGesture.java`
- Create: `source/BoopNotificationPuppetView.java`
- Test: `source-test/BoopNotificationSwipeGestureTest.java`
- Modify: `source/BoopNotificationInPlaceController.java`
- Modify: `source/BoopNotificationOverlayController.java`
- Modify: `source/BoopNotificationLockActivity.java`
- Modify: `source/BoopNotificationInboxActivity.java`
- Modify: `scripts/materialize-unified.sh`

**Interfaces:**
- `BoopNotificationPuppetView.Callback.onOpen(String key)`, `onOpenBundle()`, `onDismiss()`.
- `BoopNotificationSwipeGesture.isDismiss(float downX, float downY, float upX, float upY, float density)` requires at least 72dp travel and dominant-axis travel greater than 1.25x the perpendicular axis.
- Runtime layers are current `BoopFaceView`, exact paired-hand PNG, app/icon/card layer and count badge.

- [ ] **Step 1: Transfer and hash the exact notification-hand binary**

```bash
mkdir -p unified/assets/boop-notifications
git show origin/animation-freddie-mercury:boop-yellow-hands-approved.png \
  > unified/assets/boop-notifications/boop-yellow-hands-approved.png
sha256sum unified/assets/boop-notifications/boop-yellow-hands-approved.png
```

Expected SHA-256 exactly:

```text
26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1
```

`README.md` records that this is the lock-screen notification hand source and explicitly says it does not replace the older general hand-master history.

- [ ] **Step 2: Write asset-integrity and swipe tests before view wiring**

Python integrity test:

```python
from hashlib import sha256
from pathlib import Path

def test_notification_hands_are_exact_locked_binary():
    p = Path('unified/assets/boop-notifications/boop-yellow-hands-approved.png')
    assert sha256(p.read_bytes()).hexdigest() == '26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1'
```

JUnit swipe test:

```java
@Test public void deliberateSwipeDismissesButTapDoesNot() {
    assertTrue(BoopNotificationSwipeGesture.isDismiss(10f, 10f, 190f, 20f, 2f));
    assertFalse(BoopNotificationSwipeGesture.isDismiss(10f, 10f, 20f, 18f, 2f));
}
```

- [ ] **Step 3: Verify RED for the missing swipe helper and PASS for the exact binary**

```bash
python -m pytest -q tests/test_unified_notification_asset_integrity.py
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotificationSwipeGestureTest' --stacktrace
```

Expected: asset test PASS; JUnit FAIL until the helper exists.

- [ ] **Step 4: Implement asset materialization without modifying pixels**

`scripts/materialize-boop-notification-assets.py` verifies the hash above and byte-copies the PNG to:

`boop-build/BOOP-Alpha1/app/src/main/res/drawable-nodpi/boop_notification_hands.png`

It must not crop, threshold, recolor, recompress or regenerate the file. Run this script late in `materialize-unified.sh` after the app resource tree exists.

- [ ] **Step 5: Implement `BoopNotificationPuppetView`**

Use a black `FrameLayout` with:

- current materialized `BoopFaceView` as the eye layer so saved iris hue and current accepted renderer apply automatically;
- one `ImageView` for the exact paired hands;
- one centered notification card containing app icon and count; add title/text only when `presentation.locked() == false`;
- card entrance: alpha 0->1 and `translationY(-16dp)->0` with `OvershootInterpolator(0.7f)` over 260ms;
- hands entrance: scale 0.96->1 over 220ms;
- existing BOOP face wake/blink language only; do not generate a new eye pose or alter the face bitmap.

Tap on a single card calls `onOpen(key)`, bundle tap calls `onOpenBundle()`, qualifying swipe calls `onDismiss()`.

- [ ] **Step 6: Wire the same view into all three presentation surfaces**

The in-place controller, overlay controller and lock Activity must all construct the same `BoopNotificationPuppetView`; surface wrappers own only window/activity lifecycle and timeout.

- [ ] **Step 7: Run focused tests and compile**

```bash
python -m pytest -q tests/test_unified_notification_asset_integrity.py
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotification*' --stacktrace
```

Expected: PASS. No automated visual claim.

- [ ] **Step 8: Commit**

```bash
git add unified/assets/boop-notifications scripts/materialize-boop-notification-assets.py \
  tests/test_unified_notification_asset_integrity.py source/BoopNotificationSwipeGesture.java \
  source/BoopNotificationPuppetView.java source/BoopNotificationInPlaceController.java \
  source/BoopNotificationOverlayController.java source/BoopNotificationLockActivity.java \
  source/BoopNotificationInboxActivity.java source-test/BoopNotificationSwipeGestureTest.java \
  scripts/materialize-unified.sh
git commit -m "feat(unified): add exact notification puppet layers"
```

---

### Task 9: Add BOOP's local replacement cue and vibration without double-alerting

**Files:**
- Create: `source/BoopNotificationCuePolicy.java`
- Create: `source/BoopNotificationCueRenderer.java`
- Create: `source/BoopNotificationCue.java`
- Test: `source-test/BoopNotificationCuePolicyTest.java`
- Test: `source-test/BoopNotificationCueRendererTest.java`
- Modify: `source/BoopNotificationRuntime.java`

**Interfaces:**
- `boolean BoopNotificationCuePolicy.shouldPlay(boolean coordinatorRequestsCue, BoopNotificationChannelInfo channel)`.
- True only when coordinator requests a new cue and `channel.nativeEffectsSilent()` is true.
- `short[] BoopNotificationCueRenderer.render(int sampleRate)` returns deterministic 320ms mono PCM16.
- `void BoopNotificationCue.play()` uses `AudioAttributes.USAGE_NOTIFICATION` and one short vibration waveform.

- [ ] **Step 1: Write failing cue-policy/renderer tests**

```java
@Test public void neverDoubleAlertsWithNativeEffectsStillOn() {
    BoopNotificationChannelInfo noisy = new BoopNotificationChannelInfo(
        "com.chat", "messages", "Messages", true, true, false, 1L);
    BoopNotificationChannelInfo silent = new BoopNotificationChannelInfo(
        "com.chat", "messages", "Messages", true, false, false, 1L);
    assertFalse(BoopNotificationCuePolicy.shouldPlay(true, noisy));
    assertTrue(BoopNotificationCuePolicy.shouldPlay(true, silent));
    assertFalse(BoopNotificationCuePolicy.shouldPlay(false, silent));
}

@Test public void goofyCueIsShortDeterministicAndBounded() {
    short[] a = BoopNotificationCueRenderer.render(44100);
    short[] b = BoopNotificationCueRenderer.render(44100);
    assertEquals(14112, a.length);
    assertArrayEquals(a, b);
    int peak = 0;
    for (short sample : a) peak = Math.max(peak, Math.abs((int) sample));
    assertTrue(peak > 2000);
    assertTrue(peak <= 32767);
}
```

- [ ] **Step 2: Run and verify RED**

Run notification-focused Gradle tests. Expected: FAIL because cue classes are absent.

- [ ] **Step 3: Implement the deterministic local sound**

Render 320ms at the requested sample rate as three envelope-controlled components, summed and clamped to +/-0.72 full scale:

```java
double knock1 = t < 0.050 ? Math.sin(2.0 * Math.PI * 190.0 * t) * Math.exp(-55.0 * t) * 0.42 : 0.0;
double u2 = t - 0.078;
double knock2 = u2 >= 0.0 && u2 < 0.050 ? Math.sin(2.0 * Math.PI * 285.0 * u2) * Math.exp(-52.0 * u2) * 0.34 : 0.0;
double uc = t - 0.138;
double chirp = 0.0;
if (uc >= 0.0 && uc < 0.170) {
    double phase = 2.0 * Math.PI * (520.0 * uc + 0.5 * 1650.0 * uc * uc);
    double envelope = Math.sin(Math.PI * uc / 0.170);
    chirp = Math.sin(phase) * envelope * 0.24;
}
```

This keeps the daft cue generated locally in code, tiny and network-independent.

`BoopNotificationCue.play()` uses mono PCM16 `AudioTrack` with `AudioAttributes.USAGE_NOTIFICATION` and vibrates `new long[]{0, 35, 55, 28}` once. Do not loop.

- [ ] **Step 4: Wire cue policy into runtime**

Runtime evaluates cue policy only on coordinator decisions with `playCue=true`. Updates absorbed into the visible bundle never replay sound/vibration. Unlock transition never replays it.

- [ ] **Step 5: Run tests and compile**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotification*' --stacktrace
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add source/BoopNotificationCuePolicy.java source/BoopNotificationCueRenderer.java \
  source/BoopNotificationCue.java source/BoopNotificationRuntime.java \
  source-test/BoopNotificationCuePolicyTest.java source-test/BoopNotificationCueRendererTest.java
git commit -m "feat(unified): add BOOP notification cue"
```

---

### Task 10: Declare supported Android capabilities and add structural/security CI contracts

**Files:**
- Modify: `source/AndroidManifest.xml`
- Create: `tests/test_unified_notification_manifest_contract.py`
- Modify: `.github/workflows/build-boop-unified.yml`

**Interfaces/manifest contract:**
- Uses permissions: `android.permission.SYSTEM_ALERT_WINDOW`, `android.permission.VIBRATE`.
- Listener service: `.BoopNotificationListenerService`, `android:exported="false"`, `android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"`, action `android.service.notification.NotificationListenerService`.
- Activities `.BoopNotificationOnboardingActivity`, `.BoopNotificationSettingsActivity`, `.BoopNotificationLockActivity`, `.BoopNotificationInboxActivity` are `android:exported="false"`.
- No `USE_FULL_SCREEN_INTENT`, device-admin receiver, accessibility service declaration or `QUERY_ALL_PACKAGES`.

- [ ] **Step 1: Write the failing manifest contract first**

```python
from pathlib import Path
import xml.etree.ElementTree as ET

ANDROID = '{http://schemas.android.com/apk/res/android}'

def test_notification_components_and_permissions_are_exact():
    root = ET.parse('boop-build/BOOP-Alpha1/app/src/main/AndroidManifest.xml').getroot()
    permissions = {n.get(ANDROID + 'name') for n in root.findall('uses-permission')}
    assert 'android.permission.SYSTEM_ALERT_WINDOW' in permissions
    assert 'android.permission.VIBRATE' in permissions
    assert 'android.permission.USE_FULL_SCREEN_INTENT' not in permissions
    assert 'android.permission.QUERY_ALL_PACKAGES' not in permissions
    app = root.find('application')
    listener = next(n for n in app.findall('service')
                    if n.get(ANDROID + 'name') == '.BoopNotificationListenerService')
    assert listener.get(ANDROID + 'permission') == 'android.permission.BIND_NOTIFICATION_LISTENER_SERVICE'
    assert listener.get(ANDROID + 'exported') == 'false'
```

Extend the same test to assert all four Activities are non-exported and no service/receiver declares `android.accessibilityservice.AccessibilityService` or device-admin metadata.

- [ ] **Step 2: Materialize and verify RED**

```bash
bash scripts/materialize-unified.sh
python -m pytest -q tests/test_unified_notification_manifest_contract.py
```

Expected: FAIL because manifest declarations are not present yet.

- [ ] **Step 3: Add exact manifest declarations**

Add the two uses-permissions and the listener/Activities above. Keep the existing package, launcher, auth callback, assistant and unified manifest behavior intact.

- [ ] **Step 4: Add notification-focused tests to canonical CI**

In the existing non-visual test phase add:

```bash
python -m pytest -q tests/test_unified_notification_asset_integrity.py
```

After materialization add:

```bash
python -m pytest -q tests/test_unified_notification_manifest_contract.py
```

Add `--tests '*BoopNotification*'` to the existing focused unified Gradle test step. Do not add emulator install, screenshots, visual comparison or audio-quality assertions.

- [ ] **Step 5: Run all local non-visual contracts**

```bash
python -m pytest -q tests/test_unified_docked_shield_pass.py tests/test_approved_eye_master_contract.py \
  tests/test_unified_notification_asset_integrity.py
bash scripts/materialize-unified.sh
python -m pytest -q tests/test_unified_wake_handoff_contract.py tests/test_unified_notification_manifest_contract.py
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotification*' --tests '*BoopWake*' --tests '*BoopDeviceProfileTest' \
  --tests '*ShieldEntryRouteTest' --tests '*BoopAssistant*' --stacktrace
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add source/AndroidManifest.xml tests/test_unified_notification_manifest_contract.py \
  .github/workflows/build-boop-unified.yml
git commit -m "test(unified): lock notification platform contracts"
```

---

### Task 11: Version, sign, document and hand the candidate to Ryan for physical acceptance

**Files:**
- Modify: `unified/app-build.gradle`
- Modify after successful workflow: `SESSION_HANDOFF.md`
- Modify after successful workflow: `BOOP_STATUS.md`
- Modify after successful workflow: `BOOP_UNIFIED_MEMORY.md`

**Interfaces/release rule:**
- Read the current canonical `versionCode` at execution time and increment by exactly 1. Do not assume v63 because the eye/sleep reconciliation may have consumed intervening versions.
- Preserve application ID `com.boop.alpha1` and permanent signer.

- [ ] **Step 1: Re-fetch before final version/release commit**

```bash
git fetch origin main boop-unified
git rev-parse HEAD
git rev-parse origin/boop-unified
git rev-parse origin/main
```

If `origin/boop-unified` advanced beyond this task's reviewed history, reconcile normally. Never overwrite/force.

- [ ] **Step 2: Read and bump the live version exactly once**

```bash
grep -n "versionCode\|versionName" unified/app-build.gradle
```

Increment `versionCode` by 1 from the value shown. Set a descriptive `versionName` ending in `-unified-notification-presenter` while preserving the established numeric lineage prefix.

- [ ] **Step 3: Run the complete canonical non-visual verification locally**

```bash
python -m pytest -q tests/test_unified_docked_shield_pass.py tests/test_approved_eye_master_contract.py \
  tests/test_unified_notification_asset_integrity.py
bash scripts/materialize-unified.sh
python -m pytest -q tests/test_unified_wake_handoff_contract.py tests/test_unified_notification_manifest_contract.py
gradle --no-daemon -p launcher lintDebug --stacktrace
gradle --no-daemon -p shield-overlay :app:testDebugUnitTest --stacktrace
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:assembleDebug --stacktrace
```

Expected: zero test failures; compile/build success. This is still not visual/device/acoustic acceptance.

- [ ] **Step 4: Commit the reviewed release source**

```bash
git add unified/app-build.gradle
git commit -m "release(unified): notification presenter candidate"
git push origin HEAD:boop-unified
```

- [ ] **Step 5: Verify GitHub Actions and permanent signing**

Wait for canonical `Build BOOP Unified APK` on the exact pushed source commit. Record workflow run ID, artifact ID, APK SHA-256 and permanent signer SHA-256 from the run. Require SUCCESS before offering the APK.

- [ ] **Step 6: Update handoff/status/memory with exact evidence only**

Record:

- exact built commit;
- version code/name;
- workflow run + artifact ID;
- APK SHA-256 and signer SHA-256;
- notification functional test counts;
- explicit `CI/signer green; physical visual/lock-screen/acoustic acceptance pending`;
- this physical checklist: first-run permission flow, decline paths, first allowed app/channel, Android channel-silencing guide, screen-off wake, locked redaction, unlock transition, full-screen interruption over another app, in-place Wall presentation, single tap/open, auto-cancel where applicable, swipe preserving shade notification, same-app bomb bundle, mixed-app bundle/inbox, 8s/default and custom timeout, process/reboot rebuild, permission-revocation repair, replacement cue/vibration.

- [ ] **Step 7: Commit documentation-only evidence and verify live head**

```bash
git add SESSION_HANDOFF.md BOOP_STATUS.md BOOP_UNIFIED_MEMORY.md
git commit -m "docs(unified): record notification presenter candidate"
git push origin HEAD:boop-unified
git fetch origin boop-unified
git rev-parse HEAD
git rev-parse origin/boop-unified
```

Expected: local and remote heads match. Do not create a physical rollback checkpoint until Ryan explicitly says this exact signed candidate works on the real Pixel.

---

## Self-review checklist for the executor

Before implementation is called complete, confirm every item below against the approved spec:

- default deny requires master + app + observed channel;
- denied notification rich content is not read/persisted by BOOP;
- listener learns channels through `Ranking.getChannel()` and never tries privileged third-party channel enumeration/update;
- first-run permission setup is non-blocking and Shield-excluded;
- all controls live under Voice Settings -> Notifications;
- locked content is redacted; screen-off selects privacy-safe locked surface;
- overlay interruption is full-screen/focusable only with explicit overlay grant;
- swipe/timeout cannot cancel Android notifications;
- tap uses original `PendingIntent`; auto-cancel only follows successful send;
- active burst absorbs later notifications without repeated cue;
- BOOP inbox is reconstructed from Android-active transient state, not a message database;
- exact notification hands and existing face renderer are runtime layers;
- replacement cue is one local short cue and only plays when native channel sound+vibration are both off;
- no root/device-admin/accessibility/full-screen-intent abuse/QUERY_ALL_PACKAGES/new foreground service;
- CI remains non-visual; real-device appearance and sound remain Ryan's acceptance gate.
