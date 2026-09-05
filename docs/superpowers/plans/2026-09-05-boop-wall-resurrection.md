# BOOP Wall Resurrection Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore BOOP Wall from the Alpha 6.5.5 natural-wake lineage with all 33 wake phrases, BOOP OpenCode conversation, immediate local Home Assistant and media control, current Shield QR companion, and update-compatible stable signing, while keeping timed routines absent.

**Architecture:** Start an isolated branch/worktree at `origin/alpha6.5.5-shake-muppet`, because that line contains the natural-wake, customization, and puppet work that the separate 6.6.x timed branch omitted. Preserve its existing command router and local-first Home Assistant client, teach agent discovery to prefer the current exact `BOOP` agent, then integrate the Shield-only pairing companion and the current stable signing contract without touching protected Shield runtime code.

**Tech Stack:** Android Java 17, Android API 36, sherpa-onnx 1.13.7 local keyword spotting, Android `SpeechRecognizer`, Home Assistant REST/WebSocket conversation APIs, OkHttp 4.12.0, JUnit 4, Python `unittest`, GitHub Actions, stable BOOP development JKS signing.

**Spec:** `docs/superpowers/specs/2026-09-05-boop-wall-resurrection-design.md`

## Global Constraints

- Package/application ID remains exactly `com.boop.alpha1`.
- Base implementation is exactly `origin/alpha6.5.5-shake-muppet` (`e6f3f5afc3390ff0dded1239abbb81f1a3763ed0`).
- Preserve all 33 non-empty lines from `wake-assets/boop-kws/keywords_raw.txt`; every compiled line ends in `@BOOP`.
- Timed routines remain absent: no `BoopTimedRoutineFlow`, `Once or recurring?`, or timed follow-up entry point.
- Immediate Home Assistant and direct media control remain local-first and usable when OpenCode is unavailable.
- General conversation uses Home Assistant's current agent named `BOOP`; Android contains no direct OpenAI credential.
- Shield QR pairing stays in a separate `ShieldPairingActivity` and never enters `MainActivity`.
- Use the stable signer whose public SHA-256 fingerprint is in `shield-overlay/signing/boop-dev-cert-sha256.txt`; never commit a keystore or secret.
- Do not modify, move, or repoint `checkpoint-shield-home-f8e8135` or `checkpoint-shield-routines-3fa18c6`.
- Do not modify protected Shield overlay/Home runtime source.
- CI-green and physically green remain different states.
- Do not create a new BOOP Wall checkpoint before the Pixel 7 Pro acceptance tests pass.

## File Structure

### Imported unchanged from Alpha 6.5.5

- `source/MainActivity.java`: face, speech, wake lifecycle, tap fallback, command entry, and puppet states.
- `source/BoopWake*.java`, `source/BoopSherpaWakeSpotter.java`, `source/BoopWakeSensitivity.java`: local wake pipeline and tuning.
- `wake-assets/boop-kws/keywords_raw.txt`, `wake-assets/boop-kws/keywords.txt`: exact 33-phrase source and tokenized KWS asset.
- `source/HomeAssistantClient.java`: local-first colour, direct-media, and HA conversation handling.
- `source/HomeAssistantDirectMediaClient.java`, `source/HomeAssistantMediaSelector.java`: direct media execution and safe targeting.
- `source/HomeAssistantGeneralAssistantClient.java`, `source/BoopCommandRouter.java`: local `NO_MATCH` fallback to general conversation.
- `source/BoopFaceView.java`, `source/BoopShakeDetector.java`, `source/BoopShakeEyeMotion.java`: latest pre-timed puppet behaviour.

### Modified

- `source/HomeAssistantOpenCodeAgentSelector.java`: prefer exact current BOOP identity, retain historic BOOP/OpenCode compatibility, never select unrelated agents.
- `source-test/HomeAssistantOpenCodeAgentSelectorTest.java`: selection priority and compatibility tests.
- `source/AndroidManifest.xml`: register the separate Shield pairing deep-link activity.
- `source/app-build.gradle`: version code 29 and current stable BOOP development signing environment contract.
- `scripts/materialize-android.sh`: copy companion Java sources into the materialized Android project.
- `.github/workflows/build-boop-wall-resurrection.yml`: complete source, JVM, Android, signer, launch, and deep-link gate for the resurrection branch.
- `BOOP_MEMORY.txt`, `BOOP_STATUS.md`: implementation and verification evidence only; never claim physical success early.

### Created by selective copy from the current documented Shield branch

- `source/companion/PairingLink.java`: strict token-free BOOP Shield pairing URI parser.
- `source/companion/HaLoopbackAuthServer.java`: loopback HA OAuth callback with state validation.
- `source/companion/PinnedTlsPairingClient.java`: pinned relay of the short-lived authorization code to Shield.
- `source/companion/ShieldPairingActivity.java`: separate Shield pairing UI/deep-link lifecycle.
- `tests/test_wall_resurrection_contract.py`: exact source-lineage, wake, routing, and timed-absence gate.
- `tests/test_wall_pairing_companion.py`: companion separation/security/materialization gate.
- `tests/test_wall_stable_signing.py`: signing configuration, fingerprint, ignore, and workflow gate.
- `shield-overlay/signing/boop-dev-cert-sha256.txt`: public certificate fingerprint only.

---

### Task 1: Create the isolated resurrection branch and prove the baseline

**Files:**
- Import: `BOOP_MEMORY.txt`
- Import: `BOOP_STATUS.md`
- Import: `docs/superpowers/specs/2026-09-05-boop-wall-resurrection-design.md`
- Import: `docs/superpowers/plans/2026-09-05-boop-wall-resurrection.md`

**Interfaces:**
- Consumes: remote commit `e6f3f5afc3390ff0dded1239abbb81f1a3763ed0`.
- Produces: isolated branch `boop-wall-resurrection` with Alpha 6.5.5 source plus current durable docs.

- [ ] **Step 1: Create the isolated worktree using the required worktree skill**

Invoke `superpowers:using-git-worktrees`, then create a sibling worktree from the exact remote commit:

```powershell
git worktree add -b boop-wall-resurrection C:\Users\ryank\Documents\Codex\BOOP-wall-resurrection e6f3f5afc3390ff0dded1239abbb81f1a3763ed0
```

- [ ] **Step 2: Import the approved design, plan, memory, and status without importing Shield runtime source**

Run in the new worktree:

```powershell
git checkout boop-shield-home-implementation -- BOOP_MEMORY.txt BOOP_STATUS.md docs/superpowers/specs/2026-09-05-boop-wall-resurrection-design.md docs/superpowers/plans/2026-09-05-boop-wall-resurrection.md
```

- [ ] **Step 3: Run the Alpha 6.5.5 baseline regression suite**

Run:

```powershell
python -m unittest discover -s tests -p "test_*.py" -v
```

Expected: all existing Alpha 6.5.5 Python tests pass before integration edits.

- [ ] **Step 4: Prove the recovered lineage has 33 phrases and no timed runtime**

Run:

```powershell
$wakeLines = Get-Content wake-assets\boop-kws\keywords_raw.txt | Where-Object { $_.Trim() }
if ($wakeLines.Count -ne 33) { throw "Expected 33 wake phrases, found $($wakeLines.Count)" }
if (rg -n "BoopTimedRoutineFlow|Once or recurring\?" source) { throw "Timed routine code is present" }
```

Expected: count is 33 and the timed-code search has no matches.

- [ ] **Step 5: Commit the isolated documented baseline**

```powershell
git add BOOP_MEMORY.txt BOOP_STATUS.md docs/superpowers/specs/2026-09-05-boop-wall-resurrection-design.md docs/superpowers/plans/2026-09-05-boop-wall-resurrection.md
git commit -m "docs: establish BOOP Wall resurrection baseline"
```

### Task 2: Lock the resurrection contract and exact wake collection

**Files:**
- Create: `tests/test_wall_resurrection_contract.py`
- Modify: `tests/test_alpha653_natural_wake.py`
- Modify: `tests/test_alpha655_shake_muppet.py`
- Modify: `source/app-build.gradle`

**Interfaces:**
- Consumes: Alpha 6.5.5 source and the 33-line raw/tokenized wake assets.
- Produces: version identity `versionCode 29`, `versionName "0.4.9-alpha6.5.6-wall"`, and a source-level regression gate for required/forbidden features.

- [ ] **Step 1: Write the failing resurrection contract test**

Create `tests/test_wall_resurrection_contract.py`:

```python
from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]


class WallResurrectionContractTest(unittest.TestCase):
    def test_exact_natural_wake_collection_is_preserved(self):
        raw = (ROOT / "wake-assets/boop-kws/keywords_raw.txt").read_text(encoding="utf-8")
        compiled = (ROOT / "wake-assets/boop-kws/keywords.txt").read_text(encoding="utf-8")
        raw_lines = [line.strip() for line in raw.splitlines() if line.strip()]
        compiled_lines = [line.strip() for line in compiled.splitlines() if line.strip()]
        self.assertEqual(33, len(raw_lines))
        self.assertEqual(33, len(compiled_lines))
        for phrase in ("BOOP", "HEY BOOP", "EY BOOP", "HELLO BOOP", "OI BOOP",
                       "WAKE UP BOOP", "ARE YOU THERE BOOP", "EXCUSE ME BOOP"):
            self.assertTrue(any(line.startswith(phrase + " :") for line in raw_lines), phrase)
        self.assertTrue(all(line.endswith("@BOOP") for line in compiled_lines))

    def test_required_smart_paths_and_tap_fallback_are_present(self):
        main = (ROOT / "source/MainActivity.java").read_text(encoding="utf-8")
        ha = (ROOT / "source/HomeAssistantClient.java").read_text(encoding="utf-8")
        for required in ("BoopWakeWordController", "BoopCommandRouter",
                         "HomeAssistantGeneralAssistantClient", "startListening"):
            self.assertIn(required, main)
        self.assertIn("HomeAssistantDirectMediaClient", ha)

    def test_timed_routines_are_absent(self):
        source = "\n".join(
            path.read_text(encoding="utf-8")
            for path in (ROOT / "source").rglob("*.java")
        )
        for forbidden in ("BoopTimedRoutineFlow", "Once or recurring?",
                          "Recurring routines need setup first"):
            self.assertNotIn(forbidden, source)

    def test_resurrection_build_identity_is_monotonic(self):
        gradle = (ROOT / "source/app-build.gradle").read_text(encoding="utf-8")
        self.assertIn("versionCode 29", gradle)
        self.assertIn('versionName "0.4.9-alpha6.5.6-wall"', gradle)


if __name__ == "__main__":
    unittest.main()
```

- [ ] **Step 2: Run the new test and verify the version assertion fails**

Run:

```powershell
python -m unittest tests.test_wall_resurrection_contract -v
```

Expected: wake/routing/timed checks pass; build identity fails because Alpha 6.5.5 is still code 28.

- [ ] **Step 3: Advance only the resurrection build identity and remove stale identity assertions**

In `source/app-build.gradle`, replace:

```groovy
versionCode 28
versionName "0.4.9-alpha6.5.5"
```

with:

```groovy
versionCode 29
versionName "0.4.9-alpha6.5.6-wall"
```

Remove only the obsolete code-28/version-name assertion methods from
`tests/test_alpha653_natural_wake.py` and `tests/test_alpha655_shake_muppet.py`;
retain all behavioural assertions.

- [ ] **Step 4: Run the contract and historic Alpha tests**

Run:

```powershell
python -m unittest tests.test_wall_resurrection_contract tests.test_alpha653_natural_wake tests.test_alpha655_shake_muppet -v
```

Expected: all tests pass.

- [ ] **Step 5: Commit the resurrection contract**

```powershell
git add tests/test_wall_resurrection_contract.py tests/test_alpha653_natural_wake.py tests/test_alpha655_shake_muppet.py source/app-build.gradle
git commit -m "test: lock BOOP Wall resurrection contract"
```

### Task 3: Select the current Home Assistant agent named BOOP

**Files:**
- Modify: `source-test/HomeAssistantOpenCodeAgentSelectorTest.java`
- Modify: `source/HomeAssistantOpenCodeAgentSelector.java`

**Interfaces:**
- Consumes: `List<HomeAssistantOpenCodeAgentSelector.Agent>` where each agent has `String id` and `String name`.
- Produces: `static String select(List<Agent> agents)` selecting exact BOOP first, historic BOOP/OpenCode second, generic OpenCode last, or `""`.

- [ ] **Step 1: Add failing exact-name and priority tests**

Add to `HomeAssistantOpenCodeAgentSelectorTest`:

```java
@Test public void exactBoopNameWinsEvenWhenIdDoesNotContainOpenCode() {
    String selected = HomeAssistantOpenCodeAgentSelector.select(Arrays.asList(
            new HomeAssistantOpenCodeAgentSelector.Agent("conversation.generic_opencode", "OpenCode"),
            new HomeAssistantOpenCodeAgentSelector.Agent("conversation.boop", "BOOP")));
    assertEquals("conversation.boop", selected);
}

@Test public void exactBoopMatchIsTrimmedAndCaseInsensitive() {
    String selected = HomeAssistantOpenCodeAgentSelector.select(Arrays.asList(
            new HomeAssistantOpenCodeAgentSelector.Agent("conversation.current", "  boop  ")));
    assertEquals("conversation.current", selected);
}

@Test public void unrelatedBoopSubstringIsIgnored() {
    String selected = HomeAssistantOpenCodeAgentSelector.select(Arrays.asList(
            new HomeAssistantOpenCodeAgentSelector.Agent("conversation.sboop", "Sboop")));
    assertEquals("", selected);
}
```

- [ ] **Step 2: Materialize and run the focused JUnit test to verify failure**

Run:

```powershell
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests com.boop.alpha1.HomeAssistantOpenCodeAgentSelectorTest --stacktrace
```

Expected: the exact-name tests fail because the old selector requires `opencode`.

- [ ] **Step 3: Implement explicit selection priority**

Replace `select` with:

```java
static String select(List<Agent> agents) {
    String boopOpenCode = "";
    String genericOpenCode = "";
    for (Agent agent : agents) {
        String id = agent.id.trim().toLowerCase(Locale.ROOT);
        String name = agent.name.trim().toLowerCase(Locale.ROOT);
        if (name.equals("boop") || id.equals("conversation.boop")) {
            return agent.id;
        }
        boolean openCode = id.contains("opencode") || name.contains("opencode");
        boolean boop = id.contains("boop") || name.contains("boop");
        if (openCode && boop && boopOpenCode.isBlank()) {
            boopOpenCode = agent.id;
        } else if (openCode && genericOpenCode.isBlank()) {
            genericOpenCode = agent.id;
        }
    }
    return boopOpenCode.isBlank() ? genericOpenCode : boopOpenCode;
}
```

- [ ] **Step 4: Run focused and full source tests**

Run:

```powershell
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --tests com.boop.alpha1.HomeAssistantOpenCodeAgentSelectorTest --stacktrace
python -m unittest tests.test_alpha6_routing tests.test_wall_resurrection_contract -v
```

Expected: all pass; generic ChatGPT/Gemini agents remain ignored.

- [ ] **Step 5: Commit agent discovery**

```powershell
git add source/HomeAssistantOpenCodeAgentSelector.java source-test/HomeAssistantOpenCodeAgentSelectorTest.java
git commit -m "fix: discover current BOOP conversation agent"
```

### Task 4: Integrate the separate Shield QR pairing companion

**Files:**
- Create: `source/companion/PairingLink.java`
- Create: `source/companion/HaLoopbackAuthServer.java`
- Create: `source/companion/PinnedTlsPairingClient.java`
- Create: `source/companion/ShieldPairingActivity.java`
- Create: `tests/test_wall_pairing_companion.py`
- Modify: `scripts/materialize-android.sh`
- Modify: `source/AndroidManifest.xml`

**Interfaces:**
- Consumes: `boop://shield-pair?...` and `boop://shield-pair-return?...` deep links.
- Produces: separate `ShieldPairingActivity`; `MainActivity` remains free of Shield-pairing code.

- [ ] **Step 1: Copy the proven companion source from exact commit `33edce7`**

Read the four exact source objects:

```powershell
git show 33edce7:source/companion/PairingLink.java
git show 33edce7:source/companion/HaLoopbackAuthServer.java
git show 33edce7:source/companion/PinnedTlsPairingClient.java
git show 33edce7:source/companion/ShieldPairingActivity.java
```

Use `apply_patch` to add those exact contents under `source/companion/`; do not
rewrite or refactor them during this integration task.

- [ ] **Step 2: Write the failing companion integration test**

Create `tests/test_wall_pairing_companion.py`:

```python
from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]


class WallPairingCompanionTest(unittest.TestCase):
    def test_companion_is_token_free_and_pinned(self):
        files = {p.name: p.read_text(encoding="utf-8") for p in (ROOT / "source/companion").glob("*.java")}
        self.assertEqual({"PairingLink.java", "HaLoopbackAuthServer.java",
                          "PinnedTlsPairingClient.java", "ShieldPairingActivity.java"}, set(files))
        combined = "\n".join(files.values()).lower()
        self.assertNotIn("refresh_token", combined)
        self.assertNotIn("access_token", combined)
        self.assertIn("messagedigest.isequal", combined)
        self.assertIn("127.0.0.1", combined)

    def test_main_activity_stays_pairing_free(self):
        main = (ROOT / "source/MainActivity.java").read_text(encoding="utf-8")
        self.assertNotIn("ShieldPairingActivity", main)
        self.assertNotIn("shield-pair", main)

    def test_manifest_and_materializer_include_companion(self):
        manifest = (ROOT / "source/AndroidManifest.xml").read_text(encoding="utf-8")
        materializer = (ROOT / "scripts/materialize-android.sh").read_text(encoding="utf-8")
        self.assertIn('android:name=".ShieldPairingActivity"', manifest)
        self.assertIn('android:host="shield-pair"', manifest)
        self.assertIn('android:host="shield-pair-return"', manifest)
        self.assertIn('cp source/companion/*.java "$MAIN"/', materializer)


if __name__ == "__main__":
    unittest.main()
```

- [ ] **Step 3: Run the test and verify manifest/materializer failure**

Run:

```powershell
python -m unittest tests.test_wall_pairing_companion -v
```

Expected: security/separation passes; manifest and materializer assertion fails.

- [ ] **Step 4: Register and materialize the separate activity**

Add before `</application>` in `source/AndroidManifest.xml`:

```xml
<activity
    android:name=".ShieldPairingActivity"
    android:exported="true"
    android:excludeFromRecents="true"
    android:launchMode="singleTask">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="boop" android:host="shield-pair" />
        <data android:scheme="boop" android:host="shield-pair-return" />
    </intent-filter>
</activity>
```

Add after `cp source/*.java "$MAIN"/` in `scripts/materialize-android.sh`:

```bash
cp source/companion/*.java "$MAIN"/
```

- [ ] **Step 5: Run companion and full Python regressions**

Run:

```powershell
python -m unittest tests.test_wall_pairing_companion -v
python -m unittest discover -s tests -p "test_*.py" -v
```

Expected: all pass.

- [ ] **Step 6: Commit the companion integration**

```powershell
git add source/companion source/AndroidManifest.xml scripts/materialize-android.sh tests/test_wall_pairing_companion.py
git commit -m "feat: preserve Shield QR companion in BOOP Wall"
```

### Task 5: Adopt the current stable signer without exposing keys

**Files:**
- Create: `tests/test_wall_stable_signing.py`
- Create: `shield-overlay/signing/boop-dev-cert-sha256.txt`
- Modify: `source/app-build.gradle`
- Modify: `.gitignore`

**Interfaces:**
- Consumes: CI secrets `BOOP_DEV_KEYSTORE_B64`, `BOOP_DEV_STORE_PASSWORD`, `BOOP_DEV_KEY_PASSWORD`, and temporary `BOOP_SIGNING_STORE_FILE`.
- Produces: APK signed by alias `boop-dev` whose SHA-256 certificate digest equals the committed public fingerprint.

- [ ] **Step 1: Import only the public signer fingerprint from `33edce7`**

Copy `shield-overlay/signing/boop-dev-cert-sha256.txt` from commit `33edce7`. Do not copy any keystore.

- [ ] **Step 2: Write the failing stable-signing test**

Create `tests/test_wall_stable_signing.py`:

```python
from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]


class WallStableSigningTest(unittest.TestCase):
    def test_gradle_uses_current_stable_signer_contract(self):
        gradle = (ROOT / "source/app-build.gradle").read_text(encoding="utf-8")
        for required in ("BOOP_SIGNING_STORE_FILE", "BOOP_DEV_STORE_PASSWORD",
                         "BOOP_DEV_KEY_PASSWORD", "keyAlias 'boop-dev'",
                         "signingConfig signingConfigs.boopDev"):
            self.assertIn(required, gradle)
        self.assertNotIn("BOOP_KEY_ALIAS", gradle)

    def test_private_signing_files_are_ignored(self):
        ignore = (ROOT / ".gitignore").read_text(encoding="utf-8")
        self.assertIn("*.jks", ignore)
        self.assertIn("*.keystore", ignore)

    def test_public_fingerprint_is_present(self):
        digest = (ROOT / "shield-overlay/signing/boop-dev-cert-sha256.txt").read_text(encoding="utf-8").strip()
        self.assertEqual(64, len(digest))
        int(digest, 16)


if __name__ == "__main__":
    unittest.main()
```

- [ ] **Step 3: Run the test and verify the old signing contract fails**

Run:

```powershell
python -m unittest tests.test_wall_stable_signing -v
```

Expected: Gradle assertions fail because Alpha 6.5.5 still uses the old `BOOP_KEY_*` variables.

- [ ] **Step 4: Replace the Gradle signing variables and config**

Use these declarations above `android {`:

```groovy
def boopKeystorePath = System.getenv('BOOP_SIGNING_STORE_FILE')
def boopKeystorePassword = System.getenv('BOOP_DEV_STORE_PASSWORD')
def boopKeyPassword = System.getenv('BOOP_DEV_KEY_PASSWORD')
```

Use this signing configuration:

```groovy
signingConfigs {
    boopDev {
        if (boopKeystorePath) {
            storeFile file(boopKeystorePath)
            storePassword boopKeystorePassword
            keyAlias 'boop-dev'
            keyPassword boopKeyPassword
        }
    }
}

buildTypes {
    debug {
        signingConfig signingConfigs.boopDev
    }
}
```

Ensure `.gitignore` contains separate `*.jks` and `*.keystore` lines.

- [ ] **Step 5: Run signing and complete Python regressions**

Run:

```powershell
python -m unittest tests.test_wall_stable_signing -v
python -m unittest discover -s tests -p "test_*.py" -v
```

Expected: all pass.

- [ ] **Step 6: Commit stable signing**

```powershell
git add source/app-build.gradle .gitignore tests/test_wall_stable_signing.py shield-overlay/signing/boop-dev-cert-sha256.txt
git commit -m "build: use update-compatible BOOP signer"
```

### Task 6: Add the unified resurrection CI and APK inspection gate

**Files:**
- Create: `.github/workflows/build-boop-wall-resurrection.yml`
- Modify: `tests/test_wall_resurrection_contract.py`
- Modify: `tests/test_wall_pairing_companion.py`
- Modify: `tests/test_wall_stable_signing.py`

**Interfaces:**
- Consumes: repository sources, wake assets, GitHub signing secrets, Android API/build-tools 36.
- Produces: artifact `BOOP-Wall-Resurrection-debug` plus evidence for package, version, 33 wake mappings, native KWS library, activities, signer, tests, and launch survival.

- [ ] **Step 1: Add failing workflow-contract assertions**

Add a shared workflow read in each focused test and assert the new workflow contains:

```python
workflow = (ROOT / ".github/workflows/build-boop-wall-resurrection.yml").read_text(encoding="utf-8")
```

The resurrection contract test asserts:

```python
for required in ("boop-wall-resurrection", "testDebugUnitTest", "versionCode='29'",
                 "assets/boop-kws/keywords.txt", "libsherpa-onnx-jni.so",
                 "BOOP-Wall-Resurrection-debug"):
    self.assertIn(required, workflow)
```

The pairing test asserts:

```python
for required in ("ShieldPairingActivity", "boop://shield-pair-return?sid=test"):
    self.assertIn(required, workflow)
```

The signing test asserts:

```python
for required in ("BOOP_DEV_KEYSTORE_B64", "BOOP_SIGNING_STORE_FILE",
                 "boop-dev-cert-sha256.txt", "apksigner"):
    self.assertIn(required, workflow)
```

- [ ] **Step 2: Run the three tests and verify missing-workflow failure**

Run:

```powershell
python -m unittest tests.test_wall_resurrection_contract tests.test_wall_pairing_companion tests.test_wall_stable_signing -v
```

Expected: all new workflow assertions fail because the file does not exist.

- [ ] **Step 3: Create the unified workflow from the proven Alpha 6.5.5 gate**

Copy the structure of `.github/workflows/build-alpha655-shake-muppet.yml` and make these exact changes:

```yaml
name: Build BOOP Wall Resurrection APK

on:
  push:
    branches: [ "boop-wall-resurrection" ]
  workflow_dispatch:

concurrency:
  group: boop-wall-resurrection-${{ github.ref }}
  cancel-in-progress: true

env:
  BOOP_DEV_KEYSTORE_B64: ${{ secrets.BOOP_DEV_KEYSTORE_B64 }}
  BOOP_DEV_STORE_PASSWORD: ${{ secrets.BOOP_DEV_STORE_PASSWORD }}
  BOOP_DEV_KEY_PASSWORD: ${{ secrets.BOOP_DEV_KEY_PASSWORD }}
```

Retain the existing Python suite, Java harnesses, `materialize-android.sh`, wake
asset checks, Android unit tests, API 36 emulator, microphone grant, and process
survival gate. Require exactly 33 `@BOOP` lines.

Decode the signer and export its path:

```bash
printf '%s' "$BOOP_DEV_KEYSTORE_B64" | base64 --decode > "${RUNNER_TEMP}/boop-dev.jks"
chmod 600 "${RUNNER_TEMP}/boop-dev.jks"
echo "BOOP_SIGNING_STORE_FILE=${RUNNER_TEMP}/boop-dev.jks" >> "$GITHUB_ENV"
```

Inspect the APK using:

```bash
APK=boop-build/BOOP-Alpha1/app/build/outputs/apk/debug/app-debug.apk
AAPT="${ANDROID_HOME}/build-tools/36.0.0/aapt"
APKSIGNER="${ANDROID_HOME}/build-tools/36.0.0/apksigner"
"$AAPT" dump badging "$APK" | tee badging.txt
"$AAPT" dump xmltree "$APK" AndroidManifest.xml | tee manifest-tree.txt
grep -q "package: name='com.boop.alpha1'" badging.txt
grep -q "versionCode='29'" badging.txt
grep -q "versionName='0.4.9-alpha6.5.6-wall'" badging.txt
grep -q "MainActivity" manifest-tree.txt
grep -q "ShieldPairingActivity" manifest-tree.txt
unzip -l "$APK" | grep -q 'assets/boop-kws/keywords.txt'
unzip -l "$APK" | grep -q 'lib/.*/libsherpa-onnx-jni.so'
"$APKSIGNER" verify --print-certs "$APK" | tee signer.txt
ACTUAL="$(sed -n 's/^Signer #1 certificate SHA-256 digest: //p' signer.txt | tr -d '\r\n' | tr '[:upper:]' '[:lower:]')"
EXPECTED="$(tr -d '\r\n' < shield-overlay/signing/boop-dev-cert-sha256.txt | tr '[:upper:]' '[:lower:]')"
test -n "$ACTUAL"
test "$ACTUAL" = "$EXPECTED"
```

After the launch-survival check, add the pairing route smoke test:

```bash
adb shell am start -W -a android.intent.action.VIEW \
  -d 'boop://shield-pair-return?sid=test' com.boop.alpha1
sleep 2
adb shell dumpsys activity activities | grep -E 'com\.boop\.alpha1/.ShieldPairingActivity'
adb shell pidof com.boop.alpha1
```

Upload with:

```yaml
- name: Upload BOOP Wall resurrection APK
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: BOOP-Wall-Resurrection-debug
    path: boop-build/BOOP-Alpha1/app/build/outputs/apk/debug/app-debug.apk
    if-no-files-found: error
```

- [ ] **Step 4: Run workflow-contract and full local tests**

Run:

```powershell
python -m unittest tests.test_wall_resurrection_contract tests.test_wall_pairing_companion tests.test_wall_stable_signing -v
python -m unittest discover -s tests -p "test_*.py" -v
```

Expected: all pass.

- [ ] **Step 5: Materialize the Android tree and run unit tests if the local SDK/runtime is available**

Run:

```powershell
bash scripts/materialize-android.sh
gradle -p boop-build/BOOP-Alpha1 :app:testDebugUnitTest --stacktrace
```

Expected: Android unit tests pass. If dependency download is unavailable locally,
record that limitation and require the identical CI step to pass; do not label the
APK CI-green before it does.

- [ ] **Step 6: Commit the unified CI gate**

```powershell
git add .github/workflows/build-boop-wall-resurrection.yml tests/test_wall_resurrection_contract.py tests/test_wall_pairing_companion.py tests/test_wall_stable_signing.py
git commit -m "ci: build verified BOOP Wall resurrection APK"
```

### Task 7: Run full verification, publish the branch, and retrieve the APK

**Files:**
- Modify: `BOOP_STATUS.md`
- Modify: `BOOP_MEMORY.txt`
- Output: `C:\Users\ryank\Desktop\BOOP-Wall-Resurrection.apk`

**Interfaces:**
- Consumes: completed `boop-wall-resurrection` commits and GitHub Actions secrets.
- Produces: pushed branch, green workflow run, signed downloadable APK, and truthful CI-only status.

- [ ] **Step 1: Run the final local verification gate**

Run:

```powershell
python -m unittest discover -s tests -p "test_*.py" -v
git diff --check
git status --short
```

Expected: tests pass, no whitespace errors, and only intended documentation status changes remain.

- [ ] **Step 2: Update status as implemented but not yet CI/physically green**

In `BOOP_STATUS.md` and `BOOP_MEMORY.txt`, record the branch, exact code HEAD,
restored 33-phrase lineage, BOOP agent selection, companion/signing integration,
and timed-routine absence. Label physical status explicitly `NOT YET TESTED`.

- [ ] **Step 3: Commit the implementation status**

```powershell
git add BOOP_STATUS.md BOOP_MEMORY.txt
git commit -m "docs: record BOOP Wall resurrection implementation"
```

- [ ] **Step 4: Push the new branch**

```powershell
git push -u origin boop-wall-resurrection
```

- [ ] **Step 5: Wait for the named workflow and inspect every job**

```powershell
$boopRunId = gh run list --branch boop-wall-resurrection --workflow build-boop-wall-resurrection.yml --limit 1 --json databaseId --jq '.[0].databaseId'
gh run watch $boopRunId --exit-status
gh run view $boopRunId --log-failed
```

Expected: the run concludes `success`. On failure, diagnose and repair under TDD;
do not download or present a failed artifact as ready.

- [ ] **Step 6: Download and place the exact green artifact on the Desktop**

Download to a temporary directory, verify the APK again with `apksigner`, then
copy the single verified file to:

```text
C:\Users\ryank\Desktop\BOOP-Wall-Resurrection.apk
```

Do not overwrite an unrelated file silently; if the target exists, compare its
hash and replace it only as the explicit output of this build.

- [ ] **Step 7: Update docs from implemented to CI-green and commit/push**

Record the workflow run URL/ID, tested commit, artifact SHA-256, package/version,
and verified signer fingerprint. Keep physical status `NOT YET TESTED`.

```powershell
git add BOOP_STATUS.md BOOP_MEMORY.txt
git commit -m "docs: mark BOOP Wall resurrection CI green"
git push
```

### Task 8: Physical Pixel acceptance and checkpoint

**Files:**
- Modify after user evidence: `BOOP_STATUS.md`
- Modify after user evidence: `BOOP_MEMORY.txt`
- Create only after every gate passes: an annotated tag whose name is generated
  from `checkpoint-boop-wall-` plus the exact seven-character tested HEAD.

**Interfaces:**
- Consumes: the exact signed APK and user-observed Pixel 7 Pro behaviour.
- Produces: physical evidence record and protected checkpoint only if every required path passes.

- [ ] **Step 1: Ask the user to install the Desktop APK as an update**

Expected: Android accepts it without uninstalling. If Android reports a signature
or downgrade conflict, stop and preserve the installed app/data while diagnosing.

- [ ] **Step 2: Test eyes, tap, and representative natural wake phrases**

Verify eyes remain responsive, tap-to-talk works, and at minimum test:

```text
BOOP
Hey BOOP
Ey BOOP
Hello BOOP
Oi BOOP
Wake up BOOP
Are you there BOOP?
Excuse me BOOP
```

Verify a continuous phrase such as `Hey BOOP pause the music` retains the command
and wake detection rearms after completion.

- [ ] **Step 3: Test BOOP OpenCode conversation**

Ask an ordinary non-house question and confirm the Home Assistant agent named
`BOOP` supplies a useful reply that BOOP displays/speaks normally.

- [ ] **Step 4: Test immediate local home and media control**

Run a room-scoped light/device command, play/pause, and one additional available
media action. Confirm the intended real targets change and no same-named target
in another room is substituted.

- [ ] **Step 5: Test degraded conversation without sacrificing local control**

Make the BOOP OpenCode agent temporarily unavailable using the user's normal safe
integration control, verify an ordinary question fails plainly, then verify an
immediate local home command still succeeds. Restore OpenCode afterward.

- [ ] **Step 6: Prove removed timed behaviour stays absent and pairing remains alive**

Say a timed command and verify BOOP does not ask `Once or recurring?` or enter the
old automatic follow-up. Invoke a Shield pairing QR/deep link and confirm the
separate pairing companion opens.

- [ ] **Step 7: Record evidence and create the physical checkpoint only after all passes**

Update memory/status with each observed result and commit it. Read the resulting
exact commit IDs and create the tag without a hand-written placeholder:

```powershell
$boopTestedHead = git rev-parse HEAD
$boopShortHead = git rev-parse --short=7 HEAD
$boopCheckpointTag = "checkpoint-boop-wall-$boopShortHead"
git tag -a $boopCheckpointTag $boopTestedHead -m "Physically verified BOOP Wall resurrection"
git push origin boop-wall-resurrection $boopCheckpointTag
```

Never reuse, move, or infer a checkpoint tag.
