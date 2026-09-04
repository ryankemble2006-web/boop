# BOOP Alpha 2 Local Home Assistant Control Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Living Room Pixel 7 Pro discover and authenticate to Home Assistant once, then turn the correct Govee `Fan` on/off from BOOP's existing tap-to-speech interaction without any OpenAI/cloud dependency in the control path.

**Architecture:** Keep Home Assistant as the only house authority. BOOP discovers `_home-assistant._tcp.local.` with Android NSD, uses Home Assistant OAuth/IndieAuth through a tiny GitHub Pages callback bridge, stores the refresh token with Android Keystore, qualifies unscoped commands with the fixed Alpha 2 home area `Living Room`, and sends text to `POST /api/conversation/process`. The first slice intentionally uses plain local success replies; cloud-generated cheeky acknowledgements are a separate follow-up after physical device control is proven.

**Tech Stack:** Java 17, Android platform APIs (API 29+), `NsdManager`, `HttpURLConnection`, `org.json`, Android Keystore AES/GCM, Home Assistant OAuth2/IndieAuth, Home Assistant Conversation REST API, GitHub Pages, JUnit 4, existing Python source-regression tests, GitHub Actions Android 16 emulator.

**Spec:** `docs/superpowers/specs/2026-09-04-boop-alpha2-home-assistant-design.md`

## Global Constraints

- BOOP is a puppet, not a fake person or autonomous co-pilot.
- Pixel 7 Pro Alpha 2 home area is fixed to exactly `Living Room`; reusable **“Where am I?”** area selection is deferred.
- Home Assistant is the authority for entity exposure, areas, state, and execution.
- If an entity is exposed to Home Assistant Assist, BOOP may control it; BOOP adds no second permissions list.
- Ordinary device controls execute immediately with no confirmation.
- Automation create/change/delete is out of scope for this plan and later requires explicit confirmation.
- OpenAI/cloud services must not be required to switch a device.
- No Home Assistant access token, refresh token, OpenAI key, or other credential may be committed to the repository or embedded in the APK.
- Explicit room wording and multi-target wording always beat BOOP's local room default.
- If the Living Room target is unavailable, BOOP must not silently try the Bedroom device with the same name.
- Existing Alpha 1 speech behavior and the BCP-47 language-tag fix must remain intact.
- Target/compile SDK for CI remains Android 36; minSdk remains 29; Java remains 17.
- This plan does not implement music ducking, idle/wake animation, finger-follow eyes, camera tracking, wake word, sensor UI, widgets, or cloud-generated success wording.

## Scope split

The approved design contains two independently testable subsystems: (1) local Home Assistant control and authentication, and (2) optional OpenAI-generated reply flavor after successful actions. This plan implements subsystem (1) completely and ends with a physical Govee acceptance test. Subsystem (2) gets its own follow-up plan after the house-control path is proven, so cloud security and latency cannot block the first real BOOP action.

## File map

### Existing files modified

- `.github/workflows/build-apk.yml` — overlay all editable Alpha 2 Java/config/test sources into the unpacked project, run unit tests, build and publish `BOOP-Alpha2-debug`.
- `source/MainActivity.java` — preserve face/speech behavior; add one-time HA discovery/auth orchestration and route successful speech transcripts into Home Assistant on a background executor.
- `README.md` — document Alpha 2 architecture, one-time auth bridge, and physical test flow.
- `tests/test_speech_recognizer_mode.py` — keep unchanged unless source layout assertions need path adaptation; the existing speech regression remains mandatory.

### New Android source/config files

- `source/app-build.gradle` — authoritative editable app module config: Android 36, Java 17, Alpha 2 version, JUnit test dependencies.
- `source/AndroidManifest.xml` — permissions and BOOP auth callback deep link while preserving launcher/record-audio declarations.
- `source/RoomContext.java` — pure-Java conservative home-area qualification.
- `source/HomeAssistantAuthUrls.java` — pure-Java OAuth authorize/token URL/form construction and constants.
- `source/HomeAssistantDiscovery.java` — Android NSD browse/resolve of Home Assistant on the LAN.
- `source/SecureTokenStore.java` — Android Keystore-backed encrypted refresh-token storage plus non-secret HA base URL storage.
- `source/HomeAssistantAuth.java` — OAuth state lifecycle, browser launch data, authorization-code exchange, refresh-token flow.
- `source/HomeAssistantResponse.java` — small immutable result model for action/query/error outcomes.
- `source/HomeAssistantResponseParser.java` — pure-Java parser for Home Assistant Conversation API responses.
- `source/HomeAssistantClient.java` — authenticated REST calls to `/api/conversation/process` and entity state checks for failed entity targets.
- `source/LocalReply.java` — deliberately plain local success/failure wording.

### New JVM tests

- `source-test/RoomContextTest.java`
- `source-test/HomeAssistantAuthUrlsTest.java`
- `source-test/HomeAssistantResponseParserTest.java`
- `source-test/LocalReplyTest.java`

### New repository regression tests

- `tests/test_alpha2_build_surface.py` — ensures the workflow overlays all Alpha 2 files, manifest has required permissions/deep link, artifact name is Alpha 2, and no secret-looking literal is introduced.

### New auth-bridge files

- `web/ha-auth/index.html` — stable Home Assistant OAuth client ID page at `https://ryankemble2006-web.github.io/boop/ha-auth/`.
- `web/ha-auth/callback.html` — same-origin OAuth redirect that immediately deep-links the returned `code` and `state` to `boop://auth-callback`.
- `.github/workflows/deploy-ha-auth.yml` — deploys `web/` to GitHub Pages.

---

### Task 1: Make Alpha 2 Sources and JVM Tests First-Class Build Inputs

**Files:**
- Create: `source/app-build.gradle`
- Create: `source/AndroidManifest.xml`
- Create: `tests/test_alpha2_build_surface.py`
- Modify: `.github/workflows/build-apk.yml`

**Interfaces:**
- Consumes: existing unpacked project at `boop-build/BOOP-Alpha1`.
- Produces: workflow convention that every `source/*.java` file is copied to `app/src/main/java/com/boop/alpha1/`, every `source-test/*.java` file is copied to `app/src/test/java/com/boop/alpha1/`, and editable manifest/build.gradle replace the copies inside the project ZIP.

- [ ] **Step 1: Write the failing repository regression test**

Create `tests/test_alpha2_build_surface.py`:

```python
import re
import unittest
from pathlib import Path

WORKFLOW = Path('.github/workflows/build-apk.yml').read_text(encoding='utf-8')
MANIFEST = Path('source/AndroidManifest.xml').read_text(encoding='utf-8') if Path('source/AndroidManifest.xml').exists() else ''
BUILD = Path('source/app-build.gradle').read_text(encoding='utf-8') if Path('source/app-build.gradle').exists() else ''


class Alpha2BuildSurfaceTest(unittest.TestCase):
    def test_workflow_overlays_all_editable_sources_and_unit_tests(self):
        self.assertIn('cp source/*.java', WORKFLOW)
        self.assertIn('cp source/AndroidManifest.xml', WORKFLOW)
        self.assertIn('cp source/app-build.gradle', WORKFLOW)
        self.assertIn('cp source-test/*.java', WORKFLOW)
        self.assertIn(':app:testDebugUnitTest', WORKFLOW)

    def test_manifest_declares_alpha2_network_and_callback_surface(self):
        self.assertIn('android.permission.INTERNET', MANIFEST)
        self.assertIn('android.permission.NEARBY_WIFI_DEVICES', MANIFEST)
        self.assertIn('android:scheme="boop"', MANIFEST)
        self.assertIn('android:host="auth-callback"', MANIFEST)

    def test_build_is_alpha2_android36_java17(self):
        self.assertIn('compileSdk 36', BUILD)
        self.assertIn('targetSdk 36', BUILD)
        self.assertIn('minSdk 29', BUILD)
        self.assertIn('versionName "0.2.0-alpha2"', BUILD)
        self.assertIn('JavaVersion.VERSION_17', BUILD)
        self.assertIn("testImplementation 'junit:junit:4.13.2'", BUILD)

    def test_workflow_publishes_alpha2_artifact(self):
        self.assertIn('name: BOOP-Alpha2-debug', WORKFLOW)

    def test_no_literal_long_lived_credentials_are_committed(self):
        text = '\n'.join(
            p.read_text(encoding='utf-8')
            for root in ('source', 'web')
            if Path(root).exists()
            for p in Path(root).rglob('*')
            if p.is_file() and p.suffix in {'.java', '.xml', '.gradle', '.html', '.js'}
        )
        self.assertIsNone(re.search(r'Bearer\\s+[A-Za-z0-9_-]{20,}', text))
        self.assertNotIn('sk-', text)


if __name__ == '__main__':
    unittest.main()
```

- [ ] **Step 2: Run the repository test and verify RED**

Run:

```bash
python3 -m unittest tests.test_alpha2_build_surface -v
```

Expected: FAIL because `source/AndroidManifest.xml`, `source/app-build.gradle`, source-test overlay, and Alpha 2 artifact wiring do not exist yet.

- [ ] **Step 3: Create the editable Alpha 2 app build file**

Create `source/app-build.gradle`:

```gradle
plugins {
    id 'com.android.application'
}

android {
    namespace 'com.boop.alpha1'
    compileSdk 36

    defaultConfig {
        applicationId 'com.boop.alpha1'
        minSdk 29
        targetSdk 36
        versionCode 2
        versionName "0.2.0-alpha2"
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.json:json:20250517'
}
```

- [ ] **Step 4: Create the editable Alpha 2 manifest**

Create `source/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission
        android:name="android.permission.NEARBY_WIFI_DEVICES"
        android:usesPermissionFlags="neverForLocation" />

    <queries>
        <intent>
            <action android:name="android.speech.RecognitionService" />
        </intent>
    </queries>

    <application
        android:allowBackup="false"
        android:icon="@drawable/boop_eyes"
        android:label="BOOP"
        android:supportsRtl="true"
        android:theme="@style/Theme.BOOP">
        <activity
            android:name=".MainActivity"
            android:configChanges="keyboardHidden|orientation|screenSize"
            android:exported="true"
            android:launchMode="singleTask"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="boop" android:host="auth-callback" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 5: Update the workflow overlay and test order**

Replace the single-file copy step in `.github/workflows/build-apk.yml` with:

```yaml
      - name: Apply editable BOOP source
        run: |
          MAIN=boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1
          TEST=boop-build/BOOP-Alpha1/app/src/test/java/com/boop/alpha1
          mkdir -p "$MAIN" "$TEST"
          cp source/*.java "$MAIN"/
          cp source/AndroidManifest.xml boop-build/BOOP-Alpha1/app/src/main/AndroidManifest.xml
          cp source/app-build.gradle boop-build/BOOP-Alpha1/app/build.gradle
          if compgen -G 'source-test/*.java' > /dev/null; then cp source-test/*.java "$TEST"/; fi
```

After Gradle setup and before `assembleDebug`, add:

```yaml
      - name: Run Android unit tests
        run: gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

Remove the `sed`-based Android 37→36 step because the editable app build file already pins 36. Rename workflow title to `Build BOOP Alpha 2 APK` and artifact to `BOOP-Alpha2-debug`.

- [ ] **Step 6: Run repository regression suite GREEN**

Run:

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
```

Expected: existing speech recognizer test PASS and new Alpha 2 surface tests PASS.

- [ ] **Step 7: Commit**

```bash
git add .github/workflows/build-apk.yml source/app-build.gradle source/AndroidManifest.xml tests/test_alpha2_build_surface.py
git commit -m "build: make Alpha 2 sources first-class"
```

---

### Task 2: Add Conservative Living Room Context

**Files:**
- Create: `source/RoomContext.java`
- Create: `source-test/RoomContextTest.java`

**Interfaces:**
- Consumes: raw speech transcript `String`, fixed home area `String`, known local areas `List<String>`.
- Produces: `RoomContext.qualify(String transcript)` returning the text to send to Home Assistant.

- [ ] **Step 1: Write the failing room-context unit tests**

Create `source-test/RoomContextTest.java`:

```java
package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Test;

public final class RoomContextTest {
    private final RoomContext context = new RoomContext(
            "Living Room",
            List.of("Living Room", "Bedroom")
    );

    @Test public void unqualifiedFanGetsLivingRoom() {
        assertEquals(
                "turn on the fan in the Living Room",
                context.qualify("turn on the fan")
        );
    }

    @Test public void explicitBedroomWins() {
        assertEquals(
                "turn on the bedroom fan",
                context.qualify("turn on the bedroom fan")
        );
    }

    @Test public void explicitLivingRoomIsNotDuplicated() {
        assertEquals(
                "turn off the living room fan",
                context.qualify("turn off the living room fan")
        );
    }

    @Test public void bothFansIsNotNarrowed() {
        assertEquals("turn on both fans", context.qualify("turn on both fans"));
    }

    @Test public void allLightsIsNotNarrowed() {
        assertEquals("turn off all lights", context.qualify("turn off all lights"));
    }
}
```

- [ ] **Step 2: Run the unit test and verify RED**

Run:

```bash
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests com.boop.alpha1.RoomContextTest
```

Expected: FAIL because `RoomContext` does not exist.

- [ ] **Step 3: Implement the smallest conservative qualifier**

Create `source/RoomContext.java`:

```java
package com.boop.alpha1;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

final class RoomContext {
    private static final Pattern MULTI_TARGET = Pattern.compile(
            "\\b(all|both|every|each|fans|lights)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private final String homeArea;
    private final List<String> knownAreas;

    RoomContext(String homeArea, List<String> knownAreas) {
        this.homeArea = homeArea;
        this.knownAreas = List.copyOf(knownAreas);
    }

    String qualify(String transcript) {
        String text = transcript == null ? "" : transcript.trim();
        if (text.isEmpty()) {
            return text;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        if (MULTI_TARGET.matcher(lower).find()) {
            return text;
        }
        for (String area : knownAreas) {
            if (lower.contains(area.toLowerCase(Locale.ROOT))) {
                return text;
            }
        }
        return text + " in the " + homeArea;
    }
}
```

- [ ] **Step 4: Run room-context tests GREEN**

Run:

```bash
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests com.boop.alpha1.RoomContextTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add source/RoomContext.java source-test/RoomContextTest.java
git commit -m "feat: add BOOP home room context"
```

---

### Task 3: Add Home Assistant OAuth Client and Callback Bridge

**Files:**
- Create: `source/HomeAssistantAuthUrls.java`
- Create: `source-test/HomeAssistantAuthUrlsTest.java`
- Create: `web/ha-auth/index.html`
- Create: `web/ha-auth/callback.html`
- Create: `.github/workflows/deploy-ha-auth.yml`

**Interfaces:**
- Consumes: discovered Home Assistant base URL and random OAuth `state`.
- Produces: `HomeAssistantAuthUrls.authorizeUrl(String baseUrl, String state)` and stable constants `CLIENT_ID`, `REDIRECT_URI`; public same-origin callback page that forwards `code` and `state` to `boop://auth-callback`.

The fixed URLs for this repo are:

```text
CLIENT_ID    = https://ryankemble2006-web.github.io/boop/ha-auth/
REDIRECT_URI = https://ryankemble2006-web.github.io/boop/ha-auth/callback.html
```

Home Assistant requires the OAuth client ID to be a website URL. A redirect on the same host/port as that client ID is valid without pretending to be the official Home Assistant app. The callback page is infrastructure, not an end-user setup wizard.

- [ ] **Step 1: Write failing OAuth URL tests**

Create `source-test/HomeAssistantAuthUrlsTest.java`:

```java
package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class HomeAssistantAuthUrlsTest {
    @Test public void authorizeUrlUsesBoopClientAndCallback() {
        String url = HomeAssistantAuthUrls.authorizeUrl(
                "http://192.168.1.10:8123",
                "state-123"
        );
        assertTrue(url.startsWith("http://192.168.1.10:8123/auth/authorize?"));
        assertTrue(url.contains("client_id=https%3A%2F%2Fryankemble2006-web.github.io%2Fboop%2Fha-auth%2F"));
        assertTrue(url.contains("redirect_uri=https%3A%2F%2Fryankemble2006-web.github.io%2Fboop%2Fha-auth%2Fcallback.html"));
        assertTrue(url.contains("state=state-123"));
    }

    @Test public void tokenBodiesReuseExactClientId() {
        assertTrue(HomeAssistantAuthUrls.authorizationCodeBody("abc")
                .contains("client_id=https%3A%2F%2Fryankemble2006-web.github.io%2Fboop%2Fha-auth%2F"));
        assertTrue(HomeAssistantAuthUrls.refreshBody("refresh")
                .contains("client_id=https%3A%2F%2Fryankemble2006-web.github.io%2Fboop%2Fha-auth%2F"));
    }

    @Test public void callbackSchemeIsStable() {
        assertEquals("boop://auth-callback", HomeAssistantAuthUrls.APP_CALLBACK);
    }
}
```

- [ ] **Step 2: Run tests RED**

Run:

```bash
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests com.boop.alpha1.HomeAssistantAuthUrlsTest
```

Expected: FAIL because `HomeAssistantAuthUrls` does not exist.

- [ ] **Step 3: Implement OAuth URL/form construction**

Create `source/HomeAssistantAuthUrls.java`:

```java
package com.boop.alpha1;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class HomeAssistantAuthUrls {
    static final String CLIENT_ID = "https://ryankemble2006-web.github.io/boop/ha-auth/";
    static final String REDIRECT_URI = "https://ryankemble2006-web.github.io/boop/ha-auth/callback.html";
    static final String APP_CALLBACK = "boop://auth-callback";

    private HomeAssistantAuthUrls() { }

    static String authorizeUrl(String baseUrl, String state) {
        return trimSlash(baseUrl) + "/auth/authorize"
                + "?client_id=" + enc(CLIENT_ID)
                + "&redirect_uri=" + enc(REDIRECT_URI)
                + "&state=" + enc(state);
    }

    static String authorizationCodeBody(String code) {
        return "grant_type=authorization_code"
                + "&code=" + enc(code)
                + "&client_id=" + enc(CLIENT_ID);
    }

    static String refreshBody(String refreshToken) {
        return "grant_type=refresh_token"
                + "&refresh_token=" + enc(refreshToken)
                + "&client_id=" + enc(CLIENT_ID);
    }

    static String tokenUrl(String baseUrl) {
        return trimSlash(baseUrl) + "/auth/token";
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String trimSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
```

- [ ] **Step 4: Create the stable OAuth client page**

Create `web/ha-auth/index.html`:

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>BOOP Home Assistant Sign-In</title>
</head>
<body>
  <main>
    <h1>BOOP</h1>
    <p>This page identifies the BOOP Android app to your Home Assistant.</p>
  </main>
</body>
</html>
```

Create `web/ha-auth/callback.html`:

```html
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Back to BOOP</title>
</head>
<body>
  <p>Returning to BOOP…</p>
  <script>
    const target = 'boop://auth-callback' + window.location.search;
    window.location.replace(target);
  </script>
  <noscript>JavaScript is required to return to BOOP after Home Assistant sign-in.</noscript>
</body>
</html>
```

- [ ] **Step 5: Add GitHub Pages deployment workflow**

Create `.github/workflows/deploy-ha-auth.yml`:

```yaml
name: Deploy BOOP auth bridge

on:
  workflow_dispatch:
  push:
    branches: [ "main" ]
    paths:
      - "web/**"
      - ".github/workflows/deploy-ha-auth.yml"

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: boop-pages
  cancel-in-progress: true

jobs:
  deploy:
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/configure-pages@v5
      - uses: actions/upload-pages-artifact@v4
        with:
          path: web
      - id: deployment
        uses: actions/deploy-pages@v4
```

If GitHub reports that Pages is not yet enabled for the repository, enable **Settings → Pages → Source: GitHub Actions** once. `actions/configure-pages` cannot auto-enable a repository with the default `GITHUB_TOKEN`; do not add a PAT merely to hide this one-time developer setting.

- [ ] **Step 6: Run OAuth URL tests GREEN**

Run:

```bash
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests com.boop.alpha1.HomeAssistantAuthUrlsTest
```

Expected: PASS.

- [ ] **Step 7: Verify the deployed client and callback pages are HTTPS-reachable**

After the Pages workflow succeeds, verify both return HTTP 200:

```text
https://ryankemble2006-web.github.io/boop/ha-auth/
https://ryankemble2006-web.github.io/boop/ha-auth/callback.html
```

Expected: both pages load without redirects to GitHub login.

- [ ] **Step 8: Commit**

```bash
git add source/HomeAssistantAuthUrls.java source-test/HomeAssistantAuthUrlsTest.java web/ha-auth .github/workflows/deploy-ha-auth.yml
git commit -m "feat: add BOOP Home Assistant auth bridge"
```

---

### Task 4: Discover Home Assistant on the LAN

**Files:**
- Create: `source/HomeAssistantDiscovery.java`
- Modify: `source/MainActivity.java`

**Interfaces:**
- Produces: `HomeAssistantDiscovery.Listener.onFound(String displayName, String baseUrl)` and `.onUnavailable(String reason)`.
- MainActivity calls `start()` only when no saved Home Assistant base URL/refresh token exists.

Home Assistant advertises `_home-assistant._tcp.local.`. Android `NsdManager.discoverServices` uses service type `_home-assistant._tcp.`. Prefer the `internal_url` TXT attribute when present; otherwise construct a local HTTP URL from resolved host + advertised port.

- [ ] **Step 1: Add a failing source regression assertion for NSD and local permission handling**

Extend `tests/test_alpha2_build_surface.py` with:

```python
    def test_discovery_uses_android_nsd_not_cloud_lookup(self):
        source = Path('source/HomeAssistantDiscovery.java').read_text(encoding='utf-8') if Path('source/HomeAssistantDiscovery.java').exists() else ''
        self.assertIn('NsdManager', source)
        self.assertIn('_home-assistant._tcp.', source)
        self.assertNotIn('ui.nabu.casa', source)
```

- [ ] **Step 2: Run the regression test RED**

Run:

```bash
python3 -m unittest tests.test_alpha2_build_surface.Alpha2BuildSurfaceTest.test_discovery_uses_android_nsd_not_cloud_lookup -v
```

Expected: FAIL because discovery source does not exist.

- [ ] **Step 3: Implement the NSD discovery wrapper**

Create `source/HomeAssistantDiscovery.java` with this public contract and behavior:

```java
package com.boop.alpha1;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

final class HomeAssistantDiscovery {
    interface Listener {
        void onFound(String displayName, String baseUrl);
        void onUnavailable(String reason);
    }

    static final String SERVICE_TYPE = "_home-assistant._tcp.";

    private final NsdManager nsd;
    private final Listener listener;
    private boolean stopped;

    HomeAssistantDiscovery(Context context, Listener listener) {
        this.nsd = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        this.listener = listener;
    }

    void start() {
        stopped = false;
        nsd.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    void stop() {
        if (stopped) return;
        stopped = true;
        try { nsd.stopServiceDiscovery(discoveryListener); } catch (IllegalArgumentException ignored) { }
    }

    private final NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
        @Override public void onDiscoveryStarted(String serviceType) { }
        @Override public void onServiceLost(NsdServiceInfo serviceInfo) { }
        @Override public void onDiscoveryStopped(String serviceType) { }
        @Override public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            stop();
            listener.onUnavailable("discovery failed " + errorCode);
        }
        @Override public void onStopDiscoveryFailed(String serviceType, int errorCode) { }
        @Override public void onServiceFound(NsdServiceInfo serviceInfo) {
            nsd.resolveService(serviceInfo, resolveListener);
        }
    };

    private final NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
        @Override public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) { }

        @Override public void onServiceResolved(NsdServiceInfo serviceInfo) {
            String baseUrl = preferredBaseUrl(serviceInfo);
            if (baseUrl == null) return;
            stop();
            listener.onFound(serviceInfo.getServiceName(), baseUrl);
        }
    };

    private static String preferredBaseUrl(NsdServiceInfo info) {
        Map<String, byte[]> attributes = info.getAttributes();
        byte[] internal = attributes.get("internal_url");
        if (internal != null) {
            String value = new String(internal, StandardCharsets.UTF_8).trim();
            if (value.startsWith("http://") || value.startsWith("https://")) return trimSlash(value);
        }
        InetAddress host = info.getHost();
        if (host == null) return null;
        String address = host.getHostAddress();
        if (host instanceof Inet6Address) address = "[" + address + "]";
        return "http://" + address + ":" + info.getPort();
    }

    private static String trimSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
```

Do not add Nabu Casa/cloud discovery fallback in this task.

- [ ] **Step 4: Add one-time Nearby Devices permission gate in MainActivity**

Add a request code:

```java
private static final int REQ_NEARBY_WIFI = 1002;
```

Before NSD on Android 13+:

```java
if (Build.VERSION.SDK_INT >= 33
        && checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
    requestPermissions(new String[]{Manifest.permission.NEARBY_WIFI_DEVICES}, REQ_NEARBY_WIFI);
    return;
}
startHomeAssistantDiscovery();
```

In `onRequestPermissionsResult`, when `REQ_NEARBY_WIFI` is granted, immediately call `startHomeAssistantDiscovery()`. If denied, speak a plain local line such as `"I can't find the house without nearby-device permission."` and do not loop the prompt.

- [ ] **Step 5: Run source regression GREEN**

Run:

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add source/HomeAssistantDiscovery.java source/MainActivity.java tests/test_alpha2_build_surface.py
git commit -m "feat: discover Home Assistant on the LAN"
```

---

### Task 5: Store OAuth Refresh Tokens Securely and Complete Sign-In

**Files:**
- Create: `source/SecureTokenStore.java`
- Create: `source/HomeAssistantAuth.java`
- Modify: `source/MainActivity.java`

**Interfaces:**
- `SecureTokenStore.saveConnection(String baseUrl, String refreshToken)`
- `SecureTokenStore.getBaseUrl(): String|null`
- `SecureTokenStore.getRefreshToken(): String|null`
- `SecureTokenStore.clear()`
- `HomeAssistantAuth.begin(String baseUrl): String` returns browser authorize URL and persists pending OAuth state/base URL.
- `HomeAssistantAuth.handleCallback(Uri callback): Tokens` validates state, exchanges code, stores refresh token, and returns short-lived access token.
- `HomeAssistantAuth.refreshAccessToken(): String` obtains a fresh access token using stored refresh token.

- [ ] **Step 1: Add failing source regression checks for secure token storage**

Extend `tests/test_alpha2_build_surface.py`:

```python
    def test_refresh_token_is_keystore_encrypted(self):
        source = Path('source/SecureTokenStore.java').read_text(encoding='utf-8') if Path('source/SecureTokenStore.java').exists() else ''
        self.assertIn('AndroidKeyStore', source)
        self.assertIn('AES/GCM/NoPadding', source)
        self.assertNotIn('putString("refresh_token", refreshToken)', source)
```

- [ ] **Step 2: Run regression RED**

Run:

```bash
python3 -m unittest tests.test_alpha2_build_surface.Alpha2BuildSurfaceTest.test_refresh_token_is_keystore_encrypted -v
```

Expected: FAIL because token store does not exist.

- [ ] **Step 3: Implement `SecureTokenStore` with Android Keystore AES/GCM**

Use constants exactly:

```java
private static final String KEYSTORE = "AndroidKeyStore";
private static final String KEY_ALIAS = "boop-ha-refresh-v1";
private static final String TRANSFORM = "AES/GCM/NoPadding";
private static final String PREFS = "boop-ha";
private static final String PREF_BASE_URL = "base_url";
private static final String PREF_TOKEN_CIPHERTEXT = "refresh_ciphertext";
private static final String PREF_TOKEN_IV = "refresh_iv";
```

Key generation must use:

```java
KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
        KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .build();
```

Encryption must save only Base64 IV + ciphertext in `SharedPreferences`; plaintext refresh tokens must exist only in method-local memory. `clear()` removes prefs but leaves/reuses the app-specific Keystore key.

- [ ] **Step 4: Implement `HomeAssistantAuth` state and token exchange**

Use `SecureRandom` to generate 32 random bytes and URL-safe Base64 state without padding:

```java
byte[] random = new byte[32];
new SecureRandom().nextBytes(random);
String state = Base64.encodeToString(random, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
```

Persist only pending `state` and `baseUrl` in a separate `boop-ha-auth-pending` preferences file so Android process death during browser auth does not lose the validation context.

Authorization-code exchange:

```text
POST {baseUrl}/auth/token
Content-Type: application/x-www-form-urlencoded
body = HomeAssistantAuthUrls.authorizationCodeBody(code)
```

Parse JSON keys `access_token`, `refresh_token`, `expires_in`; store only the refresh token persistently. Refresh flow posts `HomeAssistantAuthUrls.refreshBody(refreshToken)` and returns the new short-lived access token.

Use `HttpURLConnection` with connect/read timeout 5 seconds for token calls. Any callback whose `state` does not exactly equal the pending state must be rejected and must not alter stored credentials.

- [ ] **Step 5: Wire discovery → “I found your house” → browser auth → deep-link callback**

In `MainActivity`, when discovery returns a base URL, show one `AlertDialog`:

```java
new AlertDialog.Builder(this)
        .setTitle("I found your house")
        .setMessage(displayName)
        .setPositiveButton("Connect", (dialog, which) -> beginHomeAssistantAuth(baseUrl))
        .setNegativeButton("Not now", null)
        .show();
```

`beginHomeAssistantAuth` calls `HomeAssistantAuth.begin(baseUrl)` then launches:

```java
startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authorizeUrl)));
```

Handle `boop://auth-callback` in both `onCreate(getIntent())` and `onNewIntent(Intent intent)`. Token exchange runs on a background executor; on success, return to normal eyes and speak a short local confirmation such as `"House connected."` once.

- [ ] **Step 6: Run all repository regressions GREEN and build**

Run:

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
gradle -p boop-build/BOOP-Alpha1 :app:assembleDebug :app:testDebugUnitTest --stacktrace
```

Expected: all tests PASS and APK builds.

- [ ] **Step 7: Commit**

```bash
git add source/SecureTokenStore.java source/HomeAssistantAuth.java source/MainActivity.java tests/test_alpha2_build_surface.py
git commit -m "feat: authenticate BOOP to Home Assistant"
```

---

### Task 6: Parse Home Assistant Conversation Results Before Wiring Real Actions

**Files:**
- Create: `source/HomeAssistantResponse.java`
- Create: `source/HomeAssistantResponseParser.java`
- Create: `source-test/HomeAssistantResponseParserTest.java`

**Interfaces:**
- `HomeAssistantResponseParser.parse(String json): HomeAssistantResponse`
- `HomeAssistantResponse.Kind`: `ACTION_DONE`, `QUERY_ANSWER`, `NO_INTENT_MATCH`, `NO_VALID_TARGETS`, `FAILED_TO_HANDLE`, `UNKNOWN_ERROR`
- `HomeAssistantResponse.successTargets(): List<Target>`
- `HomeAssistantResponse.failedTargets(): List<Target>`
- `HomeAssistantResponse.speech(): String`
- `Target(name, type, id)` where `id` may be null.

- [ ] **Step 1: Write parser tests from the current documented HA response schema**

Create `source-test/HomeAssistantResponseParserTest.java`:

```java
package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class HomeAssistantResponseParserTest {
    @Test public void parsesSuccessfulActionTargets() throws Exception {
        String json = "{\"response\":{\"response_type\":\"action_done\",\"data\":{\"success\":[{\"name\":\"Fan\",\"type\":\"entity\",\"id\":\"fan.living_room\"}],\"failed\":[]},\"speech\":{\"plain\":{\"speech\":\"Turned on Fan\"}}}}";
        HomeAssistantResponse result = HomeAssistantResponseParser.parse(json);
        assertEquals(HomeAssistantResponse.Kind.ACTION_DONE, result.kind());
        assertEquals(1, result.successTargets().size());
        assertEquals("fan.living_room", result.successTargets().get(0).id());
        assertTrue(result.failedTargets().isEmpty());
    }

    @Test public void parsesFailedActionTargetWithoutClaimingSuccess() throws Exception {
        String json = "{\"response\":{\"response_type\":\"action_done\",\"data\":{\"success\":[],\"failed\":[{\"name\":\"Fan\",\"type\":\"entity\",\"id\":\"fan.living_room\"}]}}}";
        HomeAssistantResponse result = HomeAssistantResponseParser.parse(json);
        assertTrue(result.successTargets().isEmpty());
        assertEquals(1, result.failedTargets().size());
    }

    @Test public void parsesNoValidTargetsError() throws Exception {
        String json = "{\"response\":{\"response_type\":\"error\",\"data\":{\"code\":\"no_valid_targets\"},\"speech\":{\"plain\":{\"speech\":\"No matching target\"}}}}";
        HomeAssistantResponse result = HomeAssistantResponseParser.parse(json);
        assertEquals(HomeAssistantResponse.Kind.NO_VALID_TARGETS, result.kind());
        assertEquals("No matching target", result.speech());
    }
}
```

- [ ] **Step 2: Run parser tests RED**

Run:

```bash
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests com.boop.alpha1.HomeAssistantResponseParserTest
```

Expected: FAIL because response classes do not exist.

- [ ] **Step 3: Implement immutable response model**

`source/HomeAssistantResponse.java` must define:

```java
package com.boop.alpha1;

import java.util.List;

final class HomeAssistantResponse {
    enum Kind {
        ACTION_DONE,
        QUERY_ANSWER,
        NO_INTENT_MATCH,
        NO_VALID_TARGETS,
        FAILED_TO_HANDLE,
        UNKNOWN_ERROR
    }

    record Target(String name, String type, String id) { }

    private final Kind kind;
    private final List<Target> successTargets;
    private final List<Target> failedTargets;
    private final String speech;

    HomeAssistantResponse(Kind kind, List<Target> successTargets, List<Target> failedTargets, String speech) {
        this.kind = kind;
        this.successTargets = List.copyOf(successTargets);
        this.failedTargets = List.copyOf(failedTargets);
        this.speech = speech == null ? "" : speech;
    }

    Kind kind() { return kind; }
    List<Target> successTargets() { return successTargets; }
    List<Target> failedTargets() { return failedTargets; }
    String speech() { return speech; }
}
```

- [ ] **Step 4: Implement parser with exact HA codes**

`HomeAssistantResponseParser.parse` must map:

```text
action_done      -> ACTION_DONE
query_answer     -> QUERY_ANSWER
no_intent_match  -> NO_INTENT_MATCH
no_valid_targets -> NO_VALID_TARGETS
failed_to_handle -> FAILED_TO_HANDLE
anything else    -> UNKNOWN_ERROR
```

Extract `response.data.success[]`, `response.data.failed[]`, and `response.speech.plain.speech` when present. Missing arrays become empty lists. Do not infer success from speech text.

- [ ] **Step 5: Run parser tests GREEN**

Run:

```bash
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests com.boop.alpha1.HomeAssistantResponseParserTest
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add source/HomeAssistantResponse.java source/HomeAssistantResponseParser.java source-test/HomeAssistantResponseParserTest.java
git commit -m "feat: parse Home Assistant conversation results"
```

---

### Task 7: Add Authenticated Home Assistant Conversation Client

**Files:**
- Create: `source/HomeAssistantClient.java`
- Create: `source/LocalReply.java`
- Create: `source-test/LocalReplyTest.java`

**Interfaces:**
- `HomeAssistantClient.process(String text): CommandOutcome`
- `CommandOutcome.status`: `SUCCESS`, `TARGET_OFFLINE`, `NO_MATCH`, `NO_TARGET`, `FAILED`, `UNREACHABLE`, `AUTH_REQUIRED`
- `CommandOutcome.targetName`: nullable display target.
- `LocalReply.forOutcome(CommandOutcome outcome): String`

- [ ] **Step 1: Write failing local-reply tests**

Create `source-test/LocalReplyTest.java`:

```java
package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class LocalReplyTest {
    @Test public void successIsDeliberatelyPlain() {
        assertEquals("Done.", LocalReply.forOutcome(CommandOutcome.success("Fan")));
    }

    @Test public void houseUnreachableIsSpecific() {
        assertEquals(
                "I can't reach the house right now.",
                LocalReply.forOutcome(CommandOutcome.unreachable())
        );
    }

    @Test public void offlineLivingRoomTargetDoesNotSuggestAnotherRoom() {
        assertEquals(
                "The living room fan is offline.",
                LocalReply.forOutcome(CommandOutcome.targetOffline("Fan", "Living Room"))
        );
    }

    @Test public void noTargetAsksForRoom() {
        assertEquals("Which room?", LocalReply.forOutcome(CommandOutcome.noTarget()));
    }
}
```

- [ ] **Step 2: Run reply tests RED**

Run:

```bash
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests com.boop.alpha1.LocalReplyTest
```

Expected: FAIL because client/outcome/reply classes do not exist.

- [ ] **Step 3: Implement `CommandOutcome` as a package-private immutable type inside `HomeAssistantClient.java`**

Required factories:

```java
static CommandOutcome success(String targetName)
static CommandOutcome targetOffline(String targetName, String area)
static CommandOutcome noMatch()
static CommandOutcome noTarget()
static CommandOutcome failed()
static CommandOutcome unreachable()
static CommandOutcome authRequired()
```

- [ ] **Step 4: Implement conversation POST and refresh-on-401**

`HomeAssistantClient.process` must:

1. call `HomeAssistantAuth.refreshAccessToken()` before the command;
2. POST JSON to `{baseUrl}/api/conversation/process`:

```json
{
  "text": "turn on the fan in the Living Room",
  "language": "en-GB"
}
```

3. send `Authorization: Bearer <short-lived access token>` and `Content-Type: application/json`;
4. use 5-second connect and read timeouts;
5. parse HTTP 200 with `HomeAssistantResponseParser`;
6. return `UNREACHABLE` on `IOException`/socket timeout;
7. return `AUTH_REQUIRED` if token refresh fails because stored credentials are invalid/revoked;
8. never call OpenAI or a Nabu Casa/cloud URL.

- [ ] **Step 5: Map HA action/error results conservatively**

Rules:

```text
ACTION_DONE + success non-empty + failed empty -> SUCCESS
ACTION_DONE + any failed target              -> inspect failed entity state if possible
NO_INTENT_MATCH                              -> NO_MATCH
NO_VALID_TARGETS                             -> NO_TARGET
FAILED_TO_HANDLE / UNKNOWN_ERROR             -> FAILED
QUERY_ANSWER                                 -> SUCCESS for this plumbing layer; preserve HA speech for later sensor/query support
```

For a failed target with `type == "entity"` and a non-empty `id`, GET `{baseUrl}/api/states/{urlencoded-id}` with the same bearer token. If JSON `state` is exactly `"unavailable"`, return `TARGET_OFFLINE` using target name and `Living Room`. Otherwise return `FAILED`.

Critically, do not issue a second conversation command aimed at Bedroom when the Living Room target fails.

- [ ] **Step 6: Implement plain local reply mapping**

`source/LocalReply.java`:

```java
package com.boop.alpha1;

final class LocalReply {
    private LocalReply() { }

    static String forOutcome(CommandOutcome outcome) {
        return switch (outcome.status()) {
            case SUCCESS -> "Done.";
            case TARGET_OFFLINE -> "The "
                    + outcome.area().toLowerCase()
                    + " " + outcome.targetName().toLowerCase()
                    + " is offline.";
            case NO_MATCH -> "I didn't understand that.";
            case NO_TARGET -> "Which room?";
            case UNREACHABLE -> "I can't reach the house right now.";
            case AUTH_REQUIRED -> "I need to reconnect to the house.";
            case FAILED -> "That didn't work.";
        };
    }
}
```

- [ ] **Step 7: Run reply/unit suite GREEN**

Run:

```bash
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

Expected: all JVM tests PASS.

- [ ] **Step 8: Commit**

```bash
git add source/HomeAssistantClient.java source/LocalReply.java source-test/LocalReplyTest.java
git commit -m "feat: send BOOP commands to Home Assistant"
```

---

### Task 8: Route Alpha 1 Speech into the Real House-Control Path

**Files:**
- Modify: `source/MainActivity.java`
- Modify: `README.md`

**Interfaces:**
- Consumes: existing `onResults(Bundle results)` best speech transcript.
- Uses: `RoomContext`, `SecureTokenStore`, `HomeAssistantAuth`, `HomeAssistantClient`, `LocalReply`.
- Produces: tap → speech → room-qualified HA conversation request → real action → spoken local result.

- [ ] **Step 1: Write failing source regression checks for background networking and preserved speech behavior**

Extend `tests/test_alpha2_build_surface.py`:

```python
    def test_main_activity_routes_transcript_to_ha_off_main_thread(self):
        source = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('ExecutorService', source)
        self.assertIn('RoomContext', source)
        self.assertIn('HomeAssistantClient', source)
        self.assertIn('LocalReply.forOutcome', source)
        self.assertNotIn('speak("You said, " + best)', source)

    def test_existing_bcp47_speech_language_remains(self):
        source = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('Locale.getDefault().toLanguageTag()', source)
```

- [ ] **Step 2: Run source regression RED**

Run:

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
```

Expected: new integration assertion FAIL while existing speech tests remain PASS.

- [ ] **Step 3: Add the Alpha 2 room and background executor fields**

Use exactly:

```java
private static final String HOME_AREA = "Living Room";
private final RoomContext roomContext = new RoomContext(
        HOME_AREA,
        List.of("Living Room", "Bedroom")
);
private final ExecutorService houseExecutor = Executors.newSingleThreadExecutor();
```

Initialize `SecureTokenStore`, `HomeAssistantAuth`, and `HomeAssistantClient` in `onCreate` after the activity content view exists.

- [ ] **Step 4: Replace Alpha 1 transcript echo with HA dispatch**

In `onResults`, keep the existing extraction of `best`, then call:

```java
private void handleRecognizedSpeech(String transcript) {
    String command = roomContext.qualify(transcript);
    houseExecutor.execute(() -> {
        CommandOutcome outcome = homeAssistantClient.process(command);
        String reply = LocalReply.forOutcome(outcome);
        runOnUiThread(() -> speak(reply));
    });
}
```

If no saved connection exists when a transcript arrives, trigger discovery/auth instead of trying a network command. Do not drop the transcript into a cloud service.

- [ ] **Step 5: Preserve lifecycle cleanup**

In `onDestroy`:

```java
if (homeAssistantDiscovery != null) {
    homeAssistantDiscovery.stop();
}
houseExecutor.shutdownNow();
```

Keep existing `SpeechRecognizer.destroy()` and TTS shutdown logic.

- [ ] **Step 6: Update README to Alpha 2**

Document exactly:

```text
- Alpha 2 first device is BOOP Wall / Living Room.
- One-time flow: “I found your house” -> Home Assistant sign-in.
- “Where am I?” is intentionally deferred; this build is fixed to Living Room.
- “turn on the fan” is qualified to Living Room.
- “turn on the bedroom fan” stays Bedroom.
- WAN/OpenAI loss must not stop LAN Home Assistant control.
- Success wording is deliberately plain in this first control slice.
- Cloud puppet wording comes only after physical local control is proven.
```

- [ ] **Step 7: Run full local verification**

Run:

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest :app:assembleDebug --stacktrace
```

Expected: all tests PASS; APK builds.

- [ ] **Step 8: Commit**

```bash
git add source/MainActivity.java README.md tests/test_alpha2_build_surface.py
git commit -m "feat: route BOOP speech into Home Assistant"
```

---

### Task 9: CI/Emulator Verification and Physical Govee Acceptance

**Files:**
- Modify only if CI exposes an actual defect: the smallest file responsible for that defect.

**Interfaces:**
- Consumes: main branch Alpha 2 build.
- Produces: `BOOP-Alpha2-debug` artifact that survives a clean Android 16 emulator launch and then passes the physical Pixel/Govee test.

- [ ] **Step 1: Push and inspect the GitHub Actions run**

Required successful steps:

```text
Run source regression tests
Apply editable BOOP source
Run Android unit tests
Build BOOP APK
Inspect built APK
Boot clean Android 16 emulator
Launch BOOP and capture crash
Upload BOOP APK
```

Do not call the build green while any required step is pending/skipped/failed.

- [ ] **Step 2: Inspect APK badging**

Expected:

```text
package name: com.boop.alpha1
versionName: 0.2.0-alpha2
sdkVersion >= 29
targetSdkVersion: 36
launchable activity: com.boop.alpha1.MainActivity
```

- [ ] **Step 3: Verify GitHub Pages auth bridge before installing**

Expected HTTP pages:

```text
https://ryankemble2006-web.github.io/boop/ha-auth/
https://ryankemble2006-web.github.io/boop/ha-auth/callback.html
```

If the Pages workflow fails only because Pages has never been enabled, enable repository Pages with source `GitHub Actions` once and rerun. Do not create/store a PAT for this.

- [ ] **Step 4: Install the exact current `BOOP-Alpha2-debug` artifact on Pixel 7 Pro**

Install over the existing Alpha 1 build so package identity remains stable.

- [ ] **Step 5: Complete the one-time Home Assistant connection**

Expected physical flow:

```text
BOOP requests Nearby Devices permission once if Android requires it.
BOOP finds local Home Assistant.
BOOP shows “I found your house”.
Tap Connect.
Home Assistant normal sign-in/authorization opens.
After approval, callback returns to BOOP.
BOOP says “House connected.”
No token is copied or pasted.
No “Where am I?” prompt appears in this build.
```

- [ ] **Step 6: Prove Living Room duplicate-name handling**

On the Living Room Pixel say:

```text
turn on the fan
```

Expected: only Govee `Fan` in `Living Room` turns on. Bedroom `Fan` stays unchanged. BOOP gives the plain local success reply.

Then say:

```text
turn off the fan
```

Expected: only Living Room fan turns off.

- [ ] **Step 7: Prove explicit room override**

Say:

```text
turn on the bedroom fan
```

Expected: Bedroom `Fan` turns on; Living Room default does not overwrite the explicit room.

- [ ] **Step 8: Prove local-first operation by removing WAN but keeping LAN/HA**

Disable internet/WAN while keeping Pixel Wi-Fi and Home Assistant reachable on LAN. Repeat:

```text
turn on the fan
```

Expected: Living Room fan still switches. BOOP still replies locally. No OpenAI/cloud call is necessary.

- [ ] **Step 9: Prove Home Assistant unreachable handling**

Make Home Assistant unreachable while BOOP stays on Wi-Fi. Say:

```text
turn off the fan
```

Expected exactly:

```text
I can't reach the house right now.
```

No success claim and no Bedroom fallback.

- [ ] **Step 10: Prove unavailable Living Room fan does not spill into Bedroom**

Restore HA, make only Living Room `Fan` unavailable, then say:

```text
turn on the fan
```

Expected when HA returns a failed entity target whose state is `unavailable`:

```text
The living room fan is offline.
```

Bedroom fan must remain unchanged. If HA's Conversation API returns only a non-entity failed target and therefore cannot identify the entity state, BOOP must fall back to `That didn't work.` — still never try Bedroom. Capture that actual HA response for a later precision improvement rather than guessing.

- [ ] **Step 11: Use verification-before-completion before declaring Alpha 2 local control complete**

Fresh evidence required:

```text
all Python regressions green
all JVM tests green
Gradle assembleDebug green
APK badging correct
clean Android 16 emulator process alive after launch
Pages auth bridge reachable
physical Pixel OAuth completes
Living Room fan on/off works
Bedroom explicit override works
WAN-off LAN control works
HA-unreachable message works
no wrong-room fallback on unavailable target
```

- [ ] **Step 12: Commit only evidence-driven fixes discovered by the physical test**

Each defect gets its own minimal test-first commit. Do not bundle music ducking, eye animation, cloud reply personality, or “Where am I?” into the control milestone.

---

## Follow-up plan after Task 9 passes

Create a separate plan for **cloud puppet replies**. That plan may generate a fresh short one-liner only after Home Assistant reports success, while retaining `LocalReply` as the immediate fallback. It must design a credential-safe OpenAI path; an OpenAI API key must never be embedded in the APK. This follow-up is where BOOP becomes unpredictable online and deliberately plainer offline.
