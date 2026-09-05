# BOOP Shield Local Pairing Gate Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prove the LAN-only BOOP Wall → Shield → Home Assistant authorization handoff with a permanent development signing identity, 120-second QR-pinned TLS pairing, and the Shield storing its own Home Assistant refresh credential.

**Architecture:** Keep `com.boop.shieldoverlay` and `com.boop.alpha1` as separate Android apps signed by the same private BOOP development key. The Shield discovers Home Assistant, creates a temporary pinned TLS pairing listener, and displays a `boop://shield-pair` QR. BOOP Wall receives that link, runs Home Assistant authorization with a phone-local loopback callback, and relays only the one-time authorization code plus the exact `client_id` to the waiting Shield; the Shield exchanges the code itself and stores its own refresh token with Android Keystore-backed encryption.

**Tech Stack:** Android Java 17, compileSdk/targetSdk 36, minSdk 26 for Shield, Android `NsdManager`, `SSLSocket`/`SSLServerSocket`, OkHttp 4.12.0 + `okhttp-tls` 4.12.0, ZXing Core 3.5.3, Android Keystore AES/GCM, Python source-regression tests, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-05-boop-shield-home-design.md`

## Global Constraints

- Execute on `boop-shield-home-implementation`.
- Preserve package IDs exactly: Shield `com.boop.shieldoverlay`; BOOP Wall `com.boop.alpha1`.
- One intentional uninstall/reinstall migration is accepted for both currently ephemeral-signed private development APKs when they first move to the permanent BOOP development signing key. After that, CI builds must keep the same certificate.
- Never commit keystore bytes and never reuse the private development key for a future public/Play release.
- Shield stays Java-only at the source level; do not add the Kotlin Gradle plugin.
- No BOOP cloud relay, hosted callback, account server, telemetry endpoint, or internet dependency for pairing/control.
- Pairing listener lifetime is exactly 120 seconds, accepts one successful use, and closes on success/cancel/expiry.
- Pairing transport is TLS with the temporary Shield certificate identity pinned from QR data. Never fall back to unauthenticated HTTP.
- QR data must never contain an HA password, access token, refresh token, or BOOP signing secret.
- BOOP Wall relays only a one-time HA authorization result. The Shield performs `/auth/token` exchange itself using the exact same `client_id`.
- First auth callback strategy is phone-local same-origin loopback: `http://127.0.0.1:<ephemeral-port>/` as `client_id`, with `/auth_callback` on the same host/port.
- If physical HA rejects that callback model, stop this plan and return to design. Do not add a cloud callback.
- Do not add routines, dashboard controls, voice, mic-key interception, sensors, or a permanent local server here.

## File Map

Shield pairing code lives in focused classes under `shield-overlay/app/src/main/java/com/boop/shieldoverlay/`: `HomeAssistantDiscovery`, `PairingSession`, `PairingQrPayload`, `TemporaryTlsPairingServer`, `PairingRelayMessage`, `HomeAssistantAuthClient`, `SecureCredentialStore`, `QrCodeBitmap`, and `PairingGateController`. BOOP Wall companion code lives in `source/companion/` and is copied into the unpacked Alpha 1 project by CI. Build-time patch scripts live in `scripts/`. Stable signer verification lives in both Android workflows and `shield-overlay/signing/boop-dev-cert-sha256.txt`.

---

### Task 1: Lock one permanent BOOP development signing identity

**Files:**
- Modify: `.gitignore`
- Modify: `shield-overlay/app/build.gradle`
- Modify: `.github/workflows/build-shield-overlay-poc.yml`
- Modify: `.github/workflows/build-apk.yml`
- Create: `scripts/apply_boop_dev_signing.py`
- Create: `shield-overlay/signing/boop-dev-cert-sha256.txt`
- Create: `docs/BOOP_DEV_SIGNING.md`
- Create: `tests/test_boop_signing.py`

**Interfaces:**
- Consumes GitHub Actions secrets `BOOP_DEV_KEYSTORE_B64`, `BOOP_DEV_STORE_PASSWORD`, `BOOP_DEV_KEY_PASSWORD` and alias `boop-dev`.
- Produces Shield and BOOP Wall APKs with the same stable development signer; CI verifies signer SHA-256 against the committed public certificate digest.

- [ ] **Step 1: Write the failing regression**

```python
from pathlib import Path
import unittest
ROOT = Path(__file__).resolve().parents[1]

class BoopSigningTest(unittest.TestCase):
    def test_both_workflows_use_stable_boop_signing(self):
        for rel in ('.github/workflows/build-shield-overlay-poc.yml', '.github/workflows/build-apk.yml'):
            text = (ROOT / rel).read_text(encoding='utf-8')
            self.assertIn('BOOP_DEV_KEYSTORE_B64', text)
            self.assertIn('BOOP_DEV_STORE_PASSWORD', text)
            self.assertIn('BOOP_DEV_KEY_PASSWORD', text)
            self.assertIn('boop-dev-cert-sha256.txt', text)

    def test_private_key_files_are_ignored(self):
        text = (ROOT / '.gitignore').read_text(encoding='utf-8')
        self.assertIn('*.jks', text)
        self.assertIn('*.keystore', text)
```

- [ ] **Step 2: Run RED**

```bash
python3 -m unittest tests.test_boop_signing -v
```

Expected: FAIL because the existing workflows use runner-generated debug signing.

- [ ] **Step 3: Generate the key locally and commit only the public digest**

```bash
mkdir -p .local-signing
read -s -p 'BOOP dev store password: ' BOOP_DEV_STORE_PASSWORD; echo
read -s -p 'BOOP dev key password: ' BOOP_DEV_KEY_PASSWORD; echo
export BOOP_DEV_STORE_PASSWORD BOOP_DEV_KEY_PASSWORD
keytool -genkeypair -keystore .local-signing/boop-dev.jks -storetype PKCS12 \
  -alias boop-dev -keyalg RSA -keysize 3072 -validity 36500 \
  -storepass "$BOOP_DEV_STORE_PASSWORD" -keypass "$BOOP_DEV_KEY_PASSWORD" \
  -dname 'CN=BOOP Development,O=BOOP'
keytool -exportcert -alias boop-dev -keystore .local-signing/boop-dev.jks \
  -storepass "$BOOP_DEV_STORE_PASSWORD" -rfc > .local-signing/boop-dev-cert.pem
openssl x509 -in .local-signing/boop-dev-cert.pem -noout -fingerprint -sha256 \
  | sed 's/^sha256 Fingerprint=//; s/://g' | tr '[:upper:]' '[:lower:]' \
  > shield-overlay/signing/boop-dev-cert-sha256.txt
```

Set the three repository Actions secrets from this same local key. The keystore bytes go only into `BOOP_DEV_KEYSTORE_B64` as base64; do not put them in Git.

- [ ] **Step 4: Configure both workflows/builds**

In `shield-overlay/app/build.gradle`, use:

```groovy
def boopStore = System.getenv('BOOP_SIGNING_STORE_FILE')
def boopStorePassword = System.getenv('BOOP_DEV_STORE_PASSWORD')
def boopKeyPassword = System.getenv('BOOP_DEV_KEY_PASSWORD')
android {
    signingConfigs {
        boopDev {
            if (boopStore != null) {
                storeFile file(boopStore)
                storePassword boopStorePassword
                keyAlias 'boop-dev'
                keyPassword boopKeyPassword
            }
        }
    }
    buildTypes {
        debug {
            if (boopStore != null) signingConfig signingConfigs.boopDev
        }
    }
}
```

Each workflow decodes `${{ secrets.BOOP_DEV_KEYSTORE_B64 }}` to `${RUNNER_TEMP}/boop-dev.jks`, exports `BOOP_SIGNING_STORE_FILE`, and fails if any signing secret is absent. `scripts/apply_boop_dev_signing.py` injects the equivalent config into the unpacked Alpha 1 `app/build.gradle` before build.

After each APK build:

```bash
apksigner verify --print-certs "$APK" | tee signer.txt
ACTUAL="$(grep 'Signer #1 certificate SHA-256 digest:' signer.txt | awk '{print $NF}')"
EXPECTED="$(cat shield-overlay/signing/boop-dev-cert-sha256.txt)"
test "$ACTUAL" = "$EXPECTED"
```

- [ ] **Step 5: Run GREEN and commit**

```bash
python3 -m unittest discover -s tests -p 'test_*.py' -v
git add .gitignore shield-overlay/app/build.gradle .github/workflows/build-shield-overlay-poc.yml \
  .github/workflows/build-apk.yml scripts/apply_boop_dev_signing.py \
  shield-overlay/signing/boop-dev-cert-sha256.txt docs/BOOP_DEV_SIGNING.md tests/test_boop_signing.py
git commit -m 'build: lock BOOP development signing identity'
```

### Task 2: Add strict 120-second pairing session and QR payload models

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/PairingSession.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/PairingQrPayload.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/PairingSessionTest.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/PairingQrPayloadTest.java`
- Create: `tests/test_shield_pairing_source.py`

**Interfaces:** `PairingSession.newSession(long nowMs)`, `isActive(long nowMs)`, `consume(String secret,long nowMs)`; `PairingQrPayload.toUri()` / `parse(Uri)` using `boop://shield-pair?v=1`.

- [ ] **Step 1: Write RED tests**

```java
@Test public void sessionExpiresAt120Seconds() {
    PairingSession s = PairingSession.newSession(1000L);
    assertTrue(s.isActive(120999L));
    assertFalse(s.isActive(121000L));
}

@Test public void correctSecretConsumesOnlyOnce() {
    PairingSession s = PairingSession.newSession(1000L);
    assertTrue(s.consume(s.secret(), 2000L));
    assertFalse(s.consume(s.secret(), 3000L));
}

@Test public void qrHasNoHaCredentials() {
    PairingQrPayload p = new PairingQrPayload('192.168.1.50', 42123, 'sid', 'secret', 'pin', 'http://homeassistant.local:8123');
    String uri = p.toUri().toString();
    assertFalse(uri.contains('refresh_token'));
    assertFalse(uri.contains('access_token'));
    assertFalse(uri.contains('password'));
}
```

Use Java double-quoted strings in the actual test source.

- [ ] **Step 2: Run RED**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --tests '*Pairing*' --stacktrace
```

- [ ] **Step 3: Implement minimal models**

`PairingSession` uses `SecureRandom` for a 128-bit URL-safe session ID and 256-bit URL-safe secret, `expiresAtMs = createdAtMs + 120_000L`, and `MessageDigest.isEqual` for secret comparison. `PairingQrPayload` serializes only:

```text
boop://shield-pair?v=1&host=<lan-host>&port=<tls-port>&sid=<session-id>&secret=<one-time-secret>&pin=<cert-sha256>&ha=<urlencoded-ha-lan-url>
```

Reject wrong scheme/host/version, invalid port, missing fields, and non-HTTP(S) HA URLs.

- [ ] **Step 4: Add source security assertions and run GREEN**

`tests/test_shield_pairing_source.py` asserts `120_000L` and `MessageDigest.isEqual` are present and forbids `api.openai.com`, Firebase, Cloudflare, hard-coded cloud relay URLs, and serialized HA token/password field names.

```bash
gradle -p shield-overlay :app:testDebugUnitTest --tests '*Pairing*' --stacktrace
python3 -m unittest tests.test_shield_pairing_source -v
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/PairingSession.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/PairingQrPayload.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/PairingSessionTest.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/PairingQrPayloadTest.java tests/test_shield_pairing_source.py
git commit -m 'feat: add BOOP local pairing session model'
```

### Task 3: Add temporary QR-pinned TLS listener on Shield

**Files:**
- Modify: `shield-overlay/app/build.gradle`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/PairingRelayMessage.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/TemporaryTlsPairingServer.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/TemporaryTlsPairingServerTest.java`

**Interfaces:** `start(InetAddress bindAddress, PairingSession session, Listener listener)` returns host/port/certificate SHA-256; listener receives one validated `PairingRelayMessage(authorizationCode, clientId)`; `close()` is idempotent.

- [ ] **Step 1: Write RED tests** covering one pinned client then close, wrong secret rejected, expired session rejected, explicit `close()` stops listening.

- [ ] **Step 2: Run RED**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --tests '*TemporaryTlsPairingServerTest' --stacktrace
```

- [ ] **Step 3: Implement TLS listener**

Add:

```groovy
implementation 'com.squareup.okhttp3:okhttp:4.12.0'
implementation 'com.squareup.okhttp3:okhttp-tls:4.12.0'
```

Generate one OkHttp `HeldCertificate` per session, place its private key/cert chain into an in-memory `KeyStore`, initialize `KeyManagerFactory` + `SSLContext`, and bind `SSLServerSocket` to the selected LAN address and port `0`. Pin is SHA-256 of the DER leaf certificate.

Accept one UTF-8 JSON line, maximum 8192 bytes:

```json
{"session_id":"...","secret":"...","authorization_code":"...","client_id":"http://127.0.0.1:43123/"}
```

Validate session ID and secret before constructing the relay message. On acceptance reply `{"status":"accepted"}`, consume session, close listener, invoke callback once. On failure reply `{"status":"rejected"}`.

- [ ] **Step 4: Run GREEN and commit**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --tests '*TemporaryTlsPairingServerTest' --stacktrace
git add shield-overlay/app/build.gradle shield-overlay/app/src/main/java/com/boop/shieldoverlay/PairingRelayMessage.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/TemporaryTlsPairingServer.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/TemporaryTlsPairingServerTest.java
git commit -m 'feat: add temporary pinned TLS pairing listener'
```

### Task 4: Add Home Assistant LAN discovery on Shield

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantDiscovery.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/DiscoveredHomeAssistant.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/HomeAssistantDiscoveryTest.java`
- Modify: `shield-overlay/app/src/main/AndroidManifest.xml`
- Modify: `tests/test_shield_overlay_source.py`

**Interfaces:** `HomeAssistantDiscovery.start(Listener)` / `stop()`; listener returns name, UUID and local base URL. Prefer Zeroconf TXT `internal_url`; if blank, fall back to resolved host/port. Never choose `external_url` for normal pairing.

- [ ] **Step 1: Write RED tests** for `internal_url` preference, host/port fallback, and `external_url` rejection.

- [ ] **Step 2: Run RED**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --tests '*HomeAssistantDiscoveryTest' --stacktrace
```

- [ ] **Step 3: Implement `NsdManager` discovery**

Use service type:

```java
private static final String SERVICE_TYPE = "_home-assistant._tcp.";
```

Read `location_name`, `uuid`, `internal_url`, `external_url`. Add manifest permissions:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Update the old source regression so network is intentionally allowed while `RECORD_AUDIO`, `RECEIVE_BOOT_COMPLETED`, and `BIND_ACCESSIBILITY_SERVICE` remain forbidden on Shield.

- [ ] **Step 4: Run GREEN and commit**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --tests '*HomeAssistantDiscoveryTest' --stacktrace
python3 -m unittest tests.test_shield_overlay_source -v
git add shield-overlay/app/src/main/AndroidManifest.xml shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantDiscovery.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/DiscoveredHomeAssistant.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/HomeAssistantDiscoveryTest.java tests/test_shield_overlay_source.py
git commit -m 'feat: discover Home Assistant on the local network'
```

### Task 5: Add BOOP Wall pairing companion without disturbing Alpha 1 speech

**Files:**
- Create: `source/companion/PairingLink.java`
- Create: `source/companion/HaLoopbackAuthServer.java`
- Create: `source/companion/PinnedTlsPairingClient.java`
- Create: `source/companion/ShieldPairingActivity.java`
- Create: `scripts/patch_alpha1_companion.py`
- Modify: `.github/workflows/build-apk.yml`
- Create: `tests/test_alpha1_pairing_companion.py`

**Interfaces:** `ShieldPairingActivity` handles `boop://shield-pair` and `boop://shield-pair-return`; loopback server binds only `127.0.0.1`; pinned client accepts only the QR certificate digest and sends only session fields, code and client ID.

- [ ] **Step 1: Write RED source tests** asserting `source/MainActivity.java` remains unchanged by companion routing, companion activity is copied at build time, manifest patch adds `INTERNET` + BROWSABLE activity, loopback binds `127.0.0.1`, and phone code never stores HA access/refresh tokens.

- [ ] **Step 2: Run RED**

```bash
python3 -m unittest tests.test_alpha1_pairing_companion -v
```

- [ ] **Step 3: Implement strict QR parsing and cert-pinned TLS relay**

`PairingLink` accepts only version-1 Shield links. `PinnedTlsPairingClient` uses an `X509TrustManager` that SHA-256 hashes the leaf certificate DER and compares it with the QR pin using `MessageDigest.isEqual`, then sends:

```json
{"session_id":"...","secret":"...","authorization_code":"...","client_id":"..."}
```

No token exchange occurs on the phone.

- [ ] **Step 4: Implement phone-local HA callback**

`HaLoopbackAuthServer` binds:

```java
new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
```

It creates `client_id = http://127.0.0.1:<port>/` and same-origin `redirect_uri = .../auth_callback`, validates random OAuth `state`, captures one `code`, then responds `302 Location: boop://shield-pair-return?sid=<session-id>` and closes.

`ShieldPairingActivity` launches:

```text
<ha-base-url>/auth/authorize?client_id=<encoded>&redirect_uri=<encoded>&state=<random>
```

with `Intent.ACTION_VIEW`. On return, relay code/client ID over pinned TLS and show **“Found it.”** or a plain retry message.

- [ ] **Step 5: Patch unpacked Alpha 1 in CI**

`scripts/patch_alpha1_companion.py` adds `INTERNET` once and adds `.ShieldPairingActivity` with `exported=true`, `launchMode=singleTask`, and BROWSABLE VIEW filters for `shield-pair` and `shield-pair-return`, preserving existing MainActivity and permissions.

Workflow copy step:

```bash
cp source/companion/*.java boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/
python3 scripts/patch_alpha1_companion.py boop-build/BOOP-Alpha1/app/src/main/AndroidManifest.xml
python3 scripts/apply_boop_dev_signing.py boop-build/BOOP-Alpha1/app/build.gradle
```

- [ ] **Step 6: Run GREEN and commit**

```bash
python3 -m unittest tests.test_alpha1_pairing_companion -v
git add source/companion scripts/patch_alpha1_companion.py .github/workflows/build-apk.yml tests/test_alpha1_pairing_companion.py
git commit -m 'feat: add BOOP Wall Shield pairing handoff'
```

### Task 6: Exchange auth code on Shield and store only the Shield credential

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantAuthClient.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/AuthTokenSet.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/SecureCredentialStore.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/StoredHomeAssistantCredential.java`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/HomeAssistantAuthClientTest.java`
- Modify: `shield-overlay/app/src/main/AndroidManifest.xml`

**Interfaces:** `exchangeAuthorizationCode(baseUrl,code,clientId)`, `refresh(baseUrl,refreshToken,clientId)`, and secure store `save/load/clear`.

- [ ] **Step 1: Write RED request/response tests** with OkHttp `MockWebServer`: initial exchange posts `grant_type=authorization_code`, code, exact client ID; refresh posts `grant_type=refresh_token`, Shield refresh token, same client ID; logs must not include token bodies.

- [ ] **Step 2: Run RED**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --tests '*HomeAssistantAuthClientTest' --stacktrace
```

- [ ] **Step 3: Implement `/auth/token` client** using form-encoded POST, parse `access_token`, `refresh_token`, `expires_in`, `token_type`, reject non-2xx/malformed responses. Because local HA commonly uses HTTP, set `android:usesCleartextTraffic="true"`; Shield still accepts HA base URLs only from local discovery in this slice.

- [ ] **Step 4: Implement Android Keystore-backed refresh-token storage**

Use AES/GCM key alias `boop_shield_ha_refresh_v1`, `AndroidKeyStore`, `PURPOSE_ENCRYPT | PURPOSE_DECRYPT`, `BLOCK_MODE_GCM`, `ENCRYPTION_PADDING_NONE`. Persist HA base URL/client ID as ordinary preferences and persist refresh token only as encoded IV + ciphertext. Keep access token in process memory only.

- [ ] **Step 5: Run GREEN and commit**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --tests '*HomeAssistantAuthClientTest' --stacktrace
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/HomeAssistantAuthClient.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/AuthTokenSet.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/SecureCredentialStore.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/StoredHomeAssistantCredential.java \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/HomeAssistantAuthClientTest.java shield-overlay/app/src/main/AndroidManifest.xml
git commit -m 'feat: store Shield Home Assistant credential securely'
```

### Task 7: Wire minimal Shield pairing-gate UI and QR

**Files:**
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/QrCodeBitmap.java`
- Create: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/PairingGateController.java`
- Modify: `shield-overlay/app/src/main/java/com/boop/shieldoverlay/MainActivity.java`
- Modify: `shield-overlay/app/build.gradle`
- Modify: `shield-overlay/app/src/main/res/values/strings.xml`
- Create: `shield-overlay/app/src/test/java/com/boop/shieldoverlay/PairingGateControllerTest.java`

**Interfaces:** controller emits `SEARCHING`, `QR_READY`, `AUTHORIZING`, `CONNECTED`, `STALE`, `FAILED`; QR renderer returns a square bitmap. Existing overlay service start logic remains intact.

- [ ] **Step 1: Write RED controller tests** for discovery→one pairing server, QR contains current cert pin, expiry closes listener, accepted relay exchanges with relay client ID, success stores Shield refresh token, cancel closes discovery/listener.

- [ ] **Step 2: Run RED**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --tests '*PairingGateControllerTest' --stacktrace
```

- [ ] **Step 3: Add QR renderer and minimal gate UI**

Add:

```groovy
implementation 'com.google.zxing:core:3.5.3'
```

After existing overlay permission is granted, start `BoopOverlayService` and show a black TV screen with **“I found your house”**, a large QR `ImageView`, simple retry state, and **“Found it.”** after token storage. Do not add dashboard, room picker, routines, or voice in this gate.

- [ ] **Step 4: Run full tests and commit**

```bash
gradle -p shield-overlay :app:testDebugUnitTest --stacktrace
python3 -m unittest discover -s tests -p 'test_*.py' -v
git add shield-overlay/app/src/main/java/com/boop/shieldoverlay/QrCodeBitmap.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/PairingGateController.java \
  shield-overlay/app/src/main/java/com/boop/shieldoverlay/MainActivity.java \
  shield-overlay/app/src/main/res/values/strings.xml shield-overlay/app/build.gradle \
  shield-overlay/app/src/test/java/com/boop/shieldoverlay/PairingGateControllerTest.java
git commit -m 'feat: add Shield local pairing gate'
```

### Task 8: CI + physical pairing gate

**Files:**
- Modify: `.github/workflows/build-shield-overlay-poc.yml`
- Modify: `.github/workflows/build-apk.yml`

**Interfaces:** produces stable-signed Shield and BOOP Wall artifacts. Passing the physical gate unlocks the dashboard plan.

- [ ] **Step 1: Tighten APK inspection**

Shield CI asserts package `com.boop.shieldoverlay`, targetSdk 36, network + overlay FGS permissions present, `RECORD_AUDIO`, boot and accessibility permissions absent, signer digest exact. BOOP Wall CI asserts package `com.boop.alpha1`, companion activity present, and same BOOP development signer.

- [ ] **Step 2: Run both workflows GREEN**. Do not install an artifact with a missing/different signer.

- [ ] **Step 3: Perform the one-time signing migration**

Shield: clear old app data, uninstall old ephemeral-signed BOOP Shield, install stable-signed build, grant overlay permission once.

Pixel BOOP Wall: uninstall old ephemeral-signed Alpha 1, install stable-signed Wall build, re-grant microphone permission when the existing Alpha 1 flow asks. This is the only planned signing-driven uninstall for these private packages.

- [ ] **Step 4: Run physical acceptance**

1. Launch Shield BOOP and confirm the known-good little overlay still runs.
2. Wait for **“I found your house”** + QR.
3. Scan with Pixel camera; Android opens BOOP Wall.
4. BOOP Wall opens local HA authorization; authorize.
5. Browser returns to BOOP Wall automatically.
6. BOOP Wall relays the one-time code over pinned TLS.
7. Shield says **“Found it.”**
8. Relaunch and prove Shield can refresh/authenticate from its stored credential without scanning again.
9. Disable internet while retaining LAN and repeat authentication.
10. Let a fresh QR age past 120 seconds; confirm it is unusable.
11. Cancel another fresh session; confirm that QR is unusable.
12. Reuse a successful QR; confirm it is unusable and no permanent listener remains.

**STOP CONDITION:** If HA rejects the loopback same-origin client ID/redirect flow or Android blocks the pinned temporary TLS design, record the exact failure and return to the design. Do not add cloud or unauthenticated fallback.

- [ ] **Step 5: Commit any CI-only corrections**

```bash
git add .github/workflows/build-shield-overlay-poc.yml .github/workflows/build-apk.yml
git commit -m 'test: verify BOOP local Home Assistant pairing gate'
```

Passing this gate is the prerequisite for `2026-09-05-boop-shield-home-first-slice.md`.
