# BOOP Alpha 2 Local Home Assistant Control Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the Living Room Pixel 7 Pro discover and authenticate to Home Assistant once, then switch the correct Govee `Fan` from BOOP's existing tap-to-speech interaction without any OpenAI/cloud dependency in the control path.

**Architecture:** Home Assistant remains the only authority for house state and actions. BOOP discovers `_home-assistant._tcp.local.` with Android NSD, authenticates with Home Assistant OAuth/IndieAuth through a tiny GitHub Pages callback bridge, encrypts the refresh token with Android Keystore, qualifies unscoped speech with the fixed Alpha 2 home area `Living Room`, and sends the text to `POST /api/conversation/process`. This first plan deliberately ends with plain local acknowledgements; unpredictable cloud puppet wording is a separate follow-up after physical local control is proven.

**Tech Stack:** Java 17, Android platform APIs (minSdk 29, target/compile SDK 36), `NsdManager`, `HttpURLConnection`, Android `org.json`, Android Keystore AES/GCM, Home Assistant OAuth2/IndieAuth, Home Assistant Conversation REST API, GitHub Pages, JUnit 4, Python `unittest`, GitHub Actions Android 16 emulator.

**Spec:** `docs/superpowers/specs/2026-09-04-boop-alpha2-home-assistant-design.md`

## Global Constraints

- BOOP is a puppet, not a fake person or autonomous co-pilot.
- This Pixel is fixed to exactly `Living Room` for Alpha 2; reusable **“Where am I?”** setup is later.
- Home Assistant owns entity exposure, areas, state, intent resolution, and execution.
- If Home Assistant exposes something to Assist, BOOP may control it; BOOP adds no second permissions list.
- Ordinary device controls execute immediately with no confirmation.
- Automation create/change/delete is out of scope here and later requires explicit confirmation.
- WAN/OpenAI loss must not stop LAN Home Assistant control.
- Never commit/embed a Home Assistant access token, refresh token, OpenAI key, or other secret.
- Explicit room wording and explicit multi-target wording always beat the local-room default.
- If the Living Room target is unavailable, BOOP never tries the Bedroom device merely because it has the same name.
- Preserve the working Alpha 1 speech recognizer and `Locale.getDefault().toLanguageTag()` fix.
- Keep Java 17, minSdk 29, compileSdk/targetSdk 36.
- Do not add music ducking, idle/wake animation, finger-follow eyes, camera tracking, wake word, sensors/widgets, cloud reply generation, or **“Where am I?”** in this plan.

## Scope split

The approved design contains two independently testable subsystems: local Home Assistant control/authentication and optional cloud-generated reply flavor. This plan implements the first subsystem completely. Cloud cheekiness gets its own follow-up plan so API-key security and cloud latency cannot block BOOP's first real house action.

## File Map

### Modify
- `.github/workflows/build-apk.yml` — use the editable Alpha 2 source surface, run Python + JVM tests, build/emulator-test, publish `BOOP-Alpha2-debug`.
- `source/MainActivity.java` — preserve eyes/speech; orchestrate discovery/auth and dispatch transcripts to HA off the UI thread.
- `README.md` — document Alpha 2 behavior and physical test.

### Create
- `scripts/materialize-android.sh` — reproducibly unpack ZIP and overlay editable config/main/test sources.
- `source/app-build.gradle` — Android 36 / Java 17 / Alpha 2 module config + JVM test dependencies.
- `source/AndroidManifest.xml` — record audio, internet, Nearby Devices, launcher, BOOP auth callback.
- `source/RoomContext.java` — conservative area qualifier.
- `source/HomeAssistantAuthUrls.java` — stable OAuth client/callback URLs and form builders.
- `source/HomeAssistantDiscovery.java` — local mDNS/NSD discovery.
- `source/SecureTokenStore.java` — Keystore encryption for refresh token.
- `source/HomeAssistantAuth.java` — state validation, code exchange, access-token refresh.
- `source/HomeAssistantResponse.java` — parsed HA result model.
- `source/HomeAssistantResponseParser.java` — HA Conversation API JSON parser.
- `source/CommandOutcome.java` — app-level success/failure model.
- `source/HomeAssistantClient.java` — authenticated Conversation API client and failed-entity availability check.
- `source/LocalReply.java` — deliberately plain local acknowledgement/failure text.
- `source-test/RoomContextTest.java`
- `source-test/HomeAssistantAuthUrlsTest.java`
- `source-test/HomeAssistantResponseParserTest.java`
- `source-test/LocalReplyTest.java`
- `tests/test_alpha2_build_surface.py`
- `web/ha-auth/index.html`
- `web/ha-auth/callback.html`
- `.github/workflows/deploy-ha-auth.yml`

---

### Task 1: Make the Alpha 2 Build Surface Reproducible

**Files:**
- Create: `scripts/materialize-android.sh`
- Create: `source/app-build.gradle`
- Create: `source/AndroidManifest.xml`
- Create: `tests/test_alpha2_build_surface.py`
- Modify: `.github/workflows/build-apk.yml`

**Interfaces:**
- Produces `boop-build/BOOP-Alpha1` with every `source/*.java` overlaid into production source and every `source-test/*.java` overlaid into JVM tests.
- Later tasks run `bash scripts/materialize-android.sh` before Gradle commands.

- [ ] **Step 1: Write the failing repository test**

Create `tests/test_alpha2_build_surface.py`:

```python
import re
import unittest
from pathlib import Path


class Alpha2BuildSurfaceTest(unittest.TestCase):
    def test_materializer_overlays_all_editable_sources(self):
        text = Path('scripts/materialize-android.sh').read_text(encoding='utf-8') if Path('scripts/materialize-android.sh').exists() else ''
        self.assertIn('cp source/*.java', text)
        self.assertIn('cp source/AndroidManifest.xml', text)
        self.assertIn('cp source/app-build.gradle', text)
        self.assertIn('cp source-test/*.java', text)

    def test_manifest_has_network_and_auth_callback(self):
        text = Path('source/AndroidManifest.xml').read_text(encoding='utf-8') if Path('source/AndroidManifest.xml').exists() else ''
        self.assertIn('android.permission.INTERNET', text)
        self.assertIn('android.permission.NEARBY_WIFI_DEVICES', text)
        self.assertIn('android:scheme="boop"', text)
        self.assertIn('android:host="auth-callback"', text)

    def test_build_is_alpha2_android36_java17(self):
        text = Path('source/app-build.gradle').read_text(encoding='utf-8') if Path('source/app-build.gradle').exists() else ''
        self.assertIn('compileSdk 36', text)
        self.assertIn('targetSdk 36', text)
        self.assertIn('minSdk 29', text)
        self.assertIn('versionName "0.2.0-alpha2"', text)
        self.assertIn('JavaVersion.VERSION_17', text)
        self.assertIn("testImplementation 'junit:junit:4.13.2'", text)

    def test_workflow_runs_jvm_tests_and_publishes_alpha2(self):
        text = Path('.github/workflows/build-apk.yml').read_text(encoding='utf-8')
        self.assertIn('bash scripts/materialize-android.sh', text)
        self.assertIn(':app:testDebugUnitTest', text)
        self.assertIn('name: BOOP-Alpha2-debug', text)

    def test_no_literal_credentials_are_committed(self):
        text = '\n'.join(
            p.read_text(encoding='utf-8')
            for root in ('source', 'web')
            if Path(root).exists()
            for p in Path(root).rglob('*')
            if p.is_file() and p.suffix in {'.java', '.xml', '.gradle', '.html', '.js'}
        )
        self.assertIsNone(re.search(r'Bearer\s+[A-Za-z0-9_-]{20,}', text))
        self.assertNotIn('sk-', text)


if __name__ == '__main__':
    unittest.main()
```

- [ ] **Step 2: Verify RED**

```bash
python3 -m unittest tests.test_alpha2_build_surface -v
```

Expected: FAIL because Alpha 2 materializer/config files do not exist.

- [ ] **Step 3: Create the editable app module config**

`source/app-build.gradle`:

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
    testImplementation 'org.json:json:20240303'
}
```

- [ ] **Step 4: Create the editable manifest**

`source/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission
        android:name="android.permission.NEARBY_WIFI_DEVICES"
        android:usesPermissionFlags="neverForLocation" />

    <queries>
        <intent><action android:name="android.speech.RecognitionService" /></intent>
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

- [ ] **Step 5: Create one reproducible materializer**

`scripts/materialize-android.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail
rm -rf boop-build
unzip -q BOOP-Alpha1-project.zip -d boop-build
ROOT=boop-build/BOOP-Alpha1
MAIN="$ROOT/app/src/main/java/com/boop/alpha1"
TEST="$ROOT/app/src/test/java/com/boop/alpha1"
mkdir -p "$MAIN" "$TEST"
cp source/*.java "$MAIN"/
cp source/AndroidManifest.xml "$ROOT/app/src/main/AndroidManifest.xml"
cp source/app-build.gradle "$ROOT/app/build.gradle"
if compgen -G 'source-test/*.java' > /dev/null; then
  cp source-test/*.java "$TEST"/
fi
```

- [ ] **Step 6: Update CI to use the materializer**

In `.github/workflows/build-apk.yml`:
- rename workflow to `Build BOOP Alpha 2 APK`;
- keep Python regressions before unpack/build;
- replace `Unpack BOOP`, single-source overlay, and Android 37→36 `sed` steps with:

```yaml
      - name: Materialize editable BOOP project
        run: bash scripts/materialize-android.sh
```

After Gradle setup add:

```yaml
      - name: Run Android unit tests
        run: gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

Rename uploaded artifact to:

```yaml
          name: BOOP-Alpha2-debug
```

- [ ] **Step 7: Verify GREEN**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
bash scripts/materialize-android.sh
```

Expected: Python tests PASS and materialized tree exists.

- [ ] **Step 8: Commit**

```bash
git add scripts/materialize-android.sh source/app-build.gradle source/AndroidManifest.xml tests/test_alpha2_build_surface.py .github/workflows/build-apk.yml
git commit -m "build: create Alpha 2 editable build surface"
```

---

### Task 2: Add Conservative Living Room Context

**Files:**
- Create: `source/RoomContext.java`
- Create: `source-test/RoomContextTest.java`

**Interfaces:**
- `RoomContext(String homeArea, List<String> knownAreas)`
- `String qualify(String transcript)`

- [ ] **Step 1: Write failing tests**

`source-test/RoomContextTest.java`:

```java
package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import java.util.List;
import org.junit.Test;

public final class RoomContextTest {
    private final RoomContext context = new RoomContext(
            "Living Room", List.of("Living Room", "Bedroom"));

    @Test public void unqualifiedFanGetsLivingRoom() {
        assertEquals("turn on the fan in the Living Room", context.qualify("turn on the fan"));
    }

    @Test public void unqualifiedLightsStillGetLivingRoom() {
        assertEquals("turn on the lights in the Living Room", context.qualify("turn on the lights"));
    }

    @Test public void explicitBedroomWins() {
        assertEquals("turn on the bedroom fan", context.qualify("turn on the bedroom fan"));
    }

    @Test public void explicitLivingRoomIsNotDuplicated() {
        assertEquals("turn off the living room fan", context.qualify("turn off the living room fan"));
    }

    @Test public void bothFansIsNotNarrowed() {
        assertEquals("turn on both fans", context.qualify("turn on both fans"));
    }

    @Test public void allLightsIsNotNarrowed() {
        assertEquals("turn off all lights", context.qualify("turn off all lights"));
    }
}
```

- [ ] **Step 2: Materialize and verify RED**

```bash
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests com.boop.alpha1.RoomContextTest
```

Expected: FAIL because `RoomContext` does not exist.

- [ ] **Step 3: Implement only area qualification**

`source/RoomContext.java`:

```java
package com.boop.alpha1;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

final class RoomContext {
    private static final Pattern EXPLICIT_MULTI = Pattern.compile(
            "\\b(all|both|every|each)\\b", Pattern.CASE_INSENSITIVE);

    private final String homeArea;
    private final List<String> knownAreas;

    RoomContext(String homeArea, List<String> knownAreas) {
        this.homeArea = homeArea;
        this.knownAreas = List.copyOf(knownAreas);
    }

    String qualify(String transcript) {
        String text = transcript == null ? "" : transcript.trim();
        if (text.isEmpty()) return text;
        String lower = text.toLowerCase(Locale.ROOT);
        if (EXPLICIT_MULTI.matcher(lower).find()) return text;
        for (String area : knownAreas) {
            if (lower.contains(area.toLowerCase(Locale.ROOT))) return text;
        }
        return text + " in the " + homeArea;
    }
}
```

This class does not parse device types or invent aliases. For Alpha 2 its known area names are exactly `Living Room` and `Bedroom`.

- [ ] **Step 4: Verify GREEN**

```bash
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests com.boop.alpha1.RoomContextTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add source/RoomContext.java source-test/RoomContextTest.java
git commit -m "feat: add Living Room command context"
```

---

### Task 3: Add a Legitimate Home Assistant OAuth Client Bridge

**Files:**
- Create: `source/HomeAssistantAuthUrls.java`
- Create: `source-test/HomeAssistantAuthUrlsTest.java`
- Create: `web/ha-auth/index.html`
- Create: `web/ha-auth/callback.html`
- Create: `.github/workflows/deploy-ha-auth.yml`

**Interfaces:**
- `HomeAssistantAuthUrls.authorizeUrl(baseUrl, state)`
- `HomeAssistantAuthUrls.authorizationCodeBody(code)`
- `HomeAssistantAuthUrls.refreshBody(refreshToken)`
- Stable client ID: `https://ryankemble2006-web.github.io/boop/ha-auth/`
- Same-origin redirect: `https://ryankemble2006-web.github.io/boop/ha-auth/callback.html`
- App callback: `boop://auth-callback`

- [ ] **Step 1: Write failing OAuth URL tests**

`source-test/HomeAssistantAuthUrlsTest.java`:

```java
package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public final class HomeAssistantAuthUrlsTest {
    @Test public void authorizeUrlUsesBoopWebsiteIdentity() {
        String url = HomeAssistantAuthUrls.authorizeUrl("http://192.168.1.10:8123", "abc123");
        assertTrue(url.startsWith("http://192.168.1.10:8123/auth/authorize?"));
        assertTrue(url.contains("client_id=https%3A%2F%2Fryankemble2006-web.github.io%2Fboop%2Fha-auth%2F"));
        assertTrue(url.contains("redirect_uri=https%3A%2F%2Fryankemble2006-web.github.io%2Fboop%2Fha-auth%2Fcallback.html"));
        assertTrue(url.contains("state=abc123"));
    }

    @Test public void tokenFormsReuseExactClientId() {
        assertTrue(HomeAssistantAuthUrls.authorizationCodeBody("code").contains("client_id=https%3A%2F%2Fryankemble2006-web.github.io%2Fboop%2Fha-auth%2F"));
        assertTrue(HomeAssistantAuthUrls.refreshBody("refresh").contains("client_id=https%3A%2F%2Fryankemble2006-web.github.io%2Fboop%2Fha-auth%2F"));
    }

    @Test public void appCallbackIsStable() {
        assertEquals("boop://auth-callback", HomeAssistantAuthUrls.APP_CALLBACK);
    }
}
```

- [ ] **Step 2: Verify RED**

```bash
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests com.boop.alpha1.HomeAssistantAuthUrlsTest
```

Expected: FAIL because class does not exist.

- [ ] **Step 3: Implement URL/form builder**

`source/HomeAssistantAuthUrls.java`:

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
        return trim(baseUrl) + "/auth/authorize?client_id=" + enc(CLIENT_ID)
                + "&redirect_uri=" + enc(REDIRECT_URI)
                + "&state=" + enc(state);
    }

    static String tokenUrl(String baseUrl) {
        return trim(baseUrl) + "/auth/token";
    }

    static String authorizationCodeBody(String code) {
        return "grant_type=authorization_code&code=" + enc(code)
                + "&client_id=" + enc(CLIENT_ID);
    }

    static String refreshBody(String refreshToken) {
        return "grant_type=refresh_token&refresh_token=" + enc(refreshToken)
                + "&client_id=" + enc(CLIENT_ID);
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String trim(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
```

- [ ] **Step 4: Create the website identity and callback**

`web/ha-auth/index.html`:

```html
<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>BOOP Home Assistant Sign-In</title></head>
<body><main><h1>BOOP</h1><p>This page identifies the BOOP Android app to your Home Assistant.</p></main></body></html>
```

`web/ha-auth/callback.html`:

```html
<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Back to BOOP</title></head>
<body><p>Returning to BOOP…</p><script>window.location.replace('boop://auth-callback' + window.location.search);</script></body></html>
```

Because the OAuth redirect is the same host/port as the website client ID, this follows Home Assistant's documented IndieAuth client rule without impersonating the official companion app.

- [ ] **Step 5: Deploy `web/` with GitHub Pages**

`.github/workflows/deploy-ha-auth.yml`:

```yaml
name: Deploy BOOP auth bridge
on:
  workflow_dispatch:
  push:
    branches: [ "main" ]
    paths: [ "web/**", ".github/workflows/deploy-ha-auth.yml" ]
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

If Pages has never been enabled, enable **Repository Settings → Pages → Source: GitHub Actions** once. Do not create a PAT to automate that setting; it is developer infrastructure, not BOOP end-user setup.

- [ ] **Step 6: Verify GREEN and public URLs**

```bash
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests com.boop.alpha1.HomeAssistantAuthUrlsTest
```

Expected: PASS. After Pages deploy, both URLs return public HTTPS pages:

```text
https://ryankemble2006-web.github.io/boop/ha-auth/
https://ryankemble2006-web.github.io/boop/ha-auth/callback.html
```

- [ ] **Step 7: Commit**

```bash
git add source/HomeAssistantAuthUrls.java source-test/HomeAssistantAuthUrlsTest.java web/ha-auth .github/workflows/deploy-ha-auth.yml
git commit -m "feat: add BOOP Home Assistant OAuth bridge"
```

---

### Task 4: Discover Home Assistant on the Local Network

**Files:**
- Create: `source/HomeAssistantDiscovery.java`
- Modify: `tests/test_alpha2_build_surface.py`

**Interfaces:**

```java
interface HomeAssistantDiscovery.Listener {
    void onFound(String displayName, String baseUrl);
    void onUnavailable(String reason);
}
void start();
void stop();
```

- [ ] **Step 1: Add failing source regression**

Add to `tests/test_alpha2_build_surface.py`:

```python
    def test_discovery_is_local_android_nsd(self):
        p = Path('source/HomeAssistantDiscovery.java')
        text = p.read_text(encoding='utf-8') if p.exists() else ''
        self.assertIn('NsdManager', text)
        self.assertIn('_home-assistant._tcp.', text)
        self.assertNotIn('ui.nabu.casa', text)
```

- [ ] **Step 2: Verify RED**

```bash
python3 -m unittest tests.test_alpha2_build_surface.Alpha2BuildSurfaceTest.test_discovery_is_local_android_nsd -v
```

Expected: FAIL.

- [ ] **Step 3: Implement NSD discovery**

`source/HomeAssistantDiscovery.java` must use:

```java
static final String SERVICE_TYPE = "_home-assistant._tcp.";
```

and `NsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)`.

When a service resolves:
1. prefer UTF-8 TXT attribute `internal_url` when it begins `http://` or `https://`;
2. otherwise use resolved host + advertised port;
3. bracket IPv6 literal hosts;
4. stop discovery after the first valid local instance;
5. never substitute Nabu Casa/cloud discovery.

Core resolution helper:

```java
private static String preferredBaseUrl(NsdServiceInfo info) {
    byte[] internal = info.getAttributes().get("internal_url");
    if (internal != null) {
        String value = new String(internal, StandardCharsets.UTF_8).trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
        }
    }
    InetAddress host = info.getHost();
    if (host == null) return null;
    String address = host.getHostAddress();
    if (host instanceof Inet6Address) address = "[" + address + "]";
    return "http://" + address + ":" + info.getPort();
}
```

- [ ] **Step 4: Verify source regression GREEN**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add source/HomeAssistantDiscovery.java tests/test_alpha2_build_surface.py
git commit -m "feat: discover Home Assistant over local NSD"
```

---

### Task 5: Complete OAuth and Encrypt the Refresh Token

**Files:**
- Create: `source/SecureTokenStore.java`
- Create: `source/HomeAssistantAuth.java`
- Modify: `tests/test_alpha2_build_surface.py`

**Interfaces:**

```java
final class SecureTokenStore {
    void saveConnection(String baseUrl, String refreshToken) throws GeneralSecurityException;
    String getBaseUrl();
    String getRefreshToken() throws GeneralSecurityException;
    boolean hasConnection();
    void clear();
}

final class HomeAssistantAuth {
    String begin(String baseUrl);
    void completeCallback(Uri callback) throws IOException, GeneralSecurityException, AuthRejectedException;
    String freshAccessToken() throws IOException, GeneralSecurityException, AuthRejectedException;
    static final class AuthRejectedException extends Exception { ... }
}
```

- [ ] **Step 1: Add failing secure-storage regression**

Add:

```python
    def test_refresh_token_is_keystore_encrypted(self):
        p = Path('source/SecureTokenStore.java')
        text = p.read_text(encoding='utf-8') if p.exists() else ''
        self.assertIn('AndroidKeyStore', text)
        self.assertIn('AES/GCM/NoPadding', text)
        self.assertNotIn('putString("refresh_token", refreshToken)', text)
```

- [ ] **Step 2: Verify RED**

```bash
python3 -m unittest tests.test_alpha2_build_surface.Alpha2BuildSurfaceTest.test_refresh_token_is_keystore_encrypted -v
```

Expected: FAIL.

- [ ] **Step 3: Implement encrypted token storage**

Use constants exactly:

```java
private static final String KEYSTORE = "AndroidKeyStore";
private static final String KEY_ALIAS = "boop-ha-refresh-v1";
private static final String TRANSFORM = "AES/GCM/NoPadding";
private static final String PREFS = "boop-ha";
private static final String PREF_BASE = "base_url";
private static final String PREF_CIPHER = "refresh_ciphertext";
private static final String PREF_IV = "refresh_iv";
```

Generate the key with:

```java
KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
        KEY_ALIAS,
        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .build();
```

Encrypt refresh token with `Cipher.getInstance(TRANSFORM)`, save Base64 ciphertext + IV only, and decrypt only inside `getRefreshToken()`. `base_url` is non-secret and may be normal SharedPreferences text.

- [ ] **Step 4: Implement OAuth state/code/refresh flow**

`HomeAssistantAuth.begin(baseUrl)`:
- generate 32 random bytes with `SecureRandom`;
- URL-safe Base64 without padding;
- persist pending `state` and `baseUrl` in `boop-ha-auth-pending` preferences;
- return `HomeAssistantAuthUrls.authorizeUrl(baseUrl, state)`.

`completeCallback(Uri callback)`:
- require scheme `boop`, host `auth-callback`;
- read `code` and `state`;
- compare state exactly with pending state;
- POST `HomeAssistantAuthUrls.authorizationCodeBody(code)` to `{baseUrl}/auth/token` as `application/x-www-form-urlencoded`;
- parse `access_token`, `refresh_token`, `expires_in`;
- call `SecureTokenStore.saveConnection(baseUrl, refreshToken)`;
- clear pending state.

`freshAccessToken()`:
- read saved base URL + decrypted refresh token;
- POST `HomeAssistantAuthUrls.refreshBody(refreshToken)` to `/auth/token`;
- return JSON `access_token`;
- on token HTTP 400/403 throw `AuthRejectedException`;
- network exceptions remain `IOException`.

Every token HTTP connection uses 5-second connect/read timeouts. No access token is persisted.

Define the exception fully:

```java
static final class AuthRejectedException extends Exception {
    AuthRejectedException(String message) { super(message); }
}
```

- [ ] **Step 5: Verify regressions GREEN and compile**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:assembleDebug --stacktrace
```

Expected: Python PASS, Java compile/build PASS.

- [ ] **Step 6: Commit**

```bash
git add source/SecureTokenStore.java source/HomeAssistantAuth.java tests/test_alpha2_build_surface.py
git commit -m "feat: secure Home Assistant authentication"
```

---

### Task 6: Parse HA Results and Map Them to Plain BOOP Outcomes

**Files:**
- Create: `source/HomeAssistantResponse.java`
- Create: `source/HomeAssistantResponseParser.java`
- Create: `source/CommandOutcome.java`
- Create: `source/HomeAssistantClient.java`
- Create: `source/LocalReply.java`
- Create: `source-test/HomeAssistantResponseParserTest.java`
- Create: `source-test/LocalReplyTest.java`

**Interfaces:**

```java
HomeAssistantResponse HomeAssistantResponseParser.parse(String json) throws JSONException;
CommandOutcome HomeAssistantClient.process(String text);
String LocalReply.forOutcome(CommandOutcome outcome);
```

`CommandOutcome.Status` is exactly:

```java
SUCCESS, TARGET_OFFLINE, NO_MATCH, NO_TARGET, FAILED, UNREACHABLE, AUTH_REQUIRED
```

- [ ] **Step 1: Write parser tests from current HA schema**

`source-test/HomeAssistantResponseParserTest.java`:

```java
package com.boop.alpha1;

import static org.junit.Assert.*;
import org.junit.Test;

public final class HomeAssistantResponseParserTest {
    @Test public void parsesActionDoneSuccess() throws Exception {
        String json = "{\"response\":{\"response_type\":\"action_done\",\"data\":{\"success\":[{\"name\":\"Fan\",\"type\":\"entity\",\"id\":\"fan.living_room\"}],\"failed\":[]},\"speech\":{\"plain\":{\"speech\":\"Turned on Fan\"}}}}";
        HomeAssistantResponse r = HomeAssistantResponseParser.parse(json);
        assertEquals(HomeAssistantResponse.Kind.ACTION_DONE, r.kind());
        assertEquals("fan.living_room", r.successTargets().get(0).id());
        assertTrue(r.failedTargets().isEmpty());
    }

    @Test public void parsesFailedTarget() throws Exception {
        String json = "{\"response\":{\"response_type\":\"action_done\",\"data\":{\"success\":[],\"failed\":[{\"name\":\"Fan\",\"type\":\"entity\",\"id\":\"fan.living_room\"}]}}}";
        HomeAssistantResponse r = HomeAssistantResponseParser.parse(json);
        assertEquals(1, r.failedTargets().size());
        assertTrue(r.successTargets().isEmpty());
    }

    @Test public void parsesNoValidTargets() throws Exception {
        String json = "{\"response\":{\"response_type\":\"error\",\"data\":{\"code\":\"no_valid_targets\"},\"speech\":{\"plain\":{\"speech\":\"No matching target\"}}}}";
        HomeAssistantResponse r = HomeAssistantResponseParser.parse(json);
        assertEquals(HomeAssistantResponse.Kind.NO_VALID_TARGETS, r.kind());
    }
}
```

- [ ] **Step 2: Write local reply tests**

`source-test/LocalReplyTest.java`:

```java
package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public final class LocalReplyTest {
    @Test public void successIsPlain() {
        assertEquals("Done.", LocalReply.forOutcome(CommandOutcome.success("Fan")));
    }

    @Test public void unreachableIsHouseSpecific() {
        assertEquals("I can't reach the house right now.", LocalReply.forOutcome(CommandOutcome.unreachable()));
    }

    @Test public void offlineFanNamesLocalRoom() {
        assertEquals("The living room fan is offline.", LocalReply.forOutcome(CommandOutcome.targetOffline("Fan", "Living Room")));
    }

    @Test public void noTargetAsksWhichRoom() {
        assertEquals("Which room?", LocalReply.forOutcome(CommandOutcome.noTarget()));
    }
}
```

- [ ] **Step 3: Verify RED**

```bash
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests 'com.boop.alpha1.*'
```

Expected: FAIL because result/client/reply types do not exist.

- [ ] **Step 4: Implement exact parsed response model**

`HomeAssistantResponse.Kind`:

```java
ACTION_DONE, QUERY_ANSWER, NO_INTENT_MATCH, NO_VALID_TARGETS, FAILED_TO_HANDLE, UNKNOWN_ERROR
```

`HomeAssistantResponse.Target` is a normal immutable Java class with `name()`, `type()`, `id()` getters; `id` may be null. `HomeAssistantResponse` exposes immutable `successTargets()`, `failedTargets()`, `speech()`.

Parser mappings:

```text
action_done      -> ACTION_DONE
query_answer     -> QUERY_ANSWER
no_intent_match  -> NO_INTENT_MATCH
no_valid_targets -> NO_VALID_TARGETS
failed_to_handle -> FAILED_TO_HANDLE
anything else    -> UNKNOWN_ERROR
```

Parse `response.data.success[]`, `response.data.failed[]`, and `response.speech.plain.speech`. Missing lists become empty. Never infer success from speech text.

- [ ] **Step 5: Implement exact app outcome model**

`source/CommandOutcome.java` must expose:

```java
final class CommandOutcome {
    enum Status { SUCCESS, TARGET_OFFLINE, NO_MATCH, NO_TARGET, FAILED, UNREACHABLE, AUTH_REQUIRED }
    private final Status status;
    private final String targetName;
    private final String area;

    static CommandOutcome success(String targetName) { return new CommandOutcome(Status.SUCCESS, targetName, null); }
    static CommandOutcome targetOffline(String targetName, String area) { return new CommandOutcome(Status.TARGET_OFFLINE, targetName, area); }
    static CommandOutcome noMatch() { return new CommandOutcome(Status.NO_MATCH, null, null); }
    static CommandOutcome noTarget() { return new CommandOutcome(Status.NO_TARGET, null, null); }
    static CommandOutcome failed() { return new CommandOutcome(Status.FAILED, null, null); }
    static CommandOutcome unreachable() { return new CommandOutcome(Status.UNREACHABLE, null, null); }
    static CommandOutcome authRequired() { return new CommandOutcome(Status.AUTH_REQUIRED, null, null); }
    Status status() { return status; }
    String targetName() { return targetName; }
    String area() { return area; }
    private CommandOutcome(Status status, String targetName, String area) {
        this.status = status; this.targetName = targetName; this.area = area;
    }
}
```

- [ ] **Step 6: Implement authenticated Conversation API client**

`HomeAssistantClient` constructor:

```java
HomeAssistantClient(SecureTokenStore store, HomeAssistantAuth auth, String homeArea)
```

`process(text)` does:
1. `auth.freshAccessToken()`;
2. POST `{baseUrl}/api/conversation/process` with `Authorization: Bearer <access>`;
3. JSON body:

```json
{"text":"turn on the fan in the Living Room","language":"en-GB"}
```

4. 5-second connect/read timeouts;
5. parse HTTP 200 response;
6. `IOException` => `UNREACHABLE`;
7. `AuthRejectedException` => `AUTH_REQUIRED`.

Map parsed response:

```text
ACTION_DONE + success non-empty + failed empty -> SUCCESS
NO_INTENT_MATCH                               -> NO_MATCH
NO_VALID_TARGETS                              -> NO_TARGET
FAILED_TO_HANDLE / UNKNOWN_ERROR              -> FAILED
```

For `ACTION_DONE` with failed targets: if a failed target has `type == "entity"` and non-empty `id`, GET `{baseUrl}/api/states/{encoded-id}` with the same token. If returned JSON has `state == "unavailable"`, return `targetOffline(target.name(), homeArea)`. Otherwise return `FAILED`.

Never issue a second command toward Bedroom after a failed Living Room action.

- [ ] **Step 7: Implement deliberately plain local reply mapping**

`source/LocalReply.java`:

```java
package com.boop.alpha1;

import java.util.Locale;

final class LocalReply {
    private LocalReply() { }

    static String forOutcome(CommandOutcome outcome) {
        return switch (outcome.status()) {
            case SUCCESS -> "Done.";
            case TARGET_OFFLINE -> "The " + outcome.area().toLowerCase(Locale.ROOT)
                    + " " + outcome.targetName().toLowerCase(Locale.ROOT) + " is offline.";
            case NO_MATCH -> "I didn't understand that.";
            case NO_TARGET -> "Which room?";
            case FAILED -> "That didn't work.";
            case UNREACHABLE -> "I can't reach the house right now.";
            case AUTH_REQUIRED -> "I need to reconnect to the house.";
        };
    }
}
```

- [ ] **Step 8: Verify GREEN**

```bash
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

Expected: all JVM tests PASS.

- [ ] **Step 9: Commit**

```bash
git add source/HomeAssistantResponse.java source/HomeAssistantResponseParser.java source/CommandOutcome.java source/HomeAssistantClient.java source/LocalReply.java source-test/HomeAssistantResponseParserTest.java source-test/LocalReplyTest.java
git commit -m "feat: add local Home Assistant command client"
```

---

### Task 7: Wire Discovery/Auth/HA Control into the Existing Puppet Activity

**Files:**
- Modify: `source/MainActivity.java`
- Modify: `tests/test_alpha2_build_surface.py`
- Modify: `README.md`

**Interfaces:**
- Existing speech result becomes input to `RoomContext.qualify()` then `HomeAssistantClient.process()`.
- Network/auth work runs on one `ExecutorService`; UI/TTS updates return via `runOnUiThread`.

- [ ] **Step 1: Add failing integration source assertions**

Add:

```python
    def test_main_routes_speech_to_ha_off_ui_thread(self):
        text = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('ExecutorService', text)
        self.assertIn('RoomContext', text)
        self.assertIn('HomeAssistantClient', text)
        self.assertIn('LocalReply.forOutcome', text)
        self.assertNotIn('speak("You said, " + best)', text)

    def test_bcp47_speech_fix_is_preserved(self):
        text = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('Locale.getDefault().toLanguageTag()', text)
```

- [ ] **Step 2: Verify RED without breaking old speech regression**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
```

Expected: new HA routing assertion FAIL; existing speech regression PASS.

- [ ] **Step 3: Add fixed Alpha 2 room identity and service fields**

Use exactly:

```java
private static final int REQ_NEARBY_WIFI = 1002;
private static final String HOME_AREA = "Living Room";
private final RoomContext roomContext = new RoomContext(HOME_AREA, List.of("Living Room", "Bedroom"));
private final ExecutorService houseExecutor = Executors.newSingleThreadExecutor();
```

Initialize `SecureTokenStore`, `HomeAssistantAuth`, `HomeAssistantClient` after `setContentView`.

- [ ] **Step 4: Gate first discovery with the Android Nearby Devices permission**

If no stored HA connection exists, call:

```java
private void ensureHouseConnection() {
    if (tokenStore.hasConnection()) return;
    if (Build.VERSION.SDK_INT >= 33
            && checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(new String[]{Manifest.permission.NEARBY_WIFI_DEVICES}, REQ_NEARBY_WIFI);
        return;
    }
    startHomeAssistantDiscovery();
}
```

When permission is granted, immediately start discovery. If denied, speak `"I can't find the house without nearby-device permission."` once; do not re-prompt in a loop.

- [ ] **Step 5: Show the one minimal discovery prompt**

On `onFound(displayName, baseUrl)`:

```java
runOnUiThread(() -> new AlertDialog.Builder(this)
        .setTitle("I found your house")
        .setMessage(displayName)
        .setPositiveButton("Connect", (d, w) -> beginHomeAssistantAuth(baseUrl))
        .setNegativeButton("Not now", null)
        .show());
```

No URL entry, token entry, wizard, or **“Where am I?”** is added.

- [ ] **Step 6: Launch Home Assistant auth and handle the BOOP callback**

`beginHomeAssistantAuth(baseUrl)`:

```java
String authorizeUrl = homeAssistantAuth.begin(baseUrl);
startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authorizeUrl)));
```

Handle callback in both `onCreate` and `onNewIntent`:

```java
private void handleAuthIntent(Intent intent) {
    Uri data = intent == null ? null : intent.getData();
    if (data == null || !"boop".equals(data.getScheme()) || !"auth-callback".equals(data.getHost())) return;
    houseExecutor.execute(() -> {
        try {
            homeAssistantAuth.completeCallback(data);
            runOnUiThread(() -> speak("House connected."));
        } catch (Exception e) {
            runOnUiThread(() -> speak("That didn't connect."));
        }
    });
}
```

- [ ] **Step 7: Replace Alpha 1 echo with the HA command dispatch**

Keep existing best-transcript extraction. Replace `speak("You said, " + best)` with:

```java
private void handleRecognizedSpeech(String transcript) {
    if (!tokenStore.hasConnection()) {
        ensureHouseConnection();
        return;
    }
    String command = roomContext.qualify(transcript);
    houseExecutor.execute(() -> {
        CommandOutcome outcome = homeAssistantClient.process(command);
        String reply = LocalReply.forOutcome(outcome);
        runOnUiThread(() -> speak(reply));
    });
}
```

The action itself must never call OpenAI.

- [ ] **Step 8: Preserve lifecycle cleanup**

Before existing recognizer/TTS destruction completes:

```java
if (homeAssistantDiscovery != null) homeAssistantDiscovery.stop();
houseExecutor.shutdownNow();
```

- [ ] **Step 9: Update README**

Document these exact Alpha 2 truths:
- first device = BOOP Wall / Living Room;
- first-use path = Nearby Devices permission if Android asks → **“I found your house”** → HA sign-in;
- no **“Where am I?”** yet;
- unqualified fan/lights get Living Room context;
- explicit Bedroom wins;
- WAN/OpenAI loss does not stop LAN HA control;
- first success wording is intentionally plain; cloud puppet wording comes after the real hardware test.

- [ ] **Step 10: Verify all local gates**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest :app:assembleDebug --stacktrace
```

Expected: Python PASS, JVM PASS, APK build PASS.

- [ ] **Step 11: Commit**

```bash
git add source/MainActivity.java tests/test_alpha2_build_surface.py README.md
git commit -m "feat: route BOOP speech into the house"
```

---

### Task 8: CI, Emulator, and Physical Govee Acceptance

**Files:**
- Modify only when fresh evidence identifies a specific defect; each defect gets its own failing test first.

**Interfaces:**
- Produces current `BOOP-Alpha2-debug` artifact and physical evidence that room-safe local HA control works.

- [ ] **Step 1: Verify the GitHub Pages auth bridge**

Required public URLs:

```text
https://ryankemble2006-web.github.io/boop/ha-auth/
https://ryankemble2006-web.github.io/boop/ha-auth/callback.html
```

If deployment fails solely because Pages is disabled, enable Repository Settings → Pages → GitHub Actions once and rerun. Do not create a PAT.

- [ ] **Step 2: Verify the full APK workflow**

Required green steps:

```text
Run source regression tests
Materialize editable BOOP project
Run Android unit tests
Build BOOP APK
Inspect built APK
Boot clean Android 16 emulator
Launch BOOP and capture crash
Upload BOOP APK
```

Do not call the build green while any required step is pending, skipped, or failed.

- [ ] **Step 3: Inspect APK badging**

Expected:

```text
package: com.boop.alpha1
versionName: 0.2.0-alpha2
min sdk: 29
target sdk: 36
launchable activity: com.boop.alpha1.MainActivity
```

- [ ] **Step 4: Install the exact current `BOOP-Alpha2-debug` artifact over Alpha 1 on Pixel 7 Pro**

Expected: package identity remains stable and the eyes still launch normally.

- [ ] **Step 5: Complete one-time HA connection**

Expected physical flow:

```text
Android may ask Nearby Devices once.
BOOP finds local HA.
“I found your house” appears.
Tap Connect.
Normal Home Assistant authorization opens.
Authorize once.
Browser callback returns to BOOP.
BOOP says “House connected.”
No token copy/paste.
No “Where am I?” prompt.
```

- [ ] **Step 6: Prove Living Room default**

Say:

```text
turn on the fan
```

Expected: only Govee `Fan` in `Living Room` turns on; Bedroom fan does not move; BOOP gives plain local success acknowledgement.

Then:

```text
turn off the fan
```

Expected: only Living Room fan turns off.

- [ ] **Step 7: Prove explicit Bedroom override**

Say:

```text
turn on the bedroom fan
```

Expected: Bedroom fan turns on; Living Room default does not rewrite it.

- [ ] **Step 8: Prove local-first WAN-off behavior**

Remove WAN/internet while preserving Pixel Wi-Fi and LAN access to HA. Say:

```text
turn on the fan
```

Expected: Living Room fan still switches and BOOP still gives a plain local reply.

- [ ] **Step 9: Prove HA-unreachable behavior**

Make HA unreachable but leave BOOP running. Say:

```text
turn off the fan
```

Expected exactly:

```text
I can't reach the house right now.
```

No success claim and no Bedroom fallback.

- [ ] **Step 10: Prove local target failure never spills into Bedroom**

Restore HA; make only Living Room `Fan` unavailable; say:

```text
turn on the fan
```

If HA returns a failed entity target whose state is `unavailable`, expected:

```text
The living room fan is offline.
```

If HA only returns a non-entity failure that cannot be state-checked, expected fallback is:

```text
That didn't work.
```

Either outcome is safe only if Bedroom fan remains unchanged. Capture the real HA JSON before refining wording later; never guess another target.

- [ ] **Step 11: Run verification-before-completion**

Before declaring this milestone complete, freshly verify:

```text
Python regressions green
JVM tests green
Gradle assembleDebug green
APK badging correct
clean Android 16 emulator launch survives
Pages auth bridge reachable
physical OAuth completes
Living Room fan on/off works
explicit Bedroom override works
WAN-off LAN control works
HA-unreachable wording works
no wrong-room fallback when local target fails
```

- [ ] **Step 12: Keep follow-up feel work separate**

After this passes, create a separate cloud-reply plan. It may ask OpenAI for one fresh short puppet line only **after** HA has succeeded, with `LocalReply` as the immediate fallback. Never embed an OpenAI key in the APK.
