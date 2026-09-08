# SHIELD TURBO Performance Capability Probe Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build SHIELD TURBO v0.5.9 as a read-only real-device capability probe that discovers the Shield firmware's NVIDIA performance clues, stock CPU/GPU controls, shell writability, Android thermal status, and fixed-performance command support without changing any performance setting.

**Architecture:** Add a focused `performance` package containing pure parsing/model code and one Android/ADB-backed read-only probe. Reuse the existing worker thread in `MainActivity` and the existing trusted loopback ADB transport. Stage 1 deliberately adds no performance writer, boot receiver, watchdog service, foreground-service permission, governor write, processor-mode write, or fixed-performance command.

**Tech Stack:** Kotlin/JVM 17, Android API 28-36, `PowerManager`, existing `LocalBridge`/`AdbWire`, JUnit 4.13.2, Python `unittest` source contracts, existing GitHub Actions signing/package/archive checks.

**Spec:** `docs/superpowers/specs/2026-09-08-shield-turbo-stock-performance-mode-design.md`

## Global Constraints

- Branch: `shield-turbo-v01`; package: `com.boop.shieldturbo`.
- Starting live branch head for this plan: `7ccd9443406e83084f2730a6838067a85985d91b`.
- Permanent signer must remain `CN=BOOP Development,O=BOOP`, certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Stage 1 is **read-only**. It must not issue `settings put`, sysfs writes, `cmd power set-fixed-performance-mode-enabled true`, governor changes, frequency changes, fan changes, processor-mode changes, or any other performance write.
- Use `LocalBridge.withTrustedAdb` only for capability shell reads. Stage 1 must never trigger a fresh RSA approval.
- No root/su, bootloader/kernel/boot-image operation, voltage modification, thermal/throttle disable, frequency above firmware limits, or generic recursive `/sys` crawler.
- Do not add a thermal watchdog service or a performance boot receiver in this stage.
- Do not change CLEAN START files, brightness behavior, APPS behavior, animation undo, or package/signing identity.
- Android fixed-performance mode is diagnostic-only in v0.5.9.
- Machine tests must not claim visual quality, Dolphin performance gain, thermal effectiveness, or physical safety.
- Physical Shield evidence is required before writing Plan 2 for any performance control.

## File Structure

- Create `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/PerformanceCapability.kt`: immutable capability data types plus pure formatting helpers.
- Create `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/PerformanceDiscoveryPolicy.kt`: reviewed path allowlist, read-only shell command generation, parser for settings/property clues and path-probe output.
- Create `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/PerformanceCapabilityProbe.kt`: Android thermal read + trusted-ADB read-only discovery and conversion to existing `ProbeResult` cards.
- Create `shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/PerformanceDiscoveryPolicyTest.kt`: pure parser/allowlist tests.
- Create `shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/PerformanceCapabilityTest.kt`: thermal label/summary tests.
- Create `shield-turbo/tests/test_performance_capability_contract.py`: source-level guard proving Stage 1 contains no performance writes or new resident components.
- Modify `shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt`: append performance capability results inside the existing background `analyse()` flow.
- Modify `shield-turbo/app/src/main/res/values/strings.xml`: Stage-1 explanatory copy only.
- Modify `shield-turbo/app/build.gradle` and `.github/workflows/shield-turbo.yml` only at the final release stamp, from code 15 / `0.5.8` to code 16 / `0.5.9`.

---

### Task 1: Pure capability model and discovery policy

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/PerformanceCapability.kt`
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/PerformanceDiscoveryPolicy.kt`
- Create: `shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/PerformanceDiscoveryPolicyTest.kt`
- Create: `shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/PerformanceCapabilityTest.kt`

**Interfaces:**
- Produces:
  - `data class SettingClue(val namespace: String, val key: String, val value: String)`
  - `data class PathCapability(val path: String, val present: Boolean, val shellWritable: Boolean, val value: String)`
  - `data class PerformanceCapabilitySnapshot(...)`
  - `object PerformanceDiscoveryPolicy`
  - `fun PerformanceDiscoveryPolicy.settingClues(namespace: String, text: String): List<SettingClue>`
  - `fun PerformanceDiscoveryPolicy.pathProbeCommand(): String`
  - `fun PerformanceDiscoveryPolicy.parsePathProbe(text: String): List<PathCapability>`
  - `fun thermalLabel(status: Int?): String`
- Consumes: no Android runtime except `PowerManager` integer constants may be referenced from production code only if the pure unit test remains JVM-safe; otherwise use numeric mappings documented below.

- [ ] **Step 1: Write the failing parser/allowlist tests**

Create tests covering exact clue matching, path parsing, and read-only command construction:

```kotlin
package com.boop.shieldturbo.performance

import org.junit.Assert.*
import org.junit.Test

class PerformanceDiscoveryPolicyTest {
    @Test fun findsOnlyPerformanceRelatedSettingClues() {
        val text = """
            animator_duration_scale=0.5
            nvidia_processor_mode=optimized
            fan_mode=quiet
            unrelated_key=hello
            performance_hint=1
        """.trimIndent()
        val clues = PerformanceDiscoveryPolicy.settingClues("global", text)
        assertEquals(listOf("nvidia_processor_mode", "fan_mode", "performance_hint"), clues.map { it.key })
        assertTrue(clues.all { it.namespace == "global" })
    }

    @Test fun pathProbeParserPreservesPresenceWritabilityAndValue() {
        val parsed = PerformanceDiscoveryPolicy.parsePathProbe(
            "/sys/a\t1\t0\tschedutil\n/sys/b\t1\t1\t2035200000\n/sys/c\t0\t0\t\n"
        )
        assertEquals(PathCapability("/sys/a", true, false, "schedutil"), parsed[0])
        assertEquals(PathCapability("/sys/b", true, true, "2035200000"), parsed[1])
        assertEquals(PathCapability("/sys/c", false, false, ""), parsed[2])
    }

    @Test fun commandIsReadOnlyAndUsesOnlyReviewedPaths() {
        val command = PerformanceDiscoveryPolicy.pathProbeCommand()
        assertTrue(command.contains("test -w"))
        assertTrue(command.contains("cat \"$p\""))
        assertFalse(command.contains("settings put"))
        assertFalse(command.contains("set-fixed-performance-mode-enabled true"))
        assertFalse(Regex("echo\\s+.+>").containsMatchIn(command))
        PerformanceDiscoveryPolicy.paths.forEach { assertTrue(command.contains(it)) }
    }
}
```

Create thermal formatting tests:

```kotlin
class PerformanceCapabilityTest {
    @Test fun thermalLabelsAreStable() {
        assertEquals("Not exposed", thermalLabel(null))
        assertEquals("None", thermalLabel(0))
        assertEquals("Light", thermalLabel(1))
        assertEquals("Moderate", thermalLabel(2))
        assertEquals("Severe", thermalLabel(3))
        assertEquals("Critical", thermalLabel(4))
        assertEquals("Emergency", thermalLabel(5))
        assertEquals("Shutdown", thermalLabel(6))
    }
}
```

- [ ] **Step 2: Run the new unit tests and verify RED**

Run:

```bash
gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --tests 'com.boop.shieldturbo.performance.*' --console=plain --stacktrace
```

Expected: compile/test failure because the `performance` types and policy do not exist.

- [ ] **Step 3: Implement the immutable models and exact allowlist**

Use these model shapes:

```kotlin
package com.boop.shieldturbo.performance

data class SettingClue(val namespace: String, val key: String, val value: String)

data class PathCapability(
    val path: String,
    val present: Boolean,
    val shellWritable: Boolean,
    val value: String
)

data class PerformanceCapabilitySnapshot(
    val settingClues: List<SettingClue>,
    val propertyClues: List<SettingClue>,
    val paths: List<PathCapability>,
    val thermalStatus: Int?,
    val fixedPerformanceCommandExposed: Boolean,
    val trustedAdbAvailable: Boolean,
    val adbDetail: String
)

fun thermalLabel(status: Int?): String = when (status) {
    null -> "Not exposed"
    0 -> "None"
    1 -> "Light"
    2 -> "Moderate"
    3 -> "Severe"
    4 -> "Critical"
    5 -> "Emergency"
    6 -> "Shutdown"
    else -> "Unknown ($status)"
}
```

The reviewed read-only path allowlist must be literal and bounded:

```kotlin
val paths = listOf(
    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq",
    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq",
    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors",
    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq",
    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq",
    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq",
    "/sys/devices/system/cpu/cpufreq/policy0/scaling_cur_freq",
    "/sys/devices/system/cpu/cpufreq/policy0/scaling_governor",
    "/sys/devices/system/cpu/cpufreq/policy0/scaling_available_governors",
    "/sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq",
    "/sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq",
    "/sys/devices/system/cpu/cpufreq/policy0/cpuinfo_max_freq",
    "/sys/class/devfreq/57000000.gpu/cur_freq",
    "/sys/class/devfreq/57000000.gpu/min_freq",
    "/sys/class/devfreq/57000000.gpu/max_freq",
    "/sys/class/devfreq/57000000.gpu/governor",
    "/sys/class/devfreq/57000000.gpu/available_governors",
    "/sys/class/devfreq/57000000.gpu/available_frequencies",
    "/sys/devices/57000000.gpu/devfreq/57000000.gpu/cur_freq",
    "/sys/devices/57000000.gpu/devfreq/57000000.gpu/min_freq",
    "/sys/devices/57000000.gpu/devfreq/57000000.gpu/max_freq",
    "/sys/devices/57000000.gpu/devfreq/57000000.gpu/governor",
    "/sys/devices/57000000.gpu/devfreq/57000000.gpu/available_governors",
    "/sys/devices/57000000.gpu/devfreq/57000000.gpu/available_frequencies"
)
```

`settingClues()` must parse `key=value` lines and retain only keys containing one of these case-insensitive tokens: `nvidia`, `processor`, `performance`, `fan`, `power`. It must cap each input to 120 matched lines and each value to 512 characters.

`pathProbeCommand()` must only read/test:

```sh
for p in '<allowlisted path 1>' '<allowlisted path 2>' ...; do
  if [ -e "$p" ]; then
    w=0
    [ -w "$p" ] && w=1
    v=$(cat "$p" 2>/dev/null | head -c 1024 | tr '\n' ' ')
    printf '%s\t1\t%s\t%s\n' "$p" "$w" "$v"
  else
    printf '%s\t0\t0\t\n' "$p"
  fi
done
```

Generate the quoted path list from the literal Kotlin allowlist. Do not shell-glob or recurse.

- [ ] **Step 4: Run focused tests and make them GREEN**

Run the same Gradle command from Step 2. Expected: all `performance` unit tests pass.

- [ ] **Step 5: Run the entire current JVM suite**

```bash
gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --console=plain --stacktrace
```

Expected: all pre-existing tests plus the new tests pass.

- [ ] **Step 6: Commit Task 1**

```bash
git add shield-turbo/app/src/main/java/com/boop/shieldturbo/performance \
        shield-turbo/app/src/test/java/com/boop/shieldturbo/performance
git commit -m "feat(shield-turbo): add read-only performance discovery policy"
```

---

### Task 2: Android + trusted-ADB capability probe

**Files:**
- Create: `shield-turbo/app/src/main/java/com/boop/shieldturbo/performance/PerformanceCapabilityProbe.kt`
- Test: `shield-turbo/app/src/test/java/com/boop/shieldturbo/performance/PerformanceDiscoveryPolicyTest.kt`

**Interfaces:**
- Consumes:
  - `PerformanceDiscoveryPolicy.paths`
  - `PerformanceDiscoveryPolicy.pathProbeCommand()`
  - `PerformanceDiscoveryPolicy.settingClues(namespace, text)`
  - existing `LocalBridge.withTrustedAdb { adb -> ... }`
  - existing `LocalBridge.checked(adb, command)`
- Produces:
  - `class PerformanceCapabilityProbe(private val context: Context, private val bridge: LocalBridge = LocalBridge(context))`
  - `fun read(): PerformanceCapabilitySnapshot`
  - `fun readResults(): List<ProbeResult>`

- [ ] **Step 1: Add failing pure tests for the exact read-only clue commands**

Add assertions that a new `PerformanceDiscoveryPolicy.clueCommands` equals these commands exactly:

```kotlin
assertEquals(listOf(
    "settings list global | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120",
    "settings list secure | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120",
    "settings list system | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120",
    "getprop | grep -Ei 'nvidia|processor|performance|fan|power' | head -n 120",
    "cmd power help"
), PerformanceDiscoveryPolicy.clueCommands)
```

Expected RED: `clueCommands` does not yet exist.

- [ ] **Step 2: Implement exact clue commands and rerun focused tests**

Add the literal command list above. Do not add any write command.

- [ ] **Step 3: Implement `PerformanceCapabilityProbe` with no fresh ADB approval**

The probe flow must be:

```kotlin
fun read(): PerformanceCapabilitySnapshot {
    val thermal = currentThermalStatusOrNull()
    return try {
        bridge.withTrustedAdb { adb ->
            val global = bridge.checked(adb, PerformanceDiscoveryPolicy.clueCommands[0])
            val secure = bridge.checked(adb, PerformanceDiscoveryPolicy.clueCommands[1])
            val system = bridge.checked(adb, PerformanceDiscoveryPolicy.clueCommands[2])
            val props = bridge.checked(adb, PerformanceDiscoveryPolicy.clueCommands[3])
            val powerHelp = bridge.checked(adb, PerformanceDiscoveryPolicy.clueCommands[4])
            val paths = bridge.checked(adb, PerformanceDiscoveryPolicy.pathProbeCommand())
            PerformanceCapabilitySnapshot(
                settingClues = PerformanceDiscoveryPolicy.settingClues("global", global) +
                    PerformanceDiscoveryPolicy.settingClues("secure", secure) +
                    PerformanceDiscoveryPolicy.settingClues("system", system),
                propertyClues = PerformanceDiscoveryPolicy.settingClues("property", props),
                paths = PerformanceDiscoveryPolicy.parsePathProbe(paths),
                thermalStatus = thermal,
                fixedPerformanceCommandExposed = powerHelp.contains("set-fixed-performance-mode-enabled"),
                trustedAdbAvailable = true,
                adbDetail = "Trusted local ADB read completed"
            )
        }
    } catch (error: Exception) {
        PerformanceCapabilitySnapshot(
            settingClues = emptyList(),
            propertyClues = emptyList(),
            paths = emptyList(),
            thermalStatus = thermal,
            fixedPerformanceCommandExposed = false,
            trustedAdbAvailable = false,
            adbDetail = error.message?.take(240) ?: error.javaClass.simpleName
        )
    }
}
```

`currentThermalStatusOrNull()` must use `PowerManager.currentThermalStatus` only on API 29+, returning null on API 28 or if the platform call fails. It must not register a listener in Stage 1.

- [ ] **Step 4: Convert the snapshot into existing read-only result cards**

`readResults()` must return these stable card labels:

```text
Performance probe / Trusted ADB read | ADB TURBO not authorised
Processor-mode clues / <matched setting/property summary> | No matching key exposed
CPU stock controls / <present paths and values>
GPU stock controls / <present paths and values>
Performance write access / <N writable of M present allowlisted paths>
Android thermal status / None|Light|Moderate|Severe|Critical|Emergency|Shutdown|Not exposed
Android fixed-performance / Command exposed (diagnostic only) | Not exposed
```

Use `ProbeStatus.AVAILABLE` for real readings, `RESTRICTED` when trusted ADB is unavailable, and `UNSUPPORTED` when the corresponding firmware surface is absent. Evidence text must identify the exact matched setting key or sysfs path but never include secrets or the local ADB key.

- [ ] **Step 5: Run full JVM tests**

```bash
gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --console=plain --stacktrace
```

Expected: GREEN.

- [ ] **Step 6: Commit Task 2**

```bash
git add shield-turbo/app/src/main/java/com/boop/shieldturbo/performance \
        shield-turbo/app/src/test/java/com/boop/shieldturbo/performance
git commit -m "feat(shield-turbo): probe stock performance capabilities"
```

---

### Task 3: Integrate capability results into the existing TURBO scan

**Files:**
- Modify: `shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt`
- Modify: `shield-turbo/app/src/main/res/values/strings.xml`
- Test: `shield-turbo/tests/test_performance_capability_contract.py`

**Interfaces:**
- Consumes: `PerformanceCapabilityProbe(context).readResults(): List<ProbeResult>`
- Produces: read-only capability cards appended to the existing `ANALYSE SHIELD` result list.

- [ ] **Step 1: Write the failing source contract before touching UI production code**

Create `shield-turbo/tests/test_performance_capability_contract.py` with these guards:

```python
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'app/src/main/java/com/boop/shieldturbo'
ANDROID = '{http://schemas.android.com/apk/res/android}'

class PerformanceCapabilityContractTest(unittest.TestCase):
    def test_stage_one_is_read_only_and_uses_trusted_adb(self):
        perf = '\n'.join(p.read_text() for p in (SOURCE / 'performance').glob('*.kt'))
        self.assertIn('withTrustedAdb', perf)
        self.assertNotIn('withAdb(', perf)
        self.assertNotIn('settings put', perf)
        self.assertNotIn('set-fixed-performance-mode-enabled true', perf)
        self.assertNotIn(' su ', f' {perf} ')
        self.assertNotRegex(perf, r'echo\s+[^\n]+>')

    def test_stage_one_adds_no_resident_performance_component(self):
        manifest = ET.parse(ROOT / 'app/src/main/AndroidManifest.xml').getroot()
        app = manifest.find('application')
        services = {s.get(ANDROID + 'name') for s in app.findall('service')}
        receivers = {r.get(ANDROID + 'name') for r in app.findall('receiver')}
        self.assertNotIn('.performance.ThermalWatchdogService', services)
        self.assertNotIn('.performance.PerformanceBootReceiver', receivers)
        permissions = {p.get(ANDROID + 'name') for p in manifest.findall('uses-permission')}
        self.assertNotIn('android.permission.FOREGROUND_SERVICE', permissions)

    def test_turbo_scan_surfaces_performance_capability_results(self):
        main = (SOURCE / 'MainActivity.kt').read_text()
        self.assertIn('PerformanceCapabilityProbe(context).readResults()', main)
        self.assertIn('snapshot.results + performance + privilege', main)

if __name__ == '__main__':
    unittest.main(verbosity=2)
```

- [ ] **Step 2: Run source contracts and verify RED**

```bash
python3 -m unittest discover -s shield-turbo/tests -p 'test_*.py' -v
```

Expected: the new test fails because `MainActivity` does not yet call `PerformanceCapabilityProbe`.

- [ ] **Step 3: Wire the probe into the existing worker-thread scan**

In `MainActivity.analyse()`, after the existing `ShieldAnalyzer(...).analyze()` call and before building the privilege card:

```kotlin
val performance = PerformanceCapabilityProbe(context).readResults()
```

Change the render input from:

```kotlin
render(snapshot.results + privilege)
```

to:

```kotlin
render(snapshot.results + performance + privilege)
```

Import `com.boop.shieldturbo.performance.PerformanceCapabilityProbe`.

Do not create another executor, timer, service, boot receiver, or continuous scanner.

- [ ] **Step 4: Add plain-English Stage-1 copy**

Add strings:

```xml
<string name="performance_probe_note">Performance capability is read-only in this build. TURBO has not changed processor, GPU, fan, governor or thermal settings.</string>
```

Append that message to the TURBO result list after the cards or include it as evidence on the `Performance probe` card. Keep the existing `scan_complete` wording `No changes applied` truthful.

- [ ] **Step 5: Run source contracts and full JVM suite**

```bash
python3 -m unittest discover -s shield-turbo/tests -p 'test_*.py' -v
gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --console=plain --stacktrace
```

Expected: GREEN.

- [ ] **Step 6: Run lint**

```bash
gradle --no-daemon -p shield-turbo :app:lintDebug --console=plain --stacktrace
```

Expected: 0 lint errors. Existing warning count may change only for legitimate new localization/unused-resource findings; review any new warning before proceeding.

- [ ] **Step 7: Verify frozen subsystems were untouched**

```bash
git diff --name-only 7ccd9443406e83084f2730a6838067a85985d91b..HEAD -- \
  shield-turbo/app/src/main/java/com/boop/shieldturbo/cleanstart \
  shield-turbo/app/src/main/java/com/boop/shieldturbo/BrightnessService.kt
```

Expected: no output.

- [ ] **Step 8: Commit Task 3**

```bash
git add shield-turbo/app/src/main/java/com/boop/shieldturbo/MainActivity.kt \
        shield-turbo/app/src/main/res/values/strings.xml \
        shield-turbo/tests/test_performance_capability_contract.py
git commit -m "feat(shield-turbo): show performance capability evidence"
```

---

### Task 4: Green gate, atomic v0.5.9 release stamp, and physical-evidence handoff

**Files:**
- Modify only after all feature tests are GREEN:
  - `shield-turbo/app/build.gradle`
  - `.github/workflows/shield-turbo.yml`
- Modify after the signed candidate is verified:
  - `shield-turbo/SESSION_HANDOFF.md`
  - `shield-turbo/STATUS.md`
  - `shield-turbo/MEMORY.md`

**Interfaces:**
- Produces: signed `com.boop.shieldturbo` v0.5.9 / versionCode 16 physical capability candidate.
- Physical output required for Plan 2: exact `Processor-mode clues`, `CPU stock controls`, `GPU stock controls`, `Performance write access`, `Android thermal status`, and `Android fixed-performance` cards from Ryan's Shield.

- [ ] **Step 1: Run the complete pre-stamp verification on the feature tree**

```bash
gradle --no-daemon -p shield-turbo :app:testDebugUnitTest --console=plain --stacktrace
python3 -m unittest discover -s shield-turbo/tests -p 'test_*.py' -v
gradle --no-daemon -p shield-turbo :app:lintDebug --console=plain --stacktrace
```

Expected: all tests pass and lint reports 0 errors.

- [ ] **Step 2: Review the feature diff for prohibited writes**

```bash
git diff 7ccd9443406e83084f2730a6838067a85985d91b..HEAD -- shield-turbo/app/src/main | \
  grep -Ei 'settings put|set-fixed-performance-mode-enabled true|su |echo .*>|scaling_(min|max)_freq.*>|governor.*>' && exit 1 || true
```

Also run:

```bash
git diff --name-only 7ccd9443406e83084f2730a6838067a85985d91b..HEAD -- \
  shield-turbo/app/src/main/java/com/boop/shieldturbo/cleanstart \
  shield-turbo/app/src/main/java/com/boop/shieldturbo/BrightnessService.kt
```

Expected: no frozen subsystem file changes.

- [ ] **Step 3: Stamp versionCode 16 / versionName 0.5.9 atomically**

In `shield-turbo/app/build.gradle`:

```groovy
versionCode 16
versionName '0.5.9'
```

In `.github/workflows/shield-turbo.yml`, change only the expected badging assertions:

```bash
grep -q "versionCode='16'" shield-turbo-badging.txt
grep -q "versionName='0.5.9'" shield-turbo-badging.txt
```

Commit only those two files:

```bash
git add shield-turbo/app/build.gradle .github/workflows/shield-turbo.yml
git commit -m "release(shield-turbo): stamp performance probe v0.5.9"
```

- [ ] **Step 4: Push and verify the GitHub Actions release run**

The run must pass:

- complete JVM suite;
- complete Python source contracts;
- lint with 0 errors;
- package `com.boop.shieldturbo`;
- versionCode 16 / versionName 0.5.9;
- permanent signer certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- APK ZIP integrity;
- nonvisual install/cold/warm launch/no-package-fatal smoke.

Do not infer physical capability findings from CI or emulator output.

- [ ] **Step 5: Download and independently verify the signed artifact**

Verify the GitHub artifact digest, extract exactly one APK, verify the APK SHA-256 against the workflow receipt, verify package/version with `aapt`, and verify the permanent signer with `apksigner`.

Deliver the candidate as `Shield-Turbo-v0.5.9.apk`.

- [ ] **Step 6: Update Turbo handoff/status/memory as physical-pending**

Record:

- exact built source commit;
- workflow run/job/artifact IDs and hashes;
- test totals and lint result;
- signer/package/version receipt;
- that v0.5.9 is **read-only capability discovery only**;
- that no performance write, watchdog or persistent TURBO exists yet;
- that CLEAN START and brightness files were unchanged;
- exact physical evidence requested below.

Commit docs only with `[skip ci]`, then verify live Turbo branch and `main` heads.

- [ ] **Step 7: Physical Shield checkpoint**

Install v0.5.9 over v0.5.8, open **TURBO**, run **ANALYSE SHIELD**, and report the exact text from these cards:

1. `Processor-mode clues`
2. `CPU stock controls`
3. `GPU stock controls`
4. `Performance write access`
5. `Android thermal status`
6. `Android fixed-performance`

Also report whether the scan remained responsive and whether Android displayed any unexpected permission/RSA prompt. Expected behavior: **no performance setting changes and no fresh ADB approval prompt**.

**Stop here.** Do not write Plan 2 or any performance-control production code until the real Shield output identifies an exact stock interface that can be read, changed, read back, restored, and re-verified without root.
