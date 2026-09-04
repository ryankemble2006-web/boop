# BOOP Alpha 3 HA Device Identity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make BOOP send every recognised command to Home Assistant unchanged, while Home Assistant uses BOOP Wall's own HA device identity and Living Room area to resolve any exposed actuator automatically.

**Architecture:** BOOP registers once as a native Home Assistant `mobile_app` device named `BOOP Wall`, obtains the resulting Home Assistant device-registry ID through the registration webhook, resolves the HA area named `Living Room`, and assigns itself there through the authenticated HA WebSocket API. Normal commands then remain on the existing `/api/conversation/process` REST endpoint but add `device_id`; BOOP does not rewrite, classify, or retry command sentences.

**Tech Stack:** Android Java 17, minSdk 29 / targetSdk 36, Home Assistant OAuth + REST conversation API, Home Assistant `mobile_app` registration/webhook API, Home Assistant WebSocket API, `org.json`, OkHttp 4.12.0 for the one setup WebSocket, JUnit 4, GitHub Actions Android 16 emulator.

**Spec:** `docs/superpowers/specs/2026-09-04-boop-alpha3-ha-device-identity-design.md`

## Global Constraints

- Package/application ID remains exactly `com.boop.alpha1`.
- Permanent BOOP signing remains mandatory; never add a disposable signing fallback.
- Bump Android metadata to `versionCode 3`, `versionName "0.3.0-alpha3"`.
- Current physical BOOP identity is exactly `BOOP Wall`; current physical area is exactly `Living Room`.
- Raw speech must reach Home Assistant unchanged; no word-order changes, room suffixes, verb normalisation, device/area parsing, or rewritten retry.
- BOOP must not download, cache, or maintain an HA entity/device catalogue.
- Any actuator Home Assistant Assist can control and that the user exposes to Assist is automatically in BOOP scope; no vendor/domain allowlist.
- Sensors/state questions, automation editing, cloud AI, multi-room setup UI, and vendor-specific logic remain out of scope.
- HA success replies `Done.`; unresolved target replies `I can't find that.`; unreachable HA replies `I can't reach the house right now.`.
- Home Assistant remains the source of truth for entity names, aliases, areas, domains, supported actions, and vendor integrations.
- Basic Home Assistant control must remain local/LAN capable and must not depend on OpenAI/cloud AI.

---

## File Structure

### Create
- `source/HomeAssistantConversationRequest.java` — pure request builder for `/api/conversation/process`; guarantees text pass-through and adds `device_id`.
- `source/HomeAssistantDeviceSetupProtocol.java` — pure JSON/URL helpers for mobile-app registration, `get_config`, HA WebSocket auth, area lookup, and device-area assignment messages.
- `source/HomeAssistantDeviceSetup.java` — network orchestration for one-time BOOP registration + area assignment.
- `source-test/HomeAssistantConversationRequestTest.java` — proves arbitrary transcripts are unchanged and `device_id` is present.
- `source-test/HomeAssistantDeviceSetupProtocolTest.java` — proves registration/device-ID parsing, WebSocket URL conversion, Living Room area resolution, and exact device update payloads.

### Modify
- `source/SecureTokenStore.java` — persist BOOP's stable registration client ID and resolved HA device-registry ID; clear them only with the HA connection.
- `source/HomeAssistantClient.java` — require BOOP device ID and use `HomeAssistantConversationRequest`; remove any command-text mutation assumptions.
- `source/MainActivity.java` — remove `RoomContext`; run one-time device setup after an existing/new HA connection; send recognised transcript directly to HA.
- `source/LocalReply.java` — map `NO_TARGET` to `I can't find that.`.
- `source-test/LocalReplyTest.java` — lock the new wording.
- `source/app-build.gradle` — version bump and OkHttp WebSocket dependency.
- `tests/test_alpha2_build_surface.py` — update version expectations and add source-level guards forbidding RoomContext/command rewriting in the active speech path.

### Delete
- `source/RoomContext.java` — old BOOP-side command grammar.
- `source-test/RoomContextTest.java` — tests for behaviour Alpha 3 explicitly forbids.

`source/*.java` and `source-test/*.java` are already copied by `scripts/materialize-android.sh`, so no materializer change is required.

---

### Task 1: Lock raw conversation requests and new target wording

**Files:**
- Create: `source/HomeAssistantConversationRequest.java`
- Create: `source-test/HomeAssistantConversationRequestTest.java`
- Modify: `source/HomeAssistantClient.java`
- Modify: `source/LocalReply.java`
- Modify: `source-test/LocalReplyTest.java`

**Interfaces:**
- Produces: `HomeAssistantConversationRequest.build(String text, String language, String deviceId) -> JSONObject`.
- `HomeAssistantClient` will later consume `SecureTokenStore.getHaDeviceId()` and call this builder.
- `LocalReply.forOutcome(CommandOutcome.noTarget())` must return exactly `I can't find that.`.

- [ ] **Step 1: Write failing raw-pass-through tests**

Create `source-test/HomeAssistantConversationRequestTest.java` with tests that assert exact text preservation, including phrasing BOOP used to rewrite:

```java
package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import org.json.JSONObject;
import org.junit.Test;

public final class HomeAssistantConversationRequestTest {
    @Test public void preservesSwitchPhraseExactly() throws Exception {
        JSONObject body = HomeAssistantConversationRequest.build(
                "switch the fan on", "en-GB", "ha-device-123");
        assertEquals("switch the fan on", body.getString("text"));
        assertEquals("en-GB", body.getString("language"));
        assertEquals("ha-device-123", body.getString("device_id"));
    }

    @Test public void preservesArbitraryFutureDevicePhraseExactly() throws Exception {
        String spoken = "put the new sonoff thing on";
        JSONObject body = HomeAssistantConversationRequest.build(
                spoken, "en-GB", "ha-device-123");
        assertEquals(spoken, body.getString("text"));
    }
}
```

Update `source-test/LocalReplyTest.java` so `NO_TARGET` expects `I can't find that.` rather than `Which room?`.

- [ ] **Step 2: Run the focused tests and verify RED**

Run after materialising:

```bash
./scripts/materialize-android.sh
cd boop-build/BOOP-Alpha1
./gradlew testDebugUnitTest --tests 'com.boop.alpha1.HomeAssistantConversationRequestTest' --tests 'com.boop.alpha1.LocalReplyTest'
```

Expected: failure because `HomeAssistantConversationRequest` does not exist and/or old `NO_TARGET` wording is still `Which room?`.

- [ ] **Step 3: Add the minimal pure request builder**

Create `source/HomeAssistantConversationRequest.java`:

```java
package com.boop.alpha1;

import org.json.JSONObject;

final class HomeAssistantConversationRequest {
    private HomeAssistantConversationRequest() { }

    static JSONObject build(String text, String language, String deviceId) throws Exception {
        return new JSONObject()
                .put("text", text)
                .put("language", language)
                .put("device_id", deviceId);
    }
}
```

Change only the `NO_TARGET` branch in `LocalReply` to:

```java
case NO_TARGET:
    return "I can't find that.";
```

Change `HomeAssistantClient.postConversation(...)` to accept a `deviceId` parameter and build its payload only through:

```java
JSONObject body = HomeAssistantConversationRequest.build(
        text,
        Locale.getDefault().toLanguageTag(),
        deviceId);
```

Do not add any normalisation or fallback logic.

- [ ] **Step 4: Run focused + existing parser/reply tests and verify GREEN**

```bash
./scripts/materialize-android.sh
cd boop-build/BOOP-Alpha1
./gradlew testDebugUnitTest --tests 'com.boop.alpha1.HomeAssistantConversationRequestTest' --tests 'com.boop.alpha1.LocalReplyTest' --tests 'com.boop.alpha1.HomeAssistantResponseParserTest'
```

Expected: PASS.

- [ ] **Step 5: Commit Task 1**

```bash
git add source/HomeAssistantConversationRequest.java source/HomeAssistantClient.java source/LocalReply.java source-test/HomeAssistantConversationRequestTest.java source-test/LocalReplyTest.java
git commit -m "feat: pass raw commands to Home Assistant"
```

---

### Task 2: Persist BOOP's HA device identity and define setup protocol

**Files:**
- Modify: `source/SecureTokenStore.java`
- Create: `source/HomeAssistantDeviceSetupProtocol.java`
- Create: `source-test/HomeAssistantDeviceSetupProtocolTest.java`

**Interfaces:**
- `SecureTokenStore.getOrCreateBoopRegistrationId() -> String` returns a stable random UUID stored in existing private preferences.
- `SecureTokenStore.getHaDeviceId() -> String|null` reads the resolved HA registry ID.
- `SecureTokenStore.saveHaDeviceId(String) -> void` persists that non-secret ID.
- `SecureTokenStore.hasHaDeviceIdentity() -> boolean` is true only when `getHaDeviceId()` is non-empty.
- `HomeAssistantDeviceSetupProtocol` produces/parses the wire messages used by Task 3.

- [ ] **Step 1: Write failing protocol tests**

Create `source-test/HomeAssistantDeviceSetupProtocolTest.java` covering these exact behaviours:

```java
package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public final class HomeAssistantDeviceSetupProtocolTest {
    @Test public void websocketUrlUsesHaWebsocketEndpoint() {
        assertEquals("ws://homeassistant.local:8123/api/websocket",
                HomeAssistantDeviceSetupProtocol.websocketUrl(
                        "http://homeassistant.local:8123"));
        assertEquals("wss://ha.example.test/api/websocket",
                HomeAssistantDeviceSetupProtocol.websocketUrl(
                        "https://ha.example.test/"));
    }

    @Test public void registrationUsesBoopIdentity() throws Exception {
        JSONObject body = HomeAssistantDeviceSetupProtocol.registrationBody(
                "boop-registration-123", "Google", "Pixel 7 Pro", "16");
        assertEquals("com.boop.alpha1", body.getString("app_id"));
        assertEquals("BOOP", body.getString("app_name"));
        assertEquals("0.3.0-alpha3", body.getString("app_version"));
        assertEquals("BOOP Wall", body.getString("device_name"));
        assertEquals("boop-registration-123", body.getString("device_id"));
        assertEquals(false, body.getBoolean("supports_encryption"));
    }

    @Test public void parsesRegistrationWebhookAndHaDeviceId() throws Exception {
        assertEquals("hook-123",
                HomeAssistantDeviceSetupProtocol.parseWebhookId(
                        new JSONObject().put("webhook_id", "hook-123")));
        assertEquals("ha-device-456",
                HomeAssistantDeviceSetupProtocol.parseHaDeviceId(
                        new JSONObject().put("hass_device_id", "ha-device-456")));
    }

    @Test public void resolvesLivingRoomAreaByHumanName() throws Exception {
        JSONArray areas = new JSONArray()
                .put(new JSONObject().put("id", "bedroom").put("name", "Bedroom"))
                .put(new JSONObject().put("id", "living_room").put("name", "Living Room"));
        assertEquals("living_room",
                HomeAssistantDeviceSetupProtocol.findAreaId(areas, "Living Room"));
    }

    @Test public void deviceAreaUpdateTargetsOnlyBoopDevice() throws Exception {
        JSONObject msg = HomeAssistantDeviceSetupProtocol.deviceAreaUpdateMessage(
                2, "ha-device-456", "living_room");
        assertEquals("config/device_registry/update", msg.getString("type"));
        assertEquals("ha-device-456", msg.getString("device_id"));
        assertEquals("living_room", msg.getString("area_id"));
    }
}
```

- [ ] **Step 2: Run the protocol test and verify RED**

```bash
./scripts/materialize-android.sh
cd boop-build/BOOP-Alpha1
./gradlew testDebugUnitTest --tests 'com.boop.alpha1.HomeAssistantDeviceSetupProtocolTest'
```

Expected: compile failure because the protocol class does not exist.

- [ ] **Step 3: Add minimal identity persistence**

Extend `SecureTokenStore` private preferences with:

```java
private static final String PREF_BOOP_REGISTRATION_ID = "boop_registration_id";
private static final String PREF_HA_DEVICE_ID = "ha_device_id";
```

Add:

```java
synchronized String getOrCreateBoopRegistrationId() {
    String existing = prefs.getString(PREF_BOOP_REGISTRATION_ID, null);
    if (existing != null && !existing.isEmpty()) return existing;
    String created = java.util.UUID.randomUUID().toString();
    prefs.edit().putString(PREF_BOOP_REGISTRATION_ID, created).apply();
    return created;
}

String getHaDeviceId() {
    return prefs.getString(PREF_HA_DEVICE_ID, null);
}

void saveHaDeviceId(String deviceId) {
    prefs.edit().putString(PREF_HA_DEVICE_ID, deviceId).apply();
}

boolean hasHaDeviceIdentity() {
    String id = getHaDeviceId();
    return id != null && !id.isEmpty();
}
```

Keep the existing `clear()` semantics: reconnecting/clearing the HA connection also clears these BOOP registration fields.

- [ ] **Step 4: Implement the pure setup protocol**

Create `HomeAssistantDeviceSetupProtocol` with these exact responsibilities:

```java
static JSONObject registrationBody(
        String registrationId, String manufacturer, String model, String osVersion)
```

It returns JSON containing:
- `app_id = com.boop.alpha1`
- `app_name = BOOP`
- `app_version = 0.3.0-alpha3`
- `device_name = BOOP Wall`
- supplied `device_id`, `manufacturer`, `model`, `os_version`
- `os_name = Android`
- `supports_encryption = false`
- empty `app_data` object.

Also implement:

```java
static String parseWebhookId(JSONObject response)
static JSONObject getConfigWebhookBody()
static String parseHaDeviceId(JSONObject response)
static String websocketUrl(String baseUrl)
static JSONObject websocketAuthMessage(String accessToken)
static JSONObject areaListMessage(int id)
static String findAreaId(JSONArray result, String areaName)
static JSONObject deviceAreaUpdateMessage(int id, String deviceId, String areaId)
```

`findAreaId` must compare the HA area's human-readable `name` case-insensitively and return `null` when absent. `websocketUrl` maps `http -> ws`, `https -> wss`, trims a trailing slash, and appends `/api/websocket`.

- [ ] **Step 5: Run protocol + full unit suite and verify GREEN**

```bash
./scripts/materialize-android.sh
cd boop-build/BOOP-Alpha1
./gradlew testDebugUnitTest
```

Expected: all existing and new JVM tests pass.

- [ ] **Step 6: Commit Task 2**

```bash
git add source/SecureTokenStore.java source/HomeAssistantDeviceSetupProtocol.java source-test/HomeAssistantDeviceSetupProtocolTest.java
git commit -m "feat: define BOOP Home Assistant device identity"
```

---

### Task 3: Register BOOP Wall and assign it to Living Room

**Files:**
- Create: `source/HomeAssistantDeviceSetup.java`
- Modify: `source/app-build.gradle`

**Interfaces:**
- Produces: `HomeAssistantDeviceSetup.ensureReady() -> SetupResult`.
- `SetupResult` values: `READY`, `AUTH_REQUIRED`, `UNREACHABLE`, `AREA_NOT_FOUND`, `FORBIDDEN`, `FAILED`.
- Consumes: `SecureTokenStore`, `HomeAssistantAuth`, and `HomeAssistantDeviceSetupProtocol` from Tasks 1–2.
- On `READY`, `SecureTokenStore.getHaDeviceId()` must return the resolved BOOP Wall HA device ID.

- [ ] **Step 1: Add the WebSocket dependency only**

In `source/app-build.gradle`, add:

```groovy
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
```

Do not migrate the existing `HttpURLConnection` REST code to OkHttp; YAGNI. OkHttp exists here only because HA's registry update API is WebSocket-only.

- [ ] **Step 2: Implement mobile-app registration over the existing authenticated HA connection**

Create `HomeAssistantDeviceSetup` with constructor:

```java
HomeAssistantDeviceSetup(SecureTokenStore tokenStore, HomeAssistantAuth auth)
```

and public package method:

```java
SetupResult ensureReady()
```

`ensureReady()` logic:
1. If no base URL: `AUTH_REQUIRED`.
2. If `tokenStore.hasHaDeviceIdentity()`: `READY` without registration or registry writes.
3. Obtain `auth.freshAccessToken()`.
4. POST authenticated JSON to `{baseUrl}/api/mobile_app/registrations` using `HomeAssistantDeviceSetupProtocol.registrationBody(...)`, with `Build.MANUFACTURER`, `Build.MODEL`, `Build.VERSION.RELEASE`, and `tokenStore.getOrCreateBoopRegistrationId()`.
5. Require HTTP 201; 401/403 -> `AUTH_REQUIRED`; network IO -> `UNREACHABLE`; other HTTP -> `FAILED`.
6. Parse returned `webhook_id`.
7. POST unauthenticated JSON to `{baseUrl}/api/webhook/{webhook_id}` with `HomeAssistantDeviceSetupProtocol.getConfigWebhookBody()`.
8. Parse `hass_device_id` from that response but do **not** persist it until area assignment succeeds.

The HTTP helper methods must use 5-second connect/read timeouts, UTF-8 JSON, and close/disconnect resources exactly like the existing HA client/auth code.

- [ ] **Step 3: Implement one bounded HA WebSocket session for area lookup + assignment**

Using OkHttp `WebSocket`, connect to `HomeAssistantDeviceSetupProtocol.websocketUrl(baseUrl)` and process only this state machine:

```text
HA auth_required
 -> send {type:"auth", access_token:...}
HA auth_ok
 -> send id=1 {type:"config/area_registry/list"}
id=1 success
 -> find result[].name == "Living Room"
 -> send id=2 {type:"config/device_registry/update", device_id:<boop>, area_id:<living-room-id>}
id=2 success
 -> complete READY
```

Use a `CountDownLatch` with a 5-second upper bound for the whole setup session. Close the socket after success or terminal error. Map `auth_invalid` to `AUTH_REQUIRED`; an explicit HA error whose code/message indicates unauthorised/admin-required to `FORBIDDEN`; missing Living Room to `AREA_NOT_FOUND`; timeout/network to `UNREACHABLE`; malformed/unexpected protocol to `FAILED`.

On id=2 success only:

```java
tokenStore.saveHaDeviceId(haDeviceId);
```

This ensures a half-created registration cannot make BOOP believe room-aware identity is ready.

- [ ] **Step 4: Add a repeat-call guard test at the protocol/source-regression level**

Extend `tests/test_alpha2_build_surface.py` with source assertions that `HomeAssistantDeviceSetup.ensureReady()` contains an early `hasHaDeviceIdentity()` return before `/api/mobile_app/registrations`, and that the setup source contains `config/area_registry/list` and `config/device_registry/update` while containing no entity-registry list/download operation.

Run:

```bash
python -m unittest tests/test_alpha2_build_surface.py
```

Expected: PASS only when setup is one-time and does not introduce a BOOP entity catalogue.

- [ ] **Step 5: Materialise and run full JVM tests**

```bash
./scripts/materialize-android.sh
cd boop-build/BOOP-Alpha1
./gradlew testDebugUnitTest
```

Expected: PASS and Android source compiles with OkHttp dependency resolution.

- [ ] **Step 6: Commit Task 3**

```bash
git add source/HomeAssistantDeviceSetup.java source/app-build.gradle tests/test_alpha2_build_surface.py
git commit -m "feat: register BOOP Wall with Home Assistant"
```

---

### Task 4: Remove BOOP grammar and integrate one-time identity setup

**Files:**
- Modify: `source/MainActivity.java`
- Modify: `source/HomeAssistantClient.java`
- Delete: `source/RoomContext.java`
- Delete: `source-test/RoomContextTest.java`
- Modify: `source/app-build.gradle`
- Modify: `tests/test_alpha2_build_surface.py`

**Interfaces:**
- `MainActivity.handleRecognizedSpeech(String transcript)` passes `transcript` directly to `HomeAssistantClient.process(transcript)`.
- `HomeAssistantClient.process(String text)` obtains the already-saved HA device ID from `SecureTokenStore`; absence returns `AUTH_REQUIRED` only after MainActivity has tried setup.
- `HomeAssistantDeviceSetup.ensureReady()` is called after OAuth completion and lazily before a command when identity is absent.

- [ ] **Step 1: Write RED source regressions forbidding sentence rewriting**

Update `tests/test_alpha2_build_surface.py` to assert:

```python
main = read("source/MainActivity.java")
assert "RoomContext" not in main
assert "roomContext.qualify" not in main
assert "haClient.process(transcript)" in main
assert not Path("source/RoomContext.java").exists()
assert not Path("source-test/RoomContextTest.java").exists()
```

Also inspect `source/HomeAssistantConversationRequest.java` and assert it contains `.put("text", text)` and `.put("device_id", deviceId)` with no `replace(`, `Pattern.compile`, `Matcher`, or hardcoded `Living Room`.

Run:

```bash
python -m unittest tests/test_alpha2_build_surface.py
```

Expected: RED until the old grammar path is removed.

- [ ] **Step 2: Remove RoomContext and wire raw speech**

In `MainActivity`:
- delete the `RoomContext roomContext` field and constructor call;
- add `HomeAssistantDeviceSetup deviceSetup`;
- construct it from the existing `tokenStore` + `haAuth`;
- in `handleRecognizedSpeech`, do not create any `qualified` string;
- when HA connection exists but device identity is missing, run `deviceSetup.ensureReady()` on the existing executor before sending the command;
- on `READY`, call exactly `haClient.process(transcript)`;
- on setup `AUTH_REQUIRED`, use the existing reconnect path;
- on `UNREACHABLE`, speak `I can't reach the house right now.`;
- on `AREA_NOT_FOUND`/`FORBIDDEN`/`FAILED`, speak one plain setup message such as `I couldn't set my room.` and do not rewrite or send the command.

After OAuth callback succeeds, queue `deviceSetup.ensureReady()` immediately so most users never hit the lazy path on their first command. `House connected.` may still be spoken; setup failure must not cause repeated speech on every tap—track a process-lifetime boolean `setupFailureSpoken` and only announce the setup failure once until a later successful setup or reconnect.

Delete `source/RoomContext.java` and `source-test/RoomContextTest.java`.

- [ ] **Step 3: Make HomeAssistantClient require the saved device ID**

At the start of `HomeAssistantClient.process(String text)`, after base URL validation:

```java
String deviceId = tokenStore.getHaDeviceId();
if (deviceId == null || deviceId.isEmpty()) {
    return CommandOutcome.authRequired();
}
```

Then call:

```java
HomeAssistantResponse response = postConversation(
        baseUrl, accessToken, text, deviceId);
```

No other command text should be created.

- [ ] **Step 4: Bump the installable version**

In `source/app-build.gradle` change only:

```groovy
versionCode 3
versionName "0.3.0-alpha3"
```

Keep `applicationId 'com.boop.alpha1'` and permanent signing configuration unchanged.

Update the build-surface regression's expected version code/name to 3 / `0.3.0-alpha3`.

- [ ] **Step 5: Run source regressions and full JVM suite**

```bash
python -m unittest tests/test_alpha2_build_surface.py
./scripts/materialize-android.sh
cd boop-build/BOOP-Alpha1
./gradlew testDebugUnitTest
```

Expected: all green. Confirm `RoomContext.java` and `RoomContextTest.java` are absent from the materialised project.

- [ ] **Step 6: Commit Task 4**

```bash
git add -A source source-test tests source/app-build.gradle
git commit -m "feat: give BOOP room-aware HA identity"
```

---

### Task 5: CI verification and signed over-install artifact

**Files:**
- No new production files unless CI exposes a real defect.
- Existing workflow: `.github/workflows/build-apk.yml`

**Interfaces:**
- Output artifact is the permanently signed Alpha 3 APK from the existing workflow.
- Physical acceptance occurs only after CI is green and the Pixel installs Alpha 3 over permanent-signed Alpha 2 without uninstalling.

- [ ] **Step 1: Run all local/source checks available in CI**

From repository root:

```bash
python -m unittest tests/test_alpha2_build_surface.py
./scripts/materialize-android.sh
cd boop-build/BOOP-Alpha1
./gradlew testDebugUnitTest
```

Expected: PASS.

- [ ] **Step 2: Push/commit any final verification-only correction and let GitHub Actions run**

Do not weaken or bypass these workflow gates:
- source regression tests
- Android/JVM unit tests
- permanent signing secret guard
- APK build + badging inspection
- Android 16 emulator boot/install/launch/crash capture
- artifact upload only on success.

- [ ] **Step 3: Inspect the completed workflow, not only the green badge**

Verify every required step reports success, especially:
- `Run Android unit tests`
- `Prepare permanent BOOP signing key`
- `Build BOOP APK`
- `Inspect built APK`
- `Launch BOOP and capture crash`
- `Upload BOOP APK`.

If any step fails, use systematic debugging; do not rerun blindly unless the failure is demonstrably transient.

- [ ] **Step 4: Record artifact metadata**

Fetch the workflow artifact metadata and record artifact name, run ID, and digest/size if exposed. Confirm APK inspection reports:
- package `com.boop.alpha1`
- `versionCode=3`
- `versionName=0.3.0-alpha3`.

- [ ] **Step 5: Physical acceptance on Pixel 7 Pro**

The user installs Alpha 3 directly over the currently installed permanent-signed Alpha 2, with **no uninstall**.

Acceptance requires real-device confirmation of all of these:
1. Android accepts the update in-place.
2. Existing HA OAuth connection remains; no forced re-login caused by the update.
3. BOOP Wall appears as a Home Assistant device and is assigned to Living Room.
4. `turn the fan on`, `switch the fan on`, and another natural phrase work without BOOP-side sentence rules.
5. A different exposed actuator can be controlled without adding BOOP code.
6. HA target failure says `I can't find that.` rather than `Which room?`.

Do not claim any physical item above until the user confirms it on the Pixel.

---

## Self-review checklist

- Spec coverage: raw pass-through, device ID, one-time BOOP Wall registration, Living Room assignment, automatic future actuator support, no BOOP device catalogue, failure wording, version bump, CI, and physical acceptance each have an explicit task.
- Placeholder scan: no TBD/TODO/"handle appropriately" steps remain.
- Type consistency: `HomeAssistantConversationRequest.build(text, language, deviceId)`, `SecureTokenStore.getHaDeviceId()/saveHaDeviceId()/hasHaDeviceIdentity()`, and `HomeAssistantDeviceSetup.ensureReady()` are named consistently across tasks.
- Scope check: no sensors, state queries, automations, cloud AI, vendor-specific logic, or multi-room UI were added.
