# BOOP Shield Home First Slice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the proven Shield eye overlay into the first usable BOOP Home TV shell: hide/show the known-good eyes, pair locally to Home Assistant, choose the Shield room with one D-pad click, show a chunky hybrid dashboard, and execute one real binary HA control while remaining useful when HA is offline.

**Architecture:** Keep `com.boop.shieldoverlay` and its proven overlay service as the protected runtime. `MainActivity` remains the launcher/overlay-permission gateway and launches a new full-screen `BoopHomeActivity`; that activity hides the existing overlay by command while foreground and restores it when leaving. A small HA session/repository layer uses the credential proved by the pairing-gate plan, Home Assistant WebSocket commands for areas/entities/state/actions, and local preferences/cache for room identity, favourites, and stale UI state.

**Tech Stack:** Android Java 17, compileSdk/targetSdk 36, minSdk 26, plain Android TV Views/D-pad focus, OkHttp WebSocket, Home Assistant WebSocket API, SharedPreferences, existing Android Keystore credential store, existing stable BOOP dev signer.

**Spec:** `docs/superpowers/specs/2026-09-05-boop-shield-home-design.md`

## Global Constraints

- Prerequisite: `2026-09-05-boop-shield-local-pairing-gate.md` physically passed. If it did not, do not execute this plan.
- Continue on `boop-shield-home-implementation` with the stable BOOP development signer already configured.
- Keep Shield package/application ID `com.boop.shieldoverlay`; no second Shield package and no further signing-driven uninstall.
- Preserve the known-good overlay renderer, size, position, `TYPE_APPLICATION_OVERLAY`, non-focusable/non-touchable flags, foreground service, and display-change `postInvalidateOnAnimation()` behavior.
- Hide/show must not destroy the overlay service, recreate the eye renderer, or remove/re-add the window for normal BOOP Home transitions.
- BOOP Home is D-pad/Select/Back first. No keyboard fields on Shield.
- Launcher and Netflix-button mapping both open BOOP Home; BOOP itself does not globally intercept the Netflix key.
- First-run normal flow is local HA discovery → QR pairing → **“Found it.”** → **“Where am I?”** → one-click room card.
- Home uses chunky favourites first, then room cards; sensors and sensor graphs are excluded.
- First slice implements only one real binary control path. Rich dimmer/fan/climate/media controls, voice, routines, full-screen giant puppet, and phone routine editor remain out of scope.
- If HA is unreachable, cached dashboard structure/states remain visible as stale, HA controls are disabled, local BOOP navigation/settings remain usable, and BOOP never claims action success.
- Dolby Vision remains unverified. Do not claim DV compatibility from HDR10 evidence.

## File Map

- `MainActivity.java` — permission gateway + starts service + opens `BoopHomeActivity`.
- `BoopOverlayService.java` — protected overlay plus explicit hide/show actions only.
- `BoopOverlayController.java` — activity-facing hide/show commands.
- `BoopHomeActivity.java` — full-screen TV shell and page switching only.
- `TvHomeView.java`, `TvRoomPickerView.java`, `TvPairingView.java`, `TvSettingsView.java`, `TvRoutinesView.java` — focused TV UI components.
- `FocusCardView.java` — consistent big card focus scale/glow treatment.
- `BoopPreferences.java` — selected room + local favourite pins only.
- `HomeAssistantSession.java` — access-token refresh and authenticated WebSocket lifecycle.
- `HomeAssistantWebSocket.java` — request/response command transport.
- `HomeAssistantRepository.java` — areas, display entities, states, binary action dispatch.
- `AreaInfo.java`, `EntityCard.java`, `EntityState.java`, `DashboardSnapshot.java` — UI-safe models.
- `DashboardCache.java` — last-known dashboard snapshot/timestamp.
- launcher art under `res/mipmap-*` / `res/drawable-xhdpi`.

---

### Task 1: Add a protected overlay hide/show interface

**Files:**
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopOverlayService.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopOverlayController.java`
- Modify: `tests/test_shield_overlay_source.py`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/OverlayVisibilityPolicyTest.java`

**Interfaces:**
- Actions: `com.boop.shieldoverlay.action.HIDE_EYES`, `com.boop.shieldoverlay.action.SHOW_EYES`.
- `BoopOverlayController.hide(Context)` / `show(Context)` send explicit intents to the existing service.
- Hidden state uses existing attached `overlayView.setVisibility(View.GONE)`; show uses `View.VISIBLE` + one `postInvalidateOnAnimation()`.

- [ ] **Step 1: Write RED regressions**

```python
def test_home_visibility_does_not_recreate_overlay(self):
    source = self.read('java/com/boop/shieldoverlay/BoopOverlayService.java')
    self.assertIn('ACTION_HIDE_EYES', source)
    self.assertIn('ACTION_SHOW_EYES', source)
    self.assertIn('setVisibility(View.GONE)', source)
    self.assertIn('setVisibility(View.VISIBLE)', source)
    self.assertNotIn('removeOverlay(); // hide', source)
```

Add a small JVM policy test proving `HIDE` and `SHOW` are distinct commands and normal service start remains `START_STICKY`.

- [ ] **Step 2: Run RED**

```bash
python3 -m unittest tests.test_shield_overlay_source -v
gradle -p shield-overlay :app:testDebugUnitTest --tests '*OverlayVisibilityPolicyTest' --stacktrace
```

- [ ] **Step 3: Implement only the visibility bridge**

In `onStartCommand`:

```java
String action = intent == null ? null : intent.getAction();
if (ACTION_HIDE_EYES.equals(action)) {
    if (overlayView != null) overlayView.setVisibility(View.GONE);
    return START_STICKY;
}
if (ACTION_SHOW_EYES.equals(action)) {
    ensureOverlay();
    if (overlayView != null) {
        overlayView.setVisibility(View.VISIBLE);
        overlayView.postInvalidateOnAnimation();
    }
    return START_STICKY;
}
```

Do not alter the display listener, window flags, geometry, art, or wake animation path.

- [ ] **Step 4: Run GREEN and commit**

```bash
python3 -m unittest tests.test_shield_overlay_source -v
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopOverlayService.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopOverlayController.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/OverlayVisibilityPolicyTest.java tests/test_shield_overlay_source.py
git commit -m 'feat: let BOOP Home hide and restore overlay eyes'
```

### Task 2: Split launcher gateway from full-screen BOOP Home

**Files:**
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/MainActivity.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopHomeActivity.java`
- Modify: `shield-overlay/app/src/main/AndroidManifest.xml`
- Modify: `shield-overlay/app/src/main/res/values/themes.xml`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/HomeLifecyclePolicyTest.java`

**Interfaces:** `MainActivity` handles overlay permission, starts foreground service, then launches `BoopHomeActivity`. `BoopHomeActivity.onStart()` hides eyes; `onStop()` restores them. Back from root calls `finish()`.

- [ ] **Step 1: Write RED lifecycle tests** for gateway→home launch, root Back finish, and hide/show lifecycle calls.

- [ ] **Step 2: Run RED**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --tests '*HomeLifecyclePolicyTest' --stacktrace
```

- [ ] **Step 3: Implement gateway and TV activity**

`MainActivity.startOverlayAndOpenHome()`:

```java
startForegroundService(new Intent(this, BoopOverlayService.class));
startActivity(new Intent(this, BoopHomeActivity.class));
finish();
```

`BoopHomeActivity` uses landscape, true black background, immersive TV chrome, and no touch assumptions. Manifest keeps both `LAUNCHER` and `LEANBACK_LAUNCHER` on `MainActivity`; `BoopHomeActivity` is non-exported.

- [ ] **Step 4: Run GREEN and commit**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/MainActivity.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopHomeActivity.java \
  shield-overlay/app/src/main/AndroidManifest.xml shield-overlay/app/src/main/res/values/themes.xml \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/HomeLifecyclePolicyTest.java
git commit -m 'feat: add BOOP Home TV activity shell'
```

### Task 3: Add new Shield launcher icon and Leanback banner

**Files:**
- Create/replace: `shield-overlay/app/src/main/res/mipmap-xhdpi/ic_boop_shield.png`
- Create/replace: `shield-overlay/app/src/main/res/mipmap-xhdpi/ic_boop_shield_round.png`
- Create: `shield-overlay/app/src/main/res/drawable-xhdpi/boop_shield_banner.png`
- Modify: `shield-overlay/app/src/main/AndroidManifest.xml`
- Create: `tests/test_shield_launcher_assets.py`

**Interfaces:** application icon points to BOOP Shield art; application banner points to 320×180 xhdpi banner with visible BOOP name. Existing `boop_eyes.png` overlay art remains byte-for-byte untouched.

- [ ] **Step 1: Write RED asset tests**

```python
from PIL import Image

def test_tv_banner_dimensions(self):
    image = Image.open(ROOT / 'shield-overlay/app/src/main/res/drawable-xhdpi/boop_shield_banner.png')
    self.assertEqual((320, 180), image.size)

def test_tv_icon_is_at_least_160_square(self):
    image = Image.open(ROOT / 'shield-overlay/app/src/main/res/mipmap-xhdpi/ic_boop_shield.png')
    self.assertGreaterEqual(image.size[0], 160)
    self.assertEqual(image.size[0], image.size[1])
```

Also hash the approved overlay `boop_eyes.png` before/after the art task during execution and require no change.

- [ ] **Step 2: Generate and visually approve the Shield-specific art**

Use BOOP's eye-led visual language, sofa-readable contrast, no Forki visual borrowing, no sensor/dashboard clutter. Banner includes the word **BOOP** and is exactly 320×180 px xhdpi; launcher icon is square and at least 160×160 px xhdpi. Obtain user visual approval before committing the generated assets.

- [ ] **Step 3: Wire manifest and run tests**

```xml
<application
    android:icon="@mipmap/ic_boop_shield"
    android:roundIcon="@mipmap/ic_boop_shield_round"
    android:banner="@drawable/boop_shield_banner"
    ...>
```

```bash
python3 -m unittest tests.test_shield_launcher_assets -v
```

- [ ] **Step 4: Commit**

```bash
git add shield-overlay/app/src/main/res/mipmap-xhdpi shield-overlay/app/src/main/res/drawable-xhdpi/boop_shield_banner.png \
  shield-overlay/app/src/main/AndroidManifest.xml tests/test_shield_launcher_assets.py
git commit -m 'feat: add BOOP Shield launcher identity'
```

### Task 4: Move the proven pairing gate into BOOP Home first-run flow

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvPairingView.java`
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopHomeActivity.java`
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/PairingGateController.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/FirstRunStateTest.java`

**Interfaces:** first-run states are `PAIRING`, `PAIRING_SUCCESS`, `ROOM_PICKER`, `HOME`. Existing saved credential skips pairing after successful refresh. Success shows **“Found it.”** briefly then enters room picker.

- [ ] **Step 1: Write RED state-machine tests**

```java
@Test public void freshInstallStartsAtPairing() { /* no credential -> PAIRING */ }
@Test public void pairedWithoutRoomShowsRoomPicker() { /* credential + no room -> ROOM_PICKER */ }
@Test public void pairedWithRoomShowsHome() { /* credential + room -> HOME */ }
@Test public void successfulPairingPassesThroughFoundIt() { /* PAIRING_SUCCESS then ROOM_PICKER */ }
```

- [ ] **Step 2: Run RED**, implement state coordinator, run GREEN.

`TvPairingView` contains only BOOP branding, **“I found your house”**, a huge QR, and one obvious retry/connect focus target. No dashboard is visible behind it.

- [ ] **Step 3: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvPairingView.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopHomeActivity.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/PairingGateController.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/FirstRunStateTest.java
git commit -m 'feat: integrate local pairing into BOOP Home'
```

### Task 5: Add authenticated Home Assistant WebSocket session

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantSession.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantWebSocket.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/HaCommand.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/HomeAssistantWebSocketTest.java`

**Interfaces:** `HomeAssistantSession.ensureAccessToken()` refreshes from secure stored credential; `HomeAssistantWebSocket.connect(baseUrl,accessToken,Listener)` authenticates and supports `send(String type, JSONObject body, Callback)` with monotonically increasing IDs.

- [ ] **Step 1: Write RED protocol tests** using MockWebServer WebSocket: server sends `auth_required`; client sends `{"type":"auth","access_token":"..."}`; `auth_ok` marks ready; command IDs increment; `auth_invalid` becomes a reauth failure and is not reported as success.

- [ ] **Step 2: Run RED**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --tests '*HomeAssistantWebSocketTest' --stacktrace
```

- [ ] **Step 3: Implement session transport**

Derive WebSocket URL from HA base URL: `http→ws`, `https→wss`, append `/api/websocket`. Keep access token in memory only. On disconnect, emit offline state; do not loop faster than a bounded reconnect backoff of 2s, 5s, 15s, then 30s maximum while BOOP Home is foreground.

- [ ] **Step 4: Run GREEN and commit**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --tests '*HomeAssistantWebSocketTest' --stacktrace
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantSession.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantWebSocket.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/HaCommand.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/HomeAssistantWebSocketTest.java
git commit -m 'feat: add local Home Assistant session transport'
```

### Task 6: Add one-click “Where am I?” room selection

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/AreaInfo.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantRepository.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopPreferences.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvRoomPickerView.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/HomeAssistantRepositoryTest.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/BoopPreferencesTest.java`

**Interfaces:** repository sends WebSocket command `config/area_registry/list`; preferences save only selected `area_id` and display name plus local favourite IDs. `TvRoomPickerView` emits one selected `AreaInfo` via D-pad Select.

- [ ] **Step 1: Write RED repository tests** asserting exact command:

```json
{"id":1,"type":"config/area_registry/list"}
```

and parsing area `area_id`/`name`. Preference tests prove a saved Bedroom choice is local to that app installation data store.

- [ ] **Step 2: Run RED**, implement models/repository/preferences, run GREEN.

- [ ] **Step 3: Build chunky picker**

Screen copy is exactly **“Where am I?”** followed by very large room cards. First room card receives focus. Select saves the room and immediately enters Home. No typing, search box, entity IDs, or HA jargon.

- [ ] **Step 4: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/AreaInfo.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantRepository.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopPreferences.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvRoomPickerView.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/HomeAssistantRepositoryTest.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/BoopPreferencesTest.java
git commit -m 'feat: add one-click BOOP room identity'
```

### Task 7: Build the chunky Home/Routines/Settings TV shell

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/FocusCardView.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvHomeView.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvRoutinesView.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvSettingsView.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvNavigationModel.java`
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopHomeActivity.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/TvNavigationModelTest.java`

**Interfaces:** left rail order is `HOME`, `ROUTINES`, `SETTINGS`; D-pad Left from page content returns to rail; selecting a rail item swaps page while keeping deterministic focus.

- [ ] **Step 1: Write RED navigation tests** for rail order, Home default, left-to-rail, Back from nested page→Home, Back from Home root→finish.

- [ ] **Step 2: Run RED**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --tests '*TvNavigationModelTest' --stacktrace
```

- [ ] **Step 3: Implement big TV shell**

`FocusCardView` applies obvious focus enlargement and elevation/outline without tiny switches. `TvHomeView` reserves top row for Favourites and below for Rooms. `TvRoutinesView` in this slice says **Routines — coming next** and contains no editor. `TvSettingsView` exposes connection status and **Where am I?**; no URL/token/YAML fields.

- [ ] **Step 4: Run GREEN and commit**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/FocusCardView.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvHomeView.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvRoutinesView.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvSettingsView.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvNavigationModel.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopHomeActivity.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/TvNavigationModelTest.java
git commit -m 'feat: add chunky BOOP Home TV navigation'
```

### Task 8: Populate one room-aware favourite and execute one real binary HA action

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/EntityCard.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/EntityState.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/DashboardSnapshot.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/FavouriteSelector.java`
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantRepository.java`
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvHomeView.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/FavouriteSelectorTest.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/BinaryActionTest.java`

**Interfaces:** repository requests `config/entity_registry/list_for_display` and `get_states`, joins by area ID, excludes hidden/config/diagnostic entries and all `sensor`/`binary_sensor` domains for the dashboard. First-slice control candidates are `light`, `switch`, `fan`, `input_boolean` with state `on`/`off`. Selector chooses a candidate from the selected room and stores its entity ID only as a local favourite presentation preference.

- [ ] **Step 1: Write RED selection tests**

```java
@Test public void selectedRoomWins() { /* Living Room never auto-picks Bedroom entity */ }
@Test public void sensorsNeverBecomeFavouriteControls() { /* sensor.temperature excluded */ }
@Test public void hiddenOrDiagnosticEntitiesAreExcluded() { /* hb/ec entries excluded */ }
```

- [ ] **Step 2: Write RED binary action tests** asserting off→`turn_on`, on→`turn_off`, exact target entity ID, and failed HA result is never mapped to success.

The action message is:

```json
{"type":"call_service","domain":"light","service":"turn_on","target":{"entity_id":"light.example"}}
```

Use the entity's actual domain; supported first-slice domains all use `turn_on` / `turn_off`.

- [ ] **Step 3: Run RED**, implement entity join/selection/action, run GREEN.

After HA reports the service call result as successful, request current state again before rendering the new card state. Do not optimistically claim a new state before HA confirms.

- [ ] **Step 4: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/EntityCard.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/EntityState.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/DashboardSnapshot.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/FavouriteSelector.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantRepository.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvHomeView.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/FavouriteSelectorTest.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/BinaryActionTest.java
git commit -m 'feat: control one room-aware Home Assistant favourite'
```

### Task 9: Add stale/offline dashboard behavior

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/DashboardCache.java`
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvHomeView.java`
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantSession.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/OfflineDashboardTest.java`

**Interfaces:** cache stores last snapshot + `savedAtMs`; Home view receives `LIVE` or `STALE`; actions are enabled only in `LIVE`.

- [ ] **Step 1: Write RED tests** proving disconnected session keeps cached cards visible, marks state stale, disables action dispatch, and reconnect replaces stale snapshot without requiring re-pair.

- [ ] **Step 2: Run RED**, implement cache/state behavior, run GREEN.

User-facing failure copy: **“I can't reach the house right now.”** Cards remain readable but clearly stale; local rail/settings continue to work.

- [ ] **Step 3: Commit**

```bash
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/DashboardCache.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/TvHomeView.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantSession.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/OfflineDashboardTest.java
git commit -m 'feat: keep BOOP Home useful when HA is offline'
```

### Task 10: CI hardening and first physical BOOP Home acceptance

**Files:**
- Modify: `.github/workflows/build-shield-overlay-poc.yml`
- Modify: `tests/test_shield_overlay_source.py`
- Create: `tests/test_shield_home_source.py`

**Interfaces:** produces one stable-signed `com.boop.shieldoverlay` APK installable over the pairing-gate build.

- [ ] **Step 1: Add source/manifest assertions** for: no accessibility service, no boot receiver, no microphone permission, overlay flags unchanged, HDR display listener unchanged, launcher + Leanback entries present, TV banner present, no sensor dashboard code path, no cloud relay hostname, and no permanent pairing listener.

- [ ] **Step 2: Run all automated verification**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
gradle -p shield-overlay :app:assembleDebug --stacktrace
```

CI must verify the stable signer digest before artifact upload.

- [ ] **Step 3: Install over the stable pairing-gate build without uninstalling** and confirm Android accepts the update. If Android asks for uninstall, stop and fix signing before any functional test.

- [ ] **Step 4: Run physical acceptance**

1. Launch from Shield launcher; little overlay hides and BOOP Home opens.
2. Exit with Back from Home root; previous app returns and little eyes reappear.
3. Map Netflix button in existing Button Mapper to BOOP; launch again from that key.
4. On a fresh-data install, pair by QR with no Shield typing.
5. See **“Found it.”**, then **“Where am I?”**.
6. Pick **Living Room** with one Select press.
7. Confirm chunky left rail, favourites row, and room cards are readable from sofa distance.
8. Press the implemented favourite; confirm only the intended Living Room HA entity changes.
9. Reopen BOOP Home; pairing, room and favourite persist.
10. Disable internet but keep LAN/HA; control still works.
11. Make HA unreachable; cards remain visible as stale, control is disabled, Settings/navigation still work.
12. Restore HA; live state/control returns without re-pairing.
13. Play the already-tested HDR10 material, switch Kodi↔Home/resolution, sleep/wake, and confirm the little overlay remains the known-good behavior whenever BOOP Home is not foreground.
14. Install the same stable-signed APK on the bedroom Shield, pair, choose **Bedroom**, and confirm its room identity is independent.

Do not mark Dolby Vision passed unless a separate physical DV file is actually tested.

- [ ] **Step 5: Commit verification-only corrections**

```bash
git add .github/workflows/build-shield-overlay-poc.yml tests/test_shield_overlay_source.py tests/test_shield_home_source.py
git commit -m 'test: verify first BOOP Shield Home slice'
```

Passing this plan establishes the new known-good Shield Home baseline. Voice, rich controls and routine authoring each get their own later baby-step design/plan.
