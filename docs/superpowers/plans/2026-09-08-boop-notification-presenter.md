# BOOP Notification Presenter Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build BOOP's opt-in phone-wide notification presenter so selected app/channel notifications can wake the display, interrupt with BOOP, group bursts, open the original target, and leave Android's real notification state intact except for normal successful-tap auto-cancel semantics.

**Architecture:** Keep notification policy as pure Java domain logic in `source/`, with Android adapters for Notification Listener intake, onboarding/settings, lock-screen Activity, overlay, inbox, sound/vibration and the existing Wall. The listener learns third-party channels from `NotificationListenerService.Ranking.getChannel()` and reads rich notification content only after the user's master + package + channel allowlist passes. Presentation uses the current reconciled BOOP face renderer plus the exact approved notification-hand binary as separate runtime layers, never a newly generated or baked mascot.

**Tech Stack:** Java 17, Android SDK 36, minSdk 29, framework Views, `NotificationListenerService`, `WindowManager.TYPE_APPLICATION_OVERLAY`, `Activity.setShowWhenLocked`, `Activity.setTurnScreenOn`, `PendingIntent`, `LauncherApps`, `SharedPreferences`, `AudioTrack`, `VibrationEffect`, JUnit 4, Python 3.12 structural/integrity tests, Gradle 9.6 and the existing permanent BOOP signing workflow.

**Spec:** `docs/superpowers/specs/2026-09-08-boop-notification-presenter-design.md`

## Global Constraints

- Canonical app branch is `boop-unified`; package remains `com.boop.alpha1`; permanent signer remains unchanged.
- Do not begin notification production-code commits until the current procedural-eye/sleep/hue work is reconciled into canonical `boop-unified` and Ryan physically accepts that reconciled state.
- Phone modes only. `BoopDeviceProfile.Mode.SHIELD` must not initialize, onboard, configure or present phone notifications.
- Default deny is absolute: `masterEnabled && appEnabled && channelEnabled` is required before BOOP may read rich content or present a notification.
- Timeout defaults to 8,000 ms and is user-settable from 3,000 to 30,000 ms.
- Burst window is 4,000 ms. A second notification within that window turns the visible single notification into a bundle. Once a bundle exists, additional allowed notifications join it without replaying the cue until that presentation is dismissed/times out.
- Locked or screen-off UI exposes app identity/icon and aggregate count only. No sender, title, body, account detail, contact photo or preview while locked.
- Swipe or timeout never cancels the Android notification.
- Successful tap sends the original `PendingIntent`; `FLAG_AUTO_CANCEL` is mirrored only after a successful send.
- Persist package/channel choices, timeout, onboarding state and minimal channel/dedupe metadata only. Never persist notification title/body/sender/contact/message history and never upload notification content.
- Do not call `NotificationListenerService.getNotificationChannels(pkg,user)` or `updateNotificationChannel(pkg,user,channel)` for arbitrary third-party apps. BOOP is an ordinary notification listener, not Notification Assistant/device-association authority.
- Learn channel/category inventory from `Ranking.getChannel()` on active/new notifications. An unseen channel remains ineligible until BOOP observes it and the user explicitly enables it.
- Open Android channel settings with `Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS`. BOOP never silently changes another app's channel sound/vibration.
- Play BOOP's own cue/vibration only when the selected observed channel is known to have native sound and vibration both disabled. If not, visual BOOP presentation may occur but his replacement cue is suppressed to avoid double alerts.
- First-run setup asks for Notification Access and Display over other apps. Declining either remains non-blocking. It never silently enables the master switch or an app/channel.
- Do not add root, device admin, accessibility automation, secure-setting writes, alarm/call full-screen-intent abuse, keyguard bypass, `QUERY_ALL_PACKAGES`, or a new foreground service for this feature.
- Preserve the approved eyes, saved iris hue, blink, current reconciled procedural-eye/sleep renderer, wake architecture, HA behavior, Launcher and Shield behavior.
- Notification hands must be exact bytes from `animation-freddie-mercury:boop-yellow-hands-approved.png`, SHA-256 `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`, stored under a notification-specific asset path. Do not overwrite the older general hand-master history/hash.
- Automated checks are functional, structural and asset-identity only. No screenshot/golden-image/emulator appearance/audio-quality acceptance in CI.
- Ryan owns real-device visual, lock-screen, interruption and acoustic acceptance.

---

## Hard Gate 0: Reconcile eye/sleep work before notification implementation

This is a preflight gate, not a production-code task.

- [ ] **Step 1: Fetch live heads without disturbing another task**

```bash
git fetch origin main boop-unified boop-unified-v63-fast-eyes animation-freddie-mercury --tags
git rev-parse origin/main
git rev-parse origin/boop-unified
git rev-parse origin/boop-unified-v63-fast-eyes
```

- [ ] **Step 2: Confirm canonical docs explicitly record Ryan's accepted reconciled eye/sleep/hue state**

```bash
sed -n '1,240p' SESSION_HANDOFF.md
sed -n '1,240p' BOOP_STATUS.md
sed -n '1,280p' BOOP_UNIFIED_MEMORY.md
```

If sleep/hue remains described as experimental, WIP, pending or physically unaccepted, stop here. Finish that track, promote only the accepted result to canonical, update its handoff/status/memory, then resume this plan from the new live `boop-unified` head.

- [ ] **Step 3: Verify exact visual sources**

```bash
sha256sum unified/assets/boop-eyes/boopApprovedEyes.png
git show origin/animation-freddie-mercury:boop-yellow-hands-approved.png | sha256sum
```

Expected:

```text
ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22  unified/assets/boop-eyes/boopApprovedEyes.png
26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1  -
```

- [ ] **Step 4: Use `superpowers:using-git-worktrees` to create an isolated execution worktree from the accepted live canonical head**

Never reset, switch or reuse another running task's checkout.

---

## File Map

**Create, domain/config:**
- `source/BoopNotificationChannelInfo.java`
- `source/BoopNotificationSettingsState.java`
- `source/BoopNotificationSettingsCodec.java`
- `source/BoopNotificationSettingsStore.java`
- `source/BoopNotificationPolicy.java`
- `source/BoopNotificationIntakePolicy.java`
- `source/BoopNotificationSurface.java`
- `source/BoopNotificationEnvelope.java`
- `source/BoopNotificationPresentation.java`
- `source/BoopNotificationCoordinator.java`

**Create, Android runtime/UI:**
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

**Create, assets/materialization/tests:**
- `unified/assets/boop-notifications/README.md`
- `unified/assets/boop-notifications/boop-yellow-hands-approved.png`
- `scripts/materialize-boop-notification-assets.py`
- `scripts/patch-unified-notifications.py`
- `tests/test_unified_notification_manifest_contract.py`
- `tests/test_unified_notification_asset_integrity.py`
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
- `unified/app-build.gradle` only at release-candidate version bump.
- `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md` after an exact signed candidate exists.

---

### Task 1: Default-deny settings, channel metadata and privacy intake gate

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
- `BoopNotificationChannelInfo(String packageName, String channelId, String channelName, boolean effectsKnown, boolean nativeSoundEnabled, boolean nativeVibrationEnabled, long lastSeenMs)`.
- `boolean nativeEffectsSilent()` returns `effectsKnown && !nativeSoundEnabled && !nativeVibrationEnabled`.
- `String encode()` / `static BoopNotificationChannelInfo decode(String encoded)` use URL-safe Base64 without padding for arbitrary package/channel/name text.
- `BoopNotificationSettingsState(boolean masterEnabled, long timeoutMs, Set<String> enabledApps, Set<String> enabledChannelKeys)`.
- `static BoopNotificationSettingsState defaults()` is master OFF, timeout 8,000 ms, empty sets.
- `boolean isAppEnabled(String packageName)` / `boolean isChannelEnabled(String packageName, String channelId)`.
- `String BoopNotificationSettingsCodec.channelKey(String packageName, String channelId)`.
- `boolean BoopNotificationPolicy.allows(BoopNotificationSettingsState state, String packageName, String channelId)`.
- `BoopNotificationIntakePolicy.Mode decide(BoopNotificationSettingsState state, String packageName, String channelId)` returns `OBSERVE_CHANNEL_ONLY` or `READ_RICH_CONTENT`.

- [ ] **Step 1: Write failing tests**

```java
@Test public void defaultStateDeniesEverything() {
    BoopNotificationSettingsState state = BoopNotificationSettingsState.defaults();
    assertFalse(BoopNotificationPolicy.allows(state, "com.chat.app", "messages"));
    assertEquals(8000L, state.timeoutMs());
}

@Test public void masterAppAndChannelAreAllRequired() {
    Set<String> apps = Set.of("com.chat.app");
    Set<String> channels = Set.of(BoopNotificationSettingsCodec.channelKey("com.chat.app", "messages"));
    assertFalse(BoopNotificationPolicy.allows(
        new BoopNotificationSettingsState(false, 8000L, apps, channels), "com.chat.app", "messages"));
    assertTrue(BoopNotificationPolicy.allows(
        new BoopNotificationSettingsState(true, 8000L, apps, channels), "com.chat.app", "messages"));
}

@Test public void deniedChannelCannotReadRichContent() {
    assertEquals(BoopNotificationIntakePolicy.Mode.OBSERVE_CHANNEL_ONLY,
        BoopNotificationIntakePolicy.decide(BoopNotificationSettingsState.defaults(),
            "com.chat.app", "messages"));
}

@Test public void channelMetadataRoundTripsUnicodeAndSeparators() {
    BoopNotificationChannelInfo original = new BoopNotificationChannelInfo(
        "com.example|odd", "messages/a|b", "Messages • 家族", true, false, false, 1234L);
    assertEquals(original, BoopNotificationChannelInfo.decode(original.encode()));
}
```

- [ ] **Step 2: Run RED**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotificationSettingsCodecTest' \
  --tests '*BoopNotificationPolicyTest' \
  --tests '*BoopNotificationIntakePolicyTest' --stacktrace
```

Expected: compile/test failure because the types do not exist.

- [ ] **Step 3: Implement the minimal settings model/store**

Use constants:

```java
static final long DEFAULT_TIMEOUT_MS = 8_000L;
static final long MIN_TIMEOUT_MS = 3_000L;
static final long MAX_TIMEOUT_MS = 30_000L;
```

Policy is exactly:

```java
return state != null
        && state.masterEnabled()
        && state.isAppEnabled(packageName)
        && state.isChannelEnabled(packageName, channelId);
```

`BoopNotificationSettingsStore` uses preferences file `boop_notifications` and only these data keys: `master_enabled`, `timeout_ms`, `enabled_apps`, `enabled_channels`, `observed_channels`. `observed_channels` contains encoded `BoopNotificationChannelInfo` only.

- [ ] **Step 4: Run GREEN**

Repeat Step 2. Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add source/BoopNotificationChannelInfo.java source/BoopNotificationSettingsState.java \
  source/BoopNotificationSettingsCodec.java source/BoopNotificationSettingsStore.java \
  source/BoopNotificationPolicy.java source/BoopNotificationIntakePolicy.java \
  source-test/BoopNotificationSettingsCodecTest.java source-test/BoopNotificationPolicyTest.java \
  source-test/BoopNotificationIntakePolicyTest.java
git commit -m "feat(unified): add notification opt-in policy"
```

---

### Task 2: Transient presentation, lock redaction, dedupe and burst bundling

**Files:**
- Create: `source/BoopNotificationSurface.java`
- Create: `source/BoopNotificationEnvelope.java`
- Create: `source/BoopNotificationPresentation.java`
- Create: `source/BoopNotificationCoordinator.java`
- Test: `source-test/BoopNotificationPresentationTest.java`
- Test: `source-test/BoopNotificationCoordinatorTest.java`

**Interfaces:**
- `enum BoopNotificationSurface { LOCKED, IN_PLACE, OVERLAY }`.
- `BoopNotificationEnvelope` fields: `key`, `packageName`, `appLabel`, `channelId`, `channelName`, `title`, `text`, `postTimeMs`, `autoCancel`.
- `BoopNotificationPresentation.from(List<BoopNotificationEnvelope> bundle, BoopNotificationSurface surface, boolean locked)`.
- Locked cards retain `key`, `packageName`, `appLabel`, but set title/text to `null`.
- `BoopNotificationCoordinator(long burstWindowMs)` production value 4,000 ms.
- `Decision onPosted(BoopNotificationEnvelope envelope, long nowMs, BoopNotificationSettingsState settings)`.
- `void rebuild(Collection<BoopNotificationEnvelope> active, BoopNotificationSettingsState settings)` populates active inbox only, never visible presentation/cue.
- `void onRemoved(String key)`; `void onPresentationDismissed()`.
- `List<BoopNotificationEnvelope> activeNotifications()` / `visibleBundle()` immutable snapshots.
- `Decision.kind()` is `IGNORE`, `PRESENT`, or `UPDATE`; `Decision.playCue()` is true only for a newly started single presentation.

- [ ] **Step 1: Write failing tests**

```java
@Test public void lockedPresentationRemovesRichText() {
    BoopNotificationEnvelope n = fixture("k1", "com.chat", "messages", "Alice", "Dinner?");
    BoopNotificationPresentation p = BoopNotificationPresentation.from(
        List.of(n), BoopNotificationSurface.LOCKED, true);
    assertNull(p.cards().get(0).title());
    assertNull(p.cards().get(0).text());
}

@Test public void secondWithinFourSecondsCreatesBundleWithoutSecondCue() {
    BoopNotificationCoordinator c = new BoopNotificationCoordinator(4000L);
    BoopNotificationSettingsState s = allowed("com.chat", "messages");
    assertTrue(c.onPosted(fixture("k1", "com.chat", "messages", "A", "1"), 1000L, s).playCue());
    BoopNotificationCoordinator.Decision second =
        c.onPosted(fixture("k2", "com.chat", "messages", "B", "2"), 2200L, s);
    assertEquals(BoopNotificationCoordinator.Kind.UPDATE, second.kind());
    assertFalse(second.playCue());
    assertEquals(2, second.bundle().size());
}

@Test public void separateLateSecondNotificationStartsFreshSingle() {
    BoopNotificationCoordinator c = new BoopNotificationCoordinator(4000L);
    BoopNotificationSettingsState s = allowed("com.chat", "messages");
    c.onPosted(fixture("k1", "com.chat", "messages", "A", "1"), 1000L, s);
    BoopNotificationCoordinator.Decision second =
        c.onPosted(fixture("k2", "com.chat", "messages", "B", "2"), 6500L, s);
    assertEquals(BoopNotificationCoordinator.Kind.PRESENT, second.kind());
    assertTrue(second.playCue());
    assertEquals(1, second.bundle().size());
}

@Test public void rebuildNeverInterrupts() {
    BoopNotificationCoordinator c = new BoopNotificationCoordinator(4000L);
    BoopNotificationSettingsState s = allowed("com.chat", "messages");
    c.rebuild(List.of(fixture("k1", "com.chat", "messages", "A", "1")), s);
    assertEquals(1, c.activeNotifications().size());
    assertTrue(c.visibleBundle().isEmpty());
}
```

Also test mixed-app bundling inside the window, same-key updates without cue, removal, dismissal and denied-channel rejection.

- [ ] **Step 2: Run RED**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotificationPresentationTest' --tests '*BoopNotificationCoordinatorTest' --stacktrace
```

- [ ] **Step 3: Implement the state machine**

Use `LinkedHashMap<String,BoopNotificationEnvelope>` for Android-active entries, `LinkedHashSet<String>` for visible keys, and `visibleStartedAtMs`.

Rules:
1. denied -> `IGNORE`;
2. repost existing visible key -> update card, `UPDATE`, no cue;
3. no visible keys -> new single, `PRESENT`, cue;
4. visible size 1 and `nowMs-visibleStartedAtMs <= burstWindowMs` -> add second, `UPDATE`, no cue;
5. visible size 1 outside burst window -> replace visible single with the new key, `PRESENT`, cue; old notification remains only in active inbox;
6. visible size >=2 -> add/update any allowed notification, `UPDATE`, no cue until presentation dismissal/timeout;
7. `rebuild` never sets visible keys.

- [ ] **Step 4: Run GREEN and commit**

```bash
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotificationPresentationTest' --tests '*BoopNotificationCoordinatorTest' --stacktrace
git add source/BoopNotificationSurface.java source/BoopNotificationEnvelope.java \
  source/BoopNotificationPresentation.java source/BoopNotificationCoordinator.java \
  source-test/BoopNotificationPresentationTest.java source-test/BoopNotificationCoordinatorTest.java
git commit -m "feat(unified): add notification bundle coordinator"
```

---

### Task 3: Notification Listener intake and app-owned runtime

**Files:**
- Create: `source/BoopNotificationRuntime.java`
- Create: `source/BoopNotificationListenerService.java`
- Modify: `unified/UnifiedApplication.java`
- Test: extend `source-test/BoopNotificationIntakePolicyTest.java`

**Interfaces:**
- Nested `BoopNotificationRuntime.RuntimeRecord(BoopNotificationEnvelope envelope, PendingIntent contentIntent, BoopNotificationChannelInfo channelInfo)`.
- `static BoopNotificationRuntime initialize(Application app)` / `static BoopNotificationRuntime get(Context context)`.
- `BoopNotificationSettingsState settings()` reads the current store state; `void refreshSettings()` refreshes coordinator decisions after settings edits.
- `void attachListener(BoopNotificationListenerService listener)` / `detachListener(BoopNotificationListenerService listener)`.
- `void observeChannel(BoopNotificationChannelInfo info)`.
- `void post(BoopNotificationEnvelope envelope, PendingIntent contentIntent, BoopNotificationChannelInfo channelInfo)`.
- `void rebuild(List<BoopNotificationRuntime.RuntimeRecord> records)` seeds active state/PendingIntents without presenting/cueing.
- `void remove(String key)`.
- `void cancelAfterSuccessfulAutoCancelTap(String key)` calls attached listener `cancelNotification(key)` only when listener is connected.

- [ ] **Step 1: Lock the rich-content privacy gate with one more test**

```java
@Test public void fullyAllowedChannelCanReadRichContentButSiblingCannot() {
    BoopNotificationSettingsState state = fullyAllowed("com.chat", "messages");
    assertEquals(BoopNotificationIntakePolicy.Mode.READ_RICH_CONTENT,
        BoopNotificationIntakePolicy.decide(state, "com.chat", "messages"));
    assertEquals(BoopNotificationIntakePolicy.Mode.OBSERVE_CHANNEL_ONLY,
        BoopNotificationIntakePolicy.decide(state, "com.chat", "promotions"));
}
```

- [ ] **Step 2: Implement listener ordering**

`onNotificationPosted` must execute in this order:

```java
String key = sbn.getKey();
String packageName = sbn.getPackageName();
Ranking ranking = new Ranking();
NotificationChannel channel = rankingMap != null && rankingMap.getRanking(key, ranking)
        ? ranking.getChannel() : null;
BoopNotificationChannelInfo info = channelInfo(packageName, sbn.getNotification(), channel);
runtime.observeChannel(info);
if (BoopNotificationIntakePolicy.decide(runtime.settings(), packageName, info.channelId())
        != BoopNotificationIntakePolicy.Mode.READ_RICH_CONTENT) {
    return;
}
BoopNotificationEnvelope envelope = richEnvelope(sbn, info);
runtime.post(envelope, sbn.getNotification().contentIntent, info);
```

`richEnvelope(StatusBarNotification, BoopNotificationChannelInfo)` is the only listener method that reads `Notification.EXTRA_TITLE`, `EXTRA_TEXT` or equivalent rich extras.

If `Ranking.getChannel()` is unavailable, channel metadata uses `Notification.getChannelId()`, `effectsKnown=false`, sound/vibration booleans false, and a display label equal to the channel ID. Unknown effects always suppress BOOP's own cue later.

`onListenerConnected()` scans `getActiveNotifications()`, observes channel metadata for every active item, reads rich content only for currently allowed package+channel pairs, then calls one `runtime.rebuild(records)`. No rebuild presentation/cue.

`onNotificationRemoved()` calls `runtime.remove(key)`. `onListenerDisconnected()` detaches the listener and leaves Android authoritative.

- [ ] **Step 3: Initialize runtime only for WALL/LAUNCHER**

In `UnifiedApplication.onCreate()`, resolve mode once. For WALL/LAUNCHER initialize notification runtime before returning from the existing non-Shield path. SHIELD keeps its density/crash-handler code and never initializes the phone notification runtime.

- [ ] **Step 4: Materialize, test, compile and commit**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:compileDebugJavaWithJavac :app:testDebugUnitTest \
  --tests '*BoopNotification*' --tests '*BoopDeviceProfileTest' --stacktrace
git add source/BoopNotificationRuntime.java source/BoopNotificationListenerService.java \
  unified/UnifiedApplication.java source-test/BoopNotificationIntakePolicyTest.java
git commit -m "feat(unified): add notification listener runtime"
```

---

### Task 4: First-run Notification Access + overlay onboarding

**Files:**
- Create: `source/BoopNotificationPermissionState.java`
- Create: `source/BoopNotificationOnboardingState.java`
- Create: `source/BoopNotificationStartupGate.java`
- Create: `source/BoopNotificationOnboardingActivity.java`
- Test: `source-test/BoopNotificationStartupGateTest.java`
- Modify: `unified/UnifiedEntryActivity.java`

**Interfaces:**
- `boolean hasListenerAccess(Context)` uses `NotificationManager.isNotificationListenerAccessGranted(ComponentName)`.
- `boolean hasOverlayAccess(Context)` uses `Settings.canDrawOverlays(context)`.
- `Intent notificationListenerSettingsIntent()` -> `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`.
- `Intent overlaySettingsIntent(Context)` -> `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` with `package:<packageName>`.
- `BoopNotificationOnboardingState.isSeen(Context)` / `markSeen(Context)` uses `boop_notifications:onboarding_seen_v1`.
- `BoopNotificationStartupGate.Target resolve(BoopDeviceProfile.Mode mode, boolean seen)` returns `NOTIFICATION_ONBOARDING` only for unseen WALL/LAUNCHER.

- [ ] **Step 1: Write RED startup tests**

```java
@Test public void unseenPhonesGetNotificationOnboarding() {
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

- [ ] **Step 2: Implement onboarding phases exactly**

Use `INTRO`, `WAITING_LISTENER_SETTINGS`, `WAITING_OVERLAY_SETTINGS`, `DONE`.

- `INTRO`: `Set up` and `Not now`.
- `Not now`: mark seen, finish, normal BOOP continues.
- `Set up`: record `WAITING_LISTENER_SETTINGS`, launch Notification Access settings.
- Return from listener settings: record actual grant result, transition once to overlay step and open overlay settings.
- Return from overlay settings: record actual grant result, mark seen, finish regardless of grant state.
- Do not enable master/app/channel.

Persist only `onboarding_seen_v1`; transient phase survives recreation through `savedInstanceState`, not preferences.

- [ ] **Step 3: Gate phone routing without touching Shield assistant setup**

Before normal WALL/LAUNCHER routing in `UnifiedEntryActivity`, resolve startup gate. Start onboarding once and resume normal route after it returns. Existing Shield assistant first-run flow remains unchanged.

- [ ] **Step 4: Test and commit**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotificationStartupGateTest' --tests '*ShieldEntryRouteTest' \
  --tests '*BoopDeviceProfileTest' --stacktrace
git add source/BoopNotificationPermissionState.java source/BoopNotificationOnboardingState.java \
  source/BoopNotificationStartupGate.java source/BoopNotificationOnboardingActivity.java \
  source-test/BoopNotificationStartupGateTest.java unified/UnifiedEntryActivity.java
git commit -m "feat(unified): add notification first-run setup"
```

---

### Task 5: Voice Settings -> Notifications app/channel selector

**Files:**
- Create: `source/BoopNotificationAppEntry.java`
- Create: `source/BoopNotificationAppCatalogModel.java`
- Create: `source/BoopNotificationAppCatalog.java`
- Create: `source/BoopNotificationSettingsActivity.java`
- Test: `source-test/BoopNotificationAppCatalogModelTest.java`
- Create: `scripts/patch-unified-notifications.py`
- Modify: `scripts/materialize-unified.sh`

**Interfaces:**
- `List<BoopNotificationAppEntry> BoopNotificationAppCatalogModel.merge(Collection<BoopNotificationAppEntry> launchable, Collection<BoopNotificationAppEntry> observed)` dedupes package and sorts by label then package.
- `BoopNotificationAppCatalog.load(Context, Collection<BoopNotificationChannelInfo>)` unions `LauncherApps.getActivityList(null, Process.myUserHandle())` with observed packages; no `QUERY_ALL_PACKAGES`.
- Settings Activity reads/writes `BoopNotificationSettingsStore`, then calls `BoopNotificationRuntime.get(this).refreshSettings()` after each change.
- Channel settings route uses `Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS`, `Settings.EXTRA_APP_PACKAGE`, `Settings.EXTRA_CHANNEL_ID`.

- [ ] **Step 1: Write RED catalog test**

```java
@Test public void mergesLaunchableAndObservedPackagesWithoutDuplicates() {
    List<BoopNotificationAppEntry> result = BoopNotificationAppCatalogModel.merge(
        List.of(new BoopNotificationAppEntry("com.chat", "Chat")),
        List.of(new BoopNotificationAppEntry("com.chat", "Chat"),
                new BoopNotificationAppEntry("com.sync", "Sync Service")));
    assertEquals(List.of("com.chat", "com.sync"),
        result.stream().map(BoopNotificationAppEntry::packageName).collect(Collectors.toList()));
}
```

- [ ] **Step 2: Implement settings UI in this order**

1. readiness: `Ready`, `Needs Notification Access`, `Needs Display Permission`, or both missing;
2. master BOOP Notifications switch, default OFF;
3. timeout control 3-30 seconds, default 8;
4. app list with app toggle;
5. under an enabled app, only observed channel/category rows;
6. each channel row: Android channel name, BOOP allow toggle, native-alert status, `Open Android channel settings`.

When no category has been observed, show exactly:

`No notification categories seen yet. When this app sends one, BOOP will learn the category here. It will not interrupt until you enable that category.`

If the chosen channel still has sound/vibration, show exactly:

`Android is still alerting for this category. Make it silent there before BOOP uses his own sound.`

- [ ] **Step 3: Add one late, idempotent materialization patch for Voice Settings**

`scripts/patch-unified-notifications.py` inserts one `Notifications` button before Voice `Done`, marked `// BOOP_NOTIFICATION_SETTINGS_ENTRY_V1`, launching `BoopNotificationSettingsActivity`. It must fail if the structural anchor is missing and must do nothing if the marker already exists.

Run this patch late in `scripts/materialize-unified.sh`, after current eye/sleep/hue patches.

- [ ] **Step 4: Materialize, test integration and commit**

```bash
bash scripts/materialize-unified.sh
test "$(grep -c 'BOOP_NOTIFICATION_SETTINGS_ENTRY_V1' boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java)" -eq 1
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotification*' --stacktrace
git add source/BoopNotificationAppEntry.java source/BoopNotificationAppCatalogModel.java \
  source/BoopNotificationAppCatalog.java source/BoopNotificationSettingsActivity.java \
  source-test/BoopNotificationAppCatalogModelTest.java scripts/patch-unified-notifications.py \
  scripts/materialize-unified.sh
git commit -m "feat(unified): add notification controls under Voice"
```

The marker check verifies patch application only, never appearance.

---

### Task 6: Surface selection, full interruption and lock-screen lifecycle

**Files:**
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
- `BoopNotificationHost.show(BoopNotificationPresentation presentation, long timeoutMs)`.
- `BoopNotificationHost.update(BoopNotificationPresentation presentation, long timeoutMs)`.
- `BoopNotificationHost.hide()`.
- `BoopNotificationRuntime.registerWallHost(BoopNotificationHost host)` / `unregisterWallHost(BoopNotificationHost host)`.
- `void transitionAfterUnlock()` re-renders the same visible bundle unlocked with `playCue=false`.

- [ ] **Step 1: Write RED surface tests**

```java
@Test public void screenOffIsAlwaysPrivacySafeLockedSurface() {
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

- [ ] **Step 2: Implement selector and controllers**

Selector rule: `!interactive || keyguardLocked -> LOCKED`; else visible Wall host -> `IN_PLACE`; otherwise -> `OVERLAY`.

`BoopNotificationOverlayController` uses a full-screen, focusable `TYPE_APPLICATION_OVERLAY` only when `Settings.canDrawOverlays(context)` is true. Catch `SecurityException`/window-add failure, remove partial state, and report presentation failure to runtime without cancelling source notification.

`BoopNotificationLockActivity` calls `setShowWhenLocked(true)` and `setTurnScreenOn(true)`, adds `FLAG_KEEP_SCREEN_ON` only while presentation is active, and finishes on swipe/timeout. It never dismisses keyguard automatically.

Register `ACTION_USER_PRESENT` dynamically while lock Activity is alive. On API 33+ use `Context.RECEIVER_NOT_EXPORTED`; on older supported APIs use the legacy overload. On unlock call `runtime.transitionAfterUnlock()` then finish lock Activity. Do not replay cue.

- [ ] **Step 3: Extend the late Wall patch with in-place host wiring**

Patch materialized `MainActivity` to construct `BoopNotificationInPlaceController(interactionSurface)`, register in `onResume`, unregister in `onPause`, marker `// BOOP_NOTIFICATION_IN_PLACE_HOST_V1`. Do not alter eye geometry, sleep, blink, microphone or wake logic.

- [ ] **Step 4: Test and commit**

```bash
bash scripts/materialize-unified.sh
test "$(grep -c 'BOOP_NOTIFICATION_IN_PLACE_HOST_V1' boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java)" -eq 1
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotification*' --tests '*BoopPresenceStateTest' --stacktrace
git add source/BoopNotificationSurfaceSelector.java source/BoopNotificationHost.java \
  source/BoopNotificationInPlaceController.java source/BoopNotificationOverlayController.java \
  source/BoopNotificationLockActivity.java source/BoopNotificationRuntime.java \
  source-test/BoopNotificationSurfaceSelectorTest.java scripts/patch-unified-notifications.py
git commit -m "feat(unified): add notification presentation surfaces"
```

---

### Task 7: Original PendingIntent semantics, keyguard-authenticated opening and BOOP inbox

**Files:**
- Create: `source/BoopNotificationTapPolicy.java`
- Create: `source/BoopNotificationTapLauncher.java`
- Create: `source/BoopNotificationInboxActivity.java`
- Test: `source-test/BoopNotificationTapPolicyTest.java`
- Modify: `source/BoopNotificationRuntime.java`
- Modify: `source/BoopNotificationLockActivity.java`

**Interfaces:**
- `static final int NO_BACKGROUND_START_OVERRIDE = -1`.
- `boolean shouldCancelAfterSuccessfulSend(boolean autoCancel, boolean sendSucceeded)`.
- `int backgroundStartModeForSdk(int sdk)` returns API 36+ `MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE`, API 34-35 `MODE_BACKGROUND_ACTIVITY_START_ALLOWED`, API <=33 `NO_BACKGROUND_START_OVERRIDE`.
- `BoopNotificationTapLauncher.Result send(Context context, PendingIntent intent, int sdk)` returns `OPENED` or `CANCELLED`.
- Inbox obtains active data from runtime at render time; no rich content is serialized into Intent extras or persisted.

- [ ] **Step 1: Write RED tap tests**

```java
@Test public void autoCancelOnlyAfterSuccessfulSend() {
    assertTrue(BoopNotificationTapPolicy.shouldCancelAfterSuccessfulSend(true, true));
    assertFalse(BoopNotificationTapPolicy.shouldCancelAfterSuccessfulSend(true, false));
    assertFalse(BoopNotificationTapPolicy.shouldCancelAfterSuccessfulSend(false, true));
}

@Test public void backgroundStartModesAreVersionSpecific() {
    assertEquals(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE,
        BoopNotificationTapPolicy.backgroundStartModeForSdk(36));
    assertEquals(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED,
        BoopNotificationTapPolicy.backgroundStartModeForSdk(34));
    assertEquals(BoopNotificationTapPolicy.NO_BACKGROUND_START_OVERRIDE,
        BoopNotificationTapPolicy.backgroundStartModeForSdk(33));
}
```

- [ ] **Step 2: Implement PendingIntent sending**

API 36+: set `MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE` on `ActivityOptions`. API 34-35: set `MODE_BACKGROUND_ACTIVITY_START_ALLOWED`. API <=33: send without an options override. Catch `PendingIntent.CanceledException` and return `CANCELLED`.

On `OPENED && autoCancel`, runtime asks attached listener to `cancelNotification(key)`. On `CANCELLED`, keep Android notification and BOOP card, show `Can't open that right now.` Swipe/timeout never call cancellation.

- [ ] **Step 3: Require real keyguard authentication before opening from locked UI**

For single-card tap while keyguard is locked, `BoopNotificationLockActivity` calls `KeyguardManager.requestDismissKeyguard(this, callback)`. Only `onDismissSucceeded()` may call runtime open. `onDismissCancelled()`/`onDismissError()` leave card and source notification in place.

For locked bundle tap, use the same keyguard request, then launch `BoopNotificationInboxActivity` only after successful dismissal. BOOP never supplies credentials and never bypasses keyguard.

- [ ] **Step 4: Build BOOP inbox**

Bundle tap while unlocked opens `BoopNotificationInboxActivity`. It queries `runtime.activeNotifications()` at render time, groups current allowed cards by app, and sends an individual item through the same tap launcher. If runtime has no active items, finish cleanly.

- [ ] **Step 5: Test and commit**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotification*' --stacktrace
git add source/BoopNotificationTapPolicy.java source/BoopNotificationTapLauncher.java \
  source/BoopNotificationInboxActivity.java source/BoopNotificationRuntime.java \
  source/BoopNotificationLockActivity.java source-test/BoopNotificationTapPolicyTest.java
git commit -m "feat(unified): preserve notification open semantics"
```

---

### Task 8: Exact hands + reusable runtime BOOP notification view

**Files:**
- Create: `unified/assets/boop-notifications/README.md`
- Create exact binary: `unified/assets/boop-notifications/boop-yellow-hands-approved.png`
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
- `boolean BoopNotificationSwipeGesture.isDismiss(float downX, float downY, float upX, float upY, float density)` requires >=72dp dominant travel and dominant travel >1.25x perpendicular travel.

- [ ] **Step 1: Transfer exact bytes and verify hash**

```bash
mkdir -p unified/assets/boop-notifications
git show origin/animation-freddie-mercury:boop-yellow-hands-approved.png \
  > unified/assets/boop-notifications/boop-yellow-hands-approved.png
sha256sum unified/assets/boop-notifications/boop-yellow-hands-approved.png
```

Expected `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`.

`README.md` records that this notification-specific exact binary does not replace the older general hand-master historical record.

- [ ] **Step 2: Write integrity + RED swipe tests**

```python
from hashlib import sha256
from pathlib import Path

def test_notification_hands_are_exact_locked_binary():
    p = Path('unified/assets/boop-notifications/boop-yellow-hands-approved.png')
    assert sha256(p.read_bytes()).hexdigest() == '26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1'
```

```java
@Test public void deliberateSwipeDismissesButTapDoesNot() {
    assertTrue(BoopNotificationSwipeGesture.isDismiss(10f, 10f, 190f, 20f, 2f));
    assertFalse(BoopNotificationSwipeGesture.isDismiss(10f, 10f, 20f, 18f, 2f));
}
```

- [ ] **Step 3: Materialize assets byte-for-byte**

`scripts/materialize-boop-notification-assets.py` verifies the exact SHA above and byte-copies it to `boop-build/BOOP-Alpha1/app/src/main/res/drawable-nodpi/boop_notification_hands.png`. It must not crop, threshold, recolor, recompress or regenerate the file.

Run this script late in `scripts/materialize-unified.sh` after resources exist.

- [ ] **Step 4: Implement one reusable puppet view**

Use a black `FrameLayout` containing:
- current materialized `BoopFaceView`, so current approved eye renderer and saved hue stay authoritative;
- exact paired-hand PNG in one `ImageView`;
- centered notification prop/card with app icon and count;
- title/text views only when `presentation.locked() == false`.

Entrance motion: card alpha 0->1 + `translationY(-16dp)->0`, `OvershootInterpolator(0.7f)`, 260ms; hands scale 0.96->1, 220ms. Reuse existing face wake/blink behavior. Do not alter face bitmap or generate eye/hand poses.

Single tap -> `onOpen(key)`. Bundle tap -> `onOpenBundle()`. Deliberate swipe -> `onDismiss()`.

- [ ] **Step 5: Use the same view in in-place, overlay and lock surfaces**

Only wrappers differ in lifecycle/window ownership. Presentation UI/gesture code must not be copied three times.

- [ ] **Step 6: Test, compile and commit**

```bash
python -m pytest -q tests/test_unified_notification_asset_integrity.py
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotification*' --stacktrace
git add unified/assets/boop-notifications scripts/materialize-boop-notification-assets.py \
  tests/test_unified_notification_asset_integrity.py source/BoopNotificationSwipeGesture.java \
  source/BoopNotificationPuppetView.java source/BoopNotificationInPlaceController.java \
  source/BoopNotificationOverlayController.java source/BoopNotificationLockActivity.java \
  source/BoopNotificationInboxActivity.java source-test/BoopNotificationSwipeGestureTest.java \
  scripts/materialize-unified.sh
git commit -m "feat(unified): add exact notification puppet layers"
```

No visual acceptance is inferred from these checks.

---

### Task 9: Local BOOP replacement sound + vibration, one cue per presentation

**Files:**
- Create: `source/BoopNotificationCuePolicy.java`
- Create: `source/BoopNotificationCueRenderer.java`
- Create: `source/BoopNotificationCue.java`
- Test: `source-test/BoopNotificationCuePolicyTest.java`
- Test: `source-test/BoopNotificationCueRendererTest.java`
- Modify: `source/BoopNotificationRuntime.java`

**Interfaces:**
- `boolean shouldPlay(boolean coordinatorRequestsCue, BoopNotificationChannelInfo channel)` true only for requested cue + known silent native channel.
- `short[] BoopNotificationCueRenderer.render(int sampleRate)` deterministic 320ms PCM16 mono.
- `void BoopNotificationCue.play()` uses `AudioAttributes.USAGE_NOTIFICATION`; vibration waveform is one `long[]{0,35,55,28}` sequence.

- [ ] **Step 1: Write RED sound-policy tests**

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

@Test public void cueIsShortDeterministicAndBounded() {
    short[] a = BoopNotificationCueRenderer.render(44100);
    short[] b = BoopNotificationCueRenderer.render(44100);
    assertEquals(14112, a.length);
    assertArrayEquals(a, b);
    int peak = 0;
    for (short sample : a) peak = Math.max(peak, Math.abs((int) sample));
    assertTrue(peak > 2000 && peak <= 32767);
}
```

- [ ] **Step 2: Implement the deliberately daft local cue**

For each sample time `t`, sum these three components and clamp to +/-0.72 full scale:

```java
double knock1 = t < 0.050
        ? Math.sin(2.0 * Math.PI * 190.0 * t) * Math.exp(-55.0 * t) * 0.42 : 0.0;
double u2 = t - 0.078;
double knock2 = u2 >= 0.0 && u2 < 0.050
        ? Math.sin(2.0 * Math.PI * 285.0 * u2) * Math.exp(-52.0 * u2) * 0.34 : 0.0;
double uc = t - 0.138;
double chirp = 0.0;
if (uc >= 0.0 && uc < 0.170) {
    double phase = 2.0 * Math.PI * (520.0 * uc + 0.5 * 1650.0 * uc * uc);
    double envelope = Math.sin(Math.PI * uc / 0.170);
    chirp = Math.sin(phase) * envelope * 0.24;
}
```

Use mono PCM16 `AudioTrack`, `AudioAttributes.USAGE_NOTIFICATION`, no loop and no network/downloaded sound asset. Use `VibrationEffect.createWaveform(new long[]{0,35,55,28}, -1)`.

- [ ] **Step 3: Wire cue policy**

Only coordinator `playCue=true` may request cue playback. Bundle updates and unlock transitions never replay it. Unknown/noisy native channel effects suppress BOOP's own sound/vibration while visual presentation remains allowed.

- [ ] **Step 4: Test and commit**

```bash
bash scripts/materialize-unified.sh
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotification*' --stacktrace
git add source/BoopNotificationCuePolicy.java source/BoopNotificationCueRenderer.java \
  source/BoopNotificationCue.java source/BoopNotificationRuntime.java \
  source-test/BoopNotificationCuePolicyTest.java source-test/BoopNotificationCueRendererTest.java
git commit -m "feat(unified): add BOOP notification cue"
```

---

### Task 10: Manifest, platform security contracts and canonical CI

**Files:**
- Modify: `source/AndroidManifest.xml`
- Create: `tests/test_unified_notification_manifest_contract.py`
- Modify: `.github/workflows/build-boop-unified.yml`

**Manifest contract:**
- permissions: `android.permission.SYSTEM_ALERT_WINDOW`, `android.permission.VIBRATE`;
- `.BoopNotificationListenerService`: exported `false`, requires `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`, intent action `android.service.notification.NotificationListenerService`;
- `.BoopNotificationOnboardingActivity`, `.BoopNotificationSettingsActivity`, `.BoopNotificationLockActivity`, `.BoopNotificationInboxActivity`: exported `false`;
- absent: `USE_FULL_SCREEN_INTENT`, device-admin declaration, accessibility-service declaration, `QUERY_ALL_PACKAGES`.

- [ ] **Step 1: Write RED materialized-manifest test**

```python
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
    activity_names = {n.get(ANDROID + 'name'): n.get(ANDROID + 'exported')
                      for n in app.findall('activity')}
    for name in ('.BoopNotificationOnboardingActivity', '.BoopNotificationSettingsActivity',
                 '.BoopNotificationLockActivity', '.BoopNotificationInboxActivity'):
        assert activity_names[name] == 'false'
```

Add assertions that no service action is `android.accessibilityservice.AccessibilityService` and no receiver carries device-admin metadata.

- [ ] **Step 2: Materialize and verify RED**

```bash
bash scripts/materialize-unified.sh
python -m pytest -q tests/test_unified_notification_manifest_contract.py
```

- [ ] **Step 3: Add exact manifest declarations, no extra authority**

Keep existing launcher/auth/assistant/unified declarations intact.

- [ ] **Step 4: Extend CI with notification non-visual checks**

Before materialization add `tests/test_unified_notification_asset_integrity.py`. After materialization add `tests/test_unified_notification_manifest_contract.py`. Add `--tests '*BoopNotification*'` to focused unified JUnit selection.

Also remove the workflow's hardcoded `versionCode='62'` / v62 `versionName` assumptions. Derive expected code/name from `unified/app-build.gradle` with a small Python regex step and compare `aapt dump badging` against those values. Preserve explicit package, launch activity, signer and ZIP-integrity verification. This prevents the notification release bump from requiring a second brittle workflow edit.

- [ ] **Step 5: Run all non-visual contracts and commit**

```bash
python -m pytest -q tests/test_unified_docked_shield_pass.py tests/test_approved_eye_master_contract.py \
  tests/test_unified_notification_asset_integrity.py
bash scripts/materialize-unified.sh
python -m pytest -q tests/test_unified_wake_handoff_contract.py tests/test_unified_notification_manifest_contract.py
gradle --no-daemon -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest \
  --tests '*BoopNotification*' --tests '*BoopWake*' --tests '*BoopDeviceProfileTest' \
  --tests '*ShieldEntryRouteTest' --tests '*BoopAssistant*' --stacktrace
git add source/AndroidManifest.xml tests/test_unified_notification_manifest_contract.py \
  .github/workflows/build-boop-unified.yml
git commit -m "test(unified): lock notification platform contracts"
```

---

### Task 11: Version, sign, document and hand to Ryan for physical acceptance

**Files:**
- Modify: `unified/app-build.gradle`
- Modify after successful signed workflow: `SESSION_HANDOFF.md`
- Modify after successful signed workflow: `BOOP_STATUS.md`
- Modify after successful signed workflow: `BOOP_UNIFIED_MEMORY.md`

**Release rule:** Read the canonical version at execution time and increment `versionCode` by exactly 1. Do not assume v63 because the eye/sleep reconciliation may consume versions first. Preserve `com.boop.alpha1` and permanent signer.

- [ ] **Step 1: Re-fetch immediately before release commit**

```bash
git fetch origin main boop-unified
git rev-parse HEAD
git rev-parse origin/boop-unified
git rev-parse origin/main
grep -n "versionCode\|versionName" unified/app-build.gradle
```

If remote canonical advanced beyond reviewed history, reconcile without force.

- [ ] **Step 2: Bump version exactly once**

Increment the shown code by 1. Keep the established numeric version-name prefix and end the name with `-unified-notification-presenter`.

- [ ] **Step 3: Run complete canonical non-visual verification**

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

Expected: zero test failures and successful build. This is not device/visual/acoustic acceptance.

- [ ] **Step 4: Commit and push reviewed release source**

```bash
git add unified/app-build.gradle
git commit -m "release(unified): notification presenter candidate"
git push origin HEAD:boop-unified
```

- [ ] **Step 5: Verify exact GitHub Actions run**

Require canonical `Build BOOP Unified APK` SUCCESS for the exact pushed source commit. Record workflow run ID, artifact ID, APK SHA-256, signer SHA-256 and focused test counts. Do not offer an older artifact.

- [ ] **Step 6: Update handoff/status/memory with exact evidence**

Record exact built commit/version/run/artifact/APK hash/signer hash, plus explicit `CI/signer green; physical visual/lock-screen/acoustic acceptance pending`.

Physical checklist for Ryan:
1. first-run permission flow and both decline paths;
2. app selection, observed category appearance, category enablement and Android-silence guide;
3. screen-off wake and locked app/icon/count-only privacy;
4. locked single/bundle tap requires real unlock before opening;
5. unlock transition reveals rich content without second cue;
6. full interruption over another app and in-place Wall presentation;
7. original single tap target and normal auto-cancel semantics;
8. swipe/timeout leaves shade notification intact;
9. same-app and mixed-app message-bomb bundling + BOOP inbox;
10. default/custom timeout;
11. process/reboot rebuild from active Android notifications;
12. revoked access/overlay repair state;
13. BOOP's replacement sound/vibration quality.

- [ ] **Step 7: Commit evidence docs and verify live head**

```bash
git add SESSION_HANDOFF.md BOOP_STATUS.md BOOP_UNIFIED_MEMORY.md
git commit -m "docs(unified): record notification presenter candidate"
git push origin HEAD:boop-unified
git fetch origin boop-unified
git rev-parse HEAD
git rev-parse origin/boop-unified
```

Expected: local and remote heads match. Do not create a physical rollback checkpoint until Ryan explicitly accepts that exact signed APK on the real Pixel.

---

## Plan self-review result

- Spec coverage: app/channel opt-in, first-run permissions, wake/lock privacy, full unlocked interruption, grouping, inbox, swipe semantics, original PendingIntent, timeout, local-only storage, channel-silence handoff, replacement cue, failure behavior, exact runtime art, non-visual CI and physical acceptance all map to tasks above.
- Type consistency: `BoopNotificationSurface` is created before presentation tests; `RuntimeRecord`, host methods and API <=33 tap sentinel are defined before use.
- Platform safety: locked taps explicitly authenticate through Android keyguard; ordinary listener channel authority is not overstated; unknown channel effect state suppresses BOOP's cue.
- Placeholder scan: no deferred implementation markers or unspecified error-handling steps remain.
